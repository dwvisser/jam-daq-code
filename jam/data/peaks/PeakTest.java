package jam.data.peaks;

import junit.framework.TestCase;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class PeakTest extends TestCase {

	Peak p1,p2a,p2b,p3;

	/**
	 * Constructor for PeakTest.
	 * @param arg0
	 */
	public PeakTest(String arg0) {
		super(arg0);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		p1=new Peak(100,10,2);
		p2a=new Peak(150,10,2);
		p2b=new Peak(150,10,2);
		p3=new Peak(200,10,2);
	}

	public void testCompareTo() {
		assertEquals(-1,p1.compareTo(p2a));
		assertEquals(-1,p1.compareTo(p2b));
		assertEquals(-1,p1.compareTo(p3));
		assertEquals(-1,p2a.compareTo(p3));
		assertEquals(-1,p2b.compareTo(p3));
		assertEquals(0,p2a.compareTo(p2b));
		assertEquals(0,p2a.compareTo(p2b));
		assertEquals(1,p2a.compareTo(p1));
		assertEquals(1,p2b.compareTo(p1));
		assertEquals(1,p3.compareTo(p1));
		assertEquals(1,p3.compareTo(p2a));
		assertEquals(1,p3.compareTo(p2b));
	}

}
