  package jam.data;
  import jam.global.Function;
  
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
    public final static int MAX_NUMBER_TERMS=5;     
        
    protected int numberTerms;	    
    protected String title;
    protected String [] labels;
    protected double [] coeff;
        
    /**
     * Creates a new <code>CalibrationFunction</code> object.
     *
     * @param	numberTerms	number of terms in function
     * @exception   DataException   thrown if invalid <code>type</code> passed to constructor
     */        
    public CalibrationFunction(int numberTerms) throws DataException {
	if (numberTerms<MAX_NUMBER_TERMS) { 	        
	    this.numberTerms=numberTerms;    	
	} else {
	    throw new DataException("Number of terms greater than MAX_NUMBER_TERMS [CalibrationFunction]");
	}	    	    		    
    }
    public CalibrationFunction() throws DataException {
	this(MAX_NUMBER_TERMS);
    }
    
    /**
     * Number of terms
     */  
    public  int getNumberTerms(){
	return numberTerms;
    }		              
    /**
     * Given a type of <code>CalibrationFunction</code>, returns a title.  
     *
     * @return	a title indicating the form of the calibration function
     */
    public String getTitle(){
	return title;
    }
    /**
     * Given a type of <code>CalibrationFunction</code>, returns an array of parameter labels.
     *
     * @return	an array of parameter labels
     */
    public String [] getLabels() {
	return labels;
    }
    /**
     * Gets the calibration coefficients.
     *
     * @return array containing the calibration coefficients
     */    
    public double [] getCoeff(){
	return coeff;
    }
    /**
     * Get the calibration value for a particular <code>double</code> value. 
     * Actually a cast to <code>int</code> is done for now.
     *
     * @param	x	value at which to get calibration
     * @return	calibration value of the argument
     * @see #getValue(int)
     */    
    public double getValue(int x){
	return getValue((double)x);
    }  
        // Added to provide energy calibration for goto button
    public double getEnergy(int y){
   	return getEnergy((double)y);
    }      
    public double getCalculatedEnergy(double energy){
   	return getValue((double) energy);
    }  
    /**
     * Set the coefficients of the calibration function using the contents of the passed <code>Array</code>.
     * If passed a larger than necessary array, the first elements of the array will be used.
     *
     * @param aIn   array of coefficients which should be at least as large as the number of coefficients
     */    
    public abstract void setCoeff(double aIn[]) throws DataException;    
    /**
     * Get the calibration value at a specified channel.
     * 
     * @param	channel	value at which to get calibration
     * @return	calibration value of the channel
     */    
    public abstract double getValue(double channel);
    public abstract double getEnergy(double energy);
    /**
     * Do a calibration fit
     * 
     * @param x array of x values
     * @param y array of y values
     * @return	String that can be printed 
     */        
    public abstract String fit(double []x, double []y) throws DataException;    
    
    /**
     * do a linear regression of data points y=a+bx 
     * returns to fit a, and b values in an array
     *
     * @param x array of x values
     * @param y array of y values
     */
     
    protected double [] linearRegression(double []x, double []y) throws DataException {

	double sum=0.0;        
	double sumx=0.0;
	double sumy=0.0;
	double sumxx=0.0;
	double sumxy=0.0;
	double sumyy=0.0;
	double weight=1.0;
	double delta=0.0;
	double a, b;
	double [] func=new double [2];
	
	int numberPoints=x.length;	
	for(int i=0;i<numberPoints;i++){
	    weight=1.0;
	    sum=sum+weight;
	    sumx=sumx+weight*x[i];
	    sumy=sumy+y[i];
	    sumxx=sumxx+weight*x[i]*x[i];	    	        
	    sumxy=sumxy+weight*x[i]*y[i];
	    sumyy=sumyy+weight*y[i]*y[i];	    	    
	}	    
	
	delta=sum*sumxx-sumx*sumx;
	if (delta!=0.0){
	    a=(sumxx*sumy-sumx*sumxy)/delta;	    
	    b=(sumxy*sum-sumx*sumy)/delta;
	    func[0]=a;
	    func[1]=b;	    	

	} else {
	    func[0]=0.0;
	    func[1]=0.0;	    		
	    throw new DataException("Linear regression failed [CalibrationFucntion]");
	}		
	return func;
    }    
    
}    
