package jam.util;
import java.io.*;
import java.text.NumberFormat;

/**
 * Contains utilities for manipulating <code>String</code> objects.
 *  
 * @author  Dale Visser	
 * @version 0.5
 * @see	    java.lang.String
 */
public class StringUtilities {

	/**
	 * Truncates a <code>String</code> or pads the end with spaces to make it a 
	 * certain length.
	 *
	 * @param	in	<code>String</code> to modify
	 * @param	length	desired number of characters in the <code>String</code>
	 * @return	<code>String</code> with <code>length</code> characters
	 */
	public static String makeLength(String in, int length) {
		String temp = in;

		for (int i = 0; i < length; i++) {
			temp = temp + " ";
		}
		temp = temp.substring(0, length);
		return temp;
	}

	public static byte[] ASCIIarray(String in) {
		ByteArrayOutputStream bos;
		DataOutputStream dos;
		byte[] out;

		bos = new ByteArrayOutputStream(in.length());
		dos = new DataOutputStream(bos);
		try {
			dos.writeBytes(in);
		} catch (IOException ioe) {
			System.err.println("StringUtilities.ASCIIarray(): " + ioe);
		}
		out = bos.toByteArray();
		return out;
	}
	
	public static String roundDecimal(double value, int places){
		NumberFormat fval=NumberFormat.getInstance();
		fval.setGroupingUsed(false);
		fval.setMinimumFractionDigits(places);
		fval.setMaximumFractionDigits(places);
		return fval.format(value);	
	}

}
