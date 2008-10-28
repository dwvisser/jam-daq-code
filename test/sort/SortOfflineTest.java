package test.sort;

import static org.junit.Assert.assertEquals;
import jam.data.HistInt1D;
import jam.data.Histogram;
import jam.script.Session;
import jam.sort.stream.YaleCAEN_InputStream;
import jam.sort.stream.YaleInputStream;
import jam.sort.stream.YaleOutputStream;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test offline sorting.
 * 
 * @author Dale Visser
 * 
 */
public class SortOfflineTest {// NOPMD

	private transient Session session;

	private static void assertHistogramZeroed(final Histogram histogram) {
		assertEquals("Expected '" + histogram.getName() + "' to be zeroed.",
				histogram.getArea(), 0.0);
	}

	private void sortEventFile(final String eventFileName) {
		final File eventFile = session.defineFile(eventFileName);
		session.addEventFile(eventFile);
		session.beginSort();
	}

	/**
	 * Set up session.
	 */
	@Before
	public void setUp() {
		if (null == session) {
			session = new Session();
		}
	}

	/**
	 * Tear down after test.
	 */
	@After
	public void tearDown() {
		if (null != session) {
			session.resetOfflineSorting();
		}
	}

	/**
	 * Test YaleCAEN stream offline sorting.
	 */
	@Test
	public void testYaleCAENOfflineSort() {
		session.setupOffline("help.sortfiles.YaleCAENTestSortRoutine",
				YaleCAEN_InputStream.class, YaleOutputStream.class);
		final HistInt1D neutronE = Utility
				.getOneDHistogramFromSortGroup("Neutron E");
		assertHistogramZeroed(neutronE);
		sortEventFile("test/sort/YaleCAENTestData.evn");
		final int expectedEvents = 302;
		assertEquals("Events sorted wasn't the same as expected.",
				expectedEvents, session.getEventsSorted());
		assertEquals("Area in histogram wasn't the same as expected.",
				expectedEvents, neutronE.getArea());
	}

	/**
	 * Test Yale stream offline sorting.
	 */
	@Test
	public void testYaleOfflineSort() {
		final String sortRoutineName = "SpectrographExample";
		session.setupOffline("help.sortfiles." + sortRoutineName,
				YaleInputStream.class, YaleOutputStream.class);
		final HistInt1D cathode = Utility
				.getOneDHistogramFromSortGroup("Cathode");
		assertHistogramZeroed(cathode);
		sortEventFile("sampledata/example.evn");
		final double expectedArea = 789.0;
		assertEquals("Area in histogram wasn't the same as expected.",
				expectedArea, cathode.getArea());
	}
}
