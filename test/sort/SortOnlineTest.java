package test.sort;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import jam.Script;
import jam.data.HistInt1D;

import org.junit.After;
import org.junit.Test;

/**
 * Suite of tests checking the proper behavior of the online sorting mode.
 * 
 * @author Dale Visser
 */
public class SortOnlineTest {

	private static Script script = OnlineTestCommon.script;

	private static final String sortName = "help.sortfiles.EvsDE";

	private static HistInt1D verifyEnergyHistogramExists() {
		final HistInt1D energy = Utility.getOneDHistogramFromSortGroup("E");
		assertNotNull("Expected histogram to exist.", energy);
		return energy;
	}

	/**
	 * Run after every test.
	 */
	@After
	public void tearDown() {
		script.cancelOnline();
	}

	/**
	 * Tests whether we can successfully setup online sorting, cancel online
	 * sorting, then setup online sorting again.
	 */
	@Test
	public void testSetupOnThenCancelThenSetupOn() {// NOPMD
		OnlineTestCommon.setupWithinTimeoutPeriod(sortName);
		verifyEnergyHistogramExists();
		script.cancelOnline();
		OnlineTestCommon.setupWithinTimeoutPeriod(sortName);
		verifyEnergyHistogramExists();
	}

	/**
	 * Tests whether we can successfully setup online sorting, start acquisition
	 * for a few seconds and confirm counts sorted into a histogram.
	 */
	@Test
	public void testSuccessfulOnlineSort() {
		OnlineTestCommon.setupWithinTimeoutPeriod(sortName);
		final HistInt1D energy = verifyEnergyHistogramExists();
		script.startAcquisition();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException ie) {
			fail("Interrupted while sleeping." + ie.getMessage());
		}
		script.stopAcquisition();
		assertTrue("Expected counts > 0.", energy.getArea() > 0.0);
	}

}
