package jam.data.func;

import javax.swing.*;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Arrays;

/**
 * A function that can be use to calibrate a histogram. Most often used to
 * define energy calibrations of spectra. But could also do time of flight an
 * rho for a magnetic spectrometer.
 * 
 * @author Ken Swartz
 * @version 1.0
 */
public abstract class AbstractCalibrationFunction implements Function {

	/**
	 * @return whether this function implements an actual calibration
	 */
	public boolean isCalibrated() {
		return true;
	}

	/**
	 * Maximum number of terms assigned by default to <code>POLYNOMIAL</code>
	 * type.
	 */
	public final static int MAX_TERMS = 5;

	/**
	 * Term labels.
	 */
	protected transient String[] labels;

	/**
	 * Name of calibration function.
	 */
	protected transient String name;

	/**
	 * Title of calibration function.
	 */
	protected transient String title;

	/**
	 * Whether fit points were used for calibration.
	 */
	protected transient boolean fitPoints = true;

	/**
	 * Fit channels
	 */
	protected transient double[] ptsChannel;

	/**
	 * Fit energy
	 */
	protected transient double[] ptsEnergy = new double[0];

	/**
	 * Coefficient values.
	 */
	protected double[] coeff;

	/**
	 * Length histogram
	 */
	protected transient int sizeHistogram;

	/**
	 * for subclasses to use
	 * 
	 */
	protected AbstractCalibrationFunction() {
		super();
	}

	/**
	 * Creates a new <code>CalibrationFunction</code> object.
	 * 
	 * @param name
	 *            name of function
	 * @param numberTerms
	 *            number of terms in function
	 */
	AbstractCalibrationFunction(final String name, final int numberTerms) {
		super();
		this.name = name;
		if (numberTerms < MAX_TERMS) {
			coeff = new double[numberTerms];
			labels = new String[numberTerms];
		} else {
			throw new IllegalArgumentException(getClass().getName()
					+ "--Maximum terms: " + MAX_TERMS + ", asked for: "
					+ numberTerms);
		}
	}

	/**
	 * @return Number of terms
	 */
	public int getNumberTerms() {
		return coeff.length;
	}

	/**
	 * Given a type of <code>CalibrationFunction</code>, returns an array of
	 * parameter labels.
	 * 
	 * @return an array of parameter labels
	 */
	public String[] getLabels() {
		final int len = labels.length;
		final String[] rval = new String[len];
		System.arraycopy(labels, 0, rval, 0, len);
		return rval;
	}

	/**
	 * Gets the calibration coefficients.
	 * 
	 * @return array containing the calibration coefficients
	 */
	public double[] getCoeff() {
		final int len = coeff.length;
		final double[] rval = new double[len];
		System.arraycopy(coeff, 0, rval, 0, len);
		return rval;
	}

	/**
	 * 
	 * @return name of the calibration function
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return title of the calibration function
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Returns whether coeffecients are result of a fit.
	 * 
	 * @return whether coeffecients are result of a fit
	 */
	public boolean isFitPoints() {
		synchronized (this) {
			return fitPoints;
		}
	}

	/**
	 * Sets whether coefficients are result of a fit.
	 * 
	 * @param isFitIn
	 *            whether coefficients are result of a fit
	 */
	private void setIsFitPoints(final boolean isFitIn) {
		synchronized (this) {
			fitPoints = isFitIn;
		}
	}

	/**
	 * 
	 * @param numFormat
	 *            the number format to use
	 * @return the function formula
	 */
	public String getFormula(final NumberFormat numFormat) {
		return updateFormula(numFormat);
	}

	/**
	 * Set histogram size, used to convert from energy to channel
	 * 
	 * @param size
	 *            the new histogram size
	 */
	public void setSizeHistogram(final int size) {
		sizeHistogram = size;
	}

	/**
	 * Called by setCoeff() to update the formula.
	 * @param numFormat used for formatting numbers
	 * @return a string representation of the best-fit formula
	 */
	protected abstract String updateFormula(NumberFormat numFormat);

	/**
	 * Set the calibration points used for fitting.
	 * 
	 * @param ptsChannelIn
	 *            the channels
	 * @param ptsEnergyIn
	 *            the "energies"
	 */
	public void setPoints(final double[] ptsChannelIn,
			final double[] ptsEnergyIn) {
		setIsFitPoints(true);
		ptsChannel = ptsChannelIn.clone();
		ptsEnergy = ptsEnergyIn.clone();
	}

	/**
	 * Get the input point channels.
	 * 
	 * @return the input point channels
	 */
	public double[] getPtsChannel() {
		final double[] rval;
		if (ptsChannel == null) {
			rval = new double[0];
		} else {
			final int len = ptsChannel.length;
			rval = new double[len];
			System.arraycopy(ptsChannel, 0, rval, 0, len);
		}
		return rval;
	}

	/**
	 * Get the input point energies.
	 * 
	 * @return the input point energies
	 */
	public double[] getPtsEnergy() {
		return ptsEnergy.clone();
	}

	/**
	 * Set the coefficients of the calibration function using the contents of
	 * the passed <code>Array</code>. If passed a larger than necessary array,
	 * the first elements of the array will be used.
	 * 
	 * @param aIn
	 *            array of coefficients which should be at least as large as the
	 *            number of coefficients
	 */
	public void setCoeff(final double aIn[]) {
		setIsFitPoints(false);
		if (aIn.length <= coeff.length) {
			Arrays.fill(coeff, 0.0);
			System.arraycopy(aIn, 0, coeff, 0, aIn.length);
		} else {
			throw new IndexOutOfBoundsException(getClass().getName()
					+ ".setCoeff(double [" + aIn.length + "]): too many terms.");
		}
	}

	/**
	 * Get the calibration value at a specified channel.
	 * 
	 * @param channel
	 *            value at which to get calibration
	 * @return calibration value of the channel
	 */
	public abstract double getValue(double channel);

	/**
	 * Gets the channel for the given energy. Don't always have a inverse
	 * function so by default search for the best channel.
	 * 
	 * @param energy
	 *            to get channel for
	 * @return channel for the given energy
	 */
	public double getChannel(final double energy) {
		double channel = 0;
		final double bestDiff = Math.abs(getValue(channel) - energy);
		double diff;
		for (int i = 0; i < sizeHistogram; i++) {
			diff = Math.abs(getValue(i) - energy);
			if (diff < bestDiff) {
				channel = i;
			}

		}
		return channel;
	}

	/**
	 * Do a calibration fit.
	 * 
	 * @throws CalibrationFitException
	 *             if the fit fails
	 */
	public abstract void fit() throws CalibrationFitException;

	protected void loadIcon(final String iconFile) {
		final ClassLoader loader = ClassLoader.getSystemClassLoader();

		final URL urlIcon = loader.getResource(iconFile);
		if (urlIcon == null) {
			JOptionPane.showMessageDialog(null,
					"Can't load resource for calibration function icon "
							+ iconFile);
		} else {
			CalibrationFunctionCollection.setIcon(this.getName(),
					new ImageIcon(urlIcon));
		}

	}
}
