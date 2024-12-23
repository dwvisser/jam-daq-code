package jam.sort.stream;

/**
 * Exception that is thrown if we have a unrecoverable error in handling an
 * event stream.
 * 
 * @author Ken Swartz
 * @version 0.5
 * @since JDK 1.1
 */

public final class EventException extends Exception {

	EventException(final String msg) {
		super(msg);
	}

	EventException(final String msg, final Throwable thrown) {
		super(msg, thrown);
	}

	EventException(final Throwable thrown) {
		super(thrown);
	}
}