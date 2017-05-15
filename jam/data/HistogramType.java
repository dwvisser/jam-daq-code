package jam.data;

/**
 * Encapsulates the 4 different types a histogram may have.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale W Visser </a>
 */
public final class HistogramType {
	private final static int[] DIM = { 1, 2, 1, 2 };

	private final static boolean[] INT = { true, true, false, false };

	/**
	 * Histogram dimensionality compare to <code>getDimensionality()</code>
	 */
	public final static int ONE_D = 1;

	/**
	 * Value of histogram type for one dimensional <code>double</code>
	 * histograms.
	 */
	public static final HistogramType ONE_D_DOUBLE = new HistogramType(2);

	/**
	 * Value of histogram type for one dimensional <code>int</code> histograms.
	 */
	public static final HistogramType ONE_DIM_INT = new HistogramType(0);

	private final static String[] STRING = { "1D int", "2D int", "1D double",
			"2D double" };

	/**
	 * Histogram dimensionality compare to <code>getDimensionality()</code>
	 */
	public final static int TWO_D = 2;

	/**
	 * Value of histogram type for two dimensional <code>double</code>
	 * histograms.
	 */
	public static final HistogramType TWO_D_DOUBLE = new HistogramType(3);

	/**
	 * Value of histogram type for two dimensional <code>int</code> histograms.
	 */
	public static final HistogramType TWO_DIM_INT = new HistogramType(1);

	/**
	 * Gives the counts array type of the given object.
	 * 
	 * @param array
	 *            a 1-d or 2-d int or double array
	 * @return which type the array corresponds to
	 */
	protected static HistogramType getArrayType(final Object array) {
		final HistogramType rval;
		final String error = "You may pass int or double arrays of up to two dimensions as histogram counts.";
		final Class<?> type = array.getClass();
		if (!type.isArray()) {
			throw new IllegalArgumentException(error);
		}
		final Class<?> componentA = type.getComponentType();
		if (componentA.equals(int.class)) {
			rval = HistogramType.ONE_DIM_INT;
		} else if (componentA.equals(double.class)) {
			rval = HistogramType.ONE_D_DOUBLE;
		} else {
			/* Two-D, componentA assumed to be array. */
			final Class<?> componentB = componentA.getComponentType();
			if (componentB.equals(int.class)) {
				rval = HistogramType.TWO_DIM_INT;
			} else if (componentB.equals(double.class)) {
				rval = HistogramType.TWO_D_DOUBLE;
			} else {
				throw new IllegalArgumentException(error);
			}
		}
		return rval;
	}

	private transient final int typeNum;

	private HistogramType(final int num) {
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
			rval = isInteger() ? new int[sizeX]
					: new double[sizeX];
		} else {
			rval = isInteger() ? new int[sizeX][sizeY]
					: new double[sizeX][sizeY];
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
	@Override
	public String toString() {
		return STRING[typeNum];
	}
}