package test.data.peaks;

import static org.junit.Assert.assertEquals;
import jam.data.peaks.Peak;

import org.junit.Test;

/**
 * JUnit tests for <code>jam.data.peak.PeakTest</code>.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 */
public final class PeakTest {// NOPMD

	private static final String SHOULD = "peak1 should have been ";

	private static final String PEAK2 = " peak2";

	private void assertGreaterThan(final Peak peak1, final Peak peak2) {
		assertEquals(SHOULD + ">" + PEAK2, 1, peak1.compareTo(peak2));
	}

	private void assertLessThan(final Peak peak1, final Peak peak2) {
		assertEquals(SHOULD + "<" + PEAK2, -1, peak1.compareTo(peak2));
	}

	private void assertSameAs(final Peak peak1, final Peak peak2) {
		assertEquals(SHOULD + "==" + PEAK2, 0, peak1.compareTo(peak2));
	}

	/**
	 * Test for <code>compareTo(Object)</code>.
	 * 
	 * @see Peak#compareTo(Object)
	 */
	@Test
	public void testCompareTo() {
		final Peak peak1 = Peak.createPeak(100, 10, 2);
		final Peak p2a = Peak.createPeak(150, 10, 2);
		final Peak p2b = Peak.createPeak(150, 10, 2);
		final Peak peak3 = Peak.createPeak(200, 10, 2);
		assertLessThan(peak1, p2a);
		assertLessThan(peak1, p2b);
		assertLessThan(peak1, peak3);
		assertLessThan(p2a, peak3);
		assertLessThan(p2b, peak3);
		assertSameAs(p2a, p2b);
		assertSameAs(p2b, p2a);
		assertGreaterThan(p2a, peak1);
		assertGreaterThan(p2b, peak1);
		assertGreaterThan(peak3, peak1);
		assertGreaterThan(peak3, p2a);
		assertGreaterThan(peak3, p2b);
	}

}
