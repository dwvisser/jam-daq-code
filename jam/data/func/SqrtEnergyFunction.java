  package jam.data.func;
  import jam.data.*;
  
/**
 * A sqrt function that can be use to calibrate a histogram taken by a
 * magnetic spectometer.
 *  
 */
public class SqrtEnergyFunction extends CalibrationFunction {

    private final int NUMBER_TERMS=2;		            
    /**
     * Creates a new <code>SqrtEnergyFunction </code> object of the specified type.  
     *
     * @exception   DataException   thrown if invalid <code>type</code> passed to constructor
     */        
    public SqrtEnergyFunction() throws DataException {
	super(2);
	title="sqrt(E) = a0+a1*ch";		
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

	return (coeff[0]+coeff[1]*channel)*(coeff[0]+coeff[1]*channel);	

    }    

// To be implemented later when this function works
    public double getEnergy(double channel){

	return ((channel-coeff[0])/coeff[1]);	

    }       
    /**
     * do a fit of x y values
     */
    public String fit(double []chan, double []ene) throws DataException{
	double []sqrtE;
	sqrtE=new double [ene.length];
	for(int i=0; i< ene.length; i++){
	    sqrtE[i]=Math.sqrt(ene[i]);
	}
	coeff=linearRegression(chan, sqrtE);	
	return "Sqrt Energy Function"+"sqrt(E) = "+coeff[0]+" + "+coeff[1]+" x (ch)";
    }
    
}    
