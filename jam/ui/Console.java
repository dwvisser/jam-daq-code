package jam.ui;

import injection.MapListener;
import jam.global.CommandFinder;
import jam.global.CommandListener;
import jam.global.LoggerConfig;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;
import javax.swing.JTextField;

import com.google.inject.Inject;
import com.google.inject.Singleton;

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
@Singleton
public class Console extends JPanel {

	/**
	 * Command expected when input is numbers only.
	 */
	public static final String NUMBERS_ONLY = "int";

	/**
	 * Command stack size.
	 */
	private final static int STACK_SIZE = 50;

	private transient final LinkedList<String> cmdStack = new LinkedList<>();// NOPMD

	private transient final CommandFinder commandFinder;

	private transient final ConsoleLog consoleLog;

	private transient int lastCmdIndex;

	private transient final List<CommandListener> listenerList = Collections
			.synchronizedList(new ArrayList<CommandListener>());

	private transient final JTextField textIn = new JTextField();

	/**
	 * Constructs a JamConsole which has an text area for output a text field
	 * for input.
	 * 
	 * @param consoleLog
	 *            log of console activity
	 * @param finder
	 *            finds commands
	 * @param listener
	 *            listens to commands
	 */
	@Inject
	public Console(final ConsoleLog consoleLog, final CommandFinder finder,
			final @MapListener CommandListener listener) {
		super(new BorderLayout());
		this.commandFinder = finder;
		this.consoleLog = consoleLog;
		new LoggerConfig("jam", consoleLog);
		this.add(consoleLog.getComponent(), BorderLayout.CENTER);
		textIn
				.setToolTipText("Enter underlined characters from buttons to start a command.");
		this.add(textIn, BorderLayout.SOUTH);
		/* Processes event when a return is hit in input field */
        textIn.addActionListener(event -> {
            addCommand(textIn.getText());
            parseCommand(textIn.getText());
            textIn.setText(null);
        });
		/* Handle up and down arrows */
		textIn.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent evt) {
				final int keyCode = evt.getKeyCode();
				if (keyCode == KeyEvent.VK_UP) {
					previousCommand(-1);
				} else if (keyCode == KeyEvent.VK_DOWN) {
					previousCommand(1);
				}

			}
		});
		addCommandListener(listener);
	}

	/**
	 * Add a command to the command stack
	 * 
	 * @param cmdStr
	 */
	private void addCommand(final String cmdStr) {
		cmdStack.add(cmdStr);
		if (cmdStack.size() > STACK_SIZE) {
			cmdStack.removeFirst();
		}
		lastCmdIndex = cmdStack.size();
	}

	/**
	 * Where to send commands that are input need to add types.
	 * 
	 * @param msgCommand
	 *            listener to add
	 */
	public final void addCommandListener(final CommandListener msgCommand) {
		listenerList.add(msgCommand);
	}

	private boolean isNumber(final String string) {
		boolean rval;
		try {
			if (string.indexOf('.') >= 0) {
				Double.parseDouble(string);
			} else {
				Integer.parseInt(string);
			}
			rval = true;
		} catch (NumberFormatException nfe) {
			rval = false;
		}
		return rval;
	}

	/**
	 * Send console command to all registered listeners.
	 * 
	 * @param cmd
	 *            the command
	 * @param params
	 *            list of parameters
	 */
	private void notifyListeners(final String cmd, final String[] params) {
		boolean found = false;
		for (CommandListener listener : listenerList) {
			found |= listener.performParseCommand(cmd, params);
		}
		if (!found) {
			final StringBuilder buffer = new StringBuilder();
			buffer.append('\"').append(cmd)
					.append("\" is an invalid command. ");
			final Collection<String> offer = commandFinder
					.getSimilar(cmd, true);
			if (!offer.isEmpty()) {
				buffer.append("Maybe you meant ");
				if (offer.size() > 1) {
					buffer.append("one of the following:\t");
				} else {
					buffer.append(":\t");
				}
				for (String command : offer) {
					buffer.append(command).append('\t');
				}
			}
			consoleLog.errorOutln(buffer.toString());
		}
	}

	/*
	 * non-javadoc: Parses the command and issues it to the current listener.
	 */
	private void parseCommand(final String _inString) {
		final List<String> cmdTokens = parseExpression(_inString);
		final int numWords = cmdTokens.size();
		/* make string tokenizer use spaces, commas, and returns as delimiters */
		if (numWords > 0) { // check at least something was entered
			final String command;
			final String[] parameters;
			final int initIndex;
			final String first = cmdTokens.get(0);
			if (isNumber(first)) {
				/*
				 * first token is a number, command is NUMBER_ONLY and params
				 * starts with first token
				 */
				command = NUMBERS_ONLY;
				parameters = new String[numWords];
				initIndex = 0;
			} else {
				/* parameter list to hold one less */
				command = first;
				parameters = new String[numWords - 1];
				initIndex = 1;
			}
			/* Load parameter tokens */
			if (parameters.length > 0) {
				System.arraycopy(cmdTokens.toArray(), initIndex, parameters, 0,
						numWords - initIndex);
			}
			/* perform command */
			notifyListeners(command, parameters);
		}
	}

	/**
	 * Parse the input string
	 * 
	 * @param strCmd
	 * @return a array of the command tokens
	 */
	private List<String> parseExpression(final String strCmd) {
		final List<String> rval = new ArrayList<>();
		/* match anything between quotes or words (not spaces) */
		final String regex = "\"([^\"]*?)\"|(\\S+)\\s*";
		final Pattern pattern = Pattern.compile(regex);
		final Matcher matcher = pattern.matcher(strCmd);
		while (matcher.find()) {
			String cmdToken = matcher.group().trim();
			if (cmdToken.charAt(0) == '\"') {
				cmdToken = cmdToken.substring(1, cmdToken.length() - 1);
			}
			if (cmdToken != null) {
				rval.add(cmdToken);
			}
		}
		return rval;
	}

	/**
	 * Get a previous command from the command stack.
	 * 
	 * @param direction
	 *            >0 forward, <0 backward
	 */
	private void previousCommand(final int direction) {
		if (direction < 0) {
			if (lastCmdIndex > 0) {
				lastCmdIndex = lastCmdIndex - 1;
			}
			textIn.setText(cmdStack.get(lastCmdIndex));
		} else {
			if (lastCmdIndex < cmdStack.size()) {
				lastCmdIndex = lastCmdIndex + 1;
				if (lastCmdIndex < cmdStack.size()) {
					textIn.setText(cmdStack.get(lastCmdIndex));
				} else {
					textIn.setText("");
				}
			}
		}
	}

}
