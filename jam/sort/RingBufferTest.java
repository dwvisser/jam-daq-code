package jam.sort;

import junit.framework.TestCase;

/**
 * JUnit tests for <code>jam.sort.RingBuffer</data>.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @see RingBuffer
 */
public class RingBufferTest extends TestCase {

	private RingBuffer ring=new RingBuffer();
	final byte [] buffer=new byte[RingBuffer.BUFFER_SIZE];

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
			try{
				ring.putBuffer(buffer);
			} catch (RingFullException re){
				System.err.println(re);
			}
			ring.getBuffer();
		}
		assertTrue(ring.empty());
		final int most=RingBuffer.NUMBER_BUFFERS*3/4;
		for (int i=0; i<most; i++){
			try{
				ring.putBuffer(buffer);
			} catch (RingFullException re){
				System.err.println(re);
			}
		}
	}
	

}
