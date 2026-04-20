package test.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import injection.GuiceInjector;
import jam.data.Scaler;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.script.Session;
import test.sort.mockfrontend.MessageSender;

/**
 * Suite of tests checking the proper behavior of the online sorting mode.
 * @author Dale Visser
 */
public class OnlineScalerTest implements PropertyChangeListener {

    private static Session session = OnlineTestCommon.session;

    private transient final CountDownLatch latch = new CountDownLatch(1);

    private transient final Broadcaster BROADCASTER = GuiceInjector
            .getObjectInstance(Broadcaster.class);

    /**
     * set up before test
     */
    @BeforeEach
    public void setUp() {
        this.BROADCASTER.addPropertyChangeListener(this);
    }

    /**
     * Run after every test.
     */
    @AfterEach
    public void tearDown() {
        this.BROADCASTER.removePropertyChangeListener(this);
        session.cancelOnline();
    }

    /**
     * Test whether clicking scaler read results in "list scaler" being sent to
     * the front end.
     * @throws InterruptedException
     *             if times out waiting for scalers to update
     */
    @Disabled
    @Test
    public void testListScalerSendsScalerValues() throws InterruptedException {
        // TODO Make this test pass *reliably*, and turn off Ignore.
        OnlineTestCommon
                .setupWithinTimeoutPeriod("help.sortfiles.CamacScalerTest");
        final List<Scaler> scalerList = Scaler.getScalerList();
        assertEquals(1, scalerList.size(),
                "Expected list to have one element.");
        final Scaler scaler = scalerList.get(0);
        assertScalerValue(scaler, 0);
        assertTrue(GuiceInjector
                .getObjectInstance(JamStatus.class).isOnline(), "Expected status to be online.");
        session.readScalers();
        latch.await(500, TimeUnit.MILLISECONDS);
        assertTrue(
                OnlineTestCommon.getMessageReceiver().hasReceivedListScaler(),
                "Expected front end to have received a 'list scaler' command.");
        assertScalerValue(scaler, MessageSender.SCALER_VALUE);
    }

    private void assertScalerValue(final Scaler scaler, final int value) {
        assertEquals(value, scaler.getValue(), "Expected scaler to have a value of " + value + ".");
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (((BroadcastEvent) evt).getCommand() == BroadcastEvent.Command.SCALERS_UPDATE) {
            latch.countDown();
        }
    }
}
