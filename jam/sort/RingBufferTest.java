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

	private final RingBuffer ring=new RingBuffer();
	private final byte [] buffer=new byte[RingBuffer.BUFFER_SIZE];
	private final byte [] out=new byte[RingBuffer.BUFFER_SIZE];

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
		for (int i=0; i<RingBuffer.NUMBER_BUFFERS/2; i++){
			Arrays.fill(buffer,(byte)i);
			try{
				ring.putBuffer(buffer);
			} catch (RingFullException re){
				System.err.println(re);
			}
			ring.getBuffer(out);
		}
		assertTrue(ring.isEmpty());
		assertFalse(ring.isFull());
		/* Next expression true, since we did get right after each put. */
		assertTrue(Arrays.equals(buffer,out));
		ring.clear();
		for (int i=0; i<RingBuffer.NUMBER_BUFFERS; i++){
			Arrays.fill(buffer,(byte)(i+1));
			try{
				ring.putBuffer(buffer);
			} catch (RingFullException re){
				System.err.println(re);
			}
		}
		assertFalse(ring.isEmpty());
		assertTrue(ring.isFull());
		ring.getBuffer(out);
		/* Next expression is false because buffer if FIFO. */
		assertFalse(Arrays.equals(buffer,out));
		assertFalse(ring.isFull());
		ring.clear();
		assertTrue(ring.isEmpty());
	}
	
	public void testGetAvailableBuffers(){
		System.out.println("Number of buffers: "+RingBuffer.NUMBER_BUFFERS);
		assertEquals(RingBuffer.NUMBER_BUFFERS,ring.getAvailableBuffers());
		for (int i=0; i<RingBuffer.NUMBER_BUFFERS; i++){
			try{
				ring.putBuffer(buffer);
			} catch (RingFullException re){
				System.err.println(re);
			}
		}
		assertEquals(0,ring.getAvailableBuffers());
	}	
}
