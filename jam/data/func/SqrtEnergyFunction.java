package jam.data.func;
import jam.data.*;

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
	public SqrtEnergyFunction() throws DataException {
		super(NUMBER_TERMS);
		title = "sqrt(E) = a0+a1*ch";
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

	// To be implemented later when this function works
	public double getChannel(double energy) {
		return ((energy - coeff[0]) / coeff[1]);
	}

	/**
	 * do a fit of x y values
	 */
	public String fit(double[] chan, double[] ene) throws DataException {
		double[] sqrtE;
		sqrtE = new double[ene.length];
		for (int i = 0; i < ene.length; i++) {
			sqrtE[i] = Math.sqrt(ene[i]);
		}
		coeff = linearRegression(chan, sqrtE);
		return "Sqrt Energy Function"
			+ "sqrt(E) = "
			+ coeff[0]
			+ " + "
			+ coeff[1]
			+ " x (ch)";
	}
}
