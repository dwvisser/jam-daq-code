package test.sort;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import jam.sort.RingBuffer;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.junit.internal.ArrayComparisonFailure;

/**
 * JUnit tests for <code>jam.sort.RingBuffer</data>.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @see RingBuffer
 */
public final class RingBufferTest {// NOPMD

	private static final String ARRAYS_NOT_EQUAL = "Arrays should have been equal.";

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
		assertArrayEquals(ARRAYS_NOT_EQUAL, input, output);
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
	 * @returns last buffer inserted
	 */
	private byte[] fillEmptyRingBuffer(final RingBuffer ringbuffer,
			final int numBuffers) {
		assertRingBufferEmpty(ringbuffer);
		final byte[] buffer = RingBuffer.freshBuffer();
		for (int i = 0; i < numBuffers; i++) {
			Arrays.fill(buffer, (byte) (i + 1));
			putBuffer(ringbuffer, buffer, true);
		}
		return buffer;
	}

	/**
	 * @param ringbuffer
	 * @param buffer
	 */
	private void putBuffer(final RingBuffer ringbuffer, final byte[] buffer,
			final boolean expectedSuccess) {
		final String message = expectedSuccess ? "Expected success putting buffer into ring."
				: "Expected failure putting buffer into full ring.";
		if (expectedSuccess) {
			assertTrue(message, ringbuffer.tryPutBuffer(buffer));
		} else {
			assertFalse(message, ringbuffer.tryPutBuffer(buffer));
		}
	}

	/**
	 * Set up the test.
	 */
	@Before
	public void setUp() {
		ring = new RingBuffer();
		emptyRing = new RingBuffer(true);
	}

	/**
	 * Test bringing the buffer close to full.
	 */
	@Test
	public void testCloseToFull() {
		this.clear(ring);
		this.fillEmptyRingBuffer(ring, RingBuffer.NUMBER_BUFFERS
				- RingBuffer.CLOSE_TO_CAPACITY + 1);
		assertTrue("Expected buffer to be close to full.", ring.isCloseToFull());
	}

	/**
	 * Test getting the number of available buffers.
	 */
	@Test
	public void testGetAvailableBuffers() {
		assertEquals("Expected all buffers to be available.",
				RingBuffer.NUMBER_BUFFERS, ring.getAvailableBuffers());
		final byte[] buffer = RingBuffer.freshBuffer();
		for (int i = 0; i < RingBuffer.NUMBER_BUFFERS; i++) {
			putBuffer(ring, buffer, true);
			assertUsedBuffers(ring, i + 1);
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
	 * @throws InterruptedException
	 *             if a get buffer operation fails
	 * 
	 * @see RingBuffer#tryPutBuffer(byte [])
	 */
	@Test
	public void testPut() throws InterruptedException {
		final byte[] out = RingBuffer.freshBuffer();
		byte[] buffer = RingBuffer.freshBuffer();
		this.clear(ring);
		for (int i = 0; i < RingBuffer.NUMBER_BUFFERS / 2; i++) {
			Arrays.fill(buffer, (byte) i);
			putBuffer(ring, buffer, true);
			this.assertUsedBuffers(ring, 1);
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
		putBuffer(ring, buffer, true);
		assertRingBufferFull(ring);
		putBuffer(ring, buffer, false);
	}

	/**
	 * Tests that get waits on put successfully.
	 */
	@Test
	public void testGetWaitingOnPut() {
		ring.clear();
		final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(
				2);
		final ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 200L,
				TimeUnit.MILLISECONDS, queue);
		final long sleepMilliseconds = 10L;
		final long timeoutMsec = 2 * sleepMilliseconds;
		final Putter putter = new Putter(ring, sleepMilliseconds);
		final Getter getter = new Getter(ring);
		final Future<Boolean> putFuture = executor.submit(putter);
		final Future<byte[]> getFuture = executor.submit(getter);
		try {
			assertTrue("Expected put to return true.", putFuture.get(
					timeoutMsec, TimeUnit.MILLISECONDS));
			getFuture.get(timeoutMsec, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ie) {
			fail("Test interrupted.\n" + ie.getMessage());
		} catch (TimeoutException te) {
			fail("Test timed out.\n" + te.getMessage());
		} catch (ExecutionException ee) {
			fail("Worker threw an exception.\n" + ee.getMessage());
		}
	}

	static class Getter implements Callable<byte[]> {
		private transient final RingBuffer ring;
		private static final byte[] buffer = RingBuffer.freshBuffer();

		Getter(final RingBuffer ring) {
			this.ring = ring;
		}

		public byte[] call() throws InterruptedException {
			ring.getBuffer(buffer);
			return buffer; // NOPMD
		}
	}

	static class Putter implements Callable<Boolean> {

		private transient final RingBuffer ring;

		private transient final long msecToSleep;

		Putter(final RingBuffer ring, final long milliseconds) {
			this.ring = ring;
			this.msecToSleep = milliseconds;
		}

		public Boolean call() {
			Boolean rval = Boolean.FALSE;
			try {
				Thread.sleep(this.msecToSleep);
				final boolean result = ring.tryPutBuffer(RingBuffer
						.freshBuffer());
				rval = Boolean.valueOf(result);
			} catch (InterruptedException ie) {
				// failure
			}
			return rval;
		}
	}
}
