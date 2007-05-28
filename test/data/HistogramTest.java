package test.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import jam.data.DataBase;
import jam.data.Gate;
import jam.data.Group;
import jam.data.HistDouble1D;
import jam.data.HistDouble2D;
import jam.data.HistInt1D;
import jam.data.HistInt2D;
import jam.data.Histogram;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for <code>jam.data.Histogram</data>.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @see Histogram
 */
public final class HistogramTest {//NOPMD

    private transient Gate gate1, gate2;

    private transient HistInt1D hist1;
    private transient HistDouble1D hist1f;
    private transient HistInt2D hist2;
    private transient HistDouble2D hist2f;

    /**
     * Initialize local variables for the tests.
     * 
     * @see TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        final Group group = Group.createGroup("TestHistogramGroup",
                Group.Type.FILE);
        hist1 = (HistInt1D)Histogram.createHistogram(group, new int[100], "h1");
        hist1f = (HistDouble1D)Histogram.createHistogram(group, new double[100], "h1f");
        hist2 = (HistInt2D)Histogram.createHistogram(group, new int[100][100], "h2");
        hist2f = (HistDouble2D)Histogram.createHistogram(group, new double[100][100], "h2f");
        for (int i=0; i <hist1.getSizeX(); i++){
        	hist1.setCounts(i,i);
        	hist1f.setCounts(i,i);
        }
        for (int i=0; i< hist2.getSizeX(); i++){
        	for (int j=0; j<hist2.getSizeY(); j++){
        		hist2.setCounts(i,j,i+j);
        		hist2f.setCounts(i,j,i+j);
        	}
        }
        gate1 = new Gate("g1", hist1);
        gate2 = new Gate("g2", hist2);
    }
    
    @After
    public void tearDown() {
    	DataBase.getInstance().clearAllLists();
    }
    
    /**
     * test that add counts gives good results
     *
     */
    @Test
    public void testAddCounts(){
    	final double area1before = hist1.getArea();
    	final double area2before = hist2.getArea();
    	hist1.addCounts(hist1.getCounts());
    	hist2.addCounts(hist2.getCounts());
    	assertEquals("hist1 Should be double before.", hist1.getArea(),2*area1before);
    	assertEquals("hist2 Should be double before.", hist2.getArea(),2*area2before);
    	final double area1fbefore = hist1f.getArea();
    	final double area2fbefore = hist2f.getArea();
    	hist1f.addCounts(hist1f.getCounts());
    	hist2f.addCounts(hist2f.getCounts());
    	assertEquals("hist1f Should be double before.", hist1f.getArea(),2*area1fbefore);
    	assertEquals("hist2f Should be double before.", hist2f.getArea(),2*area2fbefore);
    }

    /**
     * Test for <code>getGates</code>.
     * 
     * @see Histogram#getGates
     */
    @Test
    public void testGetGates() {
        final List<Gate> h1List = hist1.getGates();
        final int size = h1List.size();
        assertEquals("List size should be 1, actually is " + size, size, 1);
    }

    /**
     * Test for <code>getHistogram(String)</code>.
     * 
     * @see Histogram#getHistogram(String)
     */
    @Test
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
     * Test for <code>hasGate(Gate)</code>.
     * 
     * @see Histogram#hasGate(Gate)
     */
    @Test
    public void testHasGate() {
        assertTrue(hist1.getFullName() + " doesn't have gate "
                + gate1.getName(), hist1.hasGate(gate1));
        assertTrue(hist2.getFullName() + " doesn't have gate "
                + gate2.getName(), hist2.hasGate(gate2));
        assertFalse(hist1.getFullName() + " has gate " + gate2.getName(), hist1
                .hasGate(gate2));
        assertFalse(hist2.getFullName() + " has gate " + gate1.getName(), hist2
                .hasGate(gate1));
    }

}
