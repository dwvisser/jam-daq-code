package jam.data;

/**
 * Utility methods for manipulating jam.data objects.
 * 
 * @author Dale Visser
 * 
 */
public final class DataUtility {
	/**
	 * @param histogram
	 *            histogram to delete
	 */
	public static void delete(Histogram histogram) {
		histogram.delete();
		Group.getGroup(histogram.getGroupName()).removeHistogram(histogram);
	}

	/**
	 * @param histogram
	 *            to get the group for
	 * @return the group containing the given histogram
	 */
	public static Group getGroup(Histogram histogram) {
		return Group.getGroup(histogram.getGroupName());
	}

}
