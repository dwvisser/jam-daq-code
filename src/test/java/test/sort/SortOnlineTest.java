package test.sort;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import injection.GuiceInjector;
import jam.data.HistInt1D;
import jam.global.JamProperties;
import jam.script.Session;

/**
 * Suite of tests checking the proper behavior of the online sorting mode.
 * @author Dale Visser
 */
@Disabled
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
        assertNotNull(energy, "Expected histogram to exist.");
        return energy;
    }

    /**
     * Run after every test.
     */
    @AfterEach
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
        assertTrue(energy.getArea() > 0.0, "Expected counts > 0.");
    }

}
