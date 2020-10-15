package test.sort;

import static org.junit.Assert.assertEquals;
import jam.sort.GainCalibration;
import jam.sort.SortException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the proper functioning of gain calibrations.
 * 
 * @author Dale Visser
 * 
 */
public final class GainCalibrationTest {

	private static final String CALIBRATION_DATA = "0 1.1 3\n1 1.0 2\n2 0.9 0";

	private transient File calibrationFile;

	/**
	 * Creates a new test object.
	 */
	public GainCalibrationTest() {// NOPMD
		// do nothing
	}

	/**
	 * prepares for the test by writing a calibration text file
	 * 
	 * @throws IOException
	 *             if there is a problem accessing the file system
	 */
	@Before
	public void before() throws IOException {
		calibrationFile = File.createTempFile("calibration", ".txt");
		calibrationFile.deleteOnExit();
		final FileWriter writer = new FileWriter(calibrationFile);
		writer.write(CALIBRATION_DATA);
		writer.close();
	}

	/**
	 * Performs the calibration test.
	 * 
	 * @throws SortException
	 *             if an unrecoverable error occurs
	 */
	@Test
	public void test() throws SortException {
		final GainCalibration gain = new GainCalibration();
		gain.gainFile(calibrationFile.getPath(), true);
		assertEquals("Gain adjusted value should be 4.1", 4.1, gain
				.adjustExact(0, 1), 0.001);
		assertEquals("Gain adjusted value should be 3.0", 3.0, gain
				.adjustExact(1, 1), 0.001);
		assertEquals("Gain adjusted value should be 0.9", 0.9, gain
				.adjustExact(2, 1), 0.001);
	}
}
