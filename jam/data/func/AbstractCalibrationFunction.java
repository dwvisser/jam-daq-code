package jam.data.func;
import jam.data.DataException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

/**
 * A function that can be use to calibrate a histogram.  
 * Most often used to define energy calibrations of spectra.
 * But could also do time of flight an rho for a magnetic spectometer.
 *
 * @author Ken Swartz
 * @version 1.0
 */
public abstract class AbstractCalibrationFunction implements Function {

	/**
	 * String used to indicate a histogram is not calibrated.
	 */
    public final static String NOT_CALIB ="Not Calibrated";	
    
	private static final Map  FUNCTIONS =new HashMap();	
	private static final List NAMES =new ArrayList();	
	private static final Map  ICONS =new HashMap();

	static {
		clearAll();
		addFunction(NOT_CALIB, null);		
		AbstractCalibrationFunction linearFunc=new LinearFunction();
		addFunction(linearFunc.getName(), linearFunc.getClass());
		AbstractCalibrationFunction sqrtEFunc=new SqrtEnergyFunction();
		addFunction(sqrtEFunc.getName(), sqrtEFunc.getClass());
	}
	
	/**
	 * Maximum number of terms assigned by default to <code>POLYNOMIAL</code> type.
	 */
	public final static int MAX_TERMS = 5;

	/**
	 * Term labels.
	 */
	protected transient String[] labels;

	/**
	 * Name of calibration function.
	 */	
	protected transient String name;
	/** 
	 * This functions class
	 */
	protected transient Class funcClass;
	/**
	 * Title of calibration function.
	 */
	protected transient String title;
	/**
	 * Title of calibration function.
	 */
	protected transient boolean isFitPoints=true;

	/**
	 * Fit channels
	 */
	protected double[] ptsChannel;
	/**
	 * Fit energy
	 */
	protected double[] ptsEnergy;
	
	/**
	 * Coeffiecient values.
	 */
	protected double[] coeff;
	
	/**
	 * The formula for the function.
	 */
	protected transient StringBuffer formula=new StringBuffer();

	/**
	 * Returns the list of function names.
	 * @return the list of function names
	 */
	static List getListNames() {
		return Collections.unmodifiableList(NAMES);
	}
	
	/**
	 * Returns the map of function names to functions.
	 * @return the map of function names to functions
	 */
	public static Map getMapFunctions() {
		return Collections.unmodifiableMap(FUNCTIONS);
	}
	
	/**
	 * Clear the collections.
	 */
	private static void clearAll(){
		FUNCTIONS.clear();
		ICONS.clear();
		NAMES.clear();
	}
	
	private static void addFunction(String name, Class funcClass) {
		/* Only add once. */
		if (!FUNCTIONS.containsKey(name)){
			FUNCTIONS.put(name, funcClass);
			NAMES.add(name);
		}		
	}
	
	/**
	 * Sets an icon for the given function name.
	 * @param name of the function
	 * @param icon for the function
	 */
	public static void setIcon(String name, ImageIcon icon){
		ICONS.put(name, icon);
	}

	static ImageIcon getIcon(String name){
		return (ImageIcon)ICONS.get(name);
	}	

