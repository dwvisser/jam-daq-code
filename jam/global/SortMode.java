/*
 * Created on May 9, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package jam.global;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version May 9, 2004
 */
public class SortMode {
	/**
	 * Sort Mode--No sort file loaded.
	 */
	static private final int noSort = 0;

	/**
	 * Sort Mode--Set to sort online data to disk.
	 */
	static private final int onlineDisk = 1;

	/**
	 * Sort Mode--Set to sort online data to tape.
	 */
	static private final int onlineNoDisk = 2;

	/**
	 * Sort Mode--Set to sort offline data from disk.
	 */
	static private final int offline = 3;

	/**
	 * Sort Mode--Acting as a client to a remote Jam process.
	 */
	static private final int remote = 5;

	/**
	 * Sort Mode--Just read in a data file.
	 */
	static private final int file = 6; //we have read in a file
	
	private final int mode;
	
	private SortMode(int i){
		mode=i;
	}

	static public final SortMode NO_SORT=new SortMode(noSort);
	static public final SortMode ONLINE_DISK=new SortMode(onlineDisk);
	static public final SortMode ONLINE_NO_DISK=new SortMode(onlineNoDisk);
	static public final SortMode OFFLINE=new SortMode(offline);
	static public final SortMode REMOTE=new SortMode(remote);
	static public final SortMode FILE=new SortMode(file);

}
