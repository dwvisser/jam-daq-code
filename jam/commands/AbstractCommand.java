package jam.commands;

import jam.global.Broadcaster;
import jam.global.CommandListenerException;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Implementation of <code>Commandable</code> interface in which
 * <code>actionPerformed()</code> executes 
 * <code>performCommand(null)</code>, which in turn executes the
 * abstract method, <code>execute(null)</code.
 * 
 * @author Ken Swartz
 */
public abstract class AbstractCommand extends AbstractAction implements 
Commandable {

	protected final JamStatus status=JamStatus.instance();
	protected final Broadcaster broadcaster=Broadcaster.getSingletonInstance();
	protected MessageHandler msghdlr;
	
	/**
	 * Constructor.
	 */
	AbstractCommand(){
		super();
		msghdlr=status.getMessageHandler();
	}
	
	/**
	 * Default implementation that does nothing.
	 */
	public void initCommand(){
	}
	
	/**
	 * Implentation for interface Action. 
	 */
	public final void actionPerformed(ActionEvent ae){
		try{
			performCommand(null);
		} catch(CommandException e){
			msghdlr.errorOutln(e.toString());
		}
	}
	
	/**
	 * Perform a command and log it. This calls 
	 * <code>execute()</code> with the given parameters.
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
	 * Perform a command and log it. This calls 
	 * <code>executeParse()</code> with the given parameters.
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
	 * Log the command, does nothing yet.
	 *
	 */
	public void logCommand() {
		
	}
	
	/**
	 * Log a command error, does nothing yet.
	 *
	 */
	public void logError(){
			
	}
	
	/**
	 * Execute a command with the given command parameters.
	 * 
	 * @param cmdParams command parameters
	 */
	protected abstract void execute(Object [] cmdParams) throws CommandException;
		
	
	/**
	 * Execute a command with the given command string tokens.
	 * 
	 * @param cmdTokens command parameters as string
	 */
	protected abstract void executeParse(String [] cmdTokens) throws CommandListenerException;
}
