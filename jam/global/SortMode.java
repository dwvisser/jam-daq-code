package jam.global;

/**
 * Enumeration of the possible sorting modes for Jam.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version May 9, 2004
 * @since 1.5.1
 * @see jam.global.JamStatus#setSortMode(SortMode)
 */
public class SortMode {
	static private final int noSort = 0;
	static private final int onlineDisk = 1;
	static private final int onlineNoDisk = 2;
	static private final int offline = 3;
	static private final int remote = 5;
	static private final int file = 6; //we have read in a file
	
	private final int mode;
	
	private SortMode(int i){
		mode=i;
	}

	/**
	 * Not sorting, and no file loaded.
	 */
	static public final SortMode NO_SORT=new SortMode(noSort);
	
	/**
	 * Sorting online data and storing events to disk.
	 */
	static public final SortMode ONLINE_DISK=new SortMode(onlineDisk);
	
	/**
	 * Sort online data without storing events.
	 */
	static public final SortMode ONLINE_NO_DISK=new SortMode(onlineNoDisk);

	/**
	 * Sorting data from disk, that is, sorting offline.
	 */
	static public final SortMode OFFLINE=new SortMode(offline);
	
	/**
	 * Acting as a client to a remote Jam process.
	 */
	static public final SortMode REMOTE=new SortMode(remote);
	
	/**
	 * Looking at data that was read in from a file.
	 */
	static public final SortMode FILE=new SortMode(file);
}
