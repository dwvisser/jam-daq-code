package jam.data;

import jam.data.func.CalibrationFunction;
import jam.util.StringUtilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Class containing spectra and the routines to perform operations on them.
 * <p>
 * Each histogram has:
 * </p>
 * <ul>
 * <li>name
 * <li>type
 * <li>size in channels, x and y (sizeY=0 for 1D)
 * <li>title
 * <li>axis labels, x and y
 * <li>data array 1 or 2 dimension
 * <li>gates
 * </ul>
 * <p>
 * Modified 2/11/99 Dale Visser to have an error array too. By default, the
 * class will assume Poisson error bars and return square root of counts. For
 * <code>Histogram</code>'s produced by adding, subtracting, or otherwise
 * manipulating other histograms, though, an appropriate error array should be
 * calculated and stored by invoking the <code>setErrors()</code> method.
 * 
 * @author Ken Swartz
 * @author Dale Visser
 * @version 0.5, 1.0
 * @see #setErrors(double[])
 * @since JDK 1.1
 */
public abstract class Histogram {

	/**
	 * Encapsulates the 4 different types a histogram may have.
	 * 
	 * @author <a href="mailto:dale@visser.name">Dale W Visser </a>
	 */
	public static class Type {
		/**
		 * Value of histogram type for one dimensional <code>int</code>
		 * histograms.
		 * 
		 * @see #getType()
		 */
		public static final Type ONE_DIM_INT = new Type(0);

		/**
		 * Value of histogram type for two dimensional <code>int</code>
		 * histograms.
		 * 
		 * @see #getType()
		 */
		public static final Type TWO_DIM_INT = new Type(1);

		/**
		 * Value of histogram type for one dimensional <code>double</code>
		 * histograms.
		 * 
		 * @see #getType()
		 */
		public static final Type ONE_D_DOUBLE = new Type(2);

		/**
		 * Value of histogram type for two dimensional <code>double</code>
		 * histograms.
		 * 
		 * @see #getType()
		 */
		public static final Type TWO_D_DOUBLE = new Type(3);

		private transient final int type;

		private final static int[] DIM = { 1, 2, 1, 2 };

		private final static boolean[] INT = { true, true, false, false };

		private final static String[] STRING = { "1D int", "2D int",
				"1D double", "2D double" };

		private Type(int num) {
			type = num;
		}

		public int getDimensionality() {
			return DIM[type];
		}

		public boolean isInteger() {
			return INT[type];
		}

		public String toString() {
			return STRING[type];
		}

		/**
		 * Gives the counts array type of the given object.
		 * 
		 * @param array
		 *            a 1-d or 2-d int or double array
		 * @return which type the array corresponds to
		 */
		static Type getArrayType(Object array) {
			final Type rval;
			final String error = "You may pass int or double arrays of up to two dimensions as histogram counts.";
			final Class type = array.getClass();
			if (!type.isArray()) {
				throw new IllegalArgumentException(error);
			}
			final Class componentA = type.getComponentType();
			if (componentA.equals(int.class)) {
				rval = Type.ONE_DIM_INT;
			} else if (componentA.equals(double.class)) {
				rval = Type.ONE_D_DOUBLE;
			} else {
				/* Two-D, componentA assumed to be array. */
				final Class componentB = componentA.getComponentType();
				if (componentB.equals(int.class)) {
					rval = Type.TWO_DIM_INT;
				} else if (componentB.equals(double.class)) {
					rval = Type.TWO_D_DOUBLE;
				} else {
					throw new IllegalArgumentException(error);
				}
			}
			return rval;
		}

		public Object getSampleArray(int sizeX, int sizeY) {
			final Object rval;
			if (sizeY == 0) {
				rval = isInteger() ? (Object) new int[sizeX]
						: (Object) new double[sizeX];
			} else {
				rval = isInteger() ? (Object) new int[sizeX][sizeY]
						: (Object) new double[sizeX][sizeY];
			}
			return rval;
		}
	}

	/**
	 * default axis labels
	 */
	static final String X_LABEL_1D = "Channels";

	static final String Y_LABEL_1D = "Counts";

	static final String X_LABEL_2D = "Channels";

	static final String Y_LABEL_2D = "Channels";

