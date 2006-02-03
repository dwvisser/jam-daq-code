package jam.sort.stream;

import jam.util.NumberUtilities;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;

/**
 * This class knows how to handle Oak Ridge tape format.
 * 
 * @version 0.5 April 98
 * @author Dale Visser, Ken Swartz
 * @see AbstractEventInputStream
 * @since JDK1.1
 */
public class LdfInputStream extends AbstractEventInputStream implements
		L002Parameters {

	private transient EventInputStatus status;

	private transient int parameter;

	private static final short ASCII_DA = 0x4441;

	private static final short ASCII_TA = 0x5441;

	private static final int REST = 0x00200000;

	// make sure to issue a setConsole() after using this constructor
	// It is here to satisfy the requirements of Class.newInstance()
	/**
	 * Called by Jam to create an instance of this input stream.
	 */
	public LdfInputStream() {
		super();
	}

	/**
	 * Default constructor.
	 * 
	 * @param console
	 *            object where messages to the user are printed
	 */
	public LdfInputStream(boolean console) {
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
	public LdfInputStream(boolean console, int eventSize) {
		super(console, eventSize);
	}

	private transient boolean skip = true;

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
	public EventInputStatus readEvent(int[] input) throws EventException {
		synchronized (this) {
			boolean gotParameter = false;
			try {
				if (skip) {
					boolean stop = false;
					do {
						stop = dataInput.readShort() == ASCII_DA;
						if (stop) {
							stop = dataInput.readShort() == ASCII_TA;
						}
						if (stop) {
							stop = dataInput.readInt() == REST;
						}
					} while (!stop);
					skip = false;
				}
				while (isParameter(readVaxShort())) {// could be event or
					// scaler
					// parameter
					gotParameter = true;
					if (status == EventInputStatus.PARTIAL_EVENT) {
						if (parameter >= eventSize) {// skip, since array
							// index
							// would be too great for
							// event array
							dataInput.readShort();
						} else {// read into array
							input[parameter] = readVaxShort(); // read event
							// word
						}
					} else if (status == EventInputStatus.SCALER_VALUE) {
						dataInput.readInt();// throw away scaler value
					}
				}
			} catch (EOFException eofe) {// we got to the end of a file or
				// stream
				status = EventInputStatus.END_FILE;
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
				parameter = paramNumber;// parameter number used in array
				parameterSuccess = true;
				status = EventInputStatus.PARTIAL_EVENT;
			} else {// 2048-4095 assumed
				parameterSuccess = true;
				status = EventInputStatus.SCALER_VALUE;
			}
		} else {// unknown word
			parameterSuccess = false;
			skip = true;
			status = EventInputStatus.IGNORE;
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
	public boolean isEndRun(final short dataWord) {
		synchronized (this) {
			return (dataWord == RUN_END_MARKER);
		}
	}

	public boolean readHeader() {
		return true;
	}

	/*
	 * non-javadoc: reads a little endian short (2 bytes) but return a 4 byte
	 * integer
	 */
	private short readVaxShort() throws IOException {
		final byte[] rval = new byte[2];
		dataInput.read(rval);
		return NumberUtilities.getInstance().bytesToShort(rval,0,ByteOrder.LITTLE_ENDIAN);
	}
}
