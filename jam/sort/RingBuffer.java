package jam.sort;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

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
	 * Total memory this ring buffer can take.
	 */
	private static final int MEMORY_FOOTPRINT = 0x400 * 0x400 * 2; // 2 MB

	/**
	 * Size in bytes of a single buffer.
	 */
	public static final int BUFFER_SIZE = 0x2000; // 8k

	/**
	 * Number of buffers in ring, must be a power of 2.
	 */
	public static final int NUMBER_BUFFERS = MEMORY_FOOTPRINT / BUFFER_SIZE;

	/**
	 * We are close to capacity if available buffers is less than this.
	 */
	public static final int CLOSE_TO_CAPACITY = Math
			.max(2, NUMBER_BUFFERS / 16);

	private transient ArrayBlockingQueue<byte[]> ring;

	private transient final LinkedBlockingDeque<byte[]> poolStack = new LinkedBlockingDeque<byte[]>(
			NUMBER_BUFFERS);

	private transient final boolean hasRing;

	/**
	 * Creates a new ring buffer.
	 */
	public RingBuffer() {
		this(false);
	}

	public RingBuffer(boolean empty) {
		super();
		hasRing = !empty; // NOPMD
		if (hasRing) {
			ring = new ArrayBlockingQueue<byte[]>(NUMBER_BUFFERS);
		}
	}

	/**
	 * 
	 * @return whether this buffer was created with no capacity
	 */
	public boolean isNull() {
		return !hasRing;
	}

	/**
	 * Copies the passed array into the ring buffer.
	 * 
	 * @param inBuffer
	 *            incoming data
	 * @return true if successful, false if full
	 */
	public boolean tryPutBuffer(final byte[] inBuffer) {
		assert !isNull() : "Attempted putBuffer() on 'null' ring buffer.";
		validateBuffer(inBuffer);
		final byte[] pushBuffer = allocateFromPoolIfPossibleAndCopy(inBuffer);
		return ring.offer(pushBuffer);
	}

	/**
	 * @param inBuffer
	 *            buffer to copy
	 * @return copy of inBuffer, either from internal pool or fresh
	 */
	private byte[] allocateFromPoolIfPossibleAndCopy(final byte[] inBuffer) {
		byte[] rval = poolStack.pollFirst();
		if (rval == null) {
			rval = inBuffer.clone();
		} else {
			System.arraycopy(inBuffer, 0, rval, 0, BUFFER_SIZE);
		}
		return rval;
	}

	private void validateBuffer(final byte[] inbuffer) {
		if (inbuffer == null) {
			throw new IllegalArgumentException("null buffer reference");
		}
		if (inbuffer.length != BUFFER_SIZE) {
			throw new IllegalArgumentException(
					"buffer capacity expected to be " + BUFFER_SIZE);
		}
	}

	/**
	 * Clear the buffer. This is a quick operation, since it just involves
	 * resetting the put and get pointers.
	 * 
	 */
	public void clear() {
		poolStack.addAll(ring);
		ring.clear();
	}

	/**
	 * Passes back a copy of the current buffer in the given <code>byte</code>
	 * array. Blocks until the buffer becomes available.
	 * 
	 * @param out
	 *            array to copy the next buffer into
	 */
	public void getBuffer(final byte[] out) throws InterruptedException {
		assert !isNull() : "Attempted getBuffer() on 'null' ring buffer.";
		this.validateBuffer(out);
		final byte[] bufferFromRing = ring.take();
		System.arraycopy(bufferFromRing, 0, out, 0, RingBuffer.BUFFER_SIZE);
		poolStack.addFirst(bufferFromRing);
	}

	/**
	 * Tells you if the ring buffer is empty. Used to check if you have read all
	 * the buffers in the ring.
	 * 
	 * @return true if there are no buffers in the ring.
	 */
	public boolean isEmpty() {
		return isNull() || ring.isEmpty();
	}

	/**
	 * Tells if the ring buffer is full.
	 * 
	 * @return <code>true</code> if there are no more available buffers
	 */
	public boolean isFull() {
		return isNull() || ring.remainingCapacity() == 0;
	}

	/**
	 * Get the number of buffers available to have data put in them.
	 * 
	 * @return the number of available buffers
	 */
	public int getAvailableBuffers() {
		return isNull() ? 0 : ring.remainingCapacity();
	}

	/**
	 * Gets whether the ring buffer is close to filling, defined as
	 * approximately 97% full.
	 * 
	 * @return <code>true</code> if the ring buffer is close to filling
	 */
	public boolean isCloseToFull() {
		return isNull() || ring.remainingCapacity() < CLOSE_TO_CAPACITY;
	}

	/**
	 * Gets the number of buffers filled with data.
	 * 
	 * @return the number of used buffers
	 */
	public int getUsedBuffers() {
		return isNull() ? 0 : ring.size();
	}

	/**
	 * Allocates a fresh buffer array of the correct size, for use by clients of
	 * this class.
	 * 
	 * @return a fresh byte array equal in size to one of the buffers
	 */
	public static byte[] freshBuffer() {
		return new byte[BUFFER_SIZE];
	}
}