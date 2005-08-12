package jam.sort;
import jam.global.GoodThread;
import jam.global.MessageHandler;
import jam.sort.stream.EventException;
import jam.sort.stream.AbstractEventInputStream;
import jam.sort.stream.AbstractEventOutputStream;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

/**
 * General class to read and write event data streams from/to storage.
 * For online takes events from a <code>RingBuffer</code> and writes
 * them out to a storage device.
 * For offline takes events from a storage device and
 * pipes them to an <code>EventInputStream</code>.
 *
 * @author Ken Swartz
 * @version 0.5
 * @since JDK 1.1
 */
public abstract class StorageDaemon extends GoodThread {

	/**
	 * Mode value indicating this daemon is set for online 
	 */
	public static final int ONLINE = 1;
	/**
	 * Mode value indicating this daemon is set for offline 
	 */
	public static final int OFFLINE = 2;

	/**
	 * @see #ONLINE
	 * @see #OFFLINE
	 */
	protected int mode;
	
	/**
	 * @see jam.SortControl
	 * @see jam.RunControl
	 */
	protected Controller controller;
	
	/**
	 * Message handler for text output.
	 */
	protected MessageHandler msgHandler;

	/**
	 * To process data from.
	 */
	protected RingBuffer ringBuffer;

	/**
	 * File to grab data from.
	 */
	protected File inputFile;
	
	/**
	 * File to save data to.
	 */
	protected File outputFile;

	/**
	 * Whether we have an input file open.
	 */
	protected boolean inputFileOpen;
	
	/**
	 * Whether we have an output file open.
	 */
	protected boolean outputFileOpen;

	/** files to sort in a list */
	private List sortFilesList;
	
	/**
	 * Iterator over the list of sort files.
	 */
	protected Iterator sortFiles;

	/**
	 * The output stream used to write events.
	 */
	protected AbstractEventOutputStream eventOutput;

	/** The input stream used to read events. */       
	protected AbstractEventInputStream eventInput;

	/**
	 * Number of buffers processed.
	 */
	protected int bufferCount = 0;
	
	/** 
	 * Number of bytes processed.
	 */
	protected int byteCount = 0;
	
	/**
	 * Number of files processed.
	 */
	protected int fileCount = 0;

	/**
	 * Constructor for online and offline sorting and writing out data.
	 * 
	 * @param controller the object controlling the sort process
	 * @param eventOutputStream the stream to send events out to
	 * @param msgHandler handle to the console for writing messages to the user
	 */
	StorageDaemon(Controller controller, MessageHandler msgHandler) {
		this.controller = controller;
		this.msgHandler = msgHandler;
		setPriority(ThreadPriorities.STORAGE);//normally 7-9 reserved for
		setDaemon(true);
		inputFileOpen = false;
		outputFileOpen = false;
	}
	
	/**
	 * Set storage daemon up for online sorting.
	 * 
	 * @param eventInputStream needed in order to determine end-of-run
	 * @param eventOutputStream presumably same format as input stream(?)
	 */
	public void setupOn(AbstractEventInputStream eventInputStream,
	AbstractEventOutputStream eventOutputStream) {
		this.mode = ONLINE;
		this.eventInput = eventInputStream;
		this.eventOutput = eventOutputStream;
	}
	
	/**
	 * Setup for offline sorting.
	 * 
	 * @param eventInputStream type of incoming data
	 * @param eventOutputStream type of outgoing data
	 */
	public void setupOff(
		AbstractEventInputStream eventInputStream,
		AbstractEventOutputStream eventOutputStream) {
		this.mode = OFFLINE;
		this.eventInput = eventInputStream;
		this.eventOutput = eventOutputStream;
	}
	
	/** 
	 * Set the ring buffer to get data from.
	 * 
	 * @param ringBuffer ring buffer to process data from
	 */
	public void setRingBuffer(RingBuffer ringBuffer) {
		this.ringBuffer = ringBuffer;
	}

	/**
	 * Sets the list of files to sort from.
	 *
	 * @param sortFilesList the list of files to sort from
	 */
	public void setEventInputList(List sortFilesList) {
		this.sortFilesList = sortFilesList;
		sortFiles = sortFilesList.iterator();
	}