	/**
	 * Maximum number of characters in the histogram name.
	 */
	public static final int NAME_LENGTH = 20;

	private final static Map NAME_MAP = new TreeMap();

	private final static SortedMap NUMBER_MAP = new TreeMap();

	/* histogramList is ordered by the creation of the histograms */
	private final static List LIST = new ArrayList();

	private final static List[] DIM_LIST = new List[2];
	static {
		DIM_LIST[0] = new ArrayList();
		DIM_LIST[1] = new ArrayList();
	}

	/**
	 * gates that belong to this histogram
	 */
	private transient final List gates = new ArrayList();

	protected transient CalibrationFunction calibFunc;

	private transient String title; // title of histogram

	private transient String name; //abreviation to refer to it by

	private int number; //histogram number

	private transient Type type; //one or two dimension

	protected transient final int sizeX; //size of histogram, for 1d size for

	// 2d

	// x size

	protected transient final int sizeY; //size used for 2d histograms y size

	private String labelX; //x axis label

	private String labelY; //y axis label

	private transient boolean labelXset = false;

	private transient boolean labelYset = false;

	/**
	 * Master constructor invoked by all other constructors.
	 * 
	 * @param nameIn
	 *            unique name of histogram, should be limited to
	 *            <code>NAME_LENGTH</code> characters, used in both .jhf and
	 *            .hdf files as the unique identifier for reloading the
	 *            histogram
	 * @param type
	 *            type and dimensionality of data
	 * @param sizeX
	 *            number of channels in x-axis
	 * @param sizeY
	 *            number of channels in y-axis
	 * @param title
	 *            lengthier title of histogram, displayed on plot
	 * @see #NAME_LENGTH
	 * @see Type
	 * @throws IllegalArgumentException
	 *             if an unknown histogram type is given
	 */
	Histogram(String nameIn, Type type, int sizeX, int sizeY, String title) {
		String addition;
		int prime;
		final StringUtilities stringUtil = StringUtilities.instance();
		this.type = type;
		this.title = title;
		this.name = nameIn;
		//give error if name is to be truncated
		String name2 = name = stringUtil.makeLength(name, NAME_LENGTH);
		name = name2;
		//find a name that does not conflict with exiting names
		prime = 1;
		while (NAME_MAP.containsKey(name)) {
			addition = "[" + prime + "]";
			name = stringUtil.makeLength(name, NAME_LENGTH - addition.length())
					+ addition;
			prime++;
		}
		gates.clear();
		assignNewNumber();
		//allow memory for gates and define sizes
		final boolean oneD = type.getDimensionality() == 1;
		if (labelX == null) {
			labelX = oneD ? X_LABEL_1D : X_LABEL_2D;
		}
		if (labelY == null) {
			labelY = oneD ? Y_LABEL_1D : Y_LABEL_2D;
		}
		this.sizeX = sizeX;
		this.sizeY = oneD ? 0 : sizeY;
		/* add to static lists */
		NAME_MAP.put(name, this);
		LIST.add(this);
		DIM_LIST[type.getDimensionality() - 1].add(this);
	}

	/**
	 * Contructor with no number given, but axis labels are given.
	 * 
	 * @param nameIn
	 *            unique name of histogram, should be limited to
	 *            <code>NAME_LENGTH</code> characters, used in both .jhf and
	 *            .hdf files as the unique identifier for reloading the
	 *            histogram
	 * @param type
	 *            dimensionality of histogram, 1 or 2
	 * @param size
	 *            number of channels, all 2d histograms have square dimensions
	 * @param title
	 *            lengthier title of histogram, displayed on plot
	 * @throws IllegalArgumentException
	 *             if an unknown histogram type is given
	 */
	Histogram(String nameIn, Type type, int size, String title) {
		this(nameIn, type, size, size, title);
	}

