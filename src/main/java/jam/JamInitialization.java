package jam;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jam.commands.CommandManager;
import jam.commands.CommandNames;
import jam.data.control.AbstractControl;
import jam.global.*;
import jam.ui.SelectionTree;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.logging.Logger;

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

	private transient final JFrame frame;

	/**
	 * @param frame
	 *            the GUI frame
	 * @param status
	 *            global status
	 * @param properties
	 *            accessor for Jam's properties files
	 * @param broadcaster
	 *            handles application-wide events
	 * @param menuBar
	 *            menu bar
	 * @param commandManager
	 *            handles commands
	 * @param jamToolBar
	 *            iconic toolbar
	 * @param sdPanel
	 *            selection and display
	 * @param initHists
	 *            initial histograms
	 * @param aars
	 *            AcquisitionAndRunState instance
	 * @param version
	 *            jam version
	 */
	@Inject
	public JamInitialization(final JFrame frame, final JamStatus status,
			final JamProperties properties, final Broadcaster broadcaster,
			final MenuBar menuBar, final CommandManager commandManager,
			final ToolBar jamToolBar, final SelectionAndDisplayPanel sdPanel,
			final InitialHistograms initHists,
			final AcquisitionAndRunState aars, final Version version) {
		this.frame = frame;
		this.properties = properties;
		broadcaster.addPropertyChangeListener(aars);
		loadIcon();
		final Container contents = this.frame.getContentPane();
		contents.setLayout(new BorderLayout());
		LOGGER.info("Welcome to Jam v" + version.getName());
		if (version.isJ2SE6()) {
			LOGGER.info("You are running on J2SE 6 or later. Good!");
		} else {
			LOGGER
					.warning("You are running on J2SE 5. Consider upgrading to J2SE 6 for better multi-thread performance.");
		}
		contents.add(jamToolBar, BorderLayout.NORTH);
		this.frame.setJMenuBar(menuBar.getMenuBar());
		contents.add(sdPanel, BorderLayout.CENTER);
		this.frame
				.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.frame.addWindowListener(this.createWindowListener(commandManager));
		broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		AbstractControl.setupAll(); // setup jam.data.control dialog boxes
		status.setSortMode(SortMode.NO_SORT, "Jam Startup");
		status.setCurrentGroup(initHists.getInitialGroup());
		SelectionTree.setCurrentHistogram(initHists.getInitialHist());
		broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT,
				initHists.getInitialHist());
	}

	private WindowListener createWindowListener(
			final CommandManager commandManager) {
		return new WindowAdapter() {
			@Override
			public void windowClosed(final WindowEvent event) {
				exit();
			}

			@Override
			public void windowClosing(final WindowEvent event) {
				exit();
			}

			private void exit() {
				final Action exit = commandManager.getAction(CommandNames.EXIT);
				if (null == exit) {
					throw new IllegalStateException(
							"Couldn't find exit action.");
				}

				final JButton temp = new JButton(exit);
				temp.doClick();
			}
		};
	}

	/**
	 * Load the application icon
	 */
	private void loadIcon() {
		final ClassLoader loader = Thread.currentThread()
				.getContextClassLoader();
		this.frame.setIconImage(new ImageIcon(loader
				.getResource("jam/nukeicon.png")).getImage());
	}

	/**
	 * Show the main window.
	 */
	protected void showMainWindow() {
		final int posx = 50;
		final int posy = 0;
		this.frame.setLocation(posx, posy);
		this.frame.setResizable(true);
		/* Important to initially display in the AWT/Swing thread. */
		final Runnable showWindow = () -> {
            JamInitialization.this.frame.pack();
            JamInitialization.this.frame.setVisible(true);

            /* print out where configuration files were read from */
            properties.outputMessages();
        };
		SwingUtilities.invokeLater(showWindow);
	}
}
