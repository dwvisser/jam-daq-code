/**
 */
package jam.fit;
import java.text.NumberFormat;
import java.lang.Math;
    /**
     * <p>Parameters used to fit. </p>
     * <dl>
     * <dt>INT</dt>	<dd>integer, such as Number of Peaks, or Minimum Channel</dd>    
     *	<dt>DOUBLE</dt>	<dd>standard variable fit parameter, includes a "fix value" checkbox </dd>    
     *	<dt>TEXT</dt>	<dd>field showing fit function and/or brief instructions </dd>    
     *	<dt>BOOLEAN</dt>	<dd>true/false option, such as Include Background, or Display Output (how this would be
     *			implemented, I'm not sure)</dd>
     * </dl>
     * <p>Options. You can use as many options as you want.</p>
     * <dl>
     *	<dt>(NO_)OUTPUT</dt>	<dd>is calculated and has no associated error bars (e.g. Chi-Squared)</dd>
     *	<dt>(NO_)MOUSE</dt>	<dd> value can be obtained with mouse from screen</dd>
     *  <dt>(NO_)ESTIMATE</dt>	<dd>can be estimated</dd>
     * <dt>(NO_)FIX</dt><dd>value is fixed..do not vary during fit</dd>
     * </dl>
     */
	public class Parameter {
    
    	// options 
		public final static int TYPE=7;     
    	// different types 
    	
    	/**
     	 * Parameter is an integer number, e.g., a histogram channel number.
     	 */
    	public final static int INT=1;  
    
    	/**  
     	 * Parameter is a floating point number.
     	 */
    	public final static int DOUBLE=2;
    
    	/**
     	 * Parameter is a boolean value...displayed as a checkbox.
     	 */
    	public final static int BOOLEAN=3;    
    
    /**
     * Parameter is simply a text box.
     */
    public final static int TEXT=4;
    // other options        
    public final static int ERROR=0;	    //default
    public final static int NO_ERROR=8;
    public final static int FIX=16; 
    public final static int NO_FIX=0;	    //default    
    public final static int ESTIMATE=32;  
    public final static int NO_ESTIMATE=0;  //default            
    public final static int MOUSE=64;
    public final static int NO_MOUSE=0;    //default
    public final static int OUTPUT=128;
    public final static int NO_OUTPUT=0;    //default
    public final static int KNOWN=256;
    public final static int NO_KNOWN=0;    //default
    
    /* *
     * type contains the parameter type.  Its value is available via getType().
     * The types are: boolean,double,text, or int
     *	input/output or output only
     *	clickable
     *	estimable
     */
    int options;
    int type;
//    int index;			    //not currently used
    boolean errorOption	    =true;
    boolean estimateOption  =false;   
    boolean fixOption	    =false;
    boolean mouseOption	    =false;
    boolean outputOption    =false;
    boolean knownOption	    =false;        
    //parameter name
    String name;
    //double fields
    double valueDbl;
    // error field
    double errorDbl;      
    //int fields
    int valueInt;
    //boolean fields
    boolean valueBln;
    //TEXT fields
    String valueTxt;
    
    double known;
    
    double valueDefaultInt;
    double valueDefaultDbl;
    
    /**
     * Whether or not the parameter is currently fixed.
     */    
    protected boolean fix;
    
    /**
     * Whether or not this parameter should be estimated automatically before doing fit.
     */
    protected boolean estimate;      
    
    public Parameter(String name, int options){//default variable parameter instance
	this.name=name;
	this.options=options;
	type=options&TYPE;
	if (type==Parameter.TEXT) options |= NO_ERROR; //change default for TEXT

	valueInt=0;	
	valueDbl=0.0;
	errorDbl=0.0;
	fix=false;
	estimate=false;
	
	//default type	
	if (type==0){
	    type=DOUBLE;
	}
	if (type==BOOLEAN){
	    errorOption=false;
	}
		    	   	
	if ((options&Parameter.NO_ERROR)!=0) {	 
	    errorOption=false;
	}	    
	
	if ((options&Parameter.FIX)!=0) {	 
	    fixOption=true;
	}	    
	if ((options&Parameter.ESTIMATE)!=0) {	 
	    estimateOption=true;
	}	    
	if ((options&Parameter.MOUSE)!=0) {	 
	    mouseOption=true;
	}	    
	if ((options&Parameter.OUTPUT)!=0) {	 
	    outputOption=true;
	}	    
	if ((options&Parameter.KNOWN)!=0) {	 
	    knownOption=true;
	}	    
	
    }
    
    public Parameter(String name, int option1, int option2){
		this(name, option1|option2);
    }
    
    public Parameter(String name, int option1, int option2, int option3){
		this(name, option1|option2|option3);
    }
    
    public Parameter(String name, int option1, int option2, int option3, int option4){
		this(name, option1|option2|option3|option4);
    }
    
    public Parameter(String name, int option1, int option2, int option3, int option4, int option5){
		this(name, option1|option2|option3|option4|option5);
    }
    
    public Parameter(String name, int option1, int option2, int option3, int option4, int option5, int option6){
		this(name, option1|option2|option3|option4|option5|option6);
    }
    
    //END OF CONSTRUCTORS

    public String getName(){
		return name;
    }
    public int getType(){
		return type;
    }    
    public int getOptions(){
		return options;
    }

    public void setFix(boolean state){
		this.fix=state;
    }
    
    /**
     * Tells whether the parameter is currently fixed.
     */
    public boolean isFix(){
		return fix;
    }
    
    public void setEstimate(boolean state){
		estimate=state;
    }
    
    public boolean isEstimate(){ 
		return(fix?false:estimate);
    }
    
    /**
     * Set the floating point value.
     *
     * @exception   FitException	    thrown if unrecoverable error occurs
     */
    public void setValue(double value) throws FitException{
		if (type==DOUBLE){
	    	valueDbl=value;
		} else throw new FitException("Parameter '"+name+"' can't set a double value.");
    }
    
    public void setValue(double value, double error){
		valueDbl=value;
		errorDbl=error;
    }
    
    public void setValue(int value){
		valueInt=value;
    }
    
    
    public void setValue(String text){
		valueTxt=text;
    }
    public void setValue(boolean flag){
		valueBln=flag;
    }
    
    public void setError (double err){
		errorDbl=err;
    }
    
    public void setKnown (double inKnown){
		known=inKnown;
    }
    
    public double getDoubleValue(){
		return valueDbl;
    }
    
    public int getIntValue(){
		return valueInt;
    }    
    
    public boolean getBooleanValue(){
		return valueBln;
    }
    
    public double getDoubleError(){
		return errorDbl;
    }
    
    public double getKnown(){
		return known;
    }
    
    public boolean isBoolean(){
		return (type==BOOLEAN);
    }    
    
    public boolean isNumberField(){
		return (type==INT||type==DOUBLE);
    }
    
    /**
     * Returns true if the parameter is represented by a text field in the dialog box.
     */
    public boolean isTextField(){
		return (type==INT||type==DOUBLE||type==TEXT);
    }
    
    boolean canBeEstimated() {
		return ((options&ESTIMATE)!=0);
    }
}
    