package jam.sort;
/**
 * Exception that is thrown if we have trouble writing
 * out data with a subclass of <code>StorageDeamon</code>.
 *
 * @author Ken Swartz
 * @version 0.9
 * @since JDK 1.1
 */
class StorageException extends Exception{
    
    /**
     * Creates an exception withe the given message.
     *
     * @param msg a descriptive message of the error condition
     */
    public StorageException(String msg){
	super(msg);
    }
    
}