package jam.data;

import junit.framework.TestCase;

/**
 * @author dwvisser
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class HistogramTest extends TestCase {

	Gate g1, g2; 
	Histogram h1,h2; 

	/**
	 * Constructor for HistogramTest.
	 * @param arg0
	 */
	public HistogramTest(String arg0) {
		super(arg0);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		h1 = new Histogram("h1", Histogram.ONE_DIM_INT, 100, "h1");
		h2 = new Histogram("h2", Histogram.ONE_DIM_INT, 100, "h2");
		g1 = new Gate("g1",h1);
		g2 = new Gate("g2",h2);
	}

	public void testHasGate() {
		assertTrue(h1.hasGate(g1));
		assertTrue(h2.hasGate(g2));
		assertTrue(!h1.hasGate(g2));
		assertTrue(!h2.hasGate(g1));
	}
	
	public void testGetHistogram() {
		assertNotNull("h1 nonexistent here", h1);
		assertNotNull("Couldn't find histogram named \""+h1.getName()+"\"",Histogram.getHistogram(h1.getName()));
		assertNotNull("Couldn't find histogram named \""+h2.getName()+"\"",Histogram.getHistogram(h2.getName()));
		assertNull("Found nonexistent histogram named \"notreal\"",Histogram.getHistogram("notreal"));
	}
	
	public void testGetGates(){
		Gate [] h1List=h1.getGates();
		assertEquals(h1List.length,1);
	}

}
