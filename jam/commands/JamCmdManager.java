package jam.commands;

import jam.global.CommandListener;
import jam.global.CommandListenerException;
import jam.global.CommandNames;
import jam.global.MessageHandler;

import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
/**
 * Class to create commands and execute them
 *
 * @author Ken Swartz
 */
public class JamCmdManager implements CommandListener {

	private final MessageHandler msghdlr;

	private static final Map cmdMap = new HashMap();
	/* initializer block for map */
	static {
		cmdMap.put(CommandNames.OPEN_HDF, OpenHDFCmd.class);
		cmdMap.put(CommandNames.SAVE_HDF, SaveHDFCmd.class);
		cmdMap.put(CommandNames.SAVE_AS_HDF, SaveAsHDFCmd.class);
		cmdMap.put(CommandNames.ADD_HDF, AddHDFCmd.class);
		cmdMap.put(CommandNames.RELOAD_HDF, ReloadHDFCmd.class);
		cmdMap.put(CommandNames.SHOW_NEW_HIST, ShowDialogNewHistogramCmd.class);
		cmdMap.put(CommandNames.EXIT, ShowDialogExitCmd.class);
		cmdMap.put(CommandNames.NEW, FileNewClearCmd.class);
		cmdMap.put(CommandNames.PARAMETERS, ShowDialogParametersCmd.class);
		cmdMap.put(CommandNames.DISPLAY_SCALERS, ShowDialogScalersCmd.class);
		cmdMap.put(CommandNames.SHOW_ZERO_SCALERS, ShowDialogZeroScalersCmd.class);
		cmdMap.put(CommandNames.SCALERS, ScalersCmd.class);
		cmdMap.put(CommandNames.EXPORT_TEXT, ExportTextFileCmd.class);		
	}

	private Commandable currentCommand;

	/**
	 * Constructor
	 *
	 * @param status
	 * @param msghdlr
	 * @param broadcaster
	 */
	public JamCmdManager(MessageHandler msghdlr) {
		this.msghdlr = msghdlr;
	}
	/**
	 * Create a new Action command
	 * 
	 * @param actionName action name as in CommandNames
	 * @return Action object
	 */
	public Action getAction(String actionName){
		createCmd(actionName);
		return  (Action)currentCommand;
	}
	/**
	 * Perform command with object parameters
	 *
	 * @param strCmd	String key indicating the command
	 * @param cmdParams	Command parameters
	 */
	public boolean performCommand(String strCmd, Object[] cmdParams)
		throws CommandException {
		boolean success=false;
		if (createCmd(strCmd)) {
			currentCommand.performCommand(cmdParams);
			success= true;
		}
		return success;
	}

	/**
	 * Perform command with string parameters
	 *
	 * @param strCmd 		String key indicating the command
	 * @param strCmdParams  Command parameters as strings
	 */
	public boolean performParseCommand(String strCmd, String[] strCmdParams) 
		throws CommandListenerException {
		boolean success=false;
		if (createCmd(strCmd)) {
			currentCommand.performParseCommand(strCmdParams);
			success=true;
		} 
		return success;
	}
	
	/**
	 * Create a command class given a key string
	 * @param strCmd name of the command
	 * @return <code>true</code> if successful, <code>false</code> if 
	 * the given command doesn't exist
	 */
	private boolean createCmd(String strCmd)  {
		final boolean exists=cmdMap.containsKey(strCmd);
		if (exists) {
			final Class cmdClass = (Class)cmdMap.get(strCmd);
			currentCommand = null;
			try {
				currentCommand = (Commandable) (cmdClass.newInstance());
				currentCommand.init(msghdlr);
			} catch (Exception e) {
				/* There was a problem resolving the command class or 
				 * with creating an instance. This should never happen
				 * if exists==true. */
				throw new RuntimeException(e);
			}
		}
		return exists;
	}
}
