package jam.global;

/**
 * The command names, used internally and also avaliable from
 * the console
 *   
 * @author Ken Swartz
 *
 */
public interface CommandNames {

	/** clear all data */
	public static final String NEWCLEAR ="clear";
	
	/** Open a hdf file */
	public static final String OPEN_HDF = "open";
	
	/** Reload hdf file */
	public static final String RELOAD_HDF = "reload";

	/** Add hdf file */
	public static final String ADD_HDF = "addfile";

	/** Save to hdf file */
	public static final String SAVE_HDF = "save";
	
	/** Save as to a hdf */ 
	public static final String SAVE_AS_HDF = "saveas";	
	
	/** Open selected histograms in hdf */ 
	public static final String OPEN_SELECTED = "opensel";	

}
