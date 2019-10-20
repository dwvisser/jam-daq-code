package test.sort;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import injection.GuiceInjector;
import jam.data.HistInt1D;
import jam.global.JamProperties;
import jam.script.Session;

/**
 * Suite of tests checking the proper behavior of the online sorting mode.
 * @author Dale Visser
 */
@Ignore
 public class SortOnlineTest {

    // TODO Make these tests pass *reliably*, and turn off Ignore.

    private static Session session = OnlineTestCommon.session;
    static {
        // Need to make sure properties have been loaded from *.ini files.
        GuiceInjector.getObjectInstance(JamProperties.class);
    }

    private static HistInt1D setupOnlineAndVerifyHistogram() {
        final String sortRoutineName = "help.sortfiles.EvsDE";
        OnlineTestCommon.setupWithinTimeoutPeriod(sortRoutineName);
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
        setupOnlineAndVerifyHistogram();
        session.cancelOnline();
        setupOnlineAndVerifyHistogram();
    }

    /**
     * Tests whether we can successfully setup online sorting, start acquisition
     * for a few seconds and confirm counts sorted into a histogram.
     */
    @Test
    public void testSuccessfulOnlineSort() {
        final HistInt1D energy = setupOnlineAndVerifyHistogram();
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
