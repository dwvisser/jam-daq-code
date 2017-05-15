package jam.data.func;

/**
 * Provides a linear regression function to subclasses.
 * 
 * @author Dale Visser
 * 
 */
public abstract class AbstractLinearRegressionFunction extends
		AbstractCalibrationFunction {

	/**
	 * @param name
	 *            name
	 * @param numberTerms
	 *            number of terms
	 */
	public AbstractLinearRegressionFunction(final String name,
			final int numberTerms) {
		super(name, numberTerms);
	}

	/**
	 * do a linear regression of data points y=a+bx returns to fit a, and b
	 * values in an array
	 * 
	 * @param xVal
	 *            array of x values
	 * @param yVal
	 *            array of y values
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
		double delta;
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
}
