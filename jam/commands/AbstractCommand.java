package jam.commands;

import jam.global.*;

/**
 * Base class for commands
 * 
 * @author Ken
 */
public abstract class AbstractCommand implements Commandable {

	protected JamStatus status;
	protected Broadcaster broadcaster;
	protected MessageHandler msghdlr;
	
	/**
	 * Constructor
	 *
	 */
	public AbstractCommand(){
		
	}
	/**
	 * Initializer
	 * 
	 * @param status
	 * @param msghdlr
	 * @param broadcaster
	 */
	public void init(JamStatus status, MessageHandler msghdlr, Broadcaster broadcaster) {
		this.status=status;
		this.msghdlr=msghdlr;
		this.broadcaster=broadcaster;		
	}
	/**
	 * Perform a command
	 *
	 * @param cmdParams the command parameters
	 */
	public void performCommand(Object [] cmdParams) {
		
		try {
			
			execute(cmdParams);
			logCommand();
			
		} catch (Exception e) {
			logError();
		}
	}

	/**
	 * Perform a command
	 *
	 * @param strCmdParams the command parameters as strings
	 */
	public void performCommandStrParam(String [] strCmdParams) {
				
		try {
			
			executeStrParam(strCmdParams);
			logCommand();
			
		} catch (Exception e) {
			logError();
		}
	}

	/**
	 * Log the command
	 *
	 */
	public void logCommand() {
		
	}
	
	/**
	 * Log a command error
	 *
	 */
	public void logError(){
			
	}
	/**
	 * Execute a command with the given command parameters
	 * 
	 * @param cmdParams command parameters
	 */
	public abstract	void execute(Object [] cmdParams);
		
	
	/**
	 * Execute a command with the given command string tokens
	 * 
	 * @param cmdTokens command parameters as string
	 */
	public abstract	void executeStrParam(String [] cmdTokens);			

}