package jam.data.func;

import java.text.NumberFormat;

import jam.data.DataException;

/**
 * Default non-calibration.
 * @author Dale Visser
 *
 */
public final class NoFunction extends AbstractCalibrationFunction {
	
	/**
	 * constructor
	 *
	 */
	public NoFunction(){
		super();
		name="Not calibrated.";
		title = "";
		coeff=new double[0];
		labels=new String[0];
	}

	@Override
	protected void updateFormula(NumberFormat numFormatCoeff) {
		// do nothing

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
	public void fit() throws DataException {
		// do nothing
	}

	public boolean isCalibrated(){
		return false;
	}
}
