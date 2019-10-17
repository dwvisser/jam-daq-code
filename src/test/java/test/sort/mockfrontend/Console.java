package test.sort.mockfrontend;

import jam.global.MessageHandler;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * Class Console displays a output of commands and error messages and allows the
 * input of commands using the keyboard.
 * 
 * @author Ken Swartz
 * @author Dale Visser
 */
public class Console extends JPanel implements MessageHandler {

	private static final long serialVersionUID = 1L;
	private final static int DISPLAY_LINES = 25;
	private final static int NUMBER_LINES_LOG = 1000;

	/**
	 * Logs to a log file.
	 */
	public static final Logger LOGGER = Logger.getLogger(Console.class
			.getPackage().getName());

	static {
		try {
			LOGGER.addHandler(new FileHandler());
		} catch (final IOException ioe) {
			System.err.println(ioe.getMessage());// NOPMD
		}
	}

	/**
	 * End of line character(s).
	 */
	private static final String END_LINE = System.getProperty("line.separator");

	private transient final JTextPane textLog; // output text area
	private transient final Document doc;
	private transient final SimpleAttributeSet attr_normal, attr_warning,
			attr_error;
	// Is the message a new one or a continuation of one
	private transient boolean msgLock;
	// a lock for message output so message don't overlap
	/**
	 * Private.
	 * 
	 * @serial
	 */
	private transient final int maxLines;
	private transient int numberLines; // number of lines in output

	private transient final Object syncLock = new Object();

	/**
	 * Private.
	 * 
	 * @serial
	 */
	private transient String messageFile; // message for file

	/**
	 * Create a JamConsole which has an text area for output a text field for
	 * input.
	 */
	public Console() {
		this(NUMBER_LINES_LOG);
	}

	/**
	 * Constructor: Create a JamConsole which has an text area for output a text
	 * field for input.
	 * 
	 * @param linesLog
	 *            maximum number of lines in the log
	 */
	public Console(final int linesLog) {
		super(new BorderLayout(5, 5));
		maxLines = linesLog;
		textLog = new JTextPane();
		doc = textLog.getStyledDocument();
		attr_normal = new SimpleAttributeSet();
		attr_warning = new SimpleAttributeSet();
		StyleConstants.setForeground(attr_warning, Color.blue);
		attr_error = new SimpleAttributeSet();
		StyleConstants.setForeground(attr_error, Color.red);
		textLog.setEditable(false);
		final JScrollPane jsp = new JScrollPane(textLog,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.add(jsp, BorderLayout.CENTER);
		msgLock = false;
		numberLines = 1;
		this.setPreferredSize(new Dimension(800, 28 + 16 * DISPLAY_LINES));

	}

	/**
	 * Outputs the string as a message to the console, which has more than one
	 * part, so message can continued by a subsequent call.
	 * 
	 * @param _message
	 *            the message to be output
	 * @param part
	 *            one of NEW, CONTINUE, or END
	 */
	public void messageOut(final String _message, final int part) {
		synchronized (syncLock) {
			if (part == NEW) {
				msgLock = true;
				messageFile = getDate() + ">" + _message;
				final String message = buildConsoleMessage(_message, false);
				try {
					doc.insertString(doc.getLength(), message, attr_normal);
				} catch (final BadLocationException e) {
					logException("messageOut", e);
				}
			} else if (part == CONTINUE) {
				messageFile = messageFile + _message;
				try {
					doc.insertString(doc.getLength(), _message, attr_normal);
				} catch (final BadLocationException e) {
					logException("messageOut", e);
				}
			} else if (part == END) {
				messageFile = messageFile + _message + END_LINE;
				try {
					doc.insertString(doc.getLength(), _message, attr_normal);
				} catch (final BadLocationException e) {
					logException("messageOut", e);
				}
				trimLog();
				textLog.setCaretPosition(doc.getLength());
				// unlock text area and notify others they can use it
				msgLock = false;
				notifyAll();
			} else {
				LOGGER.severe("Error not a valid message part [JamConsole]");
			}
		}
	}

	private String buildConsoleMessage(final String message,
			final boolean addEndLine) {
		final StringBuilder result = new StringBuilder(END_LINE);
		result.append(getTime());
		result.append('>');
		result.append(message);
		if (addEndLine) {
			result.append(END_LINE);
		}
		return result.toString();
	}

	private void logException(final String method, final Throwable throwing) {
		LOGGER.throwing("Console", method, throwing);
	}

	/**
	 * Output a message so it will be continued on the same line.
	 */
	public void messageOut(final String message) {
		messageOut(message, CONTINUE);
	}

	/**
	 * Output a message with a carriage return.
	 * 
	 * @param _message
	 *            the message to be printed to the console
	 */
	public void messageOutln(final String _message) {
		synchronized (syncLock) {
			msgLock = true;
			messageFile = getDate() + ">" + _message + END_LINE;
			final String message = this.buildConsoleMessage(_message, false);
			try {
				doc.insertString(doc.getLength(), message, attr_normal);
			} catch (final BadLocationException e) {
				logException("messageOutln", e);
			}
			trimLog();
			textLog.setCaretPosition(doc.getLength());
			// unlock text area and notify others they can use it
			msgLock = false;
		}
	}

	/**
	 * Writes an error message to the console immediately.
	 */
	public void errorOutln(final String message) {
		LOGGER.severe(message);
		promptOutln("Error: " + message, attr_error);
	}

	/**
	 * Outputs a warning message to the console immediately.
	 */
	public void warningOutln(final String message) {
		promptOutln("Warning: " + message, attr_warning);
	}

	private void promptOutln(final String _message, final AttributeSet attr) {
		synchronized (syncLock) {
			/*
			 * Don't wait for lock. Output message right away.
			 */
			String message = this.buildConsoleMessage(_message, msgLock);
			if (msgLock) { // if locked add extra returns
				messageFile = END_LINE + getDate() + ">" + message + END_LINE;
			} else { // normal message
				messageFile = getDate() + ">" + message + END_LINE;
			}
			try {
				doc.insertString(doc.getLength(), message, attr);
			} catch (final BadLocationException e) {
				logException("promptOutln", e);
			}
			trimLog();
			textLog.setCaretPosition(doc.getLength());
			/* beep */
			Toolkit.getDefaultToolkit().beep();
		}
	}

	/**
	 * Trim the text on screen Log so it does not get too long
	 */
	private void trimLog() {
		numberLines++;
		if (numberLines > maxLines) { // get rid of top line
			numberLines--;
			try {
				doc.remove(0, textLog.getText().indexOf(END_LINE)
						+ END_LINE.length());
			} catch (final BadLocationException ble) {
				logException("trimLog", ble);
			}
		}
	}

	/**
	 * get the current time
	 */
	private String getTime() {
		Date date;
		DateFormat datef;
		String stime;

		date = new java.util.Date(); // get time
		datef = DateFormat.getTimeInstance(DateFormat.MEDIUM);
		// medium time format
		datef.setTimeZone(TimeZone.getDefault()); // set time zone
		stime = datef.format(date); // format time
		return stime;
	}

	/**
	 * Get the current date and time
	 */
	private String getDate() {
		Date date; // date object
		DateFormat datef;
		String stime;

		date = new java.util.Date(); // get time
		datef = DateFormat.getDateTimeInstance(); // medium date time format
		datef.setTimeZone(TimeZone.getDefault()); // set time zone
		stime = datef.format(date); // format time
		return stime;
	}

	public void messageOutln() {
		this.messageOutln("");
	}
}
