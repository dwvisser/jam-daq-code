package jam;
import jam.commands.CommandManager;
import jam.global.CommandListener;
import jam.global.JamStatus;
import jam.global.MessageHandler;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

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
public class JamConsole extends JPanel implements MessageHandler {

	/**
	 * Number of lines in scrollback log.
	 */
	private final static int NUM_LINES = 100;
	
	/**
	 * Command stack size.
	 */
	private final static int STACK_SIZE = 50;

	/**
	 * End of line character(s).
	 */
	private static final String END_LINE =
		(String) System.getProperty("line.separator");

	private final java.util.List listenerList =
		Collections.synchronizedList(new ArrayList());
	private final JTextPane textLog = new JTextPane(); //output text area
	private final Document doc = textLog.getStyledDocument();
	private final SimpleAttributeSet attr_normal, attr_warning, attr_error;
	private final JTextField textIn; //input text field
	private final JScrollPane jsp;
	private final LinkedList cmdStack = new LinkedList();
	private int lastCmdIndex;

	/**
	 * A lock for message output so messages don't overlap.
	 */
	private boolean msgLock;

	/**
	 * Private.
	 *
	 * @serial
	 */
	private final int maxLines;
	private int numberLines; //number of lines in output

	/**
	 * Private.
	 *
	 * @serial
	 */
	private BufferedWriter logWriter; //output stream

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
		this(NUM_LINES);
	}

	/**
	 *Constructor:
	 * Create a JamConsole which has an text area for output
	 * a text field for intput
	 */
	public JamConsole(int linesLog) {
		JamStatus.instance().setMessageHandler(this);
		SetupSortOn.createSingletonInstance(this);
		maxLines = linesLog;
		setLayout(new BorderLayout());
		textLog.setToolTipText(
			"After setup, this log is (usually) written to a file, too.");
		attr_normal = new SimpleAttributeSet();
		attr_warning = new SimpleAttributeSet();
		StyleConstants.setForeground(attr_warning, Color.blue);
		attr_error = new SimpleAttributeSet();
		StyleConstants.setForeground(attr_error, Color.red);
		textLog.setEditable(false);
		jsp =
			new JScrollPane(
				textLog,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.add(jsp, BorderLayout.CENTER);

		textIn = new JTextField();
		textIn.setToolTipText(
			"Enter underlined characters from buttons to start a command.");
		this.add(textIn, BorderLayout.SOUTH);
		textIn.addActionListener(new ActionListener() {
			/* Processes event when a return is hit in input field */
			public void actionPerformed(ActionEvent event) {
				addCommand(textIn.getText());
				parseCommand(textIn.getText());
				textIn.setText(null);
			}
		});
		/* Handle up and down arrows */
		textIn.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				int keyCode = evt.getKeyCode();
				if (keyCode == KeyEvent.VK_UP) {
					previousCommand(-1);
				} else if (keyCode == KeyEvent.VK_DOWN) {
					previousCommand(1);
				}

			}
		});
		msgLock = false;
		numberLines = 1;
		logFileOn = false;
		final int defaultLines = 8;
		final int lineHeight =
			textLog.getFontMetrics(textLog.getFont()).getHeight();
		final int logHeight = lineHeight * defaultLines;
		textLog.setPreferredSize(new Dimension(700, logHeight));
		addCommandListener(CommandManager.getInstance());
	}

	/**
	 * Add a command to the command stack
	 * 
	 * @param cmdStr 
	 */
	private void addCommand(String cmdStr) {
		cmdStack.add(cmdStr);
		if (cmdStack.size() > STACK_SIZE){
			cmdStack.removeFirst();
		}
		lastCmdIndex = cmdStack.size();
	}
	/**
	 * Get a previous command from the command stack.
	 * 
	 * @param direction >0 forward, <0 backward 
	 */
	private void previousCommand(int direction) {
		if (direction < 0) {
			if (lastCmdIndex > 0){
				lastCmdIndex = lastCmdIndex - 1;
			}
			textIn.setText((String) cmdStack.get(lastCmdIndex));
		} else {
			if (lastCmdIndex < cmdStack.size()) {
				lastCmdIndex = lastCmdIndex + 1;
				if (lastCmdIndex < cmdStack.size()){
					textIn.setText((String) cmdStack.get(lastCmdIndex));
				} else {
					textIn.setText("");
				}
			}
		}
	}

	/**
	 * Outputs the string as a message to the console, which has more than one part,
	 * so message can continued by a subsequent call.
	 *
	 * @param _message the message to be output
	 * @param part one of NEW, CONTINUE, or END
	 */
	public synchronized void messageOut(String _message, int part) {
		//String message = new String(_message);
		final StringBuffer message=new StringBuffer(_message);
		if (part == NEW) {
			msgLock = true;
			messageFile = getDate() + ">" + message;
			//message = END_LINE + getTime() + ">" + message;
			message.insert(0,'>').insert(0,getTime()).insert(0,END_LINE);
			try {
				doc.insertString(doc.getLength(), message.toString(), attr_normal);
			} catch (BadLocationException e) {
				JOptionPane.showMessageDialog(
					this,
					e.getMessage(),
					getClass().getName(),
					JOptionPane.ERROR_MESSAGE);
			}
		} else if (part == CONTINUE) {
			messageFile = messageFile + message;
			try {
				doc.insertString(doc.getLength(), message.toString(), attr_normal);
			} catch (BadLocationException e) {
				JOptionPane.showMessageDialog(
					this,
					e.getMessage(),
					getClass().getName(),
					JOptionPane.ERROR_MESSAGE);
			}
		} else if (part == END) {
			messageFile = messageFile + message + END_LINE;
			try {
				doc.insertString(doc.getLength(), message.toString(), attr_normal);
			} catch (BadLocationException e) {
				JOptionPane.showMessageDialog(
					this,
					e.getMessage(),
					getClass().getName(),
					JOptionPane.ERROR_MESSAGE);
			}
			trimLog();
			textLog.setCaretPosition(doc.getLength());
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
			//unlock text area and notify others they can use it
			msgLock = false;
			notifyAll();
		} else {
			throw new IllegalArgumentException("Error not a valid message part [JamConsole]");
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
		String message = new String(_message);
		msgLock = true;
		messageFile = getDate() + ">" + message + END_LINE;
		message = END_LINE + getTime() + ">" + message;
		try {
			doc.insertString(doc.getLength(), message, attr_normal);
		} catch (BadLocationException e) {
			JOptionPane.showMessageDialog(
				this,
				e.getMessage(),
				getClass().getName(),
				JOptionPane.ERROR_MESSAGE);
		}
		trimLog();
		textLog.setCaretPosition(doc.getLength());
		//if file logging on write to file
		if (logFileOn) {
			try {
				logWriter.write(messageFile, 0, messageFile.length());
				logWriter.flush();
			} catch (IOException ioe) {
				logFileOn = false;
				errorOutln("Unable to write to log file, logging turned off [JamConsole]");
			}
		}
		//unlock text area and notify others they can use it
		msgLock = false;
		notifyAll();
	}
	
	private final String EMPTY="";
	public synchronized void messageOutln(){
		messageOutln(EMPTY);
	}

	/**
	 * Writes an error message to the console immediately.
	 */
	public synchronized void errorOutln(String message) {
		promptOutln("Error: " + message, attr_error);
	}

	/**
	 * Outputs a warning message to the console immediately.
	 */
	public synchronized void warningOutln(String message) {
		promptOutln("Warning: " + message, attr_warning);
	}

	private synchronized void promptOutln(
		final String _message,
		AttributeSet attr) {
		String message = new String(_message);
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
			JOptionPane.showMessageDialog(
				this,
				e.getMessage(),
				getClass().getName(),
				JOptionPane.ERROR_MESSAGE);
		}
		trimLog();
		textLog.setCaretPosition(doc.getLength());
		/* beep */
		Toolkit.getDefaultToolkit().beep();
		if (logFileOn) { //if file logging on write to file
			try {
				logWriter.write(messageFile, 0, messageFile.length());
				logWriter.flush();
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
	public final void addCommandListener(CommandListener msgCommand) {
		listenerList.add(msgCommand);
	}

	public static final String NUMBERS_ONLY = "int";

	/**
	 * Parses the command and issues it to the current listener.
	 */
	private void parseCommand(final String _inString) {
		final String[] cmdTokens = parseExpression(_inString);
		final int numberInWords = cmdTokens.length;
		/* make string tokenizer use spaces, commas, and returns as delimiters */
		if (cmdTokens.length > 0) { //check at least something was entered
			final String command;
			final String[] parameters;
			final int countWrd;
			if (isNumber(cmdTokens[0])) {
				/* first token is a number, command is NUMBER_ONLY 
				 * and params starts with first token */
				command = NUMBERS_ONLY;
				parameters = new String[numberInWords];
				countWrd = 0;
			} else {
				/* parameter list to hold one less */
				command = cmdTokens[0];
				parameters = new String[numberInWords - 1];
				countWrd = 1;
			}
			/* Load parameter tokens */
			System.arraycopy(
				cmdTokens,
				countWrd,
				parameters,
				0,
				numberInWords - countWrd);
			/* perform command */
			notifyListeners(command, parameters);
		}
	}

	/**
	 * Parse the input string 
	 * @param strCmd
	 * @return a array of the command tokens
	 */
	private String[] parseExpression(final String strCmd) {
		final List cmdTokenList = new ArrayList();
		/* match anything between quotes or words (not spaces) */
		final String regex = "\"([^\"]*?)\"|(\\S+)\\s*";
		final Pattern pattern = Pattern.compile(regex);
		final Matcher matcher = pattern.matcher(strCmd);
		while (matcher.find()) {
			String cmdToken = matcher.group().trim();
			if (cmdToken.charAt(0) == '\"')
				cmdToken = cmdToken.substring(1, cmdToken.length() - 1);
			if (cmdToken != null) {
				cmdTokenList.add(cmdToken);
			}
		}
		final String[] cmdTokens = new String[cmdTokenList.size()];
		for (int i = 0; i < cmdTokenList.size(); i++) {
			cmdTokens[i] = (String) cmdTokenList.get(i);
		}
		return cmdTokens;
	}

	private boolean isNumber(String s) {
		boolean rval;
		try {
			if (s.indexOf('.') >= 0) {
				Double.parseDouble(s);
			} else {
				Integer.parseInt(s);
			}
			rval = true;
		} catch (NumberFormatException nfe) {
			rval = false;
		}
		return rval;
	}

	/**
	 * Create a file for the log to be saved to.
	 * The method appends a number (starting at 1) to the file name
	 * if the file already exists.
	 *
	 * @exception   JamException    exceptions that go to the console
	 */
	String setLogFileName(String name) throws JamException {
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
			logWriter = new BufferedWriter(new FileWriter(file));

		} catch (IOException ioe) {
			throw new JamException("Not able to create log file " + newName);
		}
		return newName;
	}

	/**
	 * Close the log file
	 *
	 * @exception   JamException    exceptions that go to the console
	 */
	void closeLogFile() throws JamException {
		try {
			logWriter.flush();
			logWriter.close();
		} catch (IOException ioe) {
			throw new JamException("Could not close log file  [JamConsole]");
		}
	}

	/**
	 * Turn on the logging to a file
	 *
	 * @exception   JamException    exceptions that go to the console
	 */
	void setLogFileOn(boolean state) throws JamException {
		if (logWriter != null) {
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
			try {
				doc.remove(
					0,
					textLog.getText().indexOf(END_LINE) + END_LINE.length());
			} catch (BadLocationException e) {
				JOptionPane.showMessageDialog(
					this,
					e.getMessage(),
					getClass().getName(),
					JOptionPane.ERROR_MESSAGE);
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
		final DateFormat datef = DateFormat.getDateTimeInstance();
		//medium date time format
		datef.setTimeZone(TimeZone.getDefault()); //set time zone
		final String stime = datef.format(date); //format time
		return stime;
	}

	/**
	 * Send console command to all registered listeners.
	 * 
	 * @param cmd the command
	 * @param params list of parameters
	 */
	private void notifyListeners(String cmd, String[] params) {
		final Iterator it = listenerList.iterator();
		boolean validListenerFound = false;
		while (it.hasNext()) {
			CommandListener cl = (CommandListener) (it.next());
			validListenerFound |= cl.performParseCommand(cmd, params);
		}
		if (!validListenerFound) {
			final StringBuffer sb = new StringBuffer("\"");
			sb.append(cmd).append("\" is an invalid command. ");
			final String[] offer =
				CommandManager.getInstance().getSimilarCommnands(cmd, true);
			if (offer.length > 0) {
				sb.append("Maybe you meant ");
				if (offer.length > 1) {
					sb.append("one of the following:\t");
				} else {
					sb.append(":\t");
				}
				for (int i = 0; i < offer.length; i++) {
					sb.append(offer[i]).append("\t");
				}
			}
			errorOutln(sb.toString());
		}
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
			JOptionPane.showMessageDialog(
				this,
				e.getMessage(),
				getClass().getName(),
				JOptionPane.ERROR_MESSAGE);
		}
	}

}
