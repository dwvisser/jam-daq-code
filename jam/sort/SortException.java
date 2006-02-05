package jam.sort;

/**
 * Exception that is thrown if we have a error in sorting.
 * 
 * @author Ken Swartz
 * @version 0.9
 * @since JDK 1.1
 */
public class SortException extends Exception {

    /**
     * @see Exception#Exception(java.lang.String)
     */
    SortException(String msg) {
        super(msg);
    }
    
    SortException(String msg, Throwable thrown){
        super(msg, thrown);
    }

    SortException(Throwable thrown){
        super(thrown);
    }
}