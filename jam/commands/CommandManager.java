package jam.commands;

import jam.JamPrefs;
import jam.global.Broadcaster;
import jam.global.CommandListener;
import jam.global.CommandListenerException;
import jam.global.CommandNames;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.plot.PlotPrefs;
import jam.plot.color.ColorPrefs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observer;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.Action;

/**
 * Class to create commands and execute them
 *
 * @author Ken Swartz
 */
public class CommandManager implements CommandListener, CommandNames {

	private final JamStatus status=JamStatus.getSingletonInstance();
	private final MessageHandler msghdlr;
	private static CommandManager managerInstance=null;
	private static final Map CMD_MAP = Collections.synchronizedMap(new HashMap());
	private static final Map INSTANCES=Collections.synchronizedMap(new HashMap());
	private Commandable currentCommand;
	
	/* initializer block for map */
	static {
		/* File Menu */
		CMD_MAP.put(OPEN_HDF, OpenHDFCmd.class);
		CMD_MAP.put(SAVE_HDF, SaveHDFCmd.class);
		CMD_MAP.put(SAVE_AS_HDF, SaveAsHDFCmd.class);
		CMD_MAP.put(SAVE_GATES, SaveGatesCmd.class);
		CMD_MAP.put(ADD_HDF, AddHDFCmd.class);
		CMD_MAP.put(RELOAD_HDF, ReloadHDFCmd.class);
		/* Histogram Menu */
		CMD_MAP.put(SHOW_NEW_HIST, ShowDialogNewHistogramCmd.class);
		CMD_MAP.put(SHOW_HIST_ZERO, ShowDialogZeroHistogram.class);
		CMD_MAP.put(SHOW_HIST_COMBINE, ShowDialogHistManipulationsCmd.class);
		CMD_MAP.put(SHOW_HIST_PROJECT, ShowDialogHistProjectionCmd.class);
		CMD_MAP.put(SHOW_HIST_FIT, ShowDialogCalibrationFitCmd.class);
		CMD_MAP.put(SHOW_HIST_DISPLAY_FIT, ShowDialogCalibrationDisplayCmd.class);
		CMD_MAP.put(SHOW_HIST_GAIN_SHIFT, ShowDialogGainShiftCmd.class);				 
	    /* Gate Menu */
		CMD_MAP.put(SHOW_NEW_GATE, ShowDialogNewGateCmd.class);
		CMD_MAP.put(SHOW_SET_GATE, ShowDialogSetGate.class); 
		CMD_MAP.put(SHOW_ADD_GATE, ShowDialogAddGate.class); 
		CMD_MAP.put(SHOW_RUN_CONTROL, ShowRunControl.class); 
		CMD_MAP.put(SHOW_SORT_CONTROL, ShowSortControl.class); 
		CMD_MAP.put(START, StartAcquisition.class);
		CMD_MAP.put(STOP, StopAcquisition.class);
		CMD_MAP.put(FLUSH, FlushAcquisition.class);
		CMD_MAP.put(EXIT, ShowExitDialog.class);
		CMD_MAP.put(CLEAR, FileNewClearCmd.class);
		CMD_MAP.put(PARAMETERS, ShowDialogParametersCmd.class);
		CMD_MAP.put(DISPLAY_SCALERS, ShowDialogScalersCmd.class);
		CMD_MAP.put(SHOW_ZERO_SCALERS, ShowDialogZeroScalersCmd.class);
		CMD_MAP.put(SCALERS, ScalersCmd.class);
		CMD_MAP.put(EXPORT_TEXT, ExportTextFileCmd.class);
		CMD_MAP.put(EXPORT_DAMM, ExportDamm.class);
		CMD_MAP.put(EXPORT_SPE, ExportRadware.class);	
		CMD_MAP.put(PRINT, Print.class);
		CMD_MAP.put(PAGE_SETUP, PageSetupCmd.class);	 
		CMD_MAP.put(IMPORT_TEXT, ImportTextFile.class);
		CMD_MAP.put(IMPORT_DAMM, ImportDamm.class);
		CMD_MAP.put(IMPORT_SPE, ImportRadware.class);
		CMD_MAP.put(IMPORT_XSYS, ImportXSYS.class);
		CMD_MAP.put(IMPORT_BAN, ImportORNLban.class);
		CMD_MAP.put(OPEN_SCALERS_YALE_CAEN, OpenScalersYaleCAEN.class);	
		CMD_MAP.put(SHOW_SCALER_SCAN, ShowDialogScalerScan.class);		
		CMD_MAP.put(DELETE_HISTOGRAM, DeleteHistogram.class);
		CMD_MAP.put(HELP_ABOUT, ShowDialogAbout.class);
		CMD_MAP.put(HELP_LICENSE, ShowDialogLicense.class);
		CMD_MAP.put(USER_GUIDE, ShowUserGuide.class);	
		CMD_MAP.put(OPEN_SELECTED, OpenSelectedHistogram.class);
		CMD_MAP.put(DISPLAY_MONITORS, ShowMonitorDisplay.class);
		CMD_MAP.put(DISPLAY_MON_CONFIG, ShowMonitorConfig.class);
		CMD_MAP.put(SHOW_BATCH_EXPORT, ShowBatchExport.class);
		CMD_MAP.put(SHOW_SETUP_ONLINE, ShowSetupOnline.class);
		CMD_MAP.put(SHOW_SETUP_OFFLINE, ShowSetupOffline.class);
		CMD_MAP.put(SHOW_BUFFER_COUNT, ShowDialogCounters.class);
		/* View menu */
		CMD_MAP.put(SHOW_VIEW_NEW, ShowDialogAddView.class);
		CMD_MAP.put(SHOW_VIEW_DELETE, ShowDialogDeleteView.class);
		/* Fit Menu */
		CMD_MAP.put(SHOW_FIT_NEW, ShowDialogAddFit.class);
		/* Preferences Menu */
		CMD_MAP.put(PlotPrefs.AUTO_IGNORE_ZERO, SetAutoScaleIgnoreZero.class);
		CMD_MAP.put(PlotPrefs.AUTO_IGNORE_FULL, SetAutoScaleIgnoreFull.class);
		CMD_MAP.put(PlotPrefs.BLACK_BACKGROUND, SetBlackBackground.class);
		CMD_MAP.put(PlotPrefs.AUTO_PEAK_FIND, SetAutoPeakFind.class);
		CMD_MAP.put(ColorPrefs.SMOOTH_COLOR_SCALE, 
		SetSmoothColorScale.class);
		CMD_MAP.put(SHOW_GRADIENT_SETTINGS,ShowGradientSettings.class);
		CMD_MAP.put(PlotPrefs.AUTO_ON_EXPAND, SetAutoScaleOnExpand.class);
		CMD_MAP.put(PlotPrefs.HIGHLIGHT_GATE_CHANNELS, SetGatedChannelsHighlight.class);
		CMD_MAP.put(PlotPrefs.ENABLE_SCROLLING, SetEnableScrolling.class);
		CMD_MAP.put(PlotPrefs.DISPLAY_AXIS_LABELS, SetAxisLabels.class);		
		CMD_MAP.put(JamPrefs.VERBOSE, SetVerbose.class);
		CMD_MAP.put(JamPrefs.DEBUG, SetDebug.class);
		CMD_MAP.put(SHOW_PEAK_FIND, ShowDialogPeakFind.class);
		CMD_MAP.put(SHOW_SETUP_REMOTE, ShowSetupRemote.class);
	}
	

