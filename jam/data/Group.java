package jam.data;

import jam.global.Nameable;
import jam.util.StringUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

	/** Working group name */
	public final static String WORKING_NAME = "Working";

	/**
	 * Collection of all group objects.
	 */
	private static final NameValueCollection<Group> COLLECTION = Warehouse
			.getGroupCollection();

	/**
	 * The histograms in this group.
	 */
	public transient final HistogramCollection histograms = new HistogramCollection();

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

	/** Name of file and group concatenated. */
	private transient String fullName;

	/** Original group name */
	private transient final String groupName;

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
	protected Group(final String groupName, final Type type,
			final String fileName) {
		super();
		String tempFullName = "GROUP";
		final boolean filenameNotNull = fileName != null;
		final boolean groupNameNotNull = groupName != null;
		if (filenameNotNull && groupNameNotNull) {
			tempFullName = stringUtil.makeFullName(fileName, groupName);
		} else if (filenameNotNull) {
			tempFullName = fileName;
		} else if (groupNameNotNull) {
			tempFullName = groupName;
		}
		final String uniqueName = stringUtil.makeUniqueName(tempFullName,
				COLLECTION.getNameSet());
		this.type = type;
		this.groupName = groupName;
		this.fullName = uniqueName;
		COLLECTION.add(this, uniqueName);
	}

	protected void add(final Scaler scaler) {
		this.scalerList.add(scaler);
	}

	/**
	 * @return the group name part of the full name
	 */
	public String getGroupName() {
		return groupName;
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
	 * Set the name, used to rename group.
	 * 
	 * @param name
	 *            the new name of a group
	 * @throws DataException
	 *             if an attempt is made to rename to an existing name
	 */
	public void setName(final String name) throws DataException {
		COLLECTION.remap(this, fullName, name);
		fullName = name;
		for (AbstractHistogram hist : histograms.getList()) {
			hist.updateNames(this);
		}
	}

	@Override
	public String toString() {
		return fullName;
	}

	private static final StringUtilities stringUtil = StringUtilities
			.getInstance();

	protected HistDouble2D createHistDouble2D(final String name,
			final String title, final String labelX, final String labelY,
			final double[][] array) {
		final HistDouble2D result = new HistDouble2D(title, labelX, labelY,
				array);
		addGroupInfoToHist(result, name);
		return result;
	}

	protected HistInt2D createHistInt2D(final String name, final String title,
			final String labelX, final String labelY, final int[][] array) {
		final HistInt2D result = new HistInt2D(title, labelX, labelY, array);
		addGroupInfoToHist(result, name);
		return result;
	}

	protected HistDouble1D createHistDouble1D(final String name,
			final String title, final String labelX, final String labelY,
			final double[] array) {
		final HistDouble1D result = new HistDouble1D(title, labelX, labelY,
				array);
		addGroupInfoToHist(result, name);
		return result;
	}

	protected HistInt1D createHistInt1D(final String name, final String title,
			final String labelX, final String labelY, final int[] array) {
		final HistInt1D result = new HistInt1D(title, labelX, labelY, array);
		addGroupInfoToHist(result, name);
		return result;
	}

	private void addGroupInfoToHist(final AbstractHistogram hist,
			final String name) {
		hist.setName(stringUtil.makeUniqueName(name, histograms.getNameSet(),
				AbstractHistogram.NAME_LENGTH));
		hist.updateNames(this);// puts in name map as well
		/* Add to group */
		this.histograms.add(hist);
		/* Make a unique name in the group */
	}

	/**
	 * Test if Group is a valid Group
	 * 
	 * @return <code>true</code> if this group remains in the name mapping
	 */
	protected boolean isValid() {
		return COLLECTION.getList().contains(this);
	}
}