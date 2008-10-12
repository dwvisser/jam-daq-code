/*
 */
package jam.io;

/**
 * Exception that can be thrown by classes that import and export files.
 * 
 * @author Ken Swartz
 */

public class ImpExpException extends Exception {

	/**
	 * @see Exception#Exception(java.lang.String)
	 */
	ImpExpException(final String errorMessage) {
		super(errorMessage);
	}

	/**
	 * @see Exception#Exception(java.lang.String, Throwable)
	 */
	ImpExpException(final String errorMessage, final Throwable thrown) {
		super(errorMessage, thrown);
	}

	ImpExpException(final Throwable thrown) {
		super(thrown);
	}

}