package jam;
import jam.commands.CommandManager;
import jam.data.Histogram;
import jam.fit.LoadFit;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.CommandNames;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.global.SortMode;
import jam.plot.Display;
import jam.util.ScalerScan;
import jam.util.YaleCAENgetScalers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

/**
 *
 * Jam's menu bar. Separated from JamMain to reduce its 
 * size and separate responsibilities.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 1.4
 * @since 30 Dec 2003
 */
final class MainMenuBar extends JMenuBar implements Observer, CommandNames {

	private static final String NO_FILL_MENU_TEXT = "Disable Gate Fill";

	final private JamStatus status = JamStatus.instance();
	final private JMenu fitting = new JMenu("Fitting");
	final private JMenuItem impHist = new JMenu("Import");
	final private LoadFit loadfit;
	final private Display display;
	final private MessageHandler console;
	final private JMenu calHist = new JMenu("Calibrate");
	final private CommandManager commands = CommandManager.getInstance();

	/**
	 * Jam's menu bar. It has the following menus: 
	 * <ul>
	 * <li>File</li>
	 * <li>Setup</li>
	 * <li>Control</li>
	 * <li>Histogram</li>
	 * <li>Gate</li>
	 * <li>Scalers</li>
	 * <li>Preferencs</li>
	 * <li>Fitting </li>
	 * <li>Help</li>
	 * </ul>
	 * 
	 * @author Dale Visser
	 * @author Ken Swartz
	 */
	MainMenuBar(
		JamCommand jamCommand,
		final Display d) {
		super();
		Broadcaster.getSingletonInstance().addObserver(this);
		console = status.getMessageHandler();
		display = d;
		add(getFileMenu());
		add(getSetupMenu());
		add(getControlMenu());
		add(getHistogramMenu());
		final JMenu gate = new JMenu("Gate");
		add(gate);
		gate.add(getMenuItem(SHOW_NEW_GATE));
		gate.add(getMenuItem(SHOW_ADD_GATE));
		gate.add(getMenuItem(SHOW_SET_GATE));
		final JMenu scalers = new JMenu("Scalers");
		add(scalers);
		scalers.add(getMenuItem(DISPLAY_SCALERS));
		scalers.add(getMenuItem(SHOW_ZERO_SCALERS));
		scalers.addSeparator();
		scalers.add(getMenuItem(DISPLAY_MONITORS));
		scalers.add(getMenuItem(DISPLAY_MON_CONFIG));
		add(getPreferencesMenu(jamCommand));
		loadfit = new LoadFit(console, fitting);
		add(fitting);
		final JMenuItem loadFit = new JMenuItem("Load Fit\u2026");
		loadFit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				loadfit.showLoad();
			}
		});
		fitting.add(loadFit);
		fitting.addSeparator();
		final JMenu helpMenu = new JMenu("Help");
		add(helpMenu);
		helpMenu.add(getMenuItem(HELP_ABOUT));				
		helpMenu.add(getMenuItem(USER_GUIDE));
		final JMenuItem license = new JMenuItem("License\u2026");
		helpMenu.add(license);
		license.setActionCommand("license");
		license.addActionListener(jamCommand);
		
				
	}

	private JMenu getFileMenu() {
		final JMenu file = new JMenu("File");
		file.add(getMenuItem(NEW));
		file.add(getMenuItem(OPEN_HDF));
		file.add(getMenuItem(RELOAD_HDF));
		file.add(getMenuItem(ADD_HDF));
		file.add(getMenuItem(SAVE_HDF));
		file.add(getMenuItem(SAVE_AS_HDF));
		final JMenuItem special = new JMenu("Special");
		special.add(getMenuItem(OPEN_SELECTED));
		special.add(getMenuItem(SAVE_GATES));
		file.add(special);
		file.addSeparator();
		final JMenuItem utilities = new JMenu("Utilities");
		file.add(utilities);
		final YaleCAENgetScalers ycgs =
			new YaleCAENgetScalers();
		utilities.add(new JMenuItem(ycgs.getAction()));
		final ScalerScan ss = new ScalerScan();
		utilities.add(new JMenuItem(ss.getAction()));
		file.addSeparator();
		file.add(impHist);
		impHist.add(getMenuItem(IMPORT_TEXT));
		impHist.add(getMenuItem(IMPORT_SPE));
		impHist.add(getMenuItem(IMPORT_DAMM));
		impHist.add(getMenuItem(IMPORT_XSYS));
		impHist.add(getMenuItem(IMPORT_BAN));
		final JMenu expHist = new JMenu("Export");
		file.add(expHist);
		expHist.add(getMenuItem(EXPORT_TEXT));
		expHist.add(getMenuItem(EXPORT_SPE));
		expHist.add(getMenuItem(EXPORT_DAMM));
		expHist.add(getMenuItem(SHOW_BATCH_EXPORT));
		file.addSeparator();
		file.add(commands.getAction(PRINT));
		file.add(commands.getAction(PAGE_SETUP));
		file.addSeparator();
		file.add(getMenuItem(EXIT));
		return file;
	}
	
	private JMenu getSetupMenu(){
		final JMenu setup = new JMenu("Setup");
		setup.add(getMenuItem(SHOW_SETUP_ONLINE));
		setup.add(getMenuItem(SHOW_SETUP_OFFLINE));
		final JMenuItem setupRemote = new JMenuItem("Remote Hookup\u2026");
		setupRemote.setEnabled(false);
		setup.add(setupRemote);
		return setup;
	}

	private JMenu getControlMenu() {
		final JMenu mcontrol = new JMenu("Control");
		mcontrol.add(getMenuItem(START));
		mcontrol.add(getMenuItem(STOP));
		mcontrol.add(getMenuItem(FLUSH));
		mcontrol.addSeparator();
		mcontrol.add(getMenuItem(SHOW_RUN_CONTROL));
		mcontrol.add(getMenuItem(SHOW_SORT_CONTROL));
		mcontrol.add(getMenuItem(PARAMETERS));
		mcontrol.add(getMenuItem(SHOW_BUFFER_COUNT));
		return mcontrol;
	}

	private final JMenuItem getMenuItem(String name) {
		return new JMenuItem(commands.getAction(name));
	}

	private JMenu getHistogramMenu() {
		final JMenu histogram = new JMenu("Histogram");
		histogram.add(getMenuItem(SHOW_NEW_HIST));
		histogram.add(getMenuItem(SHOW_HIST_ZERO));
		histogram.add(commands.getAction(DELETE_HISTOGRAM));
		histogram.add(calHist);
		calHist.add(getMenuItem(SHOW_HIST_FIT));
		calHist.add(getMenuItem(SHOW_HIST_DISPLAY_FIT));
		histogram.add(getMenuItem(SHOW_HIST_PROJECT));
		histogram.add(getMenuItem(SHOW_HIST_COMBINE));
		histogram.add(getMenuItem(SHOW_HIST_GAIN_SHIFT));
		return histogram;
	}

	private JMenu getPreferencesMenu(JamCommand jamCommand) {
		final JMenu mPrefer = new JMenu("Preferences");
		final JCheckBoxMenuItem ignoreZero =
			new JCheckBoxMenuItem("Ignore zero channel on autoscale", true);
		ignoreZero.setEnabled(true);
		ignoreZero.addItemListener(jamCommand);
		mPrefer.add(ignoreZero);
		final JCheckBoxMenuItem ignoreFull =
			new JCheckBoxMenuItem("Ignore max channel on autoscale", true);
		ignoreFull.setEnabled(true);
		ignoreFull.addItemListener(jamCommand);
		mPrefer.add(ignoreFull);
		final JCheckBoxMenuItem autoOnExpand =
			new JCheckBoxMenuItem("Autoscale on Expand/Zoom", true);
		autoOnExpand.setEnabled(true);
		autoOnExpand.addItemListener(jamCommand);
		mPrefer.add(autoOnExpand);
		mPrefer.addSeparator();
		final JCheckBoxMenuItem noFill2d =
			new JCheckBoxMenuItem(
				NO_FILL_MENU_TEXT,
				JamProperties.getBooleanProperty(JamProperties.NO_FILL_GATE));
		noFill2d.setEnabled(true);
		noFill2d.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				JamProperties.setProperty(
					JamProperties.NO_FILL_GATE,
					ie.getStateChange() == ItemEvent.SELECTED);
				display.repaint();
			}
		});
		mPrefer.add(noFill2d);
		final JCheckBoxMenuItem gradientColorScale =
			new JCheckBoxMenuItem(
				"Use gradient color scale",
				JamProperties.getBooleanProperty(JamProperties.GRADIENT_SCALE));
		gradientColorScale.setToolTipText(
			"Check to use a continuous gradient color scale on 2d histogram plots.");
		gradientColorScale.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				final boolean state = ie.getStateChange() == ItemEvent.SELECTED;
				JamProperties.setProperty(JamProperties.GRADIENT_SCALE, state);
				display.setPreference(
					Display.Preferences.CONTINUOUS_2D_LOG,
					state);
			}
		});
		mPrefer.add(gradientColorScale);
		mPrefer.addSeparator();
		final JCheckBoxMenuItem autoPeakFind =
			new JCheckBoxMenuItem("Automatic peak find", true);
		autoPeakFind.setEnabled(true);
		autoPeakFind.addItemListener(jamCommand);
		mPrefer.add(autoPeakFind);
		final JMenuItem peakFindPrefs =
			new JMenuItem("Peak Find Properties\u2026");
		peakFindPrefs.setActionCommand("peakfind");
		peakFindPrefs.addActionListener(jamCommand);
		mPrefer.add(peakFindPrefs);
		mPrefer.addSeparator();
		final ButtonGroup colorScheme = new ButtonGroup();
		final JRadioButtonMenuItem whiteOnBlack =
			new JRadioButtonMenuItem("Black Background", false);
		whiteOnBlack.setEnabled(true);
		whiteOnBlack.addActionListener(jamCommand);
		colorScheme.add(whiteOnBlack);
		mPrefer.add(whiteOnBlack);
		final JRadioButtonMenuItem blackOnWhite =
			new JRadioButtonMenuItem("White Background", true);
		blackOnWhite.setEnabled(true);
		blackOnWhite.addActionListener(jamCommand);
		colorScheme.add(blackOnWhite);
		mPrefer.add(blackOnWhite);
		mPrefer.addSeparator();
		final JCheckBoxMenuItem verboseVMEReply =
			new JCheckBoxMenuItem("Verbose front end", false);
		verboseVMEReply.setEnabled(true);
		verboseVMEReply.setToolTipText(
			"If selected, the front end will send verbose messages.");
		JamProperties.setProperty(
			JamProperties.FRONTEND_VERBOSE,
			verboseVMEReply.isSelected());
		verboseVMEReply.addItemListener(jamCommand);
		mPrefer.add(verboseVMEReply);
		final JCheckBoxMenuItem debugVME =
			new JCheckBoxMenuItem("Debug front end", false);
		debugVME.setToolTipText(
			"If selected, the front end will send debugging messages.");
		debugVME.setEnabled(true);
		JamProperties.setProperty(
			JamProperties.FRONTEND_DEBUG,
			debugVME.isSelected());
		debugVME.addItemListener(jamCommand);
		mPrefer.add(debugVME);
		return mPrefer;
	}

	private void sortModeChanged() {
		final SortMode mode = status.getSortMode();
		final boolean file = mode == SortMode.FILE || mode == SortMode.NO_SORT;
		impHist.setEnabled(file);
	}

	void adjustHistogramItems(Histogram h) {
		final boolean hExists = h != null;
		final boolean oneDops = hExists && h.getDimensionality() == 1;
		calHist.setEnabled(oneDops);
	}

	public void update(Observable observe, Object obj) {
		final BroadcastEvent be = (BroadcastEvent) obj;
		final int command = be.getCommand();
		if (command == BroadcastEvent.SORT_MODE_CHANGED) {
			sortModeChanged();
		}
	}
}