package jam.sort;

import jam.global.MessageHandler;
import jam.sort.stream.EventException;
import jam.util.NumberUtilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;

/**
 * Writes events to disk and reads them back.
 * 
 * @author Ken Swartz
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 */
public final class DiskDaemon extends AbstractStorageDaemon {

	private transient FileOutputStream fos;

	private transient BufferedOutputStream bos;

	private transient FileInputStream fis;

	private transient BufferedInputStream bis;

	/**
	 * @see AbstractStorageDaemon#StorageDaemon(Controller, MessageHandler)
	 */
	public DiskDaemon(Controller controller, MessageHandler msgHandler) {
		super(controller, msgHandler);
		setName("Disk I/O for Event Data");
	}

	/**
	 * Open file to write events to.
	 * 
	 * @exception SortException
	 *                exception that sends message to console
	 */
	public void openEventInputFile(final File file) throws SortException {
		if (file == null) {
			throw new SortException(getClass().getName()
					+ ": Cannot open input event file, null name.");
		}
		try {
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis, RingBuffer.BUFFER_SIZE);
			eventInput.setInputStream(bis);
			this.inputFile = file;
			inputFileOpen = true;
		} catch (IOException ioe) {
			throw new SortException("Unable to open file: " + file.getPath()
					+ " [DiskDaemon]");
		}
	}

	/**
	 * Open file to write events to.
	 * 
	 * @exception SortException
	 *                exception that sends message to console
	 */
	public void openEventOutputFile(final File file) throws SortException {
		if (file == null) {
			throw new SortException(getClass().getName()
					+ ": Cannot open output event file, file name is null.");
		}
		try {
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos, RingBuffer.BUFFER_SIZE);
			eventOutput.setOutputStream(bos);
			this.outputFile = file;
			outputFileOpen = true;
		} catch (IOException ioe) {
			throw new SortException("Unable to open file: " + file.getPath()
					+ " [DiskDaemon]");
		}
	}

	/**
	 * Closes file that was written to or read from.
	 * 
	 * @exception SortException
	 *                exception that sends message to console
	 */
	public void closeEventOutputFile() throws SortException {
		if (outputFileOpen) {
			try {
				if (mode == Mode.OFFLINE) {
					eventOutput.writeEndRun();
				}
				bos.flush();
				fos.close();
				outputFileOpen = false;
			} catch (EventException ee) {
				throw new SortException("Unable to close file EventException:"
						+ ee.getMessage() + " [DiskDaemon]");
			} catch (IOException ioe) {
				throw new SortException("Unable to close file [DiskDaemon]");
			}
		}
	}

	/**
	 * Closes file that was written to or read from.
	 * 
	 * @exception SortException
	 *                exception that sends message to console
	 */
	public void closeEventInputFile() throws SortException {
		if (inputFileOpen) {
			try {
				fis.close();
				inputFileOpen = false;
			} catch (IOException ioe) {
				throw new SortException("Unable to close file [DiskDaemon]");
			}
		}
	}

	private transient boolean reachedRunEnd = false;

	private transient final Object rreLock = new Object();

	/*
	 * non-javadoc: Take data from ring buffer and write it out to a file until
	 * you see a end of run marker, then inform controller.
	 */
	private void writeLoop() throws IOException {
		final byte[] buffer = RingBuffer.freshBuffer();
		/*
		 * checkState() waits until state is STOP (return value=false) or RUN
		 * (return value=true)
		 */
		while (checkState()) {
			// read from pipe and write file
			ringBuffer.getBuffer(buffer);
			bos.write(buffer);
			bufferCount++;
			// check for end-of-run marker
			final int offset = buffer.length - 2;
			final short last2bytes = NumberUtilities.getInstance()
					.bytesToShort(buffer, offset, ByteOrder.BIG_ENDIAN);
			if (eventInput.isEndRun(last2bytes)) {
				// tell control we are done
				fileCount++;
				synchronized (rreLock) {
					reachedRunEnd = true;
				}
				controller.atWriteEnd();
			}
			yield();
		}
		// end loop forever
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
		boolean rval = false;
		final File file = sortFiles.next();
		try {
			openEventInputFile(file);// local open file method
			rval = eventInput.readHeader();
			if (rval) {
				fileCount++;
			} else {
				msgHandler
						.errorOutln("File does not have correct header. File: "
								+ file.getAbsolutePath());
			}
		} catch (EventException ee) {
			msgHandler.errorOutln(ee.getMessage());
			rval = false;
		} catch (SortException je) {
			msgHandler.errorOutln(je.getMessage());
			rval = false;
		}
		return rval;
	}

	/**
	 * Close event input file that is from the list, if one from the list is
	 * open.
	 */
	public boolean closeEventInputListFile() {
		boolean rval = true;
		try {
			closeEventInputFile();
		} catch (SortException ioe) {
			msgHandler.errorOutln(getClass().getName()
					+ ": Unable to close file: " + inputFile.getPath());
			rval = false;
		}
		return rval;
	}

	/**
	 * Implementation of <code>StorageDaemon</code> abstract method.
	 * 
	 * @exception SortException
	 *                thrown for unrecoverable errors
	 */
	public InputStream getEventInputFileStream() throws SortException {
		return bis;
	}

	/**
	 * Implementation of <code>StorageDaemon</code> abstract method.
	 * 
	 * @exception SortException
	 *                thrown for unrecoverable errors
	 */
	public OutputStream getEventOutputFileStream() throws SortException {
		return bos;
	}

	/**
	 * Implementation of <code>StorageDaemon</code> abstract method.
	 * 
	 * @exception SortException
	 *                thrown for unrecoverable errors
	 */
	public void writeHeader() throws SortException {
		try {
			eventOutput.writeHeader();
		} catch (EventException ioe) {
			throw new SortException(
					"Could not write Header Record [DiskDaemon]");
		}
	}

	/**
	 * Implementation of <code>StorageDaemon</code> abstract method.
	 * 
	 * @exception SortException
	 *                thrown for unrecoverable errors
	 */
	public boolean readHeader() throws SortException {
		final BufferedInputStream headerInputStream = new BufferedInputStream(
				fis, RingBuffer.BUFFER_SIZE);
		try {
			eventInput.setInputStream(headerInputStream);
			final boolean goodHeader = eventInput.readHeader();
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
			if (mode == Mode.ONLINE) {
				writeLoop();
			} else {
				throw new IllegalStateException(
						"run() called when mode not ONLINE");
			}
		} catch (IOException ioe) {
			msgHandler
					.errorOutln("[DiskDaemon] Error while writing data to file: "
							+ ioe.getMessage());
		}
	}

	/**
	 * Returns whether online sorting is all caught up with incoming buffers.
	 * 
	 * @return <code>true</code> if all received buffers have been sorted
	 */
	public boolean caughtUpOnline() {
		if (ringBuffer == null) {
			throw new IllegalStateException(
					"Should always have a ring buffer here.");
		}
		boolean rval = false;
		if (ringBuffer.isEmpty()) {
			synchronized (rreLock) {
				if (reachedRunEnd) {
					rval = true;
				}
			}
		}
		return rval;
	}

	/**
	 * Reset the "reached run end" state to <code>false</code>.
	 * 
	 */
	public void resetReachedRunEnd() {
		synchronized (rreLock) {
			reachedRunEnd = false;
		}
	}
}
