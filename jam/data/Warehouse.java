package jam.data;

/**
 * Returns unique jam.data objects (like factory, but serves up singletons).
 * 
 * @author Dale Visser
 * 
 */
public final class Warehouse {
	private Warehouse() {
		// static class
	}

	/**
	 * @return the singleton group collection
	 */
	@SuppressWarnings("unchecked")
	public static NameValueCollection<Group> getGroupCollection() {
		return (NameValueCollection<Group>) GroupCollection.getInstance();
	}

	/**
	 * @return the singleton sort group reference holder
	 */
	public static SortGroupGetter getSortGroupGetter() {
		return GroupCollection.getInstance();
	}
}