	/**
	 * Contructor with no number given, but axis labels are given.
	 * 
	 * @param name
	 *            unique name of histogram, should be limited to
	 *            <code>NAME_LENGTH</code> characters, used in both .jhf and
	 *            .hdf files as the unique identifier for reloading the
	 *            histogram
	 * @param type
	 *            dimensionality of histogram, 1 or 2
	 * @param sizeX
	 *            number of channels in x-axis
	 * @param sizeY
	 *            number of channels in y-axis
	 * @param title
	 *            lengthier title of histogram, displayed on plot
	 * @param axisLabelX
	 *            label displayed for x-axis on plot
	 * @param axisLabelY
	 *            label displayed for y-axis on plot
	 * @throws IllegalArgumentException
	 *             if an unknown histogram type is given
	 */
	Histogram(String name, Type type, int sizeX, int sizeY, String title,
			String axisLabelX, String axisLabelY) {
		this(name, type, sizeX, sizeY, title);
		setLabelX(axisLabelX);
		setLabelY(axisLabelY);
	}

	/**
	 * Contructor with no number given, but axis labels are given.
	 * 
	 * @param name
	 *            unique name of histogram, should be limited to
	 *            <code>NAME_LENGTH</code> characters, used in both .jhf and
	 *            .hdf files as the unique identifier for reloading the
	 *            histogram
	 * @param type
	 *            dimensionality of histogram, 1 or 2
	 * @param size
	 *            number of channels, all 2d histograms have square dimensions
	 * @param title
	 *            lengthier title of histogram, displayed on plot
	 * @param axisLabelX
	 *            label displayed for x-axis on plot
	 * @param axisLabelY
	 *            label displayed for y-axis on plot
	 * @throws IllegalArgumentException
	 *             if an unknown histogram type is given
	 */
	Histogram(String name, Type type, int size, String title,
			String axisLabelX, String axisLabelY) {
		this(name, type, size, size, title);
		setLabelX(axisLabelX);
		setLabelY(axisLabelY);
	}

	static public Histogram createHistogram(Object array, String name,
			String title, String labelX, String labelY) {
		final Histogram rval;
		final Type hType = Type.getArrayType(array);
		if (hType == Type.ONE_DIM_INT) {
			rval = new HistInt1D(name, title, labelX, labelY, (int[]) array);
		} else if (hType == Type.ONE_D_DOUBLE) {
			rval = new HistDouble1D(name, title, labelX, labelY,
					(double[]) array);
		} else if (hType == Type.TWO_DIM_INT) {
			rval = new HistInt2D(name, title, labelX, labelY, (int[][]) array);
		} else {//TWO_D_DOUBLE
			rval = new HistDouble2D(name, title, labelX, labelY,
					(double[][]) array);
		}
		return rval;
	}

	static public Histogram createHistogram(Object array, String name,
			String title) {
		return createHistogram(array, name, title, null, null);
	}
	
	static public Histogram createHistogram(Object array, String name) {
		return createHistogram(array, name, name, null, null);
	}

	//end of constructors

	/**
	 * Sets the list of histograms, used for remote loading of histograms.
	 * 
	 * @param inHistList
	 *            must contain all histogram objects
	 */
	public static void setHistogramList(List inHistList) {
		clearList();
		final Iterator iter = inHistList.iterator();
		while (iter.hasNext()) { //loop for all histograms
			Histogram hist = (Histogram) iter.next();
			NAME_MAP.put(hist.getName(), hist);
			LIST.add(hist);
			NUMBER_MAP.put(new Integer(hist.getNumber()), hist);
		}
	}

	/**
	 * Returns the list of all histograms.
	 * 
	 * @return all histograms
	 */
	public static List getHistogramList() {
		return Collections.unmodifiableList(LIST);
	}

	public static List getHistogramList(int dim) {
		if (dim < 1 || dim > 2) {
			throw new IllegalArgumentException(
					"Expect 1 or 2, the possible numbers of dimensions.");
		}
		return Collections.unmodifiableList(DIM_LIST[dim - 1]);
	}

	/**
	 * @return list of all histograms sorted by number
	 */
	public static Collection getListSortedByNumber() {
		return Collections.unmodifiableCollection(NUMBER_MAP.values());
	}

	/**
	 * @return list of all histograms sorted by name
	 */
	public static Collection getListSortedByName() {
		return Collections.unmodifiableCollection(NAME_MAP.values());
	}

	/**
	 * Clears the list of histograms.
	 */
	public static void clearList() {
		for (Iterator it = LIST.iterator(); it.hasNext();) {
			final Histogram his = (Histogram) it.next();
			his.clearInfo();
		}
		LIST.clear();
		NAME_MAP.clear();
		NUMBER_MAP.clear();
		DIM_LIST[0].clear();
		DIM_LIST[1].clear();
		System.gc();
	}

