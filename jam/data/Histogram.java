package jam.data;

import jam.data.func.CalibrationFunction;
import jam.data.peaks.PeakFinder;
import jam.util.StringUtilities;

import java.io.Serializable;
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
 * <li>type--the <code>double</code> types are not yet fully implemented
 * <ul>
 * <code><li>ONE_DIM_INT</li><li>TWO_DIM_INT</li><li>ONE_DIM_DOUBLE</li>
 *  <li>TWO_DIM_DOUBLE</li></code>
 * </ul>
 * <li>size
 * <li>title
 * <li>axis labels, x and y
 * <li>data array 1 or 2 dimension
 * <li>gates
 * </ul>
 * <p>
 * Modified 2/11/99 Dale Visser to have an error array too. By default, the
 * class will assume Poisson error bars and return square root of counts. For
 * <code>Histogram</code> s produced by adding, subtracting, or otherwise
 * manipulating other histograms, though, an appropriate error array should be
 * calculated and stored by invoking the <code>setErrors()</code> method.
 * 
 * @author Ken Swartz
 * @author Dale Visser
 * @version 0.5, 1.0
 * @see #setErrors(double[])
 * @since JDK 1.1
 */
public final class Histogram implements Serializable {

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
		public static final Type ONE_DIM_DOUBLE = new Type(2);

		/**
		 * Value of histogram type for two dimensional <code>double</code>
		 * histograms.
		 * 
		 * @see #getType()
		 */
		public static final Type TWO_DIM_DOUBLE = new Type(3);

		private final int type;

		private final static int[] dim = { 1, 2, 1, 2 };

		private final static boolean[] isInt = { true, true, false, false };

		private final static String[] string = { "1D int", "2D int",
				"1D double", "2D double" };

		private Type(int i) {
			type = i;
		}

		public int getDimensionality() {
			return dim[type];
		}

		public boolean isInteger() {
			return isInt[type];
		}

		public String toString() {
			return string[type];
		}

