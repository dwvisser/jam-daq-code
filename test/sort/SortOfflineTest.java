package test.sort;

import static org.junit.Assert.assertEquals;
import injection.GuiceInjector;
import jam.data.AbstractHistogram;
import jam.data.HistInt1D;
import jam.script.Session;
import jam.sort.stream.YaleCAEN_InputStream;
import jam.sort.stream.YaleInputStream;
import jam.sort.stream.YaleOutputStream;

import java.io.File;

import org.junit.Test;

/**
 * Test offline sorting.
 * @author Dale Visser
 */
public class SortOfflineTest {

    private transient final Session session = GuiceInjector
            .getObjectInstance(Session.class);

    private static void assertHistogramZeroed(final AbstractHistogram histogram) {
        assertEquals("Expected '" + histogram.getName() + "' to be zeroed.",
                histogram.getArea(), 0.0);
    }

    private void sortEventFile(final String eventFileName) {
        final File eventFile = session.defineFile(eventFileName);
        session.addEventFile(eventFile);
        session.beginSort();
    }

    /**
     * Test YaleCAEN stream offline sorting.
     */
    @Test
    public void testYaleCAENOfflineSort() {
        try {
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
                    (double) expectedEvents, neutronE.getArea());
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
        } finally {
            session.resetOfflineSorting();
        }
    }
}