	public static void deleteHistogram(String histName) {
		if (NAME_MAP.containsKey(histName)) {
			final Histogram histogram = getHistogram(histName);
			if (histogram != null) {
				histogram.clearInfo();
				LIST.remove(histogram);
				NAME_MAP.remove(histogram.getName());
				NUMBER_MAP.remove(new Integer(histogram.getNumber()));
				DIM_LIST[0].remove(histogram);
				DIM_LIST[1].remove(histogram);
			}
		}
		System.gc();
	}

	private void clearInfo() {
		gates.clear();
		calibFunc = null;
		labelX = null;
		labelY = null;
		title = null;
		clearCounts();
	}

	abstract void clearCounts();

	/**
	 * Returns the histogram with the given name, null if name doesn't exist.
	 * 
	 * @param name
	 *            name of histogram to retrieve
	 */
	public static Histogram getHistogram(String name) {
		Histogram rval = null;//default return value
		if (name != null) {
			/* get() will return null if key not in table */
			rval = (Histogram) NAME_MAP.get(name);
		}
		return rval;
	}

	/**
	 * Get the histogram with the given number.
	 * 
	 * @param num
	 *            of the histogram
	 * @return the histogram, if it exists, null otherwise
	 */
	public static Histogram getHistogram(int num) {
		return (Histogram) NUMBER_MAP.get(new Integer(num));
	}

	/* instantized methods */

	/**
	 * Returns the histogram title.
	 * 
	 * @return the title of this histogram
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Returns the histogram name.
	 * 
	 * @return the name of this histogram
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the number of the histogram, mostly used for export to ORNL
	 * files. Histograms should always be assigned unique numbers.
	 * 
	 * @return the number of this histogram
	 */
	public final int getNumber() {
		return number;
	}

	/**
	 * Returns the type of this histogram type. This can be:
	 * <ul>
	 * <code><li>ONE_DIM_INT</li><li>TWO_DIM_INT</li><li>ONE_DIM_DOUBLE</li>
	 *  <li>TWO_DIM_DOUBLE</li></code>
	 * </ul>
	 * 
	 * @return the type
	 * @see Type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Get size of x-dimension, or the only dimension for 1-d histograms.
	 * 
	 * @return the size of the x-dimension
	 */
	public int getSizeX() {
		return sizeX;
	}

	/**
	 * Get size of y-dimension, or the zero for 1-d histograms.
	 * 
	 * @return the size of the y-dimension
	 */
	public int getSizeY() {
		return sizeY;
	}

	/**
	 * Sets the X-axis label
	 * 
	 * @param label
	 *            new label for X-axis
	 * @throws IllegalStateException
	 *             if x-axis label has already been explicitly set
	 */
	public final void setLabelX(String label) {
		if (labelXset) {
			throw new IllegalStateException(
					"Please call setLabelX() only once per histogram.");
		}
		labelX = label;
		labelXset = true;
	}

	/**
	 * Sets the Y-axis label
	 * 
	 * @param label
	 *            new label for Y-axis
	 * @throws IllegalStateException
	 *             if y-axis label has already been explicitly set
	 */
	public final void setLabelY(String label) {
		if (labelYset) {
			throw new IllegalStateException(
					"Please call setLabelY() only once per histogram.");
		}
		labelY = label;
		labelYset = true;
	}

	/**
	 * Returns the X-axis label
	 * 
	 * @return the X-axis label
	 */
	public String getLabelX() {
		return labelX;
	}

	/**
	 * Returns the Y-axis label
	 * 
	 * @return the Y-axis label
	 */
	public String getLabelY() {
		return labelY;
	}

	/**
	 * Sets an energy calibration function for this histogram.
	 * 
	 * @param calibFunc
	 *            new energy calibration for this histogram
	 */
	public synchronized void setCalibration(CalibrationFunction calibFunc) {
		this.calibFunc = calibFunc;
	}

	/**
	 * Returns the calibration function for this histogram as a
	 * <code>CalibrationFunction</code> object.
	 * 
	 * @return the calibration function for this histogram
	 */
	public synchronized CalibrationFunction getCalibration() {
		return calibFunc;
	}

