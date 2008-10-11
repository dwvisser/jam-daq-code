package jam.data;

/**
 * Utility methods for manipulating jam.data objects.
 * 
 * @author Dale Visser
 * 
 */
public final class DataUtility {

	private DataUtility() {
		// make no instances
	}

	/**
	 * @param histogram
	 *            histogram to delete
	 */
	public static void delete(final Histogram histogram) {
		histogram.delete();
		Group.getGroup(histogram.getGroupName()).removeHistogram(histogram);
	}

	/**
	 * @param histogram
	 *            to get the group for
	 * @return the group containing the given histogram
	 */
	public static Group getGroup(final Histogram histogram) {
		return Group.getGroup(histogram.getGroupName());
	}

}
