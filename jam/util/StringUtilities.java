package jam.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Contains utilities for manipulating <code>String</code> objects.
 *  
 * @author  Dale Visser	
 * @version 0.5
 * @see java.lang.String
 */
public final class StringUtilities {
	
	private static final StringUtilities INSTANCE=new StringUtilities();
	private final Charset ASCII = (Charset)Charset.availableCharsets().get("US-ASCII");
	
	private StringUtilities(){
		super();
	}

	/**
	 * Get the only instance of this class.
	 * 
	 * @return the only instance of this class
	 */
	public static final StringUtilities instance(){
		return INSTANCE;
	}

	/**
	 * Truncates a <code>String</code> or pads the end with spaces to make it a 
	 * certain length.
	 *
	 * @param	input	<code>String</code> to modify
	 * @param	length	desired number of characters in the <code>String</code>
	 * @return	<code>String</code> with <code>length</code> characters
	 */
	public String makeLength(String input, int length) {
		final StringBuffer temp = new StringBuffer(input);
		for (int i = input.length(); i < length; i++) {
			temp.append(' ');
		}
		return temp.substring(0, length);
	}
	
	/**
	 * Creates a <code>String</code> from the given US-ASCII byte array.
	 * @param input US-ASCII characters as bytes
	 * @return representation of the given array
	 */
	public String getASCIIstring(byte [] input){
	    final ByteBuffer buffer = ByteBuffer.wrap(input);
	    final CharBuffer charBuffer = ASCII.decode(buffer);
	    return charBuffer.toString();
	}	
	
	public byte [] getASCIIarray(String input){
	    final ByteBuffer buffer = ASCII.encode(input);
	    return buffer.array();
	}
}
