package test.sort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;
import jam.sort.RingBuffer;
import jam.sort.RingFullException;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.internal.ArrayComparisonFailure;

/**
 * JUnit tests for <code>jam.sort.RingBuffer</data>.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @see RingBuffer
 */
public final class RingBufferTest {// NOPMD

	private static final String ARRAYS_SHOULD_HAVE_BEEN_EQUAL = "Arrays should have been equal.";

	private transient RingBuffer ring, emptyRing;

	/**
	 * @param output
	 *            result of getBuffer
	 * @param input
	 *            passed to putBuffer
	 * @throws ArrayComparisonFailure
	 */
	private void assertArraysEqualButNotSame(final byte[] output,
			final byte[] input) throws ArrayComparisonFailure {
		assertArrayEquals(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, input, output);
		assertNotSame("Expect two different arrays.", input, output);
	}

	private void assertRingBufferEmpty(final RingBuffer ringbuffer) {
		assertTrue("Expected empty ring buffer.", ringbuffer.isEmpty());
		assertFalse("Expected ring buffer to not be full.", ringbuffer.isFull());
		assertEquals("Expected all buffers in ring to be available.",
				RingBuffer.NUMBER_BUFFERS, ringbuffer.getAvailableBuffers());
	}

	private void assertRingBufferFull(final RingBuffer ringbuffer) {
		assertFalse("Expected ring buffer to not be empty.", ringbuffer
				.isEmpty());
		assertTrue("Expected full ring buffer.", ringbuffer.isFull());
		assertEquals("Expected zero available buffers.", 0, ringbuffer
				.getAvailableBuffers());
	}

	/**
	 * @param usedBuffers
	 *            number of buffers used up
	 */
	private void assertUsedBuffers(final RingBuffer ringbuffer,
			final int usedBuffers) {
		assertEquals("Expected " + usedBuffers + " buffers used.", usedBuffers,
				ringbuffer.getUsedBuffers());
		final int available = RingBuffer.NUMBER_BUFFERS - usedBuffers;
		assertEquals("Expected " + available + " buffers available.",
				available, ringbuffer.getAvailableBuffers());
	}

	private void clear(final RingBuffer ringbuffer) {
		ringbuffer.clear();
		this.assertRingBufferEmpty(ringbuffer);
	}

	/**
	 * @throws InterruptedException
	 * @returns last buffer inserted
	 */
	private byte[] fillEmptyRingBuffer(final RingBuffer ringbuffer,
			final int numBuffers) throws InterruptedException {
		assertRingBufferEmpty(ringbuffer);
		final byte[] buffer = RingBuffer.freshBuffer();
		for (int i = 0; i < numBuffers; i++) {
			Arrays.fill(buffer, (byte) (i + 1));
			try {
				ringbuffer.putBuffer(buffer);
			} catch (RingFullException re) {
				fail(re.getMessage());
			}
		}
		return buffer;
	}

	@Before
	public void setUp() {
		ring = new RingBuffer();
		emptyRing = new RingBuffer(true);
	}

	@Test
	public void testCloseToFull() throws InterruptedException {
		this.clear(ring);
		this.fillEmptyRingBuffer(ring, RingBuffer.NUMBER_BUFFERS
				- RingBuffer.CLOSE_TO_CAPACITY + 1);
		assertTrue("Expected buffer to be close to full.", ring.isCloseToFull());
	}

	@Test
	public void testGetAvailableBuffers() throws InterruptedException {
		assertEquals("Expected all buffers to be available.",
				RingBuffer.NUMBER_BUFFERS, ring.getAvailableBuffers());
		final byte[] buffer = RingBuffer.freshBuffer();
		for (int i = 0; i < RingBuffer.NUMBER_BUFFERS; i++) {
			try {
				ring.putBuffer(buffer);
				assertUsedBuffers(ring, i + 1);
			} catch (RingFullException re) {
				fail(re.getMessage());
			}
		}
		assertRingBufferFull(ring);
		assertEquals("Expect no available buffers in empty ring.", 0, emptyRing
				.getAvailableBuffers());
		this.clear(ring);
	}

	/**
	 * Test the properties of 'null' rings.
	 * 
	 */
	@Test
	public void testIsNull() {
		assertTrue("emptyRing explicitly 'null'", emptyRing.isNull());
		assertFalse("Allocated ring not null.", ring.isNull());
		assertTrue("'null' rings are full.", emptyRing.isFull());
		assertTrue("'null' rings are nearly full.", emptyRing.isCloseToFull());
		assertTrue("'null' rings are empty.", emptyRing.isEmpty());
		assertEquals("'null' rings never have used buffers.", 0, emptyRing
				.getUsedBuffers());
	}

	/**
	 * Test for <code>putBuffer()</code>.
	 * 
	 * @see RingBuffer#putBuffer(byte [])
	 */
	@Test
	public void testPut() throws InterruptedException {
		final byte[] out = RingBuffer.freshBuffer();
		byte[] buffer = RingBuffer.freshBuffer();
		this.clear(ring);
		for (int i = 0; i < RingBuffer.NUMBER_BUFFERS / 2; i++) {
			Arrays.fill(buffer, (byte) i);
			try {
				ring.putBuffer(buffer);
				this.assertUsedBuffers(ring, 1);
			} catch (RingFullException re) {
				fail(re.getMessage());
			}
			ring.getBuffer(out);
			this.assertRingBufferEmpty(ring);
			assertArraysEqualButNotSame(out, buffer);
		}
		buffer = fillEmptyRingBuffer(ring, RingBuffer.NUMBER_BUFFERS);
		assertRingBufferFull(ring);
		ring.getBuffer(out);
		/* Ring buffer is FIFO. */
		assertFalse("Expected arrays to not be equal.", Arrays.equals(buffer,
				out));
		assertFalse("Expected ring buffer to not be full.", ring.isFull());
		try {
			ring.putBuffer(buffer);
			ring.putBuffer(buffer);
			fail("Expected to hit RingFullException");
		} catch (RingFullException rfe) {// NOPMD
			// do nothing
		}
	}
}
