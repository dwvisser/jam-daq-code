package jam.data;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author dwvisser
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for jam.data");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(GateTest.class));
		suite.addTest(new TestSuite(HistogramTest.class));
		//$JUnit-END$
		return suite;
	}
}
