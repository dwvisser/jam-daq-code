/*
 *
 */
package jam.fit; 
import java.util.Vector;

/**
 * This abstract class uses <code>NonLinearFit</code> to fit a single gaussian peak with a background..
 * The background is a polynomial up to a quadradic term if desired. (Channel - Centroid) is the term
 * the polynomial is expanded in.
 *
 * @author  Dale Visser
 * @version 0.5, 8/31/98
 *
 * @see	    NonLinearFit
 */
 public class GaussianFit extends NonLinearFit {
    /**
     */

    /**
     * magic number for calculating
     */
    private final double a=0.93911;
 
    /**
     * magic number for calculating
     */
    private final double b=2.77066;
 
    /**
     * magic number for calculating
     */
    private final double c=5.20391;
 
    /**
     * magic number for calculating
     */
    static final double SIGMA_TO_FWHM=2.354;
 
    /**
     * name of <code>Parameter</code>--centroid of peak
     */
    public static final String CENTROID="Centroid";
 
    /**
     * name of <code>Parameter</code>--width of peak
     */
    public static final String WIDTH="Width";
 
    /**
     * name of <code>Parameter</code>--area of peak
     */
    public static final String AREA="Area";
 
    /**
     * function <code>Parameter</code>--area of peak
     */
    private Parameter area;
 
    /**
     * function <code>Parameter</code>--centroid of peak
     */
    private Parameter centroid;
 
    /**
     * function <code>Parameter</code>--wodth of peak
     */
    private Parameter width;
 
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
     * used for calculations
     */
    private double diff;
 
    /**
     * used for calculations
     */
    private double exp;

    /**
     * Class constructor.
     */
    public GaussianFit(){
	super("GaussianFit");
	
	//background=new Parameter("bg",Parameter.TEXT,"Background: A+B(x-Centroid)+C(x-Centroid)^2");
	//equation=new Parameter("eq",Parameter.TEXT,"Peak: 2.354*Area/(Sqrt(2 pi)Width)*exp[-2.354^2(x-Centroid)^2/2 Width^2]");
	area=new Parameter(AREA, Parameter.DOUBLE, Parameter.FIX, Parameter.ESTIMATE);
	area.setEstimate(true);
	centroid=new Parameter(CENTROID, Parameter.DOUBLE,  Parameter.FIX,  Parameter.MOUSE);
	width=new Parameter(WIDTH, Parameter.DOUBLE, Parameter.FIX, Parameter.ESTIMATE);
	width.setEstimate(true);
	A=new Parameter("A", Parameter.DOUBLE, Parameter.FIX, Parameter.ESTIMATE);
	A.setEstimate(true);
	B=new Parameter("B", Parameter.FIX);
	B.setFix(true);	
	C=new Parameter("C", Parameter.FIX);
	C.setFix(true);	
	
	addParameter(area);
	addParameter(centroid);
	addParameter(width);
	addParameter(A);
	addParameter(B);
	addParameter(C);					
	
    }
    
    /**
     * If so requested, estimates A, Area, and Width.
     *
     * @exception   FitException	    thrown if unrecoverable error occurs during estimation
     */
    public void estimate() throws FitException{
    
	orderParameters();
	
	int i,minCH,maxCH,peakLow,peakHigh;
	double area,width,centroid,distance,sigma,variance,backLevel,intWidth;
    
	minCH=getParameter(FIT_LOW).getIntValue();
	maxCH=getParameter(FIT_HIGH).getIntValue();
	centroid=getParameter(CENTROID).getDoubleValue();
	width=getParameter(WIDTH).getDoubleValue();
	backLevel=getParameter("A").getDoubleValue();
	area=getParameter(AREA).getDoubleValue();
	    
	    //estimated level of background
	    if (getParameter("A").isEstimate()){
		backLevel=((double)counts[minCH]+(double)counts[maxCH])*0.5;
		getParameter("A").setValue(backLevel);
		System.out.println("Estimated A = "+backLevel);
	    }
	    
	    //sum up counts	
	    if (getParameter(AREA).isEstimate()){
		area=0.0;
		for (i=minCH;i<=(int)maxCH;i++){

		    area+=(double)counts[i]-backLevel;

		}
		getParameter(AREA).setValue(area);
		System.out.println("Estimated area = "+area);
	    }

	    //find width	    
	    variance=0.0;
	    if (getParameter(WIDTH).isEstimate()){
		for (i=minCH;i<=maxCH;i++){
		    distance=(double)(i)-centroid;

		    variance+=((double)counts[i]/area)*(distance*distance);		    

		}
		sigma=Math.sqrt(variance);
		width=SIGMA_TO_FWHM*sigma;
		getParameter(WIDTH).setValue(width);
		System.out.println("Estimated width = "+width);
	    }
    }
    
    /**
     * Overrides normal setParameters to make sure channels are in proper order.  This
     * Allows the fit limits and centroids to be clicked in any order.
     */
    private void orderParameters() throws FitException{
    
	Matrix chVector=new Matrix(3,1);
	Matrix sorted;
    	
	chVector.element[0][0]=getParameter(FIT_LOW).getIntValue();
	chVector.element[1][0]=getParameter(CENTROID).getDoubleValue();
	chVector.element[2][0]=getParameter(FIT_HIGH).getIntValue();
		
	sorted=chVector.sort();
	
	getParameter(FIT_LOW).setValue((int)sorted.element[0][0]);
	getParameter(CENTROID).setValue(sorted.element[1][0]);
	getParameter(FIT_HIGH).setValue((int)sorted.element[2][0]);
	
    }

    /**
     * Calculates the gaussian with background at a given x.
     *
     * @param	x   value to calculate at
     * @return	    value of function at x
     */
    public double valueAt(double x){
    	diff=x-p(CENTROID);
	exp=Math.exp(-b*diff*diff/(p(WIDTH)*p(WIDTH)));
	
	double temp=p("A")+p("B")*diff+p("C")*diff*diff+p(AREA)/p(WIDTH)*a*exp;
	return temp;
    }
    
    /**
     * Evaluates derivative with respect to <code>parameterName</code> at <code>x</code>.
     *
     * @param   parameterName   the name of the parameter to differentiate with respect to
     * @param   x		value to evalueate at
     * @return			df(<code>x</code>)/d(<code>parameterName</code>) at x
     */
    public double derivative(double x, String parName){
	double temp;

    	diff=x-p(CENTROID);
	exp=Math.exp(-b*diff*diff/(p(WIDTH)*p(WIDTH)));
	
	diff=x-p(CENTROID);
    	if (parName.equals(AREA)){
	    temp=a/p(WIDTH)*exp;
	} else if (parName.equals(CENTROID)){
	    temp=c*p(AREA)*exp*diff/(p(WIDTH)*p(WIDTH)*p(WIDTH))-p("B")-2*p("C")*diff;
	} else if (parName.equals(WIDTH)) {
	    temp=-a*p(AREA)*exp/(p(WIDTH)*p(WIDTH));
	    temp=temp + c*p(AREA)*exp*diff*diff/(p(WIDTH)*p(WIDTH)*p(WIDTH)*p(WIDTH));
	} else if (parName.equals("A")) {
	    temp=1.0;
	} else if (parName.equals("B")) {
	    temp=diff;
	} else if (parName.equals("C")) {
	    temp=diff*diff;
    	} else {//not valid
	    temp=0.0;
	    System.err.println("Invalid derivative argument: "+parName);
	}
	return temp;
    }
    
}