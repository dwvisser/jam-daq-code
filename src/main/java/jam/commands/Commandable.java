package jam.commands;

import jam.global.CommandListenerException;
import jam.global.JamProperties;

import javax.swing.*;
import java.awt.*;

/**
 * Objects to be executed by <code>CommandManager</code> must
 * implement this interface.
 *  
 * @author Ken Swartz
 */
public interface Commandable extends Action {
	
	/**
	 * Performs any initial setup that wasn't possible in the
	 * constructor.
	 */
	void initCommand();
	
	/**
	 * Execute a command with the given command parameters.
	 * 
	 * @param cmdParams command parameters
	 * @throws CommandException if an error occurs
	 */
	void performCommand(Object [] cmdParams) throws CommandException;	

	/**
	 * Execute a command with the given command string tokens
	 * 
	 * @param cmdParamTokens command string parameters
	 * @throws CommandListenerException if an error occurs
	 */
	void performParseCommand(String [] cmdParamTokens) throws CommandListenerException;
	
	/**
	 * The platform-specific mask for invoking commands from the keyboard.
	 */
	int CTRL_MASK =
		JamProperties.isMacOSX() ? Event.META_MASK : Event.CTRL_MASK;
		
}
