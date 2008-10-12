package jam.data.func;

import java.text.NumberFormat;

/**
 * A sqrt function that can be use to calibrate a histogram taken by a magnetic
 * spectometer.
 * 
 */
public class SqrtEnergyFunction extends AbstractCalibrationFunction {

	private static final int NUMBER_TERMS = 2;

	/**
	 * Creates a new <code>SqrtEnergyFunction </code> object of the specified
	 * type.
	 */
	public SqrtEnergyFunction() {
		super("Linear in Square Root", NUMBER_TERMS);
		title = "\u221aE = a0 + a1\u2219ch";
		labels[0] = "a0";
		labels[1] = "a1";
		this.loadIcon("jam/data/func/sqrt.png");
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
		return (coeff[0] + coeff[1] * channel)
				* (coeff[0] + coeff[1] * channel);
	}

	@Override
	public double getChannel(final double energy) {
		return ((Math.sqrt(energy) - coeff[0]) / coeff[1]);
	}

	/**
	 * do a fit of x y values
	 */
	@Override
	public void fit() throws CalibrationFitException {
		final double[] sqrtE = new double[ptsEnergy.length];
		for (int i = 0; i < ptsEnergy.length; i++) {
			sqrtE[i] = Math.sqrt(ptsEnergy[i]);
		}
		setCoeff(linearRegression(ptsChannel, sqrtE));
	}

	@Override
	protected String updateFormula(final NumberFormat numFormat) {
		final StringBuffer formula = new StringBuffer();
		formula.append("\u221aE = ").append(numFormat.format(coeff[0])).append(
				" + ").append(numFormat.format(coeff[1])).append("\u2219ch");
		return formula.toString();
	}
}