	/**
	 * Constructor private as singleton
	 *
	 */
	private CommandManager() {
		msghdlr=status.getMessageHandler();
	}
	
	/**
	 * Singleton accessor.
	 * 
	 * @return the unique instance of this class
	 */
	public static CommandManager getInstance () {
		if (managerInstance==null) {
			managerInstance=new CommandManager();
		}		
		return managerInstance;
	}
	
	/**
	 * Perform command with string parameters
	 *
	 * @param strCmd 		String key indicating the command
	 * @param strCmdParams  Command parameters as strings
	 * @return <code>true</code> if successful
	 */
	public boolean performParseCommand(String strCmd, String[] strCmdParams) {
		boolean validCommand=false;
		if (createCmd(strCmd)) {
			if (currentCommand.isEnabled()){
				try{
					currentCommand.performParseCommand(strCmdParams);
				} catch (CommandListenerException cle){
					msghdlr.errorOutln("Performing command "+strCmd+"; "+
					cle.getMessage());
				}
			} else {
				msghdlr.errorOutln("Disabled command \""+strCmd+"\"");
			}
			validCommand=true;
		} 
		return validCommand;
	}
	
	/**
	 * See if we have the instance created, create it if necessary,
	 * and return whether it was successfully created. 
	 * 
	 * @param strCmd name of the command
	 * @return <code>true</code> if successful, <code>false</code> if 
	 * the given command doesn't exist
	 */
	private boolean createCmd(String strCmd)  {
		final boolean exists=CMD_MAP.containsKey(strCmd);
		if (exists) {
			final Class cmdClass = (Class)CMD_MAP.get(strCmd);
			currentCommand = null;
			final boolean created=INSTANCES.containsKey(strCmd);
			if (created){
				currentCommand=(Commandable) INSTANCES.get(strCmd);
			} else {
				try {
					currentCommand = (Commandable) (cmdClass.newInstance());
					currentCommand.initCommand();
					if (currentCommand instanceof Observer){
						Broadcaster.getSingletonInstance().addObserver(
						(Observer)currentCommand);
					}
				} catch (Exception e) {
					/* There was a problem resolving the command class or 
					 * with creating an instance. This should never happen
					 * if exists==true. */
					throw new IllegalArgumentException(e.getMessage());
				}
				INSTANCES.put(strCmd,currentCommand);
			}
		}
		return exists;
	}
	
	/**
	 * 
	 * @param strCmd the command to type
	 * @return the action
	 */
	public Action getAction(String strCmd){
		return createCmd(strCmd) ? currentCommand : null;
	}
	
	/**
	 * 
	 * @param cmd the command to type
	 * @param enable <code>true</code> if enabled
	 */
	public void setEnabled(String cmd, boolean enable){
		getAction(cmd).setEnabled(enable);
	}
	
	/**
	 * Help the user by getting similar commands.
	 * 
	 * @param s what the user typed
	 * @param onlyEnabled <code>true</code> means only return enabled commands
	 * @return list of similar commands
	 */
	public String [] getSimilarCommnands(final String s, boolean onlyEnabled){
		final SortedSet sim=new TreeSet();
		final Set keys=CMD_MAP.keySet();
		for (int i=s.length(); i>=1; i--){
			final String com=s.substring(0,i);
			for (Iterator it=keys.iterator(); it.hasNext();){
				final String key=(String)it.next();
				if (key.startsWith(com)){
					final boolean addIt=(!onlyEnabled) || 
					getAction(key).isEnabled();
					if (addIt){
						sim.add(key);
					}
				}
			}
			if (!sim.isEmpty()){
				break;
			}
		}
		final String [] rval=new String[sim.size()];
		sim.toArray(rval);
		return rval;
	}
	
	/**
	 * @return all commands in the map in alphabetical order
	 */
	public String [] getAllCommands(){
		final SortedSet sorted=new TreeSet(CMD_MAP.keySet());
		final String [] rval=new String[sorted.size()];
		sorted.toArray(rval);
		return rval;
	}
}
