package jam.data.func;
import jam.data.DataException;

/**
 * A linear histogram calibration function, that is, E = a0 + a1 * channel.
 */
public class LinearFunction extends CalibrationFunction {

	private static final int NUMBER_TERMS = 2;
	
	/**
	 * Creates a new <code>LinearFunction</code> object of the specified type.  
	 */
	public LinearFunction() {
		super(LinearFunction.class, "Linear", NUMBER_TERMS);
		title = "E = a0 + a1\u2219ch";
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
		return coeff[0] + coeff[1] * channel;
	}

	/**
	 * Get the calibration value at a specified channel.
	 * 
	 * @param energy physical value
	 * @return channel corresponding to <code>energy</code>
	 */
	public double getChannel(double energy) {
		return ((energy - coeff[0]) / coeff[1]);
	}
	
	/**
	 * do a fit of x y values
	 */
	public void fit() throws DataException {
		setCoeff(linearRegression(ptsChannel, ptsEnergy));
	}
	
	public void updateFormula(){
		formula.setLength(0);
		formula.append("E = ").append(coeff[0]).append(" + ").append(coeff[1])
				.append("\u2219ch");		
	}
}
