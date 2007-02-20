package jam;

import jam.data.GateTest;
import jam.data.HistogramTest;
import jam.data.func.CubicFunctionTest;
import jam.data.peaks.PeakTest;
import jam.io.ImpExpASCIITest;
import jam.sort.GainCalibrationTest;
import jam.sort.RingBufferTest;
import jam.sort.NetDaemonTest;
//import junit.framework.JUnit4TestAdapter;
//import junit.framework.Test;
//import junit.framework.TestSuite;
import org.junit.runners.Suite;
import org.junit.runner.RunWith;

/**
 * JUnit test suit for all of jam.
 * @author Dale Visser
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({GateTest.class, HistogramTest.class, PeakTest.class,
		ImpExpASCIITest.class, GainCalibrationTest.class, 
		RingBufferTest.class, NetDaemonTest.class, 
		CubicFunctionTest.class})
public class AllTests {//NOPMD	
}
