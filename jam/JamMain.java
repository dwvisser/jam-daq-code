/*
 * Copyright statement
 */
package jam;
import jam.data.DataException;
import jam.data.Histogram;
import jam.data.control.DataControl;
import jam.global.AcquisitionStatus;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.GlobalException;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.plot.Display;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;
import javax.swing.border.*;
import javax.help.*;
import java.net.*;

/**
 *
 * Main Class for Jam.
 * This class makes the pull down menu and
 *
 * It is implemented by <code>Jam</code>.
 *
 * @version  0.5 April 98
 * @author   Ken Swartz
 * @since    JDK1.1
 */
public class JamMain extends JFrame implements AcquisitionStatus, Observer {

	/**
	 * Sort Mode--No sort file loaded.
	 */
	static public final int NO_SORT = 0;

	/**
	 * Sort Mode--Set to sort online data to disk.
	 */
	static public final int ONLINE_DISK = 1;

	/**
	 * Sort Mode--Set to sort online data to tape.
	 */
	static public final int ONLINE_TAPE = 2;

	/**
	 * Sort Mode--Set to sort offline data from disk.
	 */
	static public final int OFFLINE_DISK = 3;

	/**
	 * Sort Mode--Set to sort offline data from tape.
	 */
	static public final int OFFLINE_TAPE = 4;

	/**
	 * Sort Mode--Acting as a client to a remote Jam process.
	 */
	static public final int REMOTE = 5;

	/**
	 * Sort Mode--Just read in a data file.
	 */
	static public final int FILE = 6; //we have read in a file

	/**
	 * Run State--Acquisition not currently allowed.
	 */
	public final static int NO_ACQ = 0;

	/**
	 * Run State--Not currently acuiring data.
	 */
	public final static int ACQ_OFF = 1;

	/**
	 * Run State--Currently acuiring data.
	 */
	public final static int ACQ_ON = 2;

	/**
	 * Run State--Not currently taking run data
	 */
	public final static int RUN_OFF = 3;

	/**
	 * Run State--Currently acuiring run data.
	 */
	public final static int RUN_ON = 4;

	static final String NO_FILL_MENU_TEXT = "Disable 2d Gate Fill";
	
	/**
	 * Configuration information for Jam.
	 */
	private final JamProperties jamProperties;

	/**
	 * Overall status of Jam.
	 */
	private final JamStatus status;

	/**
	 * Event distributor.
	 */
	private final Broadcaster broadcaster;

	/**
	 * Histogram displayer.
	 */
	private final Display display;

	/**
	 * Menu command handler.
	 */
	private final JamCommand jamCommand;

	/**
	 * Message output and text input.
	 */
	private final JamConsole console;

	//menu items for File menu
	private JMenuItem newClear, open, openhdf, reload, reloadhdf, save;
	private JMenuItem saveHDF;
	private JMenu impHist;

	//menu items for Control menu
	private JCheckBoxMenuItem cstartacq, cstopacq;
	private JMenuItem runacq, sortacq, statusacq, paramacq, iflushacq;

	private JLabel lrunState; //run state label
	private JComboBox histogramChooser; //reference needed by command
	private JToggleButton boverLay; //button for overlay
	private JComboBox gateChooser; // reference needed by command

	private HistogramComboBoxModel hcbm;
	private GateComboBoxModel gcbm;

	//fit menu fields
	private JMenu fitting;
	private Hashtable fitterList;

	private final Container me;

	/**
	 * Sort mode
	 * ONLINE or OFFLINE
	 */
	private int sortMode;

	/**
	 * Run state can be ACQ_ON, ACQ_OFF ....
	 */
	private int runState;

	/**
	 * Name of file if used file|open to read a file
	 */
	private String openFileName;

