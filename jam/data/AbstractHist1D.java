/*
 * Created on Nov 26, 2004
 */
package jam.data;

import jam.data.func.AbstractCalibrationFunction;
import jam.data.peaks.PeakFinder;

import java.util.List;

/**
 * The superclass of all 1-dimensional histograms.
 * 
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 */
public abstract class AbstractHist1D extends Histogram {

	/**
	 * The calibration function. Set to <code>null</code> if there is none.
	 */
	protected transient AbstractCalibrationFunction calibFunc=AbstractCalibrationFunction.getNoCalibration();

	/**
	 * Array which contains the errors in the channel counts.
	 */
	protected double[] errors;

	/**
	 * Set to true if errors are set explicitly. Put in place so as not to waste
	 * disk space on saved files. IO routines should only write out error bars
	 * when this is set to true (checked by calling <code>errorsSet()</code>.
	 * Otherwise, Poisson errors should be assumed by other software.
	 * 
	 * @see #hasErrorsSet()
	 */
	protected transient boolean errorsSet;

	AbstractHist1D(Group group, String name, Type type, int len, String title,
			String axisLabelX, String axisLabelY) {
		super(group, name, type, len, title, axisLabelX, axisLabelY);
		unsetErrors();
	}

	AbstractHist1D(Group group, String name, Type type, int len, String title) {
		super(group, name, type, len, title);
		unsetErrors();
	}

	private static final double[] EMPTY_DOUBLE = null;

	/**
	 * Make it so no error bars are <em>explicitly</em> defined for this
	 * histogram.
	 */
	protected final void unsetErrors() {
		synchronized (this) {
			errors = EMPTY_DOUBLE;
			errorsSet = false;
		}
	}

	/**
	 * Returns the number of counts in the given channel.
	 * 
	 * @param channel
	 *            that we are interested in
	 * @return number of counts
	 */
	public abstract double getCounts(int channel);

	/**
	 * Sets the counts in the given channel to the specified number of counts.
	 * 
	 * @param channel
	 *            to change
	 * @param counts
	 *            to be in the channel, rounded to <code>int</code>, if
	 *            necessary
	 */
	public abstract void setCounts(int channel, double counts);

	/**
	 * Returns the array of error bars, possibly estimated.
	 * 
	 * @return 1-sigma error bars
	 */
	public abstract double[] getErrors();

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
	public double[][] findPeaks(final double sensitivity, final double width,
			final boolean cal) {
		synchronized (this) {
			double[] histArray;
			if (getType() == Type.ONE_D_DOUBLE) {
				histArray = ((HistDouble1D)this).getCounts();
			} else if (getType() == Type.ONE_DIM_INT) { // INT type
				final int[] temp = ((HistInt1D)this).getCounts();
				histArray = new double[temp.length];
				for (int i = 0; i < temp.length; i++) {
					histArray[i] = temp[i];
				}
			} else { // 2D
				throw new UnsupportedOperationException(
						"findPeaks() called on 2D hist");
			}
			final List<Double> posn = PeakFinder.getInstance().getCentroids(
					histArray, sensitivity, width);
			double[][] rval = new double[3][posn.size()];
			if (cal && this.isCalibrated()) {
				for (int i = 0; i < posn.size(); i++) {
					rval[0][i] = posn.get(i);
					rval[1][i] = calibFunc.getValue(posn.get(i));
					rval[2][i] = histArray[(int) Math.round(posn.get(i))];
				}
			} else { // no calibration
				for (int i = 0; i < posn.size(); i++) {
					rval[0][i] = posn.get(i);
					rval[1][i] = posn.get(i);
					rval[2][i] = histArray[(int) Math.round(posn.get(i))];
				}
			}
			return rval;
		}
	}

	/**
	 * By default, the class will assume Poisson error bars and return square
	 * root of counts. For <code>AbstractHist1D</code>'s produced by adding,
	 * subtracting, or otherwise manipulating other histograms, though, an
	 * appropriate error array should be calculated and stored by invoking the
	 * <code>setErrors()</code> method.
	 * 
	 * @param errs
	 *            array representing the 1-sigma erro bars for the channel
	 *            counts
	 */
	public final void setErrors(final double[] errs) {
		synchronized (this) {
			final int size = getSizeX();
			if (!hasErrorsSet()) {
				errors = new double[size];
			}
			final int max = Math.min(size, errs.length);
			System.arraycopy(errs, 0, errors, 0, max);
			errorsSet = true;
		}
	}

	/**
	 * Returns whether errors have been explicitly set or not.
	 * 
	 * @return <code>true</code> if errors have been explicitly set,
	 *         <code>false</code> if not
	 */
	public boolean hasErrorsSet() {
		synchronized (this) {
			return errorsSet;
		}
	}

	/**
	 * Sets an energy calibration function for this histogram.
	 * 
	 * @param calibFunc
	 *            new energy calibration for this histogram
	 */

	public void setCalibration(final AbstractCalibrationFunction calibFunc) {
		synchronized (this) {
			this.calibFunc = calibFunc;
		}
	}

	/**
	 * Returns the calibration function for this histogram as a
	 * <code>CalibrationFunction</code> object.
	 * 
	 * @return the calibration function for this histogram
	 */
	public AbstractCalibrationFunction getCalibration() {
		synchronized (this) {
			return calibFunc;
		}
	}

	/**
	 * Returns whether the histogram is calibrated.
	 * 
	 * @return <code>true</code> if a calibration function has been defined,
	 *         <code>false</code> if not
	 * @see #setCalibration(AbstractCalibrationFunction)
	 */
	public boolean isCalibrated() {
		boolean calibrated;
		synchronized (this) {
			if (calibFunc!=null) {
				calibrated= calibFunc.isCalibrated();
			}else{
				calibrated=false;
			}
		}
		return calibrated;
	}
}
