package jam.data;
import jam.global.Nameable;
import jam.util.StringUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for user-defined numerical parameters that can be used during sorting.  
 * Jam creates a dialog box for the user to enter values
 * for these parameters.
 * In a sort routine you use parameter.getValue() to get the value entered into
 * the dialog box.
 *
 * @author Ken Swartz
 * @version 0.9
 * @see jam.sort.control.RunControl
 * @since JDK1.1
 */

public class DataParameter implements Nameable {

	private static final Map<String, DataParameter> TABLE = Collections.synchronizedMap(new HashMap<String,DataParameter>());
	private static final List<DataParameter> LIST = Collections.synchronizedList(new ArrayList<DataParameter>());

	/**
	 * Limit on name length.
	 */
	public final static int NAME_LENGTH = 16;

	private transient final String name; //parameter name
	private double value; //parameter value

	/**
	 * Creates a new parameter with the given name.
	 * 
	 * @param name the name for the new parameter used in the dialog box
     * @throws IllegalArgumentException if name >NAME_LENGTH characters
	 */
	public DataParameter(String name)  {
		super();
		final StringUtilities stringUtil=StringUtilities.getInstance();
		//give error if name is too long
		if (name.length() > NAME_LENGTH) {
			throw new IllegalArgumentException(
				"Parameter name '"
					+ name
					+ "' too long "
					+ NAME_LENGTH
					+ " characters or less.  Please modify sort file.");
		}
		String workingName = stringUtil.makeLength(name, NAME_LENGTH);
		//make sure name is unique
		int prime = 1;
		String addition;
		while (TABLE.containsKey(name)) {
			addition = "[" + prime + "]";
			workingName =
				stringUtil.makeLength(
					name,
					NAME_LENGTH - addition.length())
					+ addition;
			prime++;
		}
		this.name = workingName;
		this.value = 0.0; //default zero value	
		// Add to list of parameters    	
		TABLE.put(name, this);
		LIST.add(this);
	}

	/** 
	 * Returns the list of all parameters.
	 *
	 * @return the list of all currently defined parameters.
	 */
	public static List<DataParameter> getParameterList() {
		return Collections.unmodifiableList(LIST);
	}

	/**
	 * Sets the list of all parameters.
	 *
	 * @param inList list of all parameters
	 */
	public static void setParameterList(final List<DataParameter> inList) {
		/* clear current lists */
		TABLE.clear();
		LIST.clear();
		for (DataParameter parameter : inList){
			TABLE.put(parameter.getName(), parameter);
			LIST.add(parameter);
		}
	}

	/**
	 * Clears the list of parameters.
	 */
	public static void clearList() {
		TABLE.clear();
		LIST.clear();
	}
	
	/**
	* Returns the parameter with the specified name.
	*
	* @param name the name of the desired parameter 
	* @return the parameter with the specified name
	*/
	public static DataParameter getParameter(final String name) {
		return TABLE.get(name);
	}

	/**
	 * Returns the name of this parameter.
	 * 
	 * @return the name
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
	public void setValue(final double valueIn) {
		value = valueIn;
	}
}
