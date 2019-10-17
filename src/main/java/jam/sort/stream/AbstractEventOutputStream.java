package jam.sort.stream;

import java.io.DataOutputStream;
import java.io.OutputStream;

/**
 * Stream to write events out
 * 
 * @author Ken Swartz
 * @version 0.5
 * @since JDK 1.1
 */
public abstract class AbstractEventOutputStream implements EventWriter {

	/**
	 * Where to write the data.
	 */
	protected transient DataOutputStream dataOutput;

	/**
	 * The number of parameters per event.
	 */
	protected int eventSize;

	/**
	 * Creates a new event output stream.
	 */
	protected AbstractEventOutputStream() {
		super();
	}

	/**
	 * Creates a new event output stream with a given event size.
	 * 
	 * @param eventSize
	 *            the number of values per event
	 */
	protected AbstractEventOutputStream(final int eventSize) {
		super();
		setEventSize(eventSize);
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
	 * Checks whether the given <code>short</code> indicates the end-of-run.
	 * 
	 * @param event
	 *            datum from the event stream
	 * @return <code>true</code> if the datum indicates we're at the end of the
	 *         run
	 */
	abstract public boolean isEndRun(short event);

	/**
	 * Sets the event size.
	 * 
	 * @param size
	 *            the number of values per event
	 */
	public final void setEventSize(final int size) {
		eventSize = size;
	}

	/**
	 * Sets the output stream where events and headers will be written.
	 * 
	 * @param outputStream
	 *            where events and headers will be written
	 */
	public void setOutputStream(final OutputStream outputStream) {
		dataOutput = new DataOutputStream(outputStream);
	}

	/**
	 * Writes the end of run information, if any.
	 * 
	 * @throws EventException
	 *             if there's a problem while writing the end-of-run stuff
	 */
	abstract public void writeEndRun() throws EventException;

	/**
	 * Writes a header to the output stream.
	 * 
	 * @exception EventException
	 *                thrown if there's an unrecoverable error writing the
	 *                header
	 */
	abstract public void writeHeader() throws EventException;
}
