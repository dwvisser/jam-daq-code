package jam.data.func;
import jam.data.DataException;

/**
 * A polynomial function that can be use to calibrate a histogram.  Most often used to define energy
 * calibrations of spectra.
 */
public class PolynomialFunction extends CalibrationFunction {

	/**
	 * Creates a new <code>CalibrationFunction</code> object of the specified 
	 * polynomial order. 
	 *
	 * @param numberTerms terms in the polynomial (including a constant term)
	 * @exception   DataException   thrown if invalid <code>type</code> passed to constructor
	 */
	public PolynomialFunction(int numberTerms) {
		super(numberTerms);
		title = "E = a0 + a1\u2219ch + a2\u2219(ch)\u00b2 + ...";
		coeff = new double[numberTerms];
		labels = new String[numberTerms];
		for (int i = 0; i < numberTerms; i++) {
			labels[0] = "a(" + i + ")";
		}
	}
	
	public PolynomialFunction(){
		this(4);
	}

	/**
	 * Get the calibration value at a specified channel.
	 * 
	 * @param	channel	value at which to get calibration
	 * @return	calibration value of the channel
	 */
	public double getValue(double channel) {

		double chanMult;
		double value = 0.0;
		chanMult = 1.0;
		for (int i = 0; i < coeff.length; i++) {
			value = value + coeff[i] * chanMult;
			chanMult = chanMult * channel;
		}
		return value;
	}

	// To be implemented later when This Function Works
	public double getChannel(double energy) {
		return ((energy - coeff[0]) / coeff[1]);
	}
	/**
	 * do a fit of x y values
	 */
	public void fit(double[] x, double[] y) throws DataException {
		//does nothing so far
	}
	
	public void updateFormula(){
		formula = "Polynomial fit not yet implemented";		
	}

}
