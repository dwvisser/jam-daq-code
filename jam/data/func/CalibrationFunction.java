package jam.data.func;
import jam.data.DataException;

/**
 * A function that can be use to calibrate a histogram.  
 * Most often used to define energy calibrations of spectra.
 * But could also do time of flight an rho for a magnetic spectometer.
 *
 * @author Ken Swartz
 * @version 1.0
 */
public abstract class CalibrationFunction implements Function {

	/**
	 * Maximum number of terms assigned by default to <code>POLYNOMIAL</code> type.
	 */
	public final static int MAX_NUMBER_TERMS = 5;

	protected transient String[] labels;
	protected transient String title;
	protected double[] coeff;
	protected transient StringBuffer formula=new StringBuffer();

	/**
	 * Creates a new <code>CalibrationFunction</code> object.
	 *
	 * @param	numberTerms	number of terms in function
	 * @exception   DataException   thrown if invalid <code>type</code> passed to constructor
	 */
	public CalibrationFunction(int numberTerms) {
		if (numberTerms < MAX_NUMBER_TERMS) {
			coeff=new double[numberTerms];
			labels=new String[numberTerms];
		} else {
			throw new IllegalArgumentException(getClass().getName()+
			"--Maximum terms: "+MAX_NUMBER_TERMS+", asked for: "+numberTerms);
		}
	}
	
	public CalibrationFunction() {
		this(MAX_NUMBER_TERMS);
	}

	/**
	 * Number of terms
	 */
	public int getNumberTerms() {
		return coeff.length;
	}
	
	/**
	 * Given a type of <code>CalibrationFunction</code>, returns an array of parameter labels.
	 *
	 * @return	an array of parameter labels
	 */
	public String[] getLabels() {
		return labels;
	}
	
	/**
	 * Gets the calibration coefficients.
	 *
	 * @return array containing the calibration coefficients
	 */
	public double[] getCoeff() {
		return coeff;
	}
	
	/**
	 * Get the calibration value for a particular <code>double</code> value. 
	 * Actually a cast to <code>int</code> is done for now.
	 *
	 * @param	x	value at which to get calibration
	 * @return	calibration value of the argument
	 * @see #getValue(double)
	 */
	public double getValue(int x) {
		return getValue((double) x);
	}
	
	public String getTitle(){
		return title;
	}
	
	public String getFormula(){
		return formula.toString();
	}
	
	protected abstract void updateFormula(); 
	
	/**
	 * Added to provide energy calibration for goto button
	 */
	public double getChannel(int y) {
		return getChannel((double) y);
	}
	
	public double getCalculatedEnergy(double energy) {
		return getValue((double) energy);
	}
	
	/**
	 * Set the coefficients of the calibration function using the contents of the passed <code>Array</code>.
	 * If passed a larger than necessary array, the first elements of the array will be used.
	 *
	 * @param aIn   array of coefficients which should be at least as large as the number of coefficients
	 */
	public void setCoeff(double aIn[]) {
		if (aIn.length <= coeff.length) {
			//zero array
			for (int i = 0; i < coeff.length; i++) {
				coeff[i] = 0.0;
			}
			System.arraycopy(aIn, 0, coeff, 0, aIn.length);
			updateFormula();
		} else {
			throw new IndexOutOfBoundsException(getClass().getName()+".setCoeff(double ["+aIn.length+"]): too many terms.");
		}
	}

	/**
	 * Get the calibration value at a specified channel.
	 * 
	 * @param	channel	value at which to get calibration
	 * @return	calibration value of the channel
	 */
	public abstract double getValue(double channel);

	public abstract double getChannel(double energy);

	/**
	 * Do a calibration fit
	 * 
	 * @param x array of x values
	 * @param y array of y values
	 */
	public abstract void fit(double[] x, double[] y) throws DataException;



	/**
	 * do a linear regression of data points y=a+bx 
	 * returns to fit a, and b values in an array
	 *
	 * @param x array of x values
	 * @param y array of y values
	 */
	protected double[] linearRegression(double[] x, double[] y)
		throws DataException {
		double sum = 0.0;
		double sumx = 0.0;
		double sumy = 0.0;
		double sumxx = 0.0;
		double sumxy = 0.0;
		double sumyy = 0.0;
		double weight = 1.0;
		double delta = 0.0;
		double a, b;
		double[] func = new double[2];
		int numberPoints = x.length;
		for (int i = 0; i < numberPoints; i++) {
			weight = 1.0;
			sum = sum + weight;
			sumx = sumx + weight * x[i];
			sumy = sumy + y[i];
			sumxx = sumxx + weight * x[i] * x[i];
			sumxy = sumxy + weight * x[i] * y[i];
			sumyy = sumyy + weight * y[i] * y[i];
		}
		delta = sum * sumxx - sumx * sumx;
		if (delta != 0.0) {
			a = (sumxx * sumy - sumx * sumxy) / delta;
			b = (sumxy * sum - sumx * sumy) / delta;
			func[0] = a;
			func[1] = b;
		} else {
			func[0] = 0.0;
			func[1] = 0.0;
			throw new DataException("Linear regression failed [CalibrationFunction]");
		}
		return func;
	}
}
