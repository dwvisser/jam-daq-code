package test.data;

import jam.data.*;
import jam.data.func.LinearFunction;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for <code>jam.data.Histogram</code>.
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @see AbstractHistogram
 */
public final class HistogramTest {// NOPMD

    private static final String GROUP_NAME = "TestHistogramGroup";

    private transient Gate gate1, gate2;

    private transient final HistInt1D hist1;

    private transient final HistDouble1D hist1f;

    private transient final HistInt2D hist2;

    private transient final HistDouble2D hist2f;

    /**
     * Create a new instance of this test class.
     */
    public HistogramTest() {
        final Group group = Factory.createGroup(GROUP_NAME, Group.Type.FILE);
        hist1 = (HistInt1D) Factory.createHistogram(group, new int[100], "h1");
        hist1f = (HistDouble1D) Factory.createHistogram(group,
                new double[100], "h1f");
        hist2 = (HistInt2D) Factory.createHistogram(group, new int[100][100],
                "h2");
        hist2f = (HistDouble2D) Factory.createHistogram(group,
                new double[100][100], "h2f");
    }

    /**
     * Initialize local variables for the tests.
     */
    @Before
    public void setUp() {
        for (int i = 0; i < hist1.getSizeX(); i++) {
            hist1.setCounts(i, i);
            hist1f.setCounts(i, i);
        }
        for (int i = 0; i < hist2.getSizeX(); i++) {
            for (int j = 0; j < hist2.getSizeY(); j++) {
                hist2.setCounts(i, j, i + j);
                hist2f.setCounts(i, j, i + j);
            }
        }
        gate1 = new Gate("g1", hist1);
        gate2 = new Gate("g2", hist2);
    }

    /**
     * Clean up after tests.
     */
    @After
    public void tearDown() {
        DataBase.getInstance().clearAllLists();
    }

    /**
     * test that add counts gives good results
     */
    @Test
    public void testAddCounts() {
        final double area1before = hist1.getArea();
        Assert.assertEquals(
                "Expected getArea() and getCount() to yield the same result.",
                area1before, hist1.getCount(), 0.001);
        hist1.addCounts(hist1.getCounts());
        assertAreaDoubled(hist1, area1before);
        final double area2before = hist2.getArea();
        hist2.addCounts(hist2.getCounts());
        assertAreaDoubled(hist2, area2before);
        final double area1fbefore = hist1f.getArea();
        hist1f.addCounts(hist1f.getCounts());
        assertAreaDoubled(hist1f, area1fbefore);
        final double area2fbefore = hist2f.getArea();
        hist2f.addCounts(hist2f.getCounts());
        assertAreaDoubled(hist2f, area2fbefore);
        Assert.assertFalse("Expected no errors to be set.", hist1
                .hasErrorsSet());
        hist1.setErrors(new double[100]);
        Assert.assertTrue("Expected errors to be set.", hist1.hasErrorsSet());
        Assert.assertFalse("Expected no calibration.", hist1.isCalibrated());
        hist1.setCalibration(new LinearFunction());
        Assert.assertTrue("Expected calibration.", hist1.isCalibrated());
    }

    private void assertAreaDoubled(final AbstractHistogram histogram,
            final double area1before) {
        final String should = "should be double before.";
        Assert.assertEquals(histogram.getName() + should, histogram.getArea(),
                2 * area1before, 0.001);
    }

    /**
     * Test for <code>getGates</code>.
     * @see AbstractHistogram#getGateCollection
     * @see GateCollection#getGates()
     */
    @Test
    public void testGetGates() {
        final int size = hist1.getGateCollection().getGates().size();
        final int expectedSize = 1;
        Assert.assertEquals("Expected list size to be " + expectedSize, size,
                expectedSize);
    }

    /**
     * Test for <code>getHistogram(String)</code>.
     * @see AbstractHistogram#getHistogram(String)
     */
    @Test
    public void testGetHistogram() {
        Assert.assertNotNull("h1 nonexistent here", hist1);
        Assert.assertNotNull("Couldn't find histogram named \""
                + hist1.getFullName() + "\"", AbstractHistogram
                .getHistogram(hist1.getFullName()));
        Assert.assertNotNull("Couldn't find histogram named \""
                + hist2.getFullName() + "\"", AbstractHistogram
                .getHistogram(hist2.getFullName()));
        Assert.assertNull("Found nonexistent histogram named \"notreal\"",
                AbstractHistogram.getHistogram("notreal"));
    }

    /**
     * Test for <code>hasGate(Gate)</code>.
     * @see AbstractHistogram#getGateCollection()
     * @see jam.data.GateCollection#hasGate(DataElement)
     */
    @Test
    public void testHasGate() {
        assertHasGate(true, hist1, gate1);
        assertHasGate(true, hist2, gate2);
        assertHasGate(false, hist1, gate2);
        assertHasGate(false, hist2, gate1);
    }

    private void assertHasGate(final boolean hasGate,
            final AbstractHistogram histogram, final Gate gate) {
        final StringBuilder message = new StringBuilder(52);
        message.append("Expected ");
        message.append(histogram.getName()).append(" to ");
        if (!hasGate) {
            message.append("not ");
        }
        message.append("have gate ").append(gate.getName());
        Assert.assertEquals(message.toString(), hasGate, histogram
                .getGateCollection().hasGate(gate));
    }

}
