package jam.commands;

import jam.comm.CommunicationPreferences;
import jam.global.Broadcaster;
import jam.global.CommandFinder;
import jam.global.CommandListener;
import jam.global.CommandListenerException;
import jam.io.hdf.HDFPrefs;
import jam.plot.PlotPrefs;
import jam.plot.color.ColorPrefs;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;

/**
 * Class to create commands and execute them
 * 
 * @author Ken Swartz
 */
public class CommandManager implements CommandListener, CommandFinder {

	private static final Object classMonitor = new Object();

	private static final Map<String, Class<? extends Commandable>> CMD_MAP = Collections
			.synchronizedMap(new HashMap<String, Class<? extends Commandable>>());

	private static CommandManager instance = null;

	private static final Map<String, Commandable> INSTANCES = Collections
			.synchronizedMap(new HashMap<String, Commandable>());

	private static final Logger LOGGER = Logger.getLogger(CommandManager.class
			.getPackage().getName());

	private static final Commandable NO_COMMAND = new NoCommand();

	/* initializer block for map */
	static {
		/* File Menu */
		CMD_MAP.put(CommandNames.OPEN_HDF, OpenHDFCmd.class);
		CMD_MAP.put(CommandNames.OPEN_ADD_HDF, OpenAdditionalHDF.class);
		CMD_MAP.put(CommandNames.OPEN_MULTIPLE_HDF, OpenMultipleHDFCmd.class);

		CMD_MAP.put(CommandNames.SAVE_HDF, SaveHDFCmd.class);
		CMD_MAP.put(CommandNames.SAVE_AS_HDF, SaveAsHDFCmd.class);
		CMD_MAP.put(CommandNames.SAVE_GATES, SaveGatesCmd.class);
		CMD_MAP.put(CommandNames.SAVE_GROUP, SaveGroupHDFCmd.class);
		CMD_MAP.put(CommandNames.SAVE_SORT, SaveSortGroupHDFCmd.class);
		CMD_MAP.put(CommandNames.SAVE_HISTOGRAMS,
				SaveSelectHistogramsHDFCmd.class);

		CMD_MAP.put(CommandNames.ADD_HDF, AddHDF.class);
		CMD_MAP.put(CommandNames.RELOAD_HDF, ReloadHDFCmd.class);

		CMD_MAP.put(CommandNames.SHOW_NEW_GROUP, ShowDialogNewGroup.class);
		CMD_MAP
				.put(CommandNames.SHOW_RENAME_GROUP,
						ShowDialogRenameGroup.class);
		CMD_MAP.put(CommandNames.DELETE_GROUP, DeleteGroup.class);

		/* Histogram Menu */
		CMD_MAP
				.put(CommandNames.SHOW_NEW_HIST,
						ShowDialogNewHistogramCmd.class);
		CMD_MAP.put(CommandNames.SHOW_HIST_ZERO, ShowDialogZeroHistogram.class);
		CMD_MAP.put(CommandNames.SHOW_HIST_COMBINE,
				ShowDialogHistManipulationsCmd.class);
		CMD_MAP.put(CommandNames.SHOW_HIST_PROJECT,
				ShowDialogHistProjectionCmd.class);
		CMD_MAP.put(CommandNames.SHOW_HIST_FIT,
				ShowDialogCalibrationFitCmd.class);
		CMD_MAP.put(CommandNames.SHOW_GAIN_SHIFT, ShowDialogGainShiftCmd.class);
		/* Gate Menu */
		CMD_MAP.put(CommandNames.SHOW_NEW_GATE, ShowDialogNewGateCmd.class);
		CMD_MAP.put(CommandNames.SHOW_SET_GATE, ShowDialogSetGate.class);
		CMD_MAP.put(CommandNames.SHOW_ADD_GATE, ShowDialogAddGate.class);
		CMD_MAP.put(CommandNames.SHOW_RUN_CONTROL, ShowRunControl.class);
		CMD_MAP.put(CommandNames.SHOW_SORT_CONTROL, ShowSortControl.class);
		CMD_MAP.put(CommandNames.START, StartAcquisition.class);
		CMD_MAP.put(CommandNames.STOP, StopAcquisition.class);
		CMD_MAP.put(CommandNames.FLUSH, FlushAcquisition.class);
		CMD_MAP.put(CommandNames.EXIT, ShowExitDialog.class);
		CMD_MAP.put(CommandNames.CLEAR, FileNewClearCmd.class);
		CMD_MAP.put(CommandNames.PARAMETERS, ShowDialogParametersCmd.class);
		CMD_MAP.put(CommandNames.DISPLAY_SCALERS, ShowDialogScalersCmd.class);
		CMD_MAP.put(CommandNames.SHOW_ZERO_SCALERS,
				ShowDialogZeroScalersCmd.class);
		CMD_MAP.put(CommandNames.SCALERS, ScalersCmd.class);
		CMD_MAP.put(CommandNames.EXPORT_TABLE, ExportSummaryTableCmd.class);
		CMD_MAP.put(CommandNames.EXPORT_TEXT, ExportTextFileCmd.class);
		CMD_MAP.put(CommandNames.EXPORT_DAMM, ExportDamm.class);
		CMD_MAP.put(CommandNames.EXPORT_SPE, ExportRadware.class);
		CMD_MAP.put(CommandNames.PRINT, Print.class);
		CMD_MAP.put(CommandNames.PAGE_SETUP, PageSetupCmd.class);
		CMD_MAP.put(CommandNames.IMPORT_TEXT, ImportTextFile.class);
		CMD_MAP.put(CommandNames.IMPORT_DAMM, ImportDamm.class);
		CMD_MAP.put(CommandNames.IMPORT_SPE, ImportRadware.class);
		CMD_MAP.put(CommandNames.IMPORT_XSYS, ImportXSYS.class);
		CMD_MAP.put(CommandNames.IMPORT_BAN, ImportORNLban.class);
		CMD_MAP.put(CommandNames.OPEN_SCALERS, OpenScalersYaleCAEN.class);
		CMD_MAP.put(CommandNames.SHOW_SCALER_SCAN, ShowDialogScalerScan.class);
		CMD_MAP.put(CommandNames.DELETE_HISTOGRAM, DeleteHistogram.class);
		CMD_MAP.put(CommandNames.HELP_ABOUT, ShowDialogAbout.class);
		CMD_MAP.put(CommandNames.HELP_LICENSE, ShowDialogLicense.class);
		CMD_MAP.put(CommandNames.USER_GUIDE, ShowUserGuide.class);
		CMD_MAP.put(CommandNames.OPEN_SELECTED, OpenSelectedHistogram.class);
		CMD_MAP.put(CommandNames.DISPLAY_MONITORS, ShowMonitorDisplay.class);
		CMD_MAP.put(CommandNames.DISPLAY_MON_CFG, ShowMonitorConfig.class);
		CMD_MAP.put(CommandNames.SHOW_BATCH_EXPORT, ShowBatchExport.class);
		CMD_MAP.put(CommandNames.SHOW_SETUP_ONLINE, ShowSetupOnline.class);
		CMD_MAP.put(CommandNames.SHOW_SETUP_OFF, ShowSetupOffline.class);
		CMD_MAP.put(CommandNames.SHOW_BUFFER_COUNT, ShowDialogCounters.class);
		CMD_MAP.put(CommandNames.SHOW_CONFIG, ShowDialogConfiguration.class);
		/* View menu */
		CMD_MAP.put(CommandNames.SHOW_VIEW_NEW, ShowDialogAddView.class);
		CMD_MAP.put(CommandNames.SHOW_VIEW_DELETE, ShowDialogDeleteView.class);
		/* Fit Menu */
		CMD_MAP.put(CommandNames.SHOW_FIT_NEW, ShowDialogAddFit.class);
		/* Preferences Menu */
		CMD_MAP.put(PlotPrefs.AUTO_IGNORE_ZERO, SetAutoScaleIgnoreZero.class);
		CMD_MAP.put(PlotPrefs.AUTO_IGNORE_FULL, SetAutoScaleIgnoreFull.class);
		CMD_MAP.put(PlotPrefs.BLACK_BACKGROUND, SetBlackBackground.class);
		CMD_MAP.put(PlotPrefs.AUTO_PEAK_FIND, SetAutoPeakFind.class);
		CMD_MAP.put(ColorPrefs.SMOOTH_SCALE, SetSmoothColorScale.class);
		CMD_MAP.put(CommandNames.SHOW_GRADIENT, ShowGradientSettings.class);
		CMD_MAP.put(PlotPrefs.AUTO_ON_EXPAND, SetAutoScaleOnExpand.class);
		CMD_MAP.put(PlotPrefs.HIGHLIGHT_GATE, SetGatedChannelsHighlight.class);
		CMD_MAP.put(PlotPrefs.ENABLE_SCROLLING, SetEnableScrolling.class);
		CMD_MAP.put(PlotPrefs.DISPLAY_LABELS, SetAxisLabels.class);
		CMD_MAP.put(HDFPrefs.SUPPRES_EMPTY, SetHDFSuppressSaveEmpty.class);
		CMD_MAP.put(CommunicationPreferences.VERBOSE, SetVerbose.class);
		CMD_MAP.put(CommunicationPreferences.DEBUG, SetDebug.class);
		CMD_MAP.put(CommandNames.SHOW_PEAK_FIND, ShowDialogPeakFind.class);
		CMD_MAP.put(CommandNames.SHOW_SETUP_REMOTE, ShowSetupRemote.class);
	}

