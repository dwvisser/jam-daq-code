package jam.commands;

import jam.global.CommandListenerException;
import jam.global.JamProperties;
import jam.global.MessageHandler;

import java.awt.Event;

import javax.swing.Action;

/**
 * Interface for a command.
 *  
 * @author Ken Swartz
 */
public interface Commandable extends Action {
	
	/**
	 * References needed for commands. Commands have null constructors
	 * so a init method is needed 
	 * 
	 * @param msghdlr		Message and error output
	 */
	void init(MessageHandler msghdlr);

	/**
	 * Execute a command with the given command parameters
	 * 
	 * @param cmdParams command parameters
	 */
	void performCommand(Object [] cmdParams) throws CommandException;	

	/**
	 * Execute a command with the given command string tokens
	 * 
	 * @param cmdParamTokens command string parameters
	 */
	void performParseCommand(String [] cmdParamTokens) throws CommandListenerException;
	
	int CTRL_MASK =
		JamProperties.isMacOSX() ? Event.META_MASK : Event.CTRL_MASK;
		
}
