 package jam.global;
/**
 * Exception throwen when a error occurs in 
 * running a command via the ComandListener interface
 *
 * @author Ken Swartz
 */ 
public class CommandListenerException extends Exception {

    /**
     * Chaining constructor with additional message.
     * @param errorMessage error message
     * @param thrown original cause of exception
     */
	public CommandListenerException(String errorMessage, Throwable thrown) {
		super(errorMessage,thrown);
	}
	
	/**
	 * Chaining constructor.
	 * 
	 * @param thrown original cause of exception
	 */
	public CommandListenerException(Throwable thrown) {
		super(thrown);
	}
    
}