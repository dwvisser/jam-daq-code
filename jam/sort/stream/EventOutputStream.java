package jam.sort.stream;
import java.io.*;

/** 
 * Stream to write events out 
 *
 * @author Ken Swartz
 * @version 0.5
 * @since JDK 1.1
 */
public abstract class EventOutputStream /*extends OutputStream*/ {

	//status variables

	/**
	 * Status value indicating event write ok.
	 */
	static public final int OK = 0;

	/**
	 * Status value indicating end of event reached.
	 */
	static public final int EVENT_END = 1;

	/**
	 * Status value indicating end of buffer reached.
	 */
	static public final int BUFFER_END = 2;

	/**
	 * Status value indicating end of run reached.
	 */
	static public final int RUN_END = 3;

	/**
	 * Status value indicating a problem writing the stream.
	 */
	static public final int ERROR = 10;

	protected int eventSize, bufferSize, headerSize;

	protected DataOutputStream dataOutput;

	//header information
	String headerKey;
	String headerTitle;
	public int headerNumber;
	int headerEventSize;

	/** 
	 * Creates a new event output stream.
	 */
	protected EventOutputStream() {

	}

	/** 
	 * Creates a new event output stream with a given event size.
	 *
	 * @param eventSize the number of values per event
	 */
	protected EventOutputStream(int eventSize) {
		this.eventSize = eventSize;
	}

	/**
	 * Sets the event size.
	 * @param size the number of values per event
	 */
	public void setEventSize(int size) {
		this.eventSize = size;
	}

	/**
	 * Returns the event size.
	 *
	 * @return the number of values per event
	 */
	public int getEventSize() {
		return eventSize;
	}

	/**
	 * Sets the buffer size.
	 *
	 * @param size the size of the output buffer in bytes
	 */
	public void setBufferSize(int size) {
		this.bufferSize = size;
	}

	/**
	 * Returns the buffer size.
	 *
	 * @return the size of the output buffer in bytes
	 */
	public int getBufferSize() {
		return bufferSize;
	}
	/**
	 *
	 */
	public int getHeaderSize() {
		return headerSize;
	}

	/**
	 * Sets the output stream where events and headers will be written.
	 *
	 * @param outputStream where events and headers will be written
	 */
	public void setOutputStream(OutputStream outputStream) {
		dataOutput = new DataOutputStream(outputStream);
	}

	/**
	 * Implemented for <code>OutputStream</code> requirement.
	 */
	public void write(int word) throws IOException {
		dataOutput.write(word);
	}

	/* Abstract methods of class */

	/**
	 * Writes the given array as an event to the output stream.
	 * 
	 * @param	input	event data
	 * @exception EventException thrown if there's an unrecoverable error writing the event
	 */
	abstract public void writeEvent(short[] input) throws EventException;

	/**
	 * Writes the given array as an event to the output stream.
	 * 
	 * @param	input	event data
	 * @exception EventException thrown if there's an unrecoverable error writing the event
	 */
	abstract public void writeEvent(int[] input) throws EventException;

	/**
	 * Writes a header to the output stream.
	 * 
	 * @exception EventException thrown if there's an unrecoverable error writing the header
	 */
	abstract public void writeHeader() throws EventException;

	/**
	 * Writes the end of run information, if any.
	 */
	abstract public void writeEndRun() throws EventException;

	/**
	 * Checks whether the given <code>short</code> indicates the end-of-run.
	 */
	abstract public boolean isEndRun(short event);
}
