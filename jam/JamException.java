/*
 */
 package jam;
/**
 * Exception that can be thrown by classes in Jam
 * will be caught and the messeage will be output
 * with a beep in the form:
 * Error: errorMessage
 *
 * @version 17 July 98
 * @author Ken Swartz
 */ 
public class JamException extends Exception {

    public JamException(String errorMessage) {
	super(errorMessage);
    }
}