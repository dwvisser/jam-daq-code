package jam;

import jam.data.GateTest;
import jam.data.HistogramTest;
import jam.data.peaks.PeakTest;
import jam.sort.NetDaemonTest;
import jam.sort.RingBufferTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
	public static Test suite() {
		final TestSuite suite = new TestSuite("Test for jam and sub-packages.");
		//$JUnit-BEGIN$
		suite.addTestSuite(GateTest.class);
		suite.addTestSuite(HistogramTest.class);
		suite.addTestSuite(PeakTest.class);
		suite.addTestSuite(RingBufferTest.class);
		suite.addTestSuite(NetDaemonTest.class);
		//$JUnit-END$
		return suite;
	}
}
