package jam.data;
import java.awt.Point;
import java.awt.Polygon;
import java.util.*;
import jam.util.*;
import java.io.Serializable;

/**
 * A gate, used for data sorting, belongs to a histogram which determines what type of
 * gate it is.  There are two types of gates, <code>ONE_DIMENDION</code> and <code>TWO_DIMENSION</code>,
 *
 * @author Ken Swartz
 * @version 0.5
 * @since JDK 1.1
 */
public class Gate implements Serializable {

	/**
	 * Type of gate which belongs to a <code>Histogram</code> of dimensionality 1.
	 *
	 * @see Histogram#ONE_DIM_INT
	 * @see Histogram#ONE_DIM_DOUBLE
	 */
	public static final int ONE_DIMENSION = 1;

	/**
	 * Type of gate which belongs to a <code>Histogram</code> of dimensionality 2.
	 *
	 * @see Histogram#TWO_DIM_INT
	 * @see Histogram#TWO_DIM_DOUBLE
	 */
	public static final int TWO_DIMENSION = 2;
	/**
	 * Maximum number of characters in the histogram name.
	 */
	public static final int NAME_LENGTH = 16;

	//static strutures to hold all gates.
	static Hashtable gateTable = new Hashtable(37); //should be an prime number
	static List gateList = new Vector(37);

	private static List gateSetPoints;

	protected String name; //name of gate
	protected Histogram histogram; //histogram gate belongs to
	protected int number;
	protected int type; //type of gate ONE_DIMESION or TWO_DIMENSION

	private boolean isSet;

	private int sizeX; //size of gate for 1d and x of 2d
	private int sizeY; //size used for 2d histograms only

	//values for 1 d gate
	protected int lowerLimit; //lower limit for 1d gate
	protected int upperLimit; //upper limit for 1d gate

	//values for a 2d gate
	protected Polygon bananaGate; //ploygon of 2d gate
	protected boolean insideGate[][]; // true if point inside 2d gate

	/**
	 * Constructs a new gate with the given name, and belonging to the given <code>Histogram</code>.
	 * Names will be adjusted to 12 characters if they aren't that length already.
	 *
	 * @param name name of the gate which will be put on the chooser in the display
	 * @param hist the <code>Histogram</code> to which the gate will belong.
	 */
	public Gate(String name, Histogram hist) {
		StringUtilities su=StringUtilities.instance();
		this.histogram = hist;
		//work out name, and give error message if name is to be truncated
		String name2 = name = su.makeLength(name, NAME_LENGTH);
		if (name.length() > NAME_LENGTH) {
			System.err.println(
				"Gate name '"
					+ name
					+ "' too long, truncated to '"
					+ name2
					+ "'.");
		}
		name = name2;
		//add to histogramTable here
		int prime = 1;
		String addition;
		while (gateTable.containsKey(name)) {
			addition = "[" + prime + "]";
			name =
				su.makeLength(
					name,
					NAME_LENGTH - addition.length())
					+ addition;
			prime++;
		}
		this.name = name;
		_unsetLimits();
		gateTable.put(name, this);
		gateList.add(this);
		try { //register ourselves with the histogram we belong to
			histogram.addGate(this);
		} catch (DataException de) {
			//should never be here
			System.out.println("Error: gate constuctor wrong gate type ");
		}
	}

	/**
	 * Sets the list of gates, used for remote loading of histograms.
	 *
	 * @param inGateList must contain all histogram objects
	 */
	public static void setGateList(List inGateList) {
		/* clear current lists */
		gateTable.clear();
		gateList.clear();
		/* loop for all histograms */
		for (Iterator allGates = inGateList.iterator(); allGates.hasNext();) {
			Gate gate = (Gate) allGates.next();
			String name = gate.getName();
			gateTable.put(name, gate);
			gateList.add(gate);
		}
	}

	/**
	 * Gets the list of all <code>Gate</code> objects.
	 *
	 * @return ordered list of all <code>Gate</code> objects
	 */
	public static List getGateList() {
		return gateList;
	}

	public static List getGateList(int type) {
		Vector out = new Vector(gateList);
		for (int i = 0; i < out.size(); i++) {
			if (((Gate) (out.elementAt(i))).getType() != type) {
				out.removeElementAt(i);
				i--;
				//so that i++ in next loop iteration will actually access the next element
			}
		}
		return out;
	}

