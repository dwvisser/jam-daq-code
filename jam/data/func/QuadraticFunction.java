package jam.data.func;

import java.text.NumberFormat;

import jam.data.DataException;

/**
 * A quadratic histogram calibration function, that is, E = a0 + a1 *
 * channel+a2*channel^2
 */
public class QuadraticFunction extends AbstractCalibrationFunction {

	private static final int NUMBER_TERMS = 3;

	/**
	 * Creates a new <code>QuadraticFunction</code> object of the specified
	 * type.
	 */
	public QuadraticFunction() {
		super(QuadraticFunction.class, "Quadratic", NUMBER_TERMS);
		title = "E = a0 + a1\u2219ch a2\u2219ch^2";
		labels[0] = "a0";
		labels[1] = "a1";
		labels[2] = "a2";
		loadIcon(this, "jam/data/func/quad.png");
	}

	/**
	 * Get the calibration value at a specified channel.
	 * 
	 * @param channel
	 *            value at which to get calibration
	 * @return calibration value of the channel
	 */
	public double getValue(final double channel) {
		return coeff[0] + coeff[1] * channel + coeff[2] * channel * channel;
	}

	/**
	 * do a fit of x y values
	 */
	public void fit() throws DataException {
		final double[] coeffQuad = polynomialFit(ptsEnergy, ptsChannel, 2);
		System.arraycopy(coeffQuad, 0, coeff, 0, coeffQuad.length);
	}

	public void updateFormula(final NumberFormat numFormat) {
		formula.setLength(0);
		formula.append("E = ").append(numFormat.format(coeff[0])).append(" + ")
				.append(numFormat.format(coeff[1])).append("\u2219ch").append(
						" + ").append(numFormat.format(coeff[2])).append(
						"\u2219ch^2");

	}
}