/*
 */
 package jam.io;
/**
 * Exception that can be thrown by classes that
 * import and export files.
 *
 * @author Ken Swartz
 */ 
public class ImpExpException extends Exception {

    public ImpExpException(String errorMessage) {
	super(errorMessage);
    }

}