package jam;
import jam.commands.CommandException;
import jam.commands.CommandManager;
import jam.data.control.CalibrationDisplay;
import jam.data.control.CalibrationFit;
import jam.data.control.GainShift;
import jam.data.control.GateControl;
import jam.data.control.HistogramControl;
import jam.data.control.Manipulations;
import jam.data.control.MonitorControl;
import jam.data.control.ParameterControl;
import jam.data.control.Projections;
import jam.global.Broadcaster;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.global.RunInfo;
import jam.io.hdf.HDFIO;
import jam.plot.Display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;

import javax.swing.AbstractButton;
import javax.swing.JFrame;

/**
 * This class recieves the commands for many of the pull
 * down menus of JamMain.
 * It then calls the necessary methods.
 *
 * @version  0.5 April 98
 * @author   Ken Swartz
 * @see         jam.JamMain
 * @since       JDK1.1
 */

public class JamCommand
	extends MouseAdapter
	implements ActionListener, ItemListener {
		
	static final int MESSAGE_SCALER = 1; //alert scaler class
	private final String classname;

	private final JamMain jamMain;
	private final JFrame frame;
	private final Display display;
	private final JamConsole console;

	private final Broadcaster broadcaster=Broadcaster.getSingletonInstance();

	/* classes for reading and writing histograms */
	private final HDFIO hdfio;
	private final jam.io.BatchExport batchexport;

	/* control classes */
	private RunControl runControl;
	private SortControl sortControl;
	private DisplayCounters displayCounters;
	private final HistogramControl histogramControl;
	private final GateControl gateControl;
	private final MonitorControl monitorControl;
	private final ParameterControl paramControl;
	private final CalibrationDisplay calibDisplay;
	private final CalibrationFit calibFit;
	private final Projections projection;
	private final Manipulations manipulate;
	private final GainShift gainshift;

	private final PeakFindDialog peakFindDialog;

	/* classes to set up acquisition and sorting */
	private final SetupSortOn setupSortOn;
	private final SetupSortOff setupSortOff;
	private final SetupRemote setupRemote;

	private final FrontEndCommunication frontEnd;
	private final Help help;

	private final JamStatus status;

	private CommandManager jamCmdMgr;
	private RemoteAccess remoteAccess=null;
	private boolean remote=false;

	/** Constructor for this class.
	 * @param jm The launch class for the Jam application
	 * @param d the area where histograms are displayed
	 * @param jc the text input and output area
	 */
	public JamCommand(JamMain jm, Display d, JamConsole jc) {
		super();
		classname = getClass().getName() + " - ";
		this.jamMain = jm;
		this.frame =(JFrame)jamMain;
		this.display = d;
		this.console = jc;
		status = JamStatus.instance();
		/* class to hold run information */
		new RunInfo();
		/* io classes */
		hdfio = new HDFIO(jamMain, console);
		batchexport = new jam.io.BatchExport(jamMain, console);
		/* communication */
		frontEnd = new VMECommunication(jamMain, this, broadcaster, console);
		/* data bases manipulation */
		histogramControl = new HistogramControl(frame, console);
		gateControl = new GateControl(jamMain, broadcaster, console);
		monitorControl = new MonitorControl(jamMain, broadcaster, console);
		paramControl = new ParameterControl(jamMain, console);
		calibDisplay = new CalibrationDisplay(jamMain, broadcaster, console);
		calibFit = new CalibrationFit(jamMain, broadcaster, console);
		projection = new Projections(jamMain, broadcaster, console);
		manipulate = new Manipulations(jamMain, broadcaster, console);
		gainshift = new GainShift(jamMain, broadcaster, console);
		/* acquisition control */
		runControl =
			new RunControl(
				jamMain,
				histogramControl,
				(VMECommunication) frontEnd,
				hdfio,
				console);
		sortControl = new SortControl(jamMain, console);
		displayCounters = new DisplayCounters(jamMain, broadcaster, console);
		/* setup classes */
		setupSortOn =
			new SetupSortOn( 
				jamMain,
				runControl,
				displayCounters,
				frontEnd,
				console,broadcaster);
		setupSortOff =
			new SetupSortOff(
				jamMain,
				sortControl,
				displayCounters,
				broadcaster,
				console);
		setupRemote = new SetupRemote(jamMain, console);
		help = new Help(jamMain, console);//Help window
		peakFindDialog = new PeakFindDialog(jamMain, display, console);
		addObservers();
		
		jamCmdMgr = CommandManager.getInstance();
		jamCmdMgr.setMessageHandler(console); 
		console.addCommandListener(jamCmdMgr);
		console.addCommandListener(display);
	}
	
	
	/**
	 * Add observers to the list of classes to be notified of 
	 * broadcasted events.
	 */
	private final void addObservers(){
		broadcaster.addObserver(displayCounters);
		broadcaster.addObserver(display);
		broadcaster.addObserver(frontEnd);
		broadcaster.addObserver(gateControl);
		broadcaster.addObserver(manipulate);
		broadcaster.addObserver(projection);
		broadcaster.addObserver(gainshift);
	}
	
	/**
	 * Receives all the inputs from the pull down menus
	 * that are <code>ActionEvent</code>'s.
	 * Receives input from VME and dispatches messages.
	 *
	 * @param  e    Action event from pull down menus
	 * @since Version 0.5
	 */
	public void actionPerformed(ActionEvent e) {
		final String incommand = e.getActionCommand();
		try {
			if ("Black Background".equals(incommand)) {
				display.setPreference(
					Display.Preferences.BLACK_BACKGROUND,
					true);
			} else if ("White Background".equals(incommand)) {
				display.setPreference(
					Display.Preferences.WHITE_BACKGROUND,
					true);
			} else if ("batchexport".equals(incommand)) {
				batchexport.show();
			} else if ("online".equals(incommand)) {
				setupSortOn.show();
			} else if ("offline".equals(incommand)) {
				setupSortOff.show();
			} else if ("remote".equals(incommand)) {
				setupRemote.showRemote();
			} else if ("flush".equals(incommand)) {
				runControl.flushAcq();
			} else if ("run".equals(incommand)) {
				runControl.show();
			} else if ("sort".equals(incommand)) {
				sortControl.show();
			} else if ("status".equals(incommand)) {
				displayCounters.show();
//Remove KBS				
//			} else if ("zerohist".equals(incommand)) {
//				histogramControl.showZero();
			} else if ("project".equals(incommand)) {
				projection.show();
			} else if ("manipulate".equals(incommand)) {
				manipulate.show();
			} else if ("gainshift".equals(incommand)) {
				gainshift.show();
			} else if ("caldisp".equals(incommand)) {
				calibDisplay.show();
			} else if ("calfitlin".equals(incommand)) {
				calibFit.show();
			} else if ("gatenew".equals(incommand)) {
				gateControl.showNew();
			} else if ("gateadd".equals(incommand)) {
				gateControl.showAdd();
			} else if ("gateset".equals(incommand)) {
				gateControl.showSet();
			} else if ("displaymonitors".equals(incommand)) {
				monitorControl.showDisplay();
			} else if ("configmonitors".equals(incommand)) {
				monitorControl.showConfig();
			} else if ("about".equals(incommand)) {
				help.showAbout();
			} else if ("license".equals(incommand)) {
				help.showLicense();
			} else if ("peakfind".equals(incommand)) {
				peakFindDialog.show();
			} else if ("start".equals(incommand)) {
				if (status.isOnLine()) {
					runControl.startAcq();
				} else {
					console.errorOutln(
						"Error: start command given when not in Online mode.");
				}
			} else if ("stop".equals(incommand)) {
				if (status.isOnLine()) {
					runControl.stopAcq();
				} else {
					console.errorOutln(
						"Error: stop command given when not in Online mode.");
				}
			} else {//See if it a command class
				try {
					if(!jamCmdMgr.performCommand(incommand, null)) {
					console.errorOutln(getClass().getName()
						+ ": Error unrecognized command \""+ incommand+ "\"");
					}
				} catch (CommandException ce) {
					console.errorOutln(getClass().getName()
							+ ": Error performing command \""+ incommand + "\"");
				}						
			}
		} catch (JamException exc) {
			console.errorOutln("JamException: " + exc.getMessage());
		} 
	}
	
	/** 
	 * Recieves the inputs from the pull down menus that are selectable 
	 * checkboxes.
	 *
	 * @param ie event triggered by selecting an item in the menus
	 */
	public void itemStateChanged(ItemEvent ie) {
		final AbstractButton item = (AbstractButton) ie.getItem();
		if ((item == null)) { //catch error
			console.errorOutln("The selected item is null.");
		} else {
			final String text = item.getText();
			final boolean selected = item.isSelected();
			if ("Ignore zero channel on autoscale".equals(text)) {
				display.setPreference(
					Display.Preferences.AUTO_IGNORE_ZERO,
					selected);
			} else if ("Ignore max channel on autoscale".equals(text)) {
				display.setPreference(
					Display.Preferences.AUTO_IGNORE_FULL,
					selected);
			} else if ("Verbose front end".equals(text)) {
				frontEnd.verbose(selected);
				JamProperties.setProperty(
					JamProperties.FRONTEND_VERBOSE,
					selected);
			} else if ("Debug front end".equals(text)) {
				frontEnd.debug(selected);
				JamProperties.setProperty(
					JamProperties.FRONTEND_DEBUG,
					selected);
			} else if ("Autoscale on Expand/Zoom".equals(text)) {
				display.setAutoOnExpand(selected);
			} else if ("Automatic peak find".equals(text)) {
				display.setPreference(
					Display.Preferences.AUTO_PEAK_FIND,
					selected);
			}
		}
	}


	/** 
	 * Sets whether or not we are in remote mode; remote mode not yet
	 * implemented.
	 *
	 * @param on true if we are in remote mode
	 * @param ra remote accessor of this process
	 */
	public void setRemote(boolean on, RemoteAccess ra) {
		synchronized (this) {
			remoteAccess = ra;
			remote = on;
		}
	}
	
	HDFIO getHDFIO(){
		return hdfio;
	}
	
	HistogramControl getHistogramControl(){
		return histogramControl;
	}
	
	SetupSortOff getSetupSortOff(){
		return setupSortOff;
	}
	
	SortControl getSortControl(){
		return sortControl;
	}
}
