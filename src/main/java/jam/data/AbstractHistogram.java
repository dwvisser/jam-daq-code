package jam.data;

import injection.GuiceInjector;
import jam.global.Nameable;
import jam.util.StringUtilities;

import java.util.*;

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
 * @author Ken Swartz
 * @author Dale Visser
 * @version 0.5, 1.0
 * @since JDK 1.1
 */
public abstract class AbstractHistogram implements DataElement {

    private final static List<List<AbstractHistogram>> DIM_LIST = new ArrayList<>(
            2);

    private static final String EMPTY_STRING = "";

    /* histogramList is ordered by the creation of the histograms */
    private final static List<AbstractHistogram> LIST = new ArrayList<>();

    /**
     * Maximum number of characters in the histogram name.
     */
    public static final int NAME_LENGTH = 20;

    private final static Map<String, AbstractHistogram> NAME_MAP = new HashMap<>();

    private final static SortedMap<Integer, AbstractHistogram> NUMBER_MAP = new TreeMap<>();

    /**
     * default axis labels
     */
    private static final String X_LABEL = "Channels";

    private static final String Y_LABEL_1D = "Counts";

    private static final String Y_LABEL_2D = "Channels";

    static {
        DIM_LIST.add(0, new ArrayList<>());
        DIM_LIST.add(1, new ArrayList<>());
    }

    private transient final GateCollection gates;

    /**
     * Clears the list of histograms.
     */
    public static void clearList() {
        for (AbstractHistogram his : LIST) {
            his.clearInfo();
        }
        LIST.clear();
        NAME_MAP.clear();
        NUMBER_MAP.clear();
        for (List<AbstractHistogram> list : DIM_LIST) {
            list.clear();
        }
    }

    /**
     * Get the histogram with the given number.
     * @param num
     *            of the histogram
     * @return the histogram, if it exists, null otherwise
     */
    public static AbstractHistogram getHistogram(final int num) {
        return NUMBER_MAP.get(num);
    }

    /**
     * Get the histogram with the given name.
     * @param name
     *            name of histogram to retrieve
     * @return the histogram with the given name, null if name doesn't exist.
     */
    public static AbstractHistogram getHistogram(final String name) {
        final AbstractHistogram rval = name == null ? null
                : NAME_MAP.get(name);
        return rval;
    }

    /**
     * Returns the list of all histograms.
     * @return all histograms
     */
    public static List<AbstractHistogram> getHistogramList() {
        return Collections.unmodifiableList(LIST);
    }

    /**
     * @param dim
     *            1 or 2
     * @return list of all histograms with the given dimensionality
     */
    public static List<AbstractHistogram> getHistogramList(final int dim) {
        if (dim < 1 || dim > 2) {
            throw new IllegalArgumentException(
                    "Expect 1 or 2, the possible numbers of dimensions.");
        }
        return Collections.unmodifiableList(DIM_LIST.get(dim - 1));
    }

