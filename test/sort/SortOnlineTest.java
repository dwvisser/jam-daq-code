package test.sort;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import jam.data.HistInt1D;
import jam.script.Session;

import org.junit.After;
import org.junit.Test;

/**
 * Suite of tests checking the proper behavior of the online sorting mode.
 * 
 * @author Dale Visser
 */
public class SortOnlineTest {

	private static Session session = OnlineTestCommon.session;

	private static final String SORTNAME = "help.sortfiles.EvsDE";

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
		session.cancelOnline();
	}

	/**
	 * Tests whether we can successfully setup online sorting, cancel online
	 * sorting, then setup online sorting again.
	 */
	@Test
	public void testSetupOnThenCancelThenSetupOn() {// NOPMD
		OnlineTestCommon.setupWithinTimeoutPeriod(SORTNAME);
		verifyEnergyHistogramExists();
		session.cancelOnline();
		OnlineTestCommon.setupWithinTimeoutPeriod(SORTNAME);
		verifyEnergyHistogramExists();
	}

	/**
	 * Tests whether we can successfully setup online sorting, start acquisition
	 * for a few seconds and confirm counts sorted into a histogram.
	 */
	@Test
	public void testSuccessfulOnlineSort() {
		OnlineTestCommon.setupWithinTimeoutPeriod(SORTNAME);
		final HistInt1D energy = verifyEnergyHistogramExists();
		session.online.start();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException ie) {
			fail("Interrupted while sleeping." + ie.getMessage());
		}
		session.online.stop();
		assertTrue("Expected counts > 0.", energy.getArea() > 0.0);
	}

}
