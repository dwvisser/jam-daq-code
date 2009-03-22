package jam.sort;

import com.google.inject.Singleton;

/**
 * Generates RingBuffer instances appropriate to whether J2SE 6
 * java.util.concurrent is on the classpath or not.
 * 
 * @author Dale Visser
 * 
 */
@Singleton
public final class RingBufferFactory {

	/**
	 * Allocates a fresh buffer array of the correct size, for use by clients of
	 * this class.
	 * 
	 * @return a fresh byte array equal in size to one of the buffers
	 */
	public byte[] freshBuffer() {
		return new byte[RingBuffer.BUFFER_SIZE];
	}

	/**
	 * @return a new ring buffer with a backing store
	 */
	public RingBuffer create() {
		return create(false);
	}

	/**
	 * Creates a new ring buffer with or without a backing store.
	 * 
	 * @param empty
	 *            whether this is a no-capacity ring buffer
	 * @return a RingBuffer implemantation instance
	 */
	public RingBuffer create(boolean empty) {
		return new LinkedBlockingDequeRingBuffer(empty);
	}

}