    /**
     * @param names
     *            of histograms we want
     * @param <T>
     *            most general type of histogram to include in the list
     * @param type
     *            how the type gets specified
     * @return all histograms that have names matching the given list
     */
    public static <T extends AbstractHistogram> List<T> getHistogramList(
            final List<String> names, final Class<T> type) {
        final List<T> result = new ArrayList<>();
        for (String name : names) {
            if (NAME_MAP.containsKey(name)) {
                final AbstractHistogram hist = getHistogram(name);
                if (type.isInstance(hist)) {
                    result.add(type.cast(hist));
                }
            } else {
                throw new IllegalArgumentException('\"' + name
                        + "\" is not of type " + type.getName());
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * @return list of all histograms sorted by number
     */
    public static Collection<AbstractHistogram> getListSortedByNumber() {
        return Collections.unmodifiableCollection(NUMBER_MAP.values());
    }

    /**
     * Histogram is a valid histogram
     * @param hist histogram to check
     * @return <code>true</code> if this histogram remains in the name mapping
     */
    public static boolean isValid(final AbstractHistogram hist) {
        return NAME_MAP.containsValue(hist);
    }

    /**
     * Sets the list of histograms, used for remote loading of histograms.
     * @param inHistList
     *            must contain all histogram objects
     */
    public static void setHistogramList(
            final List<AbstractHistogram> inHistList) {
        clearList();
        for (AbstractHistogram hist : inHistList) {
            NAME_MAP.put(hist.getFullName(), hist);
            LIST.add(hist);
            NUMBER_MAP.put(hist.getNumber(), hist);
        }
    }

    /**
     * Calls setZero() on all histograms.
     * @see #setZero()
     */
    public static void setZeroAll() {
        synchronized (AbstractHistogram.class) {
            for (AbstractHistogram histogram : getHistogramList()) {
                histogram.setZero();
            }
        }
    }

    /**
     * whether this histogram has been cleared to an unusable state
     */
    protected transient boolean clear = false;

    /** Name of group histogram belongs to */
    private transient String groupName;

    private String labelX = ""; // x axis label

    // end of constructors

    private String labelY = ""; // y axis label

    /** abbreviation to refer to histogram */
    private transient String name;

    /** Number of histogram */
    private int number;

    private transient final int sizeX; // size of histogram, for 1d size for

    private transient final int sizeY; // size used for 2d histograms y size

    /** title of histogram */
    private transient String title;

    private transient final jam.data.HistogramType type; // one or two dimension

    /** unique name amongst all histograms */
    private transient String uniqueName;

    /**
     * Master constructor invoked by all other constructors.
     * @param type
     *            type and dimensionality of data
     * @param sizeX
     *            number of channels in x-axis
     * @param sizeY
     *            number of channels in y-axis
     * @param title
     *            lengthier title of histogram, displayed on plot
     * @see #NAME_LENGTH
     * @see jam.data.HistogramType
     * @throws IllegalArgumentException
     *             if an unknown histogram type is given
     */
    protected AbstractHistogram(final jam.data.HistogramType type,
            final int sizeX, final int sizeY, final String title) {
        super();
        this.type = type;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.title = title;
        gates = new GateCollection(type.getDimensionality());
        assignNewNumber();
        /* allow memory for gates and define sizes */
        final boolean oneD = type.getDimensionality() == 1;
        if (labelX == null) {
            labelX = X_LABEL;
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
    protected AbstractHistogram(final jam.data.HistogramType type,
            final int sizeX, final int sizeY, final String title,
            final String axisLabelX, final String axisLabelY) {
        this(type, sizeX, sizeY, title);
        setLabelX(axisLabelX);
        setLabelY(axisLabelY);
    }

    /**
     * Constructor with no number given, but axis labels are given.
     * @param type
     *            dimensionality of histogram, 1 or 2
     * @param size
     *            number of channels, all 2d histograms have square dimensions
     * @param title
     *            lengthier title of histogram, displayed on plot
     * @throws IllegalArgumentException
     *             if an unknown histogram type is given
     */
    protected AbstractHistogram(final jam.data.HistogramType type,
            final int size, final String title) {
        this(type, size, size, title);
    }

    /**
     * Contructor with no number given, but axis labels are given.
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
    protected AbstractHistogram(final jam.data.HistogramType type,
            final int size, final String title, final String axisLabelX,
            final String axisLabelY) {
        this(type, size, size, title);
        setLabelX(axisLabelX);
        setLabelY(axisLabelY);
    }

    /* instantized methods */

    /**
     * Adds the given counts to this histogram.
     * @param countsIn
     *            1d or 2d array of <code>int</code>'s or <code>double</code> 
     *            's, according to this histogram's type
     * @throws IllegalArgumentException
     *             if the parameter is the wrong type
     */
    public abstract void addCounts(Object countsIn);

    private void assignNewNumber() {
        final int last = NUMBER_MAP.isEmpty() ? 0 : NUMBER_MAP.lastKey();
        number = last + 1;
        NUMBER_MAP.put(number, this);
    }

    protected abstract void clearCounts();

    private void clearInfo() {
        gates.clear();
        labelX = EMPTY_STRING;
        labelY = EMPTY_STRING;
        title = EMPTY_STRING;
        clearCounts();
    }

    /**
     * Returns the total number of counts in the histogram.
     * @return area under the counts in the histogram
     */
    public abstract double getArea();

    public abstract double getCount();

    /**
     * Returns the number of dimensions in this histogram.
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
     * @return the name of this histogram
     */
    public String getFullName() {
        return uniqueName;
    }

    /**
     * Get the group this histograms belongs to.
     * @return the Group
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Returns the X-axis label
     * @return the X-axis label
     */
    public String getLabelX() {
        return labelX;
    }

    /**
     * Returns the Y-axis label
     * @return the Y-axis label
     */
    public String getLabelY() {
        return labelY;
    }

    /**
     * Returns the histogram name.
     * @return the name of this histogram
     */
    public String getName() {
        return name;
    }

    protected final void setName(final String value) {
        name = value;
    }

    /**
     * Returns the number of the histogram, mostly used for export to ORNL
     * files. Histograms should always be assigned unique numbers.
     * @return the number of this histogram
     */
    public final int getNumber() {
        return number;
    }

    /* -- set methods */

    /**
     * Get size of x-dimension, or the only dimension for 1-d histograms.
     * @return the size of the x-dimension
     */
    public int getSizeX() {
        return sizeX;
    }

    /**
     * Get size of y-dimension, or the zero for 1-d histograms.
     * @return the size of the y-dimension
     */
    public int getSizeY() {
        return sizeY;
    }

    /**
     * Returns the histogram title.
     * @return the title of this histogram
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the type of this histogram type. This can be:
     * <ul>
     * <li><code>ONE_DIM_INT</code></li>
     * <li><code>TWO_DIM_INT</code></li>
     * <li><code>ONE_DIM_DOUBLE</code></li>
     *  <li><code>TWO_DIM_DOUBLE</code></li>
     * </ul>
     * @return the type
     * @see jam.data.HistogramType
     */
    public jam.data.HistogramType getType() {
        return type;
    }

    /**
     * @return whether clearCounts() has been called on this histogram
     */
    public boolean isClear() {
        synchronized (this) {
            return clear;
        }
    }

    /**
     * @return the gates associated with this histogram
     */
    public GateCollection getGateCollection() {
        return this.gates;
    }

    /**
     * Set the counts array using the given <code>int</code> or
     * <code>double</code> array.
     * @param countsIn
     *            1d or 2d array of <code>int</code>'s or <code>double</code> 's
     * @throws IllegalArgumentException
     *             if countsIn is the wrong type.
     */
    public abstract void setCounts(Object countsIn);

    /**
     * Sets the X-axis label
     * @param label
     *            new label for X-axis
     * @throws IllegalStateException
     *             if x-axis label has already been explicitly set
     */
    public final void setLabelX(final String label) {
        if (labelX.length() > 0) {
            throw new IllegalStateException(
                    "Please call setLabelX() only once per histogram.");
        }
        labelX = label;
    }

    /**
     * Sets the Y-axis label
     * @param label
     *            new label for Y-axis
     * @throws IllegalStateException
     *             if y-axis label has already been explicitly set
     */
    public final void setLabelY(final String label) {
        if (labelY.length() > 0) {
            throw new IllegalStateException(
                    "Please call setLabelY() only once per histogram.");
        }
        labelY = label;
    }

    /**
     * Sets the number of this histogram. May have the side effect of bumping
     * another histogram with the desired number to a new number.
     * @param num
     *            the desired number for the histogram
     */
    public void setNumber(final int num) {
        if (NUMBER_MAP.containsKey(num)) {
            final AbstractHistogram collider = NUMBER_MAP.get(num);
            if (!collider.equals(this)) {
                collider.assignNewNumber();
            }
        }
        NUMBER_MAP.remove(number);
        number = num;
        NUMBER_MAP.put(num, this);
    }

    /**
     * Zeroes all the counts in this histogram.
     */
    public abstract void setZero();

    /**
     * Gives the name of this histogram.
     * @return its name
     */
    @Override
    public String toString() {
        return uniqueName;
    }

    /* Create the full histogram name with group name. */
    protected final void updateNames(final Nameable group) {
        groupName = group.getName();
        NAME_MAP.remove(uniqueName);
        uniqueName = GuiceInjector.getObjectInstance(StringUtilities.class)
                .makeFullName(groupName, name);
        NAME_MAP.put(uniqueName, this);
    }

    /**
     * Clear this histogram's data and delete it from all lists that refer to
     * it.
     */
    public void delete() {
        clearInfo();
        LIST.remove(this);
        NAME_MAP.remove(getFullName());
        NUMBER_MAP.remove(getNumber());
        final List<AbstractHistogram> dimList = DIM_LIST
                .get(getDimensionality() - 1);
        dimList.remove(this);
    }
}