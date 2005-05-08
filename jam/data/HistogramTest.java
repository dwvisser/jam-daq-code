package jam.data;

import java.util.List;

import junit.framework.TestCase;

/**
 * JUnit tests for <code>jam.data.Histogram</data>.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @see Histogram
 * @see AllTests
 */
public class HistogramTest extends TestCase {

    Gate gate1, gate2;

    Histogram hist1, hist2;

    /**
     * Constructor for HistogramTest.
     * 
     * @param arg0
     */
    public HistogramTest(String arg0) {
        super(arg0);
    }

    /**
     * Initialize local variables for the tests.
     * 
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        final Group group = Group.createGroup("TestHistogramGroup",
                Group.Type.FILE);
        hist1 = Histogram.createHistogram(group, new int[100], "h1");
        hist2 = Histogram.createHistogram(group, new int[100][100], "h2");
        gate1 = new Gate("g1", hist1);
        gate2 = new Gate("g2", hist2);
    }

    /**
     * Test for <code>hasGate(Gate)</code>.
     * 
     * @see Histogram#hasGate(Gate)
     */
    public void testHasGate() {
        assertTrue(hist1.getFullName() + " doesn't have gate "
                + gate1.getName(), hist1.hasGate(gate1));
        assertTrue(hist2.getFullName() + " doesn't have gate "
                + gate2.getName(), hist2.hasGate(gate2));
        assertTrue(hist1.getFullName() + " has gate " + gate2.getName(), !hist1
                .hasGate(gate2));
        assertTrue(hist2.getFullName() + " has gate " + gate1.getName(), !hist2
                .hasGate(gate1));
    }

    /**
     * Test for <code>getHistogram(String)</code>.
     * 
     * @see Histogram#getHistogram(String)
     */
    public void testGetHistogram() {
        assertNotNull("h1 nonexistent here", hist1);
        assertNotNull("Couldn't find histogram named \""
                + hist1.getFullName() + "\"", Histogram
                .getHistogram(hist1.getFullName()));
        assertNotNull("Couldn't find histogram named \""
                + hist2.getFullName() + "\"", Histogram
                .getHistogram(hist2.getFullName()));
        assertNull("Found nonexistent histogram named \"notreal\"", Histogram
                .getHistogram("notreal"));
    }

    /**
     * Test for <code>getGates</code>.
     * 
     * @see Histogram#getGates
     */
    public void testGetGates() {
        final List h1List = hist1.getGates();
        final int size = h1List.size();
        assertEquals("List size should be 1, actually is " + size, size, 1);
    }

}
