package jam.global;

/**
 * The command names, used internally and also avaliable from
 * the console
 *   
 * @author Ken Swartz
 *
 */
public interface CommandNames {

	/** Open a hdf file */
	String OPEN_HDF = "open";
	
	/** Reload hdf file */
	String RELOAD_HDF = "reload";

	/** Add hdf file */
	String ADD_HDF = "addfile";

	/** Save to hdf file */
	String SAVE_HDF = "save";
	
	/** Save as to a hdf */ 
	String SAVE_AS_HDF = "saveas";	

	/** Save gates and scalers to a hdf */ 
	String SAVE_GATES = "savegates";	
	
	/** Open selected histograms in hdf */ 
	String OPEN_SELECTED = "opensel";	

	/** Show dialog for defining a new histogram. */
	String SHOW_NEW_HIST="shownewhist";

	/** Show dialog for defining a new histogram. */
	String SHOW_HIST_ZERO="showhistzero";

	/** Show dialog for combining histogram. */
	String SHOW_HIST_COMBINE="showcombine";

	/** Show dialog for projecting histogram. */
	String SHOW_HIST_PROJECT="showproject";
	
	/** Show dialog for fitting histogram. */
	String SHOW_HIST_FIT="showfit";
	
	/** Show dialog for displaying fit of histogram. */
	String SHOW_HIST_DISPLAY_FIT="showdisplayfit";

	/** Show dialog for gain shift of histogram. */
	String SHOW_HIST_GAIN_SHIFT="showgainshift";
	
	/** Show dialog for defining a new gate. */
	String SHOW_NEW_GATE="gateshownew";
	
	/** Show dialog for defining a new gate. */
	String SHOW_SET_GATE="gateshowset";
	
	/** Show dialog for defining a new gate. */
	String SHOW_ADD_GATE="gateshowadd";

	/** Exit Jam. */
	String EXIT = "exit";
	
	/** Erase the data in memory */
	String NEW = "new";
	
	/** Show the parameters dialog. */
	String PARAMETERS="parameters";
	
	/** Show the scaler values. */
	String DISPLAY_SCALERS="displayscalers";
	
	/** Show the monitors */
	String DISPLAY_MONITORS="displaymonitors";
	
	/** Show the monitor configuration dialog */
	String DISPLAY_MON_CONFIG="displaymonitorconfig";
	
	/** Show the dialog for zeroing scalers. */
	String SHOW_RUN_CONTROL="showruncontrol";

	/** Show the dialog for zeroing scalers. */
	String SHOW_ZERO_SCALERS="showzeroscalers";
	
	/** Show batch export dialog */
	String SHOW_BATCH_EXPORT="showbatchexport";
	
	/** Read or zero scalers. */
	String SCALERS="scalers";
	
	/** Export the displayed histogram as a text file. */
	String EXPORT_TEXT="exporttext";
			
	/** Export the displayed 1-d histogram as a Radware file. */
	String EXPORT_SPE="exportspe";
			
	/** Export the displayed histogram as a .his file for DAMM. */
	String EXPORT_DAMM="exportdamm";	
	
	/** Import a text file. */
	String IMPORT_TEXT="importtext";
			
	/** Import a Radware file. */
	String IMPORT_SPE="importspe";
			
	/** Import a .his file from DAMM. */
	String IMPORT_DAMM="importdamm";	

	/** Import a .ban file from DAMM. */
	String IMPORT_BAN="importban";	
	
	/** Import an XSYS file */
	String IMPORT_XSYS="importxsys";

	/** Print the displayed plot */
	String PRINT="print";
	
	/** Setup the printing page layout */
	String PAGE_SETUP="pageSetup";
	
	/** Delete the displayed histogram */
	String DELETE_HISTOGRAM="delhist";

	/** User guide */
	String USER_GUIDE="userguide";
	
}
