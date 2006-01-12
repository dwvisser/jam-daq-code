package jam.sort.stream;

import jam.global.MessageHandler;

import java.io.EOFException;
import java.io.IOException;

/**
 * This class knows how to handle Oak Ridge tape format (with special headers as
 * used by Charles Barton's group). It extends EventInputStream, adding methods
 * for reading events and returning them as int arrays which the sorter can
 * handle.
 * 
 * @version 0.5 April 98
 * @author Dale Visser, Ken Swartz
 * @see AbstractEventInputStream
 * @since JDK1.1
 */
public final class L003InputStream extends AbstractEventInputStream implements
		L003Parameters {

	private transient int eventValue;

	private transient int parameter;

	private transient EventInputStatus status;

	private transient int byteCounter = 0;

	/**
	 * Needed to create an instance with newInstance().
	 */
	public L003InputStream() {
		super();
	}

	/**
	 * @see AbstractEventInputStream#AbstractEventInputStream(MessageHandler)
	 */
	public L003InputStream(MessageHandler console) {
		super(console);
	}

	/**
	 * @see AbstractEventInputStream#AbstractEventInputStream(MessageHandler, int)
	 */
	public L003InputStream(MessageHandler console, int eventSize) {
		super(console, eventSize);
	}

	/**
	 * Implementation of an <code>EventInputStream</code> abstract method.
	 */
	public boolean isEndRun(final short dataWord) {
		synchronized (this) {
			return (dataWord == L002Parameters.RUN_END_MARKER);
		}
	}

	/**
	 * Reads an event from the input stream. Expects the stream position to be
	 * the beginning of an event. It is up to the user to ensure this.
	 * 
	 * @exception EventException
	 *                thrown for errors in the event stream
	 */
	public EventInputStatus readEvent(int[] input) throws EventException {
		synchronized (this) {
			try {
				while (readParameter()) {
					input[parameter] = eventValue;
				}

			} catch (IOException io) {
				status = EventInputStatus.ERROR;
				throw new EventException("Reading Event from IOException "
						+ io.getMessage() + " [L003InputStream]");

			}
			return status; // if event read return ok
		}
	}

	/**
	 * Implementation of <code>EventInputStream</code> abstract method. Note,
	 * this reads a Vax byte order version of L002 headers unlike
	 * L002HeaderRecord.
	 * 
	 * @throws EventException
	 *             thrown for errors in the event stream
	 */
	public boolean readHeader() throws EventException {
		final byte[] headerStart = new byte[32];
		final byte[] date = new byte[16];
		final byte[] title = new byte[80];
		final byte[] reserved1 = new byte[8];
		final byte[] reserved2 = new byte[92];
		int number = 0;
		int size = 0;

		try {
			dataInput.readFully(headerStart);
			dataInput.readFully(date);
			dataInput.readFully(title);
			number = readVaxInt();
			dataInput.readFully(reserved1);
			readVaxInt();// number of secondary headers
			readVaxInt(); // header record length
			readVaxInt();// number of image records
			readVaxInt(); // IMAGE_RECORD_LENGTH
			size = readVaxInt();// event size
			readVaxInt(); // DATA_RECORD_LENGTH
			dataInput.readFully(reserved2);

			// save reads to header variables
			headerKey = String.valueOf(headerStart);
			headerRunNumber = number;
			headerTitle = String.valueOf(title);
			headerEventSize = size;
			headerDate = String.valueOf(date);

			loadRunInfo();
			return headerKey.equals(HEADER_START);
		} catch (IOException ioe) {
			throw new EventException("Reading event header -"
					+ ioe.getMessage());
		}
	}

	/*
	 * non-javadoc: Read a event parameter
	 */
	private boolean readParameter() throws EventException, IOException {
		boolean rval;
		int paramWord;
		try {
			paramWord = readVaxShort(); // read parameter word
			// check special types parameter
			if (paramWord == L002Parameters.EVENT_END_MARKER) {
				// need another read as marker is 2 shorts
				paramWord = readVaxShort();
				numberEvents++;
				rval = false;
				status = EventInputStatus.EVENT;
			} else if (paramWord == L002Parameters.BUFFER_END_MARKER) {
				rval = false;
				status = EventInputStatus.END_BUFFER;
			} else if (paramWord == L002Parameters.RUN_END_MARKER) {
				rval = false;
				status = EventInputStatus.END_RUN;
				// get parameter value if not special type
			} else if (0 == (paramWord & L002Parameters.EVENT_PARAMETER)) {
                rval = false;
                status = EventInputStatus.UNKNOWN_WORD;
                throw new EventException("L003InputStream parameter value: "
                        + paramWord + " [L003InputStream]");
            } else {
				parameter = (paramWord & EVENT_MASK) - 1;
				// parameter number
				eventValue = readVaxShort();
				// read event word
				rval = true;
				status = EventInputStatus.PARTIAL_EVENT;
			}
			// we got to the end of a file
		} catch (EOFException eof) {
			showErrorMessage(eof);
			rval = false;
			status = EventInputStatus.END_FILE;
		} catch (IOException ioe) {
			showErrorMessage(ioe);
			rval = scalerRead();
			if (rval) {
				status = EventInputStatus.END_BUFFER;
			} else {
				status = EventInputStatus.END_FILE;
			}

		}
		return rval;
	}

	/*
	 * non-javadoc: reads a little endian integer (4 bytes)
	 */
	private int readVaxInt() throws IOException {
		final int ch1 = dataInput.read();
		final int ch2 = dataInput.read();
		final int ch3 = dataInput.read();
		final int ch4 = dataInput.read();
		return (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0);
	}

	/*
	 * non-javadoc: reads a little endian short (2 bytes) but return a 4 byte
	 * integer
	 */
	private short readVaxShort() throws IOException {
		final int ch1 = dataInput.read();
		final int ch2 = dataInput.read();
		return ((ch1 | ch2) < 0) ? -1 : (short) ((ch2 << 8) + (ch1 << 0));
	}

	/*
	 * non-javadoc: read in a scaler dump this is a 32000 byte ascii record
	 */
	private boolean scalerRead() throws IOException {
		boolean readStatus = true;
		final byte[] scalerDump = new byte[SCALER_BUFF_SIZE];
		String scalerString = null;
		int nScalerBytes = 0;
		int bytesRead = 0;
		while ((nScalerBytes < SCALER_REC_SIZE) && bytesRead >= 0) {
			if (nScalerBytes < SCALER_REC_SIZE - SCALER_BUFF_SIZE) {
				bytesRead = dataInput.read(scalerDump);
			} else {
				bytesRead = dataInput.read(scalerDump, 0, SCALER_REC_SIZE
						- nScalerBytes);
			}
			if (bytesRead != SCALER_BUFF_SIZE) {
				showWarningMessage("scaler Not a full read, only read in "
						+ bytesRead + " bytes");
			}
			if (bytesRead > 0) {
				byteCounter += bytesRead;
				nScalerBytes += bytesRead;
			}
			// save first read
			if (nScalerBytes < SCALER_BUFF_SIZE + 1) {
				scalerString = String.valueOf(scalerDump);
			}
		}
		showMessage("scaler dump total bytes read " + nScalerBytes);
		showMessage("scaler dump = " + scalerString);
		if (bytesRead < 0) {
			readStatus = false;
			showMessage("End of file in scaler read");
		}
		return readStatus;
	}
}