	/**
	 * Clears the list of gates.
	 */
	public static void clearList() {
		gateList.clear();
		gateTable.clear();
		//run garbage collector, memory should be freed
		System.gc();
	}

	/**
	 * Returns the <code>Gate</code> with the given name.
	 *
	 * @param	name name of the desired gate
	 * @return	<code>Gate</code> with the given name, null if non-existent
	 */
	public static Gate getGate(String name) {
		return (Gate) gateTable.get(name);
	}

	/**
	 * update methode used in setting gate
	 */
	public static void update(Point p) {
		gateSetPoints.add(p);
	}

	/**
	 * update methode used in setting gate
	 */
	public static List getSetPoints() {
		return gateSetPoints;
	}

	/**
	 * Returns the name of the <code>Gate</code>.
	 *
	 * @return the name of the <code>Gate</code>
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the type of <code>Gate</code>.
	 *
	 * @return either <code>ONE_DIMENSION</code> or <code>TWO_DIMENSION</code>
	 * @see #ONE_DIMENSION
	 * @see #TWO_DIMENSION
	 */
	public int getType() {
		return type;
	}

	/**
	 * Returns the <code>Histogram</code> this <code>Gate</code> belongs to.
	 *
	 * @return the <code>Histogram</code> this <code>Gate</code> belongs to
	 */
	public Histogram getHistogram() {
		return histogram;
	}

	/**
	 * Returns the limits for a <code>Gate</code> of type <code>ONE_DIMENSION</code>
	 *
	 * @return a 2-element array with the hi- and lo- limits of the gate
	 * @exception DataException thrown if called for wrong type of gate
	 */
	public int[] getLimits1d() throws DataException {

		int[] bounds = new int[2];
		if (type != ONE_DIMENSION)
			throw new DataException(
				"getLimits1d(): can only be called for gates"
					+ " of type ONE_DIMENSION");
		bounds[0] = lowerLimit;
		bounds[1] = upperLimit;
		return bounds;
	}

	/**
	 * Returns the limits for the <code>Gate</code> of type <code>TWO_DIMENSION</code>, actually a
	 * boolean array to quickly mask when sorting.
	 *
	 * @return	a 2-d array of <code>boolean</code>'s which are true for channels inside the gate
	 * @exception DataException thrown if called for wrong type of gate
	 */
	public boolean[][] getLimits2d() throws DataException {
		if (type != TWO_DIMENSION)
			throw new DataException(
				"getLimits2d(): can only be called for gates"
					+ " of type TWO_DIMENSION");
		return insideGate;
	}

	/**
	 * Returns the <code>Polygon</code> object corresponding to a 2-d gate.
	 *
	 * @return the gate
	 * @exception DataException thrown if called for wrong type of gate
	 */
	public Polygon getBananaGate() throws DataException {
		if (type != TWO_DIMENSION)
			throw new DataException(
				"getBananaGate can only be called for gates"
					+ " of type TWO_DIMENSION");
		return bananaGate;
	}

	/**
	 * Sets the limts for a 1-d gate.
	 *
	 * @param ll lower channel limit
	 * @param ul upper channel limit
	 * @exception DataException thrown if called for wrong type of gate
	 */
	public void setLimits(int ll, int ul) throws DataException {
		if (type != ONE_DIMENSION)
			throw new DataException(
				"setLimits(int,int): can only be called for gates"
					+ " of type ONE_DIMENSION");
		if (ll <= ul) {
			lowerLimit = ll;
			upperLimit = ul;
		} else {
			lowerLimit = ul;
			upperLimit = ll;
		}
		isSet = true;
	}

	/**
	 * Sets the limits for a 2-d gate, given a set of coodinates for points.
	 *
	 * @param gatePoints a <code>Vector</code> of <code>Point</code> objects
	 * @exception DataException thrown if called for wrong type of gate
	 */
	public void setLimits(List gatePoints) throws DataException {
		int pointX;
		int pointY;
		Polygon gatePoly = new Polygon();
		;

		if (type != TWO_DIMENSION)
			throw new DataException(
				"setLimits(Vector): can only be called for gates"
					+ " of type TWO_DIMENSION");
		//make a plolygon from points
		for (int i = 0; i < gatePoints.size(); i++) {
			pointX = ((Point) gatePoints.get(i)).x;
			pointY = ((Point) gatePoints.get(i)).y;
			gatePoly.addPoint(pointX, pointY);
		}
		setLimits(gatePoly);
	}