	/**
	 * Returns the list of sort files to be sorted from.
	 * 
	 * @return the list of sort files to be sorted from
	 */
	public List getEventInputList() {
		return sortFilesList;
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
	 * Returns the number of buffers that have been processed.
	 *
	 * @return the number of buffers that have been processed
	 */
	public int getBufferCount() {
		return bufferCount;
	}

	/**
	 * Sets the number of buffers processed.
	 * 
	 * @param count the number of buffers which have been processed
	 */
	public void setBufferCount(int count) {
		bufferCount = count;
	}

	/**
	 * Get the umber of files read or written.
	 * 
	 * @return number of files processed
	 */
	public int getFileCount() {
		return fileCount;
	}

	/**
	 * Sets the number of files processed.
	 *
	 * @param count the number of files processed
	 */
	public void setFileCount(int count) {
		fileCount = count;
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
	 * Sets the number of bytes processed.
	 *
	 * @param count the number of bytes processed
	 */
	public void setBytesCount(int count) {
		byteCount = count;
	}

	/**
	 * Opens a file using the given filename for writing to during online sorting.
	 * 
	 * @param file the file to write to
	 * @exception SortException thrown if an error condition occurs while attempting to open file
	 */
	public abstract void openEventOutputFile(File file)
		throws SortException;

	/**
	 * Closes file that was written to during online sorting.
	 *
	 * @exception SortException thrown if an error occurs while closing file
	 */
	public abstract void closeEventOutputFile() throws SortException;
	
	/**
	 * Opens a file using the given filename for writing to during online sorting.
	 * 
	 * @param file the file to write to
	 * @exception SortException thrown if an error condition occurs while attempting to open file
	 */
	public abstract void openEventInputFile(File file)
		throws SortException;

	/**
	 * Closes file that was written to during online sorting.
	 *
	 * @exception SortException thrown if an error occurs while closing file
	 */
	public abstract void closeEventInputFile() throws SortException;

	/**
	 * Returns whether there are more files in the list to sort.
	 * 
	 * @return <code>true</code> if there are more files, <code>false</code> if not
	 */
	public abstract boolean hasMoreFiles();

	/**
	 * Open the next file in the list, does not throw
	 * exception so it can be easily put in loop. 
	 * Reports error itself if it could not open file.
	 * 
	 * @return <code>true<code> if successful, <code>false</code> if not
	 */
	public abstract boolean openEventInputListFile();

	/**
	 * Closes the current file in the list, does not throw
	 * exception so it can be easily put in loop. 
	 * Reports error itself if it could not close the file.
	 * 
	 * @return <code>true<code> if successful, <code>false</code> if not
	 */
	public abstract boolean closeEventInputListFile();

	/**
	 * Returns the input stream of the storage file.
	 *
	 * @return the input stream for the file being read from
	 * @exception SortException thrown if an unrecoverable error occurs in a <code>jam.sort</code> class
	 * @exception EventException thrown if an unrecoverable error occurs in an event stream class
	 */
	public abstract InputStream getEventInputFileStream()
		throws SortException, EventException;

	/**
	 * Returns the input stream of the storage file.
	 *
	 * @return the input stream for the file being read from
	 * @exception SortException thrown if an unrecoverable error occurs in a <code>jam.sort</code> class
	 * @exception EventException thrown if an unrecoverable error occurs in an event stream class
	 */
	public abstract OutputStream getEventOutputFileStream()
		throws SortException, EventException;

	/**
	 * Writes out an event record header.
	 *
	 * @exception SortException thrown if an unrecoverable error occurs in a <code>jam.sort</code> class
	 * @exception EventException thrown if an unrecoverable error occurs in an event stream class
	 */
	public abstract void writeHeader() throws SortException, EventException;

	/**
	 * Reads in a event record header and returns a status flag..
	 * 
	 * @return <code>true</code> if succesful, <code>false</code> if not
	 * @exception SortException thrown if an unrecoverable error occurs in a <code>jam.sort</code> class
	 * @exception EventException thrown if an unrecoverable error occurs in an event stream class
	 */

	public abstract boolean readHeader() throws SortException, EventException;
}
