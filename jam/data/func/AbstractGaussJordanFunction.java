package jam.data.func;

/**
 * @author Dale Visser
 * 
 */
public abstract class AbstractGaussJordanFunction extends
		AbstractCalibrationFunction {
	/**
	 * @param name
	 *            name
	 * @param numberTerms
	 *            number of terms
	 */
	public AbstractGaussJordanFunction(final String name, final int numberTerms) {
		super(name, numberTerms);
	}

	/**
	 * do a least squares fit of data points y=a+bx returns to fit a, and b
	 * values in an array
	 * 
	 * @param xVal
	 *            array of x values
	 * @param yVal
	 *            array of y values
	 * @return with polynomial coefficients
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
	 */
	private void buildPolyMatrix(final double xVal[], final double yVal[],
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

	/**
	 * Gauss-Jordan reduction from numerical recipes
	 * 
	 * @param alpha
	 *            alpha matrix
	 * @param beta
	 *            beta matrix
	 */
	private void gaussj(final double[][] alpha, final double[][] beta)
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
