package jam;

import jam.data.GateTest;
import jam.data.HistogramTest;
import jam.data.func.CubicFunctionTest;
import jam.data.peaks.PeakTest;
import jam.io.ImpExpASCIITest;
import jam.sort.GainCalibrationTest;
import jam.sort.NetDaemonTest;
import jam.sort.RingBufferTest;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * JUnit test suit for all of jam.
 * @author Dale Visser
 *
 */
public class AllTests {//NOPMD
	
	/**
	 * 
	 * @return the test suite to run
	 */
	public static Test suite() {
		final TestSuite suite = new TestSuite("Test for jam and sub-packages.");
		//$JUnit-BEGIN$
		suite.addTestSuite(GateTest.class);
		suite.addTestSuite(HistogramTest.class);
		suite.addTestSuite(PeakTest.class);
		suite.addTestSuite(ImpExpASCIITest.class);
		suite.addTestSuite(GainCalibrationTest.class);
		suite.addTestSuite(RingBufferTest.class);
		//following line adds UI awkwardness to test suite
		//suite.addTestSuite(NetDaemonTest.class);
		suite.addTestSuite(CubicFunctionTest.class);
		//$JUnit-END$
		return suite;
	}
}