	/**
	 * Singleton accessor.
	 * 
	 * @return the unique instance of this class
	 */
	public static CommandManager getInstance() {
		synchronized (classMonitor) {
			if (instance == null) {
				instance = new CommandManager();
			}
			return instance;
		}
	}

	private transient Commandable currentCom;

	/**
	 * Constructor private as singleton
	 * 
	 */
	private CommandManager() {
		super();
	}

	/**
	 * See if we have the instance created, create it if necessary, and return
	 * whether it was successfully created.
	 * 
	 * @param strCmd
	 *            name of the command
	 * @return <code>true</code> if successful, <code>false</code> if the
	 *         given command doesn't exist
	 */
	private boolean createCmd(final String strCmd) {
		final boolean exists = CMD_MAP.containsKey(strCmd);
		if (exists) {
			final Class<? extends Commandable> cmdClass = CMD_MAP.get(strCmd);
			currentCom = NO_COMMAND;
			final boolean created = INSTANCES.containsKey(strCmd);
			if (created) {
				currentCom = INSTANCES.get(strCmd);
			} else {
				try {
					currentCom = cmdClass.newInstance();
					currentCom.initCommand();
					if (currentCom instanceof Observer) {
						Broadcaster.getSingletonInstance().addObserver(
								(Observer) currentCom);
					}
				} catch (InstantiationException ie) {
					/*
					 * There was a problem resolving the command class or with
					 * creating an instance. This should never happen if
					 * exists==true.
					 */
					LOGGER.log(Level.SEVERE, ie.getMessage(), ie);
				} catch (IllegalAccessException iae) {
					LOGGER.log(Level.SEVERE, iae.getMessage(), iae);
				}
				INSTANCES.put(strCmd, currentCom);
			}
		}
		return exists;
	}

