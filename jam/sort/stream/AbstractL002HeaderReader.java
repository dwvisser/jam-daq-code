package jam.sort.stream;

import static jam.sort.stream.L002Parameters.BUFFER_END_MARKER;
import static jam.sort.stream.L002Parameters.EVENT_END_MARKER;
import static jam.sort.stream.L002Parameters.HEADER_START;
import static jam.sort.stream.L002Parameters.RUN_END_MARKER;
import jam.util.StringUtilities;

import java.io.IOException;

/**
 * This class takes care of reading standard Oak Ridge L002 header records. All
 * event input streams which wish to do the same should extend this.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Feb 2, 2004
 */
public abstract class AbstractL002HeaderReader extends AbstractEventInputStream {

	protected transient int parameter;

	protected transient EventInputStatus status;

	/**
	 * Creates the input stream given an event size.
	 * 
	 * @param eventSize
	 *            number of parameters per event.
	 * @param consoleExists
	 *            whether a console exists
	 */
	AbstractL002HeaderReader(final boolean consoleExists, final int eventSize) {
		super(consoleExists, eventSize);
	}

	/**
	 * @param consoleExists
	 *            whether a console exists
	 */
	public AbstractL002HeaderReader(final boolean consoleExists) {
		super(consoleExists);
	}

	/**
	 * @see AbstractEventInputStream#AbstractEventInputStream()
	 */
	public AbstractL002HeaderReader() {
		super();
	}

	/**
	 * Implementers must describe their particular variant of L002 in plain
	 * English.
	 * 
	 * @return a string description
	 */
	public abstract String getFormatDescription();

	/**
	 * @see jam.sort.stream.AbstractEventInputStream#readHeader()
	 */
	@Override
	public final boolean readHeader() throws EventException {
		final byte[] headerStart = new byte[32];// header key
		final byte[] date = new byte[16];// date mo/da/yr hr:mn
		final byte[] title = new byte[80];// title
		final byte[] reserved1 = new byte[8];// reserved set to 0
		final byte[] reserved2 = new byte[92];// reserved set to 0
		final byte[] secHead = new byte[256];// read buffer for secondary
		// headers
		final StringUtilities stringUtil = StringUtilities.getInstance();
		try {
			dataInput.readFully(headerStart); // key
			dataInput.readFully(date); // date
			dataInput.readFully(title); // title
			final int number = dataInput.readInt();// header number
			dataInput.readFully(reserved1);
			final int numSecHead = dataInput.readInt();// number of secondary
			// header records
			dataInput.readInt();// header record length
			dataInput.readInt();// Block line image records
			dataInput.readInt();// IMAGE_RECORD_LENGTH
			final int eventParams = dataInput.readInt();
			dataInput.readInt();// DATA_RECORD_LENGTH
			dataInput.readFully(reserved2);
			/* save reads to header variables */
			headerKey = stringUtil.getASCIIstring(headerStart);
			headerRunNumber = number;
			headerTitle = stringUtil.getASCIIstring(title);
			headerEventSize = eventParams;
			headerDate = stringUtil.getASCIIstring(date);
			loadRunInfo();
			/* read secondary headers */
			for (int i = 0; i < numSecHead; i++) {
				dataInput.readFully(secHead);
			}
			return headerKey.equals(HEADER_START);
		} catch (IOException ioe) {
			throw new EventException("Problem reading header.", ioe);
		}
	}

	protected boolean isEndParameter(final short paramWord) {
		boolean rval = false;
		/* check if it's a special type of parameter */
		if (paramWord == EVENT_END_MARKER) {
			rval = true;
			status = EventInputStatus.EVENT;
		} else if (paramWord == BUFFER_END_MARKER) {
			rval = true;
			status = EventInputStatus.END_BUFFER;
		} else if (paramWord == RUN_END_MARKER) {
			rval = true;
			status = EventInputStatus.END_RUN;
		}
		return rval;
	}

	/**
	 * @param exception
	 * @throws EventException
	 */
	protected void handleGeneralException(final Exception exception)
			throws EventException {
		status = EventInputStatus.UNKNOWN_WORD;
		throw new EventException(getClass().getName()
				+ ".readEvent() parameter = " + parameter, exception);
	}

	/**
	 * 
	 */
	protected void handleEndOfFileException() {
		// we got to the end of a file or stream
		status = EventInputStatus.END_FILE;
		LOGGER
				.warning(getClass().getName()
						+ ".readEvent(): End of File reached...file may be corrupted, or run not ended properly.");
	}

}
