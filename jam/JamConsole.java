package jam;
import jam.global.CommandListener;
import jam.global.MessageHandler;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.swing.*;
import javax.swing.text.*;

/**
 * Class Console displays a output of commands and error messages
 * and allows the input of commands using the keyboard.
 *
 * @author  Ken Swartz
 * @author Dale Visser
 * @version 1.2 alpha last edit 15 Feb 2000
 * @version 0.5 last edit 11-98
 * @version 0.5 last edit 1-99
 */
public class JamConsole
	extends JPanel
	implements MessageHandler, ActionListener {

	final static int NUMBER_LINES_DISPLAY = 8;
	final static int NUMBER_LINES_LOG = 100;

	/**
	 * End of line character(s).
	 */
	private String END_LINE = (String) System.getProperty("line.separator");

	/**
	 * Private.
	 *
	 * @serial
	 */
	private CommandListener currentListener;

	/**
	 * Private.
	 *
	 * @serial
	 */
	private JTextPane textLog; //output text area
	private Document doc;
	private SimpleAttributeSet attr_normal, attr_warning, attr_error;
	/**
	 * Private.
	 *
	 * @serial
	 */
	private JTextField textIn; //input text field

	/**
	 * Private.
	 *
	 * @serial
	 */
	private boolean newMessage;
	//Is the message a new one or a continuation of one
	private boolean msgLock;
	//a lock for message output so message dont overlap
	/**
	 * Private.
	 *
	 * @serial
	 */
	private int maxLines;
	private int numberLines; //number of lines in output

	/**
	 * Private.
	 *
	 * @serial
	 */
	private String logFileName; //name of file to which the log is written
	/**
	 * Private.
	 *
	 * @serial
	 */
	private BufferedWriter logFileWriter; //output stream
	/**
	 * Private.
	 *
	 * @serial
	 */
	private boolean logFileOn; //are we logging to a file
	/**
	 * Private.
	 *
	 * @serial
	 */
	private String messageFile; //message for file

	/**
	 * Create a JamConsole which has an text area for output
	 * a text field for intput.
	 */
	public JamConsole() {
		this(NUMBER_LINES_LOG);
	}

	/**
	 *Constructor:
	 * Create a JamConsole which has an text area for output
	 * a text field for intput
	 */
	public JamConsole(int linesLog) {
		maxLines = linesLog;
		this.setLayout(new BorderLayout(5, 5));
		textLog = new JTextPane();
		doc = textLog.getStyledDocument();
		attr_normal = new SimpleAttributeSet();
		attr_warning = new SimpleAttributeSet();
		StyleConstants.setForeground(attr_warning, Color.blue);
		attr_error = new SimpleAttributeSet();
		StyleConstants.setForeground(attr_error, Color.red);
		textLog.setEditable(false);
		JScrollPane jsp =
			new JScrollPane(
				textLog,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.add(jsp, BorderLayout.CENTER);
		textIn = new JTextField(" ");
		this.add(textIn, BorderLayout.SOUTH);
		textIn.addActionListener(this);
		newMessage = true;
		msgLock = false;
		numberLines = 1;
		logFileOn = false;
		this.setPreferredSize(new Dimension(800, 28+16*NUMBER_LINES_DISPLAY));
	}

	/**
	 * Process event when a return is hit in input field
	 */
	public void actionPerformed(ActionEvent ae) {
		parseCommand(textIn.getText());
		textIn.setText(null);
	}

	/**
	 * Outputs the string as a message to the console, which has more than one part,
	 * so message can continued by a subsequent call.
	 *
	 * @param _message the message to be output
	 * @param part one of NEW, CONTINUE, or END
	 */
	public synchronized void messageOut(String _message, int part) {
		String message=new String(_message);
		if (part == NEW) {
			msgLock = true;
			messageFile = getDate() + ">" + message;
			message = END_LINE + getTime() + ">" + message;
			try {
				doc.insertString(doc.getLength(), message, attr_normal);
			} catch (BadLocationException e) {
				System.err.println(e);
			}
		} else if (part == CONTINUE) {
			messageFile = messageFile + message;
			try {
				doc.insertString(doc.getLength(), message, attr_normal);
			} catch (BadLocationException e) {
				System.err.println(e);
			}
		} else if (part == END) {
			messageFile = messageFile + message + END_LINE;
			try {
				doc.insertString(doc.getLength(), message, attr_normal);
			} catch (BadLocationException e) {
				System.err.println(e);
			}
			trimLog();
			textLog.setCaretPosition(doc.getLength());
			//if file logging on write to file
			if (logFileOn) {
				try {
					logFileWriter.write(messageFile, 0, messageFile.length());
					logFileWriter.flush();
				} catch (IOException ioe) {
					logFileOn = false;
					errorOutln("Unable to write to log file, logging turned off [JamConsole]");
				}
			}
			//unlock text area and notify others they can use it
			msgLock = false;
			notifyAll();
		} else {
			System.err.println("Error not a valid message part [JamConsole]");
		}
	}

	/**
	 * Output a message so it will be continued on the same line.
	 */
	public synchronized void messageOut(String message) {
		messageOut(message, CONTINUE);
	}

	/**
	 * Output a message with a carriage return.
	 *
	 * @param _message the message to be printed to the console
	 */
	public synchronized void messageOutln(String _message) {
		String message=new String(_message);
		msgLock = true;
		messageFile = getDate() + ">" + message + END_LINE;
		message = END_LINE + getTime() + ">" + message;
		try {
			doc.insertString(doc.getLength(), message, attr_normal);
		} catch (BadLocationException e) {
			System.err.println(e);
		}
		trimLog();
		textLog.setCaretPosition(doc.getLength());
		//if file logging on write to file
		if (logFileOn) {
			try {
				logFileWriter.write(messageFile, 0, messageFile.length());
				logFileWriter.flush();
			} catch (IOException ioe) {
				logFileOn = false;
				errorOutln("Unable to write to log file, logging turned off [JamConsole]");
			}
		}
		//unlock text area and notify others they can use it
		msgLock = false;
		notifyAll();
		//FIXME get rid of
		newMessage = true;
	}

	/**
	 * Writes an error message to the console immediately.
	 */
	public void errorOutln(String message) {
		promptOutln("Error: " + message, attr_error);
	}

	/**
	 * Outputs a warning message to the console immediately.
	 */
	public void warningOutln(String message) {
		promptOutln("Warning: " + message, attr_warning);
	}

	private synchronized void promptOutln(final String _message, AttributeSet attr) {
		String message=new String(_message);
		/* Dont wait for lock.  
		 * Output message right away. */
		if (msgLock) { //if locked add extra returns
			messageFile = END_LINE + getDate() + ">" + message + END_LINE;
			message = END_LINE + getTime() + ">" + message + END_LINE;
		} else { //normal message
			messageFile = getDate() + ">" + message + END_LINE;
			message = END_LINE + getTime() + ">" + message;
		}
		try {
			doc.insertString(doc.getLength(), message, attr);
		} catch (BadLocationException e) {
			System.err.println(e);
		}
		trimLog();
		textLog.setCaretPosition(doc.getLength());
		/* beep */
		Toolkit.getDefaultToolkit().beep();
		if (logFileOn) { //if file logging on write to file
			try {
				logFileWriter.write(messageFile, 0, messageFile.length());
				logFileWriter.flush();
			} catch (IOException ioe) {
				logFileOn = false;
				errorOutln("Unable to write to log file, logging turned off [JamConsole]");
			}
		}
	}

	/**
	 * Where to send commands that are input
	 * need to add types
	 *
	 */
	public void setCommandListener(CommandListener msgCommand) {
		currentListener = msgCommand;
	}

	public static final String INTS_ONLY="int";
	
	/**
	 * Parses the command and issues it to the current listener.
	 */
	private void parseCommand(String _inString) {
		int countParam = 0;
		/* make string tokenizer use spaces, commas, and returns as delimiters */
		String inString = _inString.trim();
		StringTokenizer inLine = new StringTokenizer(inString, " ,END_LINE");
		int numberInWords = inLine.countTokens();
		if (inLine.hasMoreTokens()) {//check at least something was entered
			int [] parameters=new int[numberInWords];
			String command = inLine.nextToken();
			try {// try to see if first token is a number
				parameters[countParam] = Integer.parseInt(command);
				//if we got this far first token is a int
				countParam++;
				command = INTS_ONLY;
			} catch (NumberFormatException nfe) {
				/* reset parameter list to hold one less */
				parameters = new int[numberInWords - 1];
				countParam = 0;
			}
			try {// rest of tokens must be numbers
				while (inLine.hasMoreTokens()) {
					parameters[countParam] =
						Integer.parseInt(inLine.nextToken());
					countParam++;
				}
			} catch (NumberFormatException nfe) {
				errorOutln("Input not a integer");
			}
			if (currentListener != null) {//perform command
				currentListener.commandPerform(command, parameters);
			} else {
				warningOutln("No current Listener for commands [JamConsole");
			}
		}
	}

	/**
	 * Create a file for the log to be saved to.
	 * The method appends a number (starting at 1) to the file name
	 * if the file already exists.
	 *
	 * @exception   JamException    exceptions that go to the console
	 */
	public String setLogFileName(String name) throws JamException {
		String newName = name + ".log";
		File file = new File(newName);
		/* create a unique file, append a number if a 
		 * log already exits */
		int i = 1;
		while (file.exists()) {
			newName = name + i + ".log";
			file = new File(newName);
			i++;
		}
		try {
			logFileWriter = new BufferedWriter(new FileWriter(file));

		} catch (IOException ioe) {
			throw new JamException("Not able to create log file " + newName);
		}
		logFileName = newName;
		return newName;
	}

	/**
	 * Close the log file
	 *
	 * @exception   JamException    exceptions that go to the console
	 */
	public void closeLogFile() throws JamException {
		try {
			logFileWriter.flush();
			logFileWriter.close();
		} catch (IOException ioe) {
			throw new JamException("Could not close log file  [JamConsole]");
		}
	}

	/**
	 * Turn on the logging to a file
	 *
	 * @exception   JamException    exceptions that go to the console
	 */
	public void setLogFileOn(boolean state) throws JamException {
		if (logFileWriter != null) {
			logFileOn = state;
		} else {
			logFileOn = false;
			throw new JamException("Cannot turn on logging to file, log file does not exits  [JamConsole]");
		}
	}

	/**
	 * Trim the text on screen Log so it does not get too long
	 */
	private void trimLog() {
		numberLines++;
		if (numberLines > maxLines) { //get rid of top line
			numberLines--;
			try{
				doc.remove(0,textLog.getText().indexOf(END_LINE)+END_LINE.length());
			} catch (BadLocationException ble){
				System.err.println(ble);
			}
		}
	}

	/**
	 * get the current time
	 */
	private String getTime() {
		final Date date = new java.util.Date(); //get time
		final DateFormat datef = DateFormat.getTimeInstance(DateFormat.MEDIUM);
		datef.setTimeZone(TimeZone.getDefault()); //set time zone
		final String stime = datef.format(date); //format time
		return stime;
	}

	/**
	 * Get the current date and time
	 */
	private String getDate() {
		final Date date = new java.util.Date(); //get time
		final DateFormat datef = DateFormat.getDateTimeInstance(); //medium date time format
		datef.setTimeZone(TimeZone.getDefault()); //set time zone
		final String stime = datef.format(date); //format time
		return stime;
	}

	/**
	 * On a class destruction close log file
	 */
	protected void finalize() {
		try {
			if (logFileOn) {
				closeLogFile();
			}
		} catch (Exception e) {
			System.err.println(
				"Error closing log file in finalize [JamConsole]");
		}
	}
}
