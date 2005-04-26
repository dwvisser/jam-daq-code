package jam.data;
import jam.util.StringUtilities;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A gate, used for data sorting, belongs to a histogram which determines what type of
 * gate it is.  There are two types of gates, <code>ONE_DIMENDION</code> and <code>TWO_DIMENSION</code>,
 *
 * @author Ken Swartz
 * @version 0.5
 * @since JDK 1.1
 */
public final class Gate implements DataElement {

	/**
	 * Maximum number of characters in the histogram name.
	 */
	public static final int NAME_LENGTH = 16;

	/* static structures to hold all gates */
	private static final Map TABLE = Collections.synchronizedMap(new HashMap());
	private static final List LIST = Collections.synchronizedList(new ArrayList());
	private static final List [] DIM_LIST= new List[2];
	static {
		for (int i=0; i<DIM_LIST.length; i++){
			DIM_LIST[i]=Collections.synchronizedList(new ArrayList());
		}		
	}

	private transient final String name; //name of gate
	private transient final String histName; //histogram gate belongs to
	private transient final int dimensions; //type of gate ONE_DIMESION or TWO_DIMENSION

	private transient final int sizeX; //size of gate for 1d and x of 2d
	private transient final int sizeY; //size used for 2d histograms only

	private transient boolean isSet;

	/* values for 1 d gate */
	private transient int lowerLimit; //lower limit for 1d gate
	private transient int upperLimit; //upper limit for 1d gate

	/* values for a 2d gate */
	private transient final Polygon bananaGate=new Polygon(); //polygon of 2d gate
	private transient boolean insideGate[][]; // true if point inside 2d gate

	/**
	 * Constructs a new gate with the given name, and belonging to the given <code>Histogram</code>.
	 * Names will be adjusted to 12 characters if they aren't that length already.
	 *
	 * @param name name of the gate which will be put on the chooser in the display
	 * @param hist the <code>Histogram</code> to which the gate will belong.
	 */
	public Gate(String name, Histogram hist) {
		final StringUtilities stringUtil=StringUtilities.instance();
		histName = hist.getUniqueFullName();
		//work out name, and give error message if name is to be truncated
		String name2 = name = stringUtil.makeLength(name, NAME_LENGTH);
		name = name2;
		//add to histogramTable here
		int prime = 1;
		String addition;
		while (TABLE.containsKey(name)) {
			addition = "[" + prime + "]";
			name =
				stringUtil.makeLength(
					name,
					NAME_LENGTH - addition.length())
					+ addition;
			prime++;
		}
		this.name = name;
		dimensions = hist.getDimensionality();
		sizeX = hist.getSizeX();
		sizeY = hist.getSizeY();
		unsetLimits();
		addToCollections();
		hist.addGate(this);
	}
	
	private final void addToCollections(){
		TABLE.put(name, this);
		LIST.add(this);
		DIM_LIST[dimensions-1].add(this);
	}

	/**
	 * Sets the list of gates, used for remote loading of histograms.
	 *
	 * @param inGateList must contain all histogram objects
	 */
	public static void setGateList(List inGateList) {
		clearList();
		/* loop for all histograms */
		for (final Iterator allGates = inGateList.iterator(); allGates.hasNext();) {
			final Gate gate = (Gate) allGates.next();
			final String name = gate.getName();
			TABLE.put(name, gate);
			LIST.add(gate);
		}
	}

	/**
	 * Gets the list of all <code>Gate</code> objects.
	 *
	 * @return ordered list of all <code>Gate</code> objects
	 */
	public static List getGateList() {
		return Collections.unmodifiableList(LIST);
	}

	/**
	 * Returns an unmodifiable list of gates with the given dimensionality.
	 * 
	 * @param dimension of gates
	 * @return list of gates with the given dimensionality
	 */
	public static List getGateList(int dimension) {
		return Collections.unmodifiableList(DIM_LIST[dimension-1]);
	}
	
	private static final boolean [][] NO_AREA=new boolean[0][0];

	/**
	 * Clears the list of gates.
	 */
	public static void clearList() {
		for (final Iterator it=LIST.iterator(); it.hasNext();){
			final Gate gate=(Gate)it.next();
			gate.insideGate=NO_AREA;
			gate.bananaGate.reset();
		}
		LIST.clear();
		DIM_LIST[0].clear();
		DIM_LIST[1].clear();
		TABLE.clear();
		/* run garbage collector, memory should be freed */
		System.gc();
	}

	/**
	 * Returns the <code>Gate</code> with the given name.
	 *
	 * @param	name name of the desired gate
	 * @return	<code>Gate</code> with the given name, null if non-existent
	 */
	public static Gate getGate(String name) {
		Gate rval=null;
		if (name != null && TABLE.containsKey(name)){
			rval = (Gate)TABLE.get(name);
		}
		return rval;
	}

	/**
	 * Returns the name of the <code>Gate</code>.
	 *
	 * @return the name of the <code>Gate</code>
	 */
	public String getName() {
		return name;
	}

