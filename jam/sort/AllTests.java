package jam.sort;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		final TestSuite suite = new TestSuite("Test for jam.sort");
		//$JUnit-BEGIN$
		suite.addTestSuite(RingBufferTest.class);
		suite.addTestSuite(NetDaemonTest.class);
		//$JUnit-END$
		return suite;
	}

}
