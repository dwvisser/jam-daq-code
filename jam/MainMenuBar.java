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

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

/**
 *
 * Jam's menu bar. Separated from JamMain to reduce its 
 * size and separate responsibilities.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 1.4
 * @since 30 Dec 2003
 */
final class MainMenuBar extends JMenuBar implements Observer {

	private static final String NO_FILL_MENU_TEXT = "Disable Gate Fill";
	private final static int CTRL_MASK= JamProperties.isMacOSX() ? 
	Event.META_MASK : Event.CTRL_MASK;

	final private Action paramAction;
	final private JamStatus status=JamStatus.instance();
	final private JMenu fitting=new JMenu("Fitting");;
	final private JMenuItem impHist= new JMenu("Import");
	final private JMenuItem runacq = new JMenuItem("Run\u2026");
	final private JMenuItem sortacq = new JMenuItem("Sort\u2026");
	final private JMenuItem statusacq = new JMenuItem("Buffer Count\u2026");
	final private JMenuItem iflushacq = new JMenuItem("flush");
	final private JCheckBoxMenuItem cstartacq= new JCheckBoxMenuItem("start", false);
	final private JCheckBoxMenuItem cstopacq = new JCheckBoxMenuItem("stop", true);
	final private LoadFit loadfit;
	final private Display display;
	final private JamMain jamMain;
	final private MessageHandler console;
	final private JMenuItem zeroHistogram = new JMenuItem("Zero\u2026");
	final private JMenu calHist = new JMenu("Calibrate");
	final private JMenuItem projectHistogram = new JMenuItem("Projections\u2026");
	final private JMenuItem manipHistogram = new JMenuItem("Combine\u2026");
	final private JMenuItem gainShift = new JMenuItem("Gain Shift\u2026");
	final private CommandManager commands=CommandManager.getInstance();
	
	/**
	 * Define and display menu bar.
	 * The menu bar has the following menus: 
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
	 * @author Ken Swartz
	 * @return the menu bar
	 */
	MainMenuBar(
		final JamMain jm,
		JamCommand jamCommand,
		final Display d,
		MessageHandler c) {
		super();
		paramAction=commands.getAction(CommandNames.PARAMETERS);
		Broadcaster.getSingletonInstance().addObserver(this);
		console=c;
		display = d;
		jamMain=jm;
		/* load fitting routine */
		add(getFileMenu());
		final JMenu setup = new JMenu("Setup");
		add(setup);
		final JMenuItem setupOnline = new JMenuItem("Online sorting\u2026");
		setupOnline.setActionCommand("online");
		setupOnline.addActionListener(jamCommand);
		setup.add(setupOnline);
		setup.addSeparator();
		final JMenuItem setupOffline = new JMenuItem("Offline sorting\u2026");
		setupOffline.setActionCommand("offline");
		setupOffline.addActionListener(jamCommand);
		setup.add(setupOffline);
		setup.addSeparator();
		final JMenuItem setupRemote = new JMenuItem("Remote Hookup\u2026");
		setupRemote.setActionCommand("remote");
		setupRemote.addActionListener(jamCommand);
		setupRemote.setEnabled(false);
		setup.add(setupRemote);
		add(getControlMenu(jamCommand));
		add(getHistogramMenu(jamCommand));
		final JMenu gate = new JMenu("Gate");
		add(gate);
		final JMenuItem gateNew = new JMenuItem(commands.getAction(
				CommandNames.SHOW_NEW_GATE));
			gate.add(gateNew);				
		final JMenuItem gateAdd = new JMenuItem(commands.getAction(
		CommandNames.SHOW_ADD_GATE));
		gate.add(gateAdd);
		final JMenuItem gateSet = new JMenuItem(commands.getAction(
		CommandNames.SHOW_SET_GATE));
		gate.add(gateSet);
		final JMenu scalers = new JMenu("Scalers");
		add(scalers);
		final JMenuItem showScalers = new JMenuItem(commands.getAction(
		CommandNames.DISPLAY_SCALERS));
		scalers.add(showScalers);
		final JMenuItem clearScalers = new JMenuItem(commands.getAction(
		CommandNames.SHOW_ZERO_SCALERS));
		scalers.add(clearScalers);
		scalers.addSeparator();
		final JMenuItem showMonitors = new JMenuItem(commands.getAction(
		CommandNames.DISPLAY_MONITORS));
		scalers.add(showMonitors);
		final JMenuItem configMonitors = new JMenuItem(commands.getAction(
		CommandNames.DISPLAY_MON_CONFIG));
		scalers.add(configMonitors);
		add(getPreferencesMenu(jamCommand));
		loadfit = new LoadFit(jm, display, console, fitting);
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
		final JMenuItem about = new JMenuItem("About\u2026");
		helpMenu.add(about);
		about.setActionCommand("about");
		about.addActionListener(jamCommand);
		final JMenuItem userG = new JMenuItem(
		commands.getAction(CommandNames.USER_GUIDE));
		helpMenu.add(userG);
		final JMenuItem license = new JMenuItem("License\u2026");
		helpMenu.add(license);
		license.setActionCommand("license");
		license.addActionListener(jamCommand);
	}
	
