package jam.sort.stream;

import jam.global.MessageHandler;

import java.io.EOFException;

/**
 * This class knows how to handle Oak Ridge tape format. It extends
 * <code>EventInputStream</code>, adding methods for reading events and
 * returning them as int arrays which the sorter can handle.
 * 
 * @version 0.5 April 98
 * @author Dale Visser, Ken Swartz
 * @see AbstractEventInputStream
 * @since JDK1.1
 */
public class L002InputStream extends AbstractL002HeaderReader implements
		L002Parameters {

	private transient EventInputStatus status;

	private transient int parameter;

	/**
	 * Make sure to issue a setConsole() after using this constructor. It is
	 * here to satisfy the requirements of Class.newInstance()
	 */
	public L002InputStream() {
		super();
	}

	/**
	 * @see AbstractEventInputStream#EventInputStream(MessageHandler)
	 */
	public L002InputStream(MessageHandler console) {
		super(console);
	}

	/**
	 * Creates the input stream given an event size.
	 * 
	 * @param eventSize
	 *            number of parameters per event
	 * @param console
	 *            the place to write messages
	 */
	public L002InputStream(MessageHandler console, int eventSize) {
		super(console, eventSize);
	}

	/**
	 * Reads an event from the input stream Expects the stream position to be
	 * the beginning of an event. It is up to the user to ensure this.
	 * 
	 * @exception EventException
	 *                thrown for errors in the event stream
	 */
	public EventInputStatus readEvent(int[] input) throws EventException {
		synchronized (this) {
			try {
				while (isParameter(dataInput.readShort())) {
					// could be event or scaler parameter
					if (status == EventInputStatus.PARTIAL_EVENT) {
						final short possibleData = dataInput.readShort();
						if (parameter < eventSize) {
							// within array bounds
							input[parameter] = possibleData;
						}
					} else if (status == EventInputStatus.SCALER_VALUE) {
						dataInput.readInt();// throw away scaler value
					}
				}
			} catch (EOFException eofe) {
				// we got to the end of a file or stream
				status = EventInputStatus.END_FILE;
				console
						.warningOutln(getClass().getName()
								+ ".readEvent(): End of File reached...file may be corrupted, or run not ended properly.");
			} catch (Exception e) {
				status = EventInputStatus.UNKNOWN_WORD;
				throw new EventException(getClass().getName()
						+ ".readEvent() parameter = " + parameter
						+ " Exception: " + e.toString());
			}
			return status;
		}
	}

	/*
	 * non-javadoc: Read an event parameter.
	 */
	private boolean isParameter(final short paramWord) {
		boolean parameterSuccess;
		/* check if it's a special type of parameter */
		if (paramWord == EVENT_END_MARKER) {
			parameterSuccess = false;
			status = EventInputStatus.EVENT;
		} else if (paramWord == BUFFER_END_MARKER) {
			parameterSuccess = false;
			status = EventInputStatus.END_BUFFER;
		} else if (paramWord == RUN_END_MARKER) {
			parameterSuccess = false;
			status = EventInputStatus.END_RUN;
			/* get parameter value if not special type */
		} else if (passesParamMask(paramWord)) {
			final int paramNumber = paramWord & EVENT_PARAMETER_MASK;
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

	private boolean passesParamMask(final short paramWord) {
		return (paramWord & EVENT_PARAMETER_MARKER) != 0;
	}

	/**
	 * Check for end of run word
	 */
	public boolean isEndRun(final short dataWord) {
		synchronized (this) {
			return (dataWord == RUN_END_MARKER);
		}
	}
}
