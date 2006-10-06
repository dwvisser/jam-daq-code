package jam.data.func;
import jam.data.DataException;

/**
 * A polynomial histogram calibration function of up to 4th order.
 */
public class PolynomialFunction extends AbstractCalibrationFunction {

	/**
	 * Creates a new <code>CalibrationFunction</code> object of the specified 
	 * polynomial order. 
	 *
	 * @param numberTerms terms in the polynomial (including a constant term)
	 */
	public PolynomialFunction(int numberTerms) {
		super(PolynomialFunction.class, "Polynomial",numberTerms);
		title = "E = a0 + a1\u2219ch + a2\u2219(ch)\u00b2 + ...";
		coeff = new double[numberTerms];
		labels = new String[numberTerms];
		for (int i = 0; i < numberTerms; i++) {
			labels[0] = "a(" + i + ")";
		}
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
	public void fit() throws DataException {
		//does nothing so far
	}
	
	public void updateFormula(){
		formula.setLength(0);
		formula.append("Polynomial fit not yet implemented");		
	}
	/**
	 * Test of polynomial fit
	 * @param args
	 */
	public static void main(final String args[]) {
		
		PolynomialFunction pf= new PolynomialFunction(1);
		double []x=new double [3];
		double []y= new double [3];
		double [] coeff;
		
		x[0]=1; y[0]=2;
		x[1]=2; y[1]=5;
		x[2]=3; y[2]=10;
		
		try {
			coeff= pf.polynomialFit(x, y, 2);
			System.out.println("Coeff ");
			for (int i=0; i< 3;i++)
			{
				System.out.println(""+coeff[i]);
			}
		}
		catch(DataException de) {
			System.out.println(de);
		}


	}

}
