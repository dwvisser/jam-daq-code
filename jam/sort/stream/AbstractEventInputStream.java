package jam.sort.stream;

import jam.global.RunInfo;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

/**
 * A general-purpose <code>InputStream</code> of experiment events that can be
 * sorted.
 * 
 * @version 0.5 August 98
 * @author Ken Swartz
 * @since JDK1.1
 */

public abstract class AbstractEventInputStream {
	
	/**
	 * Status type for event input streams.
	 * @author Dale Visser
	 *
	 */
	public enum EventInputStatus {
		/**
		 * Value to initialize to instead of null.
		 */
		NONE,
		
		/**
		 * Status if just read an event.
		 */
		END_BUFFER,

		/**
		 * Status if just reached the end of a buffer.
		 */
		END_FILE,

		/**
		 * Status if just reached the end of a run.
		 */
		END_RUN,

		/**
		 * Status if just reached the end of a file.
		 */
		END_STREAM,

		/**
		 * Status if just reached the end of the stream.
		 */
		ERROR,

		/**
		 * Status if only a partial event was just read.
		 */
		EVENT,

		/**
		 * Status if unidentified word was just read.
		 */
		IGNORE,

		/**
		 * Status if there is an unrecoverable error when reading the stream.
		 */
		PARTIAL_EVENT,

		/**
		 * Status if the most recent read parameter is actually a scaler value.
		 */
		SCALER_VALUE,

		/**
		 * Status if the last bit of the stream was ignorable.
		 */
		UNKNOWN_WORD
	}

	/**
	 * Handles logging messages.
	 */
	protected static final Logger LOGGER = Logger
			.getLogger(AbstractEventInputStream.class.getPackage().getName());

	/**
	 * 
	 */
	protected transient int bufferCount;

	/**
	 * Size of a buffer, if appropriate
	 */
	protected int bufferSize;

	/**
	 * Stream events are read from
	 */
	protected transient DataInputStream dataInput;

	/**
	 * 
	 */
	protected transient int eventCount;

	/**
	 * Number of signal values for each event.
	 */
	protected int eventSize;

	/**
	 * 
	 */
	protected transient String headerDate = "No Date";

	/**
	 * 
	 */
	protected transient int headerEventSize = 0;

	/**
	 * Header information
	 */
	protected String headerKey;

	/**
	 * 
	 */
	protected transient int headerLength = 0;

	/**
	 * 
	 */
	public transient int headerRunNumber;

	/**
	 * Number of bytes in header
	 */
	protected transient int headerSize;

	/**
	 * 
	 */
	protected transient String headerTitle = "No Title";

	/**
	 * 
	 */
	protected int numberEvents;

	/**
	 * Make sure to issue a setConsole() after using this constructor It is here
	 * to satisfy the requirements of Class.newInstance()
	 */
	public AbstractEventInputStream() {
		super();
		eventCount = 0;
		bufferCount = 0;
	}

	/**
	 * Default constructor.
	 * 
	 * @param consoleExists
	 *            whether console exists
	 */
	public AbstractEventInputStream(boolean consoleExists) {
		this();
		setConsoleExists(consoleExists);
	}

	/**
	 * Constructor with event size given.
	 * 
	 * @param consoleExists
	 *            whether console exists
	 * @param size
	 *            the number of signals per event
	 */
	public AbstractEventInputStream(boolean consoleExists, int size) {
		this(consoleExists);
		eventSize = size;
	}

	/**
	 * Returns the size of the input buffer.
	 * 
	 * @return the size of the input buffer
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * Returns the event size.
	 * 
	 * @return the number of signals per event
	 */
	public int getEventSize() {
		return eventSize;
	}

	/**
	 * @return the size of the header block
	 */
	public int getHeaderSize() {
		return headerSize;
	}

	/**
	 * Checks if a word is an end-of-run marker.
	 * 
	 * @param word
	 *            to be checked whether it is an end-of-run marker
	 * @return <code>true</code> if yes, <code>false</code> if no
	 */
	abstract public boolean isEndRun(short word);

