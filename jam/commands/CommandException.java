/*
 */
 package jam.commands;
/**
 * Exception throwen when a error occurs in 
 * the jam.commands package. 
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