	/**
	 * Returns whether the histogram is calibrated.
	 * 
	 * @return <code>true</code> if a calibration function has been defined,
	 *         <code>false</code> if not
	 * @see #setCalibration(CalibrationFunction)
	 */
	public synchronized boolean isCalibrated() {
		return (calibFunc != null);
	}

	/**
	 * Sets the number of this histogram. May have the side effect of bumping
	 * another histogram with the desired number to a new number.
	 * 
	 * @param num
	 *            the desired number for the histogram
	 */
	public void setNumber(int num) {
		final Integer newKey = new Integer(num);
		if (NUMBER_MAP.containsKey(newKey)) {
			final Histogram collider = (Histogram) NUMBER_MAP.get(newKey);
			if (!collider.equals(this)) {
				collider.assignNewNumber();
			}
		}
		NUMBER_MAP.remove(new Integer(number));
		number = num;
		NUMBER_MAP.put(new Integer(num), this);
	}

	private void assignNewNumber() {
		final int last = NUMBER_MAP.isEmpty() ? 0 : ((Integer) NUMBER_MAP
				.lastKey()).intValue();
		number = last + 1;
		NUMBER_MAP.put(new Integer(number), this);
	}

	/**
	 * Returns the counts in the histogram as an array of the appropriate type.
	 * It is necessary to cast the returned array as follows:
	 * <ul>
	 * <li><code>ONE_DIM_INT</code> cast with <code>(int [])</code></li>
	 * <li><code>TWO_DIM_INT</code> cast with <code>(int [][])</code></li>
	 * <li><code>ONE_DIM_DOUBLE</code> cast with <code>(double [])</code>
	 * </li>
	 * <li><code>TWO_DIM_DOUBLE</code> cast with <code>(double [][])</code>
	 * </li>
	 * </ul>
	 * 
	 * @return <code>Object</code> which must be cast as indicated above
	 */
	public abstract Object getCounts();

	/*
	 * { Object rval = null; if (type == Type.ONE_DIM_INT) { rval = counts; }
	 * else if (type == Type.TWO_DIM_INT) { rval = counts2d; } else if (type ==
	 * Type.ONE_D_DOUBLE) { rval = countsDouble; } else if (type ==
	 * Type.TWO_D_DOUBLE) { rval = counts2dD; } else { throw new
	 * IllegalStateException("Histogram not a recognized type."); } return rval; }
	 */

	/**
	 * Gets the counts in the specified channel.
	 * 
	 * @param channel
	 *            to get the counts of
	 * @return counts in the specified channel
	 * @throws UnsupportedOperationException
	 *             if not called on a 1-d histogram
	 */
	/*
	 * public abstract double getCounts(int channel); /*{ double rval = 0; if
	 * (type == Type.ONE_DIM_INT) { rval = counts[channel]; } else if (type ==
	 * Type.ONE_D_DOUBLE) { rval = countsDouble[channel]; } else { throw new
	 * UnsupportedOperationException(getName() + ": getCounts(int) cannot be
	 * called on a 2-d histogram."); } return rval; }
	 */

	/**
	 * Gets the counts in the specified channel.
	 * 
	 * @param chX
	 *            x-channel
	 * @param chY
	 *            y-channel
	 * @return counts in the specified channel
	 * @throws UnsupportedOperationException
	 *             if not called on a 2-d histogram
	 */
	/*
	 * public abstract double getCounts(int chX, int chY);/* { double rval = 0;
	 * if (type == Type.TWO_DIM_INT) { rval = counts2d[chX][chY]; } else if
	 * (type == Type.TWO_D_DOUBLE) { rval = counts2dD[chX][chY]; } else { throw
	 * new UnsupportedOperationException( getName() + ": getCounts(int,int)
	 * cannot be called on a 1-d histogram."); } return rval; }
	 */

	/* -- set methods */

	/**
	 * Zeroes all the counts in this histogram.
	 */
	public abstract void setZero();

