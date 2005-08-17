package jam.sort;

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * JUnit tests for <code>jam.sort.RingBuffer</data>.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @see RingBuffer
 */
public class RingBufferTest extends TestCase {

	private transient final RingBuffer ring = new RingBuffer();

	private transient final RingBuffer emptyRing = new RingBuffer(true);

	private transient final byte[] buffer = new byte[RingBuffer.BUFFER_SIZE];

	private transient final byte[] out = new byte[RingBuffer.BUFFER_SIZE];

	/**
	 * Constructor for HistogramTest.
	 * 
	 * @param arg0
	 */
	public RingBufferTest(String arg0) {
		super(arg0);
	}

	/**
	 * Initialize local variables for the tests.
	 * 
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * Test for <code>putBuffer()</code>.
	 * 
	 * @see RingBuffer#putBuffer(byte [])
	 */
	public void testPut() {
		for (int i = 0; i < RingBuffer.NUMBER_BUFFERS / 2; i++) {
			Arrays.fill(buffer, (byte) i);
			try {
				ring.putBuffer(buffer);
			} catch (RingFullException re) {
				re.printStackTrace();
			}
			ring.getBuffer(out);
		}
		assertTrue("Buffer not empty when it should have been.", ring.isEmpty());
		assertFalse("Buffer full when it shouldn't have been.", ring.isFull());
		/* Next expression true, since we did get right after each put. */
		final boolean equality = Arrays.equals(buffer, out);
		assertTrue("Arrays should have been equal.", equality);
		ring.clear();
		for (int i = 0; i < RingBuffer.NUMBER_BUFFERS; i++) {
			Arrays.fill(buffer, (byte) (i + 1));
			try {
				ring.putBuffer(buffer);
			} catch (RingFullException re) {
				re.printStackTrace();
			}
		}
		assertFalse(ring.isEmpty());
		assertTrue(
				"After filling buffer, expected it to to indicate it was full.",
				ring.isFull());
		ring.getBuffer(out);
		/* Next expression is false because buffer if FIFO. */
		assertFalse(Arrays.equals(buffer, out));
		assertFalse(ring.isFull());
		ring.clear();
		assertTrue("After clearing ring buffer, expected it to be empty.", ring
				.isEmpty());
	}

	/**
	 * JUnit test.
	 * 
	 */
	public void testGetAvailableBuffers() {
		assertEquals("Before filling buffers, expected all buffers available.",
				RingBuffer.NUMBER_BUFFERS, ring.getAvailableBuffers());
		for (int i = 0; i < RingBuffer.NUMBER_BUFFERS; i++) {
			try {
				ring.putBuffer(buffer);
			} catch (RingFullException re) {
				re.printStackTrace();
			}
		}
		assertEquals("After filling buffer, expected zero available buffers.",
				0, ring.getAvailableBuffers());
		assertEquals("Expect no available buffers in empty ring.", 0, emptyRing
				.getAvailableBuffers());
	}
	
	public void testIsNull(){
		assertTrue("emptyRing explicitly 'null'",emptyRing.isNull());
		assertFalse("Allocated ring not null.",ring.isNull());
		assertTrue("'null' rings are full.", emptyRing.isFull());
		assertTrue("'null' rings are nearly full.", emptyRing.isCloseToFull());
		assertTrue("'null' rings are empty.",emptyRing.isEmpty());
		assertEquals("'null' rings never have used buffers.",0,emptyRing.getUsedBuffers());
	}
}
