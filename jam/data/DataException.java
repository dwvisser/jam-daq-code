 package jam.data;
 
/**
 * Exception that can be thrown when unrecoverable error conditions occur in <code>jam.data</code>
 * classes.
 *
 * @author Ken Swartz
 */ 
public class DataException extends Exception {
    
    /**
     * Constructor called with a message.
     * 
     * @param errorMessage  description of the error condition
     */
    public DataException(String errorMessage) {
		super(errorMessage);
    }
}