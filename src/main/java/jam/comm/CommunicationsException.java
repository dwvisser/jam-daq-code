package jam.comm;

/**
 * Wrapper exception class for when we want to add information to exceptions
 * that occur during communications.
 * 
 * @author Dale Visser
 */
@SuppressWarnings("serial")
public class CommunicationsException extends Exception {

	/**
	 * Default constructor.
	 */
	public CommunicationsException() {
		super();
	}

	/**
	 * @param arg0
	 *            message
	 */
	public CommunicationsException(final String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 *            wrapped exception
	 */
	public CommunicationsException(final Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 *            message
	 * @param arg1
	 *            wrapped exception
	 */
	public CommunicationsException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}
}
