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

    HDFException(String errorMessage) {
        super(errorMessage);
    }

    HDFException(String msg, Throwable thrown){
        super(msg,thrown);
    }

}