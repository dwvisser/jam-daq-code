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
 * Class Console displays a output of commands and error messages and allows the
 * input of commands using the keyboard.
 * 
 * @author Ken Swartz
 * @author Dale Visser
 * @version 1.2 alpha last edit 15 Feb 2000
 * @version 0.5 last edit 11-98
 * @version 0.5 last edit 1-99
 */
public class FitConsole extends JPanel implements MessageHandler {

	private static final String EMPTY = "";

	/**
	 * End of line character(s).
	 */
	private static final String END_LINE = System.getProperty("line.separator");

	private transient final SimpleAttributeSet attr_normal, attr_warning,
			attr_error;

	private transient final Document doc;

	private transient final int maxLines;

	/** A lock for message output so messages dont overlap. */
	private transient boolean msgLock;

	private transient int numberLines; // number of lines in output

	private transient final JTextPane textLog; // output text area

	private transient final JScrollBar verticalBar;

	/**
	 * Create a JamConsole which has an text area for output a text field for
	 * intput.
	 */
	public FitConsole() {
		this(100);
	}

	/**
	 * Constructs a FitConsole which has an text area for output a text field
	 * for input.
	 * 
	 * @param linesLog
	 *            number of lines to hold in text area
	 */
	public FitConsole(final int linesLog) {
		super(new BorderLayout());
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
		add(jsp, BorderLayout.CENTER);
		verticalBar = jsp.getVerticalScrollBar();
		msgLock = false;
		numberLines = 1;
	}

	/**
	 * Writes an error message to the console immediately.
	 */
	public void errorOutln(final String message) {
		promptOutln("Error: " + message, attr_error);
	}

	/**
	 * Output a message so it will be continued on the same line.
	 */
	public void messageOut(final String message) {
		messageOut(message, CONTINUE);
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
		synchronized (this) {
			final StringBuffer message = new StringBuffer(_message);
			if (part == NEW) {
				msgLock = true;
				message.insert(0, END_LINE);
				try {
					doc.insertString(doc.getLength(), message.toString(),
							attr_normal);
				} catch (BadLocationException e) {
					JOptionPane.showMessageDialog(this, e.getMessage(),
							getClass().getName(), JOptionPane.ERROR_MESSAGE);
				}
			} else if (part == CONTINUE) {
				try {
					doc.insertString(doc.getLength(), message.toString(),
							attr_normal);
				} catch (BadLocationException e) {
					JOptionPane.showMessageDialog(this, e.getMessage(),
							getClass().getName(), JOptionPane.ERROR_MESSAGE);
				}
			} else if (part == END) {
				try {
					doc.insertString(doc.getLength(), message.toString(),
							attr_normal);
				} catch (BadLocationException e) {
					JOptionPane.showMessageDialog(this, e.getMessage(),
							getClass().getName(), JOptionPane.ERROR_MESSAGE);
				}
				trimLog();
				textLog.setCaretPosition(doc.getLength());
				verticalBar.setValue(verticalBar.getMaximum());
				/* if file logging on write to file */
				/* unlock text area and notify others they can use it */
				msgLock = false;
				notifyAll();
			} else {
				throw new IllegalArgumentException(
						"Error not a valid message part [FitConsole]");
			}
		}
	}

	public void messageOutln() {
		messageOutln(EMPTY);
	}

	/**
	 * Output a message with a carriage return.
	 * 
	 * @param _message
	 *            the message to be printed to the console
	 */
	public void messageOutln(final String _message) {
		synchronized (this) {
			final StringBuffer message = new StringBuffer(_message);
			msgLock = true;
			message.insert(0, END_LINE);
			try {
				doc.insertString(doc.getLength(), message.toString(),
						attr_normal);
			} catch (BadLocationException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), getClass()
						.getName(), JOptionPane.ERROR_MESSAGE);
			}
			trimLog();
			textLog.setCaretPosition(doc.getLength());
			/* unlock text area and notify others they can use it */
			msgLock = false;
			notifyAll();
		}
	}

	private void promptOutln(final String _message, final AttributeSet attr) {
		synchronized (this) {
			final StringBuffer message = new StringBuffer(_message);
			/*
			 * Don't wait for lock. Output message right away.
			 */
			message.insert(0, END_LINE);
			if (msgLock) { // if locked add extra returns
				message.append(END_LINE);
			}
			try {
				doc.insertString(doc.getLength(), message.toString(), attr);
			} catch (BadLocationException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), getClass()
						.getName(), JOptionPane.ERROR_MESSAGE);
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
			} catch (BadLocationException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), getClass()
						.getName(), JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Outputs a warning message to the console immediately.
	 */
	public void warningOutln(final String message) {
		promptOutln("Warning: " + message, attr_warning);
	}

}
