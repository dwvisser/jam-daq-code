package jam.sort;
import jam.global.MessageHandler;
import jam.sort.stream.EventException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Writes events to disk and reads them back.
 *
 * @author Ken Swartz
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 */
public class DiskDaemon extends StorageDaemon {

	/**
	 * Constructor for online sorting to write out data.
	 */
	public DiskDaemon(Controller controller, MessageHandler msgHandler) {
		super(controller, msgHandler);
		setName("Disk I/O for Event Data");
	}
	
	/**
	 * Open file to write events to.
	 *
	 * @exception   SortException    exception that sends message to console
	 */
	public void openEventInputFile(File file) throws SortException {
		if (file == null)
			throw new SortException("Cannot open input event file, null name [DiskDaemon]");
		try {
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis, RingBuffer.BUFFER_SIZE);
			eventInput.setInputStream(bis);
			this.inputFile = file;
			inputFileOpen = true;
		} catch (IOException ioe) {
			throw new SortException(
				"Unable to open file: " + file.getPath() + " [DiskDaemon]");
		}
	}

	/**
	 * Open file to write events to.
	 *
	 * @exception   SortException    exception that sends message to console
	 */
	public void openEventOutputFile(File file) throws SortException {
		if (file == null)
			throw new SortException("Cannot open output event file, file name is null [DiskDaemon]");
		try {
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos, RingBuffer.BUFFER_SIZE);
			eventOutput.setOutputStream(bos);
			this.outputFile = file;
			outputFileOpen = true;
		} catch (IOException ioe) {
			throw new SortException(
				"Unable to open file: " + file.getPath() + " [DiskDaemon]");
		}
	}

	/**
	 * Closes file that was written to or read from.
	 * 
	 * @exception   SortException    exception that sends message to console
	 */
	public void closeEventOutputFile() throws SortException {
		if (outputFileOpen) {
			try {
				if (mode == OFFLINE) {
					eventOutput.writeEndRun();
				}
				bos.flush();
				fos.close();
				this.outputFile = null;
				outputFileOpen = false;
			} catch (EventException ee) {
				throw new SortException(
					"Unable to close file EventException:"
						+ ee.getMessage()
						+ " [DiskDaemon]");
			} catch (IOException ioe) {
				throw new SortException("Unable to close file [DiskDaemon]");
			}
		}
	}

	/**
	 * Closes file that was written to or read from.
	 * 
	 * @exception   SortException    exception that sends message to console
	 */
	public void closeEventInputFile() throws SortException {
		if (inputFileOpen) {
			try {
				fis.close();
				this.inputFile = null;
				inputFileOpen = false;
			} catch (IOException ioe) {
				throw new SortException("Unable to close file [DiskDaemon]");
			}
		}
	}
	
	private boolean reachedRunEnd=false;
	private final Object rreLock=new Object();
	
	/**
	 * Take data from ring buffer and write it out to a file 
	 * until you see a end of run marker, then inform controller
	 */
	private void writeLoop() throws IOException {
		final byte [] buffer=RingBuffer.freshBuffer();
		/* checkState() waits until state is STOP (return value=false)
		 * or RUN (return value=true) */	
		while (checkState()) {
			//read from pipe and write file
			ringBuffer.getBuffer(buffer);
			final int bytesIn = buffer.length;
			bos.write(buffer, 0, bytesIn);
			bufferCount++;
			//check for end-of-run marker			    		    		
			final short last2bytes =
				(short) (((buffer[bytesIn - 2] & 0xFF) << 8)
					+ (buffer[bytesIn - 1] & 0xFF));
			if (eventInput.isEndRun(last2bytes)) {
				//tell control we are done		    
				fileCount++;
				synchronized(rreLock){
					reachedRunEnd=true;
				}
				controller.atWriteEnd();
			}
			yield();
		}
		//end loop forever 
	}

	/* implementations of StorageDeamon abstract methods */

	/**
	 * Need to implement such that sets a variable to stop write loop.
	 */
	public boolean hasMoreFiles() {
		return sortFiles.hasNext();
	}

	/**
	 * Open next file in list.
	 */
	public boolean openEventInputListFile() {
		boolean goodHeader = false;
		final File file = (File)sortFiles.next();
		try {
			openEventInputFile(file);// local open file method
			goodHeader = eventInput.readHeader();
			if (goodHeader) {
				fileCount++;
			} else {
				msgHandler.errorOutln(
					"File does not have correct header. File: " + file.getAbsolutePath());
			}
			return goodHeader;
		} catch (EventException ee) {
			msgHandler.errorOutln(ee.getMessage());
			return false;
		} catch (SortException je) {
			msgHandler.errorOutln(je.getMessage());
			return false;
		}
	}
	
	/**
	 * Close event input file that is from the list,
	 * if one from the list is open.
	 */
	public boolean closeEventInputListFile() {
		try {
			closeEventInputFile();
			return true;
		} catch (SortException ioe) {
			msgHandler.errorOutln(
				"Unable to close file: " + inputFile.getPath() + "[DiskDaemon]");
			return false;
		}
	}

	/**
	 * Implementation of <code>StorageDaemon</code> abstract method.
	 *
	 * @exception SortException thrown for unrecoverable errors
	 */
	public InputStream getEventInputFileStream() throws SortException {
		return bis;
	}

	/**
	 * Implementation of <code>StorageDaemon</code> abstract method.
	 *
	 * @exception SortException thrown for unrecoverable errors
	 */
	public OutputStream getEventOutputFileStream() throws SortException {
		return bos;
	}

	/**
	 * Implementation of <code>StorageDaemon</code> abstract method.
	 *
	 * @exception SortException thrown for unrecoverable errors
	 */
	public void writeHeader() throws SortException {
		try {
			eventOutput.writeHeader();
		} catch (EventException ioe) {
			throw new SortException("Could not write Header Record [DiskDaemon]");
		}
	}

	/**
	 * Implementation of <code>StorageDaemon</code> abstract method.
	 *
	 * @exception SortException thrown for unrecoverable errors
	 */
	public boolean readHeader() throws SortException {
		final BufferedInputStream headerInputStream =
			new BufferedInputStream(fis, RingBuffer.BUFFER_SIZE);
		try {
			eventInput.setInputStream(headerInputStream);
			boolean goodHeader = eventInput.readHeader();
			return goodHeader;
		} catch (EventException ioe) {
			throw new SortException("Could not read Header Record [DiskDaemon]");
		}

	}

	/**
	 * Starting point of thread for online writing to disk
	 */
	public void run() {
		try {
			if (mode == ONLINE) {
				writeLoop();
			} else {
				throw new IllegalStateException(
					"run() called when mode not ONLINE");
			}
		} catch (IOException ioe) {
			msgHandler.errorOutln(
				"[DiskDaemon] Error while writing data to file: "
					+ ioe.getMessage());
		}
	}
	
	public boolean caughtUpOnline(){
		if (ringBuffer == null){
			throw new IllegalStateException("Should always have a ring buffer here.");
		}
		boolean rval=false;
		if (ringBuffer.isEmpty()){
			synchronized (rreLock){
				if (reachedRunEnd){
					rval=true;
				}
			}
		}
		return rval;
	}
	
	public void resetReachedRunEnd(){
		synchronized(rreLock){
			reachedRunEnd=false;
		}
	}
}
