package jam.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

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
	 * Make a unique name out of the given name that differs from
	 * names in the given set.
	 * 
	 * @param name name to make unique
	 * @param nameSet contains the existing names
	 * @return unique name
	 */
	public String makeUniqueName(String name, Set nameSet) {
		
		String nameTemp = name.trim();
		boolean isUnique=false;
		int prime = 1;
		boolean copyFound;
		
		/* find a name that does not conflict with existing names */
		while(!isUnique) {
			copyFound=false;
			Iterator nameIter = nameSet.iterator();
			while (nameIter.hasNext()){
				String nameNext=(String)nameIter.next();
				if (nameTemp.compareTo(nameNext)==0) {
					copyFound=true;
					break;
				}
			}
			if (copyFound) { 
				final String nameAddition = "[" + prime + "]";
				nameTemp = name+nameAddition;
				prime++;				
			} else {
				isUnique=true;
			}
		}
		
		return nameTemp;
	}
	
	/**
	 * Make a unique name out of the given name that differs from
	 * names in the given set.
	 * 
	 * @param name name to make unique
	 * @param nameSet contains the existing names
	 * @return unique name
	 */
	public String makeUniqueName(String name, Set nameSet, int nameLength) {
		
		String nameTemp = makeLength(name, nameLength);
		boolean warn=name.length()>nameTemp.length();
		boolean isUnique=false;
		int prime = 1;
		boolean copyFound;
		
		/* find a name that does not conflict with existing names */
		while(!isUnique) {
			copyFound=false;
			Iterator nameIter = nameSet.iterator();
			while (nameIter.hasNext()){
				String nameNext=(String)nameIter.next();
				if (nameTemp.compareTo(nameNext)==0) {
					copyFound=true;
					break;
				}
			}
			if (copyFound) { 
				final String nameAddition = "[" + prime + "]";
				nameTemp = makeLength(nameTemp, nameLength - nameAddition.length());
				warn |= name.length()>nameTemp.length();
				nameTemp += nameAddition;
				prime++;				
			} else {
				isUnique=true;
			}
		}
		
		if (warn){
		    System.err.println("\""+name+"\" truncated to produce new name \""+
		            nameTemp+"\".");
		}
		return nameTemp;
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
	 * Remove extension from file name
	 * @param fileNameIn file name in 
	 * @return fileName without extension
	 */
	public String removeExtensionFileName(String fileNameIn) {
		String fileName;
		int index;
		index =fileNameIn.lastIndexOf(".");
		//Extension 3 or less characters, index -1 if not found
		if(index>=fileNameIn.length()-4) {
			fileName =fileNameIn.substring(0, index);
		} else {
			fileName =fileNameIn;
		}
		return fileName;
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
