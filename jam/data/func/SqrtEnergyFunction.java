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
	 *
	 * @exception   DataException   thrown if invalid <code>type</code> passed to constructor
	 */
	public SqrtEnergyFunction() {
		super(NUMBER_TERMS);
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
	public void fit(double[] chan, double[] ene) throws DataException {
		final double[] sqrtE = new double[ene.length];
		for (int i = 0; i < ene.length; i++) {
			sqrtE[i] = Math.sqrt(ene[i]);
		}
		setCoeff(linearRegression(chan, sqrtE));
	}
	
	protected void updateFormula(){
		formula = "\u221aE = "
			+ coeff[0]
			+ " + "
			+ coeff[1]
			+ "\u2219ch";
	}
}
