package jam;
import jam.data.control.DataControl;
import jam.global.AcquisitionStatus;
import jam.global.Broadcaster;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.global.SortMode;
import jam.plot.Display;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 * Launcher and main window for Jam.
 *
 * @version  0.5 April 98
 * @author   Ken Swartz
 * @since    JDK1.1
 */
public final class JamMain extends JFrame {
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
	private final Container me;
	private final MainMenuBar menubar;
	private final SelectionToolbar selectBar;

	private SortMode sortMode;
	private final String classname;
	private RunState runState = RunState.NO_ACQ;
	private String openFileName;

	private JamMain(final boolean showGUI) {
		super("Jam");
		setLookAndFeel();
		final int titleDisplayTime = 10000; //milliseconds
		new SplashWindow(this, titleDisplayTime);
		final ClassLoader cl = getClass().getClassLoader();
		setIconImage(
			(new ImageIcon(cl.getResource("jam/nukeicon.png")).getImage()));
		classname = getClass().getName() + "--";
		me = this.getContentPane();
		jamProperties = new JamProperties(); //class that has properties
		status = JamStatus.instance(); //class that is statically available
		status.setFrame(this);
		status.setJamMain(this);
		status.setAcqisitionStatus(new AcquisitionStatus() {
			public boolean isAcqOn() {
				return runState.isAcqOn();
			}

			public boolean isOnLine() {
				return (sortMode == SortMode.ONLINE_DISK);
			}
		});
		/* class to distrute events to all listeners */
		broadcaster = new Broadcaster();
		this.setResizable(true);
		me.setLayout(new BorderLayout());
		console = new JamConsole();
		console.messageOutln("Welcome to Jam v" + Version.getName());
		//me.add(console, BorderLayout.SOUTH);
		/* histogram displayer (needed by jamCommand) */
		display = new Display(broadcaster, console);
		//me.add(display, BorderLayout.CENTER);
		final JSplitPane splitCenter =
			new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, display, console);
		/*fraction of resize space that goes to display*/
		me.add(splitCenter, BorderLayout.CENTER);
		/* create user command listener */
		jamCommand = new JamCommand(this, display, broadcaster, console);
		menubar = new MainMenuBar(this, jamCommand, display, console);
		this.setJMenuBar(menubar);
		selectBar = new SelectionToolbar(console, status, broadcaster, display, menubar);
		broadcaster.addObserver(selectBar);
		me.add(selectBar, BorderLayout.NORTH);
		display.addToolbarAction(); //the left-hand action toolbar
		/* operations to close window */
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				jamCommand.actionPerformed(new ActionEvent(this, 0,  "exit"));
				//KBS Remove 
				//showExitDialog();
			}
			public void windowClosed(WindowEvent e) {
				jamCommand.actionPerformed(new ActionEvent(this, 0, "exit"));
				//KBS Remove
				//showExitDialog();
			}
		});
		new InitialHistograms();
		DataControl.setupAll(); //setup jam.data.control dialog boxes
		setSortMode(SortMode.NO_SORT);
		/* Important to initially display in the AWT/Swing thread. */
		final Runnable showWindow = new Runnable() {
			public void run() {
				pack();
				selectBar.setChoosersToFirstItems();
				splitCenter.setResizeWeight(0.5);
				final int posx = 50;
				final int posy = 0;
				setLocation(posx, posy);
				if (showGUI){
					show();
				}
				/* print out where config files were read from */
				jamProperties.setMessageHandler(console);
				jamProperties.outputMessages(console);
			}
		};
		SwingUtilities.invokeLater(showWindow);
	}
	
	JamMain(Script s){
		this(false);
		s.setJamCommand(jamCommand);
	}

	/* KBS Remove now in  command ShowDialogExitCmd 
	void showExitDialog() {
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
	*/

	/**
	 * Set the mode for sorting data, adjusting title and menu items as 
	 * appropriate.
	 *
	 * @exception JamException sends a message to the console if 
	 * there is an inappropriate call
	 * @see #ONLINE_DISK
	 * @see #ONLINE_NODISK
	 * @see #OFFLINE_DISK
	 * @see #FILE
	 * @see #REMOTE
	 * @see #NO_SORT
	 * @param mode the new mode for Jam to be in
	 */
	public void setSortMode(SortMode mode) {
		final StringBuffer title = new StringBuffer("Jam - ");
		final String disk = "disk";
		
		//Check that run state can be changed
		if (!((mode == SortMode.NO_SORT) || (mode == SortMode.FILE))) {
			boolean error = true;
			final StringBuffer etext =
				new StringBuffer("Can't setup, setup is locked for ");
			if (sortMode == SortMode.ONLINE_DISK) {
				etext.append("online");
			} else if (sortMode == SortMode.OFFLINE) {
				etext.append("offline");
			} else if (sortMode == SortMode.REMOTE) {
				etext.append("remote");
			} else {
				error = false;
			}
			if (error) {
				throw new UnsupportedOperationException(etext.toString());
			}
		}
		
		synchronized (this) {
			sortMode = mode;
		}
		menubar.setSortMode(mode);
		if (mode == SortMode.ONLINE_DISK || mode == SortMode.ONLINE_NO_DISK) {
			setRunState(RunState.ACQ_OFF);
			title.append("Online Sorting");
			if (mode == SortMode.ONLINE_DISK) {
				title.append(" TO ").append(disk);
			} 
			setTitle(title.toString());
		} else if (mode == SortMode.OFFLINE) {
			setRunState(RunState.ACQ_OFF);
			title.append("Offline Sorting");
			if (mode == SortMode.OFFLINE) {
				title.append(" FROM ").append(disk);
			}
			this.setTitle(title.toString());
		} else if (mode == SortMode.REMOTE) { //remote display
			setRunState(RunState.NO_ACQ);
			this.setTitle(title.append("Remote Mode").toString());
		} else if (mode == SortMode.FILE) { //just read in a file
			setRunState(RunState.NO_ACQ);
			this.setTitle(title.append(openFileName).toString());
			menubar.setSaveEnabled(true);
		} else if (mode == SortMode.NO_SORT) {
			setRunState(RunState.NO_ACQ);
			title.append("sorting not enabled");
			this.setTitle(title.toString());
		} else {
			console.errorOutln("Invalid sort mode: " + mode);
		}
	}

	/**
	 * Sets the sort mode to FILE and gives it the file name.
	 *
	 * @exception JamException sends a message to the console if 
	 * there is a problem
	 * @param fileName the file to be sorted?
	 */
	public void setSortMode(File file) {
		synchronized (this) {
			this.openFileName=file.getName();
		}
		setSortMode(SortMode.FILE);
	}

	/**
	 * @return the current sort mode.
	 *
	 * @see #setSortMode(int)
	 * @see #setSortModeFile(String)
	 */
	public SortMode getSortMode() {
		return sortMode;
	}

	/**
	 * @return true is the mode can be changed
	 */
	public boolean canSetSortMode() {
		return ((sortMode == SortMode.NO_SORT) || (sortMode == SortMode.FILE));
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
	 * @param  rs one of the possible run states
	 * control dialog box
	 */
	public void setRunState(RunState rs) {
		menubar.setRunState(rs);
		selectBar.setRunState(rs);
		synchronized (this) {
			this.runState = rs;
		}
	}

	/**
	 * @return the current run state
	 */
	public RunState getRunState() {
		synchronized (this){
			return runState;
		}
	}
	
	private void setLookAndFeel(){
		final String linux = "Linux";
		final String kunststoff =
			"com.incors.plaf.kunststoff.KunststoffLookAndFeel";
		boolean useKunststoff = linux.equals(System.getProperty("os.name"));
		if (useKunststoff) {
			try {
				UIManager.setLookAndFeel(kunststoff);
			} catch (ClassNotFoundException e) {
				useKunststoff = false;
			} catch (Exception e) { //all other exceptions
				final String title = "Jam--error setting GUI appearance";
				JOptionPane.showMessageDialog(
					null,
					e.getMessage(),
					title,
					JOptionPane.WARNING_MESSAGE);
			}
		}
		if (!useKunststoff) {
			try {
				UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				final String title = "Jam--error setting GUI appearance";
				JOptionPane.showMessageDialog(
					null,
					e.getMessage(),
					title,
					JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/**
	 * Main method that is run to start up full Jam process
	 * 
	 * @param args not used currently
	 */
	public static void main(String args[]) {
		new JamMain(true);
	}
}
