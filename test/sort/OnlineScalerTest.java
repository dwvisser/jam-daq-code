package test.sort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jam.Script;
import jam.data.Scaler;
import jam.global.JamStatus;

import java.util.List;

import org.junit.After;
import org.junit.Test;

import test.sort.mockfrontend.MessageSender;

/**
 * Suite of tests checking the proper behavior of the online sorting mode.
 * 
 * @author Dale Visser
 */
public class OnlineScalerTest {

	private static Script script = OnlineTestCommon.script;

	/**
	 * Run after every test.
	 */
	@After
	public void tearDown() {
		script.cancelOnline();
	}

	/**
	 * Test whether clicking scaler read results in "list scaler" being sent to
	 * the front end.
	 */
	@Test
	public void testListScalerSendsScalerValues() {
		OnlineTestCommon
				.setupWithinTimeoutPeriod("help.sortfiles.CamacScalerTest");
		final List<Scaler> scalerList = Scaler.getScalerList();
		assertEquals("Expected list to have one element.", 1, scalerList.size());
		final Scaler scaler = scalerList.get(0);
		assertScalerValue(scaler, 0);
		assertTrue("Expected status to be online.", JamStatus
				.getSingletonInstance().isOnline());
		script.readScalers();
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
}