	public int getCount() {
		return (int)getArea();
	}
	
	public int getElementType() {
		return DataElement.ELEMENT_TYPE_GATE;
	}
	/**
	 * Returns the dimensionality  of this <code>Gate</code>, which
	 * is the same as its associated <code>Histogram</code>.
	 *
	 * @return either 1 or 2
	 * @see Histogram#getDimensionality()
	 */
	public int getDimensionality() {
		return dimensions;
	}

	/**
	 * Returns the <code>Histogram</code> this <code>Gate</code> belongs to.
	 *
	 * @return the <code>Histogram</code> this <code>Gate</code> belongs to
	 */
	public Histogram getHistogram() {
		return Histogram.getHistogram(histName);
	}

	/**
	 * Returns the limits for a <code>Gate</code> of type <code>ONE_DIMENSION</code>
	 *
	 * @return a 2-element array with the hi- and lo- limits of the gate
	 * @throws UnsupportedOperationException if called for 1d gate
	 */
	public int[] getLimits1d() {
		final int [] bounds = new int[2];
		if (dimensions != 1){
			throw new UnsupportedOperationException(
				"getLimits1d(): can only be called for 1D gates.");
		}
		bounds[0] = lowerLimit;
		bounds[1] = upperLimit;
		return bounds;
	}

	/**
	 * Returns the limits for the <code>Gate</code> of type <code>TWO_DIMENSION</code>, actually a
	 * boolean array to quickly mask when sorting.
	 *
	 * @return	a 2-d array of <code>boolean</code>'s which are true for channels inside the gate
	 * @throws UnsupportedOperationException thrown if called for 1d gate
	 */
	public boolean[][] getLimits2d() {
		if (dimensions != 2) {
			throw new UnsupportedOperationException (
				"getLimits2d(): can only be called for 2D gates.");
		}
		final int lenX=insideGate.length;
		final int lenY=insideGate[0].length;
		final boolean [][] rval = new boolean[lenX][lenY];
		for (int i=0; i<lenX; i++){
		    System.arraycopy(insideGate[i],0,rval[i],0,lenY);
		}
		return rval;
	}

	/**
	 * Returns the <code>Polygon</code> object corresponding to a 2-d gate.
	 *
	 * @return the gate
	 * @throws UnsupportedOperationException if called for 1d gate
	 */
	public Polygon getBananaGate() {
		if (dimensions != 2){
			throw new UnsupportedOperationException (
				"getBananaGate can only be called for 2D gates.");
		}
		return bananaGate;
	}

	/**
	 * Sets the limts for a 1-d gate.
	 *
	 * @param lower lower channel limit
	 * @param upper upper channel limit
	 * @throws UnsupportedOperationException if called for 2d gate
	 */
	public void setLimits(int lower, int upper) {
		if (dimensions != 1){
			throw new UnsupportedOperationException(
				"setLimits(int,int): can only be called for 1D gates");
		}
		if (lower <= upper) {
			lowerLimit = lower;
			upperLimit = upper;
		} else {
			lowerLimit = upper;
			upperLimit = lower;
		}
		isSet = true;
	}

	/**
	 * Sets the limits for a 2-d gate, given a <code>Polygon</code>.
	 *
	 * @param gatePoly used to define the gate
	 * @throws UnsupportedOperationException thrown if called for 1d gate
	 */
	public void setLimits(Polygon gatePoly) {
		bananaGate.reset();
		for (int i=0; i<gatePoly.npoints; i++){
			bananaGate.addPoint(gatePoly.xpoints[i],gatePoly.ypoints[i]);
		}
		if (dimensions != 2) {
			throw new UnsupportedOperationException(
				"setLimits(Polygon): can only be called for 2D gates.");
		}
		//set points true if in plolygon
		for (int i = 0; i < sizeX; i++) {
			for (int j = 0; j < sizeY; j++) {
				if (gatePoly.contains(i, j)) {
					insideGate[i][j] = true;
				} else { //to make sure we zero all (old) points
					insideGate[i][j] = false;
				}
			}
		}
		isSet = true;
	}

	/**
	 * "Removes" the gate's limits, so that it will 
	 * return false for all calls to <code>inGate()</code>.
	 */
	public final void unsetLimits() {
		isSet = false;
		if (dimensions == 1) {
			lowerLimit = 0;
			upperLimit = 0;
		} else if (dimensions == 2) {
			insideGate = new boolean[sizeX][sizeY];
			bananaGate.reset();
		}
	}

	/**
	 * Returns whether a gate has been set.
	 *
	 * @return <code>true</code> if gate is currently set, <code>false</code> if not.
	 */
	public boolean isDefined() {
		return isSet;
	}

	/**
	 * Returns whether channel is inside 1-d gate.  If the channel is equal to 
	 * wither limiting channel or between them, it is inside the gate.
	 *
	 * @param channel channel of interest to check
	 * @return <code>true</code> if channel is inside gate, <code>false</code> if not.
	 * @throws UnsupportedOperationException thrown if called for 2d gate
	 */
	public boolean inGate(int channel) {
		if (dimensions != 1){
			throw new UnsupportedOperationException(
				"inGate(int): can only be called for 1D gates.");
		}
		return (isSet && (channel >= lowerLimit) && (channel <= upperLimit));
	}

