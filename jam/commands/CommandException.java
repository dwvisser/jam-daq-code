package jam.commands;
/**
 * Exception thrown when an error occurs in 
 * the <code>jam.commands</code> package. 
 *
 * @author Ken Swartz
 */ 
public class CommandException extends Exception {
    
    /**
     * @see Exception#Exception(java.lang.Throwable)
     */
	public CommandException(Throwable thrown) {
		super(thrown);
	}
}