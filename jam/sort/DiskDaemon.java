package jam.sort;
import java.io.*;
import jam.sort.stream.*;
import jam.global.*;

/**
 * Writes events to disk and reads them back.
 *
 * @author Ken Swartz
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 */
public class DiskDaemon extends StorageDaemon {

	String device; //not actually used
	/**
	 * Constructor for online sorting to write out data.
	 */
	public DiskDaemon(Controller controller, MessageHandler msgHandler) {
		super(controller, msgHandler);
	}
	/**
	 * set the path of the tape device 
	 */
	public void setDevice(String dev) {
		device = dev;
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
			bis = new BufferedInputStream(fis, RECORD_SIZE);
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
			bos = new BufferedOutputStream(fos, RECORD_SIZE);
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
				//unnecessary, I think eventOutput.flush();
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
	/**
	 * Take data from ring buffer and write it out to a file 
	 * until you see a end of run marker, then inform controller
	 */
	private void writeLoop() throws IOException {
		int bytesIn;
		short last2bytes;
		boolean endRun;

		//checkState() waits until state is STOP or RUN to return a value		
		while (this.checkState()) {
			//read from pipe and write file
			buffer = ringBuffer.getBuffer();
			bytesIn = buffer.length;
			bos.write(buffer, 0, bytesIn);
			bufferCount++;
			//check for end-of-run marker			    		    		
			last2bytes =
				(short) (((buffer[bytesIn - 2] & 0xFF) << 8)
					+ (buffer[bytesIn - 1] & 0xFF));
			if (endRun = eventInput.isEndRun(last2bytes)) {
				//tell control we are done		    
				fileCount++;
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
		String fileName = (String) (sortFiles.next());
		// local open file method
		try {
			openEventInputFile(new File(fileName));
			goodHeader = eventInput.readHeader();
			if (goodHeader) {
				fileCount++;
			} else {
				msgHandler.errorOutln(
					"File does not have correct header. File: " + fileName);
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
	 * close event input file that is from the list
	 * only close if one from the list is open
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

		int headerSize = RECORD_SIZE;
		boolean goodHeader = false;
		BufferedInputStream headerInputStream =
			new BufferedInputStream(fis, headerSize);

		try {

			eventInput.setInputStream(headerInputStream);
			goodHeader = eventInput.readHeader();
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
				System.err.println(
					"Error: diskdaemon should not be here no read"
						+ " loop [DiskDaemon]");
			}
		} catch (IOException ioe) {
			msgHandler.errorOutln(
				"[DiskDaemon] Error while writing data to file: "
					+ ioe.getMessage());
		}
	}
}
