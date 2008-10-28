package jam;

import jam.commands.CommandManager;
import jam.commands.CommandNames;
import jam.data.control.AbstractControl;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.global.SortMode;
import jam.plot.PlotDisplay;
import jam.sort.control.SetupSortOn;
import jam.ui.Console;
import jam.ui.SelectionTree;
import jam.ui.SummaryTable;
import jam.ui.Utility;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

/**
 * Launcher and main window for Jam.
 * 
 * @version 0.5 April 98
 * @author Ken Swartz
 * @since JDK1.1
 */
public final class JamMain extends JFrame {

	/**
	 * Message output and text input.
	 */
	private static final Console console;

	private static final Logger LOGGER;

	static {
		Utility.setLookAndFeel();
		final String packageName = JamMain.class.getPackage().getName();
		LOGGER = Logger.getLogger(packageName);
		console = jam.ui.Factory.createConsole(packageName);
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

	/**
	 * Overall status of Jam.
	 */
	private transient final JamStatus status = JamStatus.getSingletonInstance();

	private transient final SummaryTable summaryTable;

	private static JamMain instance;

	private static Object staticLock = new Object();

	/**
	 * Returns the singleton instance.
	 * 
	 * @param showGUI
	 *            whether to have the window visible when returned
	 * @return the singleton instance
	 */
	public static JamMain getInstance(final boolean showGUI) {
		synchronized (staticLock) {
			if (null == instance) {
				instance = new JamMain(showGUI);
			} else {
				instance.setVisible(showGUI);
			}
		}
		return instance;
	}

	@Override
	public void setVisible(final boolean show) {
		super.setVisible(show);
		status.setShowGUI(show);
	}

	private JamMain(final boolean showGUI) {
		super("Jam");
		status.setShowGUI(showGUI);
		showSplashScreen(showGUI);

		/* Application initialization */
		properties = new JamProperties(); // class that has properties
		status.setFrame(this);

		/* this object rooted by Broadcaster and JamStatus */
		new AcquisitionAndRunState(this);
		/* class to distribute events to all listeners */
		/* Create main window GUI */
		loadIcon();
		final Container contents = getContentPane();
		contents.setLayout(new BorderLayout());
		/* Output/Input text console */
		LOGGER.info("Welcome to Jam v" + Version.getInstance().getName());
		if (!SetupSortOn.exists()) {
			SetupSortOn.createInstance(console.getLog());
		}
		final ToolBar jamToolBar = new ToolBar();
		contents.add(jamToolBar, BorderLayout.NORTH);
		/* histogram displayer */
		final PlotDisplay plotDisplay = new PlotDisplay(console, CommandManager
				.getInstance().getCommandFinder());
		PlotDisplay.setDisplay(plotDisplay);
		summaryTable = new SummaryTable();
		SummaryTable.setTable(summaryTable);
		final Display display = new Display(plotDisplay, summaryTable);
		final JSplitPane splitCenter = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT, true, display, console);
		splitCenter.setResizeWeight(0.9);
		/* fraction of resize space that goes to display */
		contents.add(splitCenter, BorderLayout.CENTER);
		setJMenuBar(MenuBar.getMenuBar());
		/* Histogram selection tree */
		final SelectionTree selectTree = new SelectionTree();
		contents.add(selectTree, BorderLayout.WEST);
		final JSplitPane splitTree = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, true, selectTree, splitCenter);
		splitTree.setResizeWeight(0.1);
		contents.add(splitTree, BorderLayout.CENTER);
		/* operations to close window */
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(final WindowEvent event) {
				exit();
			}

			@Override
			public void windowClosing(final WindowEvent event) {
				exit();
			}
		});
		/* Initial histograms and setup */
		final InitialHistograms initHists = new InitialHistograms();
		AbstractControl.setupAll(); // setup jam.data.control dialog boxes
		status.setSortMode(SortMode.NO_SORT, "Jam Startup");
		status.setCurrentGroup(initHists.getInitialGroup());
		SelectionTree.setCurrentHistogram(initHists.getInitialHist());
		Broadcaster.getSingletonInstance().broadcast(
				BroadcastEvent.Command.HISTOGRAM_SELECT,
				initHists.getInitialHist());
		showMainWindow(showGUI);
	}

	private void exit() {
		final Action exit = CommandManager.getInstance().getAction(
				CommandNames.EXIT);
		if (null == exit) {
			throw new IllegalStateException("Couldn't find exit action.");
		}

		final JButton temp = new JButton();
		temp.doClick();
	}

	/**
	 * Load the application icon
	 */
	private void loadIcon() {
		final ClassLoader loader = Thread.currentThread()
				.getContextClassLoader();
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

}
