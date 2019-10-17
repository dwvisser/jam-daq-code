package jam.sort.stream;

import java.io.EOFException;

import static jam.sort.stream.L002Parameters.*;

/**
 * This class knows how to handle Oak Ridge tape format.
 * 
 * @version 0.5 April 98
 * @author Dale Visser, Ken Swartz
 * @see AbstractEventInputStream
 * @since JDK1.1
 */
public class L00XInputStream extends AbstractL002HeaderReader {

	// make sure to issue a setConsole() after using this constructor
	// It is here to satisfy the requirements of Class.newInstance()
	/**
	 * Called by Jam to create an instance of this input stream.
	 */
	public L00XInputStream() {
		super();
	}

	/**
	 * Default constructor.
	 * 
	 * @param console
	 *            object where messages to the user are printed
	 */
	public L00XInputStream(final boolean console) {
		super(console);
	}

	/**
	 * Creates the input stream given an event size.
	 * 
	 * @param eventSize
	 *            number of parameters per event.
	 * @param console
	 *            object where messages to the user are printed
	 */
	public L00XInputStream(final boolean console, final int eventSize) {
		super(console, eventSize);
	}

	@Override
	public String getFormatDescription() {
		return "A later modification, used by UConn, I think.";
	}

	/**
	 * Reads an event from the input stream Expects the stream position to be
	 * the beginning of an event. It is up to the user to ensure this.
	 * 
	 * @param input
	 *            source of event data
	 * @exception EventException
	 *                thrown for errors in the event stream
	 * @return status after attempt to read an event
	 */
	@Override
	public EventInputStatus readEvent(int[] input) throws EventException {
		synchronized (this) {
			boolean gotParameter = false;
			try {
				while (isParameter(dataInput.readShort())) {// could be event or
					// scaler parameter
					gotParameter = true;
					if (status == EventInputStatus.PARTIAL_EVENT) {
						if (parameter >= eventSize) {// skip, since array
							// index would be too
							// great for event array
							dataInput.readShort();
						} else {// read into array
							input[parameter] = dataInput.readShort(); // read
							// event
							// word
						}
					} else if (status == EventInputStatus.SCALER_VALUE) {
						dataInput.readInt();// throw away scaler value
					}
				}
			} catch (EOFException eofe) {// we got to the end of a file or
				// stream
				status = EventInputStatus.END_FILE;
				LOGGER
						.warning(getClass().getName()
								+ ".readEvent(): End of File reached...file may be corrupted, or run not ended properly.");
			} catch (Exception e) {
				status = EventInputStatus.UNKNOWN_WORD;
				throw new EventException(getClass().getName()
						+ ".readEvent() parameter = " + parameter, e);
			}
			if (!gotParameter && status == EventInputStatus.EVENT) {
				status = EventInputStatus.IGNORE;
			}
			return status;
		}
	}

	/*
	 * non-javadoc: Read an event parameter.
	 */
	private boolean isParameter(final short paramWord) {
		boolean parameterSuccess;
		// check special types parameter
		if (paramWord == EVENT_END_MARKER) {
			parameterSuccess = false;
			status = EventInputStatus.EVENT;
		} else if (paramWord == BUFFER_END_MARKER) {
			parameterSuccess = false;
			status = EventInputStatus.END_BUFFER;
		} else if (paramWord == RUN_END_MARKER) {
			parameterSuccess = false;
			status = EventInputStatus.END_RUN;
			// get parameter value if not special type
		} else if ((paramWord & EVENT_PARAMETER) != 0) {// NOPMD
			final int paramNumber = paramWord & EVENT_MASK;
			if (paramNumber < 2048) {
				parameter = paramNumber - 1;// parameter number used in array
				parameterSuccess = true;
				status = EventInputStatus.PARTIAL_EVENT;
			} else {// 2048-4095 assumed
				parameterSuccess = true;
				status = EventInputStatus.SCALER_VALUE;
			}
		} else {// unknown word
			parameter = paramWord;
			parameterSuccess = false;
			status = EventInputStatus.UNKNOWN_WORD;
		}
		return parameterSuccess;
	}

	/**
	 * Check for end of run word
	 * 
	 * @param dataWord
	 *            smallest atomic unit in data stream
	 * @return whether the data word was an end-of-run word
	 */
	@Override
	public boolean isEndRun(final short dataWord) {
		synchronized (this) {
			return (dataWord == RUN_END_MARKER);
		}
	}
}
