package jam.sort;

/**
 * Exception that is thrown if <code>RingBuffer</code> is full.
 * 
 * @author Ken Swartz
 * @version 0.9
 * @see RingBuffer
 * @since JDK1.1
 */
class RingFullException extends Exception {

	public RingFullException(String msg) {
		super(msg);
	}
}