package jam.sort.stream;

import java.io.EOFException;

import static jam.sort.stream.L002Parameters.*;

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
public class L002InputStream extends AbstractL002HeaderReader {

	/**
	 * Make sure to issue a setConsole() after using this constructor. It is
	 * here to satisfy the requirements of Class.newInstance()
	 */
	public L002InputStream() {
		super();
	}

	/**
	 * @see AbstractEventInputStream#AbstractEventInputStream(boolean)
	 * @param console whether console exists
	 */
	public L002InputStream(final boolean console) {
		super(console);
	}

	/**
	 * Creates the input stream given an event size.
	 *
	 * @param console whether console exists
	 * @param eventSize the number of signals per event
	 */
	public L002InputStream(final boolean console, final int eventSize) {
		super(console, eventSize);
	}

	@Override
	public String getFormatDescription() {
		return "Original implementation of ORNL L002 format at Yale.";
	}

	/**
	 * Reads an event from the input stream Expects the stream position to be
	 * the beginning of an event. It is up to the user to ensure this.
	 * 
	 * @exception EventException
	 *                thrown for errors in the event stream
	 */
	@Override
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
				handleEndOfFileException();
			} catch (Exception e) {
				handleGeneralException(e);
			}
			return status;
		}
	}

	/*
	 * non-javadoc: Read an event parameter.
	 */
	private boolean isParameter(final short paramWord) {
		boolean rval;
		/* check if it's a special type of parameter */
		if (isEndParameter(paramWord)) {// assigns status
			rval = false;
			/* get parameter value if not special type */
		} else if (passesParamMask(paramWord)) {
			final int paramNumber = paramWord & EVENT_MASK;
			if (paramNumber < 2048) {
				parameter = paramNumber - 1;// parameter number used in array
				rval = true;
				status = EventInputStatus.PARTIAL_EVENT;
			} else {// 2048-4095 assumed
				rval = true;
				status = EventInputStatus.SCALER_VALUE;
			}
		} else {// unknown word
			parameter = paramWord;
			rval = false;
			status = EventInputStatus.UNKNOWN_WORD;
		}
		return rval;
	}

	private boolean passesParamMask(final short paramWord) {
		return (paramWord & EVENT_PARAMETER) != 0;
	}

	/**
	 * Check for end of run word
	 */
	@Override
	public boolean isEndRun(final short dataWord) {
		synchronized (this) {
			return (dataWord == RUN_END_MARKER);
		}
	}
}
