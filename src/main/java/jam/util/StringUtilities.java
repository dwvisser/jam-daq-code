package jam.util;

import com.google.inject.Singleton;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Contains utilities for manipulating <code>String</code> objects.
 * 
 * @author Dale Visser
 * @version 0.5
 * @see java.lang.String
 */
@Singleton
public final class StringUtilities {

	private static final Charset ASCII = Charset.availableCharsets().get(
			"US-ASCII");

	private static final char ELEMENT_SEPARATOR = '/';

	private static final Logger LOGGER = Logger.getLogger(StringUtilities.class
			.getPackage().getName());

	/**
	 * 
	 * @param input
	 *            a string object
	 * @return array of ASCII bytes representing the input string
	 */
	public byte[] getASCIIarray(final String input) {
		return ASCII.encode(input).array();
	}

	/**
	 * Creates a <code>String</code> from the given US-ASCII byte array.
	 * 
	 * @param input
	 *            US-ASCII characters as bytes
	 * @return representation of the given array
	 */
	public String getASCIIstring(final byte[] input) {
		final ByteBuffer buffer = ByteBuffer.wrap(input);
		return ASCII.decode(buffer).toString();
	}

	/**
	 * Make the full path to a data element
	 * 
	 * @param parentName
	 *            name of parent element
	 * @param name
	 *            name of element
	 * @return fullName
	 */
	public String makeFullName(final String parentName, final String name) {
		return parentName + ELEMENT_SEPARATOR + name;
	}

	/**
	 * Truncates a <code>String</code> or pads the end with spaces to make it a
	 * certain length.
	 * 
	 * @param input
	 *            <code>String</code> to modify
	 * @param length
	 *            desired number of characters in the <code>String</code>
	 * @return <code>String</code> with <code>length</code> characters
	 */
	public String makeLength(final String input, final int length) {
		final StringBuilder temp = new StringBuilder(input);
		for (int i = input.length(); i < length; i++) {
			temp.append(' ');
		}
		return temp.substring(0, length);
	}

	/**
	 * Make a unique name out of the given name that differs from names in the
	 * given set.
	 * 
	 * @param name
	 *            name to make unique
	 * @param nameSet
	 *            contains the existing names
	 * @return unique name
	 */
	public String makeUniqueName(final String name, final Set<String> nameSet) {
		String nameTemp = name.trim();
		boolean isUnique = false;
		int prime = 1;
		boolean copyFound;
		/* find a name that does not conflict with existing names */
		while (!isUnique) {
			copyFound = false;
			for (String nameNext : nameSet) {
				if (nameTemp.compareTo(nameNext) == 0) {
					copyFound = true;
					break;
				}
			}
			if (copyFound) {
				final String nameAddition = "[" + prime + "]";
				nameTemp = name + nameAddition;
				prime++;
			} else {
				isUnique = true;
			}
		}
		return nameTemp;
	}

	/**
	 * Make a unique name out of the given name that differs from names in the
	 * given set.
	 * 
	 * @param name
	 *            name to make unique
	 * @param nameSet
	 *            contains the existing names
	 * @param nameLength
	 *            target length of name
	 * @return unique name
	 */
	public String makeUniqueName(final String name, final Set<String> nameSet,
			final int nameLength) {
		final StringBuilder rval = new StringBuilder(makeLength(name, nameLength));
		boolean warn = name.length() > rval.length();
		boolean isUnique = false;
		int prime = 1;
		boolean copyFound;
		/* find a name that does not conflict with existing names */
		while (!isUnique) {
			copyFound = false;
			for (String nameNext : nameSet) {
				if (rval.toString().compareTo(nameNext) == 0) {
					copyFound = true;
					break;
				}
			}
			if (copyFound) {
				final String nameAddition = "[" + prime + "]";
				final String temp = makeLength(rval.toString(), nameLength
						- nameAddition.length());
				rval.setLength(0);
				rval.append(temp);
				warn |= name.length() > rval.length();
				rval.append(nameAddition);
				prime++;
			} else {
				isUnique = true;
			}
		}

		if (warn) {
			LOGGER.warning("\"" + name + "\" truncated to produce new name \""
					+ rval + "\".");
		}
		return rval.toString();
	}
}
