package jam.data.func;
import jam.data.*;

/**
 * A polynomial function that can be use to calibrate a histogram.  Most often used to define energy
 * calibrations of spectra.
 */
public class LinearFunction extends CalibrationFunction {

	private static final int NUMBER_TERMS = 2;
	
	/**
	 * Creates a new <code>LinearFunction</code> object of the specified type.  
	 *
	 * @exception   DataException   thrown if invalid <code>type</code> passed to constructor
	 */
	public LinearFunction() throws DataException {
		super(NUMBER_TERMS);
		title = "E = a0+a1*ch";
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
	public String fit(double[] x, double[] y) throws DataException {
		coeff = linearRegression(x, y);
		return "Linear fit";
		//FIXME		    "E = "+numFormat.format(a)+" + "+numFormat.format(b)+" x ch");	    				    	
	}
}