	/*
	 * { if (type == Type.ONE_DIM_INT) { for (int i = 0; i < sizeX; i++) {
	 * counts[i] = 0; errors = null; } } else if (type == Type.ONE_D_DOUBLE) {
	 * for (int i = 0; i < sizeX; i++) { countsDouble[i] = 0.0; errors = null; } }
	 * else if (type == Type.TWO_DIM_INT) { for (int i = 0; i < sizeX; i++) {
	 * for (int j = 0; j < sizeY; j++) { counts2d[i][j] = 0; } } } else if (type ==
	 * Type.TWO_D_DOUBLE) { for (int i = 0; i < sizeX; i++) { for (int j = 0; j <
	 * sizeY; j++) { counts2dD[i][j] = 0.0; } } } }
	 */

	/**
	 * Calls setZero() on all histograms.
	 * 
	 * @see #setZero()
	 */
	public synchronized static void setZeroAll() {
		final Iterator iter = getHistogramList().iterator();
		while (iter.hasNext()) {
			final Histogram histogram = (Histogram) iter.next();
			histogram.setZero();
		}
	}

	/**
	 * Returns the list of gates that belong to this histogram.
	 * 
	 * @return the list of gates that belong to this histogram
	 */
	public List getGates() {
		return Collections.unmodifiableList(gates);
	}

	/**
	 * Returns whether this histogram has the given gate.
	 */
	public synchronized boolean hasGate(Gate gate) {
		boolean rval = false;//default return value
		for (int i = 0; i < gates.size(); i++) {
			if ((Gate) gates.get(i) == gate) {
				rval = true;
				break;//drop out of loop
			}
		}
		return rval;
	}

	/**
	 * Add a <code>Gate</code> to this histogram.
	 * 
	 * @throws UnsupportedOperationException
	 *             if a gate of a different type is given
	 */
	public synchronized void addGate(Gate gate) {
		if (gate.getDimensionality() == getDimensionality()) {
			if (!gates.contains(gate)) {
				gates.add(gate);
			}
		} else {
			throw new UnsupportedOperationException("Can't add "
					+ gate.getDimensionality() + "D gate to "
					+ getDimensionality() + "D histogram.");
		}
	}

	/**
	 * Set the counts array using the given <code>int</code> or
	 * <code>double</code> array.
	 * 
	 * @param countsIn
	 *            1d or 2d array of <code>int</code>'s or <code>double</code>
	 *            's
	 * @throws IllegalArgumentException
	 *             if countsIn is the wrong type.
	 */
	public abstract void setCounts(Object countsIn);

	/*
	 * { final Type countsType = Type.getArrayType(countsIn); if (type !=
	 * countsType) { throw new IllegalArgumentException( "The given array is of
	 * the wrong type."); } if (getDimensionality() == 1) { final boolean isInt =
	 * (type == Type.ONE_DIM_INT); final Object dest = isInt ? (Object) counts :
	 * countsDouble; final int inLength = isInt ? ((int[]) countsIn).length :
	 * ((double[]) countsIn).length; System.arraycopy(countsIn, 0, dest, 0,
	 * Math.min(inLength, sizeX)); } else { if (type == Type.TWO_DIM_INT) {
	 * setCounts((int[][]) countsIn); } else if (type == Type.TWO_D_DOUBLE) {
	 * setCounts((double[][]) countsIn); } } }
	 */

	/**
	 * Sets counts in the specified channel.
	 * 
	 * @param channel
	 *            to set counts in
	 * @param cts
	 *            counts to set
	 * @throws UnsupportedOperationException
	 *             if not called on a 1-d histogram
	 */
	/*
	 * public synchronized void setCounts(int channel, double cts) { if (type ==
	 * Type.ONE_DIM_INT) { counts[channel] = (int) cts; } else if (type ==
	 * Type.ONE_D_DOUBLE) { countsDouble[channel] = cts; } else { throw new
	 * UnsupportedOperationException( getName() + ": setCounts(int,double)
	 * cannot be called on a 2-d histogram."); } }
	 */

	/**
	 * Set the counts in the specified channel.
	 * 
	 * @param chX
	 *            x-channel
	 * @param chY
	 *            y-channel
	 * @param cts
	 *            counts to set
	 * @throws UnsupportedOperationException
	 *             if not called on a 2-d histogram
	 */
	/*
	 * public synchronized void setCounts(int chX, int chY, double cts) { if
	 * (type == Type.TWO_DIM_INT) { counts2d[chX][chY] = (int) cts; } else if
	 * (type == Type.TWO_D_DOUBLE) { counts2dD[chX][chY] = cts; } else { throw
	 * new UnsupportedOperationException( getName() + ":
	 * setCounts(int,int,double) cannot be called on a 1-d histogram."); } }
	 */

