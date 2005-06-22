package jam.sort.stream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/** 
 * Stream to write events out 
 *
 * @author Ken Swartz
 * @version 0.5
 * @since JDK 1.1
 */
public abstract class EventOutputStream {

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

	/**
	 * The number of parameters per event.
	 */
	protected int eventSize;
	
	/**
	 * The number of bytes per event buffer.
	 */
	private int bufferSize;
	
	/**
	 * Where to write the data.
	 */
	protected DataOutputStream dataOutput;

	/** 
	 * Creates a new event output stream.
	 */
	protected EventOutputStream() {
		super();
	}

	/** 
	 * Creates a new event output stream with a given event size.
	 *
	 * @param eventSize the number of values per event
	 */
	protected EventOutputStream(int eventSize) {
		setEventSize(eventSize);
	}

	/**
	 * Sets the event size.
	 * @param size the number of values per event
	 */
	public final void setEventSize(int size) {
		eventSize = size;
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
	 * Sets the output stream where events and headers will be written.
	 *
	 * @param outputStream where events and headers will be written
	 */
	public void setOutputStream(OutputStream outputStream) {
		dataOutput = new DataOutputStream(outputStream);
	}

	/**
	 * Implemented for <code>OutputStream</code> requirement.
	 * @param word to write the output stream
	 * @throws IOException if there's a problem writing
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
	 * @throws EventException if there's a problem while writing the end-of-run stuff
	 */
	abstract public void writeEndRun() throws EventException;

	/**
	 * Checks whether the given <code>short</code> indicates the end-of-run.
	 * 
	 * @param event datum from the event stream
	 * @return <code>true</code> if the datum indicates we're at the end of the run
	 */
	abstract public boolean isEndRun(short event);
}
