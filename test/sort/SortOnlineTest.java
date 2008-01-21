package test.sort;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import jam.Script;
import jam.data.HistInt1D;
import jam.sort.stream.YaleInputStream;
import jam.sort.stream.YaleOutputStream;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Test;

import test.sort.mockfrontend.GUI;

/**
 * Suite of tests checking the proper behavior of the online sorting mode.
 * 
 * @author Dale Visser
 */
public class SortOnlineTest {

	private static Script script = new Script();

	private static final Runnable setupRunner = new Runnable() {
		public void run() {
			script.setupOnline("help.sortfiles.EvsDE", YaleInputStream.class,
					YaleOutputStream.class);
		}
	};

	static {
		GUI.main(null);
	}

	private static Future<?> createSetupOnlineFuture() {
		final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(
				1);
		final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 200L,
				TimeUnit.MILLISECONDS, queue);
		return executor.submit(setupRunner);
	}

	private static void setupWithinTimeoutPeriod() {
		final Future<?> setupOnlineFuture = createSetupOnlineFuture();
		try {
			setupOnlineFuture.get(2L, TimeUnit.SECONDS);
		} catch (TimeoutException te) {
			fail(te.getMessage());
		} catch (ExecutionException ee) {
			fail(ee.getMessage());
		} catch (InterruptedException ie) {
			fail(ie.getMessage());
		}
	}

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
		setupWithinTimeoutPeriod();
		verifyEnergyHistogramExists();
		script.cancelOnline();
		setupWithinTimeoutPeriod();
		verifyEnergyHistogramExists();
	}

	/**
	 * Tests whether we can successfully setup online sorting, start acquisition
	 * for a few seconds and confirm counts sorted into a histogram.
	 */
	@Test
	public void testSuccessfulOnlineSort() {
		setupWithinTimeoutPeriod();
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
