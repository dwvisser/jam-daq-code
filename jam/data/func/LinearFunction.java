package jam.data.func;

import java.text.NumberFormat;

/**
 * A linear histogram calibration function, that is, E = a0 + a1 * channel.
 */
public class LinearFunction extends AbstractCalibrationFunction {

	private static final int NUMBER_TERMS = 2;

	/**
	 * Creates a new <code>LinearFunction</code> object of the specified type.
	 */
	public LinearFunction() {
		super("Linear", NUMBER_TERMS);
		title = "E = a0 + a1\u2219ch";
		labels[0] = "a0";
		labels[1] = "a1";
		AbstractCalibrationFunctionCollection.loadIcon(this,
				"jam/data/func/line.png");
	}

	/**
	 * Get the calibration value at a specified channel.
	 * 
	 * @param channel
	 *            value at which to get calibration
	 * @return calibration value of the channel
	 */
	public double getValue(final double channel) {
		return coeff[0] + coeff[1] * channel;
	}

	/**
	 * Get the calibration value at a specified channel.
	 * 
	 * @param energy
	 *            physical value
	 * @return channel corresponding to <code>energy</code>
	 */
	public double getChannel(final double energy) {
		return ((energy - coeff[0]) / coeff[1]);
	}

	/**
	 * do a fit of x y values
	 */
	public void fit() throws CalibrationFitException {
		final double[] coeffLinRegress = linearRegression(ptsChannel, ptsEnergy);
		System.arraycopy(coeffLinRegress, 0, coeff, 0, coeffLinRegress.length);
	}

	public void updateFormula(final NumberFormat numFormat) {
		formula.setLength(0);
		formula.append("E = ").append(numFormat.format(coeff[0])).append(" + ")
				.append(numFormat.format(coeff[1])).append("\u2219ch");
	}
}