	/**
	 * Sets the limits for a 2-d gate, given a <code>Polygon</code>.
	 *
	 * @exception DataException thrown if called for wrong type of gate
	 */
	public void setLimits(Polygon gatePoly) throws DataException {

		this.bananaGate = gatePoly;

		if (type != TWO_DIMENSION)
			throw new DataException(
				"setLimits(Polygon): can only be called for gates"
					+ " of type TWO_DIMENSION");
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
	private void _unsetLimits() {
		isSet = false;
		if (histogram.getDimensionality() == 1) {
			type = ONE_DIMENSION;
			sizeX = histogram.getSizeX();
			sizeY = 0;
			lowerLimit = 0;
			upperLimit = 0;
		} else if (histogram.getDimensionality() == 2) {
			type = TWO_DIMENSION;
			sizeX = histogram.getSizeX();
			sizeY = histogram.getSizeY();
			insideGate = new boolean[sizeX][sizeY];
			bananaGate = null;
		}
	}

	/**
	 * "Removes" the gate's limits, so that it will 
	 * return false for all calls to <code>inGate()</code>.
	 */
	public void unsetLimits() {
		/* delegate to private method to allow constructor
		 * to avoid a call to an overridable method
		 */
		_unsetLimits();
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
	 * @exception DataException thrown if called for wrong type of gate
	 */
	public boolean inGate(int channel) throws DataException {
		if (type != ONE_DIMENSION)
			throw new DataException(
				"inGate(int): can only be called for gates"
					+ " of type ONE_DIMENSION");
		return (isSet && (channel >= lowerLimit) && (channel <= upperLimit));
	}

	/**
	 * Returns whether channel is inside 2-d gate.
	 *
	 * @param channelX x-channel of interest
	 * @param channelY y-channel of interest
	 * @return <code>true</code> if channel is inside gate, <code>false</code> if not.
	 * @exception DataException thrown if called for wrong type of gate
	 */
	public boolean inGate(int channelX, int channelY) throws DataException {
		boolean inside = false; //default value if not all conditions are met
		if (type != TWO_DIMENSION)
			throw new DataException(
				"inGate(int,int): can only be called for gates"
					+ " of type TWO_DIMENSION");
		if (isSet) {
			if ((channelX >= 0)
				&& (channelX < sizeX)
				&& (channelY >= 0)
				&& (channelY < sizeY)) {
				inside = insideGate[channelX][channelY];
			}
		}
		return inside;
	}

	/**
	 * Gets the number of counts in the gate for the histogram which the gate belongs to.
	 *
	 * @return	sum of counts in gate
	 */
	public double getArea() {
		double sum = 0.0;
		if (type == ONE_DIMENSION) {
			if (histogram.getType() == Histogram.ONE_DIM_DOUBLE) {
				final double[] counts = (double[]) histogram.getCounts();
				for (int i = lowerLimit; i <= upperLimit; i++) {
					sum += counts[i];
				}
			} else { //1d int
				final int[] counts = (int[]) histogram.getCounts();
				for (int i = lowerLimit; i <= upperLimit; i++) {
					sum += counts[i];
				}
			}
		} else { //2d
			if (histogram.getType() == Histogram.TWO_DIM_INT) {
				final int[][] counts2d = (int[][]) histogram.getCounts();
				for (int i = 0; i < sizeX; i++) {
					for (int j = 0; j < sizeY; j++) {
						if (insideGate[i][j]) {
							sum += counts2d[i][j];
						}
					}
				}
			} else { //2d double
				final double[][] counts2d = (double[][]) histogram.getCounts();
				for (int i = 0; i < sizeX; i++) {
					for (int j = 0; j < sizeY; j++) {
						if (insideGate[i][j]) {
							sum += counts2d[i][j];
						}
					}
				}
			}
		}
		return sum;
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
		if (type == ONE_DIMENSION) {
			if (histogram.getType() == Histogram.ONE_DIM_INT) {
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
		} else if (type == TWO_DIMENSION) {
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
			if (type == ONE_DIMENSION) {
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
	public int getNumber() {
		return histogram.getNumber();
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
