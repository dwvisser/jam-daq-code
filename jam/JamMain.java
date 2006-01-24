package jam;

import jam.commands.CommandManager;
import jam.data.DataBase;
import jam.data.control.AbstractControl;
import jam.global.AcquisitionStatus;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.CommandNames;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.global.LoggerConfig;
import jam.global.RunState;
import jam.global.SortMode;
import jam.plot.PlotDisplay;
import jam.sort.control.SetupSortOn;
import jam.ui.Console;
import jam.ui.JamToolBar;
import jam.ui.MenuBar;
import jam.ui.SelectionTree;
import jam.ui.SummaryTable;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

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
	 * Message output and text input.
	 */
	private static final Console console = new Console();

	private static final String packageName = JamMain.class.getPackage()
			.getName();

	private static final Logger LOGGER;
	static {
		new LoggerConfig(packageName, console);
		LOGGER = Logger.getLogger(packageName);
	}

	/**
	 * Main method that is run to start up full Jam process
	 * 
	 * @param args
	 *            not used currently
	 */
	public static void main(final String args[]) {
		new JamMain(true);
	}

	/**
	 * Configuration information for Jam.
	 */
	private transient final JamProperties properties;

	private RunState runState = RunState.NO_ACQ;

	/**
	 * Overall status of Jam.
	 */
	private transient final JamStatus status = JamStatus.getSingletonInstance();

	private transient final SummaryTable summaryTable;

	JamMain(final boolean showGUI) {
		super("Jam");
		status.setShowGUI(showGUI);
		setLookAndFeel();
		showSplashScreen(showGUI);
		/* Application initialization */
		properties = new JamProperties(); // class that has properties
		status.setFrame(this);
		status.setValidator(DataBase.getInstance());
		status.setAcqisitionStatus(new AcquisitionStatus() {
			public boolean isAcqOn() {
				return getRunState().isAcqOn();
			}
		});
		/* class to distrute events to all listeners */
		final Broadcaster broadcaster = Broadcaster.getSingletonInstance();
		broadcaster.addObserver(this);
		/* Create main window GUI */
		loadIcon();
		final Container contents = getContentPane();
		contents.setLayout(new BorderLayout());

		/* Ouput/Input text console */
		LOGGER.info("Welcome to Jam v" + Version.getInstance().getName());
		JamStatus.getSingletonInstance().setMessageHandler(console);
		SetupSortOn.createInstance(console);
		final JamToolBar jamToolBar = new JamToolBar();
		contents.add(jamToolBar, BorderLayout.NORTH);

		/* histogram displayer */
		final PlotDisplay plotDisplay = new PlotDisplay(console);
		JamStatus.getSingletonInstance().setDisplay(plotDisplay);
		summaryTable = new SummaryTable();
		JamStatus.getSingletonInstance().setTable(summaryTable);
		final Display display = new Display(plotDisplay, summaryTable);
		final JSplitPane splitCenter = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT, true, display, console);
		splitCenter.setResizeWeight(0.9);
		/* fraction of resize space that goes to display */
		contents.add(splitCenter, BorderLayout.CENTER);
		setJMenuBar(MenuBar.getMenuBar());
		/* Histogram selection tree */
		SelectionTree selectTree = new SelectionTree();
		contents.add(selectTree, BorderLayout.WEST);
		final JSplitPane splitTree = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, true, selectTree, splitCenter);
		splitTree.setResizeWeight(0.1);
		contents.add(splitTree, BorderLayout.CENTER);
		/* operations to close window */
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosed(final WindowEvent event) {
				exit();
			}

			public void windowClosing(final WindowEvent event) {
				exit();
			}
		});
		/* Initial histograms and setup */
		final InitialHistograms initHists = new InitialHistograms();
		AbstractControl.setupAll(); // setup jam.data.control dialog boxes
		status.setSortMode(SortMode.NO_SORT, "Jam Startup");
		status.setCurrentGroup(initHists.getInitialGroup());
		status.setCurrentHistogram(initHists.getInitialHist());
		broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT,
				initHists.getInitialHist());
		showMainWindow(showGUI);
	}

	private void exit() {
		final JButton temp = new JButton(CommandManager.getInstance()
				.getAction(CommandNames.EXIT));
		temp.doClick();
	}

	/**
	 * @return the current run state
	 */
	private RunState getRunState() {
		synchronized (runState) {
			return runState;
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

	private void setLookAndFeel() {
		try {
			String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
			// Override system look and feel for gtk
			if (lookAndFeel
					.equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")) {
				lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
			}
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (Exception e) {
			warning(e, "Jam--error setting GUI appearance");
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
	private void setRunState(final RunState state) {
		synchronized (runState) {
			runState = state;
		}
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
				properties.outputMessages();
				summaryTable.repaint();
			}
		};
		SwingUtilities.invokeLater(showWindow);
	}

	/**
	 * Show the splash screen
	 * 
	 * @param showGUI
	 *            to to show splash screen
	 */
	private void showSplashScreen(final boolean showGUI) {
		if (showGUI) {
			final int displayTime = 10000; // milliseconds
			new SplashWindow(this, displayTime);
		}
	}

	/**
	 * Set the mode for sorting data, adjusting title and menu items as
	 * appropriate.
	 * 
	 * @see jam.global.SortMode
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
		} else if (mode == SortMode.REMOTE) { // remote display
			setRunState(RunState.NO_ACQ);
			this.setTitle(title.append("Remote Mode").toString());
		} else if (mode == SortMode.FILE) { // just read in a file
			setRunState(RunState.NO_ACQ);
			this.setTitle(title.append(status.getSortName()).toString());
		} else if (mode == SortMode.NO_SORT) {
			setRunState(RunState.NO_ACQ);
			title.append("sorting not enabled");
			this.setTitle(title.toString());
		}
	}

	/**
	 * @see Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(final Observable event, final Object param) {
		final BroadcastEvent beParam = (BroadcastEvent) param;
		final BroadcastEvent.Command command = beParam.getCommand();
		if (command == BroadcastEvent.Command.SORT_MODE_CHANGED) {
			sortModeChanged();
		} else if (command == BroadcastEvent.Command.RUN_STATE_CHANGED) {
			setRunState((RunState) beParam.getContent());
		}
	}

	void warning(final Exception exception, final String title) {
		JOptionPane.showMessageDialog(null, exception.getMessage(), title,
				JOptionPane.WARNING_MESSAGE);
	}
}
