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
	String CLEAR = "clear";
	
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
	String SHOW_SORT_CONTROL="showsortcontrol";

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
	
	/** Open file with Yale CAEN scalers */
	String OPEN_SCALERS_YALE_CAEN ="openscalers";

	/** Show the dialog for scaler scan */
	String SHOW_SCALER_SCAN ="showscalerscan";
	
	/** Print the displayed plot */
	String PRINT="print";
	
	/** Setup the printing page layout */
	String PAGE_SETUP="pageSetup";
	
	/** Delete the displayed histogram */
	String DELETE_HISTOGRAM="delhist";

	/** Help About guide */
	String HELP_ABOUT="helpabout";

	/** Help About guide */
	String HELP_LICENSE="helplicense";

	/** User guide */
	String USER_GUIDE="userguide";
	
	/** Start data acqisition */
	String START="start";
	
	/** Stop data acqisition */
	String STOP="stop";
	
	/** Flush daq current buffer */
	String FLUSH="flush";
	
	/** Show the online sorting setup dialog. */
	String SHOW_SETUP_ONLINE="showSetupOnline";

	/** Show the offline sorting setup dialog. */
	String SHOW_SETUP_OFFLINE="showSetupOffline";

	/** Show the offline sorting setup dialog. */
	String SHOW_SETUP_REMOTE="showSetupRemote";

	/** Show the dialog with event and buffer counts.*/
	String SHOW_BUFFER_COUNT="showCounters";
	
	/** Show the peak find setup.*/
	String SHOW_PEAK_FIND="showPeakFind";

	/** Show the dialog to add a fit*/
	String SHOW_FIT_NEW="showfitnew";
	
	/** Show dialog to add view */
	String SHOW_VIEW_NEW="showviewnew";
	
	/** Show dialog to delete view */
	String SHOW_VIEW_DELETE="showviewdelete";
	
	/** Show dialog to change color gradient. */
	String SHOW_GRADIENT_SETTINGS="showgradient";	
}
