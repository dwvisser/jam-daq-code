/*
 * Created on Nov 29, 2004
 */
package jam.data;

import jam.data.func.AbstractCalibrationFunction;

import java.util.Arrays;

/**
 * @author <a href="mailto:dale@visser.name">Dale W Visser </a>
 */
public final class HistDouble1D extends AbstractHist1D {

	private transient double[] countsDouble;

	/**
	 * Given an array of counts, create a new 1-d <code>Histogram</code> and
	 * give it a number.
	 * 
	 * @param group
	 *            for this histogram to belong to
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
	HistDouble1D(String title, double[] countsIn) {
		super(Type.ONE_D_DOUBLE, countsIn.length, title);
		initCounts(countsIn);
	}

	HistDouble1D(String title, String axisLabelX, String axisLabelY,
			double[] countsIn) {
		super(Type.ONE_D_DOUBLE, countsIn.length, title, axisLabelX, axisLabelY);
		initCounts(countsIn);
	}

	private void initCounts(final double[] countsIn) {
		countsDouble = new double[getSizeX()];
		System.arraycopy(countsIn, 0, countsDouble, 0, countsIn.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.data.AbstractHist1D#getCounts(int)
	 */
	public double getCounts(final int channel) {
		synchronized (this) {
			return countsDouble[channel];
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.data.AbstractHist1D#setCounts(int, double)
	 */
	public void setCounts(final int channel, final double counts) {
		synchronized (this) {
			countsDouble[channel] = counts;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.data.AbstractHist1D#getErrors()
	 */
	public double[] getErrors() {
		synchronized (this) {
			final int length = countsDouble.length;
			if (!hasErrorsSet()) {
				errors = new double[length];
				for (int i = 0; i < length; i++) {
					if (countsDouble[i] <= 0.0) {
						/* set errors according to Poisson with error = 1 */
						errors[i] = 1.0;
					} else {
						errors[i] = Math.sqrt(countsDouble[i]);
					}
				}
			}
			return errors;
		}
	}

	private static final double[] EMPTY = new double[0];

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.data.Histogram#clearCounts()
	 */
	void clearCounts() {
		synchronized (this) {
			countsDouble = EMPTY;
			unsetErrors();
			setCalibration(AbstractCalibrationFunction.getNoCalibration());
			clear = true;
		}
	}

	/**
	 * @return a copy of this histogram's counts
	 */
	public double[] getCounts() {
		synchronized (this) {
			return countsDouble.clone();
		}
	}

	public double getCount() {
		return getArea();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.data.Histogram#setZero()
	 */
	public void setZero() {
		Arrays.fill(countsDouble, 0);
		unsetErrors();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.data.Histogram#setCounts(java.lang.Object)
	 */
	public void setCounts(final Object countsIn) {
		if (Type.getArrayType(countsIn) != getType()) {
			throw new IllegalArgumentException("Expected array for type "
					+ getType());
		}
		final int inLength = ((double[]) countsIn).length;
		System.arraycopy(countsIn, 0, countsDouble, 0, Math.min(inLength,
				getSizeX()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.data.Histogram#addCounts(java.lang.Object)
	 */
	public void addCounts(final Object countsIn) {
		if (Type.getArrayType(countsIn) != getType()) {
			throw new IllegalArgumentException("Expected array for type "
					+ getType());
		}
		addCounts((double[]) countsIn);
	}

	private void addCounts(final double[] countsIn) {
		synchronized (this) {
			final double[] temp = countsIn.clone();
			final int max = Math.min(temp.length, getSizeX()) - 1;
			for (int i = max; i >= 0; i--) {
				countsDouble[i] += temp[i];
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.data.Histogram#getArea()
	 */
	public double getArea() {
		final int size = getSizeX();
		double sum = 0.0;
		for (int i = 0; i < size; i++) {
			sum += countsDouble[i];
		}
		return sum;
	}

}