 package jam.global;
/**
 * Exception throwen when a error occurs in 
 * running a command via the ComandListener interface
 *
 * @author Ken Swartz
 */ 
public class CommandListenerException extends Exception {

	public CommandListenerException(String errorMessage) {
		super(errorMessage);
	}
	public CommandListenerException(Exception e) {
		super(e);
	}
    
}