	/**
	 * Loads the run information, usually after it is read from a header.
	 */
	public void loadRunInfo() {
		RunInfo.runNumber = headerRunNumber;
		RunInfo.runTitle = headerTitle;
		RunInfo.runStartTimeSt = headerDate;
		RunInfo.runEventSize = headerEventSize;
		RunInfo.runRecordLength = headerLength;
	}

	/**
	 * Reads a byte. Only implemented as a requirement of extending
	 * <code>InputStream</code>, which defines this method
	 * <code>abstract</code>.
	 * 
	 * @return the next byte in the stream defined in
	 *         <code>setInputStream()</code>
	 * @exception IOException
	 *                thrown if ther's a problem reading from the stream
	 * @see #setInputStream
	 */
	public int read() throws IOException {
		return dataInput.read();
	}

	/**
	 * Reads the next data word as a short. Can be overidden by subclasses.
	 * 
	 * @return a <code>short</code> read from the input stream
	 * @exception IOException
	 *                thrown if ther's a problem reading from the stream
	 * @see #setInputStream
	 */
	public short readDataWord() throws IOException {
		return dataInput.readShort();
	}

	/**
	 * Reads an event into the passed array and returns a status flag. This can
	 * take on the following values
	 * <ul>
	 * <li>EVENT</li>
	 * <li>END_RUN</li>
	 * <li>END_BUFFER</li>
	 * <li>ERROR</li>
	 * </ul>
	 * 
	 * @param event
	 *            container for the event info read from the event stream
	 * @return an indicator of the status after the read from the event stream
	 * @exception EventException
	 *                thrown if an error condition cannot be handled
	 * @see EventInputStatus
	 */
	abstract public EventInputStatus readEvent(int[] event)
			throws EventException;

	/**
	 * Reads a header and return a status flag.
	 * 
	 * @return <code>true</code> if OK, <code>false</code> if there was a
	 *         problem
	 * @exception EventException
	 *                thrown if an error condition cannot be handled
	 */
	abstract public boolean readHeader() throws EventException;

	// abstract methods for class

	/**
	 * Sets the size of the input buffer.
	 * 
	 * @param size
	 *            the size in bytes of the input buffer
	 */
	public void setBufferSize(final int size) {
		bufferSize = size;
	}

	/**
	 * Whether a console exists.
	 */
	protected transient boolean consoleExists = false;

	/**
	 * Define the console.
	 * 
	 * @param exists
	 *            whether the console exists
	 */
	public final void setConsoleExists(final boolean exists) {
		this.consoleExists = exists;
	}

	/**
	 * Sets the event size.
	 * 
	 * @param size
	 *            the number of signals per event
	 */
	public void setEventSize(final int size) {
		this.eventSize = size;
	}

	/**
	 * Sets the input stream which will be used as the source of events (and
	 * headers).
	 * 
	 * @param inputStream
	 *            source of event data
	 */
	public void setInputStream(final InputStream inputStream) {
		dataInput = new DataInputStream(inputStream);
	}

	/**
	 * Pops up an error dialog.
	 * 
	 * @param exception
	 *            the cause of the error
	 */
	protected final void showErrorMessage(final Exception exception) {
		final String cname = getClass().getName();
		final String message = exception.getMessage();
		LOGGER.log(Level.SEVERE, message, exception);
		if (!consoleExists) {
			JOptionPane.showMessageDialog(null, message, cname,
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Prints a message to the console.
	 * 
	 * @param message
	 *            message
	 */
	protected final void showMessage(final String message) {
		LOGGER.info(message);
	}

	/**
	 * Pops up a warning dialog.
	 * 
	 * @param string
	 *            the warning message
	 */
	protected final void showWarningMessage(final String string) {
		final String cname = getClass().getName();
		LOGGER.warning(string);
		if (!consoleExists) {
			JOptionPane.showMessageDialog(null, string, cname,
					JOptionPane.WARNING_MESSAGE);
		}
	}

}
