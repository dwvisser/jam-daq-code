package jam.data;

import junit.framework.TestCase;
import java.awt.Polygon;

/**
 * @author dwvisser
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class GateTest extends TestCase {

	Gate g1, g2; //1d and 2d, respectively
	Histogram h1; //1d
	Histogram h2; //2d

	Polygon box; //box for 2d gate
	int[] xpoints = { 10, 50, 50, 10 };
	int[] ypoints = { 10, 10, 50, 50 };
	int npoints = 4;

	/**
	 * Constructor for GateTest.
	 * @param arg0
	 */
	public GateTest(String arg0) {
		super(arg0);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		h1 = new Histogram("h1", Histogram.ONE_DIM_INT, 100, "h1");
		h2 = new Histogram("h2", Histogram.TWO_DIM_INT, 100, "h2");
		g1 = new Gate("g1", h1);
		g1.setLimits(10,50);
		g2 = new Gate("g2", h2);
		box = new Polygon(xpoints, ypoints, 4);
		g2.setLimits(box);
	}

	/*
	 * Test for boolean inGate(int)
	 */
	public void testInGateI() {
		try {
			boolean assertion1 = g1.inGate(20);
			boolean assertion2 = !g1.inGate(5);
			boolean assertion3 = !g1.inGate(60);
			this.assertTrue(assertion1);
			this.assertTrue(assertion2);
			this.assertTrue(assertion3);
		} catch (DataException de) {
			System.err.println(de);
		}
	}

	/*
	 * Test for boolean inGate(int, int)
	 */
	public void testInGateII() {
		try {
			boolean assertion1 = g2.inGate(20, 20);
			boolean assertion2 = !g2.inGate(5, 20);
			boolean assertion3 = !g2.inGate(60, 20);
			boolean assertion4 = !g2.inGate(20, 5);
			boolean assertion5 = !g2.inGate(5, 5);
			boolean assertion6 = !g2.inGate(60, 60);
			boolean assertion7 = !g2.inGate(20, 60);
			this.assertTrue(assertion1);
			this.assertTrue(assertion2);
			this.assertTrue(assertion3);
			this.assertTrue(assertion4);
			this.assertTrue(assertion5);
			this.assertTrue(assertion6);
			this.assertTrue(assertion7);
		} catch (DataException de) {
			System.err.println(de);
		}
	}

}
