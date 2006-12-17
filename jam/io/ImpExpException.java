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
    ImpExpException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * @see Exception#Exception(java.lang.String, Throwable)
     */
    ImpExpException(String errorMessage, Throwable thrown) {
        super(errorMessage,thrown);
    }
    
    ImpExpException(Throwable thrown){
    	super(thrown);
    }

}