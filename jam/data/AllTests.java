package jam.data;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * JUnit test suite for the <code>jam.data</code> package.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @see GateTest
 * @see HistogramTest
 */
public class AllTests {
	
	private AllTests(){
	}

	/**
	 * @return the test suite
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for jam.data");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(GateTest.class));
		suite.addTest(new TestSuite(HistogramTest.class));
		//$JUnit-END$
		return suite;
	}
}
