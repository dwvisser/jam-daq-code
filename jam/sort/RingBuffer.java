package jam.sort;

import javax.swing.JOptionPane;

/**
 * <code>RingBuffer</code> is a list of buffers which starts repeating after
 * the last buffer is filled. It allows asynchronous inter-Thread communication.
 * If a buffer is placed in a full ring, an exception is thrown.
 * 
 * @author Ken Swartz
 * @version 0.5,0.9
 * @since JDK 1.1
 */
public final class RingBuffer {

	/**
	 * Size in bytes of a single buffer.
	 */
	public static final int BUFFER_SIZE = 8 * 1024;

	/**
	 * Number of buffers in ring, a power of 2.
	 */
	protected static final int NUMBER_BUFFERS = 1 << 6; //64 buffers in ring

	private static final int CLOSE_TO_CAPACITY = NUMBER_BUFFERS >> 5;

	/**
	 * Mask that makes counter less than Number buffers
	 */
	protected static final int MASK = NUMBER_BUFFERS - 1;

	private final byte[][] ringBuffer = new byte[NUMBER_BUFFERS][BUFFER_SIZE];;

	/**
	 * where we will put the next buffer
	 */
	private int posPut = 0;

	/**
	 * where we will get the next buffer from
	 */
	private int posGet = 0;

	/**
	 * Creates a new ring buffer.
	 */
	public RingBuffer() {
		clear();
	}

	/**
	 * Copies the passed array into the ring buffer.
	 * 
	 * @exception RingFullException
	 *                thrown when the ring is too full to be written to
	 */
	public synchronized void putBuffer(byte[] inBuffer)
			throws RingFullException {
		if (isFull()) {
			final StringBuffer message = new StringBuffer();
			message.append("Lost a buffer in thread \"");
			message.append(Thread.currentThread().getName());
			message.append("\" when putBuffer() called while already full.");
			throw new RingFullException(message.toString());
		}
		System.arraycopy(inBuffer, 0, ringBuffer[posPut & MASK], 0,
				inBuffer.length);
		final boolean emptyBeforePut = isEmpty();
		posPut++;
		/*
		 * The only reason another thread could be in wait() on this object is
		 * that we were empty. Checking eliminates a lot of needless notify()
		 * calls.
		 */
		if (emptyBeforePut) {
			notifyAll();
		}
	}

	public synchronized void clear() {
		posPut = 0;
		posGet = 0;
	}

	/**
	 * Gives a pointer to the next buffer in the ring.
	 * 
	 * @return the next buffer in the ring
	 */
	public synchronized void getBuffer(final byte[] out) {
		while (isEmpty()) {
			try {
				/*
				 * notified when a putBuffer() occurs on the empty ring
				 */
				wait();
			} catch (InterruptedException ie) {
				JOptionPane.showMessageDialog(null, ie.getMessage(), getClass()
						.getName(), JOptionPane.ERROR_MESSAGE);
			}
		}
		/* & MASK serves to keep index accessed running 0..63,0..63, etc. */
		System.arraycopy(ringBuffer[(posGet++) & MASK], 0, out, 0, out.length);
	}

	/**
	 * Tells you if the ring buffer is empty. Used to check if you have read all
	 * the buffers in the ring.
	 * 
	 * @return true if there are no buffers in the ring.
	 */
	public synchronized boolean isEmpty() {
		return posPut == posGet;
	}

	/**
	 * Tells if the ring buffer is full.
	 * 
	 * @return <code>true</code> if there are no more available buffers
	 */
	public synchronized boolean isFull() {
		return posPut - posGet + 1 > NUMBER_BUFFERS;
	}

	public synchronized int getAvailableBuffers() {
		return NUMBER_BUFFERS - getUsedBuffers();
	}

	public synchronized boolean isCloseToFull() {
		return getAvailableBuffers() < CLOSE_TO_CAPACITY;
	}

	public synchronized int getUsedBuffers() {
		return posPut - posGet;
	}

	public static byte[] freshBuffer() {
		return new byte[BUFFER_SIZE];
	}
}