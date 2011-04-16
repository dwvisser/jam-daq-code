package jam.commands;

import injection.GuiceInjector;
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
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.google.inject.Inject;

/**
 * Class to create commands and execute them
 * @author Ken Swartz
 */
public final class CommandManager implements CommandListener, ActionCreator {

    private static final Map<String, Commandable> INSTANCES = Collections
            .synchronizedMap(new HashMap<String, Commandable>());

    private static final Logger LOGGER = Logger.getLogger(CommandManager.class
            .getPackage().getName());

    private transient final CommandMap commandMap = new CommandMap(this);

    private transient Commandable currentCom;

    private transient final Broadcaster broadcaster;

    /**
     * Constructor private as singleton
     */
    @Inject
    protected CommandManager(final Broadcaster broadcaster) {
        super();
        this.broadcaster = broadcaster;
    }

    /**
     * See if we have the instance created, create it if necessary, and return
     * whether it was successfully created.
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
            currentCom = new NoCommand();
            final boolean created = INSTANCES.containsKey(strCmd);
            if (created) {
                currentCom = INSTANCES.get(strCmd);
            } else {
                currentCom = GuiceInjector.getObjectInstance(cmdClass);
                currentCom.initCommand();
                if (currentCom instanceof Observer) {
                    this.broadcaster.addObserver((Observer) currentCom);
                }
                INSTANCES.put(strCmd, currentCom);
            }
        }
        return exists;
    }

    /**
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
     * Produce a menu item that invokes the action given by the lookup table in
     * <code>jam.commands.CommandManager</code>
     * @param name
     *            name of the command
     * @return JMenuItem that invokes the associated action
     */
    public JMenuItem getMenuItem(final String name) {
        final Action action = this.getAction(name);
        if (null == action) {
            throw new IllegalArgumentException("Couldn't find action for '"
                    + name + "'.");
        }
        return new JMenuItem(action);
    }

    /**
     * @param name
     *            of menu
     * @param commandNames
     *            of commands
     * @return a menu
     */
    public JMenu createMenu(final String name, final String... commandNames) {
        final JMenu result = new JMenu(name);
        for (String commandName : commandNames) {
            result.add(this.getMenuItem(commandName));
        }

        return result;
    }

    /**
     * Perform command with string parameters
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
