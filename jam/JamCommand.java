package jam;
import jam.commands.CommandException;
import jam.commands.CommandManager;
import jam.global.MessageHandler;
import jam.plot.Display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
	implements ActionListener {

	private final MessageHandler console;
	private final PeakFindDialog peakFindDialog;
	private final SetupRemote setupRemote;
	private final CommandManager jamCmdMgr= CommandManager.getInstance();
	private RemoteAccess remoteAccess = null;
	private boolean remote = false;

	/** Constructor for this class.
	 * @param jm The launch class for the Jam application
	 * @param d the area where histograms are displayed
	 * @param jc the text input and output area
	 */
	public JamCommand(JamMain jam, Display d, MessageHandler jc) {
		super();
		console = jc;
		/* acquisition control */
		setupRemote = new SetupRemote(jam, console);
		peakFindDialog = new PeakFindDialog(d, console);
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
