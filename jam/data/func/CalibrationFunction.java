package jam.data.func;
import jam.data.DataException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * A function that can be use to calibrate a histogram.  
 * Most often used to define energy calibrations of spectra.
 * But could also do time of flight an rho for a magnetic spectometer.
 *
 * @author Ken Swartz
 * @version 1.0
 */
public abstract class CalibrationFunction implements Function {

	private static final Map  mapFunctions =new HashMap();	
	private static final List names =new ArrayList();	

	/**
	 * Maximum number of terms assigned by default to <code>POLYNOMIAL</code> type.
	 */
	public final static int MAX_NUMBER_TERMS = 5;

	/**
	 * Term labels.
	 */
	protected transient String[] labels;

	/**
	 * Name of calibration function.
	 */	
	protected transient String name;
	
	/**
	 * Title of calibration function.
	 */
	protected transient String title;
	
	/**
	 * Coeffiecient values.
	 */
	protected double[] coeff;
	
	/**
	 * The formula for the function.
	 */
	protected transient StringBuffer formula=new StringBuffer();

	public static List getListNames() {
		return names;
	}
	public static Map getMapFunctions() {
		return mapFunctions;
	}	
	public static void clearAll(){
		mapFunctions.clear();
		names.clear();
	}
	public static void addFunction(String name, Class funcClass) {
		//Only add once
		if (!mapFunctions.containsKey(name)){
			mapFunctions.put(name, funcClass);
			names.add(name);
		}		
	}
		

	/**
	 * Creates a new <code>CalibrationFunction</code> object.
	 *
	 * @param	numberTerms	number of terms in function
	 */
	public CalibrationFunction(Class inClass, String name, int numberTerms) {
		this.name=name;
		addFunction(name, inClass);
		if (numberTerms < MAX_NUMBER_TERMS) {
			coeff=new double[numberTerms];
			labels=new String[numberTerms];
		} else {
			throw new IllegalArgumentException(getClass().getName()+
			"--Maximum terms: "+MAX_NUMBER_TERMS+", asked for: "+numberTerms);
		}
	}
		
	/**
	 * @return Number of terms
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
	 * 
	 * @return name of the calibration function
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * 
	 * @return title of the calibration function
	 */
	public String getTitle(){
		return title;
	}
	
	/**
	 * 
	 * @return the function formula
	 */
	public String getFormula(){
		return formula.toString();
	}
	
	/**
	 * Called by setCoeff() to update the formula.
	 *
	 */
	protected abstract void updateFormula(); 
		
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

	/**
	 * Gets the channel for the given energy.
	 * @param energy to get channel for
	 * @return channel for the given energy
	 */
	public abstract double getChannel(double energy);

	/**
	 * Do a calibration fit
	 * 
	 * @param x array of x values
	 * @param y array of y values
	 * @throws DataException if the fit fails
	 */
	public abstract void fit(double[] x, double[] y) throws DataException;

	/**
	 * do a linear regression of data points y=a+bx 
	 * returns to fit a, and b values in an array
	 *
	 * @param x array of x values
	 * @param y array of y values
	 * @throws DataException if regression fails
	 * @return array where first element is constant, second is slope
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
