package jam.sort.stream;

import jam.global.MessageHandler;
import jam.global.RunInfo;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

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
	public enum EventInputStatus {
	    /*
	     * Status if just read an event.
	     */
	    EVENT,

	    /*
	     * Status if just reached the end of a buffer.
	     */
	    END_BUFFER,

	    /*
	     * Status if just reached the end of a run.
	     */
	    END_RUN,

	    /*
	     * Status if just reached the end of a file.
	     */
	    END_FILE,

	    /*
	     * Status if just reached the end of the stream.
	     */
	    END_STREAM,

	    /*
	     * Status if only a partial event was just read.
	     */
	    PARTIAL_EVENT,

	    /*
	     * Status if unidentified word was just read.
	     */
	    UNKNOWN_WORD,

	    /*
	     * Status if there is an unrecoverable error when reading the stream.
	     */
	    ERROR,

	    /*
	     * Status if the most recent read parameter is actually a scaler value.
	     */
	    SCALER_VALUE,

	    /*
	     * Status if the last bit of the stream was ignorable.
	     */
	    IGNORE
	}
	
	/**
	 * Number of signal values for each event.
	 */
	protected int eventSize;

	/**
	 * Size of a buffer, if appropriate
	 */
	protected int bufferSize;

	/**
	 * 
	 */
	protected int numberEvents;

	/**
	 * 
	 */
	protected transient int bufferCount;

	/**
	 * 
	 */
	protected transient int eventCount;

	/**
	 * Stream events are read from
	 */
	protected transient DataInputStream dataInput;

	/**
	 * Number of bytes in header
	 */
	protected transient int headerSize;

	/**
	 * Header information
	 */
	protected String headerKey;

	/**
	 * 
	 */
	public transient int headerRunNumber;

	/**
	 * 
	 */
	protected transient String headerTitle = "No Title";

	/**
	 * 
	 */
	protected transient String headerDate = "No Date";

	/**
	 * 
	 */
	protected transient int headerEventSize = 0;

	/**
	 * 
	 */
	protected transient int headerLength = 0;

	/**
	 * The place to print messages.
	 */
	protected transient MessageHandler console;

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
	 * @param console
	 *            where to print messages
	 */
	public AbstractEventInputStream(MessageHandler console) {
		this();
		setConsole(console);
	}

	/**
	 * Constructor with event size given.
	 * 
	 * @param console
	 *            where to write text output to the user
	 * @param size
	 *            the number of signals per event
	 */
	public AbstractEventInputStream(MessageHandler console, int size) {
		this(console);
		eventSize = size;
	}

	/**
	 * Define the console.
	 * 
	 * @param console
	 *            where to write text output to the user
	 */
	public final void setConsole(final MessageHandler console) {
		this.console = console;
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
	 * Returns the event size.
	 * 
	 * @return the number of signals per event
	 */
	public int getEventSize() {
		return eventSize;
	}

	/**
	 * Sets the size of the input buffer.
	 * 
	 * @param size
	 *            the size in bytes of the input buffer
	 */
	public void setBufferSize(final int size) {
		this.bufferSize = size;
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
	 * @return the size of the header block
	 */
	public int getHeaderSize() {
		return headerSize;
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
	 * Loads the run information, usually after it is read from a header.
	 */
	public void loadRunInfo() {
		RunInfo.runNumber = headerRunNumber;
		RunInfo.runTitle = headerTitle;
		RunInfo.runStartTimeSt = headerDate;
		RunInfo.runEventSize = headerEventSize;
		RunInfo.runRecordLength = headerLength;
	}

	// abstract methods for class

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

	/**
	 * Checks if a word is an end-of-run marker.
	 * 
	 * @param word
	 *            to be checked whether it is an end-of-run marker
	 * @return <code>true</code> if yes, <code>false</code> if no
	 */
	abstract public boolean isEndRun(short word);

	/**
	 * Pops up an error dialog.
	 * 
	 * @param exception
	 *            the cause of the error
	 */
	protected final void showErrorMessage(final Exception exception) {
		final String cname = getClass().getName();
		if (console == null) {
			JOptionPane.showMessageDialog(null, exception.getMessage(), cname,
					JOptionPane.ERROR_MESSAGE);
		} else {
			console.errorOutln(cname + "--" + exception.getMessage());
		}
	}

	/**
	 * Pops up a warning dialog.
	 * 
	 * @param string
	 *            the warning message
	 */
	protected final void showWarningMessage(final String string) {
		final String cname = getClass().getName();
		if (console == null) {
			JOptionPane.showMessageDialog(null, string, cname,
					JOptionPane.WARNING_MESSAGE);
		} else {
			console.errorOutln(cname + "--" + string);
		}
	}

	/**
	 * Prints a message to the console.
	 * 
	 * @param message
	 *            message
	 */
	protected final void showMessage(final String message) {
		if (console == null) {
			System.out.println(message);
		} else {
			console.messageOutln(message);
		}
	}

}
