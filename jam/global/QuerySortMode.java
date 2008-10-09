package jam.global;

/**
 * Interface to determine if and what type of sort mode the application is in.
 * 
 * @author Dale Visser
 * 
 */
public interface QuerySortMode {

	/**
	 * Returns whether this mode represents offline sorting.
	 * 
	 * @return whether this mode represents offline sorting
	 */
	boolean isOffline();

	/**
	 * Returns whether this mode represents online data acquisition.
	 * 
	 * @return whether this mode represents online data acquisition
	 */
	boolean isOnline();
}