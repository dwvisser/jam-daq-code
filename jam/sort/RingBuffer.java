package jam.sort;

/**
 * @author Dale Visser
 * 
 */
public interface RingBuffer {
	/**
	 * Size in bytes of a single buffer.
	 */
	int BUFFER_SIZE = 0x2000; // 8k

	/**
	 * Total memory this ring buffer can take.
	 */
	int MEMORY_FOOTPRINT = 0x400 * 0x400 * 2; // 2 MB

	/**
	 * Number of buffers in ring, must be a power of 2.
	 */
	int NUMBER_BUFFERS = MEMORY_FOOTPRINT / BUFFER_SIZE;

	/**
	 * We are close to capacity if available buffers is less than this.
	 */
	int CLOSE_TO_CAPACITY = Math.max(2, NUMBER_BUFFERS / 16);

	/**
	 * Passes back a copy of the current buffer in the given <code>byte</code>
	 * array. Blocks until the buffer becomes available.
	 * 
	 * @param buffer
	 *            array to copy the next buffer into
	 * @throws InterruptedException
	 *             if the thread is interrupted
	 */
	void getBuffer(byte[] buffer) throws InterruptedException;

	/**
	 * Tells you if the ring buffer is empty. Used to check if you have read all
	 * the buffers in the ring.
	 * 
	 * @return true if there are no buffers in the ring.
	 */
	boolean isEmpty();

	/**
	 * Copies the passed array into the ring buffer.
	 * 
	 * @param inBuffer
	 *            incoming data
	 * @return true if successful, false if full
	 */
	boolean tryPutBuffer(final byte[] inBuffer);

	/**
	 * 
	 * @return whether this buffer was created with no capacity
	 */
	boolean isNull();

	/**
	 * Gets whether the ring buffer is close to filling, defined as
	 * approximately 97% full.
	 * 
	 * @return <code>true</code> if the ring buffer is close to filling
	 */
	boolean isCloseToFull();

	/**
	 * Clear all buffers from the ring. For test purposes only.
	 */
	void clear();

	/**
	 * Get the number of buffers available to have data put in them. For test
	 * purposes only.
	 * 
	 * @return the number of available buffers
	 */
	int getAvailableBuffers();

	/**
	 * Gets the number of buffers filled with data. For test purposes only.
	 * 
	 * @return the number of used buffers
	 */
	int getUsedBuffers();

	/**
	 * Tells if the ring buffer is full. For test purposes only.
	 * 
	 * @return <code>true</code> if there are no more available buffers
	 */
	boolean isFull();
}
