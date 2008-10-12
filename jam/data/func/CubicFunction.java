package jam.data.func;

import java.text.NumberFormat;

/**
 * A Cubic histogram calibration function, that is, E = a0 + a1 * channel+
 * a2*channel^2+a3*channel^3
 */
public class CubicFunction extends AbstractCalibrationFunction {

	private static final int NUMBER_TERMS = 4;

	/**
	 * Creates a new <code>LinearFunction</code> object of the specified type.
	 */
	public CubicFunction() {
		super("Cubic", NUMBER_TERMS);
		title = "E = a0 + a1\u2219ch+a2\u2219ch^2+a3\u2219ch^3";
		labels[0] = "a0";
		labels[1] = "a1";
		labels[2] = "a2";
		labels[3] = "a3";
		this.loadIcon("jam/data/func/cubic.png");
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
		return coeff[0] + coeff[1] * channel + coeff[2] * channel * channel
				+ coeff[3] * channel * channel * channel;
	}

	/**
	 * do a fit of x y values
	 */
	@Override
	public void fit() throws CalibrationFitException {
		final double[] coeffCubic = polynomialFit(ptsEnergy, ptsChannel, 2);
		System.arraycopy(coeffCubic, 0, coeff, 0, coeffCubic.length);
	}

	@Override
	public String updateFormula(final NumberFormat numFormat) {
		final StringBuffer buffer = new StringBuffer(32);
		buffer.append("E = ").append(numFormat.format(coeff[0])).append(" + ")
				.append(numFormat.format(coeff[1])).append("\u2219ch + ")
				.append(numFormat.format(coeff[2])).append("\u2219ch^2 + ")
				.append(numFormat.format(coeff[2])).append("\u2219ch^3");
		return buffer.toString();
	}
}
