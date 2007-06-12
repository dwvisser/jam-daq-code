package jam.comm;

/**
 * Wrapper exception class for when we want to add information
 * to exceptions that occur during communications.
 * 
 * @author Dale Visser
 */
public class CommunicationsException extends Exception {

	public CommunicationsException() {
		super();
	}

	public CommunicationsException(String arg0) {
		super(arg0);
	}

	public CommunicationsException(Throwable arg0) {
		super(arg0);
	}

	public CommunicationsException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}