	/**
	 * Returns whether channel is inside 2-d gate.
	 *
	 * @param channelX x-channel of interest
	 * @param channelY y-channel of interest
	 * @return <code>true</code> if channel is inside gate, <code>false</code> if not.
	 * @throws UnsupportedOperationException thrown if called for 1d gate
	 */
	public boolean inGate(int channelX, int channelY) {
		boolean inside = false; //default value if not all conditions are met
		if (dimensions != 2) {
			throw new UnsupportedOperationException(
					"inGate(int,int): can only be called for 2D gates");
		}
		if (isSet) {
			if ((channelX >= 0) && (channelX < sizeX) && (channelY >= 0)
					&& (channelY < sizeY)) {
				inside = insideGate[channelX][channelY];
			}
		}
		return inside;
	}

	/**
	 * Gets the number of counts in the gate for the histogram which the gate
	 * belongs to.
	 * 
	 * @return sum of counts in gate
	 */
	public double getArea() {
		return (dimensions == 1) ? getArea1d() : getArea2d();
	}
	
	private double getArea1d(){
		final Histogram histogram=Histogram.getHistogram(histName);
		final Histogram.Type htype=histogram.getType();
		double rval=0.0;
		if (htype == Histogram.Type.ONE_D_DOUBLE) {
			final double[] counts = (double[]) histogram.getCounts();
			for (int i = lowerLimit; i <= upperLimit; i++) {
				rval += counts[i];
			}
		} else { //1d int
			final int[] counts = (int[]) histogram.getCounts();
			for (int i = lowerLimit; i <= upperLimit; i++) {
				rval += counts[i];
			}
		}
		return rval;
	}
	
	private double getArea2d(){
		final Histogram histogram=Histogram.getHistogram(histName);
		final Histogram.Type htype=histogram.getType();
		double rval=0.0;
		if (htype == Histogram.Type.TWO_DIM_INT) {
			final int[][] counts2d = (int[][]) histogram.getCounts();
			for (int i = 0; i < sizeX; i++) {
				for (int j = 0; j < sizeY; j++) {
					if (insideGate[i][j]) {
						rval += counts2d[i][j];
					}
				}
			}
		} else { //2d double
			final double[][] counts2d = (double[][]) histogram.getCounts();
			for (int i = 0; i < sizeX; i++) {
				for (int j = 0; j < sizeY; j++) {
					if (insideGate[i][j]) {
						rval += counts2d[i][j];
					}
				}
			}
		}
		return rval;
	}

	/**
	 * Gets the centriod of the counts in the gate for the
	 * histogram which the gate belongs to.
	 * This method only works for 1d gates, for 2d gates it returns 0
	 *
	 * @return	centroid the centroid of the counts in gate
	 */
	public double getCentroid() {
		double centroid = 0.0;
		double area = 0.0;
		if (dimensions == 1) {
			final Histogram histogram=Histogram.getHistogram(histName);
			if (histogram.getType() == Histogram.Type.ONE_DIM_INT) {
				final int[] counts = (int[]) histogram.getCounts();
				//sum up counts and weight
				for (int i = lowerLimit; i <= upperLimit; i++) {
					area += counts[i];
					centroid += i * counts[i];
				}
				// calculate centroid
				if (area > 0) { // must have more than zero counts
					centroid = centroid / area;
				} else {
					centroid = 0.0;
				}
			} else {//1d double
				final double [] counts = (double []) histogram.getCounts();
				//sum up counts and weight
				for (int i = lowerLimit; i <= upperLimit; i++) {
					area += counts[i];
					centroid += i * counts[i];
				}
				// calculate centroid
				if (area > 0) { // must have more than zero counts
					centroid = centroid / area;
				} else {
					centroid = 0.0;
				}
			}
		} else if (dimensions == 2) {
			centroid = 0.0;
		}
		return centroid;
	}

	/**
	 * Gets number of individual points in gate.  For 1-d, this is always 2. For 2-d banana gates
	 * the number is variable. This is part of the class for <code>HistogramIO</code> to be able to write HDF files
	 * more efficiently.
	 *
	 * @return number of points to define gate
	 */
	public int size() {
		int temp = 0;
		if (isSet) {
			if (dimensions == 1) {
				temp = 2;
			} else {
				temp = bananaGate.npoints;
			}
		} else {
			temp = 0;
		}
		return temp;
	}

	/**
	 * Returns the number of the histogram this gate belongs to.
	 *
	 * @return	the number of the associated <code>Histogram</code>
	 */
	public int getHistogramNumber() {
		return Histogram.getHistogram(histName).getNumber();
	}

	/**
	 * Gives the name of this gate.
	 * 
	 * @return its name
	 */
	public String toString() {
		return name;
	}
}
