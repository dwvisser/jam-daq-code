package jam.io;

/**
 * Enumeration of the possible modes available when opening files.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version May 3, 2004
 */
public class FileOpenMode {
	private final static int OPEN_VALUE = 1;
	private final static int RELOAD_VALUE = 2;
	private final static int ADD_VALUE = 3;
	
	private final int value;

	private FileOpenMode(int mode){
		value=mode;
	}
	
	public boolean equals(Object o){
	    return o instanceof FileOpenMode ? value==((FileOpenMode)o).value : false;
	}
	
	/**
	 * Mode for deleting the data in memory and replacing them with
	 * the contents of the file.
	 */
	public static final FileOpenMode OPEN=new FileOpenMode(OPEN_VALUE);
	
	/**
	 * Mode for replacing the contents of data in memory with any 
	 * objects in the file representing the same thing.
	 */
	public static final FileOpenMode RELOAD=new FileOpenMode(RELOAD_VALUE);
	
	/**
	 * Mode for adding counts in histograms and scalers in the file to the counts
	 * of the same objects in memory.
	 */
	public static final FileOpenMode ADD=new FileOpenMode(ADD_VALUE);
}
