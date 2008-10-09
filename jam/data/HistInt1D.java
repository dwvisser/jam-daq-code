/*
 * Created on Nov 26, 2004
 */
package jam.data;

import jam.data.func.AbstractCalibrationFunctionCollection;

import java.util.Arrays;

/**
 * The 1-dimensional histogram class to use for online and offline sorting.
 * 
 * @author <a href="mailto:dale@visser.name">Dale W Visser </a>
 */
public final class HistInt1D extends AbstractHist1D {
	private static final int[] EMPTY_INT = new int[0];

	private int counts[]; // array to hold counts for 1d int

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
	 * @param group
	 *            that this histogram belongs to
	 */
	HistInt1D(final String title, final String axisLabelX,
			final String axisLabelY, final int[] countsIn) {
		super(Type.ONE_DIM_INT, countsIn.length, title, axisLabelX, axisLabelY);
		initCounts(countsIn);
	}

	/**
	 * Adds the given counts to this histogram.
	 * 
	 * @param add
	 *            1d array of <code>int</code>'s
	 * @throws IllegalArgumentException
	 *             if the parameter is the wrong type
	 */
	@Override
	public void addCounts(final Object add) {
		if (Type.getArrayType(add) != getType()) {
			throw new IllegalArgumentException("Expected array for type "
					+ getType());
		}
		addCountsArray((int[]) add);
	}

	private void addCountsArray(final int[] countsIn) {
		synchronized (this) {
			final int[] temp = countsIn.clone();
			final int max = Math.min(countsIn.length, getSizeX()) - 1;
			for (int i = max; i >= 0; i--) {
				counts[i] += temp[i];
			}
		}
	}

	@Override
	void clearCounts() {
		synchronized (this) {
			counts = EMPTY_INT;
			unsetErrors();
			setCalibration(AbstractCalibrationFunctionCollection.NO_CALIBRATION);
			clear = true;
		}
	}

	/**
	 * Returns the total number of counts in the histogram.
	 * 
	 * @return area under the counts in the histogram
	 */
	@Override
	public double getArea() {
		synchronized (this) {
			final int size = getSizeX();
			double sum = 0.0;
			for (int i = 0; i < size; i++) {
				sum += counts[i];
			}
			return sum;
		}
	}

	@Override
	public double getCount() {
		return getArea();
	}

	/**
	 * Returns the counts in the histogram as an array of the appropriate type.
	 * It is necessary to cast the returned array with <code>(int [])</code>.
	 * 
	 * @return <code>int []</code>
	 */
	public int[] getCounts() {
		synchronized (this) {
			return counts.clone();
		}
	}

	@Override
	protected void getCounts(final double[] array) {
		synchronized (this) {
			final int max = Math.min(array.length, counts.length);
			for (int i = 0; i < max; i++) {// NOPMD
				array[i] = counts[i];
			}
		}
	}

	/**
	 * Returns the number of counts in the given channel.
	 * 
	 * @param channel
	 *            that we are interested in
	 * @return number of counts
	 */
	@Override
	public double getCounts(final int channel) {
		synchronized (this) {
			return counts[channel];
		}
	}

	/**
	 * Returns the array of error bars, possibly estimated.
	 * 
	 * @return 1-sigma error bars
	 */
	@Override
	public double[] getErrors() {
		synchronized (this) {
			final int length = counts.length;
			if (errors == null) {
				errors = new double[length];
				for (int i = 0; i < length; i++) {
					if (counts[i] == 0) {
						/* set errors according to Poisson with error = 1 */
						errors[i] = 1.0;
					} else {
						errors[i] = Math.sqrt(counts[i]);
					}
				}
			}
			return errors;
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
	public void inc(final int dataWord) {
		final int size = getSizeX();
		int incCh = dataWord;
		/* check for overflow */
		if (incCh >= size) {
			incCh = size - 1;
		} else if (dataWord < 0) {
			incCh = 0;
		}
		synchronized (this) {
			counts[incCh]++;
		}
	}

	private void initCounts(final int[] countsIn) {
		counts = new int[getSizeX()];
		System.arraycopy(countsIn, 0, counts, 0, countsIn.length);
	}

	/**
	 * Sets the counts in the given channel to the specified number of counts.
	 * 
	 * @param channel
	 *            to change
	 * @param count
	 *            to be in the channel, rounded to <code>int</code>, if
	 *            necessary
	 */
	@Override
	public void setCounts(final int channel, final double count) {
		synchronized (this) {
			counts[channel] = (int) Math.round(count);
		}
	}

	/**
	 * Set the counts array using the given <code>int []</code>.
	 * 
	 * @param countsIn
	 *            <code>int []</code>
	 * @throws IllegalArgumentException
	 *             if countsIn is the wrong type.
	 */
	@Override
	public void setCounts(final Object countsIn) {
		if (Type.getArrayType(countsIn) != getType()) {
			throw new IllegalArgumentException("Expected array for type "
					+ getType());
		}
		final int inLength = ((int[]) countsIn).length;
		synchronized (this) {
			System.arraycopy(countsIn, 0, counts, 0, Math.min(inLength,
					getSizeX()));
		}
	}

	/**
	 * Zeroes all the counts in this histogram.
	 */
	@Override
	public void setZero() {
		synchronized (this) {
			Arrays.fill(counts, 0);
			unsetErrors();
		}
	}

}