package jam.data;

import jam.data.Group.Type;
import jam.util.StringUtilities;

import java.util.Set;
import java.util.TreeSet;

/**
 * Create objects in jam.data.
 * 
 * @author Dale Visser
 * 
 */
public final class Factory {

	private Factory() {
		// static class
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
		return Factory.createGroup(groupName, null, type);
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
			final String fileName, final Group.Type type) {
		return new Group(groupName, type, fileName);
	}

	/**
	 * Creates a new histogram, using the given array as the template.
	 * 
	 * @param group
	 *            group to create histogram in
	 * @param array
	 *            1d or 2d int or double array
	 * @param name
	 *            unique identifier
	 * @param title
	 *            verbose description
	 * @param labelX
	 *            x-axis label
	 * @param labelY
	 *            y-axis label
	 * @return a newly created histogram
	 */
	public static AbstractHistogram createHistogram(final Group group,
			final Object array, final String name, final String title,
			final String labelX, final String labelY) {
		final AbstractHistogram rval;
		final jam.data.HistogramType hType = jam.data.HistogramType
				.getArrayType(array);
		if (hType == jam.data.HistogramType.ONE_DIM_INT) {
			rval = group.createHistInt1D(name, title, labelX, labelY,
					(int[]) array);
		} else if (hType == jam.data.HistogramType.ONE_D_DOUBLE) {
			rval = group.createHistDouble1D(name, title, labelX, labelY,
					(double[]) array);
		} else if (hType == jam.data.HistogramType.TWO_DIM_INT) {
			rval = group.createHistInt2D(name, title, labelX, labelY,
					(int[][]) array);
		} else {// TWO_D_DOUBLE
			rval = group.createHistDouble2D(name, title, labelX, labelY,
					(double[][]) array);
		}
		return rval;
	}

	/**
	 * Create a scaler object and add it to this group.
	 * 
	 * @param group
	 *            the group to add to
	 * @param nameIn
	 *            desired name
	 * @param idNum
	 *            desired id number
	 * @return the scaler object
	 */
	public static Scaler createScaler(final Group group, final String nameIn,
			final int idNum) {
		// Set of names of gates for histogram this gate belongs to
		final Set<String> scalerNames = new TreeSet<String>();
		for (Scaler scaler : group.getScalerList()) {
			scalerNames.add(scaler.getName());
		}

		final StringUtilities stringUtil = StringUtilities.getInstance();
		final String name = stringUtil.makeUniqueName(nameIn, scalerNames,
				Scaler.NAME_LENGTH);
		final String uniqueName = stringUtil
				.makeFullName(group.getName(), name);
		final Scaler result = new Scaler(name, uniqueName, idNum);
		group.add(result);
		/* Add to list of scalers */
		return result;
	}

	/**
	 * Creates a new histogram, using the given array as the template.
	 * 
	 * @param group
	 *            group to create histogram in
	 * @param array
	 *            1d or 2d int or double array
	 * @param name
	 *            unique identifier
	 * @return a newly created histogram
	 */
	public static AbstractHistogram createHistogram(final Group group,
			final Object array, final String name) {
		return createHistogram(group, array, name, name, null, null);
	}

	/**
	 * Creates a new histogram, using the given array as the template.
	 * 
	 * @param group
	 *            group to create histogram in
	 * @param array
	 *            1d or 2d int or double array
	 * @param name
	 *            unique identifier
	 * @param title
	 *            verbose description
	 * @return a newly created histogram
	 */
	public static AbstractHistogram createHistogram(final Group group,
			final Object array, final String name, final String title) {
		return createHistogram(group, array, name, title, null, null);
	}

	protected static NameValueCollection<AbstractHistogram> createHistogramCollection() {
		return new HistogramCollection();
	}
}
