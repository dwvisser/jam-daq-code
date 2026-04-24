package jam.global;

/**
 * Enumeration of the possible sorting modes for Jam.
 *
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version May 9, 2004
 * @since 1.5.1
 * @see jam.global.JamStatus#setSortMode(QuerySortMode, String)
 */
public final class SortMode implements QuerySortMode {

  private static final int I_FILE = 6; // we have read in a file

  private static final int I_NOSORT = 0;

  private static final int I_OFFLINE = 3;

  private static final int I_ON_DISK = 1;

  private static final int I_ON_NODISK = 2;

  private static final int I_REMOTE = 5;

  /** Looking at data that was read in from a file. */
  public static final QuerySortMode FILE = new SortMode(I_FILE);

  /** Not sorting, and no file loaded. */
  public static final QuerySortMode NO_SORT = new SortMode(I_NOSORT);

  /** Sorting data from disk, that is, sorting offline. */
  public static final QuerySortMode OFFLINE = new SortMode(I_OFFLINE);

  /** Sort online data without storing events. */
  public static final QuerySortMode ON_NO_DISK = new SortMode(I_ON_NODISK);

  /** Sorting online data and storing events to disk. */
  public static final QuerySortMode ONLINE_DISK = new SortMode(I_ON_DISK);

  /** Acting as a client to a remote Jam process. */
  public static final QuerySortMode REMOTE = new SortMode(I_REMOTE);

  private final transient int mode;

  private SortMode(final int iMode) {
    super();
    mode = iMode;
  }

  /*
   * (non-Javadoc)
   *
   * @see jam.global.QuerySortMode#isOffline()
   */
  public boolean isOffline() {
    return mode == I_OFFLINE;
  }

  /*
   * (non-Javadoc)
   *
   * @see jam.global.QuerySortMode#isOnline()
   */
  public boolean isOnline() {
    return mode == I_ON_DISK || mode == I_ON_NODISK;
  }
}