	/**
	 * 
	 * @param strCmd
	 *            the command to type
	 * @return the action
	 */
	public Action getAction(final String strCmd) {
		Action rval = null;
		if (createCmd(strCmd)) {
			rval = currentCom;
		}
		return rval;
	}

	/**
	 * @return all commands in the map in alphabetical order
	 */
	public Collection<String> getAllCommands() {
		return new TreeSet<String>(CMD_MAP.keySet());
	}

	/* (non-Javadoc)
	 * @see jam.commands.CommandFinder#getSimilarCommnands(java.lang.String, boolean)
	 */
	public Collection<String> getSimilarCommnands(final String string,
			final boolean onlyEnabled) {
		final SortedSet<String> rval = new TreeSet<String>();
		final Set<String> keys = CMD_MAP.keySet();
		for (int i = string.length(); i >= 1; i--) {
			final String com = string.substring(0, i);
			for (String element : keys) {
				final String key = element;
				if (key.startsWith(com)) {
					final boolean addIt = (!onlyEnabled)
							|| getAction(key).isEnabled();
					if (addIt) {
						rval.add(key);
					}
				}
			}
			if (!rval.isEmpty()) {
				break;
			}
		}
		return Collections.unmodifiableCollection(rval);
	}

	/**
	 * Perform command with string parameters
	 * 
	 * @param strCmd
	 *            String key indicating the command
	 * @param strCmdParams
	 *            Command parameters as strings
	 * @return <code>true</code> if successful
	 */
	public boolean performParseCommand(final String strCmd,
			final String[] strCmdParams) {
		boolean validCommand = false;
		if (createCmd(strCmd)) {
			if (currentCom.isEnabled()) {
				try {
					currentCom.performParseCommand(strCmdParams);
				} catch (CommandListenerException cle) {
					LOGGER.log(Level.SEVERE, "Performing command " + strCmd
							+ "; " + cle.getMessage(), cle);
				}
			} else {
				LOGGER.severe("Disabled command \"" + strCmd + "\"");
			}
			validCommand = true;
		}
		return validCommand;
	}

	/**
	 * 
	 * @param cmd
	 *            the command to type
	 * @param enable
	 *            <code>true</code> if enabled
	 */
	public void setEnabled(final String cmd, final boolean enable) {
		getAction(cmd).setEnabled(enable);
	}
}// NOPMD
