package jam.data;

import jam.util.StringUtilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
 * 
 * @author Ken Swartz
 * @author Dale Visser
 * @version 0.5, 1.0
 * @since JDK 1.1
 */
public abstract class Histogram implements DataElement {

	/**
	 * Encapsulates the 4 different types a histogram may have.
	 * 
	 * @author <a href="mailto:dale@visser.name">Dale W Visser </a>
	 */
	public static class Type {
		private final static int[] DIM = { 1, 2, 1, 2 };

		private final static boolean[] INT = { true, true, false, false };

		/**
		 * Histogram dimensionality compare to <code>getDimensionality()</code>
		 */
		public final static int ONE_D = 1;

		/**
		 * Value of histogram type for one dimensional <code>double</code>
		 * histograms.
		 * 
		 * @see #getType()
		 */
		public static final Type ONE_D_DOUBLE = new Type(2);

		/**
		 * Value of histogram type for one dimensional <code>int</code>
		 * histograms.
		 * 
		 * @see #getType()
		 */
		public static final Type ONE_DIM_INT = new Type(0);

		private final static String[] STRING = { "1D int", "2D int",
				"1D double", "2D double" };

		/**
		 * Histogram dimensionality compare to <code>getDimensionality()</code>
		 */
		public final static int TWO_D = 2;

		/**
		 * Value of histogram type for two dimensional <code>double</code>
		 * histograms.
		 * 
		 * @see #getType()
		 */
		public static final Type TWO_D_DOUBLE = new Type(3);

		/**
		 * Value of histogram type for two dimensional <code>int</code>
		 * histograms.
		 * 
		 * @see #getType()
		 */
		public static final Type TWO_DIM_INT = new Type(1);

