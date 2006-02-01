package jam.global;

/**
 * The command names, used internally and also avaliable from
 * the console
 *   
 * @author Ken Swartz
 *
 */
public interface CommandNames {

	/** Add hdf file */
	String ADD_HDF = "addfile";
	
	/** Erase the data in memory */
	String CLEAR = "clear";

	/** Show dialog for defining a new group. */
	String DELETE_GROUP="deletegroup";
	
	/** Delete the displayed histogram */
	String DELETE_HISTOGRAM="delhist";

	/** Show the monitor configuration dialog */
	String DISPLAY_MON_CFG="displaymonitorconfig";

	/** Show the monitors */
	String DISPLAY_MONITORS="displaymonitors";
	
	/** Show the scaler values. */
	String DISPLAY_SCALERS="displayscalers";	

	/** Exit Jam. */
	String EXIT = "exit";	

	/** Export the displayed histogram as a .his file for DAMM. */
	String EXPORT_DAMM="exportdamm";	

	/** Export the displayed 1-d histogram as a Radware file. */
	String EXPORT_SPE="exportspe";	

	/** Export the summary table to a file. */
	String EXPORT_TABLE="exporttable";	
	
	/** Export the displayed histogram as a text file. */
	String EXPORT_TEXT="exporttext";	

	/** Flush daq current buffer */
	String FLUSH="flush";

	/** Help About guide */
	String HELP_ABOUT="helpabout";
	
	/** Help About guide */
	String HELP_LICENSE="helplicense";	
	
	/** Import a .ban file from DAMM. */
	String IMPORT_BAN="importban";

	/** Import a .his file from DAMM. */
	String IMPORT_DAMM="importdamm";

	/** Import a Radware file. */
	String IMPORT_SPE="importspe";

	/** Import a text file. */
	String IMPORT_TEXT="importtext";
	
	/** Import an XSYS file */
	String IMPORT_XSYS="importxsys";
	
	/** Open an additional hdf file */
	String OPEN_ADD_HDF = "openadditional";

	/** Open a hdf file */
	String OPEN_HDF = "open";
	
	/** Open multiple hdf file */
	String OPEN_MULTIPLE_HDF = "openmultiple";
	
	/** Open file with Yale CAEN scalers */
	String OPEN_SCALERS ="openscalers";
	
	/** Open selected histograms in hdf */ 
	String OPEN_SELECTED = "opensel";

	/** Setup the printing page layout */
	String PAGE_SETUP="pageSetup";
	
	/** Show the parameters dialog. */
	String PARAMETERS="parameters";
	
	/** Print the displayed plot */
	String PRINT="print";
	
	/** Reload hdf file */
	String RELOAD_HDF = "reload";
	
	/** Save as to a hdf */ 
	String SAVE_AS_HDF = "saveas";
	
	/** Save gates and scalers to a hdf */ 
	String SAVE_GATES = "savegates";
	
	/** Save a group of histogram*/ 
	String SAVE_GROUP = "savegroup";

	/** Save to hdf file */
	String SAVE_HDF = "save";

	/** Save selected histograms*/ 
	String SAVE_HISTOGRAMS = "savehistograms";
	
	/** Save sort hdf histograms */ 
	String SAVE_SORT = "savesort";
	
	/** Read or zero scalers. */
	String SCALERS="scalers";

	/** Show dialog for defining a new gate. */
	String SHOW_ADD_GATE="gateshowadd";
	
	/** Show batch export dialog */
	String SHOW_BATCH_EXPORT="showbatchexport";
			
	/** Show the dialog with event and buffer counts.*/
	String SHOW_BUFFER_COUNT="showCounters";
			
	/** Show the configuration dialog. */
	String SHOW_CONFIG="showConfiguration";	
	
	/** Show dialog for displaying fit of histogram. */
	String SHOW_DISPLAY_FIT="showdisplayfit";
			
	/** Show the dialog to add a fit*/
	String SHOW_FIT_NEW="showfitnew";
			
	/** Show dialog for gain shift of histogram. */
	String SHOW_GAIN_SHIFT="showgainshift";	

	/** Show dialog to change color gradient. */
	String SHOW_GRADIENT="showgradient";	
	
	/** Show dialog for combining histogram. */
	String SHOW_HIST_COMBINE="showcombine";
	
	/** Show dialog for fitting histogram. */
	String SHOW_HIST_FIT="showfit";

	/** Show dialog for projecting histogram. */
	String SHOW_HIST_PROJECT="showproject";
	
	/** Show dialog for defining a new histogram. */
	String SHOW_HIST_ZERO="showhistzero";
	
	/** Show dialog for defining a new gate. */
	String SHOW_NEW_GATE="gateshownew";
	
	/** Show dialog for defining a new group. */
	String SHOW_NEW_GROUP="shownewgroup";

	/** Show dialog for defining a new histogram. */
	String SHOW_NEW_HIST="shownewhist";

	/** Show the peak find setup.*/
	String SHOW_PEAK_FIND="showPeakFind";

	/** Show dialog for defining a new group. */
	String SHOW_RENAME_GROUP="showrenamegroup";
	
	/** Show the dialog for zeroing scalers. */
	String SHOW_RUN_CONTROL="showruncontrol";
	
	/** Show the dialog for scaler scan */
	String SHOW_SCALER_SCAN ="showscalerscan";
	
	/** Show dialog for defining a new gate. */
	String SHOW_SET_GATE="gateshowset";
	
	/** Show the offline sorting setup dialog. */
	String SHOW_SETUP_OFF="showSetupOffline";

	/** Show the online sorting setup dialog. */
	String SHOW_SETUP_ONLINE="showSetupOnline";

	/** Show the offline sorting setup dialog. */
	String SHOW_SETUP_REMOTE="showSetupRemote";

	/** Show the dialog for zeroing scalers. */
	String SHOW_SORT_CONTROL="showsortcontrol";
	
	/** Show dialog to delete view */
	String SHOW_VIEW_DELETE="showviewdelete";
	
	/** Show dialog to add view */
	String SHOW_VIEW_NEW="showviewnew";

	/** Show the dialog for zeroing scalers. */
	String SHOW_ZERO_SCALERS="showzeroscalers";
	
	/** Start data acqisition */
	String START="start";
	
	/** Stop data acqisition */
	String STOP="stop";
	
	/** User guide */
	String USER_GUIDE="helpuserguide";	
}
