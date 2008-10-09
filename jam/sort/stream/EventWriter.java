package jam.sort.stream;

import java.io.IOException;

/**
 * Writes events out to a stream.
 * 
 * @author Dale Visser
 * 
 */
public interface EventWriter {

	/**
	 * Writes the given array as an event to the output stream.
	 * 
	 * @param input
	 *            event data
	 * @exception IOException
	 *                thrown if there's an unrecoverable error writing the event
	 */
	void writeEvent(short[] input) throws IOException;

	/**
	 * Writes the given array as an event to the output stream.
	 * 
	 * @param input
	 *            event data
	 * @exception IOException
	 *                thrown if there's an unrecoverable error writing the event
	 */
	void writeEvent(int[] input) throws IOException;
}