		/**
		 * Gives the counts array type of the given object.
		 * 
		 * @param o
		 *            a 1-d or 2-d int or double array
		 * @return which type the array corresponds to
		 */
		private static Type getArrayType(Object o) {
			final Type rval;
			final String error = "You may pass int or double arrays of up to two dimensions as histogram counts.";
			final Class type = o.getClass();
			if (!type.isArray()) {
				throw new IllegalArgumentException(error);
			}
			final Class componentA = type.getComponentType();
			if (componentA.equals(int.class)) {
				rval = Type.ONE_DIM_INT;
			} else if (componentA.equals(double.class)) {
				rval = Type.ONE_DIM_DOUBLE;
			} else {
				/* Two-D, componentA assumed to be array. */
				final Class componentB = componentA.getComponentType();
				if (componentB.equals(int.class)) {
					rval = Type.TWO_DIM_INT;
				} else if (componentB.equals(double.class)) {
					rval = Type.TWO_DIM_DOUBLE;
				} else {
					throw new IllegalArgumentException(error);
				}
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

	private final static Map sortedNameMap = new TreeMap();

	private final static SortedMap sortedNumberMap = new TreeMap();

	/* histogramList is ordered by the creation of the histograms */
	private final static List histogramList = new ArrayList();

	private final static List[] listByDim = new List[2];
	static {
		listByDim[0] = new ArrayList();
		listByDim[1] = new ArrayList();
	}

	/**
	 * gates that belong to this histogram
	 */
	final List gates = new ArrayList();

	/**
	 * Set to true if errors are set explicitly. Put in place so as not to waste
	 * disk space on saved files. IO routines should only write out error bars
	 * when this is set to true (checked by calling <code>errorsSet()</code>.
	 * Otherwise, Poisson errors should be assumed by other software.
	 * 
	 * @see #errorsSet()
	 */
	protected boolean errorsSet;

	private CalibrationFunction calibFunc;

	private String title; // title of histogram

	private String name; //abreviation to refer to it by

	private int number; //histogram number

	private Type type; //one or two dimension

	private final int sizeX; //size of histogram, for 1d size for 2d x size

	private final int sizeY; //size used for 2d histograms y size

	private String labelX; //x axis label

	private String labelY; //y axis label

	private int counts[]; // array to hold counts for 1d int

	private int counts2d[][]; // array to hold counts for 2d inc

	private double[] countsDouble; //array to hold counts for 1d double

	private double[][] counts2dDouble; //array to hold counds for 2d double

	private boolean labelXset = false;

	private boolean labelYset = false;

	/**
	 * Array which contains the errors in the channel counts.
	 */
	protected double[] errors;

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
	 * @see #ONE_DIM_INT
	 * @see #TWO_DIM_INT
	 * @see #ONE_DIM_DOUBLE
	 * @see #TWO_DIM_DOUBLE
	 * @throws IllegalArgumentException
	 *             if an unknown histogram type is given
	 */
	public Histogram(String nameIn, Type type, int sizeX, int sizeY,
			String title) {
		String addition;
		int prime;

		StringUtilities su = StringUtilities.instance();
		this.type = type;
		this.title = title;
		this.errors = null;
		this.name = nameIn;
		errorsSet = false;
		//give error if name is to be truncated
		String name2 = name = su.makeLength(name, NAME_LENGTH);
		name = name2;
		//find a name that does not conflict with exiting names
		prime = 1;
		while (sortedNameMap.containsKey(name)) {
			addition = "[" + prime + "]";
			name = su.makeLength(name, NAME_LENGTH - addition.length())
					+ addition;
			prime++;
		}
		gates.clear();
		assignNewNumber();
		//allow memory for gates and define sizes
		if (type == Type.ONE_DIM_INT) {
			this.sizeX = sizeX;
			this.sizeY = 0;
			counts = new int[sizeX];
			if (labelX == null) {
				labelX = X_LABEL_1D;
			}
			if (labelY == null) {
				labelY = Y_LABEL_1D;
			}
		} else if (type == Type.TWO_DIM_INT) {
			this.sizeX = sizeX;
			this.sizeY = sizeY;
			counts2d = new int[sizeX][sizeY];
			if (labelX == null) {
				labelX = X_LABEL_2D;
			}
			if (labelY == null) {
				labelY = Y_LABEL_2D;
			}
		} else if (type == Type.ONE_DIM_DOUBLE) {
			this.sizeX = sizeX;
			this.sizeY = 0;
			countsDouble = new double[sizeX];
			if (labelX == null) {
				labelX = X_LABEL_1D;
			}
			if (labelY == null) {
				labelY = Y_LABEL_1D;
			}
		} else if (type == Type.TWO_DIM_DOUBLE) {
			this.sizeX = sizeX;
			this.sizeY = sizeY;
			counts2dDouble = new double[sizeX][sizeY];
			if (labelX == null) {
				labelX = X_LABEL_2D;
			}
			if (labelY == null) {
				labelY = Y_LABEL_2D;
			}
		} else {
			throw new IllegalArgumentException("Error histogram '" + name
					+ "' defined with unknown type: " + type);
		}
		/* add to static lists */
		sortedNameMap.put(name, this);
		histogramList.add(this);
		listByDim[getDimensionality() - 1].add(this);
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
	public Histogram(String nameIn, Type type, int size, String title) {
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
	public Histogram(String name, Type type, int sizeX, int sizeY,
			String title, String axisLabelX, String axisLabelY) {
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
	public Histogram(String name, Type type, int size, String title,
			String axisLabelX, String axisLabelY) {
		this(name, type, size, size, title);
		setLabelX(axisLabelX);
		setLabelY(axisLabelY);
	}

	/**
	 * Given an array of counts, create a new 1-d <code>Histogram</code> and
	 * give it a number.
	 * 
	 * @param name
	 *            unique name of histogram, should be limited to
	 *            <code>NAME_LENGTH</code> characters, used in both .jhf and
	 *            .hdf files as the unique identifier for reloading the
	 *            histogram
	 * @param title
	 *            lengthier title of histogram, displayed on plot
	 * @param countsIn
	 *            array of counts to initialize with
	 */
	public Histogram(String name, String title, int[] countsIn) {
		this(name, Type.ONE_DIM_INT, countsIn.length, title);
		System.arraycopy(countsIn, 0, counts, 0, countsIn.length);
	}

	/**
	 * Given an array of counts, create a new 1-d <code>Histogram</code> and
	 * give it a number.
	 * 
	 * @param name
	 *            unique name of histogram, should be limited to
	 *            <code>NAME_LENGTH</code> characters, used in both .jhf and
	 *            .hdf files as the unique identifier for reloading the
	 *            histogram
	 * @param title
	 *            lengthier title of histogram, displayed on plot
	 * @param countsIn
	 *            array of counts to initialize with
	 */
	public Histogram(String name, String title, double[] countsIn) {
		this(name, Type.ONE_DIM_DOUBLE, countsIn.length, title);
		System.arraycopy(countsIn, 0, countsDouble, 0, countsIn.length);
	}

	/**
	 * Create a new 1-d <code>Histogram</code> with the counts known and with
	 * axes labeled.
	 * 
	 * @param name
	 *            unique name of histogram, should be limited to
	 *            <code>NAME_LENGTH</code> characters, used in both .jhf and
	 *            .hdf files as the unique identifier for reloading the
	 *            histogram
	 * @param title
	 *            lengthier title of histogram, displayed on plot
	 * @param axisLabelX
	 *            label displayed for x-axis on plot
	 * @param axisLabelY
	 *            label displayed for y-axis on plot
	 * @param countsIn
	 *            array of counts to initialize with
	 */
	public Histogram(String name, String title, String axisLabelX,
			String axisLabelY, int[] countsIn) {
		this(name, Type.ONE_DIM_INT, countsIn.length, title, axisLabelX,
				axisLabelY);
		System.arraycopy(countsIn, 0, counts, 0, countsIn.length);
	}

	/**
	 * Create a new 2-d histogram with counts known and automatically give it a
	 * number
	 * 
	 * @param name
	 *            unique name of histogram, should be limited to
	 *            <code>NAME_LENGTH</code> characters, used in both .jhf and
	 *            .hdf files as the unique identifier for reloading the
	 *            histogram
	 * @param title
	 *            lengthier title of histogram, displayed on plot
	 * @param countsIn
	 *            array of counts to initialize with, must be square
	 */
	public Histogram(String name, String title, int[][] countsIn) {
		this(name, Type.TWO_DIM_INT, countsIn.length, countsIn[0].length, title);
		for (int i = 0; i < countsIn.length; i++) { //copy arrays
			System
					.arraycopy(countsIn[i], 0, counts2d[i], 0,
							countsIn[0].length);
		}
	}

	/**
	 * Create a new 2-d histogram with counts known and automatically give it a
	 * number
	 * 
	 * @param name
	 *            unique name of histogram, should be limited to
	 *            <code>NAME_LENGTH</code> characters, used in both .jhf and
	 *            .hdf files as the unique identifier for reloading the
	 *            histogram
	 * @param title
	 *            lengthier title of histogram, displayed on plot
	 * @param countsIn
	 *            array of counts to initialize with, must be square
	 */
	public Histogram(String name, String title, double[][] countsIn) {
		this(name, Type.TWO_DIM_DOUBLE, countsIn.length, countsIn[0].length,
				title);
		for (int i = 0; i < countsIn.length; i++) { //copy arrays
			System.arraycopy(countsIn[i], 0, counts2dDouble[i], 0,
					countsIn[0].length);
		}
	}

	/**
	 * Create a new 2-d histogram with counts known (must be square histogram)
	 * and with the axis label given.
	 * 
	 * @param name
	 *            unique name of histogram, should be limited to
	 *            <code>NAME_LENGTH</code> characters, used in both .jhf and
	 *            .hdf files as the unique identifier for reloading the
	 *            histogram
	 * @param title
	 *            lengthier title of histogram, displayed on plot
	 * @param axisLabelX
	 *            label displayed for x-axis on plot
	 * @param axisLabelY
	 *            label displayed for y-axis on plot
	 * @param countsIn
	 *            array of counts to initialize with
	 */
	public Histogram(String name, String title, String axisLabelX,
			String axisLabelY, int[][] countsIn) {
		this(name, Type.TWO_DIM_INT, countsIn.length, countsIn[0].length,
				title, axisLabelX, axisLabelY);
		for (int i = 0; i < countsIn.length; i++) { //copy arrays
			System
					.arraycopy(countsIn[i], 0, counts2d[i], 0,
							countsIn[0].length);
		}
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
		Iterator allHistograms = inHistList.iterator();
		while (allHistograms.hasNext()) { //loop for all histograms
			Histogram hist = (Histogram) allHistograms.next();
			sortedNameMap.put(hist.getName(), hist);
			histogramList.add(hist);
			sortedNumberMap.put(new Integer(hist.getNumber()), hist);
		}
	}

	/**
	 * Returns the list of all histograms.
	 * 
	 * @return all histograms
	 */
	public static List getHistogramList() {
		return Collections.unmodifiableList(histogramList);
	}

	public static List getHistogramList(int dim) {
		if (dim < 1 || dim > 2) {
			throw new IllegalArgumentException(
					"Expect 1 or 2, the possible numbers of dimensions.");
		}
		return Collections.unmodifiableList(listByDim[dim - 1]);
	}

	/**
	 * @return list of all histograms sorted by number
	 */
	public static Collection getListSortedByNumber() {
		return Collections.unmodifiableCollection(sortedNumberMap.values());
	}

	/**
	 * @return list of all histograms sorted by name
	 */
	public static Collection getListSortedByName() {
		return Collections.unmodifiableCollection(sortedNameMap.values());
	}

	/**
	 * Clears the list of histograms.
	 */
	public static void clearList() {
		for (Iterator it = histogramList.iterator(); it.hasNext();) {
			final Histogram his = (Histogram) it.next();
			his.clearInfo();
		}
		histogramList.clear();
		sortedNameMap.clear();
		sortedNumberMap.clear();
		listByDim[0].clear();
		listByDim[1].clear();
		System.gc();
	}

	public static void deleteHistogram(String histName) {
		Histogram h = getHistogram(histName);
		if (h != null) {
			h.clearInfo();
			histogramList.remove(h);
			sortedNameMap.remove(h.getName());
			sortedNumberMap.remove(new Integer(h.getNumber()));
			listByDim[0].remove(h);
			listByDim[1].remove(h);
			h = null;
			System.gc();
		}
	}

	private void clearInfo() {
		gates.clear();
		calibFunc = null;
		counts = null;
		counts2d = null;
		counts2dDouble = null;
		countsDouble = null;
		errors = null;
		labelX = null;
		labelY = null;
		title = null;
	}

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
			rval = (Histogram) sortedNameMap.get(name);
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
		return (Histogram) sortedNumberMap.get(new Integer(num));
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
	 * @see #ONE_DIM_INT
	 * @see #TWO_DIM_INT
	 * @see #ONE_DIM_DOUBLE
	 * @see #TWO_DIM_DOUBLE
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
	public void setLabelX(String label) {
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
	public void setLabelY(String label) {
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
	 * @param n
	 *            the desired number for the histogram
	 */
	public void setNumber(int n) {
		final Integer newKey = new Integer(n);
		if (sortedNumberMap.containsKey(newKey)) {
			final Histogram collider = (Histogram) sortedNumberMap.get(newKey);
			if (!collider.equals(this)) {
				collider.assignNewNumber();
			}
		}
		sortedNumberMap.remove(new Integer(number));
		number = n;
		sortedNumberMap.put(new Integer(n), this);
	}

	private void assignNewNumber() {
		final int last = sortedNumberMap.isEmpty() ? 0
				: ((Integer) sortedNumberMap.lastKey()).intValue();
		number = last + 1;
		sortedNumberMap.put(new Integer(number), this);
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
	public synchronized Object getCounts() {
		Object rval = null;
		if (type == Type.ONE_DIM_INT) {
			rval = counts;
		} else if (type == Type.TWO_DIM_INT) {
			rval = counts2d;
		} else if (type == Type.ONE_DIM_DOUBLE) {
			rval = countsDouble;
		} else if (type == Type.TWO_DIM_DOUBLE) {
			rval = counts2dDouble;
		} else {
			throw new IllegalStateException("Histogram not a recognized type.");
		}
		return rval;
	}

	/**
	 * Gets the counts in the specified channel.
	 * 
	 * @param channel
	 *            to get the counts of
	 * @return counts in the specified channel
	 * @throws UnsupportedOperationException
	 *             if not called on a 1-d histogram
	 */
	public synchronized double getCounts(int channel) {
		double rval = 0;
		if (type == Type.ONE_DIM_INT) {
			rval = counts[channel];
		} else if (type == Type.ONE_DIM_DOUBLE) {
			rval = countsDouble[channel];
		} else {
			throw new UnsupportedOperationException(getName()
					+ ": getCounts(int) cannot be called on a 2-d histogram.");
		}
		return rval;
	}

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
	public synchronized double getCounts(int chX, int chY) {
		double rval = 0;
		if (type == Type.TWO_DIM_INT) {
			rval = counts2d[chX][chY];
		} else if (type == Type.TWO_DIM_DOUBLE) {
			rval = counts2dDouble[chX][chY];
		} else {
			throw new UnsupportedOperationException(
					getName()
							+ ": getCounts(int,int) cannot be called on a 1-d histogram.");
		}
		return rval;
	}

	/* -- set methods */

	/**
	 * Zeroes all the counts in this histogram.
	 */
	public synchronized void setZero() {
		if (type == Type.ONE_DIM_INT) {
			for (int i = 0; i < sizeX; i++) {
				counts[i] = 0;
				errors = null;
			}
		} else if (type == Type.ONE_DIM_DOUBLE) {
			for (int i = 0; i < sizeX; i++) {
				countsDouble[i] = 0.0;
				errors = null;
			}

		} else if (type == Type.TWO_DIM_INT) {
			for (int i = 0; i < sizeX; i++) {
				for (int j = 0; j < sizeY; j++) {
					counts2d[i][j] = 0;
				}
			}

		} else if (type == Type.TWO_DIM_DOUBLE) {
			for (int i = 0; i < sizeX; i++) {
				for (int j = 0; j < sizeY; j++) {
					counts2dDouble[i][j] = 0.0;
				}
			}
		}
	}

	/**
	 * Calls setZero() on all histograms.
	 *
	 * @see #setZero()
	 */
	public synchronized static void setZeroAll() {
		final Iterator it = getHistogramList().iterator();
		while (it.hasNext()) {
			final Histogram h = (Histogram) it.next();
			h.setZero();
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
		if (gate.type == Gate.ONE_DIMENSION) {
			if (gate.histogram.getDimensionality() == 1) {
				if (!gates.contains(gate)) {
					gates.add(gate);
				}
			} else {
				throw new UnsupportedOperationException(
						"Can't add 1-d gate to 2-dim histogram.");
			}
		} else if (gate.type == Gate.TWO_DIMENSION) {
			if (gate.histogram.getDimensionality() == 2) {
				if (!gates.contains(gate)) {
					gates.add(gate);
				}
			} else {
				throw new UnsupportedOperationException(
						"Can't add 2-d gate to 1-dim histogram.");
			}
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
	public synchronized void setCounts(Object countsIn) {
		final Type countsType = Type.getArrayType(countsIn);
		if (type != countsType) {
			throw new IllegalArgumentException(
					"The given array is of the wrong type.");
		}
		if (getDimensionality() == 1) {
			final boolean isInt = (type == Type.ONE_DIM_INT);
			final Object dest = isInt ? (Object) counts : countsDouble;
			final int inLength = isInt ? ((int[]) countsIn).length
					: ((double[]) countsIn).length;
			System.arraycopy(countsIn, 0, dest, 0, Math.min(inLength, sizeX));
		} else {
			if (type == Type.TWO_DIM_INT) {
				setCounts((int[][]) countsIn);
			} else if (type == Type.TWO_DIM_DOUBLE) {
				setCounts((double[][]) countsIn);
			}
		}
	}

	/**
	 * Sets counts in the specified channel.
	 *  
	 * @param channel to set counts in
	 * @param cts counts to set
	 * @throws UnsupportedOperationException if not called on a
	 * 1-d histogram
	 */
	public synchronized void setCounts(int channel, double cts) {
		if (type == Type.ONE_DIM_INT) {
			counts[channel] = (int) cts;
		} else if (type == Type.ONE_DIM_DOUBLE) {
			countsDouble[channel] = cts;
		} else {
			throw new UnsupportedOperationException(
					getName()
							+ ": setCounts(int,double) cannot be called on a 2-d histogram.");
		}
	}

	/**
	 * Set the counts in the specified channel.
	 * 
	 * @param chX x-channel
	 * @param chY y-channel
	 * @param cts counts to set
	 * @throws UnsupportedOperationException if not called on a
	 * 2-d histogram
	 */
	public synchronized void setCounts(int chX, int chY, double cts) {
		if (type == Type.TWO_DIM_INT) {
			counts2d[chX][chY] = (int) cts;
		} else if (type == Type.TWO_DIM_DOUBLE) {
			counts2dDouble[chX][chY] = cts;
		} else {
			throw new UnsupportedOperationException(
					getName()
							+ ": setCounts(int,int,double) cannot be called on a 1-d histogram.");
		}
	}

	private synchronized void addCounts(int[] countsIn) {
		final int max = Math.min(countsIn.length, sizeX) - 1;
		for (int i = max; i >= 0; i--) {
			counts[i] += countsIn[i];
		}
	}

	/**
	 * Adds the given counts to this histogram.
	 * 
	 * @param countsIn
	 *            1d or 2d array of <code>int</code>'s or <code>double</code>
	 *            's, according to this histogram's type
	 * @throws IllegalArgumentException
	 *             if the parameter is the wrong type
	 */
	public synchronized void addCounts(Object countsIn) {
		final Type inType = Type.getArrayType(countsIn);
		if (type != inType) {
			throw new IllegalArgumentException(
					"addCounts() needs to be called with the appropriate array type.");
		}
		if (type == Type.ONE_DIM_INT) {
			addCounts((int[]) countsIn);
		} else if (type == Type.TWO_DIM_INT) {
			addCounts((int[][]) countsIn);
		} else if (type == Type.ONE_DIM_DOUBLE) {
			addCounts((double[]) countsIn);
		} else {
			addCounts((double[][]) countsIn);
		}
	}

	private synchronized void addCounts(double[] countsIn) {
		final int max = Math.min(countsIn.length, sizeX) - 1;
		for (int i = max; i >= 0; i--) {
			counts[i] += countsIn[i];
		}
	}

	private synchronized void setCounts(int[][] countsIn) {
		final int loopLen = Math.min(counts2d.length, countsIn.length);
		for (int i = 0; i < loopLen; i++) {
			System.arraycopy(countsIn[i], 0, counts2d[i], 0, Math.min(
					countsIn[i].length, counts2d[i].length));
		}
	}

	private synchronized void addCounts(int[][] countsIn) {
		final int maxX = Math.min(sizeX, countsIn.length) - 1;
		final int maxY = Math.min(sizeY, countsIn[0].length) - 1;
		for (int x = maxX; x >= 0; x--) {
			for (int y = maxY; y >= 0; y--) {
				counts2d[x][y] += countsIn[x][y];
			}
		}
	}

	private synchronized void addCounts(double[][] countsIn) {
		final int maxX = Math.min(sizeX, countsIn.length) - 1;
		final int maxY = Math.min(sizeY, countsIn[0].length) - 1;
		for (int x = maxX; x >= 0; x--) {
			for (int y = maxY; y >= 0; y--) {
				counts2d[x][y] += countsIn[x][y];
			}
		}
	}

	private synchronized void setCounts(double[][] countsIn) {
		final int loopLen = Math.min(countsIn.length, counts2dDouble.length);
		for (int i = 0; i < loopLen; i++) {
			System.arraycopy(countsIn[i], 0, counts2dDouble[i], 0, Math.min(
					countsIn[i].length, counts2dDouble[i].length));
		}
	}

	/**
	 * Increments the counts by one in the given channel. Must be a histogram of
	 * type <code>ONE_DIM_INT</code>.
	 * 
	 * @param dataWord
	 *            the channel to be incremented
	 * @exception UnsupportedOperationException
	 *                thrown if method called for inappropriate type of
	 *                histogram
	 */
	public void inc(int dataWord) {
		int incCh = dataWord;
		if (type != Type.ONE_DIM_INT)
			throw new UnsupportedOperationException(
					"Can only call inc(int) for type ONE_DIM_INT, name=" + name);
		//check for overflow
		if (incCh >= sizeX) {
			incCh = sizeX - 1;
		} else if (dataWord < 0) {
			incCh = 0;
		}
		synchronized (this) {
			counts[incCh]++;
		}
	}

	/**
	 * Increments the counts by one in the given channel. Must be a histogram of
	 * type <code>TWO_DIM_INT</code>.
	 * 
	 * @param dataWordX
	 *            the x-channel to be incremented
	 * @param dataWordY
	 *            the y-channel to be incremented
	 * @exception UnsupportedOperationException
	 *                thrown if method called for inappropriate type of
	 *                histogram
	 */
	public void inc(int dataWordX, int dataWordY) {
		int incX = dataWordX;
		int incY = dataWordY;
		if (type != Type.TWO_DIM_INT)
			throw new UnsupportedOperationException(
					"Can only call inc(int,int) for type TWO_DIM_INT, name="
							+ name);
		//check for overflow and underflow
		if (incX >= sizeX) {
			incX = sizeX - 1;
		} else if (incX < 0) {
			incX = 0;
		}
		if (incY >= sizeY) {
			incY = sizeY - 1;
		} else if (dataWordY < 0) {
			incY = 0;
		}
		synchronized (this) {
			counts2d[incX][incY]++;
		}
	}

	/**
	 * Gets the errors associated with the channel counts, only valid for 1-d
	 * histograms.
	 * 
	 * @return an array of the associated errors for the channel counts
	 * @exception UnsupportedOperationException
	 *                thrown if called on 2-d histogram
	 */
	public synchronized double[] getErrors() {
		if (type == Type.ONE_DIM_INT) {
			final int length = counts.length;
			if (errors == null) {
				//set errors according to Poisson with error = 1 for zero
				// counts
				errors = new double[length];
				for (int i = 0; i < length; i++) {
					if (counts[i] == 0) {
						errors[i] = 1.0;
					} else {
						errors[i] = java.lang.Math.sqrt((double) counts[i]);
					}
				}
			}
		} else if (type == Type.ONE_DIM_DOUBLE) {
			final int length = countsDouble.length;
			if (errors == null) {
				//set errors according to Poisson with error = 1 for zero
				// counts
				errors = new double[length];
				for (int i = 0; i < length; i++) {
					if (countsDouble[i] == 0) {
						errors[i] = 1.0;
					} else {
						errors[i] = java.lang.Math.sqrt(countsDouble[i]);
					}
				}
			}
		} else { // invalid call if 2-d
			throw new UnsupportedOperationException(
					"Can't call getErrors() for a 2-d histogram");
		}
		return errors;
	}

	/**
	 * Sets the errors associated with channel counts, only valid for 1-d
	 * histograms.
	 * 
	 * @param errors
	 *            the associated errors for the channel counts
	 * @exception UnsupportedOperationException
	 *                thrown if called on 2-d histogram
	 */
	public synchronized void setErrors(double[] errors) {
		if (getDimensionality() == 1) {
			this.errors = errors;
			errorsSet = true;
		} else { // invalid call if 2-d
			throw new UnsupportedOperationException(
					"Cannot set Error for 2-d [Histogram]");
		}
	}

	/**
	 * Returns the number of dimensions in this histogram.
	 * 
	 * @return the number of dimensions in this histogram.
	 */
	public int getDimensionality() {
		return type.getDimensionality();
	}

	/**
	 * Returns whether errors have been explicitly set or not.
	 * 
	 * @return <code>true</code> if errors have been explicitly set,
	 *         <code>false</code> if not
	 */
	public synchronized boolean errorsSet() {
		return errorsSet;
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
	 * Attempt to find gaussian peaks.
	 * 
	 * @param sensitivity
	 *            if larger, peaks need to be more significant to be found
	 * @param width
	 *            target FWHM of peak
	 * @param cal
	 *            whether we return calibrated values
	 * @return centroids, in channel units or calibrated units, depending on
	 *         <code>cal</code>
	 * @throws UnsupportedOperationException
	 *             if called on a 2d histogram
	 */
	public double[][] findPeaks(double sensitivity, double width, boolean cal) {
		double[] histArray;
		if (getType() == Type.ONE_DIM_DOUBLE) {
			histArray = countsDouble;
		} else if (getType() == Type.ONE_DIM_INT) { //INT type
			int[] temp = counts;
			histArray = new double[temp.length];
			for (int i = 0; i < temp.length; i++) {
				histArray[i] = temp[i];
			}
		} else { //2D
			throw new UnsupportedOperationException(
					"findPeaks() called on 2D hist");
		}
		double[] posn = PeakFinder.getCentroids(histArray, sensitivity, width);
		double[][] rval = new double[3][posn.length];
		if (cal && this.isCalibrated()) {
			for (int i = 0; i < posn.length; i++) {
				rval[0][i] = posn[i];
				rval[1][i] = calibFunc.getValue(posn[i]);
				rval[2][i] = histArray[(int) Math.round(posn[i])];
			}
		} else { //no calibration
			for (int i = 0; i < posn.length; i++) {
				rval[0][i] = posn[i];
				rval[1][i] = posn[i];
				rval[2][i] = histArray[(int) Math.round(posn[i])];
			}
		}
		return rval;
	}
}