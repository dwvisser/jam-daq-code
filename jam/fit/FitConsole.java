package jam.fit;
import jam.global.MessageHandler;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
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
public class FitConsole
	extends JPanel
	implements MessageHandler {
	

	/**
	 * End of line character(s).
	 */
	private static final String END_LINE = System.getProperty("line.separator");

	private final JTextPane textLog; //output text area
	private final Document doc;
	private final SimpleAttributeSet attr_normal, attr_warning, attr_error;
	final JScrollBar verticalBar;

	/** A lock for message output so message dont overlap. */
	private boolean msgLock;
	
	/**
	 * Private.
	 *
	 * @serial
	 */
	private int maxLines;
	private int numberLines; //number of lines in output


	/**
	 * Create a JamConsole which has an text area for output
	 * a text field for intput.
	 */
	public FitConsole() {
		this(100);
	}

	/**
	 * Constructs a FitConsole which has an text area for output
	 * a text field for intput.
	 * 
	 * @param linesLog number of lines to hold in text area
	 */
	public FitConsole(int linesLog) {
		maxLines = linesLog;
		setLayout(new BorderLayout());
		textLog = new JTextPane();
		doc = textLog.getStyledDocument();
		attr_normal = new SimpleAttributeSet();
		attr_warning = new SimpleAttributeSet();
		StyleConstants.setForeground(attr_warning, Color.blue);
		attr_error = new SimpleAttributeSet();
		StyleConstants.setForeground(attr_error, Color.red);
		textLog.setEditable(false);
		final JScrollPane jsp =
			new JScrollPane(
				textLog,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(jsp, BorderLayout.CENTER);
		verticalBar=jsp.getVerticalScrollBar();
		msgLock = false;
		numberLines = 1;
	}

	/**
	 * Outputs the string as a message to the console, which has more than one part,
	 * so message can continued by a subsequent call.
	 *
	 * @param _message the message to be output
	 * @param part one of NEW, CONTINUE, or END
	 */
	public synchronized void messageOut(String _message, int part) {
		String message=String.valueOf(_message);
		if (part == NEW) {
			msgLock = true;
			message = END_LINE + message;
			try {
				doc.insertString(doc.getLength(), message, attr_normal);
			} catch (BadLocationException e) {
				JOptionPane.showMessageDialog(this,e.getMessage(),
				getClass().getName(),JOptionPane.ERROR_MESSAGE);
			}
		} else if (part == CONTINUE) {
			try {
				doc.insertString(doc.getLength(), message, attr_normal);
			} catch (BadLocationException e) {
				JOptionPane.showMessageDialog(this,e.getMessage(),
				getClass().getName(),JOptionPane.ERROR_MESSAGE);
			}
		} else if (part == END) {
			try {
				doc.insertString(doc.getLength(), message, attr_normal);
			} catch (BadLocationException e) {
				JOptionPane.showMessageDialog(this,e.getMessage(),
				getClass().getName(),JOptionPane.ERROR_MESSAGE);
			}
			trimLog();
			textLog.setCaretPosition(doc.getLength());
			verticalBar.setValue(verticalBar.getMaximum());
			/* if file logging on write to file */
			/* unlock text area and notify others they can use it */
			msgLock = false;
			notifyAll();
		} else {
			throw new IllegalArgumentException("Error not a valid message part [FitConsole]");
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
		String message=String.valueOf(_message);
		msgLock = true;
		message = END_LINE + message;
		try {
			doc.insertString(doc.getLength(), message, attr_normal);
		} catch (BadLocationException e) {
			JOptionPane.showMessageDialog(this,e.getMessage(),
			getClass().getName(),JOptionPane.ERROR_MESSAGE);
		}
		trimLog();
		textLog.setCaretPosition(doc.getLength());
		/* unlock text area and notify others they can use it */
		msgLock = false;
		notifyAll();
	}
	
	private static final String EMPTY="";
	public void messageOutln(){
		messageOutln(EMPTY);
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
		String message=String.valueOf(_message);
		/* Dont wait for lock.  
		 * Output message right away. */
		if (msgLock) { //if locked add extra returns
			message = END_LINE + message + END_LINE;
		} else { //normal message
			message = END_LINE + message;
		}
		try {
			doc.insertString(doc.getLength(), message, attr);
		} catch (BadLocationException e) {
			JOptionPane.showMessageDialog(this,e.getMessage(),
			getClass().getName(),JOptionPane.ERROR_MESSAGE);
		}
		trimLog();
		textLog.setCaretPosition(doc.getLength());
		/* beep */
		Toolkit.getDefaultToolkit().beep();
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
			} catch (BadLocationException e){
				JOptionPane.showMessageDialog(this,e.getMessage(),
				getClass().getName(),JOptionPane.ERROR_MESSAGE);
			}
		}
	}

}
