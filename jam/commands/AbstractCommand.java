package jam.commands;

import jam.global.Broadcaster;
import jam.global.CommandListenerException;
import jam.global.JamStatus;
import jam.global.MessageHandler;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Base class for commands
 * 
 * @author Ken
 */
public abstract class AbstractCommand extends AbstractAction implements Commandable {

	protected final JamStatus status=JamStatus.instance();
	protected final Broadcaster broadcaster=Broadcaster.getSingletonInstance();
	protected MessageHandler msghdlr;
	
	/**
	 * Constructor
	 *
	 */
	AbstractCommand(){
		super();	
	}
	
	/**
	 * Initializer
	 * 
	 * @param status
	 * @param msghdlr
	 * @param broadcaster
	 */
	public void init(MessageHandler mh) {
		msghdlr=mh;
	}
	
	public void actionPerformed(ActionEvent ae){
		try{
			performCommand(null);
		} catch(CommandException e){
			msghdlr.errorOutln(e.toString());
		}
	}
	
	/**
	 * Perform a command
	 *
	 * @param cmdParams the command parameters
	 */
	public void performCommand(Object [] cmdParams)  throws CommandException {
		try {
			execute(cmdParams);
			logCommand();
		} catch (Exception e) {			
			logError();
			throw new CommandException(e);
		}
	}

	/**
	 * Perform a command
	 *
	 * @param strCmdParams the command parameters as strings
	 */
	public void performParseCommand(String [] strCmdParams) throws CommandListenerException{			
		try {	
			executeParse(strCmdParams);
			logCommand();
		} catch (Exception e) {
			logError();
			throw new CommandListenerException(e);
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
	protected abstract	void execute(Object [] cmdParams) throws CommandException;
		
	
	/**
	 * Execute a command with the given command string tokens
	 * 
	 * @param cmdTokens command parameters as string
	 */
	protected abstract	void executeParse(String [] cmdTokens) throws CommandListenerException;			
}
