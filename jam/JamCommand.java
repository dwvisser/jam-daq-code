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
import java.awt.Frame;
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
import javax.swing.SwingUtilities;
import jam.fit.LoadFit;

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

	private JamMain jamMain;
	private Frame frame;
	private Display display;
	private JamConsole console;
	private MessageHandler msgHandler;

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
	public LoadFit loadFit;

	//control of data objects
	private HistogramControl histogramControl;
	private GateControl gateControl;
	private ScalerControl scalerControl;
	private MonitorControl monitorControl;
	private ParameterControl paramControl;
	private CalibrationDisplay calibDisplay;
	private CalibrationFit calibFit;
	private Projections projection;
	private Manipulations manipulate;
	private GainShift gainshift;

	private PeakFindDialog peakFindDialog;

	//class to setup acquistion and sorting
	private SetupSortOn setupSortOn;
	private SetupSortOff setupSortOff;
	private SetupRemote setupRemote;

	private FrontEndCommunication frontEnd;
	private RemoteAccess remoteAccess;
	private Help help;

	private boolean overlay;
	private boolean remote;

	private boolean selectEnabled = true; //is selection enabled
	private JamStatus status;

	/** Constructor for this class.
	 * @param jamMain The launch class for the Jam application
	 * @param display the area where histograms are displayed
	 * @param broadcaster the object which handles application-wide communications
	 * @param console the text input and output area
	 */
	public JamCommand(
		JamMain jamMain,
		Display display,
		Broadcaster broadcaster,
		JamConsole console) {
		this.jamMain = jamMain;
		this.display = display;
		this.broadcaster = broadcaster;
		this.console = console;
		this.msgHandler = console;
		this.frame = (Frame) jamMain;
		status = JamStatus.instance();
		// class to hold run information
		new RunInfo();
		//io classes
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
		gateControl = new GateControl(frame, broadcaster, console);
		scalerControl = new ScalerControl(frame, broadcaster, console);
		monitorControl = new MonitorControl(frame, broadcaster, console);
		paramControl = new ParameterControl(frame, broadcaster, console);
		calibDisplay = new CalibrationDisplay(frame, broadcaster, console);
		calibFit = new CalibrationFit(frame, broadcaster, console);
		projection = new Projections(frame, broadcaster, console);
		manipulate = new Manipulations(frame, broadcaster, console);
		gainshift = new GainShift(frame, broadcaster, console);
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
		help = new Help(jamMain);
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
		//let display listen for text input commands
		console.setCommandListener((CommandListener) display);
		//are we going to overlay a histogram
		overlay = false;
		//remote display histograms
		remote = false;
		//New PageFormat object
		//pageFormat=new PageFormat();
	}

	/**
	 * Receives all the inputs from the pull down menus
	 * that are <code>ActionEvent</code>'s.
	 * Receives input from VME and dispatches messages.
	 *
	 * @param  e    Action event from pull down menus
	 * @since Version 0.5
	 */
	public synchronized void actionPerformed(ActionEvent e) {
		String incommand = e.getActionCommand();
		try {
			if (incommand == "overlay") {
				overlay = jamMain.overlaySelected();
				if (overlay) {
					console.messageOut("Overlay Spectrum ", MessageHandler.NEW);
				}
			} else if (incommand == "selecthistogram") {
				if (selectEnabled) { //nested to avoid missing "selecthistogram"
					Object item = ((JComboBox) e.getSource()).getSelectedItem();
					if (item instanceof String) {
						Histogram h = Histogram.getHistogram((String) item);
						if (h != null) {
							selectHistogram(h);
						}
					}
				}
			} else if (incommand == "selectgate") {
				Object item = ((JComboBox) e.getSource()).getSelectedItem();
				if (selectEnabled && item instanceof String) {
					Gate gate = Gate.getGate((String) item);
					if (gate != null)
						selectGate(gate);
				}
			} else if (incommand == "Black Background") {
				display.setPreference(Display.BLACK_BACKGROUND, true);
			} else if (incommand == "White Background") {
				display.setPreference(Display.WHITE_BACKGROUND, true);
			} else if (incommand == "newclear") {
				jamMain.setSortMode(JamMain.NO_SORT);
				DataBase.clearAllLists();
				jamMain.dataChanged();
			} else if (incommand == "open") {
				selectEnabled = false;
				if (histio.readJHFFile()) {
					jamMain.setSortModeFile(histio.getFileNameOpen());
					DataControl.setupAll(); //setup all data bases
					jamMain.dataChanged();
					jamMain.repaint();
				}
				selectEnabled = true;
			} else if (incommand == "openhdf") {
				selectEnabled = false;
				if (hdfio.readFile(HDFIO.OPEN)) { //true if successful
					jamMain.setSortModeFile(hdfio.getFileNameOpen());
					DataControl.setupAll();
					jamMain.dataChanged();
					jamMain.repaint();
				}
				selectEnabled = true;
			} else if (incommand == "reloadhdf") {
				if (hdfio.readFile(HDFIO.RELOAD)) { //true if successful
					scalerControl.displayScalers();
				}
			} else if (incommand == "reload") {
				if (histio.reloadJHFFile()) {
					scalerControl.displayScalers();
				}
			} else if (incommand == "save") {
				histio.writeJHFFile();
			} else if (incommand == "savehdf") {
				hdfio.writeFile(HDFIO.lastValidFile());
			} else if (incommand == "saveas") {
				histio.writeJHFFile();
			} else if (incommand == "saveAsHDF") {
				hdfio.writeFile();
			} else if (incommand == "openascii") {
				if (impExpASCII.openFile()) {
					jamMain.setSortModeFile(impExpASCII.getLastFileName());
					DataControl.setupAll();
					jamMain.dataChanged();
				}
			} else if (incommand == "openspe") {
				if (impExpSPE.openFile()) {
					jamMain.setSortModeFile(impExpSPE.getLastFileName());
					DataControl.setupAll();
					jamMain.dataChanged();
				}
			} else if (incommand == "openornl") {
				selectEnabled = false;
				if (impExpORNL.openFile()) {
					jamMain.setSortModeFile(impExpORNL.getLastFileName());
					DataControl.setupAll();
					jamMain.dataChanged();
				}
				selectEnabled = true;
			} else if (incommand == "openxsys") {
				selectEnabled = false;
				if (impExpXSYS.openFile()) {
					jamMain.setSortModeFile(impExpXSYS.getLastFileName());
					DataControl.setupAll();
					jamMain.dataChanged();
				}
				selectEnabled = true;
			} else if (incommand == "saveascii") {
				impExpASCII.saveFile(
					Histogram.getHistogram(status.getCurrentHistogramName()));
			} else if (incommand == "savespe") {
				impExpSPE.saveFile(
					Histogram.getHistogram(status.getCurrentHistogramName()));
			} else if (incommand == "saveornl") {
				impExpORNL.saveFile(
					Histogram.getHistogram(status.getCurrentHistogramName()));
			} else if (incommand == "savexsys") { //FIXME not implemented
				console.errorOutln("Save xsys file NOT implemented");
				impExpXSYS.saveFile(
					Histogram.getHistogram(status.getCurrentHistogramName()));
			} else if (incommand == "batchexport") {
				batchexport.show();
			} else if (incommand == "print") {
				printHistogram();
			} else if (incommand == "printsetup") {
				PrinterJob.getPrinterJob();
			} else if (incommand == "online") {
				setupSortOn.show();
			} else if (incommand == "offline") {
				setupSortOff.show();
			} else if (incommand == "remote") {
				setupRemote.showRemote();
			} else if (incommand == "flush") {
				runControl.flushAcq();
			} else if (incommand == "run") {
				runControl.show();
			} else if (incommand == "sort") {
				sortControl.show();
			} else if (incommand == "parameters") {
				paramControl.show();
			} else if (incommand == "status") {
				displayCounters.show();
			} else if (incommand == "rewindtape") {
				console.messageOut("Rewinding tape... ", JamConsole.NEW);
				console.messageOut(jam.sort.TapeDaemon.rewindTape());
			} else if (incommand == "newhist") {
				histogramControl.showNew();
			} else if (incommand == "zerohist") {
				histogramControl.showZero();
			} else if (incommand == "project") {
				projection.show();
			} else if (incommand == "manipulate") {
				manipulate.show();
			} else if (incommand == "gainshift") {
				gainshift.show();
			} else if (incommand == "caldisp") {
				calibDisplay.show();
			} else if (incommand == "calfitlin") {
				calibFit.showLinFit();
			} else if (incommand == "gatenew") {
				gateControl.showNew();
			} else if (incommand == "gateadd") {
				gateControl.showAdd();
			} else if (incommand == "gateset") {
				gateControl.showSet();
			} else if (incommand == "displayscalers") {
				scalerControl.showDisplay();
			} else if (incommand == "displaymonitors") {
				monitorControl.showDisplay();
			} else if (incommand == "configmonitors") {
				monitorControl.showConfig();
			} else if (incommand == "zeroscalers") {
				scalerControl.showZero();
			} else if (incommand == "loadfit") {
				loadFit.showLoad();
			} else if (incommand == "about") {
				help.showAbout();
			} else if (incommand == "jamdoc") {
				help.showJamDocs();
			} else if (incommand.equals("license")) {
				help.showLicense();
			} else if (incommand.equals("peakfind")) {
				peakFindDialog.show();
			} else if (incommand == "start") {
				if (status.isOnLine()) {
					runControl.startAcq();
				} else {
					console.errorOutln(
						"Error: start command given when not in Online mode.");
				}
			} else if (incommand == "stop") {
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

	/** Recieves the inputs from the pull down menus that are selectable checkboxes.
	 * @param ie event triggered by selecting an item in the menus
	 */
	public void itemStateChanged(ItemEvent ie) {
		AbstractButton item = (AbstractButton) ie.getItem();
		if ((item == null)) { //catch error
			System.err.println("Error: Item selected is null");
			return;
		}
		String text = ((AbstractButton) item).getText();
		if (text == "Ignore zero channel on autoscale") {
			display.setPreference(Display.AUTO_IGNORE_ZERO, item.isSelected());
		} else if (text == "Ignore max channel on autoscale") {
			display.setPreference(Display.AUTO_IGNORE_FULL, item.isSelected());
		} else if (text == "Verbose front end") {
			frontEnd.verbose(item.isSelected());
			JamProperties.setProperty(
				JamProperties.FRONTEND_VERBOSE,
				item.isSelected());
		} else if (text == "Debug front end") {
			frontEnd.debug(item.isSelected());
			JamProperties.setProperty(
				JamProperties.FRONTEND_DEBUG,
				item.isSelected());
		} else if (text == "Autoscale on Expand/Zoom") {
			jam.plot.Action.setAutoOnExpand(item.isSelected());
		} else if (text.equals("Automatic peak find")) {
			display.setPreference(Display.AUTO_PEAK_FIND, item.isSelected());
		} else if (text.equals(JamMain.NO_FILL_2D)) {
			JamProperties.setProperty(
				JamProperties.NO_FILL_2D,
				item.isSelected());
		}

	}

	/** A histogram has been selected so tell all
	 * applicable classes about it.
	 * @param hist The histogram to be selected and displayed.
	 */
	void selectHistogram(Histogram hist) throws GlobalException {
		if (hist != null) {
			if (!overlay) {
				status.setCurrentHistogramName(hist.getName());
				Thread worker = new Thread() {
					public void run() {
						try {
							SwingUtilities.invokeAndWait(new Runnable() {
								public void run() {
									try {
										broadcaster.broadcast(
											BroadcastEvent.HISTOGRAM_SELECT);
									} catch (GlobalException ge) {
										msgHandler.errorOutln(
											getClass().getName()
												+ ".selectHistogram(): "
												+ ge);
									}
									display.displayHistogram(
										Histogram.getHistogram(
											status.getCurrentHistogramName()));
									jamMain.gatesChanged();
								}
							});
						} catch (InterruptedException ie) {
							msgHandler.errorOutln(ie.toString());
						} catch (
							java.lang.reflect.InvocationTargetException ite) {
							msgHandler.errorOutln(ite.toString());
						}
					}
				};
				worker.start();
			} else {
				status.setOverlayHistogramName(hist.getName());
				console.messageOut(hist.getName(), MessageHandler.END);
				display.overlayHistogram(hist);
				overlay = false;
				jamMain.deselectOverlay();
			}
		} else { //null object passed
			System.err.println(
				getClass().getName()
					+ ".selectHistogram(Histogram): null argument");
		}
	}

	/**
	 * A gate has been selected. Tell all appropriate classes, like
	 * Display and JamStatus.
	 *
	 * @exception DataException thrown if could not get limits
	 */
	private void selectGate(Object gateObject) {
		int lowerLimit, upperLimit, area;
		double centroid;

		if (gateObject instanceof Gate) {
			Gate gate = (Gate) gateObject;
			try {
				status.setCurrentGateName(gate.getName());
				broadcaster.broadcast(BroadcastEvent.GATE_SELECT);
				if (gate.getType() == Gate.ONE_DIMENSION) {
					area = gate.getArea();
					centroid =
						(double) ((int) (gate.getCentroid() * 100.0)) / 100.0;
					lowerLimit = gate.getLimits1d()[0];
					upperLimit = gate.getLimits1d()[1];
					msgHandler.messageOut(
						"Gate: "
							+ gate.getName()
							+ ", Ch. "
							+ lowerLimit
							+ " to "
							+ upperLimit,
						MessageHandler.NEW);
					msgHandler.messageOut(
						"  Area = " + area + ", Centroid = " + centroid,
						MessageHandler.END);
				} else {
					area = gate.getArea();
					msgHandler.messageOut(
						"Gate " + gate.getName(),
						MessageHandler.NEW);
					msgHandler.messageOut(
						", Area = " + area,
						MessageHandler.END);
				}
				display.displayGate(gate);
			} catch (DataException de) {
				msgHandler.errorOutln(de.getMessage());
			} catch (GlobalException ge) {
				msgHandler.errorOutln(
					getClass().getName() + ".selectGate(): " + ge);
			}
		} else {
			//error not a Gate
			System.err.println(
				"Error: JamCommand.selectGate(Object): Object instanceof "
					+ gateObject.getClass().getName()
					+ ", String rep=\""
					+ gateObject
					+ "\"");
		}
	}

	/** Sets whether or not we are in remote mode...broken for now...may be
	 * implemented in the future.
	 * @param on Whether we are in remote mode or not.
	 * @param remoteAccess ???
	 */
	public void setRemote(boolean on, RemoteAccess remoteAccess) {
		this.remoteAccess = remoteAccess;
		remote = on;
	}

	/**
	 * Print a histogram
	 *
	 * calls plot to actually draw the histogram
	 */
	public void printHistogram() {
		/* see nutshell java examples pages 75 and 177 */
		Toolkit tk = jamMain.getToolkit();
		PrintJob pjob;
		int pagedpi;
		Dimension pageSize;
		Graphics gpage = null;
		String jobname = "jam_histogram";
		Properties printprefs = System.getProperties();
		printprefs.put("awt.print.orientation", "landscape");
		pjob = tk.getPrintJob((Frame) jamMain, jobname, printprefs);
		if (pjob != null) { //user cancelled print request
			gpage = pjob.getGraphics();
			/* cancel pjob=null might not work on windows 95 
			 * so try graphics object */
			if (gpage != null) {
				pageSize = pjob.getPageDimension();
				pagedpi = pjob.getPageResolution();
				System.err.println(
					getClass().getName()
						+ ".printHistogram(): printPlot pageSize "
						+ pageSize);
				System.err.println(
					getClass().getName()
						+ ".printHistogram(): printPlot pagedpi "
						+ pagedpi);
				msgHandler.messageOut(
					"Printing histogram: " + status.getCurrentHistogramName(),
					MessageHandler.NEW);
				msgHandler.messageOut(" . ");
				/* work done by display... */
				display.printHistogram(gpage, pageSize, pagedpi);
				gpage.dispose();
				pjob.end();
				msgHandler.messageOut("done!", MessageHandler.END);
			}
		}
	}
}
