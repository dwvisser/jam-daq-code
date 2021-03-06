/*
 * Created on Nov 29, 2004
 */
package jam.data;

import java.util.Arrays;

/**
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale W Visser</a>
 */
public final class HistDouble2D extends AbstractHist2D {

	/* array to hold counts for 2d double */
	private transient double[][] counts2dD;

	HistDouble2D(final String title, final String axisLabelX,
			final String axisLabelY, final double[][] countsIn) {
		super(jam.data.HistogramType.TWO_D_DOUBLE, countsIn.length, countsIn[0].length,
				title, axisLabelX, axisLabelY);
		initCounts(countsIn);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.data.Histogram#addCounts(java.lang.Object)
	 */
	@Override
	public void addCounts(final Object countsIn) {
		if (jam.data.HistogramType.getArrayType(countsIn) != getType()) {
			throw new IllegalArgumentException("Expected array for type "
					+ getType());
		}
		addCountsArray((double[][]) countsIn);
	}

	private void addCountsArray(final double[][] countsIn) {
		synchronized (this) {
			final int len = countsIn.length;
			double[][] temp = new double[len][];
			final int maxX = Math.min(getSizeX(), len) - 1;
			final int maxY = Math.min(getSizeY(), countsIn[0].length) - 1;
			for (int i = 0; i <= maxX; i++) {
				temp[i] = countsIn[i].clone();
			}
			for (int x = maxX; x >= 0; x--) {
				for (int y = maxY; y >= 0; y--) {
					counts2dD[x][y] += temp[x][y];
				}
			}
		}
	}

	private static final double[][] EMPTY = new double[0][0];

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.data.Histogram#clearCounts()
	 */
	@Override
	protected void clearCounts() {
		synchronized (this) {
			counts2dD = EMPTY;
			clear = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.data.Histogram#getArea()
	 */
	@Override
	public double getArea() {
		final int size = getSizeX();
		double sum = 0.0;
		for (int i = 0; i < size; i++) {
			final int sizeY = getSizeY();
			for (int j = 0; j < sizeY; j++) {
				sum += counts2dD[i][j];
			}
		}
		return sum;
	}

	@Override
	public double getCount() {
		return getArea();
	}

	/**
	 * @return a copy of this histograms counts.
	 */
	public double[][] getCounts() {
		synchronized (this) {
			final int len = counts2dD.length;
			double[][] rval = new double[len][];
			for (int i = 0; i < len; i++) {
				rval[i] = counts2dD[i].clone();
			}
			return rval;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.data.AbstractHist2D#getCounts(int, int)
	 */
	@Override
	public double getCounts(final int chX, final int chY) {
		return counts2dD[chX][chY];
	}

	private void initCounts(final double[][] countsIn) {
		counts2dD = new double[getSizeX()][getSizeY()];
		for (int i = 0; i < countsIn.length; i++) { // copy arrays
			System.arraycopy(countsIn[i], 0, counts2dD[i], 0,
					countsIn[0].length);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.data.AbstractHist2D#setCounts(int, int, double)
	 */
	@Override
	public void setCounts(final int chX, final int chY, final double counts) {
		counts2dD[chX][chY] = counts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.data.Histogram#setCounts(java.lang.Object)
	 */
	@Override
	public void setCounts(final Object countsIn) {
		if (jam.data.HistogramType.getArrayType(countsIn) != getType()) {
			throw new IllegalArgumentException("Expected array for type "
					+ getType());
		}
		setCountsArray((double[][]) countsIn);
	}

	private void setCountsArray(final double[][] countsIn) {
		synchronized (this) {
			final int loopLen = Math.min(countsIn.length, counts2dD.length);
			for (int i = 0; i < loopLen; i++) {
				System.arraycopy(countsIn[i], 0, counts2dD[i], 0, Math.min(
						countsIn[i].length, counts2dD[i].length));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.data.Histogram#setZero()
	 */
	@Override
	public void setZero() {
		final int size = getSizeX();
		for (int i = 0; i < size; i++) {
			Arrays.fill(counts2dD[i], 0);
		}
	}

}
