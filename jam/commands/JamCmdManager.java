package jam.commands;

import java.util.*;
import jam.global.*;

/**
 * Class to create commands and execute them
 *
 * @author Ken Swartz
 */
public class JamCmdManager implements CommandListener {

	private final JamStatus status=JamStatus.instance();
	private final Broadcaster broadcaster=Broadcaster.getSingletonInstance();
	private final MessageHandler msghdlr;

	private Map cmdMap = new HashMap();
	private final String COMMAND_PATH="jam.commands.";

	private Commandable currentCommand;

	/**
	 * Constructor
	 *
	 * @param status
	 * @param msghdlr
	 * @param broadcaster
	 */
	public JamCmdManager(MessageHandler msghdlr) {
		this.msghdlr=msghdlr;

		//Commands to add to manager
		//could be read from a file
		cmdMap.put(CommandNames.OPEN_HDF, "OpenHDFCmd");
		cmdMap.put(CommandNames.SAVE_HDF, "SaveHDFCmd");
		cmdMap.put(CommandNames.SAVE_AS_HDF, "SaveAsHDFCmd");
		cmdMap.put("shownewhist", "ShowDialogNewHistogramCmd");
		cmdMap.put("exit", "ShowDialogExitCmd");
		cmdMap.put("newclear", "FileNewClearCmd");
		cmdMap.put("parameters", "ShowDialogParametersCmd");
		cmdMap.put("displayscalers", "ShowDialogScalersCmd");
		cmdMap.put("showzeroscalers", "ShowDialogZeroScalersCmd");
		cmdMap.put("scalers", "ScalersCmd");
		cmdMap.put("exporttext", "ExportTextFileCmd");
	}

	/**
	 * Perform command with object parameters
	 *
	 * @param strCmd	String key indicating the command
	 * @param cmdParams	Command parameters
	 */
	public boolean performCommand(String strCmd, Object [] cmdParams) throws CommandException {


		if(createCmd(strCmd)) {
			currentCommand.performCommand(cmdParams);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Perform command with string parameters
	 *
	 * @param strCmd 		String key indicating the command
	 * @param strCmdParams  Command parameters as strings
	 */
	public boolean performParseCommand(String strCmd,  String [] strCmdParams) throws CommandListenerException {

		try {

			if (createCmd(strCmd)) {
				currentCommand.performParseCommand(strCmdParams);
				return true;
			} else {
				return false;
			}

		} catch (CommandException ce) {
			throw new CommandListenerException(ce.getMessage());
		}

	}
	/**
	 * Create a command class given a key string
	 * @param strCmd
	 * @return
	 */
	private boolean createCmd (String strCmd) throws CommandException {


		String cmdClassName = (String)cmdMap.get(strCmd);

		//No command with given command name
		if (cmdClassName==null)
				return false;

		//create a command
		try {
			currentCommand=null;

			Class cmdClass =Class.forName(COMMAND_PATH+cmdClassName);
			currentCommand = (Commandable)(cmdClass.newInstance() );
			currentCommand.init(status, msghdlr, broadcaster);
			return true;

		} catch (ClassNotFoundException cnfe) {
			//could not find class
			throw new RuntimeException(cnfe);

		} catch (InstantiationException ie) {
			//could not create class
			throw new RuntimeException(ie);

		} catch (IllegalAccessException iae) {
			//could not create class
			throw new RuntimeException(iae);
		}

	}


}
