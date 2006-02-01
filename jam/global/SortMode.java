package jam.global;

/**
 * Enumeration of the possible sorting modes for Jam.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version May 9, 2004
 * @since 1.5.1
 * @see jam.global.JamStatus#setSortMode(SortMode,String)
 */
public final class SortMode {// NOPMD

	static private final int I_FILE = 6; // we have read in a file

	static private final int I_NOSORT = 0;

	static private final int I_OFFLINE = 3;

	static private final int I_ON_DISK = 1;

	static private final int I_ON_NODISK = 2;

	static private final int I_REMOTE = 5;

	/**
	 * Looking at data that was read in from a file.
	 */
	static public final SortMode FILE = new SortMode(I_FILE);
	
	/**
	 * Not sorting, and no file loaded.
	 */
	static public final SortMode NO_SORT = new SortMode(I_NOSORT);

	/**
	 * Sorting data from disk, that is, sorting offline.
	 */
	static public final SortMode OFFLINE = new SortMode(I_OFFLINE);

	/**
	 * Sort online data without storing events.
	 */
	static public final SortMode ON_NO_DISK = new SortMode(I_ON_NODISK);

	/**
	 * Sorting online data and storing events to disk.
	 */
	static public final SortMode ONLINE_DISK = new SortMode(I_ON_DISK);

	/**
	 * Acting as a client to a remote Jam process.
	 */
	static public final SortMode REMOTE = new SortMode(I_REMOTE);

	private final transient int mode;

	private SortMode(int iMode) {
		super();
		mode = iMode;
	}

	/**
	 * Returns whether this mode represents offline sorting.
	 * 
	 * @return whether this mode represents offline sorting
	 */
	public boolean isOffline() {
		return mode == I_OFFLINE;
	}

	/**
	 * Returns whether this mode represents online data acquisition.
	 * 
	 * @return whether this mode represents online data acquisition
	 */
	public boolean isOnline() {
		return mode == I_ON_DISK || mode == I_ON_NODISK;
	}
}
