package jam.sort;

import jam.global.GoodThread;
import jam.sort.stream.EventException;
import jam.sort.stream.AbstractEventInputStream;
import jam.sort.stream.AbstractEventOutputStream;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

/**
 * General class to read and write event data streams from/to storage. For
 * online takes events from a <code>RingBuffer</code> and writes them out to a
 * storage device. For offline takes events from a storage device and pipes them
 * to an <code>EventInputStream</code>.
 * 
 * @author Ken Swartz
 * @version 0.5
 * @since JDK 1.1
 */
public abstract class AbstractStorageDaemon extends GoodThread {

	enum Mode {
		/**
		 * Mode value indicating this daemon is set for offline
		 */		
		OFFLINE, 
		/**
		 * Mode value indicating this daemon is set for online
		 */
		ONLINE
	};

	/**
	 * Number of buffers processed.
	 */
	protected int bufferCount = 0;

	/**
	 * Number of bytes processed.
	 */
	protected transient int byteCount = 0;

	/**
	 * Message handler for text output.
	 */
	//protected transient MessageHandler msgHandler;

	/**
	 * @see jam.SortControl
	 * @see jam.RunControl
	 */
	protected transient Controller controller;

	/** The input stream used to read events. */
	protected transient AbstractEventInputStream eventInput;

	/**
	 * The output stream used to write events.
	 */
	protected transient AbstractEventOutputStream eventOutput;

	/**
	 * Number of files processed.
	 */
	protected int fileCount = 0;

	/**
	 * File to grab data from.
	 */
	protected transient File inputFile;

	/**
	 * Whether we have an input file open.
	 */
	protected transient boolean inputFileOpen;

	protected transient Mode mode;

	/**
	 * File to save data to.
	 */
	protected File outputFile;

	/**
	 * Whether we have an output file open.
	 */
	protected transient boolean outputFileOpen;

	/**
	 * To process data from.
	 */
	protected transient RingBuffer ringBuffer;

	/**
	 * Iterator over the list of sort files.
	 */
	protected transient Iterator<File> sortFiles;

	/** files to sort in a list */
	private transient List<File> sortFilesList;

	/**
	 * Constructor for online and offline sorting and writing out data.
	 * 
	 * @param controller
	 *            the object controlling the sort process
	 * @param eventOutputStream
	 *            the stream to send events out to
	 * @param msgHandler
	 *            handle to the console for writing messages to the user
	 */
	AbstractStorageDaemon(Controller controller) {
		super();
		this.controller = controller;
		setPriority(ThreadPriorities.STORAGE);
		setDaemon(true);
		inputFileOpen = false;
		outputFileOpen = false;
	}

	/**
	 * Closes file that was written to during online sorting.
	 * 
	 * @exception SortException
	 *                thrown if an error occurs while closing file
	 */
	public abstract void closeEventInputFile() throws SortException;

	/**
	 * Closes the current file in the list, does not throw exception so it can
	 * be easily put in loop. Reports error itself if it could not close the
	 * file.
	 * 
	 * @return <code>true<code> if successful, <code>false</code> if not
	 */
	public abstract boolean closeEventInputListFile();

	/**
	 * Closes file that was written to during online sorting.
	 * 
	 * @exception SortException
	 *                thrown if an error occurs while closing file
	 */
	public abstract void closeEventOutputFile() throws SortException;

	/**
	 * Returns the number of buffers that have been processed.
	 * 
	 * @return the number of buffers that have been processed
	 */
	public int getBufferCount() {
		return bufferCount;
	}

	/**
	 * Returns the number of bytes processed.
	 * 
	 * @return the number of bytes processed
	 */
	public int getBytesCount() {
		return byteCount;
	}

	/**
	 * Returns the name of the current file being written to or read from.
	 * 
	 * @return the name of the current file being written to or read from
	 */
	public String getEventInputFileName() {
		return inputFile.getPath();
	}

	/**
	 * Returns the input stream of the storage file.
	 * 
	 * @return the input stream for the file being read from
	 * @exception SortException
	 *                thrown if an unrecoverable error occurs in a
	 *                <code>jam.sort</code> class
	 * @exception EventException
	 *                thrown if an unrecoverable error occurs in an event stream
	 *                class
	 */
	public abstract InputStream getEventInputFileStream() throws SortException,
			EventException;

	/**
	 * Returns the list of sort files to be sorted from.
	 * 
	 * @return the list of sort files to be sorted from
	 */
	public List<File> getEventInputList() {
		return sortFilesList;
	}

