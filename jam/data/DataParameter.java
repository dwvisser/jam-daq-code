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

	private static final Map parameterTable = Collections.synchronizedMap(new HashMap());
	private static final List parameterList = Collections.synchronizedList(new ArrayList());

	/**
	 * Limit on name length.
	 */
	public final static int NAME_LENGTH = 16;

	private final String name; //parameter name
	private double value; //parameter value

	/**
	 * Creates a new parameter with the given name.
	 * 
	 * @param name the name for the new parameter used in the dialog box
     * @throws UnsupportedArgumentException if name >NAME_LENGTH characters
	 */
	public DataParameter(String name)  {
		StringUtilities su=StringUtilities.instance();
		//give error if name is too long
		if (name.length() > NAME_LENGTH) {
			throw new IllegalArgumentException(
				"Parameter name '"
					+ name
					+ "' too long "
					+ NAME_LENGTH
					+ " characters or less.  Please modify sort file.");
		}
		name = su.makeLength(name, NAME_LENGTH);
		//make sure name is unique
		int prime = 1;
		String addition;
		while (parameterTable.containsKey(name)) {
			addition = "[" + prime + "]";
			name =
				su.makeLength(
					name,
					NAME_LENGTH - addition.length())
					+ addition;
			prime++;
		}
		this.name = name;
		this.value = 0.0; //default zero value	
		// Add to list of parameters    	
		parameterTable.put(name, this);
		parameterList.add(this);
	}

	/** 
	 * Returns the list of all parameters.
	 *
	 * @return the list of all currently defined parameters.
	 */
	public static List getParameterList() {
		return Collections.unmodifiableList(parameterList);
	}

	/**
	 * Sets the list of all parameters.
	 *
	 * @param inParameterList list of all parameters
	 */
	public static void setParameterList(List inParameterList) {
		DataParameter parameter;

		//clear current lists
		parameterTable.clear();
		parameterList.clear();

		Iterator allParameters = inParameterList.iterator();

		//loop for all parameters

		while (allParameters.hasNext()) {
			parameter = (DataParameter) allParameters.next();
			parameterTable.put(parameter.getName(), parameter);
			parameterList.add(parameter);
		}

	}

	/**
	 * Clears the list of parameters.
	 */
	public static void clearList() {
		parameterTable.clear();
		parameterList.clear();
		//run garbage collector, memory should be freed
		System.gc();
	}
	/**
	* Returns the parameter with the specified name.
	*
	* @param name the name of the desired parameter 
	* @return the parameter with the specified name
	*/
	public static DataParameter getParameter(String name) {
		return (DataParameter) parameterTable.get(name);
	}

	/**
	 * Returns the name of this parameter.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the the <code>double</code> of this parameter.
	 * 
	 * @return the value of this parameter
	 */
	public double getValue() {
		return value;
	}

	/**    
	 * Sets the new value for this parameter.
	 *
	 * @param valueIn new value for this parameter
	 */
	public void setValue(double valueIn) {
		value = valueIn;
	}
}
