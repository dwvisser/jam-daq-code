/*
 */
package jam.fit;

/**
 * Exception that is throw if there is a fit exception that can be handled
 * inside fit
 * 
 */
@SuppressWarnings("serial")
public class FitException extends Exception {

	/**
	 * Constructs a fit exception with the given message.
	 * 
	 * @param msg
	 *            error message
	 * @param thrown
	 *            exception which caused this condition
	 */
	public FitException(final String msg, final Throwable thrown) {
		super(msg, thrown);
	}

	/**
	 * @param msg
	 *            message
	 */
	public FitException(final String msg) {
		super(msg);
	}

}