/*
 */
package jam.fit;

/**
 * Exception that is throw if there is a fit exception that can be handled
 * inside fit
 *  
 */
public class FitException extends Exception {

    /**
     * Constructs a fit exception with the given message.
     * 
     * @param msg error message
     * @param thrown exception which caused this condition
     */
    public FitException(String msg, Throwable thrown) {
        super(msg,thrown);
    }

}