	/**
	 * Creates a new <code>CalibrationFunction</code> object.
	 *
	 * @param inClass function to use
	 * @param name name of function
	 * @param	numberTerms	number of terms in function
	 */
	AbstractCalibrationFunction(Class inClass, String name, int numberTerms) {
		this.funcClass = inClass;
		this.name=name;
		if (numberTerms < MAX_TERMS) {
			coeff=new double[numberTerms];
			labels=new String[numberTerms];
		} else {
			throw new IllegalArgumentException(getClass().getName()+
			"--Maximum terms: "+MAX_TERMS+", asked for: "+numberTerms);
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
	    final int len=labels.length;
	    final String [] rval = new String[len];
	    System.arraycopy(labels,0,rval,0,len);
		return rval;
	}
	
	/**
	 * Gets the calibration coefficients.
	 *
	 * @return array containing the calibration coefficients
	 */
	public double[] getCoeff() {
	    final int len=coeff.length;
	    final double [] rval = new double[len];
	    System.arraycopy(coeff,0,rval,0,len);
		return rval;
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
	 * Returns whether coeffecients are result of a fit.
	 * @return whether coeffecients are result of a fit
	 */
	public synchronized boolean isFitPoints() {
		return isFitPoints;
	}
	
	/**
	 * Sets whether coefficients are result of a fit.
	 * @param isFitIn whether coefficients are result of a fit
	 */
	private synchronized void setIsFitPoints(boolean isFitIn) {
		isFitPoints=isFitIn;
	}
	
	/**
	 * 
	 * @return the function formula
	 */
	public String getFormula(){
		updateFormula();
		return formula.toString();
	}

	
	/**
	 * Called by setCoeff() to update the formula.
	 *
	 */
	protected abstract void updateFormula(); 
	/**
	 * 
	 * @param ptsChannelIn
	 * @param ptsEnergyIn
	 */
	public void setPoints(double [] ptsChannelIn, double [] ptsEnergyIn) {
		setIsFitPoints(true);
		ptsChannel =new double [ptsChannelIn.length];
		ptsEnergy =new double [ptsEnergyIn.length];
		System.arraycopy(ptsChannelIn, 0, ptsChannel, 0, ptsChannelIn.length);
		System.arraycopy(ptsEnergyIn, 0, ptsEnergy, 0, ptsEnergyIn.length);
	}

	/**
	 * Get the input point channels.
	 * @return the input point channels
	 */
	public double [] getPtsChannel() {
	    final int len=ptsChannel.length;
	    final double [] rval=new double[len];
	    System.arraycopy(ptsChannel,0,rval,0,len);
		return rval;		
	}
	
	/**
	 * Get the input point energies.
	 * @return the input point energies
	 */
	public double [] getPtsEnergy() {
	    final int len=ptsEnergy.length;
	    final double [] rval = new double[len];
	    System.arraycopy(ptsEnergy,0,rval,0,len);
		return rval;		
	}
	
	/**
	 * Set the coefficients of the calibration function using the contents of the passed <code>Array</code>.
	 * If passed a larger than necessary array, the first elements of the array will be used.
	 *
	 * @param aIn   array of coefficients which should be at least as large as the number of coefficients
	 */
	public void setCoeff(double aIn[]) {
		setIsFitPoints(false);
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
	 * Do a calibration fit.
	 * @throws DataException if the fit fails
	 */
	public abstract void fit() throws DataException;

	/**
	 * do a linear regression of data points y=a+bx 
	 * returns to fit a, and b values in an array
	 *
	 * @param xVal array of x values
	 * @param yVal array of y values
	 * @throws DataException if regression fails
	 * @return array where first element is constant, second is slope
	 */
	protected double[] linearRegression(double[] xVal, double[] yVal)
		throws DataException {
		double sum = 0.0;
		double sumx = 0.0;
		double sumy = 0.0;
		double sumxx = 0.0;
		double sumxy = 0.0;
		double sumyy = 0.0;
		double weight = 1.0;
		double delta = 0.0;
		double aEst, bEst;
		double[] func = new double[2];
		final int numberPoints = xVal.length;
		for (int i = 0; i < numberPoints; i++) {
			weight = 1.0;
			sum = sum + weight;
			sumx = sumx + weight * xVal[i];
			sumy = sumy + yVal[i];
			sumxx = sumxx + weight * xVal[i] * xVal[i];
			sumxy = sumxy + weight * xVal[i] * yVal[i];
			sumyy = sumyy + weight * yVal[i] * yVal[i];
		}
		delta = sum * sumxx - sumx * sumx;
		if (delta == 0.0) {
			func[0] = 0.0;
			func[1] = 0.0;
			throw new DataException("Linear regression failed [CalibrationFunction]");
		} 
		aEst = (sumxx * sumy - sumx * sumxy) / delta;
        bEst = (sumxy * sum - sumx * sumy) / delta;
        func[0] = aEst;
        func[1] = bEst;
        return func;
	}
}
