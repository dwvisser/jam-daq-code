 package jam.data;
 import java.util.*;
 import jam.util.*;
 
/**
 * Class for user-defined numerical parameters that can be used during sorting.  
 * Jam creates a dialog box for the user to enter values
 * for these parameters.
 * In a sort routine you use parameter.getValue() to get the value entered into
 * the dialog box.
 *
 * @author Ken Swartz
 * @version 0.9
 * @see jam.RunControl
 * @since JDK1.1
 */

public class DataParameter {



    static Hashtable parameterTable=new Hashtable(11);
    static Vector parameterList=new Vector(11);    

    /**
     * Limit on name length.
     */
    public final static int NAME_LENGTH = 16;
    

    private String name;	//parameter name
    private double value;		//parameter value



    /**
     * Creates a new parameter with the given name.
     * 
     * @param name the name for the new parameter used in the dialog box
     */ 
    public DataParameter (String name) throws DataException {

	//give error if name is too long
	if(name.length() > NAME_LENGTH) {
	    throw new DataException("Parameter name '"+name+"' too long "+
		NAME_LENGTH+" characters or less.  Please modify sort file.");
	}		
	name=StringUtilities.makeLength(name, NAME_LENGTH);	
	
	//make sure name is unique
	int prime=1;
	String addition;
	while(parameterTable.containsKey(name)){
	    addition="["+prime+"]";
	    name=StringUtilities.makeLength(name,NAME_LENGTH - addition.length())+addition;
	    prime++;

	}
		
	this.name=name;
	this.value=0.0;		//default zero value	

	// Add to list of parameters    	
    	parameterTable.put(name, this);    
    	parameterList.addElement(this);    	
    }
    
    /** 
     * Returns the list of all parameters.
     *
     * @return the list of all currently defined parameters.
     */
    public static Vector getParameterList(){
	return parameterList;
    }
    
    /**
     * Sets the list of all parameters.
     *
     * @param inParameterList list of all parameters
     */
    public static void setParameterList(Vector inParameterList){
    
	Enumeration allParameters;
	DataParameter parameter;
	
	//clear current lists
	parameterTable.clear(); 
	parameterList.removeAllElements();
	
	allParameters=inParameterList.elements();	
	
	//loop for all parameters

	while( allParameters.hasMoreElements()) {
	    parameter=(DataParameter)allParameters.nextElement();
	    parameterTable.put(parameter.getName(), parameter);
	    parameterList.addElement(parameter); 
	}

    }
    
    /**
     * Clears the list of parameters.
     */
    public static void  clearList(){
	parameterTable.clear();	   

	parameterList.removeAllElements();
	//run garbage collector, memory should be freed
	System.gc();
    }
     /**
     * Returns the parameter with the specified name.
     *
     * @param name the name of the desired parameter 
     * @return the parameter with the specified name
     */
    public static DataParameter getParameter(String name){
		return (DataParameter)parameterTable.get(name);
    }

    /**
     * Returns the name of this parameter.
     */

    public String getName(){

        return name ;

    }
    
    /**
     * Returns the the <code>double</code> of this parameter.
     * 
     * @return the value of this parameter
     */    
    public double getValue(){
	return value;
    }
    
    /**    
     * Sets the new value for this parameter.
     *
     * @param valueIn new value for this parameter
     */
    public  void setValue(double valueIn){
	value=valueIn;
    }
}    

