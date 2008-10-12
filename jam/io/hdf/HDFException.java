/*
 */
package jam.io.hdf;

/**
 * Exception that can be thrown by classes in <code>jam.io.hdf</code> package
 * 
 * @author Dale Visser
 * @author Ken Swartz
 */
public class HDFException extends Exception {

	HDFException(final String errorMessage) {
		super(errorMessage);
	}

	HDFException(final String msg, final Throwable thrown) {
		super(msg, thrown);
	}

}