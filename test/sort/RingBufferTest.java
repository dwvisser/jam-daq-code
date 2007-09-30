package test.sort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jam.sort.RingBuffer;
import jam.sort.RingFullException;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for <code>jam.sort.RingBuffer</data>.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @see RingBuffer
 */
public final class RingBufferTest {//NOPMD

	private static final String ARRAYS_SHOULD_HAVE_BEEN_EQUAL = "Arrays should have been equal.";

	private transient RingBuffer ring, emptyRing;

	private transient byte[] buffer;

	@Before
	public void setUp() {
		ring = new RingBuffer();
		emptyRing = new RingBuffer(true);
		buffer = new byte[RingBuffer.BUFFER_SIZE];
	}

	/**
	 * Test for <code>putBuffer()</code>.
	 * 
	 * @see RingBuffer#putBuffer(byte [])
	 */
	@Test
	public void testPut() {
		final byte[] out = new byte[RingBuffer.BUFFER_SIZE];
		for (int i = 0; i < RingBuffer.NUMBER_BUFFERS / 2; i++) {
			Arrays.fill(buffer, (byte) i);
			try {
				ring.putBuffer(buffer);
			} catch (RingFullException re) {
				fail(re.getMessage());
			}
			ring.getBuffer(out);
		}
		assertTrue("Buffer not empty when it should have been.", ring.isEmpty());
		assertFalse("Buffer full when it shouldn't have been.", ring.isFull());
		/* Next expression true, since we did get right after each put. */
		final boolean equality = Arrays.equals(buffer, out);
		assertTrue(ARRAYS_SHOULD_HAVE_BEEN_EQUAL, equality);
		ring.clear();
		for (int i = 0; i < RingBuffer.NUMBER_BUFFERS; i++) {
			Arrays.fill(buffer, (byte) (i + 1));
			try {
				ring.putBuffer(buffer);
			} catch (RingFullException re) {
				fail(re.getMessage());
			}
		}
		assertFalse("Buffer empty when it should not have been.", ring
				.isEmpty());
		assertTrue(
				"After filling buffer, expected it to to indicate it was full.",
				ring.isFull());
		ring.getBuffer(out);
		/* Next expression is false because buffer if FIFO. */
		assertFalse("Buffers equal when they shouldn't have been.", Arrays
				.equals(buffer, out));
		assertFalse("Ring shows full when it shouldn't be.", ring.isFull());
		ring.clear();
		assertTrue("After clearing ring buffer, expected it to be empty.", ring
				.isEmpty());
	}

	@Test
	public void testGetAvailableBuffers() {
		assertEquals("Before filling buffers, expected all buffers available.",
				RingBuffer.NUMBER_BUFFERS, ring.getAvailableBuffers());
		for (int i = 0; i < RingBuffer.NUMBER_BUFFERS; i++) {
			try {
				ring.putBuffer(buffer);
			} catch (RingFullException re) {
				fail(re.getMessage());
			}
		}
		assertEquals("After filling buffer, expected zero available buffers.",
				0, ring.getAvailableBuffers());
		assertEquals("Expect no available buffers in empty ring.", 0, emptyRing
				.getAvailableBuffers());
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
}
