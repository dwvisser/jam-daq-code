package test.sort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import injection.GuiceInjector;
import jam.data.Scaler;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.script.Session;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.sort.mockfrontend.MessageSender;

/**
 * Suite of tests checking the proper behavior of the online sorting mode.
 * 
 * @author Dale Visser
 */
public class OnlineScalerTest implements Observer {

	private static Session session = OnlineTestCommon.session;

	private transient final CountDownLatch latch = new CountDownLatch(1);

	private transient final Broadcaster BROADCASTER = Broadcaster
			.getSingletonInstance();

	/**
	 * set up before test
	 */
	@Before
	public void setUp() {
		this.BROADCASTER.addObserver(this);
	}

	/**
	 * Run after every test.
	 */
	@After
	public void tearDown() {
		this.BROADCASTER.deleteObserver(this);
		session.cancelOnline();
	}

	/**
	 * Test whether clicking scaler read results in "list scaler" being sent to
	 * the front end.
	 * 
	 * @throws InterruptedException
	 *             if times out waiting for scalers to update
	 */
	@Test
	public void testListScalerSendsScalerValues() throws InterruptedException {
		OnlineTestCommon
				.setupWithinTimeoutPeriod("help.sortfiles.CamacScalerTest");
		final List<Scaler> scalerList = Scaler.getScalerList();
		assertEquals("Expected list to have one element.", 1, scalerList.size());
		final Scaler scaler = scalerList.get(0);
		assertScalerValue(scaler, 0);
		assertTrue("Expected status to be online.", GuiceInjector
				.getJamStatus().isOnline());
		session.readScalers();
		latch.await(500, TimeUnit.MILLISECONDS);
		assertTrue(
				"Expected front end to have received a 'list scaler' command.",
				OnlineTestCommon.testFrontEnd.getMessageReceiver()
						.hasReceivedListScaler());
		assertScalerValue(scaler, MessageSender.SCALER_VALUE);
	}

	private void assertScalerValue(final Scaler scaler, final int value) {
		assertEquals("Expected scaler to have a value of " + value + ".",
				value, scaler.getValue());
	}

	public void update(final Observable observable, final Object message) {
		final BroadcastEvent event = (BroadcastEvent) message;
		final BroadcastEvent.Command command = event.getCommand();
		if (command == BroadcastEvent.Command.SCALERS_UPDATE) {
			latch.countDown();
		}
	}
}
