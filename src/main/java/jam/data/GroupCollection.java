package jam.data;

import jam.data.Group.Type;

import java.util.*;

/**
 * Collection of groups.
 * 
 * @author Dale Visser
 * 
 */
final class GroupCollection implements NameValueCollection<Group>,
		SortGroupGetter {

	private GroupCollection() {
		// singleton
	}

	/**
	 * Clear a group, removes it
	 * 
	 * @param group to remove
	 */
	public void remove(final Group group) {
		this.map.remove(group.getName());
		this.list.remove(group);
	}

	/** Map of all groups using name */
	private transient final Map<String, Group> map = new HashMap<>();
	/** List of all groups */
	private transient final List<Group> list = new ArrayList<>();

	private transient final Object lock = new Object();
	/** The sort group, group with sort histogram */
	private transient Group sortGroup;

	public void remap(final Group group, final String oldName,
			final String newName) {
		if (group != get(oldName)) {
			throw new IllegalArgumentException(
					"Given group is not currently mapped to '" + oldName + "'.");
		}

		if (this.containsName(newName)) {
			throw new IllegalArgumentException(
					"You may not rename to an existing name.");
		}

		this.map.remove(oldName);
		this.map.put(newName, group);
	}

	/**
	 * Clear all groups
	 */
	public void clear() {
		synchronized (lock) {
			this.map.clear();
			this.list.clear();
			this.sortGroup = null; // NOPMD
		}
	}

	/**
	 * Returns the group with the given name.
	 * 
	 * @param name
	 *            of group
	 * @return the group
	 */
	public Group get(final String name) {
		return this.map.get(name);
	}

	/**
	 * @param name
	 *            of group
	 * @return whether collection contains a group with the given name
	 */
	public boolean containsName(final String name) {
		return this.map.containsKey(name);
	}

	public Set<String> getNameSet() {
		return this.map.keySet();
	}

	public void add(final Group group, final String uniqueName) {
		if (Type.SORT == group.getType()) {
			if (this.sortGroup != null) {
				throw new IllegalStateException(
						"There may not be more than 1 sort group.");
			}
			this.sortGroup = group;
		}
		this.list.add(group);
		this.map.put(uniqueName, group);
	}

	/**
	 * Gets a list of all groups.
	 * 
	 * @return list of all groups
	 */
	public List<Group> getList() {
		return Collections.unmodifiableList(this.list);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.data.SortGroupGetter#getSortGroup()
	 */
	public Group getSortGroup() {
		return this.sortGroup;
	}

	private static final GroupCollection INSTANCE = new GroupCollection();

	/**
	 * @return the singleton instance
	 */
	protected static SortGroupGetter getInstance() {
		return INSTANCE;
	}
}
