package jam.data.func;
import jam.data.DataException;

/**
 * A sqrt function that can be use to calibrate a histogram taken by a
 * magnetic spectometer.
 *  
 */
public class SqrtEnergyFunction extends CalibrationFunction {

	private static final int NUMBER_TERMS = 2;
	
	/**
	 * Creates a new <code>SqrtEnergyFunction </code> object of the specified type.  
	 */
	public SqrtEnergyFunction() {
		super(SqrtEnergyFunction.class, "Linear in Square Root" ,NUMBER_TERMS);
		title = "\u221aE = a0 + a1\u2219ch";
		labels[0] = "a(0)";
		labels[1] = "a(1)";
	}
	
	/**
	 * Get the calibration value at a specified channel.
	 * 
	 * @param	channel	value at which to get calibration
	 * @return	calibration value of the channel
	 */
	public double getValue(double channel) {
		return (coeff[0] + coeff[1] * channel)
			* (coeff[0] + coeff[1] * channel);
	}

	public double getChannel(double energy) {
		return ((Math.sqrt(energy) - coeff[0]) / coeff[1]);
	}

	/**
	 * do a fit of x y values
	 */
	public void fit() throws DataException {
		final double[] sqrtE = new double[ptsEnergy.length];
		for (int i = 0; i < ptsEnergy.length; i++) {
			sqrtE[i] = Math.sqrt(ptsEnergy[i]);
		}
		setCoeff(linearRegression(ptsChannel, sqrtE));
	}
	
	protected void updateFormula(){
		formula.setLength(0);
		formula.append("\u221aE = ").append(coeff[0]).append(" + ").append(
				coeff[1]).append("\u2219ch");
	}
}
