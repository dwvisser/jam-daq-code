package test;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import test.data.GateTest;
import test.data.HistogramTest;
import test.data.ParameterTest;
import test.data.func.CubicFunctionTest;
import test.data.peaks.PeakTest;
import test.global.JamPropertiesTest;
import test.injection.CommandFinderTest;
import test.injection.FrameTest;
import test.io.ImpExpASCIITest;
import test.io.hdf.HDFIOTest;
import test.sort.GainCalibrationTest;
import test.sort.OnlineScalerTest;
import test.sort.RingBufferTest;
import test.sort.SortOfflineTest;
import test.sort.SortOnlineTest;
import test.ui.MultipleFileChooserTest;
import test.util.StringUtilitiesTest;

/**
 * JUnit test suit for all of jam.
 *
 * @author Dale Visser
 */
@Suite
@SelectClasses({
  GateTest.class,
  HistogramTest.class,
  ParameterTest.class,
  PeakTest.class,
  ImpExpASCIITest.class,
  GainCalibrationTest.class,
  RingBufferTest.class,
  CubicFunctionTest.class,
  JamPropertiesTest.class,
  SortOfflineTest.class,
  SortOnlineTest.class,
  OnlineScalerTest.class,
  StringUtilitiesTest.class,
  FrameTest.class,
  CommandFinderTest.class,
  HDFIOTest.class,
  MultipleFileChooserTest.class
})
public class AllTests { // NOPMD
}
