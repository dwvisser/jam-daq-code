package jam.fit;

/**
 * Interface for non-linear fit algorithms.
 * 
 * @author Dale Visser
 * 
 */
public interface NonLinearFit extends Fit {

	/**
	 * Evaluates at x for given parameters.
	 * 
	 * @param xValue
	 *            value at which to evaluate the fit function
	 * @return value of fit function at <code>x</code>
	 */
	double valueAt(double xValue);

	/**
	 * Evaluates derivative with respect to <code>parameterName</code> at
	 * <code>x</code>.
	 * 
	 * @param parameterName
	 *            the name of the parameter to differentiate with respect to
	 * @param xValue
	 *            value to evaluate at
	 * @return df(x)/d(parameterName) at x
	 */
	double derivative(double xValue, String parameterName);

	/**
	 * @param which
	 *            parameter to set
	 * @param value
	 *            value to set
	 */
	void setParameter(final String which, final double value);
}