package jam.sort;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import jam.sort.stream.*;
import jam.global.*;

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

	static final int RECORD_SIZE = 8 * 1024;

	protected int mode;
	protected Controller controller;
	MessageHandler msgHandler;

	protected byte buffer[];
	protected RingBuffer ringBuffer;

	File inputFile;
	File outputFile;

	boolean inputFileOpen;
	boolean outputFileOpen;

	//files to sort in a list
	Vector sortFilesList;
	Enumeration sortFiles;

	protected FileOutputStream fos;
	protected BufferedOutputStream bos;
	protected DataOutputStream dos;
	protected EventOutputStream eventOutput;

	//for reading from device	        
	protected FileInputStream fis;
	protected BufferedInputStream bis;
	protected DataInputStream dis;
	protected EventInputStream eventInput;

	int bufferCount = 0;
	int byteCount = 0;
	int fileCount = 0;

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

		//priority one higher than display		    
		this.setPriority(7);
		this.setDaemon(true);
		inputFileOpen = false;
		outputFileOpen = false;
	}
	
	/**
	 * Set storage daemon up for online sorting.
	 * 
	 * @param eventInputStream needed in order to determine end-of-run
	 * @param eventOutputStream presumably same format as input stream(?)
	 */
	public void setupOn(EventInputStream eventInputStream,
	EventOutputStream eventOutputStream) {
		this.mode = ONLINE;
		this.eventInput = eventInputStream;
		this.eventOutput = eventOutputStream;
	}
	
	/**
	 * setup for offline sorting
	 */
	public void setupOff(
		EventInputStream eventInputStream,
		EventOutputStream eventOutputStream) {
		this.mode = OFFLINE;
		this.eventInput = eventInputStream;
		this.eventOutput = eventOutputStream;
	}
	
	/** 
	 * Set the ring buffer to get data from.
	 */
	public void setRingBuffer(RingBuffer ringBuffer) {
		this.ringBuffer = ringBuffer;
	}

	/**
	 * Sets the list of files to sort from.
	 *
	 * @param sortFilesList the list of files to sort from
	 */
	public void setEventInputList(Vector sortFilesList) {

		this.sortFilesList = sortFilesList;
		sortFiles = sortFilesList.elements();
	}

	/**
	 * Returns the list of sort files to be sorted from.
	 * 
	 * @return the list of sort files to be sorted from
	 */
	public Vector getEventInputList() {
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
	 * number of files read or written
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

	/** abstract methods */
	/** 
	 * Set path for a device if needed
	 */
	public abstract void setDevice(String dev);

	/**
	 * Opens a file using the given filename for writing to during online sorting.
	 * 
	 * @param fileName name of file to write to
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
	 * @param fileName name of file to write to
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
