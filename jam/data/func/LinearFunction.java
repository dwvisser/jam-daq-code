  package jam.data.func;
  import jam.data.*;
  
/**
 * A polynomial function that can be use to calibrate a histogram.  Most often used to define energy
 * calibrations of spectra.
 */
public class LinearFunction extends CalibrationFunction {

    private final int NUMBER_TERMS=2;		            
    /**
     * Creates a new <code>LinearFunction</code> object of the specified type.  
     *
     * @exception   DataException   thrown if invalid <code>type</code> passed to constructor
     */        
    public LinearFunction() throws DataException {
	super(2);
	title="E = a0+a1*ch";		
	numberTerms=NUMBER_TERMS;
	coeff =new double [numberTerms];		
	labels=new String [numberTerms];	
	labels[0]="a(0)";
	labels[1]="a(1)";    	    
    }
    /**
     * Set the coefficients of the calibration function using the contents of the passed <code>Array</code>.
     * If passed a larger than necessary array, the first elements of the array will be used.
     *
     * @param aIn   array of coefficients which should be at least as large as the number of coefficients
     */    
    public void setCoeff(double aIn[]) throws DataException {
	
	if (aIn.length==numberTerms){
	    System.arraycopy(aIn, 0, coeff, 0, aIn.length);			        
	} else {
	    throw new DataException ("Not the correct number of terms [LinearFunction]");	    
	}	    
    }    
    /**
     * Get the calibration value at a specified channel.
     * 
     * @param	channel	value at which to get calibration
     * @return	calibration value of the channel
     */    
    public double getValue(double channel){

	return coeff[0]+coeff[1]*channel;	

    }  
        /**
     * Get the calibration value at a specified channel.
     * 
     * @param	channel	value at which to get calibration
     * @return	calibration value of the channel
     */    
    public double getEnergy(double channel){

	return ((channel-coeff[0])/coeff[1]);	

    }       
    /**
     * do a fit of x y values
     */
    public String fit(double []x, double []y) throws DataException{
	coeff=linearRegression(x, y);
	return "Linear fit";
//FIXME		    "E = "+numFormat.format(a)+" + "+numFormat.format(b)+" x ch");	    				    	
    }
}    
