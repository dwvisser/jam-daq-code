/*
 */
 package jam.commands;
/**
 * Exception thrown when an error occurs in 
 * the <code>jam.commands</code> package. 
 *
 * @author Ken Swartz
 */ 
public class CommandException extends Exception {

    public CommandException(String errorMessage) {
		super(errorMessage);
    }
    
	public CommandException(Exception e) {
		super(e);
	}
}