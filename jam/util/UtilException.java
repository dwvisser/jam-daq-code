/*
 */
 package jam.util;
/**
 * Exceptions thrown in the <code>jam.util</code> package are converted to this.
 *
 * @author Dale Visser
 */ 
public class UtilException extends Exception {

    public UtilException(String errorMessage) {
	super(errorMessage);
    }
}