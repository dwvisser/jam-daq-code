package jam.global;

/**
 * Enumeration of the possible sorting modes for Jam.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version May 9, 2004
 * @since 1.5.1
 * @see jam.global.JamStatus#setSortMode(SortMode,String)
 */
public final class SortMode implements QuerySortMode {

	static private final int I_FILE = 6; // we have read in a file

	static private final int I_NOSORT = 0;

	static private final int I_OFFLINE = 3;

	static private final int I_ON_DISK = 1;

	static private final int I_ON_NODISK = 2;

	static private final int I_REMOTE = 5;

	/**
	 * Looking at data that was read in from a file.
	 */
	static public final QuerySortMode FILE = new SortMode(I_FILE);
	
	/**
	 * Not sorting, and no file loaded.
	 */
	static public final QuerySortMode NO_SORT = new SortMode(I_NOSORT);

	/**
	 * Sorting data from disk, that is, sorting offline.
	 */
	static public final QuerySortMode OFFLINE = new SortMode(I_OFFLINE);

	/**
	 * Sort online data without storing events.
	 */
	static public final QuerySortMode ON_NO_DISK = new SortMode(I_ON_NODISK);

	/**
	 * Sorting online data and storing events to disk.
	 */
	static public final QuerySortMode ONLINE_DISK = new SortMode(I_ON_DISK);

	/**
	 * Acting as a client to a remote Jam process.
	 */
	static public final QuerySortMode REMOTE = new SortMode(I_REMOTE);

	private final transient int mode;

	private SortMode(int iMode) {
		super();
		mode = iMode;
	}

	/* (non-Javadoc)
	 * @see jam.global.QuerySortMode#isOffline()
	 */
	public boolean isOffline() {
		return mode == I_OFFLINE;
	}

	/* (non-Javadoc)
	 * @see jam.global.QuerySortMode#isOnline()
	 */
	public boolean isOnline() {
		return mode == I_ON_DISK || mode == I_ON_NODISK;
	}
}
