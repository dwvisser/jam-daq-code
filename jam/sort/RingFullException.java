package jam.sort;

/**
 * Exception that is thrown if <code>RingBuffer</code> is full.
 * 
 * @author Ken Swartz
 * @version 0.9
 * @see RingBuffer
 * @since JDK1.1
 */
public class RingFullException extends Exception {

    /**
     * @see Exception#Exception(java.lang.String)
     */
	public RingFullException(String msg) {
		super(msg);
	}
}