/*
 * Created on Nov 29, 2004
 */
package jam.data;

import java.util.Arrays;

/**
 * The 2-dimensional histogram class to use for online and offline sorting.
 * 
 * @author <a href="mailto:dale@visser.name">Dale W Visser </a>
 */
public final class HistInt2D extends AbstractHist2D {

	private transient int counts2d[][]; // array to hold counts for 2d inc

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
	HistInt2D(String name, String title, int[][] countsIn) {
		super(name, Type.TWO_DIM_INT, countsIn.length, countsIn[0].length,
				title);
		initCounts(countsIn);
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
	HistInt2D(String name, String title, String axisLabelX, String axisLabelY,
			int[][] countsIn) {
		super(name, Type.TWO_DIM_INT, countsIn.length, countsIn[0].length,
				title, axisLabelX, axisLabelY);
		initCounts(countsIn);
	}
	
	private void initCounts(int [][] countsIn){
		counts2d=new int[getSizeX()][getSizeY()];
		for (int i = 0; i < countsIn.length; i++) { //copy arrays
			System
					.arraycopy(countsIn[i], 0, counts2d[i], 0,
							countsIn[0].length);
		}
	}

	/**
	 * Returns the number of counts in the given channel.
	 * 
	 * @param chX x-channel that we are interested in
	 * @param chY y-channel that we are interested in
	 * @return number of counts
	 */
	public double getCounts(int chX, int chY) {
		return counts2d[chX][chY];
	}

	/**
	 * Sets the counts in the given channel to the specified
	 * number of counts.
	 * 
	 * @param chX x-coordinate of the bin
	 * @param chY y-coordinate of the bin
	 * @param counts to be in the channel, rounded to <code>int</code>, if
	 * necessary
	 */
	public void setCounts(int chX, int chY, double counts) {
		counts2d[chX][chY] = (int) Math.round(counts);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.data.Histogram#clearCounts()
	 */
	void clearCounts() {
		counts2d = null;
	}

	/**
	 * Returns the counts in the histogram as an array of the appropriate type.
	 * It is necessary to cast the returned array with <code>(int [][])</code>.
	 * 
	 * @return <code>int [][]</code>
	 */
	public Object getCounts() {
		return counts2d;
	}

	/**
	 * Zeroes all the counts in this histogram.
	 */
	public void setZero() {
		final int size=getSizeX();
		for (int i = 0; i < size; i++) {
			Arrays.fill(counts2d[i],0);
		}
	}

	/**
	 * Set the counts array using the given <code>int [][]</code>.
	 * 
	 * @param countsIn
	 *            <code>int [][]</code>
	 * @throws IllegalArgumentException
	 *             if countsIn is the wrong type.
	 */
	public void setCounts(Object countsIn) {
		if (Type.getArrayType(countsIn) != getType()) {
			throw new IllegalArgumentException("Expected array for type "
					+ getType());
		}
		setCounts((int[][]) countsIn);
	}

	/**
	 * Adds the given counts to this histogram.
	 * 
	 * @param countsIn
	 *            <code>int [][]</code>'s
	 * @throws IllegalArgumentException
	 *             if the parameter is the wrong type
	 */
	public void addCounts(Object countsIn) {
		if (Type.getArrayType(countsIn) != getType()) {
			throw new IllegalArgumentException("Expected array for type "
					+ getType());
		}
		addCounts((int[][]) countsIn);
	}

	/**
	 * Returns the total number of counts in the histogram.
	 * 
	 * @return area under the counts in the histogram
	 */
	public double getArea(){
		final int size=getSizeX();
		double sum=0.0;
		for (int i = 0; i < size; i++) {
			final int sizeY=getSizeY();
			for (int j = 0; j < sizeY; j++) {
				sum += counts2d[i][j];
			}
		}
		return sum;
	}

	private synchronized void setCounts(int[][] countsIn) {
		final int loopLen = Math.min(counts2d.length, countsIn.length);
		for (int i = 0; i < loopLen; i++) {
			System.arraycopy(countsIn[i], 0, counts2d[i], 0, Math.min(
					countsIn[i].length, counts2d[i].length));
		}
	}

	private synchronized void addCounts(int[][] countsIn) {
		final int maxX = Math.min(getSizeX(), countsIn.length) - 1;
		final int maxY = Math.min(getSizeY(), countsIn[0].length) - 1;
		for (int x = maxX; x >= 0; x--) {
			for (int y = maxY; y >= 0; y--) {
				counts2d[x][y] += countsIn[x][y];
			}
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
		final int size=getSizeX();
		int incX = dataWordX;
		int incY = dataWordY;
		//check for overflow and underflow
		if (incX >= size) {
			incX = size - 1;
		} else if (incX < 0) {
			incX = 0;
		}
		final int sizeY=getSizeY();
		if (incY >= sizeY) {
			incY = sizeY - 1;
		} else if (dataWordY < 0) {
			incY = 0;
		}
		synchronized (this) {
			counts2d[incX][incY]++;
		}
	}

}