	/**
	 * Returns the input stream of the storage file.
	 * 
	 * @return the input stream for the file being read from
	 * @exception SortException
	 *                thrown if an unrecoverable error occurs in a
	 *                <code>jam.sort</code> class
	 * @exception EventException
	 *                thrown if an unrecoverable error occurs in an event stream
	 *                class
	 */
	public abstract OutputStream getEventOutputFileStream()
			throws SortException, EventException;

	/**
	 * Get the umber of files read or written.
	 * 
	 * @return number of files processed
	 */
	public int getFileCount() {
		return fileCount;
	}

	/**
	 * Returns whether there are more files in the list to sort.
	 * 
	 * @return <code>true</code> if there are more files, <code>false</code>
	 *         if not
	 */
	public abstract boolean hasMoreFiles();

	/**
	 * Opens a file using the given filename for writing to during online
	 * sorting.
	 * 
	 * @param file
	 *            the file to write to
	 * @exception SortException
	 *                thrown if an error condition occurs while attempting to
	 *                open file
	 */
	public abstract void openEventInputFile(File file) throws SortException;

	/**
	 * Open the next file in the list, does not throw exception so it can be
	 * easily put in loop. Reports error itself if it could not open file.
	 * 
	 * @return <code>true<code> if successful, <code>false</code> if not
	 */
	public abstract boolean openEventInputListFile();

	/**
	 * Opens a file using the given filename for writing to during online
	 * sorting.
	 * 
	 * @param file
	 *            the file to write to
	 * @exception SortException
	 *                thrown if an error condition occurs while attempting to
	 *                open file
	 */
	public abstract void openEventOutputFile(File file) throws SortException;

	/**
	 * Reads in a event record header and returns a status flag..
	 * 
	 * @return <code>true</code> if succesful, <code>false</code> if not
	 * @exception SortException
	 *                thrown if an unrecoverable error occurs in a
	 *                <code>jam.sort</code> class
	 * @exception EventException
	 *                thrown if an unrecoverable error occurs in an event stream
	 *                class
	 */

	public abstract boolean readHeader() throws SortException, EventException;

	/**
	 * Sets the number of buffers processed.
	 * 
	 * @param count
	 *            the number of buffers which have been processed
	 */
	public void setBufferCount(final int count) {
		bufferCount = count;
	}

	/**
	 * Sets the number of bytes processed.
	 * 
	 * @param count
	 *            the number of bytes processed
	 */
	public void setBytesCount(final int count) {
		byteCount = count;
	}

	/**
	 * Sets the list of files to sort from.
	 * 
	 * @param sortFilesList
	 *            the list of files to sort from
	 */
	public void setEventInputList(final List<File> sortFilesList) {
		this.sortFilesList = sortFilesList;
		sortFiles = sortFilesList.iterator();
	}

	/**
	 * Sets the number of files processed.
	 * 
	 * @param count
	 *            the number of files processed
	 */
	public void setFileCount(final int count) {
		fileCount = count;
	}

	/**
	 * Set the ring buffer to get data from.
	 * 
	 * @param ringBuffer
	 *            ring buffer to process data from
	 */
	public void setRingBuffer(final RingBuffer ringBuffer) {
		this.ringBuffer = ringBuffer;
	}

	/**
	 * Setup for offline sorting.
	 * 
	 * @param eventInputStream
	 *            type of incoming data
	 * @param eventOutputStream
	 *            type of outgoing data
	 */
	public void setupOff(final AbstractEventInputStream eventInputStream,
			final AbstractEventOutputStream eventOutputStream) {
		mode = Mode.OFFLINE;
		this.eventInput = eventInputStream;
		this.eventOutput = eventOutputStream;
	}

	/**
	 * Set storage daemon up for online sorting.
	 * 
	 * @param eventInputStream
	 *            needed in order to determine end-of-run
	 * @param eventOutputStream
	 *            presumably same format as input stream(?)
	 */
	public void setupOn(final AbstractEventInputStream eventInputStream,
			final AbstractEventOutputStream eventOutputStream) {
		mode = Mode.ONLINE;
		this.eventInput = eventInputStream;
		this.eventOutput = eventOutputStream;
	}

	/**
	 * Writes out an event record header.
	 * 
	 * @exception SortException
	 *                thrown if an unrecoverable error occurs in a
	 *                <code>jam.sort</code> class
	 * @exception EventException
	 *                thrown if an unrecoverable error occurs in an event stream
	 *                class
	 */
	public abstract void writeHeader() throws SortException, EventException;
}
