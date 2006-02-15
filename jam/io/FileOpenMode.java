package jam.io;

/**
 * Enumeration of the possible modes available when opening files.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version May 3, 2004
 */
public final class FileOpenMode {
	
	private final static int I_OPEN = 0;
	private final static int I_OPEN_MORE=1;	
	private final static int I_RELOAD = 2;
	private final static int I_ADD = 3;
	private final static int I_ADD_OPEN_ONE = 4;
	private final static int I_ATTRIBUTES = 5;
	
	private final static String [] NAMES={"Open", "Open Additional",
	        "Reload", "Add","Add Open One", "Attributes"
	};

	private transient final int value;

	private FileOpenMode(int mode){
		super();
		value=mode;
	}
	
	public boolean equals(final Object object) {
        return object instanceof FileOpenMode ? value == ((FileOpenMode) object).value
                : false;
    }
	
	public int hashCode(){
	    return value;
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
	    return value==I_OPEN||value==I_OPEN_MORE;
	}
	
	/**
	 * Mode for deleting the data in memory and replacing them with
	 * the contents of the file.
	 */
	public static final FileOpenMode OPEN=new FileOpenMode(I_OPEN);
	
	/**
	 * Mode for opening an additional file. 
	 */
	public static final FileOpenMode OPEN_MORE=new FileOpenMode(I_OPEN_MORE);
	
	/**
	 * Mode for replacing the contents of data in memory with any 
	 * objects in the file representing the same thing.
	 */
	public static final FileOpenMode RELOAD=new FileOpenMode(I_RELOAD);
	
	/**
	 * Mode for adding counts in histograms and scalers in the file to the counts
	 * of the same objects in memory.
	 */
	public static final FileOpenMode ADD=new FileOpenMode(I_ADD);
	/**
	 * Mode for adding counts in histograms and scalers in the file 
	 * the first file opened 
	 */
	public static final FileOpenMode ADD_OPEN_ONE=new FileOpenMode(I_ADD_OPEN_ONE);

	

	/**
	 * Mode for reading histogram attributes but not the data
	 */
	public static final FileOpenMode ATTRIBUTES=new FileOpenMode(I_ATTRIBUTES);
	
}
