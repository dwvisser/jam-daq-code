package jam.io;

/**
 * Enumeration of the possible modes available when opening files.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version May 3, 2004
 */
public final class FileOpenMode {
	
	private final static int OPEN_VALUE = 0;
	private final static int OPEN_ADDITIONAL_VALUE=1;	
	private final static int RELOAD_VALUE = 2;
	private final static int ADD_VALUE = 3;
	
	private final static String [] NAMES={"Open", "Open Additional",
	        "Reload", "Add"
	};

	private final int value;

	private FileOpenMode(int mode){
		value=mode;
	}
	
	public boolean equals(Object o){
	    return o instanceof FileOpenMode ? value==((FileOpenMode)o).value : false;
	}
	
	public String toString(){
	    return NAMES[value];
	}
	
	/**
	 * Returns whether this is one of the "Open" modes, which means
	 * that new data objects will get created by Jam from the data
	 * in the file.
	 * @return whether this is one of the "Open" modes
	 */
	public boolean isOpenMode(){
	    return value==OPEN_VALUE||value==OPEN_ADDITIONAL_VALUE;
	}
	
	/**
	 * Mode for deleting the data in memory and replacing them with
	 * the contents of the file.
	 */
	public static final FileOpenMode OPEN=new FileOpenMode(OPEN_VALUE);
	/**
	 * Mode for opening an additional file. 
	 */
	public static final FileOpenMode OPEN_ADDITIONAL=new FileOpenMode(OPEN_ADDITIONAL_VALUE);
	
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
