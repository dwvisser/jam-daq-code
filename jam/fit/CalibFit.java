/*
 *
 */
package jam.fit; 

/**
 * Makes a linear fit to a number of points
 *
 * @author  Ken Swartz
 * @version 0.5, 8/31/98
 *
 * @see	    NonLinearFit
 */
 public abstract class CalibFit extends Fit {
    /**
     */
    static final int NUMBER_POINTS=8;
 
    /**
     * magic number for calculating
     */
    static final double SIGMA_TO_FWHM=2.354;
  
    /**
     * function <code>Parameter</code>--area of peak
     */
    private Parameter points[];
  
    /**
     * function <code>Parameter</code>--constant background term
     */
    private Parameter A;
 
    /**
     * function <code>Parameter</code>--linear background term
     */
    private Parameter B;
 
    /**
     * function <code>Parameter</code>--quadratic background term
     */
    private Parameter C;
 

    /**
     * Class constructor.
     */
    public CalibFit(){
	super("Calibration Fit");
	
	points= new Parameter[NUMBER_POINTS];
	String name;
    
	for (int i=0; i< NUMBER_POINTS; i++){
	    name="point "+i;
	    points[i]=new Parameter(name, Parameter.DOUBLE, Parameter.KNOWN, Parameter.NO_ERROR, Parameter.MOUSE);
	    addParameter(points[i]);
	}	    
	A=new Parameter("A",Parameter.DOUBLE, Parameter.ERROR, Parameter.FIX);
	addParameter(A);		
	B=new Parameter("B",Parameter.DOUBLE, Parameter.ERROR, Parameter.FIX);	
	addParameter(B);		
	C=new Parameter("C",Parameter.DOUBLE, Parameter.ERROR, Parameter.FIX);	
	addParameter(C);
	
    }
    
    /**
     *
     */
    public void estimate(){
    }
    
    /**
     * Performs the calibration fit.
     *
     * @return	    message for fit dialog
     * @exception   FitException	    thrown if something goes wrong in the fit
     */
    public String doFit() throws FitException{
	double a, b, c;
	System.out.println("value of point 0 "+points[0].getDoubleValue());
	a=3.0;
	b=4.0;
	c=5.0;
	A.setValue(a);
	B.setValue(b);	
	C.setValue(c);		
	return "hello";
    }
    
    public double calculate(int x){
	return 1.0;    
    }
    
}