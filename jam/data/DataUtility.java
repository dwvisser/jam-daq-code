package jam.data;

/**
 * Utility methods for manipulating jam.data objects.
 * 
 * @author Dale Visser
 * 
 */
public final class DataUtility {

	private static final NameValueCollection<Group> GROUPS = Warehouse
			.getGroupCollection();

	private DataUtility() {
		// make no instances
	}

	/**
	 * @param histogram
	 *            histogram to delete
	 */
	public static void delete(final AbstractHistogram histogram) {
		histogram.delete();
		GROUPS.get(histogram.getGroupName()).histograms
				.removeHistogram(histogram);
	}

	/**
	 * @param histogram
	 *            to get the group for
	 * @return the group containing the given histogram
	 */
	public static Group getGroup(final AbstractHistogram histogram) {
		return GROUPS.get(histogram.getGroupName());
	}

}
