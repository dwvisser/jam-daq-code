package jam.commands;

import java.util.*;
import jam.global.*;

/**
 * Class to create commands and execute them
 * 
 * @author Ken Swartz
 */
public class JamCmdManager implements CommandListener {
	
	private JamStatus status;
	private Broadcaster broadcaster;
	private MessageHandler msghdlr;

	private Map cmdMap = new HashMap();
	private final String COMMAND_PATH="jam.commands.";
	
	/**
	 * Constructor 
	 * 
	 * @param status
	 * @param msghdlr
	 * @param broadcaster
	 */
	public JamCmdManager(JamStatus status, MessageHandler msghdlr, Broadcaster broadcaster) {
		this.status=status;
		this.msghdlr=msghdlr;
		this.broadcaster=broadcaster;
			
		//Commands to add to manager 
		//could be read from a file
		cmdMap.put("shownewhist", "ShowDialogNewHistogramCmd");
		cmdMap.put("exit", "ShowDialogExitCmd");
	}
	
	/**
	 * Perform command with object parameters
	 * 
	 * @param strCmd	String key indicating the command
	 * @param cmdParams	Command parameters 
	 */			
	public boolean performCommand(String strCmd, Object [] cmdParams) {
		
		Commandable command =createCmd(strCmd);
		
		if(command!=null) {
			command.performCommand(cmdParams);
			return true;		
		} else {
			return true;
		}								
	}
	
	/**
	 * Perform command with string parameters
	 * 
	 * @param strCmd 		String key indicating the command
	 * @param strCmdParams  Command parameters as strings
	 */
	public boolean performCommand(String strCmd,  String [] strCmdParams) {
		
		Commandable command =createCmd(strCmd);		
		if(command!=null) {
			command.performCommandStrParam(strCmdParams);
			return true;		
		} else {
			return false;
		}
		
	}
	/**
	 * Create a command class given a key string
	 * @param strCmd
	 * @return
	 */
	private Commandable createCmd (String strCmd){
		
		Commandable command;
		
		command=null; 
		String cmdClassName = (String)cmdMap.get(strCmd);
				
		//create a command
		try {
			
			Class cmdClass =Class.forName(COMMAND_PATH+cmdClassName);
			command = (Commandable)(cmdClass.newInstance() );
			command.init(status, msghdlr, broadcaster);
			
		} catch (ClassNotFoundException cnfe) {
			command=null; 
			//could not find class 
		} catch (InstantiationException ie) {
			command=null; 
			//could not create class
		} catch (IllegalAccessException iae) {
			command=null; 			
			//could not create class
			//msghdlr.errorOutln("Could not create command "+strCmd);
			
		}
		
		return command;
	}
	
	
}
