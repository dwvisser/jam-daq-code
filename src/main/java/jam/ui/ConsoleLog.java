package jam.ui;

import com.google.inject.Singleton;
import jam.global.JamException;
import jam.global.MessageHandler;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Jam's console log panel.
 * 
 * @author Dale Visser
 */
@Singleton
public final class ConsoleLog implements MessageHandler {
	private transient static final SimpleAttributeSet ATR_WARN, ATR_ERR;

	/**
	 * Number of lines in scroll-back log.
	 */
	private final static int MAX_LINES = 100;

	/**
	 * End of line character(s).
	 */
	private static final String END_LINE = System.getProperty("line.separator");
	static {
		ATR_WARN = new SimpleAttributeSet();
		StyleConstants.setForeground(ATR_WARN, Color.blue);
		ATR_ERR = new SimpleAttributeSet();
		StyleConstants.setForeground(ATR_ERR, Color.red);
	}

	private transient final JTextPane textLog = new JTextPane();

	private transient final Document doc = textLog.getStyledDocument();

	private transient final JScrollPane jsp = new JScrollPane(textLog,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

	private transient boolean logFileOn; // are we logging to a file

	private transient BufferedWriter logWriter; // output stream

	private transient String messageFile; // message for file

	/**
	 * A lock for message output so messages don't overlap.
	 */
	private transient boolean msgLock;

	private transient int numberLines; // number of lines in output

	/**
	 * Log of Console activity.
	 */
	public ConsoleLog() {
		super();
		textLog
				.setToolTipText("After setup, this log is (usually) written to a file, too.");
		textLog.setEditable(false);
		final int lineHeight = textLog.getFontMetrics(textLog.getFont())
				.getHeight();
		final int defaultLines = 6;
		final int logHeight = lineHeight * defaultLines;
		textLog.setPreferredSize(new Dimension(600, logHeight));
		msgLock = false;
		numberLines = 1;
		logFileOn = false;
		Runtime.getRuntime().addShutdownHook(new ConsoleLog.LogFileCloser(this));
	}

	/**
	 * Close the log file
	 * 
	 * @exception JamException
	 *                exceptions that go to the console
	 */
	public void closeLogFile() throws JamException {
		try {
			logWriter.close();
		} catch (IOException ioe) {
			throw new JamException("Could not close log file.", ioe);
		}
	}

	/**
	 * Writes an error message to the console immediately.
	 * 
	 * @param message
	 *            error
	 */
	public void errorOutln(final String message) {
		promptOutln("Error: " + message, ATR_ERR);
	}

	class LogFileCloser extends Thread {

		private final ConsoleLog consoleLog;

		LogFileCloser(ConsoleLog consoleLog){
			this.consoleLog = consoleLog;
		}

		public void run() {
			if (this.consoleLog.logFileOn) {
				try {
					this.consoleLog.closeLogFile();
				} catch (JamException je) {
					je.printStackTrace();
				}
			}
		}
	}

	protected Component getComponent() {
		return jsp;
	}

	/*
	 * non-javadoc: Get the current date and time
	 */
	private String getDate() {
		final Date date = new java.util.Date(); // get time
		final DateFormat datef = DateFormat.getDateTimeInstance();
		// medium date time format
		datef.setTimeZone(TimeZone.getDefault()); // set time zone
		return datef.format(date); // format time
	}

	/*
	 * non-javadoc: formatted version of the current time
	 */
	private String getTime() {
		final Date date = new java.util.Date(); // get time
		final DateFormat datef = DateFormat.getTimeInstance(DateFormat.MEDIUM);
		datef.setTimeZone(TimeZone.getDefault()); // set time zone
		return datef.format(date); // format time
	}

	/**
	 * Output a message so it will be continued on the same line.
	 * 
	 * @param message
	 *            text to output
	 */
	public void messageOut(final String message) {
		synchronized (this) {
			messageOut(message, CONTINUE);
		}
	}

	/**
	 * Outputs the string as a message to the console, which has more than one
	 * part, so message can continued by a subsequent call.
	 * 
	 * @param message
	 *            the message to be output
	 * @param part
	 *            one of NEW, CONTINUE, or END
	 */
	public void messageOut(final String message, final int part) {
		synchronized (this) {
			final StringBuffer mbuff = new StringBuffer(message);
			if (part == NEW) {
				msgLock = true;
				messageFile = getDate() + ">" + mbuff;
				mbuff.insert(0, '>').insert(0, getTime()).insert(0, END_LINE);
				try {
					doc.insertString(doc.getLength(), mbuff.toString(),
							SimpleAttributeSet.EMPTY);
				} catch (BadLocationException e) {
					JOptionPane.showMessageDialog(textLog, e.getMessage(),
							getClass().getName(), JOptionPane.ERROR_MESSAGE);
				}
			} else if (part == CONTINUE) {
				messageFile = messageFile + mbuff;
				try {
					doc.insertString(doc.getLength(), mbuff.toString(),
							SimpleAttributeSet.EMPTY);
				} catch (BadLocationException e) {
					JOptionPane.showMessageDialog(textLog, e.getMessage(),
							getClass().getName(), JOptionPane.ERROR_MESSAGE);
				}
			} else if (part == END) {
				messageFile = messageFile + mbuff + END_LINE;
				appendBuffer(mbuff);
				final JScrollBar scroll = jsp.getVerticalScrollBar();
				scroll.setValue(scroll.getMaximum());
				/* if file logging on write to file */
				if (logFileOn) {
					try {
						logWriter.write(messageFile, 0, messageFile.length());
						logWriter.flush();
					} catch (IOException ioe) {
						logFileOn = false;
						errorOutln("Unable to write to log file, logging turned off [JamConsole]");
					}
				}
				// unlock text area and notify others they can use it
				msgLock = false;
				notifyAll();
			} else {
				throw new IllegalArgumentException(
						"Error not a valid message part [JamConsole]");
			}
		}
	}

	private void appendBuffer(final StringBuffer mbuff) {
		try {
			doc.insertString(doc.getLength(), mbuff.toString(),
					SimpleAttributeSet.EMPTY);
		} catch (BadLocationException e) {
			JOptionPane.showMessageDialog(textLog, e.getMessage(), getClass()
					.getName(), JOptionPane.ERROR_MESSAGE);
		}
		trimLog();
		textLog.setCaretPosition(doc.getLength());
	}

	/**
	 * Output an empty line to the console.
	 */
	public void messageOutln() {
		messageOutln("");
	}

	/**
	 * Output a message with a carriage return.
	 * 
	 * @param message
	 *            the message to be printed to the console
	 */
	public void messageOutln(final String message) {
		synchronized (this) {
			final StringBuffer mbuff = new StringBuffer();
			msgLock = true;
			messageFile = getDate() + ">" + message + END_LINE;
			mbuff.append(END_LINE).append(getTime()).append('>')
					.append(message);
			this.appendBuffer(mbuff);
			// if file logging on write to file
			if (logFileOn) {
				try {
					logWriter.write(messageFile, 0, messageFile.length());
					logWriter.flush();
				} catch (IOException ioe) {
					logFileOn = false;
					errorOutln("Unable to write to log file, logging turned off [JamConsole]");
				}
			}
			// unlock text area and notify others they can use it
			msgLock = false;
			notifyAll();
		}
	}

	private void promptOutln(final String _message, final AttributeSet attr) {
		synchronized (this) {
			final StringBuilder mbuff = new StringBuilder();
			/*
			 * Dont wait for lock. Output message right away.
			 */
			if (msgLock) { // if locked add extra returns
				messageFile = END_LINE + getDate() + ">" + _message + END_LINE;
				mbuff.append(END_LINE).append(getTime()).append('>').append(
						_message).append(END_LINE);
			} else { // normal message
				messageFile = getDate() + ">" + _message + END_LINE;
				mbuff.append(END_LINE).append(getTime()).append('>').append(
						_message);
			}
			try {
				doc.insertString(doc.getLength(), mbuff.toString(), attr);
			} catch (BadLocationException e) {
				JOptionPane.showMessageDialog(textLog, e.getMessage(),
						getClass().getName(), JOptionPane.ERROR_MESSAGE);
			}
			trimLog();
			textLog.setCaretPosition(doc.getLength());
			/* beep */
			Toolkit.getDefaultToolkit().beep();
			if (logFileOn) { // if file logging on write to file
				try {
					logWriter.write(messageFile, 0, messageFile.length());
					logWriter.flush();
				} catch (IOException ioe) {
					logFileOn = false;
					errorOutln("Unable to write to log file, logging turned off [JamConsole]");
				}
			}
		}
	}

	/**
	 * Create a file for the log to be saved to. The method appends a number
	 * (starting at 1) to the file name if the file already exists.
	 * 
	 * @param name
	 *            name to try
	 * @return actual name used
	 * @exception JamException
	 *                exceptions that go to the console
	 */
	public String setLogFileName(final String name) throws JamException {
		String newName = name + ".log";
		File file = new File(newName);
		/*
		 * create a unique file, append a number if a log already exits
		 */
		int index = 1;
		while (file.exists()) {
			newName = name + index + ".log";
			file = new File(newName);// NOPMD
			index++;
		}
		try {
			logWriter = new BufferedWriter(new FileWriter(file));
		} catch (IOException ioe) {
			throw new JamException("Problem opening log file "
					+ file.getAbsolutePath(), ioe);
		}
		return newName;
	}

	/**
	 * Turn on the logging to a file
	 * 
	 * @param state
	 *            <code>true</code> to be logging
	 * @exception JamException
	 *                exceptions that go to the console
	 */
	public void setLogFileOn(final boolean state) throws JamException {
		synchronized (this) {
			logFileOn = state;
			if (logWriter == null) {
				logFileOn = false;
				throw new JamException(
						getClass().getSimpleName()
								+ ": Cannot turn on logging to file, log file does not exist.");
			}
		}
	}

	/**
	 * Trim the text on screen Log so it does not get too long
	 */
	private void trimLog() {
		numberLines++;
		if (numberLines > MAX_LINES) { // get rid of top line
			numberLines--;
			try {
				doc.remove(0, textLog.getText().indexOf(END_LINE)
						+ END_LINE.length());
			} catch (BadLocationException e) {
				JOptionPane.showMessageDialog(textLog, e.getMessage(),
						getClass().getName(), JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Outputs a warning message to the console immediately.
	 * 
	 * @param message
	 *            warning
	 */
	public void warningOutln(final String message) {
		promptOutln("Warning: " + message, ATR_WARN);
	}

	@Override
	public String toString() {
		String rval = "";
		try {
			rval = this.doc.getText(0, this.doc.getLength());
		} catch (BadLocationException ble) {
			this.warningOutln(ble.getMessage());
		}

		return rval;
	}
}
