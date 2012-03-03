package test.sort;

import static jam.global.PropertyKeys.EVENT_INPATH;
import static org.junit.Assert.assertEquals;
import injection.GuiceInjector;
import jam.data.AbstractHistogram;
import jam.data.HistInt1D;
import jam.global.JamProperties;
import jam.script.Session;
import jam.sort.control.SortControl;
import jam.sort.stream.YaleCAEN_InputStream;
import jam.sort.stream.YaleInputStream;
import jam.sort.stream.YaleOutputStream;
import jam.ui.Icons;
import jam.ui.MultipleFileChooser;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

/**
 * Test offline sorting.
 * 
 * @author Dale Visser
 */
public class SortOfflineTest {

	private transient final Session session = GuiceInjector
			.getObjectInstance(Session.class);

	private static void assertHistogramZeroed(final AbstractHistogram histogram) {
		assertEquals("Expected '" + histogram.getName() + "' to be zeroed.",
				histogram.getArea(), 0.0);
	}

	private void sortEventFile(final String eventFileName, final int repitition) {
		final File eventFile = session.defineFile(eventFileName);
		for (int i = 0; i < repitition; i++) {
			session.addEventFile(eventFile);
		}
		session.beginSort();
	}

	/**
	 * Test YaleCAEN stream offline sorting.
	 */
	@Test
	public void testYaleCAENOfflineSort() {
		try {
			final HistInt1D neutronE = setupYaleCAENOfflineSort();
			sortEventFile("test/sort/YaleCAENTestData.evn", 1);
			assertPostConditionsForYaleCAEN(neutronE, 302);
		} finally {
			session.resetOfflineSorting();
		}
	}

	private void assertPostConditionsForYaleCAEN(final HistInt1D neutronE,
			final int expectedEvents) {
		assertEquals("Events sorted wasn't the same as expected.",
				expectedEvents, session.getEventsSorted());
		assertEquals("Area in histogram wasn't the same as expected.",
				(double) expectedEvents, neutronE.getArea());
	}

	private HistInt1D setupYaleCAENOfflineSort() {
		session.setupOffline("help.sortfiles.YaleCAENTestSortRoutine",
				YaleCAEN_InputStream.class, YaleOutputStream.class);
		final HistInt1D neutronE = Utility
				.getOneDHistogramFromSortGroup("Neutron E");
		assertHistogramZeroed(neutronE);
		return neutronE;
	}

	/**
	 * Test YaleCAEN sorting two files.
	 */
	@Test
	public void testYaleCAENOfflineSortTwoFiles() {
		try {
			final HistInt1D neutronE = setupYaleCAENOfflineSort();
			sortEventFile("test/sort/YaleCAENTestData.evn", 2);
			assertPostConditionsForYaleCAEN(neutronE, 2 * 302);
		} finally {
			session.resetOfflineSorting();
		}

	}

	/**
	 * Test Yale stream offline sorting.
	 */
	@Test
	public void testYaleOfflineSort() {
		try {
			final HistInt1D cathode = setupYaleOfflineSort();
			sortEventFile("sampledata/example.evn", 1);
			assertEquals("Area in histogram wasn't the same as expected.",
					789.0, cathode.getArea());
		} finally {
			session.resetOfflineSorting();
		}
	}

	private HistInt1D setupYaleOfflineSort() {
		final String sortRoutineName = "SpectrographExample";
		session.setupOffline("help.sortfiles." + sortRoutineName,
				YaleInputStream.class, YaleOutputStream.class);
		final HistInt1D cathode = Utility
				.getOneDHistogramFromSortGroup("Cathode");
		assertHistogramZeroed(cathode);
		return cathode;
	}

	/**
	 * Test Yale stream offline sorting.
	 */
	@Test
	public void testYaleOfflineSortTwoFiles() {
		try {
			final HistInt1D cathode = setupYaleOfflineSort();
			sortEventFile("sampledata/example.evn", 2);
			assertEquals("Area in histogram wasn't the same as expected.",
					2 * 789.0, cathode.getArea());
		} finally {
			session.resetOfflineSorting();
		}
	}

	/**
	 * Tests that SortControl defaults to EVENT_INPATH
	 * 
	 * @throws IOException
	 *             if file access problem occurs
	 */
	@Test
	public void testEventInPathBeingUsed() throws IOException {
		final String eventInpath = File.createTempFile("jam", null).getParent()
				.toString();
		JamProperties.setProperty(EVENT_INPATH, eventInpath);
		final File expectedPath = (new File(
				JamProperties.getPropString(EVENT_INPATH))).getCanonicalFile();
		final SortControl sc = new SortControl(null, null, new Icons());
		final MultipleFileChooser mfc = sc.getFileChooser();
		final File actualPath = mfc.getCurrentFolder().getCanonicalFile();
		assertEquals(expectedPath, actualPath);
	}
}
