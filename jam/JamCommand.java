package jam;
import jam.commands.CommandException;
import jam.commands.CommandManager;
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

	private final Display display;
	private final JamConsole console;
	private final Broadcaster broadcaster = Broadcaster.getSingletonInstance();
	private final PeakFindDialog peakFindDialog;
	private final SetupRemote setupRemote;
	private final FrontEndCommunication frontEnd;
	private final JamStatus status;
	private final CommandManager jamCmdMgr;
	private RemoteAccess remoteAccess = null;
	private boolean remote = false;

	/** Constructor for this class.
	 * @param jm The launch class for the Jam application
	 * @param d the area where histograms are displayed
	 * @param jc the text input and output area
	 */
	public JamCommand(JamMain jam, Display d, JamConsole jc) {
		super();
		display = d;
		console = jc;
		status = JamStatus.instance();
		/* class to hold run information */
		new RunInfo();
		/* communication */
		frontEnd = new VMECommunication(console);
		broadcaster.addObserver(frontEnd);
		/* acquisition control */
		setupRemote = new SetupRemote(jam, console);
		peakFindDialog = new PeakFindDialog(display, console);
		jamCmdMgr = CommandManager.getInstance();
		console.addCommandListener(jamCmdMgr);
		console.addCommandListener(display);
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
		if ("remote".equals(incommand)) {
			setupRemote.showRemote();
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
			if ("Verbose front end".equals(text)) {
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
}
