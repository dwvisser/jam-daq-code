package jam;
import jam.data.DataBase;
import jam.data.control.CalibrationDisplay;
import jam.data.control.CalibrationFit;
import jam.data.control.DataControl;
import jam.data.control.GainShift;
import jam.data.control.GateControl;
import jam.data.control.HistogramControl;
import jam.data.control.Manipulations;
import jam.data.control.MonitorControl;
import jam.data.control.ParameterControl;
import jam.data.control.Projections;
import jam.data.control.ScalerControl;
import jam.global.*;
import jam.io.HistogramIO;
import jam.io.ImpExpException;
import jam.io.hdf.HDFIO;
import jam.plot.Display;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import javax.swing.AbstractButton;

/**
 * This class recieves the commands for all the pull
 * down menues of JamMain.
 * It then calls the necessary methods.
 * It is implemented by <code>Jam</code>.
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
	private final Display display;
	private final JamConsole console;

	private final Broadcaster broadcaster;

	/* classes for reading and writing histograms */
	private final HistogramIO histio;
	private final HDFIO hdfio;
	private final jam.io.BatchExport batchexport;

	/* control classes */
	private RunControl runControl;
	private SortControl sortControl;
	private DisplayCounters displayCounters;
	private final HistogramControl histogramControl;
	private final GateControl gateControl;
	private final ScalerControl scalerControl;
	private final MonitorControl monitorControl;
	private final ParameterControl paramControl;
	private final CalibrationDisplay calibDisplay;
	private final CalibrationFit calibFit;
	private final Projections projection;
	private final Manipulations manipulate;
	private final GainShift gainshift;

	private final PeakFindDialog peakFindDialog;

	//class to setup acquistion and sorting
	private final SetupSortOn setupSortOn;
	private final SetupSortOff setupSortOff;
	private final SetupRemote setupRemote;

	private final FrontEndCommunication frontEnd;
	private final Help help;

	private final JamStatus status;

	private RemoteAccess remoteAccess=null;
	private boolean remote=false;

	/** Constructor for this class.
	 * @param jm The launch class for the Jam application
	 * @param d the area where histograms are displayed
	 * @param b the object which handles application-wide 
	 * communications
	 * @param jc the text input and output area
	 */
	public JamCommand(JamMain jm, Display d, Broadcaster b, 
	JamConsole jc) {
		super();
		classname = getClass().getName() + " - ";
		this.jamMain = jm;
		this.display = d;
		this.broadcaster = b;
		this.console = jc;
		status = JamStatus.instance();
		/* class to hold run information */
		new RunInfo();
		/* io classes */
		hdfio = new HDFIO(jamMain, console);
		histio = new HistogramIO(jamMain, console);
		batchexport = new jam.io.BatchExport(jamMain, console);
		/* communication */
		frontEnd = new VMECommunication(jamMain, this, broadcaster, console);
		/* data bases manipulation */
		histogramControl = new HistogramControl(jamMain, broadcaster, console);
		gateControl = new GateControl(jamMain, broadcaster, console, display);
		scalerControl = new ScalerControl(jamMain, broadcaster, console);
		monitorControl = new MonitorControl(jamMain, broadcaster, console);
		paramControl = new ParameterControl(jamMain, broadcaster, console);
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
				scalerControl,
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
		console.setCommandListener(display);
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
		broadcaster.addObserver(scalerControl);
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
			} else if ("newclear".equals(incommand)) {
				jamMain.setSortMode(JamMain.NO_SORT);
				DataBase.getInstance().clearAllLists();
				dataChanged();
			} else if ("open".equals(incommand)) {
				if (histio.readJHFFile()) {
					jamMain.setSortModeFile(histio.getFileNameOpen());
					DataControl.setupAll(); //setup all data bases
					dataChanged();
					jamMain.repaint();
				}
			} else if ("openhdf".equals(incommand)) {
				if (hdfio.readFile(HDFIO.OPEN)) { //true if successful
					jamMain.setSortModeFile(hdfio.getFileNameOpen());
					DataControl.setupAll();
					dataChanged();
					jamMain.repaint();
				}
			} else if ("reloadhdf".equals(incommand)) {
				if (hdfio.readFile(HDFIO.RELOAD)) { //true if successful
					scalerControl.displayScalers();
				}
			} else if ("reload".equals(incommand)) {
				if (histio.reloadJHFFile()) {
					scalerControl.displayScalers();
				}
			} else if ("save".equals(incommand)) {
				histio.writeJHFFile();
			} else if ("savehdf".equals(incommand)) {
				hdfio.writeFile(hdfio.lastValidFile());
			} else if ("saveas".equals(incommand)) {
				histio.writeJHFFile();
			} else if ("saveAsHDF".equals(incommand)) {
				hdfio.writeFile();
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
			} else if ("parameters".equals(incommand)) {
				paramControl.show();
			} else if ("status".equals(incommand)) {
				displayCounters.show();
			} else if ("newhist".equals(incommand)) {
				histogramControl.showNew();
			} else if ("zerohist".equals(incommand)) {
				histogramControl.showZero();
			} else if ("project".equals(incommand)) {
				projection.show();
			} else if ("manipulate".equals(incommand)) {
				manipulate.show();
			} else if ("gainshift".equals(incommand)) {
				gainshift.show();
			} else if ("caldisp".equals(incommand)) {
				calibDisplay.show();
			} else if ("calfitlin".equals(incommand)) {
				calibFit.showLinFit();
			} else if ("gatenew".equals(incommand)) {
				gateControl.showNew();
			} else if ("gateadd".equals(incommand)) {
				gateControl.showAdd();
			} else if ("gateset".equals(incommand)) {
				gateControl.showSet();
			} else if ("displayscalers".equals(incommand)) {
				scalerControl.showDisplay();
			} else if ("displaymonitors".equals(incommand)) {
				monitorControl.showDisplay();
			} else if ("configmonitors".equals(incommand)) {
				monitorControl.showConfig();
			} else if ("zeroscalers".equals(incommand)) {
				scalerControl.showZero();
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
			} else {
				console.errorOutln(
					getClass().getName()
						+ ": Error unrecognized command \""
						+ incommand
						+ "\"");
			}
		} catch (JamException exc) {
			console.errorOutln("JamException: " + exc.getMessage());
		} catch (ImpExpException iee) {
			console.errorOutln("ImpExpException: " + iee.getMessage());
		} catch (GlobalException ge) {
			console.errorOutln(
				getClass().getName()
					+ ".actionPerformed(\""
					+ incommand
					+ "\"): "
					+ "GlobalException with message \""
					+ ge.getMessage()
					+ "\"");
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
	
	void dataChanged() throws GlobalException {
		broadcaster.broadcast(BroadcastEvent.HISTOGRAM_ADD);
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
