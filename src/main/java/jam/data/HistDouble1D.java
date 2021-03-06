/*
 * Created on Nov 29, 2004
 */
package jam.data;

import jam.data.func.CalibrationFunctionCollection;

import java.util.Arrays;

/**
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale W Visser </a>
 */
public final class HistDouble1D extends AbstractHist1D {

	private transient double[] countsDouble;

	HistDouble1D(final String title, final String axisLabelX,
			final String axisLabelY, final double[] countsIn) {
		super(jam.data.HistogramType.ONE_D_DOUBLE, countsIn.length, title, axisLabelX,
				axisLabelY);
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
	@Override
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
	@Override
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
	@Override
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
	@Override
	protected void clearCounts() {
		synchronized (this) {
			countsDouble = EMPTY;
			unsetErrors();
			setCalibration(CalibrationFunctionCollection.NO_CALIBRATION);
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

	@Override
	protected void getCounts(final double[] array) {
		synchronized (this) {
			final int max = Math.min(countsDouble.length, array.length);
			System.arraycopy(countsDouble, 0, array, 0, max);
		}
	}

	@Override
	public double getCount() {
		return getArea();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.data.Histogram#setZero()
	 */
	@Override
	public void setZero() {
		Arrays.fill(countsDouble, 0);
		unsetErrors();
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
        double[] doubles = (double[]) countsIn;
        System.arraycopy(doubles, 0, countsDouble, 0, Math.min(doubles.length,
				getSizeX()));
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
	@Override
	public double getArea() {
		final int size = getSizeX();
		double sum = 0.0;
		for (int i = 0; i < size; i++) {
			sum += countsDouble[i];
		}
		return sum;
	}

}