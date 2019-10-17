package jam.data.func;

import java.text.NumberFormat;

/**
 * Default non-calibration.
 * 
 * @author Dale Visser
 * 
 */
public final class NoFunction extends AbstractCalibrationFunction {

	/**
	 * constructor
	 * 
	 */
	public NoFunction() {
		super();
		name = "Not calibrated.";
		title = "";
		coeff = new double[0];
		labels = new String[0];
	}

	@Override
	protected String updateFormula(final NumberFormat numFormatCoeff) {
		return "";
	}

	@Override
	public double getValue(final double channel) {
		return channel;
	}

	@Override
	public double getChannel(final double energy) {
		return energy;
	}

	@Override
	public void fit() {
		// do nothing
	}

	@Override
	public boolean isCalibrated() {
		return false;
	}
}
