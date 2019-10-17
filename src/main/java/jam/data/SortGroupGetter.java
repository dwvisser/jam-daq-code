package jam.data;

/**
 * Retrieves the current sort group.
 * 
 * @author Dale Visser
 * 
 */
public interface SortGroupGetter {

	/**
	 * Returns the group that is the sort group
	 * 
	 * @return the sort group
	 */
	Group getSortGroup();
}