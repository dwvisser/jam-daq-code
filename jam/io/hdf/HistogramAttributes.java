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
	public static HistogramAttributes getHistogramAttribute(String fullName) {
		return FULL_NAMES.get(fullName);
	}

	private String groupName;

	private String name;

	private String fullName;

	private String title;

	private int number;

	private int sizeX;

	private int sizeY;

	private int histDim;

	HistogramAttributes(String groupName, String name, String title, int number) {
		super();
		this.groupName = groupName;
		this.name = name;
		this.title = title;
		this.number = number;
		fullName = createFullName(groupName, name);
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
	 * @param groupNameIn
	 *            name of group the hist is in
	 * @param nameIn
	 *            the basic name of the histogram
	 */
	private String createFullName(String groupNameIn, String nameIn) {
		final StringBuffer rval = new StringBuffer();
		if (groupName != null) {
			if (!groupName.equals("")) {
				rval.append(groupNameIn).append('/');
			}
		}
		rval.append(nameIn);
		return rval.toString();
	}

	String getTitle() {
		return title;
	}

	int getHistDim() {
		return histDim;
	}

	int getNumber() {
		return number;
	}

	int getSizeX() {
		return sizeX;
	}

	int getSizeY() {
		return sizeY;
	}

}