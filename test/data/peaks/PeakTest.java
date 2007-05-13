package test.data.peaks;

import static org.junit.Assert.assertEquals;
import jam.data.peaks.Peak;

import org.junit.Test;

/**
 * JUnit tests for <code>jam.data.peak.PeakTest</code>.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 */
public final class PeakTest {//NOPMD

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
         assertEquals("peak1 should have been < p2a", -1, peak1.compareTo(p2a));
        assertEquals("peak1 should have been < p2b", -1, peak1.compareTo(p2b));
        assertEquals("peak1 should have been < peak3", -1, peak1
                .compareTo(peak3));
        assertEquals("p2a should have been < peak3", -1, p2a.compareTo(peak3));
        assertEquals("p2b should have been < peak3", -1, p2b.compareTo(peak3));
        assertEquals("p2a should have been = p2b", 0, p2a.compareTo(p2b));
        assertEquals("p2b should have been = p2a", 0, p2b.compareTo(p2a));
        assertEquals("p2a should have been > peak1", 1, p2a.compareTo(peak1));
        assertEquals("p2b should have been > peak1", 1, p2b.compareTo(peak1));
        assertEquals("peak3 should have been > peak1", 1, peak3
                .compareTo(peak1));
        assertEquals("peak3 should have been > p2a", 1, peak3.compareTo(p2a));
        assertEquals("peak3 should have been > p2b", 1, peak3.compareTo(p2b));
    }

}
