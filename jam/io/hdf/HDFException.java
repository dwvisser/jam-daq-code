/*
 */
 package jam.io.hdf;
/**
 * Exception that can be thrown by classes in io.hdf package
 *
 * @author Dale Visser
 * @author Ken Swartz
 */ 
public class HDFException extends Exception {

    public HDFException(String errorMessage) {
	super(errorMessage);
    }


}