		/**
		 * Gives the counts array type of the given object.
		 * 
		 * @param array
		 *            a 1-d or 2-d int or double array
		 * @return which type the array corresponds to
		 */
		static Type getArrayType(final Object array) {
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

		private transient final int typeNum;

		private Type(int num) {
			super();
			typeNum = num;
		}

		/**
		 * @return 1 or 2
		 */
		public int getDimensionality() {
			return DIM[typeNum];
		}

		/**
		 * @param sizeX
		 *            horizontal channels
		 * @param sizeY
		 *            vertical channels
		 * @return array of the appropriate type and size
		 */
		public Object getSampleArray(final int sizeX, final int sizeY) {
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

		/**
		 * 
		 * @return <code>true</true> if counts are integer, not floating point
		 */
		public boolean isInteger() {
			return INT[typeNum];
		}

		/**
		 * @see Object#toString()
		 */
		public String toString() {
			return STRING[typeNum];
		}
	}

	private final static List<List<Histogram>> DIM_LIST = new ArrayList<List<Histogram>>(
			2);

	private static final String EMPTY_STRING = "";

	/* histogramList is ordered by the creation of the histograms */
	private final static List<Histogram> LIST = new ArrayList<Histogram>();

	/**
	 * Maximum number of characters in the histogram name.
	 */
	public static final int NAME_LENGTH = 20;

	private final static Map<String, Histogram> NAME_MAP = new HashMap<String, Histogram>();

	private final static SortedMap<Integer, Histogram> NUMBER_MAP = new TreeMap<Integer, Histogram>();

	/**
	 * default axis labels
	 */
	static final String X_LABEL_1D = "Channels";

	static final String X_LABEL_2D = "Channels";

	static final String Y_LABEL_1D = "Counts";

	static final String Y_LABEL_2D = "Channels";

	static {
		DIM_LIST.add(0, new ArrayList<Histogram>());
		DIM_LIST.add(1, new ArrayList<Histogram>());
	}

	/**
	 * Clears the list of histograms.
	 */
	public static void clearList() {
		final Iterator iterator = LIST.iterator();
		while (iterator.hasNext()) {
			final Histogram his = (Histogram) iterator.next();
			his.clearInfo();
		}
		LIST.clear();
		NAME_MAP.clear();
		NUMBER_MAP.clear();
		for (List<Histogram> list : DIM_LIST) {
			list.clear();
		}
		System.gc();
	}

	/**
	 * Creates a new histogram, using the given array as the template.
	 * 
	 * @param group
	 *            group to create histogram in
	 * @param array
	 *            1d or 2d int or double array
	 * @param name
	 *            unique identifier
	 * @return a newly created histogram
	 */
	static public Histogram createHistogram(final Group group,
			final Object array, final String name) {
		return createHistogram(group, array, name, name, null, null);
	}

	/**
	 * Creates a new histogram, using the given array as the template.
	 * 
	 * @param group
	 *            group to create histogram in
	 * @param array
	 *            1d or 2d int or double array
	 * @param name
	 *            unique identifier
	 * @param title
	 *            verbose description
	 * @return a newly created histogram
	 */
	static public Histogram createHistogram(final Group group,
			final Object array, final String name, final String title) {
		return createHistogram(group, array, name, title, null, null);
	}

	/**
	 * Creates a new histogram, using the given array as the template.
	 * 
	 * @param group
	 *            group to create histogram in
	 * @param array
	 *            1d or 2d int or double array
	 * @param name
	 *            unique identifier
	 * @param title
	 *            verbose description
	 * @param labelX
	 *            x-axis label
	 * @param labelY
	 *            y-axis label
	 * @return a newly created histogram
	 */
	static public Histogram createHistogram(final Group group,
			final Object array, final String name, final String title,
			final String labelX, final String labelY) {
		final Histogram rval;
		final Type hType = Type.getArrayType(array);
		if (hType == Type.ONE_DIM_INT) {
			rval = new HistInt1D(group, name, title, labelX, labelY,
					(int[]) array);
		} else if (hType == Type.ONE_D_DOUBLE) {
			rval = new HistDouble1D(group, name, title, labelX, labelY,
					(double[]) array);
		} else if (hType == Type.TWO_DIM_INT) {
			rval = new HistInt2D(group, name, title, labelX, labelY,
					(int[][]) array);
		} else {// TWO_D_DOUBLE
			rval = new HistDouble2D(group, name, title, labelX, labelY,
					(double[][]) array);
		}
		return rval;
	}

	/**
	 * Remove the histogram with the given name from memory.
	 * 
	 * @param histName
	 *            name of histogram to remove
	 */
	public static void deleteHistogram(final String histName) {
		if (NAME_MAP.containsKey(histName)) {
			final Histogram histogram = getHistogram(histName);
			if (histogram != null) {
				histogram.clearInfo();
				LIST.remove(histogram);
				NAME_MAP.remove(histogram.getFullName());
				NUMBER_MAP.remove(new Integer(histogram.getNumber()));
				for (List<Histogram> list : DIM_LIST) {
					list.clear();
				}
				final Group group = histogram.getGroup();
				group.removeHistogram(histogram);
			}
		}
		System.gc();
	}

	/**
	 * Get the histogram with the given number.
	 * 
	 * @param num
	 *            of the histogram
	 * @return the histogram, if it exists, null otherwise
	 */
	public static Histogram getHistogram(final int num) {
		return NUMBER_MAP.get(num);
	}

	/**
	 * Get the histogram with the given name.
	 * 
	 * @param name
	 *            name of histogram to retrieve
	 * @return the histogram with the given name, null if name doesn't exist.
	 */
	public static Histogram getHistogram(final String name) {
		final Histogram rval = name == null ? null : (Histogram) NAME_MAP
				.get(name);
		return rval;
	}

	/**
	 * Returns the list of all histograms.
	 * 
	 * @return all histograms
	 */
	public static List<Histogram> getHistogramList() {
		return Collections.unmodifiableList(LIST);
	}

	/**
	 * @param dim
	 *            1 or 2
	 * @return list of all histograms with the given dimensionality
	 */
	public static List<Histogram> getHistogramList(final int dim) {
		if (dim < 1 || dim > 2) {
			throw new IllegalArgumentException(
					"Expect 1 or 2, the possible numbers of dimensions.");
		}
		return Collections.unmodifiableList(DIM_LIST.get(dim - 1));
	}

	/**
	 * @param names
	 *            of histograms we want
	 * @return all histograms that have names matching the given list
	 */
	public static List<Histogram> getHistogramList(final List<String> names) {
		final List<Histogram> rval = new ArrayList<Histogram>();
		for (String name : names) {
			if (NAME_MAP.containsKey(name)) {
				rval.add(getHistogram(name));
			}
		}
		return Collections.unmodifiableList(rval);
	}

	/**
	 * @return list of all histograms sorted by number
	 */
	public static Collection<Histogram> getListSortedByNumber() {
		return Collections.unmodifiableCollection(NUMBER_MAP.values());
	}

	/**
	 * Histogram is a valid histogram
	 * 
	 * @param hist
	 * @return <code>true</code> if this histogram remains in the name mapping
	 */
	public static boolean isValid(final Histogram hist) {
		return NAME_MAP.containsValue(hist);
	}

	/**
	 * Sets the list of histograms, used for remote loading of histograms.
	 * 
	 * @param inHistList
	 *            must contain all histogram objects
	 */
	public static void setHistogramList(final List<Histogram> inHistList) {
		clearList();
		for (Histogram hist : inHistList) {
			NAME_MAP.put(hist.getFullName(), hist);
			LIST.add(hist);
			NUMBER_MAP.put(hist.getNumber(), hist);
		}
	}

	/**
	 * Calls setZero() on all histograms.
	 * 
	 * @see #setZero()
	 */
	public synchronized static void setZeroAll() {
		for (Histogram histogram : getHistogramList()) {
			histogram.setZero();
		}
	}

	/**
	 * gates that belong to this histogram
	 */
	private transient final List<Gate> gates = new ArrayList<Gate>();

	/** Name of group histogram belongs to */
	private transient String groupName;

	private String labelX=""; // x axis label

	private String labelY=""; // y axis label

	// end of constructors

	/** abbreviation to refer to histogram */
	private transient String name;

	/** Number of histogram */
	private int number;

	private transient final int sizeX; // size of histogram, for 1d size for

	private transient final int sizeY; // size used for 2d histograms y size

	/** title of histogram */
	private transient String title;

	private transient Type type; // one or two dimension

	/** unique name amongst all histograms */
	private transient String uniqueName;

	/**
	 * Master constructor invoked by all other constructors.
	 * 
	 * @param group
	 *            group this histogram belongs to
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
	protected Histogram(Group group, String nameIn, Type type, int sizeX,
			int sizeY, String title) {
		super();
		this.type = type;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.title = title;
		/* Make a unique name in the group */
		final Map<String, Histogram> groupHistMap = group.getHistogramMap();
		final StringUtilities stringUtil = StringUtilities.getInstance();
		name = stringUtil.makeUniqueName(nameIn, groupHistMap.keySet(),
				NAME_LENGTH);
		updateNames(group);// puts in name map as well
		/* Add to group */
		group.addHistogram(this);
		gates.clear();
		assignNewNumber();
		/* allow memory for gates and define sizes */
		final boolean oneD = type.getDimensionality() == 1;
		if (labelX == null) {
			labelX = oneD ? X_LABEL_1D : X_LABEL_2D;
		}
		if (labelY == null) {
			labelY = oneD ? Y_LABEL_1D : Y_LABEL_2D;
		}
		/* add to static lists */
		LIST.add(this);
		DIM_LIST.get(type.getDimensionality() - 1).add(this);
	}

