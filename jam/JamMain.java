package jam;

import jam.commands.CommandManager;
import jam.data.control.AbstractControl;
import jam.global.AcquisitionStatus;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.CommandNames;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.global.SortMode;
import jam.plot.Display;
import jam.ui.SelectionToolbar;
import jam.ui.SelectionTree;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Launcher and main window for Jam.
 * 
 * @version 0.5 April 98
 * @author Ken Swartz
 * @since JDK1.1
 */
public final class JamMain extends JFrame implements Observer {
	/**
	 * Configuration information for Jam.
	 */
	private transient final JamProperties properties;

	/**
	 * Overall status of Jam.
	 */
	private transient final JamStatus status = JamStatus.instance();

	/**
	 * Event distributor.
	 */
	private transient final Broadcaster broadcaster = Broadcaster.getSingletonInstance();

	/**
	 * Histogram displayer.
	 */
	private transient final Display display;

	/**
	 * Message output and text input.
	 */
	private transient final JamConsole console;


	private transient final SelectionToolbar selectBar;

	private RunState runState = RunState.NO_ACQ;

	JamMain(final boolean showGUI) {
		super("Jam");
		status.setShowGUI(showGUI);
		setLookAndFeel();
		showSplashScreen(showGUI);
		/* Application initialization */
		properties = new JamProperties(); //class that has properties
		status.setFrame(this);
		status.setAcqisitionStatus(new AcquisitionStatus() {
			public boolean isAcqOn() {
				return getRunState().isAcqOn();
			}

			public boolean isOnLine() {
				return (status.getSortMode() == SortMode.ONLINE_DISK);
			}
		});
		/* class to distrute events to all listeners */
		broadcaster.addObserver(this);
		/* Create main window GUI */
		loadIcon();
		final Container contents = getContentPane();
		contents.setLayout(new BorderLayout());
		/* Ouput/Input text console */
		console = new JamConsole();
		console.messageOutln("Welcome to Jam v" + Version.getName());
		/* histogram displayer */
		display = new Display(console);
		final JSplitPane splitCenter = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT, true, display, console);
		splitCenter.setResizeWeight(0.5);
		/* fraction of resize space that goes to display */
		
		contents.add(splitCenter, BorderLayout.CENTER);
		
		/* Main menu bar */
		final MainMenuBar menus = new MainMenuBar();
		setJMenuBar(menus.menubar);
		/* Histogram selection menu bar */
		selectBar = new SelectionToolbar();
		contents.add(selectBar, BorderLayout.NORTH);
				
