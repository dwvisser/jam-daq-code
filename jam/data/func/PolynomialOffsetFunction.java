package jam.data.func;
import jam.data.*;

/**
 * A polynomial function that can be use to calibrate a histogram.  Most often used to define energy
 * calibrations of spectra.
 */
public class PolynomialOffsetFunction extends CalibrationFunction {

	/**
	 * Creates a new <code>CalibrationFunction</code> object of the specified type.  If passed <code>
	 * LINEAR</code>, the function is a line.  If passed <code>POLYNOMIAL</code>, it is a polynomial
	 * of order <code>POLY_NUM_TERMS - 1<code>.
	 *
	 * @param	type	one of <code>LINEAR</code> or <code>POLYNOMIAL</code>
	 * @exception   DataException   thrown if invalid <code>type</code> passed to constructor
	 */
	public PolynomialOffsetFunction(int numberTerms) throws DataException {
		super(numberTerms);
		if (numberTerms < MAX_NUMBER_TERMS) {
			title = "E = a0+a1*(ch-x0)+a2*(ch-x0)^2+ ...";
			coeff = new double[numberTerms];
			labels = new String[numberTerms];
			labels[0] = "x0";
			for (int i = 0; i < numberTerms - 1; i++) {
				labels[i + 1] = "a(" + i + ")";
			}
		} else {
			throw new DataException("Number of terms greater than MAX_NUMBER_TERMS [PolynomialOffsetFunction]");
		}
	}
	
	/**
	 * Get the calibration value at a specified channel.
	 * 
	 * @param	channel	value at which to get calibration
	 * @return	calibration value of the channel
	 */
	public double getValue(double channel) {
		//check that a calibration has been defined
		double chanMult;
		double value = 0.0;
		chanMult = 1.0;
		for (int i = 0; i < coeff.length - 1; i++) {
			value = value + coeff[i + 1] * chanMult;
			chanMult = chanMult * (channel - coeff[0]);
		}
		return value;
	}

	// To be implemented Later when this function Works 
	public double getChannel(double energy) {
		return ((energy - coeff[0]) / coeff[1]);
	}

	/**
	 * do a fit of x y values
	 */
	public String fit(double[] x, double[] y) throws DataException {

		return "Polynomial Offset Function fit not yet implemented";
	}

}