	/**
	 * Adds the given counts to this histogram.
	 * 
	 * @param countsIn
	 *            1d or 2d array of <code>int</code>'s or <code>double</code>
	 *            's, according to this histogram's type
	 * @throws IllegalArgumentException
	 *             if the parameter is the wrong type
	 */
	public abstract void addCounts(Object countsIn);

	/*
	 * { final Type inType = Type.getArrayType(countsIn); if (type != inType) {
	 * throw new IllegalArgumentException( "addCounts() needs to be called with
	 * the appropriate array type."); } if (type == Type.ONE_DIM_INT) {
	 * addCounts((int[]) countsIn); } else if (type == Type.TWO_DIM_INT) {
	 * addCounts((int[][]) countsIn); } else if (type == Type.ONE_D_DOUBLE) {
	 * addCounts((double[]) countsIn); } else { addCounts((double[][])
	 * countsIn); } }
	 */

	/**
	 * Gets the errors associated with the channel counts, only valid for 1-d
	 * histograms.
	 * 
	 * @return an array of the associated errors for the channel counts
	 * @exception UnsupportedOperationException
	 *                thrown if called on 2-d histogram
	 */
	/*
	 * public synchronized double[] getErrors(); { if (type == Type.ONE_DIM_INT) {
	 * final int length = counts.length; if (errors == null) { //set errors
	 * according to Poisson with error = 1 for zero // counts errors = new
	 * double[length]; for (int i = 0; i < length; i++) { if (counts[i] == 0) {
	 * errors[i] = 1.0; } else { errors[i] = Math.sqrt((double) counts[i]); } } } }
	 * else if (type == Type.ONE_D_DOUBLE) { final int length =
	 * countsDouble.length; if (errors == null) { //set errors according to
	 * Poisson with error = 1 for zero // counts errors = new double[length];
	 * for (int i = 0; i < length; i++) { if (countsDouble[i] == 0) { errors[i] =
	 * 1.0; } else { errors[i] = java.lang.Math.sqrt(countsDouble[i]); } } } }
	 * else { // invalid call if 2-d throw new UnsupportedOperationException(
	 * "Can't call getErrors() for a 2-d histogram"); } return errors; }
	 */

	/**
	 * Sets the errors associated with channel counts, only valid for 1-d
	 * histograms.
	 * 
	 * @param errors
	 *            the associated errors for the channel counts
	 * @exception UnsupportedOperationException
	 *                thrown if called on 2-d histogram
	 */
	/*
	 * public synchronized void setErrors(double[] errors) { if
	 * (getDimensionality() == 1) { this.errors = errors; errorsSet = true; }
	 * else { // invalid call if 2-d throw new UnsupportedOperationException(
	 * "Cannot set Error for 2-d [Histogram]"); } }
	 */

	/**
	 * Returns the number of dimensions in this histogram.
	 * 
	 * @return the number of dimensions in this histogram.
	 */
	public int getDimensionality() {
		return type.getDimensionality();
	}

	/**
	 * Gives the name of this histogram.
	 * 
	 * @return its name
	 */
	public String toString() {
		return name;
	}

	/**
	 * Returns the total number of counts in the histogram.
	 * 
	 * @return area under the counts in the histogram
	 */
	public abstract double getArea();
	/*
	 * { double sum = 0.0; if (type == Histogram.Type.ONE_D_DOUBLE) { for (int i =
	 * 0; i < sizeX; i++) { sum += countsDouble[i]; } } else if (type ==
	 * Histogram.Type.ONE_DIM_INT) { //1d int for (int i = 0; i < sizeX; i++) {
	 * sum += counts[i]; } } else if (type == Histogram.Type.TWO_DIM_INT) { for
	 * (int i = 0; i < sizeX; i++) { for (int j = 0; j < sizeY; j++) { sum +=
	 * counts2d[i][j]; } } } else { //2d double for (int i = 0; i < sizeX; i++) {
	 * for (int j = 0; j < sizeY; j++) { sum += counts2dD[i][j]; } } } return
	 * sum; }
	 */
}