	/**
	 * Contructor with no number given, but axis labels are given.
	 * 
	 * @param group
	 *            group this histogram belongs to
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
	protected Histogram(Group group, String name, Type type, int sizeX,
			int sizeY, String title, String axisLabelX, String axisLabelY) {
		this(group, name, type, sizeX, sizeY, title);
		setLabelX(axisLabelX);
		setLabelY(axisLabelY);
	}

	/**
	 * Contructor with no number given, but axis labels are given.
	 * 
	 * @param group
	 *            group this histogram belongs to
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
	protected Histogram(Group group, String nameIn, Type type, int size,
			String title) {
		this(group, nameIn, type, size, size, title);
	}

	/**
	 * Contructor with no number given, but axis labels are given.
	 * 
	 * @param group
	 *            group this histogram belongs to
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
	protected Histogram(Group group, String name, Type type, int size,
			String title, String axisLabelX, String axisLabelY) {
		this(group, name, type, size, size, title);
		setLabelX(axisLabelX);
		setLabelY(axisLabelY);
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
	public abstract void addCounts(Object countsIn);

	/* instantized methods */

	/**
	 * Add a <code>Gate</code> to this histogram.
	 * 
	 * @param gate
	 *            to add
	 * @throws UnsupportedOperationException
	 *             if a gate of a different type is given
	 */
	public synchronized void addGate(final Gate gate) {
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

	private void assignNewNumber() {
		final int last = NUMBER_MAP.isEmpty() ? 0 : (NUMBER_MAP.lastKey())
				.intValue();
		number = last + 1;
		NUMBER_MAP.put(new Integer(number), this);
	}

	abstract void clearCounts();

	private void clearInfo() {
		gates.clear();
		labelX = EMPTY_STRING;
		labelY = EMPTY_STRING;
		title = EMPTY_STRING;
		clearCounts();
	}

	/**
	 * Returns the total number of counts in the histogram.
	 * 
	 * @return area under the counts in the histogram
	 */
	public abstract double getArea();

	public abstract double getCount();

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

	/**
	 * Returns the number of dimensions in this histogram.
	 * 
	 * @return the number of dimensions in this histogram.
	 */
	public int getDimensionality() {
		return type.getDimensionality();
	}

	public DataElement.Type getElementType() {
		return DataElement.Type.HISTOGRAM;
	}

	/**
	 * Returns the histogram full name that resolves it. (could change if
	 * multiple histograms have the same name)
	 * 
	 * @return the name of this histogram
	 */
	public String getFullName() {
		return uniqueName;
	}

	/**
	 * Returns the list of gates that belong to this histogram.
	 * 
	 * @return the list of gates that belong to this histogram
	 */
	public List<Gate> getGates() {
		return Collections.unmodifiableList(gates);
	}

	/**
	 * Get the group this histograms belongs to.
	 * 
	 * @return the Group
	 */
	public Group getGroup() {
		return Group.getGroup(groupName);
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
	 * Returns the histogram name.
	 * 
	 * @return the name of this histogram
	 */
	public String getName() {
		return name;
	}

	/* -- set methods */

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
	 * Returns the histogram title.
	 * 
	 * @return the title of this histogram
	 */
	public String getTitle() {
		return title;
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
	 * @param gate
	 *            that we're wondering about
	 * @return whether this histogram has the given gate
	 */
	public synchronized boolean hasGate(final Gate gate) {
		boolean rval = false;// default return value
		for (int i = 0; i < gates.size(); i++) {
			if (gates.get(i) == gate) {
				rval = true;
				break;// drop out of loop
			}
		}
		return rval;
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

	/**
	 * Sets the X-axis label
	 * 
	 * @param label
	 *            new label for X-axis
	 * @throws IllegalStateException
	 *             if x-axis label has already been explicitly set
	 */
	public final void setLabelX(final String label) {
		if (labelX.length()>0) {
			throw new IllegalStateException(
					"Please call setLabelX() only once per histogram.");
		}
		labelX = label;
	}

	/**
	 * Sets the Y-axis label
	 * 
	 * @param label
	 *            new label for Y-axis
	 * @throws IllegalStateException
	 *             if y-axis label has already been explicitly set
	 */
	public final void setLabelY(final String label) {
		if (labelY.length()>0) {
			throw new IllegalStateException(
					"Please call setLabelY() only once per histogram.");
		}
		labelY = label;
	}

	/**
	 * Sets the number of this histogram. May have the side effect of bumping
	 * another histogram with the desired number to a new number.
	 * 
	 * @param num
	 *            the desired number for the histogram
	 */
	public void setNumber(final int num) {
		final Integer newKey = new Integer(num);
		if (NUMBER_MAP.containsKey(newKey)) {
			final Histogram collider = NUMBER_MAP.get(newKey);
			if (!collider.equals(this)) {
				collider.assignNewNumber();
			}
		}
		NUMBER_MAP.remove(new Integer(number));
		number = num;
		NUMBER_MAP.put(new Integer(num), this);
	}

	/**
	 * Zeroes all the counts in this histogram.
	 */
	public abstract void setZero();

	/**
	 * Gives the name of this histogram.
	 * 
	 * @return its name
	 */
	public String toString() {
		return uniqueName;
	}

	/* Create the full histogram name with group name. */
	final void updateNames(final Group group) {
		final StringUtilities stringUtil = StringUtilities.getInstance();
		groupName = group.getName();
		NAME_MAP.remove(uniqueName);
		uniqueName = stringUtil.makeFullName(groupName, name);
		NAME_MAP.put(uniqueName, this);
	}
}