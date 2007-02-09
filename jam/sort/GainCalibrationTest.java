package jam.sort;

import junit.framework.TestCase;

public class GainCalibrationTest extends TestCase {//NOPMD

	private transient GainCalibration gain;
	
	protected void setUp() throws Exception {
		super.setUp();
		gain=new GainCalibration(this);
	}

	protected void tearDown() throws Exception {//NOPMD
		super.tearDown();
	}

	public final void testGainFile() {
		try {
			gain.gainFile("jam/sort/GainCalibrationTest.txt", true);
		} catch (Exception e) {
			fail(e.getMessage());
		}
		assertEquals("Gain adjusted value should be 4.1", 4.1, gain.adjustExact(0, 1));
		assertEquals("Gain adjusted value should be 3.0", 3.0, gain.adjustExact(1, 1));
		assertEquals("Gain adjusted value should be 0.9", 0.9 , gain.adjustExact(2, 1));
	}

}
