package jam;
import jam.data.DataBase;
import jam.data.DataException;
import jam.data.Gate;
import jam.data.Histogram;
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
import jam.fit.LoadFit;
import jam.global.*;
import jam.io.HistogramIO;
import jam.io.ImpExpASCII;
import jam.io.ImpExpException;
import jam.io.ImpExpORNL;
import jam.io.ImpExpSPE;
import jam.io.ImpExpXSYS;
import jam.io.hdf.HDFIO;
import jam.plot.Display;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.PrintJob;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.print.PrinterJob;
import java.util.Properties;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;

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

	private JamMain jamMain;
	private Display display;
	private JamConsole console;

	private Broadcaster broadcaster;

	//classes for reading and writing histograms
	private HistogramIO histio;
	private HDFIO hdfio;
	private ImpExpASCII impExpASCII;
	private ImpExpORNL impExpORNL;
	private ImpExpSPE impExpSPE;
	private ImpExpXSYS impExpXSYS;
	private jam.io.BatchExport batchexport;

	//control classes
	private RunControl runControl;
	private SortControl sortControl;
	private DisplayCounters displayCounters;
	// fix this
	/** The "load fit" dialog box.
	 */
	private final LoadFit loadFit;

	//control of data objects
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
	private RemoteAccess remoteAccess;
	private final Help help;

	private boolean overlay;
	private boolean remote;

	private JamStatus status;

	/** Constructor for this class.
	 * @param jm The launch class for the Jam application
	 * @param d the area where histograms are displayed
	 * @param b the object which handles application-wide 
	 * communications
	 * @param jc the text input and output area
	 */
	public JamCommand(JamMain jm, Display d, Broadcaster b, JamConsole jc) {
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
		impExpASCII = new ImpExpASCII(jamMain, console);
		impExpSPE = new ImpExpSPE(jamMain, console);
		impExpXSYS = new ImpExpXSYS(jamMain, console);
		impExpORNL = new ImpExpORNL(jamMain, console);
		batchexport = new jam.io.BatchExport(jamMain, console);
		//communicates with camac crate
		frontEnd = new VMECommunication(jamMain, this, broadcaster, console);
		//data bases manipulation
		histogramControl = new HistogramControl(jamMain, broadcaster, console);
		gateControl = new GateControl(jamMain, broadcaster, console);
		scalerControl = new ScalerControl(jamMain, broadcaster, console);
		monitorControl = new MonitorControl(jamMain, broadcaster, console);
		paramControl = new ParameterControl(jamMain, broadcaster, console);
		calibDisplay = new CalibrationDisplay(jamMain, broadcaster, console);
		calibFit = new CalibrationFit(jamMain, broadcaster, console);
		projection = new Projections(jamMain, broadcaster, console);
		manipulate = new Manipulations(jamMain, broadcaster, console);
		gainshift = new GainShift(jamMain, broadcaster, console);
		//run and/or sort control
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
		//load fitting routine
		loadFit = new LoadFit(jamMain, display, console);
		//setup classes
		setupSortOn =
			new SetupSortOn(
				jamMain,
				runControl,
				displayCounters,
				frontEnd,
				console);
		setupSortOff =
			new SetupSortOff(
				jamMain,
				sortControl,
				displayCounters,
				broadcaster,
				console);
		setupRemote = new SetupRemote(jamMain, console);
		//Help window
		help = new Help(jamMain, console);
		peakFindDialog = new PeakFindDialog(jamMain, display, console);
		//add observers to the list of class to be notified of broadcasted events
		broadcaster.addObserver(jamMain);
		broadcaster.addObserver(displayCounters);
		broadcaster.addObserver(display);
		broadcaster.addObserver(frontEnd);
		broadcaster.addObserver(gateControl);
		broadcaster.addObserver(scalerControl);
		broadcaster.addObserver(manipulate);
		broadcaster.addObserver(projection);
		broadcaster.addObserver(gainshift);
		console.setCommandListener(display);
		overlay = false;
		/* remotely display histograms */
		remote = false;
	}
	
	ActionListener getLoadFit(){
		return loadFit;
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
		boolean selectEnabled = true; //is selection enabled
		try {
			if ("overlay".equals(incommand)) {
				synchronized(this){
					overlay = jamMain.overlaySelected();
				}
				if (overlay) {
					console.messageOut("Overlay Spectrum ", MessageHandler.NEW);
				}
			} else if ("selecthistogram".equals(incommand)) {
				if (selectEnabled) { //nested to avoid missing "selecthistogram"
					final Object item =
						((JComboBox) e.getSource()).getSelectedItem();
					if (item instanceof Histogram) {
						final Histogram h = (Histogram) item;
						selectHistogram(h);
					}
				}
			} else if ("selectgate".equals(incommand)) {
				final Object item =
					((JComboBox) e.getSource()).getSelectedItem();
				if (selectEnabled && item instanceof String) {
					final Gate gate = Gate.getGate((String) item);
					if (gate != null) {
						selectGate(gate);
					}
				}
			} else if ("Black Background".equals(incommand)) {
				display.setPreference(
					Display.Preferences.BLACK_BACKGROUND,
					true);
			} else if ("White Background".equals(incommand)) {
				display.setPreference(
					Display.Preferences.WHITE_BACKGROUND,
					true);
			} else if ("newclear".equals(incommand)) {
				jamMain.setSortMode(JamMain.NO_SORT);
				DataBase.clearAllLists();
				jamMain.dataChanged();
			} else if ("open".equals(incommand)) {
				selectEnabled = false;
				if (histio.readJHFFile()) {
					jamMain.setSortModeFile(histio.getFileNameOpen());
					DataControl.setupAll(); //setup all data bases
					jamMain.dataChanged();
					jamMain.repaint();
				}
				selectEnabled = true;
			} else if ("openhdf".equals(incommand)) {
				selectEnabled = false;
				if (hdfio.readFile(HDFIO.OPEN)) { //true if successful
					jamMain.setSortModeFile(hdfio.getFileNameOpen());
					DataControl.setupAll();
					jamMain.dataChanged();
					jamMain.repaint();
				}
				selectEnabled = true;
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
			} else if ("openascii".equals(incommand)) {
				if (impExpASCII.openFile()) {
					jamMain.setSortModeFile(impExpASCII.getLastFileName());
					DataControl.setupAll();
					jamMain.dataChanged();
				}
			} else if ("openspe".equals(incommand)) {
				if (impExpSPE.openFile()) {
					jamMain.setSortModeFile(impExpSPE.getLastFileName());
					DataControl.setupAll();
					jamMain.dataChanged();
				}
			} else if ("openornl".equals(incommand)) {
				selectEnabled = false;
				if (impExpORNL.openFile()) {
					jamMain.setSortModeFile(impExpORNL.getLastFileName());
					DataControl.setupAll();
					jamMain.dataChanged();
				}
				selectEnabled = true;
			} else if ("openxsys".equals(incommand)) {
				selectEnabled = false;
				if (impExpXSYS.openFile()) {
					jamMain.setSortModeFile(impExpXSYS.getLastFileName());
					DataControl.setupAll();
					jamMain.dataChanged();
				}
				selectEnabled = true;
			} else if ("saveascii".equals(incommand)) {
				impExpASCII.saveFile(
					Histogram.getHistogram(status.getCurrentHistogramName()));
			} else if ("savespe".equals(incommand)) {
				impExpSPE.saveFile(
					Histogram.getHistogram(status.getCurrentHistogramName()));
			} else if ("saveornl".equals(incommand)) {
				impExpORNL.saveFile(
					Histogram.getHistogram(status.getCurrentHistogramName()));
			} else if ("savexsys".equals(incommand)) { //FIXME not implemented
				console.errorOutln("Save xsys file NOT implemented");
				impExpXSYS.saveFile(
					Histogram.getHistogram(status.getCurrentHistogramName()));
			} else if ("batchexport".equals(incommand)) {
				batchexport.show();
			} else if ("print".equals(incommand)) {
				printHistogram();
			} else if ("printsetup".equals(incommand)) {
				PrinterJob.getPrinterJob();
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
			} else if ("rewindtape".equals(incommand)) {
				console.messageOut("Rewinding tape... ", JamConsole.NEW);
				console.messageOut(jam.sort.TapeDaemon.rewindTape());
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
			} else if ("loadfit".equals(incommand)) {
				loadFit.showLoad();
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
				jam.plot.Action.setAutoOnExpand(selected);
			} else if ("Automatic peak find".equals(text)) {
				display.setPreference(
					Display.Preferences.AUTO_PEAK_FIND,
					selected);
			}
		}
	}

	/** 
	 * A histogram has been selected so tell all
	 * applicable classes about it.
	 *
	 * @param hist The histogram to be selected and displayed
	 */
	void selectHistogram(Histogram hist) {
		if (hist != null) {
			if (!overlay) {
				status.setCurrentHistogramName(hist.getName());
				try {
					broadcaster.broadcast(BroadcastEvent.HISTOGRAM_SELECT);
					final Histogram h =
						Histogram.getHistogram(
							status.getCurrentHistogramName());
					display.displayHistogram(h);
					jamMain.gatesChanged();
					jamMain.setOverlayEnabled(h.getDimensionality() == 1);
				} catch (GlobalException ge) {
					console.errorOutln(
						getClass().getName() + ".selectHistogram(): " + ge);
				}
			} else {
				status.setOverlayHistogramName(hist.getName());
				console.messageOut(hist.getName(), MessageHandler.END);
				display.overlayHistogram(hist);
				synchronized (this) {
					overlay = false;
				}
				jamMain.deselectOverlay();
			}
		} else { //null object passed
			console.errorOutln(
				classname + "selectHistogram(Histogram): null argument");
		}
	}

	/**
	 * A gate has been selected. Tell all appropriate classes, like
	 * Display and JamStatus.
	 *
	 * @param gateObject the object, which should be a <code>Gate</code>
	 * @see jam.data.Gate
	 */
	private void selectGate(Object gateObject) {
		final String methodname = "selectGate(): ";
		if (gateObject instanceof Gate) {
			final Gate gate = (Gate) gateObject;
			try {
				status.setCurrentGateName(gate.getName());
				broadcaster.broadcast(BroadcastEvent.GATE_SELECT);
				if (gate.getType() == Gate.ONE_DIMENSION) {
					final double area = gate.getArea();
					final double centroid =
						(double) ((int) (gate.getCentroid() * 100.0)) / 100.0;
					final int lowerLimit = gate.getLimits1d()[0];
					final int upperLimit = gate.getLimits1d()[1];
					console.messageOut(
						"Gate: "
							+ gate.getName()
							+ ", Ch. "
							+ lowerLimit
							+ " to "
							+ upperLimit,
						MessageHandler.NEW);
					console.messageOut(
						"  Area = " + area + ", Centroid = " + centroid,
						MessageHandler.END);
				} else {
					final double area = gate.getArea();
					console.messageOut(
						"Gate " + gate.getName(),
						MessageHandler.NEW);
					console.messageOut(", Area = " + area, MessageHandler.END);
				}
				display.displayGate(gate);
			} catch (DataException de) {
				console.errorOutln(classname + methodname + de.getMessage());
			} catch (GlobalException ge) {
				console.errorOutln(classname + methodname + ge.getMessage());
			}
		} else {//error not a Gate
			console.errorOutln(
				classname
					+ "problem selecting gate - object instanceof "
					+ gateObject.getClass().getName()
					+ ", String rep=\""
					+ gateObject
					+ "\"");
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

	/**
	 * Print a histogram
	 *
	 * calls plot to actually draw the histogram
	 */
	public void printHistogram() {
		/* see nutshell java examples pages 75 and 177 */
		final Toolkit tk = jamMain.getToolkit();
		final String jobname = "jam_histogram";
		final Properties printprefs = System.getProperties();
		printprefs.put("awt.print.orientation", "landscape");
		final PrintJob pjob = tk.getPrintJob(jamMain, jobname, printprefs);
		if (pjob != null) { //user cancelled print request
			final Graphics gpage = pjob.getGraphics();
			/* cancel pjob=null might not work on windows 95 
			 * so try graphics object */
			if (gpage != null) {
				final Dimension pageSize = pjob.getPageDimension();
				final int pagedpi = pjob.getPageResolution();
				console.messageOut(
					"Printing histogram: " + status.getCurrentHistogramName()+" . ",
					MessageHandler.NEW);
				/* work done by display... */
				display.printHistogram(gpage, pageSize, pagedpi);
				gpage.dispose();
				pjob.end();
				console.messageOut("done!", MessageHandler.END);
			}
		}
	}
}
