package jam.global;

/**
 * The command names, used internally and also avaliable from
 * the console
 *   
 * @author Ken Swartz
 *
 */
public final class CommandNames {
	
	CommandNames(){
		super();
	}

	/** Add hdf file */
	public static final String ADD_HDF = "addfile";
	
	/** Erase the data in memory */
	public static final String CLEAR = "clear";

	/** Show dialog for defining a new group. */
	public static final String DELETE_GROUP="deletegroup";
	
	/** Delete the displayed histogram */
	public static final String DELETE_HISTOGRAM="delhist";

	/** Show the monitor configuration dialog */
	public static final String DISPLAY_MON_CFG="displaymonitorconfig";

	/** Show the monitors */
	public static final String DISPLAY_MONITORS="displaymonitors";
	
	/** Show the scaler values. */
	public static final String DISPLAY_SCALERS="displayscalers";	

	/** Exit Jam. */
	public static final String EXIT = "exit";	

	/** Export the displayed histogram as a .his file for DAMM. */
	public static final String EXPORT_DAMM="exportdamm";	

	/** Export the displayed 1-d histogram as a Radware file. */
	public static final String EXPORT_SPE="exportspe";	

	/** Export the summary table to a file. */
	public static final String EXPORT_TABLE="exporttable";	
	
	/** Export the displayed histogram as a text file. */
	public static final String EXPORT_TEXT="exporttext";	

	/** Flush daq current buffer */
	public static final String FLUSH="flush";

	/** Help About guide */
	public static final String HELP_ABOUT="helpabout";
	
	/** Help About guide */
	public static final String HELP_LICENSE="helplicense";	
	
	/** Import a .ban file from DAMM. */
	public static final String IMPORT_BAN="importban";

	/** Import a .his file from DAMM. */
	public static final String IMPORT_DAMM="importdamm";

	/** Import a Radware file. */
	public static final String IMPORT_SPE="importspe";

	/** Import a text file. */
	public static final String IMPORT_TEXT="importtext";
	
	/** Import an XSYS file */
	public static final String IMPORT_XSYS="importxsys";
	
	/** Open an additional hdf file */
	public static final String OPEN_ADD_HDF = "openadditional";

	/** Open a hdf file */
	public static final String OPEN_HDF = "open";
	
	/** Open multiple hdf file */
	public static final String OPEN_MULTIPLE_HDF = "openmultiple";
	
	/** Open file with Yale CAEN scalers */
	public static final String OPEN_SCALERS ="openscalers";
	
	/** Open selected histograms in hdf */ 
	public static final String OPEN_SELECTED = "opensel";

	/** Setup the printing page layout */
	public static final String PAGE_SETUP="pageSetup";
	
	/** Show the parameters dialog. */
	public static final String PARAMETERS="parameters";
	
	/** Print the displayed plot */
	public static final String PRINT="print";
	
	/** Reload hdf file */
	public static final String RELOAD_HDF = "reload";
	
	/** Save as to a hdf */ 
	public static final String SAVE_AS_HDF = "saveas";
	
	/** Save gates and scalers to a hdf */ 
	public static final String SAVE_GATES = "savegates";
	
	/** Save a group of histogram*/ 
	public static final String SAVE_GROUP = "savegroup";

	/** Save to hdf file */
	public static final String SAVE_HDF = "save";

	/** Save selected histograms*/ 
	public static final String SAVE_HISTOGRAMS = "savehistograms";
	
	/** Save sort hdf histograms */ 
	public static final String SAVE_SORT = "savesort";
	
	/** Read or zero scalers. */
	public static final String SCALERS="scalers";

	/** Show dialog for defining a new gate. */
	public static final String SHOW_ADD_GATE="gateshowadd";
	
	/** Show batch export dialog */
	public static final String SHOW_BATCH_EXPORT="showbatchexport";
			
	/** Show the dialog with event and buffer counts.*/
	public static final String SHOW_BUFFER_COUNT="showCounters";
			
	/** Show the configuration dialog. */
	public static final String SHOW_CONFIG="showConfiguration";	
	
	/** Show dialog for displaying fit of histogram. */
	public static final String SHOW_DISPLAY_FIT="showdisplayfit";
			
	/** Show the dialog to add a fit*/
	public static final String SHOW_FIT_NEW="showfitnew";
			
	/** Show dialog for gain shift of histogram. */
	public static final String SHOW_GAIN_SHIFT="showgainshift";	

	/** Show dialog to change color gradient. */
	public static final String SHOW_GRADIENT="showgradient";	
	
	/** Show dialog for combining histogram. */
	public static final String SHOW_HIST_COMBINE="showcombine";
	
	/** Show dialog for fitting histogram. */
	public static final String SHOW_HIST_FIT="showfit";

	/** Show dialog for projecting histogram. */
	public static final String SHOW_HIST_PROJECT="showproject";
	
	/** Show dialog for defining a new histogram. */
	public static final String SHOW_HIST_ZERO="showhistzero";
	
	/** Show dialog for defining a new gate. */
	public static final String SHOW_NEW_GATE="gateshownew";
	
	/** Show dialog for defining a new group. */
	public static final String SHOW_NEW_GROUP="shownewgroup";

	/** Show dialog for defining a new histogram. */
	public static final String SHOW_NEW_HIST="shownewhist";

	/** Show the peak find setup.*/
	public static final String SHOW_PEAK_FIND="showPeakFind";

	/** Show dialog for defining a new group. */
	public static final String SHOW_RENAME_GROUP="showrenamegroup";
	
	/** Show the dialog for zeroing scalers. */
	public static final String SHOW_RUN_CONTROL="showruncontrol";
	
	/** Show the dialog for scaler scan */
	public static final String SHOW_SCALER_SCAN ="showscalerscan";
	
	/** Show dialog for defining a new gate. */
	public static final String SHOW_SET_GATE="gateshowset";
	
	/** Show the offline sorting setup dialog. */
	public static final String SHOW_SETUP_OFF="showSetupOffline";

	/** Show the online sorting setup dialog. */
	public static final String SHOW_SETUP_ONLINE="showSetupOnline";

	/** Show the offline sorting setup dialog. */
	public static final String SHOW_SETUP_REMOTE="showSetupRemote";

	/** Show the dialog for zeroing scalers. */
	public static final String SHOW_SORT_CONTROL="showsortcontrol";
	
	/** Show dialog to delete view */
	public static final String SHOW_VIEW_DELETE="showviewdelete";
	
	/** Show dialog to add view */
	public static final String SHOW_VIEW_NEW="showviewnew";

	/** Show the dialog for zeroing scalers. */
	public static final String SHOW_ZERO_SCALERS="showzeroscalers";
	
	/** Start data acqisition */
	public static final String START="start";
	
	/** Stop data acqisition */
	public static final String STOP="stop";
	
	/** User guide */
	public static final String USER_GUIDE="helpuserguide";	
}
