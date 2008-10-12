package jam.global;

/**
 * Exception thrown when a error occurs in running a command via the
 * ComandListener interface
 * 
 * @author Ken Swartz
 */
public class CommandListenerException extends Exception {

	/**
	 * Chaining constructor with additional message.
	 * 
	 * @param errorMessage
	 *            error message
	 * @param thrown
	 *            original cause of exception
	 */
	public CommandListenerException(final String errorMessage,
			final Throwable thrown) {
		super(errorMessage, thrown);
	}

	/**
	 * Chaining constructor.
	 * 
	 * @param thrown
	 *            original cause of exception
	 */
	public CommandListenerException(final Throwable thrown) {
		super(thrown);
	}

}