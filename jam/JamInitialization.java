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
import jam.ui.Console;
import jam.ui.SelectionTree;
import jam.ui.SummaryTable;

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
import javax.swing.WindowConstants;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Launcher and main window for Jam.
 * 
 * @version 0.5 April 98
 * @author Ken Swartz
 * @since JDK1.1
 */
@Singleton
public final class JamInitialization {

	private static final Logger LOGGER = Logger
			.getLogger(JamInitialization.class.getPackage().getName());
	/**
	 * Configuration information for Jam.
	 */
	private transient final JamProperties properties;

	private transient final SummaryTable summaryTable;

	private transient final JFrame frame;

	/**
	 * @param frame
	 *            the GUI frame
	 * @param status
	 *            global status
	 * @param console
	 *            text console
	 * @param properties
	 *            accessor for Jam's properties files
	 * @param plotDisplay
	 *            plot display internal frame
	 * @param summaryTable
	 *            displays summary statistics
	 * @param aars
	 *            injected by Guice, but don't need to do anything with it
	 *            because it registers itself with the broadcaster
	 * @param selectTree
	 *            selection tree
	 * @param broadcaster
	 *            handles application-wide events
	 */
	@Inject
	public JamInitialization(final JFrame frame, final JamStatus status,
			final Console console, final JamProperties properties,
			final PlotDisplay plotDisplay, final SummaryTable summaryTable,
			final AcquisitionAndRunState aars, final SelectionTree selectTree,
			final Broadcaster broadcaster) {
		this.frame = frame;
		this.properties = properties;
		status.setFrame(this.frame);
		this.summaryTable = summaryTable;
		broadcaster.addObserver(aars);

		/* class to distribute events to all listeners */
		/* Create main window GUI */
		loadIcon();
		final Container contents = this.frame.getContentPane();
		contents.setLayout(new BorderLayout());
		/* Output/Input text console */
		LOGGER.info("Welcome to Jam v" + Version.getInstance().getName());

		/* For now, initializing ToolBar depends on status having the frame. */
		final ToolBar jamToolBar = new ToolBar();
		contents.add(jamToolBar, BorderLayout.NORTH);
		/* histogram displayer */
		PlotDisplay.setDisplay(plotDisplay);
		SummaryTable.setTable(summaryTable);
		final Display display = new Display(plotDisplay, summaryTable);
		final JSplitPane splitCenter = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT, true, display, console);
		splitCenter.setResizeWeight(0.9);
		/* fraction of resize space that goes to display */
		contents.add(splitCenter, BorderLayout.CENTER);
		this.frame.setJMenuBar(MenuBar.getMenuBar());
		/* Histogram selection tree */
		contents.add(selectTree, BorderLayout.WEST);
		final JSplitPane splitTree = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, true, selectTree, splitCenter);
		splitTree.setResizeWeight(0.1);
		contents.add(splitTree, BorderLayout.CENTER);
		/* operations to close window */
		this.frame
				.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(final WindowEvent event) {
				exit();
			}

			@Override
			public void windowClosing(final WindowEvent event) {
				exit();
			}

			private void exit() {
				final Action exit = CommandManager.getInstance().getAction(
						CommandNames.EXIT);
				if (null == exit) {
					throw new IllegalStateException(
							"Couldn't find exit action.");
				}

				final JButton temp = new JButton(exit);
				temp.doClick();
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
	}

	/**
	 * Load the application icon
	 */
	private void loadIcon() {
		final ClassLoader loader = Thread.currentThread()
				.getContextClassLoader();
		this.frame.setIconImage((new ImageIcon(loader
				.getResource("jam/nukeicon.png")).getImage()));
	}

	/**
	 * Show the main window
	 * 
	 * @param show
	 *            true to show GUI
	 */
	protected void showMainWindow() {
		final int posx = 50;
		final int posy = 0;
		this.frame.setLocation(posx, posy);
		this.frame.setResizable(true);
		/* Important to initially display in the AWT/Swing thread. */
		final Runnable showWindow = new Runnable() {
			public void run() {
				JamInitialization.this.frame.pack();
				JamInitialization.this.frame.setVisible(true);

				/* print out where configuration files were read from */
				properties.outputMessages();
				summaryTable.repaint();
			}
		};
		SwingUtilities.invokeLater(showWindow);
	}
}
