package jam.util;

/**
 * Contains utilities for manipulating <code>String</code> objects.
 *  
 * @author  Dale Visser	
 * @version 0.5
 * @see	    java.lang.String
 */
public class StringUtilities {
	
	private static final StringUtilities SU=new StringUtilities();
	
	private StringUtilities(){
		super();
	}

	public static final StringUtilities instance(){
		return SU;
	}

	/**
	 * Truncates a <code>String</code> or pads the end with spaces to make it a 
	 * certain length.
	 *
	 * @param	in	<code>String</code> to modify
	 * @param	length	desired number of characters in the <code>String</code>
	 * @return	<code>String</code> with <code>length</code> characters
	 */
	public String makeLength(String in, int length) {
		String temp = in;

		for (int i = 0; i < length; i++) {
			temp = temp + " ";
		}
		temp = temp.substring(0, length);
		return temp;
	}
}
