package jam.commands;

import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.global.Broadcaster;
/**
 * Interface for a command.
 *  
 * @author Ken Swartz
 *
 */
public interface Commandable {

	/**
	 * References needed for commands. Commands have null constructors
	 * so a init method is needed 
	 * 
	 * @param status		Reference to frame and current histograms
	 * @param msghdlr		Message and error output
	 * @param broadcaster	Change in status messages
	 */
	void init(JamStatus status, MessageHandler msghdlr, Broadcaster broadcaster);
	/**
	 * Execute a command with the given command parameters
	 * 
	 * @param cmdParams command parameters
	 */
	void performCommand(Object [] cmdParams);	
	
	/**
	 * Execute a command with the given command string tokens
	 * 
	 * @param cmdParamTokens command string parameters
	 */
	void performCommandStrParam(String [] cmdParamTokens);
	
	/**
	 * Undo the command
	 *
	 */
	//void undo();
}