	private JMenu getFileMenu(){
		final JMenu file = new JMenu("File");
		final JMenuItem newClear = new JMenuItem(commands.getAction(CommandNames.NEW));
		newClear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,CTRL_MASK));		
		file.add(newClear);
		final JMenuItem openhdf = new JMenuItem(commands.getAction(CommandNames.OPEN_HDF));
		openhdf.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,CTRL_MASK));
		file.add(openhdf);
		final JMenuItem reloadhdf = new JMenuItem(commands.getAction(CommandNames.RELOAD_HDF));
		reloadhdf.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,CTRL_MASK | Event.SHIFT_MASK));		
		file.add(reloadhdf);
		final JMenuItem addhdf=new JMenuItem(commands.getAction(
		CommandNames.ADD_HDF));
		file.add(addhdf);
		final JMenuItem saveHDF  = new JMenuItem(commands.getAction(CommandNames.SAVE_HDF));
		saveHDF.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, CTRL_MASK|KeyEvent.SHIFT_MASK));
		file.add(saveHDF);		
		final JMenuItem saveAsHDF  = new JMenuItem(commands.getAction(CommandNames.SAVE_AS_HDF));
		saveAsHDF.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, CTRL_MASK));
		file.add(saveAsHDF);		
		final JMenuItem special=new JMenu("Special");
		final JMenuItem openSelectdHist =new JMenuItem(commands.getAction(
		CommandNames.OPEN_SELECTED));
		special.add(openSelectdHist);
		final JMenuItem saveGates=new JMenuItem(commands.getAction(CommandNames.SAVE_GATES));
		special.add(saveGates);
		file.add(special);
		file.addSeparator();
		final JMenuItem utilities=new JMenu("Utilities");
		file.add(utilities);
		final YaleCAENgetScalers ycgs=new YaleCAENgetScalers(jamMain,console);
		utilities.add(new JMenuItem(ycgs.getAction()));
		final ScalerScan ss=new ScalerScan(jamMain,console);
		utilities.add(new JMenuItem(ss.getAction()));
		file.addSeparator();
		file.add(impHist);
		final JMenuItem openascii = new JMenuItem(commands.getAction(
		CommandNames.IMPORT_TEXT));
		impHist.add(openascii);
		final JMenuItem openspe = new JMenuItem(commands.getAction(
		CommandNames.IMPORT_SPE));
		impHist.add(openspe);
		final JMenuItem openornl = new JMenuItem(commands.getAction(
		CommandNames.IMPORT_DAMM));
		impHist.add(openornl);
		final JMenuItem openxsys = new JMenuItem(commands.getAction(
		CommandNames.IMPORT_XSYS));
		impHist.add(openxsys);
		final JMenuItem importBan = new JMenuItem(commands.getAction(
		CommandNames.IMPORT_BAN));
		impHist.add(importBan);
		final JMenu expHist = new JMenu("Export");
		file.add(expHist);
		final JMenuItem saveascii = new JMenuItem(commands.getAction(CommandNames.EXPORT_TEXT));
		expHist.add(saveascii);
		final JMenuItem savespe = new JMenuItem(commands.getAction(CommandNames.EXPORT_SPE));
		expHist.add(savespe);
		final JMenuItem saveornl = new JMenuItem(commands.getAction(CommandNames.EXPORT_DAMM));
		expHist.add(saveornl);
		final JMenuItem batchexport = new JMenuItem(commands.getAction(
		CommandNames.SHOW_BATCH_EXPORT));
		expHist.add(batchexport);
		file.addSeparator();
		file.add(commands.getAction(CommandNames.PRINT)).setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_P, CTRL_MASK));
		file.add(commands.getAction(CommandNames.PAGE_SETUP)).setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_P, 
		CTRL_MASK | Event.SHIFT_MASK));
		file.addSeparator();
		final JMenuItem exit = new JMenuItem(commands.getAction(
		CommandNames.EXIT));
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,CTRL_MASK));
		file.add(exit);
		return file;
	}
	
	private JMenu getControlMenu(JamCommand jamCommand){
		final JMenu mcontrol = new JMenu("Control");
		cstartacq.setEnabled(false);
		cstartacq.addActionListener(jamCommand);
		mcontrol.add(cstartacq);
		cstopacq.setEnabled(false);
		cstopacq.addActionListener(jamCommand);
		mcontrol.add(cstopacq);
		iflushacq.setEnabled(false);
		iflushacq.addActionListener(jamCommand);
		mcontrol.add(iflushacq);
		mcontrol.addSeparator();
		runacq.setEnabled(false);
		runacq.setActionCommand("run");
		runacq.addActionListener(jamCommand);
		mcontrol.add(runacq);
		sortacq.setEnabled(false);
		sortacq.setActionCommand("sort");
		sortacq.addActionListener(jamCommand);
		mcontrol.add(sortacq);
		final JMenuItem paramacq = new JMenuItem(paramAction);
		paramAction.setEnabled(false);
		paramacq.setActionCommand("parameters");
		paramacq.addActionListener(jamCommand);
		mcontrol.add(paramacq);
		statusacq.setEnabled(false);
		statusacq.setActionCommand("status");
		statusacq.addActionListener(jamCommand);
		mcontrol.add(statusacq);
		return mcontrol;
	}
	
	private JMenu getHistogramMenu(JamCommand jamCommand){
		final JMenu histogram = new JMenu("Histogram");
		
		final JMenuItem histogramNew=new JMenuItem(commands.getAction(
		CommandNames.SHOW_NEW_HIST));
		histogram.add(histogramNew);

		final JMenuItem histogramZero=new JMenuItem(commands.getAction(
		CommandNames.SHOW_HIST_ZERO));
		histogram.add(histogramZero);
		
		histogram.add(commands.getAction(
		CommandNames.DELETE_HISTOGRAM)).setAccelerator(KeyStroke.getKeyStroke(
		KeyEvent.VK_D,CTRL_MASK));
		histogram.add(calHist);
		
		final JMenuItem calibFit = new JMenuItem("Fit\u2026");
		calibFit.setActionCommand("calfitlin");
		calibFit.addActionListener(jamCommand);
		calHist.add(calibFit);
		final JMenuItem calibFunc = new JMenuItem("Enter Coefficients\u2026");
		calibFunc.setActionCommand("caldisp");
		calibFunc.addActionListener(jamCommand);
		calHist.add(calibFunc);
		projectHistogram.setActionCommand("project");
		projectHistogram.addActionListener(jamCommand);
		histogram.add(projectHistogram);
		manipHistogram.setActionCommand("manipulate");
		manipHistogram.addActionListener(jamCommand);
		histogram.add(manipHistogram);
		gainShift.setActionCommand("gainshift");
		gainShift.addActionListener(jamCommand);
		histogram.add(gainShift);
		return histogram;
	}
	
	private JMenu getPreferencesMenu(JamCommand jamCommand){
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
		final SortMode mode=status.getSortMode();
		final boolean online = mode == SortMode.ONLINE_DISK || 
		mode == SortMode.ONLINE_NO_DISK;
		final boolean offline = mode == SortMode.OFFLINE;
		final boolean sorting = online || offline;
		final boolean file = mode==SortMode.FILE || mode==SortMode.NO_SORT;
		cstartacq.setEnabled(online);
		cstopacq.setEnabled(online);
		iflushacq.setEnabled(online);
		runacq.setEnabled(online);
		sortacq.setEnabled(offline);
		paramAction.setEnabled(sorting);
		statusacq.setEnabled(sorting);
		impHist.setEnabled(file);
	}

	void setRunState(RunState rs) {
		final boolean acqmode = rs.isAcquireMode();
		final boolean acqon = rs.isAcqOn();
		iflushacq.setEnabled(acqon);
		cstartacq.setSelected(acqon);
		cstopacq.setSelected(acqmode && (!acqon));
	}
	
	void adjustHistogramItems(Histogram h){
		final boolean hExists = h!= null;
		final boolean oneDops = hExists && h.getDimensionality()==1;
		zeroHistogram.setEnabled(hExists);
		calHist.setEnabled(oneDops);
	}

	public void update(Observable observe, Object obj){
		final BroadcastEvent be=(BroadcastEvent)obj;
		final int command=be.getCommand();
		if (command==BroadcastEvent.SORT_MODE_CHANGED){
			sortModeChanged();
		}
	}
}