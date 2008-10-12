package jam.global;

/**
 * Exception that can be thrown by classes in Jam will be caught and the message
 * will be output with a beep in the form: Error: errorMessage
 * 
 * @version 17 July 98
 * @author Ken Swartz
 */
public class JamException extends Exception {

	/**
	 * Constructs a new JamException.
	 * 
	 * @param errorMessage
	 *            the error message
	 */
	public JamException(final String errorMessage) {
		super(errorMessage);
	}

	/**
	 * Constructs a new JamException.
	 * 
	 * @param message
	 *            the error message
	 * @param cause
	 *            thrown cause that is the reason for throwing this exception
	 */
	public JamException(final String message, final Throwable cause) {
		super(message, cause);
	}
}