		// Histogram selection tree 
		SelectionTree selectTree = new SelectionTree();
		contents.add(selectTree, BorderLayout.WEST);
		final JSplitPane splitTree = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, true, selectTree, splitCenter);
		splitTree.setResizeWeight(0.5);
		contents.add(splitTree, BorderLayout.CENTER);		
		/**/
		/* operations to close window */
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				exit();
			}

			public void windowClosed(WindowEvent event) {
				exit();
			}
		});
		/* Initial histograms and setup */
		new InitialHistograms();
		AbstractControl.setupAll(); //setup jam.data.control dialog boxes
		status.setSortMode(SortMode.NO_SORT, "Jam Startup");

		selectTree.loadTree();

		selectBar.setChoosersToFirstItems();
		
		showMainWindow(showGUI);
		
	}

	/**
	 * Show the splash screen
	 * 
	 * @param showGUI
	 *            to to show splash screen
	 */
	private void showSplashScreen(boolean showGUI) {
		if (showGUI) {
			final int displayTime = 10000; //milliseconds
			new SplashWindow(this, displayTime);
		}
	}

	/**
	 * Load the application icon
	 */
	private void loadIcon() {
		final ClassLoader loader = getClass().getClassLoader();
		setIconImage((new ImageIcon(loader.getResource("jam/nukeicon.png"))
				.getImage()));

	}

	/**
	 * Show the main window
	 * 
	 * @param show
	 *            true to show GUI
	 */
	private void showMainWindow(final boolean show) {
		final int posx = 50;
		final int posy = 0;
		setLocation(posx, posy);
		setResizable(true);
		/* Important to initially display in the AWT/Swing thread. */
		final Runnable showWindow = new Runnable() {
			public void run() {
				pack();
				if (show) {
					setVisible(true);
				}
				/* print out where config files were read from */
				properties.setMessageHandler(console);
				properties.outputMessages(console);
			}
		};
		SwingUtilities.invokeLater(showWindow);
	}

	private void exit() {
		final JButton temp = new JButton(CommandManager.getInstance()
				.getAction(CommandNames.EXIT));
		temp.doClick();
	}

	/**
	 * Set the mode for sorting data, adjusting title and menu items as
	 * appropriate.
	 * 
	 * @exception JamException
	 *                sends a message to the console if there is an
	 *                inappropriate call
	 * @see jam.global.SortMode
	 * @param mode
	 *            the new mode for Jam to be in
	 */
	private void sortModeChanged() {
		final StringBuffer title = new StringBuffer("Jam - ");
		final String disk = "disk";
		final SortMode mode = status.getSortMode();
		if (mode == SortMode.ONLINE_DISK || mode == SortMode.ON_NO_DISK) {
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
			this.setTitle(title.append(status.getOpenFile()).toString());
		} else if (mode == SortMode.NO_SORT) {
			setRunState(RunState.NO_ACQ);
			title.append("sorting not enabled");
			this.setTitle(title.toString());
		}
	}

	/**
	 * <p>
	 * Sets run state when taking data online. The run state mostly determints
	 * the state of control JMenu items. This method uses imformation set by
	 * <code>setSortMode()</code>. In addition:
	 * </p>
	 * <ul>
	 * <li>Control JMenu items are enabled and disabled as appropriate.</li>
	 * <li>Control JMenu items are states are set and unset as appropriate.
	 * </li>
	 * <li>The JMenu bar is to show online sort.</li>
	 * <li>Updates display status label .</li>
	 * </ul>
	 * 
	 * @param state
	 *            one of the possible run states control dialog box
	 */
	private void setRunState(RunState state) {
		synchronized (runState) {
			runState = state;
		}
	}

	/**
	 * @return the current run state
	 */
	private RunState getRunState() {
		synchronized (runState) {
			return runState;
		}
	}

	private void setLookAndFeel() {
		final String linux = "Linux";
		final String kunststoff = "com.incors.plaf.kunststoff.KunststoffLookAndFeel";
		boolean bKunststoff = linux.equals(System.getProperty("os.name"));
		if (bKunststoff) {
			try {
				UIManager.setLookAndFeel(kunststoff);
			} catch (ClassNotFoundException e) {
				bKunststoff = false;
			} catch (Exception e) { //all other exceptions
				final String title = "Jam--error setting GUI appearance";
				JOptionPane.showMessageDialog(null, e.getMessage(), title,
						JOptionPane.WARNING_MESSAGE);
			}
		}
		if (!bKunststoff) {
			try {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				final String title = "Jam--error setting GUI appearance";
				JOptionPane.showMessageDialog(null, e.getMessage(), title,
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	public void update(Observable event, Object param) {
		final BroadcastEvent beParam = (BroadcastEvent) param;
		final BroadcastEvent.Command command = beParam.getCommand();
		if (command == BroadcastEvent.Command.SORT_MODE_CHANGED) {
			sortModeChanged();
		} else if (command == BroadcastEvent.Command.RUN_STATE_CHANGED) {
			setRunState((RunState) beParam.getContent());
		}
	}

	/**
	 * Main method that is run to start up full Jam process
	 * 
	 * @param args
	 *            not used currently
	 */
	public static void main(String args[]) {
		//RepaintManager.setCurrentManager(new ThreadCheckingRepaintManager());
		new JamMain(true);
	}
}
