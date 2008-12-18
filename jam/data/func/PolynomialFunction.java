package jam.data.func;

import java.text.NumberFormat;

/**
 * A polynomial histogram calibration function of up to 4th order.
 */
public class PolynomialFunction extends AbstractGaussJordanFunction {

	/**
	 * Creates a new <code>CalibrationFunction</code> object of the specified
	 * polynomial order.
	 * 
	 * @param numberTerms
	 *            terms in the polynomial (including a constant term)
	 */
	public PolynomialFunction(final int numberTerms) {
		super("Polynomial", numberTerms);
		title = "E = a0 + a1\u2219ch + a2\u2219(ch)\u00b2 + ...";
		coeff = new double[numberTerms];
		labels = new String[numberTerms];
		for (int i = 0; i < numberTerms; i++) {
			labels[0] = "a(" + i + ")";
		}
	}

	/**
	 * Get the calibration value at a specified channel.
	 * 
	 * @param channel
	 *            value at which to get calibration
	 * @return calibration value of the channel
	 */
	@Override
	public double getValue(final double channel) {
		double chanMult;
		double value = 0.0;
		chanMult = 1.0;
		for (int i = 0; i < coeff.length; i++) {
			value = value + coeff[i] * chanMult;
			chanMult = chanMult * channel;
		}
		return value;
	}

	// To be implemented later when This Function Works
	@Override
	public double getChannel(final double energy) {
		return ((energy - coeff[0]) / coeff[1]);
	}

	/**
	 * do a fit of x y values
	 */
	@Override
	public void fit() {
		// does nothing so far
	}

	@Override
	public String updateFormula(final NumberFormat numFormat) {
		return "Polynomial fit not yet implemented";
	}

	/**
	 * Test of polynomial fit
	 * 
	 * @param args
	 */
	public static void main(final String args[]) {

		final PolynomialFunction function = new PolynomialFunction(1);
		double[] xval = new double[3];
		double[] yval = new double[3];
		double[] coeff;

		xval[0] = 1;
		yval[0] = 2;
		xval[1] = 2;
		yval[1] = 5;
		xval[2] = 3;
		yval[2] = 10;

		try {
			coeff = function.polynomialFit(xval, yval, 2);
			System.out.println("Coeff ");// NOPMD
			for (int i = 0; i < 3; i++) {
				System.out.println("" + coeff[i]);// NOPMD
			}
		} catch (CalibrationFitException de) {
			System.out.println(de);// NOPMD
		}

	}

}
