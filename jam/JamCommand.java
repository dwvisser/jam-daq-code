package jam;
import jam.commands.CommandException;
import jam.commands.CommandManager;
import jam.data.control.CalibrationDisplay;
import jam.data.control.CalibrationFit;
import jam.data.control.GainShift;
import jam.data.control.GateSet;
import jam.data.control.Manipulations;
import jam.data.control.ParameterControl;
import jam.data.control.Projections;
import jam.global.Broadcaster;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.global.RunInfo;
import jam.plot.Display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;

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
	implements ActionListener, ItemListener {

	static final int MESSAGE_SCALER = 1; //alert scaler class
	private final String classname;

	private final Display display;
	private final JamConsole console;

	private final Broadcaster broadcaster = Broadcaster.getSingletonInstance();

	/* control classes */
	private SortControl sortControl;
	private final GateSet gateControl;
	private final ParameterControl paramControl;
	private final CalibrationDisplay calibDisplay;
	private final CalibrationFit calibFit;
	private final Projections projection;
	private final Manipulations manipulate;
	private final GainShift gainshift;

	private final PeakFindDialog peakFindDialog;

	/* classes to set up acquisition and sorting */
	private final SetupSortOff setupSortOff;
	private final SetupRemote setupRemote;

	private final FrontEndCommunication frontEnd;
	private final Help help;

	private final JamStatus status;

	private CommandManager jamCmdMgr;
	private RemoteAccess remoteAccess = null;
	private boolean remote = false;

	/** Constructor for this class.
	 * @param jm The launch class for the Jam application
	 * @param d the area where histograms are displayed
	 * @param jc the text input and output area
	 */
	public JamCommand(JamMain jam, Display d, JamConsole jc) {
		super();
		classname = getClass().getName() + " - ";
		display = d;
		console = jc;
		status = JamStatus.instance();
		/* class to hold run information */
		new RunInfo();
		/* communication */
		frontEnd = new VMECommunication(console);
		/* data bases manipulation */
		gateControl = new GateSet(console);
		paramControl = new ParameterControl(jam, console);
		calibDisplay = new CalibrationDisplay(console);
		calibFit = new CalibrationFit(console);
		projection = new Projections(console);
		manipulate = new Manipulations(console);
		gainshift = new GainShift(console);
		/* acquisition control */
		sortControl = new SortControl(console);
		setupSortOff =
			new SetupSortOff(sortControl);
		setupRemote = new SetupRemote(jam, console);
		help = new Help(jam, console); //Help window
		peakFindDialog = new PeakFindDialog(display, console);
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
	private final void addObservers() {
		broadcaster.addObserver(display);
		broadcaster.addObserver(frontEnd);
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
		if ("Black Background".equals(incommand)) {
			display.setPreference(Display.Preferences.BLACK_BACKGROUND, true);
		} else if ("White Background".equals(incommand)) {
			display.setPreference(Display.Preferences.WHITE_BACKGROUND, true);
		} else if ("offline".equals(incommand)) {
			setupSortOff.show();
		} else if ("remote".equals(incommand)) {
			setupRemote.showRemote();
		} else if ("sort".equals(incommand)) {
			sortControl.show();
		} else if ("status".equals(incommand)) {
			DisplayCounters.getSingletonInstance().show();
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
		} else if ("about".equals(incommand)) {
			help.showAbout();
		} else if ("license".equals(incommand)) {
			help.showLicense();
		} else if ("peakfind".equals(incommand)) {
			peakFindDialog.show();
		} else { //See if it a command class
			try {
				if (!jamCmdMgr.performCommand(incommand, null)) {
					console.errorOutln(
						getClass().getName()
							+ ": Error unrecognized command \""
							+ incommand
							+ "\"");
				}
			} catch (CommandException ce) {
				console.errorOutln(
					getClass().getName()
						+ ": Error performing command \""
						+ incommand
						+ "\"");
			}
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

	SetupSortOff getSetupSortOff() {
		return setupSortOff;
	}

	SortControl getSortControl() {
		return sortControl;
	}
}