	/**
	 * Construtor
	 * create Jam window
	 * console is used to output log to the user.
	 *
	 */
	private JamMain() {
		super("Jam");
		new SplashWindow(this, 10000);
		me = this.getContentPane();
		jamProperties = new JamProperties(); //class that has properties
		jamProperties.loadProperties(); //load properties from file
		status = JamStatus.instance(); //class that is statically available
		status.setAcqisitionStatus(this);
		broadcaster = new Broadcaster();
		//class to distrute events to all listeners
		//Frame layout and add element to frame
		this.setSize(700, 700);
		this.setLocation(50, 50);
		this.setResizable(true);
		this.setBackground(Color.lightGray);
		this.setForeground(Color.black);
		me.setLayout(new BorderLayout());
		//ouput console (need by jamCommand)
		console = new JamConsole();
		me.add(console, BorderLayout.SOUTH);
		//histogram displayer (needed by jamCommand)
		display = new Display(broadcaster, console);
		me.add(display, BorderLayout.CENTER);
		//create user command listener
		jamCommand = new JamCommand(this, display, broadcaster, console);
		//setup menu (needs jamCommand)
		this.setJMenuBar(setupMenu());
		//add toolbar (needs jamCommand as item, action listener)
		Component pselect=addToolbarSelect();
		me.add(pselect, BorderLayout.NORTH);
		// tool bar display (on left side);
		display.addToolbarAction();
		//list of loaded fit routines
		fitterList = new Hashtable(5);
		//operations to close window
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				showExitDialog();
			}
			public void windowClosed(WindowEvent e) {
				showExitDialog();
			}
		});
		try { //create first histogams
			new InitialHistograms();
		} catch (DataException de) {
			console.errorOutln(de.getMessage());
		}
		setGateModel();
		/* setup all other dialog boxes.
		   data control, gate set, histogram manipulate, project */
		DataControl.setupAll();
		try { //setting no sort does not throw an exception
			setSortMode(NO_SORT);
		} catch (JamException je) {
			System.err.println("We should not be here, [JamMain]");
		}
		/* The pack() call and everything after it here should be executed in the 
		 * event dispatch thread. */
		final Runnable showWindow=new Runnable(){
			public void run(){ 
				pack();
				setChoosersToFirstItems();
				show();
				/* print out where config files were read from */
				jamProperties.setMessageHandler(console);
				jamProperties.outputMessages(console);
			}
		};
		SwingUtilities.invokeLater(showWindow);
	}

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
	private JMenuBar setupMenu() {
		final JMenuBar menubar = new JMenuBar();
		//file menu
		final JMenu file = new JMenu("File");
		menubar.add(file);
		newClear = new JMenuItem("New");
		newClear.setActionCommand("newclear");
		newClear.addActionListener(jamCommand);
		file.add(newClear);
		openhdf = new JMenuItem("Open(hdf)...");
		openhdf.setActionCommand("openhdf");
		openhdf.addActionListener(jamCommand);
		file.add(openhdf);
		reloadhdf = new JMenuItem("Reload(hdf)...");
		reloadhdf.setActionCommand("reloadhdf");
		reloadhdf.addActionListener(jamCommand);
		reloadhdf.setEnabled(false);
		file.add(reloadhdf);
		saveHDF = new JMenuItem("Save(hdf)");
		saveHDF.setActionCommand("savehdf");
		saveHDF.setEnabled(false);
		saveHDF.addActionListener(jamCommand);
		saveHDF.setEnabled(false);
		file.add(saveHDF);
		final JMenuItem saveAsHDF = new JMenuItem("Save as (hdf)...");
		saveAsHDF.setActionCommand("saveAsHDF");
		saveAsHDF.addActionListener(jamCommand);
		file.add(saveAsHDF);
		file.addSeparator();
		impHist = new JMenu("Import");
		file.add(impHist);
		final JMenuItem openascii = new JMenuItem("Import ASCII...");
		openascii.setActionCommand("openascii");
		openascii.addActionListener(jamCommand);
		impHist.add(openascii);
		final JMenuItem openspe = new JMenuItem("Import RADWARE .spe ...");
		openspe.setActionCommand("openspe");
		openspe.addActionListener(jamCommand);
		impHist.add(openspe);
		final JMenuItem openornl = new JMenuItem("Import ORNL .drr .his ...");
		openornl.setActionCommand("openornl");
		openornl.addActionListener(jamCommand);
		impHist.add(openornl);
		final JMenuItem openxsys = new JMenuItem("Import XSYS .dat ...");
		openxsys.setActionCommand("openxsys");
		openxsys.addActionListener(jamCommand);
		impHist.add(openxsys);
		final JMenu expHist = new JMenu("Export");
		file.add(expHist);
		final JMenuItem saveascii = new JMenuItem("Export ASCII...");
		saveascii.setActionCommand("saveascii");
		saveascii.addActionListener(jamCommand);
		expHist.add(saveascii);
		final JMenuItem savespe = new JMenuItem("Export RADWARE .spe ...");
		savespe.setActionCommand("savespe");
		savespe.addActionListener(jamCommand);
		expHist.add(savespe);
		final JMenuItem saveornl = new JMenuItem("Export ORNL .drr .his  ...");
		saveornl.setActionCommand("saveornl");
		saveornl.addActionListener(jamCommand);
		expHist.add(saveornl);
		final JMenuItem batchexport = new JMenuItem("Batch Export...");
		batchexport.setActionCommand("batchexport");
		batchexport.addActionListener(jamCommand);
		expHist.add(batchexport);
		file.addSeparator();
		final JMenu oldJHF = new JMenu("JHF Format");
		file.add(oldJHF);
		open = new JMenuItem("Open(jhf)...");
		open.setActionCommand("open");
		open.addActionListener(jamCommand);
		oldJHF.add(open);
		reload = new JMenuItem("Reload(jhf)...");
		reload.setActionCommand("reload");
		reload.addActionListener(jamCommand);
		reload.setEnabled(false);
		oldJHF.add(reload);
		save = new JMenuItem("Save(jhf)");
		save.setActionCommand("save");
		save.addActionListener(jamCommand);
		save.setEnabled(false);
		oldJHF.add(save);
		final JMenuItem saveas = new JMenuItem("Save as(jhf)...");
		saveas.setActionCommand("saveas");
		saveas.addActionListener(jamCommand);
		oldJHF.add(saveas);
		file.addSeparator();
		final JMenuItem print = new JMenuItem("Print...");
		print.setActionCommand("print");
		print.addActionListener(jamCommand);
		file.add(print);
		file.addSeparator();
		final JMenuItem printsetup = new JMenuItem("Print Setup...");
		printsetup.setActionCommand("printsetup");
		printsetup.addActionListener(jamCommand);
		file.add(printsetup);
		file.addSeparator();
		final JMenuItem exit = new JMenuItem("Exit...");
		exit.setActionCommand("exitShow");
		exit.addActionListener(new ActionListener() {
			public synchronized void actionPerformed(
				java.awt.event.ActionEvent e) {
				showExitDialog();
			}
		});
		file.add(exit);
		final JMenu setup = new JMenu("Setup");
		menubar.add(setup);
		final JMenuItem setupOnline = new JMenuItem("Online sorting...");
		setupOnline.setActionCommand("online");
		setupOnline.addActionListener(jamCommand);
		setup.add(setupOnline);
		setup.addSeparator();
		final JMenuItem setupOffline = new JMenuItem("Offline sorting...");
		setupOffline.setActionCommand("offline");
		setupOffline.addActionListener(jamCommand);
		setup.add(setupOffline);
		setup.addSeparator();
		final JMenuItem setupRemote = new JMenuItem("Remote Hookup...");
		setupRemote.setActionCommand("remote");
		setupRemote.addActionListener(jamCommand);
		setupRemote.setEnabled(false);
		setup.add(setupRemote);
		final JMenu mcontrol = new JMenu("Control");
		menubar.add(mcontrol);
		cstartacq = new JCheckBoxMenuItem("start", false);
		cstartacq.setEnabled(false);
		cstartacq.addActionListener(jamCommand);
		mcontrol.add(cstartacq);
		cstopacq = new JCheckBoxMenuItem("stop", true);
		cstopacq.setEnabled(false);
		cstopacq.addActionListener(jamCommand);
		mcontrol.add(cstopacq);
		iflushacq = new JMenuItem("flush");
		iflushacq.setEnabled(false);
		iflushacq.addActionListener(jamCommand);
		mcontrol.add(iflushacq);
		mcontrol.addSeparator();
		runacq = new JMenuItem("Run...");
		runacq.setEnabled(false);
		runacq.setActionCommand("run");
		runacq.addActionListener(jamCommand);
		mcontrol.add(runacq);
		sortacq = new JMenuItem("Sort...");
		sortacq.setEnabled(false);
		sortacq.setActionCommand("sort");
		sortacq.addActionListener(jamCommand);
		mcontrol.add(sortacq);
		paramacq = new JMenuItem("Parameters...");
		paramacq.setEnabled(false);
		paramacq.setActionCommand("parameters");
		paramacq.addActionListener(jamCommand);
		mcontrol.add(paramacq);
		statusacq = new JMenuItem("Buffer Count...");
		statusacq.setEnabled(false);
		statusacq.setActionCommand("status");
		statusacq.addActionListener(jamCommand);
		mcontrol.add(statusacq);
		final JMenu histogram = new JMenu("Histogram");
		menubar.add(histogram);
		final JMenuItem histogramNew = new JMenuItem("New...");
		histogramNew.setActionCommand("newhist");
		histogramNew.addActionListener(jamCommand);
		histogram.add(histogramNew);
		final JMenuItem zeroHistogram = new JMenuItem("Zero...");
		zeroHistogram.setActionCommand("zerohist");
		zeroHistogram.addActionListener(jamCommand);
		histogram.add(zeroHistogram);
		final JMenu calHist = new JMenu("Calibrate");
		histogram.add(calHist);
		final JMenuItem calibFit = new JMenuItem("Fit...");
		calibFit.setActionCommand("calfitlin");
		calibFit.addActionListener(jamCommand);
		calHist.add(calibFit);
		final JMenuItem calibFunc = new JMenuItem("Enter Coefficients...");
		calibFunc.setActionCommand("caldisp");
		calibFunc.addActionListener(jamCommand);
		calHist.add(calibFunc);
		final JMenuItem projectHistogram = new JMenuItem("Projections...");
		projectHistogram.setActionCommand("project");
		projectHistogram.addActionListener(jamCommand);
		histogram.add(projectHistogram);
		final JMenuItem manipHistogram = new JMenuItem("Combine...");
		manipHistogram.setActionCommand("manipulate");
		manipHistogram.addActionListener(jamCommand);
		histogram.add(manipHistogram);
		final JMenuItem gainShift = new JMenuItem("Gain Shift...");
		gainShift.setActionCommand("gainshift");
		gainShift.addActionListener(jamCommand);
		histogram.add(gainShift);
		final JMenu gate = new JMenu("Gate");
		menubar.add(gate);
		final JMenuItem gateNew = new JMenuItem("New Gate...");
		gateNew.setActionCommand("gatenew");
		gateNew.addActionListener(jamCommand);
		gate.add(gateNew);
		final JMenuItem gateAdd = new JMenuItem("Add Gate...");
		gateAdd.setActionCommand("gateadd");
		gateAdd.addActionListener(jamCommand);
		gate.add(gateAdd);
		final JMenuItem gateSet = new JMenuItem("Set Gate...");
		gateSet.setActionCommand("gateset");
		gateSet.addActionListener(jamCommand);
		gate.add(gateSet);
		final JMenu scalers = new JMenu("Scalers");
		menubar.add(scalers);
		final JMenuItem showScalers = new JMenuItem("Display Scalers...");
		showScalers.setActionCommand("displayscalers");
		showScalers.addActionListener(jamCommand);
		scalers.add(showScalers);
		final JMenuItem clearScalers = new JMenuItem("Zero Scalers...");
		scalers.add(clearScalers);
		clearScalers.setActionCommand("zeroscalers");
		clearScalers.addActionListener(jamCommand);
		scalers.addSeparator();
		final JMenuItem showMonitors = new JMenuItem("Display Monitors...");
		showMonitors.setActionCommand("displaymonitors");
		showMonitors.addActionListener(jamCommand);
		scalers.add(showMonitors);
		final JMenuItem configMonitors = new JMenuItem("Configure Monitors...");
		configMonitors.setActionCommand("configmonitors");
		configMonitors.addActionListener(jamCommand);
		scalers.add(configMonitors);
		final JMenu mPrefer = new JMenu("Preferences");
		menubar.add(mPrefer);
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
				JamProperties.getBooleanProperty(JamProperties.NO_FILL_2D));
		noFill2d.setEnabled(true);
		noFill2d.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent ie){
				JamProperties.setProperty(
				JamProperties.NO_FILL_2D,
				ie.getStateChange()==ItemEvent.SELECTED);
			}
		});
		mPrefer.add(noFill2d);
		final JCheckBoxMenuItem gradientColorScale = 
		new JCheckBoxMenuItem("Use gradient color scale",
		JamProperties.getBooleanProperty(JamProperties.GRADIENT_SCALE));
		gradientColorScale.setToolTipText(
		"Check to use a continuous gradient color scale on 2d histogram plots.");
		gradientColorScale.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent ie){
				final boolean state = ie.getStateChange()==ItemEvent.SELECTED;
				JamProperties.setProperty(JamProperties.GRADIENT_SCALE,state);
				display.setPreference(Display.Preferences.CONTINUOUS_2D_LOG,state);
			}
		});
		mPrefer.add(gradientColorScale);
		mPrefer.addSeparator();
		final JCheckBoxMenuItem autoPeakFind =
			new JCheckBoxMenuItem("Automatic peak find", true);
		autoPeakFind.setEnabled(true);
		autoPeakFind.addItemListener(jamCommand);
		mPrefer.add(autoPeakFind);
		final JMenuItem peakFindPrefs = new JMenuItem("Peak Find Properties...");
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
		final JMenu fitting = new JMenu("Fitting");
		menubar.add(fitting);
		final JMenuItem loadFit = new JMenuItem("Load Fit...");
		loadFit.setActionCommand("loadfit");
		loadFit.addActionListener(jamCommand);
		fitting.add(loadFit);
		fitting.addSeparator();
		final JMenu helpMenu = new JMenu("Help");
		menubar.add(helpMenu);
		final JMenuItem about = new JMenuItem("About...");
		helpMenu.add(about);
		about.setActionCommand("about");
		about.addActionListener(jamCommand);
		final JMenuItem userG = new JMenuItem("User Guide...");
		helpMenu.add(userG);
		userG.setActionCommand("userguide");
		userG.addActionListener(getUserGuideListener());
		final JMenuItem license = new JMenuItem("License...");
		helpMenu.add(license);
		license.setActionCommand("license");
		license.addActionListener(jamCommand);
		return menubar;
	}

	/**
	 * Adds the tool bar the at the top of the plot.
	 *
	 * @since Version 0.5
	 */
	private Component addToolbarSelect() {
		final DefaultComboBoxModel noGateComboBoxModel = new DefaultComboBoxModel();
		noGateComboBoxModel.addElement("NO GATES");

		/* panel with selection and print etc. */
		JPanel pselect = new JPanel(new BorderLayout());
		pselect.setBackground(Color.lightGray);
		pselect.setForeground(Color.black);
		//run status
		final JPanel pRunState = new JPanel(new GridLayout(1, 1));
		pRunState.setBorder(
			BorderFactory.createTitledBorder(
				new BevelBorder(BevelBorder.LOWERED),
				"Status",
				TitledBorder.CENTER,
				TitledBorder.TOP));
		lrunState = new JLabel("   Welcome   ", SwingConstants.CENTER);
		lrunState.setOpaque(true);
		lrunState.setForeground(Color.black);
		pRunState.add(lrunState);
		//histogram chooser
		final JPanel pCenter = new JPanel(new GridLayout(1, 0));
		hcbm=new HistogramComboBoxModel(jamCommand);
		histogramChooser = new JComboBox(hcbm);
		histogramChooser.setMaximumRowCount(30);
		histogramChooser.setSelectedIndex(0);
		histogramChooser.setToolTipText(
			"Click to choose histogram to display.");
		histogramChooser.setActionCommand("selecthistogram");
		histogramChooser.addActionListener(jamCommand);
		pCenter.add(histogramChooser);
		//overlay button
		boverLay = new JToggleButton("Overlay");
		boverLay.setActionCommand("overlay");
		boverLay.setToolTipText("Click to overlay next histogram chosen.");
		boverLay.addActionListener(jamCommand);
		pCenter.add(boverLay);
		gateChooser=new JComboBox(noGateComboBoxModel);
		gateChooser.setToolTipText("Click to choose gate to display.");
		gateChooser.setActionCommand("selectgate");
		gateChooser.addActionListener(jamCommand);
		pCenter.add(gateChooser);
		pselect.add(pRunState, BorderLayout.WEST);
		pselect.add(pCenter, BorderLayout.CENTER);
		return pselect;
	}

	private void showExitDialog() {
		final int rval =
			JOptionPane.showConfirmDialog(
				this,
				"Are you sure you want to exit?",
				"Exit Jam Confirmation",
				JOptionPane.YES_NO_OPTION);
		if (rval == JOptionPane.YES_OPTION) {
			System.exit(0);
		} else {
			this.setVisible(true);
		}
	}

	/**
	 * Implementation of Observable interface.
	 * 
	 * @param observable the sender
	 * @param o the message
	 */
	public void update(Observable observable, Object o) {
		try {
			final BroadcastEvent be = (BroadcastEvent) o;
			if (be.getCommand() == BroadcastEvent.HISTOGRAM_NEW) {
				final String lastHistName = status.getCurrentHistogramName();
				jamCommand.selectHistogram(
					Histogram.getHistogram(lastHistName));
			} else if (be.getCommand() == BroadcastEvent.HISTOGRAM_ADD) {
				dataChanged();
			} else if (be.getCommand() == BroadcastEvent.GATE_ADD) {
				final String lastHistName = status.getCurrentHistogramName();
				jamCommand.selectHistogram(
					Histogram.getHistogram(lastHistName));
				gatesChanged();
			}
		} catch (GlobalException ge) {
			console.errorOutln(getClass().getName() + ".update(): " + ge);
		}
	}

	/**
	 * Should be called whenever the lists of gates and histograms 
	 * change. It calls histogramsChanged() and gatesChanged(), 
	 * each of which add to the event stack, so that histograms will 
	 * be guaranteed (?) updated before gates get updated.
	 */
	void dataChanged() {
		histogramsChanged();
		gatesChanged();
	}

	void histogramsChanged() {
		histogramChooser.setSelectedIndex(0);
		histogramChooser.repaint();
	}

	void gatesChanged() {
		gateChooser.setSelectedIndex(0);
		gateChooser.repaint();
	}
	
	void setOverlayEnabled(boolean state){
		this.boverLay.setEnabled(state);
	}

	/**
	 * Sets the gate chooser to the current model of gates
	 */
	private void setGateModel() {
		gcbm = new GateComboBoxModel(jamCommand);
		gateChooser.setModel(gcbm);
	}

	/**
	 * Determines the mode for sorting data
	 * Enables and disables JMenu items as appropriate.
	 * Gives the window a title for the sort Mode.
	 *
	 * @exception   JamException    sends a message to the console if 
	 * there is an inappropriate call
	 * @see #ONLINE_DISK
	 * @see #ONLINE_TAPE
	 * @see #OFFLINE_DISK
	 * @see #OFFLINE_TAPE
	 * @see #FILE
	 * @see #REMOTE
	 * @see #NO_SORT
	 * @param mode the new mode for Jam to be in
	 */
	public void setSortMode(int mode) throws JamException {
		if (!((mode == NO_SORT) || (mode == FILE))) {
			if (sortMode == ONLINE_DISK || sortMode == ONLINE_TAPE) {
				throw new JamException(" Can't setup, setup is locked for online");
			} else if (sortMode == OFFLINE_DISK || sortMode == OFFLINE_TAPE) {
				throw new JamException(" Can't setup, setup is locked for offline");
			} else if (sortMode == REMOTE) {
				throw new JamException(" Can't setup, setup is locked for remote");
			}
		}
		sortMode = mode;
		//online sort
		if (mode == ONLINE_DISK || mode == ONLINE_TAPE) {
			cstartacq.setEnabled(true); //enable control JMenu items
			cstopacq.setEnabled(true);
			iflushacq.setEnabled(true);
			runacq.setEnabled(true);
			sortacq.setEnabled(false);
			paramacq.setEnabled(true);
			statusacq.setEnabled(true);
			newClear.setEnabled(false);
			open.setEnabled(false);
			save.setEnabled(false);
			reload.setEnabled(true);
			openhdf.setEnabled(false);
			saveHDF.setEnabled(false);
			reloadhdf.setEnabled(true);
			//impHist.setEnabled(false);
			setRunState(ACQ_OFF);
			if (mode == ONLINE_DISK) {
				this.setTitle("Jam - Online Sorting TO disk");
			} else {
				this.setTitle("Jam - Online Sorting TO tape)");
			}
			//offline sort
		} else if (mode == OFFLINE_DISK || mode == OFFLINE_TAPE) {
			cstartacq.setEnabled(false);
			cstopacq.setEnabled(false);
			iflushacq.setEnabled(false);
			runacq.setEnabled(false);
			sortacq.setEnabled(true);
			paramacq.setEnabled(true);
			statusacq.setEnabled(true);
			open.setEnabled(false);
			save.setEnabled(false);
			reload.setEnabled(true);
			openhdf.setEnabled(false);
			saveHDF.setEnabled(false);
			reloadhdf.setEnabled(true);
			newClear.setEnabled(false);
			//impHist.setEnabled(false);
			setRunState(ACQ_OFF);
			if (mode == OFFLINE_DISK) {
				this.setTitle("Jam - Offline Sorting FROM disk");
			} else {
				this.setTitle("Jam - Offline Sorting FROM tape");
			}
		} else if (mode == REMOTE) { //remote display
			cstartacq.setEnabled(false);
			cstopacq.setEnabled(false);
			iflushacq.setEnabled(false);
			runacq.setEnabled(false);
			sortacq.setEnabled(false);
			paramacq.setEnabled(false);
			statusacq.setEnabled(false);
			newClear.setEnabled(false);
			open.setEnabled(false);
			reload.setEnabled(false);
			openhdf.setEnabled(false);
			reloadhdf.setEnabled(false);
			impHist.setEnabled(false);
			setRunState(NO_ACQ);
			this.setTitle("Jam - Remote Mode");
			/* read in a file */
		} else if (mode == FILE) {
			cstartacq.setEnabled(false);
			cstopacq.setEnabled(false);
			iflushacq.setEnabled(false);
			runacq.setEnabled(false);
			sortacq.setEnabled(false);
			paramacq.setEnabled(false);
			statusacq.setEnabled(false);
			newClear.setEnabled(true);
			open.setEnabled(true);
			save.setEnabled(true);
			reload.setEnabled(false);
			openhdf.setEnabled(true);
			saveHDF.setEnabled(true);
			reloadhdf.setEnabled(false);
			impHist.setEnabled(true);
			setRunState(NO_ACQ);
			this.setTitle("Jam - " + openFileName);
		}
		if (mode == NO_SORT) {
			sortMode = mode;
			cstartacq.setEnabled(false);
			cstopacq.setEnabled(false);
			iflushacq.setEnabled(false);
			runacq.setEnabled(false);
			sortacq.setEnabled(false);
			paramacq.setEnabled(false);
			statusacq.setEnabled(false);
			newClear.setEnabled(true);
			open.setEnabled(true);
			reload.setEnabled(false);
			openhdf.setEnabled(true);
			reloadhdf.setEnabled(false);
			impHist.setEnabled(true);
			setRunState(NO_ACQ);
			this.setTitle("Jam - sorting not enabled");
		}
	}

	/**
	 * Set the jam to be in sort mode file and gives
	 * it the file Name.
	 *
	 * @exception   JamException    sends a message to the console if 
	 * there is a problem
	 * @param fileName the file to be sorted?
	 */
	public void setSortModeFile(String fileName) throws JamException {
		this.openFileName = fileName;
		setSortMode(FILE);
	}

	/**
	 * @return the current sort mode.
	 *
	 * @see #ONLINE_DISK
	 * @see #ONLINE_TAPE
	 * @see #OFFLINE_DISK
	 * @see #OFFLINE_TAPE
	 * @see #FILE
	 * @see #REMOTE
	 * @see #NO_ACQ
	 */
	public int getSortMode() {
		return sortMode;
	}
	
	/**
	 * @return true is the mode can be changed
	 */
	public boolean canSetSortMode() {
		return ((sortMode == NO_SORT) || (sortMode == FILE));
	}
	
	/**
	 * @return true if Jam is in online acquisition mode
	 */
	public boolean isOnLine() {
		return ((sortMode == ONLINE_TAPE) || (sortMode == ONLINE_DISK));
	}

	/**
	 *  <p>Sets run state when taking data online.
	 *  The run state mostly determints the state of control JMenu items.
	 *  This method uses imformation set by <code>setSortMode()</code>.
	 *  In addition:</p>
	 *  <ul>
	 *  <li>Control JMenu items are enabled and disabled as 
	 * appropriate.</li>
	 *  <li>Control JMenu items are states are set and unset as 
	 * appropriate.</li>
	 *  <li>The JMenu bar is to show online sort.</li>
	 *  <li>Updates display status label .</li>
	 * </ul>
	 *
	 * @param  rs    see the options for this just below
	 * @param  runNumber   serial number assigned the run in the run 
	 * control dialog box
	 * @see #NO_ACQ
	 * @see #ACQ_OFF
	 * @see #ACQ_ON
	 * @see #RUN_OFF
	 * @see #RUN_ON
	 */
	public void setRunState(int rs, int runNumber) {
		if (rs == NO_ACQ) {
			cstartacq.setEnabled(false); // enable start JMenu item
			cstopacq.setEnabled(false); // start stop flush
			iflushacq.setEnabled(false); // enable flush JMenu
			lrunState.setBackground(Color.lightGray);
			lrunState.setText("   Welcome   ");
		} else if (rs == ACQ_OFF) {
			cstartacq.setState(false); //check JMenu start active
			cstopacq.setState(true);
			iflushacq.setEnabled(false);
			lrunState.setBackground(Color.red);
			lrunState.setText("   Stopped   ");
		} else if (rs == ACQ_ON) {
			cstartacq.setState(true); //check JMenu stop active
			cstopacq.setState(false);
			iflushacq.setEnabled(true);
			lrunState.setBackground(Color.orange);
			lrunState.setText("   Started   ");
		} else if (rs == RUN_OFF) {
			cstartacq.setState(false); //check JMenu start active
			cstopacq.setState(true);
			iflushacq.setEnabled(false);
			lrunState.setBackground(Color.red);
			lrunState.setText("   Stopped   ");
		} else if (rs == RUN_ON) {
			cstartacq.setState(true); //check JMenu stop active
			cstopacq.setState(false);
			iflushacq.setEnabled(true);
			lrunState.setBackground(Color.green);
			lrunState.setText("   Run " + runNumber + "   ");
		}
		this.runState = rs;
	}

	/**
	 * Sets the run state with out the run number specified
	 * see <code> setRunState(int runState, int runNumber) </code>
	 *
	 * @see #getRunState()
	 * @param rs one of six possible modes
	 */
	public void setRunState(int rs) {
		setRunState(rs, 0);
	}

	/**
	 * Gets the current run state of Jam.
	 *
	 * @see #NO_SORT
	 * @see #ACQ_OFF
	 * @see #ACQ_ON
	 * @see #RUN_OFF
	 * @see #RUN_ON
	 * @see #NO_ACQ
	 * @return one of the six possible modes
	 */
	public int getRunState() {
		return runState;
	}

	/**
	 * @return true if Jam is currently taking data.
	 * either just acquistion or a run.
	 */
	public boolean isAcqOn() {
		return ((runState == ACQ_ON) || (runState == RUN_ON));
	}

	/**
	 * Add a fitting routine to the fitting JMenu
	 * give the name you want to add
	 *
	 * @param name the name of the fit routine added
	 */
	public void addFit(String name) {
		final JMenuItem fitItem = new JMenuItem(name + "...");
		fitItem.setActionCommand(name);
		fitItem.addActionListener(jamCommand.loadFit);
		fitting.add(fitItem);
	}

	/**
	 * @return whether histogram overlay mode is enabled
	 */
	public boolean overlaySelected() {
		return boverLay.isSelected();
	}

	/**
	 * De-select overlay mode.
	 */
	 public void deselectOverlay() {
		if (boverLay.isSelected()) {
			boverLay.doClick();
		}
	}
	
	/**
	 * @return a string representing the build version of Jam running
	 */
	static public String getVersion(){
		final char beta='\u03b2';
		final char alpha='\u03b1';
		final String JAM_VERSION = "1.4";
		final String VERSION_TYPE = "Release Candidate 2";
		String rval=JAM_VERSION;
		if (VERSION_TYPE.length()>0){
			rval += " ("+VERSION_TYPE+")";
		}
		return rval;
	}

	/**
	 * Selects first items in histogram and gate choosers.  Default 
	 * priveleges allows JamCommand to call this as well.
	 */
	private void setChoosersToFirstItems() {
		histogramChooser.setSelectedIndex(0);
		gateChooser.setSelectedIndex(0);
	}

	/**
	 * Return an ActionListener cabable of displaying the User
	 * Guide.
	 * 
	 * @return an ActionListener cabable of displaying the User
	 * Guide
	 */
	private ActionListener getUserGuideListener() {
		final HelpSet hs;
		final String helpsetName = "help/jam.hs";
		try {
			final URL hsURL = getClass().getClassLoader().getResource(helpsetName);
			hs = new HelpSet(null, hsURL);
		} catch (Exception ee) {
			System.out.println("HelpSet " + helpsetName + " not found");
			return null;
		}
		return new CSH.DisplayHelpFromSource(hs.createHelpBroker());
	}

	/**
	 * Main method that is run to start up full Jam process
	 * 
	 * @param args not used currently
	 */
	public static void main(String args[]) {
		System.out.println("Launching Jam v" + getVersion());
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println(e);
		}
		new JamMain();
	}
}
