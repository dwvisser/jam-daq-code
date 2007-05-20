package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import test.data.GateTest;
import test.data.HistogramTest;
import test.data.func.CubicFunctionTest;
import test.data.peaks.PeakTest;
import test.global.JamPropertiesTest;
import test.io.ImpExpASCIITest;
import test.sort.GainCalibrationTest;
import test.sort.NetDaemonTest;
import test.sort.RingBufferTest;

/**
 * JUnit test suit for all of jam.
 * @author Dale Visser
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({GateTest.class, HistogramTest.class, PeakTest.class,
		ImpExpASCIITest.class, GainCalibrationTest.class, 
		RingBufferTest.class, NetDaemonTest.class, 
		CubicFunctionTest.class, JamPropertiesTest.class})
public class AllTests {//NOPMD	
}
