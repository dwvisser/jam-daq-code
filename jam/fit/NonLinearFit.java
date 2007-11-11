package jam.fit;

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
	 *            value to evalueate at
	 * @return df(<code>x</code>)/d(<code>parameterName</code>) at x
	 */
	double derivative(double xValue, String parameterName);
	void setParameter(final String which, final double value);
}