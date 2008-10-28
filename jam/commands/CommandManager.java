package jam.commands;

import jam.global.Broadcaster;
import jam.global.CommandFinder;
import jam.global.CommandListener;
import jam.global.CommandListenerException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;

/**
 * Class to create commands and execute them
 * 
 * @author Ken Swartz
 */
public final class CommandManager implements CommandListener, ActionCreator {

	private static final Object classMonitor = new Object();

	private static CommandManager instance = null;

	private static final Map<String, Commandable> INSTANCES = Collections
			.synchronizedMap(new HashMap<String, Commandable>());

	private static final Logger LOGGER = Logger.getLogger(CommandManager.class
			.getPackage().getName());

	private static final Commandable NO_COMMAND = new NoCommand();

	private transient final CommandMap commandMap = new CommandMap(this);

	/**
	 * Singleton accessor.
	 * 
	 * @return the unique instance of this class
	 */
	public static CommandManager getInstance() {
		synchronized (classMonitor) {
			if (instance == null) {
				instance = new CommandManager();
			}
			return instance;
		}
	}

	private transient Commandable currentCom;

	/**
	 * Constructor private as singleton
	 * 
	 */
	private CommandManager() {
		super();
	}

	/**
	 * See if we have the instance created, create it if necessary, and return
	 * whether it was successfully created.
	 * 
	 * @param strCmd
	 *            name of the command
	 * @return <code>true</code> if successful, <code>false</code> if the given
	 *         command doesn't exist
	 */
	private boolean createCmd(final String strCmd) {
		final boolean exists = commandMap.containsKey(strCmd);
		if (exists) {
			final Class<? extends Commandable> cmdClass = commandMap
					.get(strCmd);
			currentCom = NO_COMMAND;
			final boolean created = INSTANCES.containsKey(strCmd);
			if (created) {
				currentCom = INSTANCES.get(strCmd);
			} else {
				try {
					currentCom = cmdClass.newInstance();
					currentCom.initCommand();
					if (currentCom instanceof Observer) {
						Broadcaster.getSingletonInstance().addObserver(
								(Observer) currentCom);
					}
				} catch (InstantiationException ie) {
					/*
					 * There was a problem resolving the command class or with
					 * creating an instance. This should never happen if
					 * exists==true.
					 */
					LOGGER.log(Level.SEVERE, ie.getMessage(), ie);
				} catch (IllegalAccessException iae) {
					LOGGER.log(Level.SEVERE, iae.getMessage(), iae);
				}
				INSTANCES.put(strCmd, currentCom);
			}
		}
		return exists;
	}

	/**
	 * 
	 * @param strCmd
	 *            the command to type
	 * @return the action
	 */
	public Action getAction(final String strCmd) {
		Action rval = null;
		if (createCmd(strCmd)) {
			rval = currentCom;
		}
		return rval;
	}

	/**
	 * Perform command with string parameters
	 * 
	 * @param strCmd
	 *            String key indicating the command
	 * @param strCmdParams
	 *            Command parameters as strings
	 * @return <code>true</code> if successful
	 */
	public boolean performParseCommand(final String strCmd,
			final String[] strCmdParams) {
		boolean validCommand = false;
		if (createCmd(strCmd)) {
			if (currentCom.isEnabled()) {
				try {
					currentCom.performParseCommand(strCmdParams);
				} catch (CommandListenerException cle) {
					LOGGER.log(Level.SEVERE, "Performing command " + strCmd
							+ "; " + cle.getMessage(), cle);
				}
			} else {
				LOGGER.severe("Disabled command \"" + strCmd + "\"");
			}
			validCommand = true;
		}
		return validCommand;
	}

	/**
	 * 
	 * @param cmd
	 *            the command to type
	 * @param enable
	 *            <code>true</code> if enabled
	 */
	public void setEnabled(final String cmd, final boolean enable) {
		final Action action = getAction(cmd);
		if (null == action) {
			throw new IllegalArgumentException("Couldn't find action for '"
					+ cmd + "'.");
		}

		getAction(cmd).setEnabled(enable);
	}

	/**
	 * @return the command finder associated with this command manager
	 */
	public CommandFinder getCommandFinder() {
		return this.commandMap;
	}
}// NOPMD
