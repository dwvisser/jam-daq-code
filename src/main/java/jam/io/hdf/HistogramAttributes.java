package jam.io.hdf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to hold histogram properties while we decide if we should load them.
 * 
 * @version 2005.03.05
 * @author Ken Swartz
 */
public final class HistogramAttributes {

	private static final Map<String, HistogramAttributes> FULL_NAMES = Collections
			.synchronizedMap(new HashMap<String, HistogramAttributes>());

	/**
	 * Clears all histogram attribute objects from the internal static map.
	 * 
	 */
	public static void clear() {
		FULL_NAMES.clear();
	}

	/**
	 * Retrieves a histogram's attributes.
	 * 
	 * @param fullName
	 *            full name for histogram, including group
	 * @return attributes for the given name
	 */
	public static HistogramAttributes getHistogramAttribute(
			final String fullName) {
		return FULL_NAMES.get(fullName);
	}

	private transient String groupName;

	private transient String name;

	private transient String fullName;

	private transient String title;

	private transient int number;

	HistogramAttributes(final String groupName, final String name,
			final String title, final int number) {
		super();
		this.groupName = groupName;
		this.name = name;
		this.title = title;
		this.number = number;
		fullName = createFullName(name);
		FULL_NAMES.put(fullName, this);
	}

	HistogramAttributes() {
		super();
	}

	/**
	 * @return the group name of the histogram
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * @return the name of the histogram
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the full name of the group/histogram
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * Full name is <groupName>/<histName.
	 * 
	 * @return the full name of the histogram
	 * @param nameIn
	 *            the basic name of the histogram
	 */
	private String createFullName(final String nameIn) {
		final StringBuilder rval = new StringBuilder();
		if (groupName != null) {
			rval.append(groupName).append('/');
		}
		rval.append(nameIn);
		return rval.toString();
	}

	protected String getTitle() {
		return title;
	}

	protected int getNumber() {
		return number;
	}
}