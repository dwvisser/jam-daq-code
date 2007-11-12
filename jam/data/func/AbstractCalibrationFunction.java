package jam.data.func;

import java.text.NumberFormat;
import java.util.Arrays;

/**
 * A function that can be use to calibrate a histogram. Most often used to
 * define energy calibrations of spectra. But could also do time of flight an
 * rho for a magnetic spectometer.
 * 
 * @author Ken Swartz
 * @version 1.0
 */
public abstract class AbstractCalibrationFunction implements Function {

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
	 * Coeffiecient values.
	 */
	protected double[] coeff;

	/**
	 * Length histogram
	 */
	protected transient int sizeHistogram;

	/**
	 * The formula for the function.
	 */
	protected transient StringBuffer formula = new StringBuffer();

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
	AbstractCalibrationFunction(String name, int numberTerms) {
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
	 * @return the function formula
	 */
	public String getFormula(final NumberFormat numFormat) {
		updateFormula(numFormat);
		return formula.toString();
	}

	/**
	 * Set histogram size, used to convert from energy to channel
	 */
	public void setSizeHistogram(final int size) {
		sizeHistogram = size;
	}

	/**
	 * Called by setCoeff() to update the formula.
	 * 
	 */
	protected abstract void updateFormula(NumberFormat numFormat);

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
	 * the passed <code>Array</code>. If passed a larger than necessary
	 * array, the first elements of the array will be used.
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

	// TODO the rest of the methods should be moved to jam.fit

	/**
	 * do a linear regression of data points y=a+bx returns to fit a, and b
	 * values in an array
	 * 
	 * @param xVal
	 *            array of x values
	 * @param yVal
	 *            array of y values
	 * @throws DataException
	 *             if regression fails
	 * @return array where first element is constant, second is slope
	 */
	protected double[] linearRegression(final double[] xVal, final double[] yVal)
			throws CalibrationFitException {
		double[] rval = new double[2];
		double sum = 0.0;
		double sumx = 0.0;
		double sumy = 0.0;
		double sumxx = 0.0;
		double sumxy = 0.0;
		double sumyy = 0.0;
		final double weight = 1.0;
		double delta = 0.0;
		double aEst, bEst;
		final int numberPoints = xVal.length;
		for (int i = 0; i < numberPoints; i++) {
			sum = sum + weight;
			sumx = sumx + weight * xVal[i];
			sumy = sumy + yVal[i];
			sumxx = sumxx + weight * xVal[i] * xVal[i];
			sumxy = sumxy + weight * xVal[i] * yVal[i];
			sumyy = sumyy + weight * yVal[i] * yVal[i];
		}
		delta = sum * sumxx - sumx * sumx;
		if (delta == 0.0) {
			rval[0] = 0.0;
			rval[1] = 0.0;
			throw new CalibrationFitException("Linear regression failed.");
		}
		aEst = (sumxx * sumy - sumx * sumxy) / delta;
		bEst = (sumxy * sum - sumx * sumy) / delta;
		rval[0] = aEst;
		rval[1] = bEst;
		return rval;
	}

	/**
	 * do a least squares fit of data points y=a+bx returns to fit a, and b
	 * values in an array
	 * 
	 * @param xVal
	 *            array of x values
	 * @param yVal
	 *            array of y values
	 * @throws DataException
	 *             if regression fails
	 * @return with polynomial coefficents
	 */
	protected double[] polynomialFit(final double[] xVal, final double[] yVal,
			final int order) throws CalibrationFitException {
		final int numTerms = order + 1;
		// Check data
		if (xVal.length < numTerms) {
			throw new IllegalArgumentException(
					"Need more positions than order for polynomial fit");
		}
		if (xVal.length != yVal.length) {
			throw new IllegalArgumentException(
					"Need same number of x and y points for polynomial fit");
		}
		// Find mean x to shift fit around mean
		double sum = 0.0;
		for (int k = 0; k < xVal.length; k++) {
			sum += xVal[k];
		}
		final double xMean = sum / xVal.length;
		// Shift x data around mean
		double[] xNorm = new double[xVal.length];
		for (int k = 0; k < xVal.length; k++) {
			xNorm[k] = xVal[k] - xMean;
		}
		final double[][] matrixA = new double[numTerms][numTerms];
		final double[] vectorB = new double[numTerms];
		buildPolyMatrix(xVal, yVal, numTerms, matrixA, vectorB);
		// Copy vector b into a column matrix
		double[][] gaussMatrixB = new double[vectorB.length][1];
		for (int i = 0; i < vectorB.length; i++) {// NOPMD
			gaussMatrixB[i][0] = vectorB[i];
		}
		// Do gaussian elimination
		gaussj(matrixA, gaussMatrixB);
		// Copy vector b into a column matrix
		double[] polyCoeffs = new double[vectorB.length];
		for (int i = 0; i < polyCoeffs.length; i++) {
			polyCoeffs[i] = gaussMatrixB[i][0];
		}
		return polyCoeffs;
	}

	/**
	 * Build a matrix for a polynomial fit
	 * 
	 * @param xVal
	 *            x values
	 * @param yVal
	 *            y values
	 * @param order
	 *            order of polynomial
	 * @return matrixA the matrix to do gaussj on.
	 */
	protected void buildPolyMatrix(final double xVal[], final double yVal[],
			final int order, double[][] matrixA, double[] vectorB) {
		double sum;

		// Alpha matrix part
		for (int row = 0; row < order; row++) {
			for (int col = 0; col < order; col++) {
				// Sum of x^n
				sum = 0.0;
				for (int k = 0; k < order; k++) {
					sum += Math.pow(xVal[k], row + col);
				}
				matrixA[row][col] = sum;
			}
		}

		// Alpha matrix part
		for (int row = 0; row < order; row++) {
			// Beta vector
			sum = 0.0;
			for (int k = 0; k < order; k++) {
				sum += Math.pow(xVal[k], row) * yVal[k];
			}
			vectorB[row] = sum;
		}

	}

	// TODO can this call jam.fit.GaussJordanElimination instead?

	/**
	 * gauss jordon reduction from numerical recipes
	 * 
	 * @param alpha
	 *            alpha matrix
	 * @param beta
	 *            beta matrix
	 * @return fit coeffients
	 */
	protected void gaussj(final double[][] alpha, final double[][] beta)
			throws CalibrationFitException {
		final int alphaLength = alpha.length;
		int column = 0;
		int row = 0;
		int[] columnIndices = new int[alphaLength];
		int[] rowIndices = new int[alphaLength];
		final int[] pivotIndices = new int[alphaLength];

		// loop over cols
		for (int i = 0; i < alphaLength; i++) {
			// search for pivot
			double big = 0.0;
			for (int j = 0; j < alphaLength; j++) {
				if (pivotIndices[j] != 1) {
					for (int k = 0; k < alphaLength; k++) {
						if (pivotIndices[k] == 0
								&& Math.abs(alpha[j][k]) >= big) {
							big = Math.abs(alpha[j][k]);
							row = j;
							column = k;
						}
					}
				}
			}
			++(pivotIndices[column]);
			if (row != column) {
				swapRows(alpha, row, column);
				swapRows(beta, row, column);
			}
			rowIndices[i] = row;
			columnIndices[i] = column;
			if (alpha[column][column] == 0.0) {
				throw new CalibrationFitException("gaussj: Singular Matrix");
			}
			normalizeToPivotAndSubtract(alpha, beta, column);
		}
		pivot(alpha, columnIndices, rowIndices);
	}

	/**
	 * @param alpha
	 * @param alphaLength
	 * @param columnIndices
	 * @param rowIndices
	 */
	private void pivot(double[][] alpha, final int[] columnIndices,
			final int[] rowIndices) {
		final int alphaLength = alpha.length;
		for (int l = alphaLength - 1; l >= 0; l--) {
			if (rowIndices[l] != columnIndices[l]) {
				for (int k = 0; k < alphaLength; k++) {
					final double temp = alpha[k][rowIndices[l]];
					alpha[k][rowIndices[l]] = alpha[k][columnIndices[l]];
					alpha[k][columnIndices[l]] = temp;
				}
			}
		}
	}

	/**
	 * @param alpha
	 * @param beta
	 * @param alphaLength
	 * @param column
	 */
	private void normalizeToPivotAndSubtract(double[][] alpha,
			final double[][] beta, final int column) {
		final double pivinv = 1.0 / alpha[column][column];
		alpha[column][column] = 1.0;
		multiplyArray(alpha[column], pivinv);
		multiplyArray(beta[column], pivinv);
		final int alphaLength = alpha.length;
		for (int j = 0; j < alphaLength; j++) {
			if (j != column) {
				final double dum = alpha[j][column];
				alpha[j][column] = 0.0;
				subtractRows(alpha, j, column, dum);
				subtractRows(beta, j, column, dum);
			}
		}
	}

	private void multiplyArray(double[] array, final double factor) {
		for (int i = array.length - 1; i >= 0; i--) {
			array[i] *= factor;
		}
	}

	private void subtractRows(double[][] array, final int row1, final int row2,
			final double factor) {
		for (int i = array[row1].length - 1; i >= 0; i--) {
			array[row1][i] -= factor * array[row2][i];
		}
	}

	private void swapRows(double[][] array, final int row1, final int row2) {
		for (int i = array[row1].length - 1; i >= 0; i--) {
			final double temp = array[row1][i];
			array[row1][i] = array[row2][i];
			array[row2][i] = temp;
		}
	}
}
