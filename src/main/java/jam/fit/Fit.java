package jam.fit;

import jam.global.MessageHandler;

interface Fit {
	Parameter<?> getParameter(String which);

	ParameterList getParameters();

	MessageHandler getTextInfo();

	/**
	 * Returns the evaluation of a signal at one particular channel. Should
	 * return zero if the given signal doesn't exist.
	 * 
	 * @param signal
	 *            which signal
	 * @param channel
	 *            to calculate signal value for
	 * @return signal value for the given channel
	 */
	double calculateSignal(int signal, int channel);

	double calculateBackground(int channel);

	/**
	 * Calculates function value for a specific channel. Uses whatever the
	 * current values are for the parameters in <code>parameters</code>.
	 * 
	 * @param channel
	 *            channel to evaluate fit function at
	 * @return double containing evaluation
	 */
	double calculate(int channel);

	/**
	 * 
	 * @return whether fit function has a background component
	 */
	boolean hasBackground();

	/**
	 * Performs calculations necessary to find a best fit to the data.
	 * 
	 * @return <code>String</code> containing information about the fit.
	 * @exception FitException
	 *                thrown if unrecoverable error occurs during fit
	 */
	String doFit() throws FitException;

	/**
	 * Changes parameter values to calculated estimates. Useful for
	 * <code>NonLinearFit</code>, which requires reasonably close guesses in
	 * order to converge on a good chi-squared minimum.
	 * 
	 * @exception FitException
	 *                thrown if unrecoverable error occurs during estimation
	 * @see AbstractNonLinearFit
	 * @see GaussianFit#estimate
	 */
	void estimate() throws FitException;

	/**
	 * Returns the number of signals modeled by the fit.
	 * 
	 * @return the number of signals modeled by the fit
	 */
	int getNumberOfSignals();

}
