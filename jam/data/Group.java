package jam.data;

import jam.global.Nameable;
import jam.util.StringUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A group of histograms, A node in the tree
 * 
 */
public final class Group implements Nameable {

	/**
	 * Enumeration of types of groups.
	 */
	public static enum Type {
		/**
		 * Group comes from a file.
		 */
		FILE,
		/**
		 * Group comes from a sort routine.
		 */
		SORT,
		/**
		 * Temporary group until save.
		 */
		TEMP
	}

	/** Default group name */
	public final static String DEFAULT_NAME = "Default";

	/** List of all groups */
	private final static List<Group> LIST = new ArrayList<Group>();

	/** Map of all groups using name */
	private final static Map<String, Group> NAME_MAP = new HashMap<String, Group>();

	/** The sort group, group with sort histogram */
	private static Group sortGroup;

	/** Working group name */
	public final static String WORKING_NAME = "Working";

	/**
	 * Clear a group, removes it
	 * 
	 * @param group
	 */
	public static void clearGroup(final Group group) {
		NAME_MAP.remove(group);
		LIST.remove(group);
	}

	/**
	 * Clear all groups
	 */
	public static void clearList() {
		synchronized (Group.class) {
			NAME_MAP.clear();
			LIST.clear();
			sortGroup = null;
		}
	}

	/**
	 * Set a group as the current group, create the group if it does not already
	 * exist
	 * 
	 * @param groupName
	 *            name of the group
	 * @param type
	 *            of group
	 * @param fileName
	 *            name of the file that this group belongs to
	 * @return the created <code>Group</code> object
	 */
	public static Group createGroup(final String groupName,
			final String fileName, final Type type) {
		synchronized (Group.class) {
			final Group group = new Group(groupName, type, fileName);
			/* Only one sort group */
			if (type == Type.SORT) {
				sortGroup = group;
			}
			return group;
		}
	}

	/**
	 * Set a group as the current group, create the group if it does not already
	 * exist
	 * 
	 * @param groupName
	 *            name of the group
	 * @param type
	 *            of group
	 * @return the created <code>Group</code> object
	 */
	public static Group createGroup(final String groupName, final Type type) {
		return Group.createGroup(groupName, null, type);
	}

	/**
	 * Returns the group with the given name.
	 * 
	 * @param name
	 *            of group
	 * @return the group
	 */
	public static Group getGroup(final String name) {
		return NAME_MAP.get(name);
	}

	/**
	 * Gets a list of all groups.
	 * 
	 * @return list of all groups
	 */
	public static List<Group> getGroupList() {
		return Collections.unmodifiableList(LIST);
	}

	/**
	 * Gets a map of groups keyed by name.
	 * 
	 * @return map of groups keyed by name
	 */
	public static Map getGroupMap() {
		return Collections.unmodifiableMap(NAME_MAP);
	}

	/**
	 * Returns the group that is the sort group
	 * 
	 * @return the sort group
	 */
	public static Group getSortGroup() {
		synchronized (Group.class) {
			return sortGroup;
		}
	}

	/**
	 * Test if Group is a valid Group
	 * 
	 * @param group
	 *            the group to test
	 * @return <code>true</code> if this group remains in the name mapping
	 */
	public static boolean isValid(final Group group) {
		boolean valid = false;
		if (group != null) {
			valid=NAME_MAP.containsKey(group.getGroupName());
		} 
		return valid;
	}

	/**
	 * Get just the class name from the full name
	 * 
	 * @param name
	 *            the full sort class name
	 * @return the classname, minus any packages
	 */
	public static String parseSortClassName(final String name) {
		final int index = name.lastIndexOf('.');
		return name.substring(index + 1, name.length());
	}

	/** Name of file that group belongs to. */
	private transient final String fileName;

	/** Name of file and group concatenated. */
	private transient String fullName;

	/** Origonal group name */
	private transient final String groupName;

	/** children histograms of group */
	private transient final List<Histogram> histList = new ArrayList<Histogram>();

	/** children of group */
	private transient final Map<String, Histogram> histogramMap = new HashMap<String, Histogram>();

	/** children scalers of group */
	private transient final List<Scaler> scalerList = new ArrayList<Scaler>();

	/** Type of group, file or sort */
	private transient final Type type;

	/**
	 * Constructor
	 * 
	 * @param groupName
	 *            name of the group
	 * @param type
	 *            the type of group
	 * @param fileName
	 *            name of file this group is associated with
	 */
	private Group(String groupName, Type type, String fileName) {
		super();
		final StringUtilities stringUtil = StringUtilities.getInstance();
		String tempFullName = "GROUP";
		final boolean noFile = fileName != null;
		final boolean noGroup = groupName != null;
		if (noFile && noGroup) {
			tempFullName = stringUtil.makeFullName(fileName, groupName);
		} else if (noFile) {
			tempFullName = fileName;
		} else if (noGroup) {
			tempFullName = groupName;
		}
		final String uniqueName = stringUtil.makeUniqueName(tempFullName, NAME_MAP
				.keySet());
		this.type = type;
		this.groupName = groupName;
		this.fileName = fileName;
		this.fullName = uniqueName;
		LIST.add(this);
		NAME_MAP.put(uniqueName, this);
	}

	/**
	 * Add a histogram to the group
	 * 
	 * @param hist
	 */
	public void addHistogram(final Histogram hist) {
		histList.add(hist);
		histogramMap.put(hist.getName(), hist);
	}

	/**
	 * Add a scaler to the stored list of scalers.
	 * 
	 * @param scaler
	 *            to add
	 */
	public void addScaler(final Scaler scaler) {
		scalerList.add(scaler);
	}

	/**
	 * @return the file name part of this group
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return the group name part of the full name
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * Retreive a histogram given its name
	 * 
	 * @param name
	 *            the histogram name
	 * @return the histogram
	 */
	public Histogram getHistogram(final String name) {
		return histogramMap.get(name);
	}

	/**
	 * @return list of histograms in this group
	 */
	public List<Histogram> getHistogramList() {
		return Collections.unmodifiableList(histList);
	}

	/**
	 * @return map of histograms in this group keyed by name
	 */
	public Map<String, Histogram> getHistogramMap() {
		return Collections.unmodifiableMap(histogramMap);
	}

	/**
	 * @return the name of this group
	 */
	public String getName() {
		return fullName;
	}

	/**
	 * Returns a view of the list of scalers.
	 * 
	 * @return a view of the list of scalers
	 */
	public List<Scaler> getScalerList() {
		return Collections.unmodifiableList(scalerList);
	}

	/**
	 * @return the type of this group
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Returns whether this group has histograms in it.
	 * 
	 * @return whether this group has histograms in it
	 */
	public boolean hasHistograms() {
		return !histList.isEmpty();
	}

	/**
	 * Remove a histogram from the group
	 * 
	 * @param hist
	 */
	public void removeHistogram(final Histogram hist) {
		histList.remove(hist);
		histogramMap.remove(hist.getName());
	}

	/**
	 * Set the name, used to rename group.
	 * 
	 * @param name
	 *            the new name of a group
	 * @throws DataException
	 *             if an attempt is made to rename to an existing name
	 */
	public void setName(final String name) throws DataException {
		if (NAME_MAP.containsKey(name)) {
			throw new DataException("You may not rename to an existing name.");
		}
		NAME_MAP.remove(this.getName());
		NAME_MAP.put(name, this);
		fullName = name;
		for (Histogram hist : getHistogramList()) {
			hist.updateNames(this);
		}
	}

	public String toString() {
		return fullName;
	}
}