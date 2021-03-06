package jam.sort.stream;

import java.io.IOException;

/**
 * This class knows how to handle Xsys event data format . It extends
 * EventInputStream, adding methods for reading events and returning them as int
 * arrays which the sorter can handle.
 * 
 * @version 0.5 April 98
 * @author Ken Swartz
 * @see AbstractEventInputStream
 * @since JDK1.1
 */
public class XsysInputStream extends AbstractEventInputStream {

	static private final int END_REC = 0x0a;

	/**
	 * @see AbstractEventInputStream#AbstractEventInputStream()
	 * 
	 */
	public XsysInputStream() {
		super();
	}

	/**
	 * @see AbstractEventInputStream#AbstractEventInputStream(boolean)
	 * @param console whether console exists
	 */
	public XsysInputStream(final boolean console) {
		super(console);
	}

	/**
	 * @see AbstractEventInputStream#AbstractEventInputStream(boolean, int)
	 * @param console whether console exists
	 * @param eventSize the number of signals per event
	 */
	public XsysInputStream(final boolean console, final int eventSize) {
		super(console, eventSize);
	}

	/**
	 * One allocation instead of many on readEvent() calls.
	 */
	private transient EventInputStatus eventInputStatus;// NOPMD

	/**
	 * Reads an event from the input stream Expects the stream position to be
	 * the beginning of an event. It is up to the user to ensure this.
	 * 
	 * @exception EventException
	 *                thrown when there is a problem with the event stream
	 */
	@Override
	public EventInputStatus readEvent(int[] input) throws EventException {
		synchronized (this) {
			eventInputStatus = EventInputStatus.ERROR;
			try {
				if (bufferCount < bufferSize) {
					for (int i = 0; i < eventSize; i++) {
						input[i] = readVaxShort();
						bufferCount++;
						if (input[i] < 0) {
							eventInputStatus = EventInputStatus.END_FILE;
							break; // jumps out of for-loop
						}
						eventInputStatus = EventInputStatus.EVENT;
					}
				} else {
					final int bufferMarker = dataInput.read();
					if (bufferMarker != END_REC) {
						throw new EventException(
								"Incorrect end of record marker");
					}
					eventInputStatus = EventInputStatus.END_BUFFER;
					bufferCount = 0;
				}
			} catch (IOException io) {
				eventInputStatus = EventInputStatus.ERROR;
				throw new EventException("Problem Reading Event.", io);

			}
			return eventInputStatus;
		}
	}

	/**
	 * Read a event stream header.
	 * 
	 * @exception EventException
	 *                thrown when there is a problem with the event stream
	 */
	@Override
	public boolean readHeader() throws EventException {
		headerRunNumber = 0;
		headerTitle = "No Title for Xsys";
		headerDate = "No Date";
		loadRunInfo();
		return true;
	}

	/**
	 * Is the word a end of run word Xsys event stream has no end of event
	 * marker
	 */
	@Override
	public boolean isEndRun(final short dataWord) {
		return false;
	}

	/*
	 * non-javadoc: reads a little endian short, 2 bytes
	 */
	private int readVaxShort() throws IOException {
		final int ch1 = dataInput.read();
		final int ch2 = dataInput.read();
		return ((ch1 | ch2) < 0) ? -1 : (ch2 << 8) + (ch1);
	}
}
