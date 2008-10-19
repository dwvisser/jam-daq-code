package jam.commands;

import jam.global.Broadcaster;
import jam.global.CommandListenerException;
import jam.global.JamStatus;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * Implementation of <code>Commandable</code> interface in which
 * <code>actionPerformed()</code> executes <code>performCommand(null)</code>,
 * which in turn executes the abstract method, <code>execute(null)</code.
 * 
 * @author Ken Swartz
 */
public abstract class AbstractCommand extends AbstractAction implements
		Commandable {

	/**
	 * logger instance for all commands
	 */
	protected static final Logger LOGGER = Logger
			.getLogger(AbstractCommand.class.getPackage().getName());

	/**
	 * Reference to <code>JamStatus</code> singleton available to all
	 * implementing classes.
	 */
	protected static final JamStatus STATUS = JamStatus.getSingletonInstance();

	/**
	 * Reference to <code>Broadcaster</code> singleton available to all
	 * implementing classes.
	 */
	protected static final Broadcaster BROADCASTER = Broadcaster
			.getSingletonInstance();

	/**
	 * Constructor.
	 */
	AbstractCommand() {
		super();
	}

	/**
	 * Provided so subclasses can override default constructor with a name.
	 * 
	 * @param name
	 *            of command
	 */
	protected AbstractCommand(final String name) {
		super(name);
	}

	/**
	 * Default implementation that does nothing.
	 */
	public void initCommand() {
		// default do-nothing
	}

	public final void actionPerformed(final ActionEvent actionEvent) {
		try {
			performCommand(null);
		} catch (CommandException e) {
			LOGGER.throwing(getClass().getName(), "actionPerformed", e);
			LOGGER.severe(e.getMessage());
		}
	}

	/**
	 * Perform a command and log it. This calls <code>execute()</code> with the
	 * given parameters.
	 * 
	 * @param cmdParams
	 *            the command parameters
	 */
	public void performCommand(final Object[] cmdParams)
			throws CommandException {
		try {
			execute(cmdParams);
			logCommand();
		} catch (CommandException e) {
			logError();
			throw new CommandException(e);
		}
	}

	/**
	 * Perform a command and log it. This calls <code>executeParse()</code> with
	 * the given parameters.
	 * 
	 * @param strCmdParams
	 *            the command parameters as strings
	 */
	public void performParseCommand(final String[] strCmdParams)
			throws CommandListenerException {
		try {
			executeParse(strCmdParams);
			logCommand();
		} catch (Exception e) {
			logError();
			throw new CommandListenerException(e);
		}
	}

	/**
	 * Log the command, does nothing yet.
	 * 
	 */
	public void logCommand() {
		// does nothing at the moment
	}

	/**
	 * Log a command error, does nothing yet.
	 * 
	 */
	public void logError() {
		// does nothing at the moment
	}

	/**
	 * Load an icon from the given path and return it.
	 * 
	 * @param path
	 *            that the icon lies at
	 * @return the icon if successful, <code>null</code> if not
	 */
	protected Icon loadToolbarIcon(final String path) {
		/* buttons initialized with text if icon==null */
		Icon rval = null;
		final ClassLoader loader = this.getClass().getClassLoader();
		final URL urlResource = loader.getResource(path);
		if (urlResource == null) {
			JOptionPane.showInputDialog(null, "Can't load resource: " + path,
					"Missing Icon", JOptionPane.ERROR_MESSAGE);
		} else { // instead use path, ugly but lets us see button
			rval = new ImageIcon(urlResource);
		}
		return rval;
	}

	/**
	 * Execute a command with the given command parameters.
	 * 
	 * @param cmdParams
	 *            command parameters
	 * @throws CommandException
	 *             if an error occurs
	 */
	protected abstract void execute(Object[] cmdParams) throws CommandException;

	/**
	 * Execute a command with the given command string tokens.
	 * 
	 * @param cmdTokens
	 *            command parameters as string
	 * @throws CommandListenerException
	 *             if an error occurs
	 */
	protected abstract void executeParse(String[] cmdTokens)
			throws CommandListenerException;
}
