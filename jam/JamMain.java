package jam;
import jam.commands.CommandManager;
import jam.data.control.DataControl;
import jam.global.AcquisitionStatus;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.CommandNames;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.global.SortMode;
import jam.plot.Display;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Launcher and main window for Jam.
 *
 * @version  0.5 April 98
 * @author   Ken Swartz
 * @since    JDK1.1
 */
public final class JamMain extends JFrame implements Observer {
	/**
	 * Configuration information for Jam.
	 */
	private final JamProperties jamProperties;

	/**
	 * Overall status of Jam.
	 */
	private final JamStatus status=JamStatus.instance();

	/**
	 * Event distributor.
	 */
	private final Broadcaster broadcaster=Broadcaster.getSingletonInstance();

	/**
	 * Histogram displayer.
	 */
	private final Display display;

	/**
	 * Message output and text input.
	 */
	private final JamConsole console;
	private final Container me;
	private final SelectionToolbar selectBar;
	private RunState runState = RunState.NO_ACQ;

	JamMain(final boolean showGUI) {
		super("Jam");
		status.setShowGUI(showGUI);
		setLookAndFeel();

		showSplashScreen(showGUI);								
				
		//Application initialization 				
		jamProperties = new JamProperties(); //class that has properties
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
		
		//Create main window GUI
		loadIcon();
		me = getContentPane();
		me.setLayout(new BorderLayout());
		//Ouput/Input text console
		console = new JamConsole();
		console.messageOutln("Welcome to Jam v" + Version.getName());
		// histogram displayer
		display = new Display(console);
		final JSplitPane splitCenter =
			new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, display, console);
		splitCenter.setResizeWeight(0.5);			
		/*fraction of resize space that goes to display*/
		me.add(splitCenter, BorderLayout.CENTER);
		//Main menu bar
		final JMenuBar menubar = new MainMenuBar();
		setJMenuBar(menubar);
		//Histogram selection menu bar
		selectBar = new SelectionToolbar();
		me.add(selectBar, BorderLayout.NORTH);
		// operations to close window
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
			public void windowClosed(WindowEvent e) {
				exit();
			}
		});
		
		//Initial histograms and setup
		new InitialHistograms();
		DataControl.setupAll(); 	//setup jam.data.control dialog boxes
		status.setSortMode(SortMode.NO_SORT);				
		selectBar.setChoosersToFirstItems();
		
		//Show Main window		
		showMainWindow(showGUI);

		
	}

	/**
	 *  Show the splash screen
	 * @param showGUI to to show splash screen
	 */
	private void showSplashScreen(boolean showGUI) {
		if (showGUI){
			final int titleDisplayTime = 10000; //milliseconds
			new SplashWindow(this, titleDisplayTime);
		}
	}
	/**
	 * Load the application icon 
	 */
	private void loadIcon() {
		final ClassLoader cl = getClass().getClassLoader();
		setIconImage(
			(new ImageIcon(cl.getResource("jam/nukeicon.png")).getImage()));
		
	}
	
	/**
	 * Show the main window
	 * 
	 * @param show true to show GUI
	 */
	private void showMainWindow(boolean show) {
		
		final boolean showGUI = show; 
		final int posx = 50;
		final int posy = 0; 
		setLocation(posx, posy);
		setResizable(true);
		
		/* Important to initially display in the AWT/Swing thread. */
		final Runnable showWindow = new Runnable() {
			public void run() {
				pack();
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
	private void exit(){
		final JButton temp=new JButton(CommandManager.getInstance().getAction(
		CommandNames.EXIT));
		temp.doClick();
	}

	/**
	 * Set the mode for sorting data, adjusting title and menu items as 
	 * appropriate.
	 *
	 * @exception JamException sends a message to the console if 
	 * there is an inappropriate call
	 * @see jam.global.SortMode
	 * @param mode the new mode for Jam to be in
	 */
	private void sortModeChanged() {
		final StringBuffer title = new StringBuffer("Jam - ");
		final String disk = "disk";
		final SortMode mode=status.getSortMode();
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
			this.setTitle(title.append(status.getOpenFile()).toString());
		} else if (mode == SortMode.NO_SORT) {
			setRunState(RunState.NO_ACQ);
			title.append("sorting not enabled");
			this.setTitle(title.toString());
		} 
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
	private void setRunState(RunState rs) {
		synchronized (runState) {
			runState = rs;
		}
	}

	/**
	 * @return the current run state
	 */
	private RunState getRunState() {
		synchronized (runState){
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
	
	public void update(Observable event, Object param){
		final BroadcastEvent be=(BroadcastEvent)param;
		final int command=be.getCommand();
		if (command==BroadcastEvent.SORT_MODE_CHANGED){
			sortModeChanged();
		} else if (command==BroadcastEvent.RUN_STATE_CHANGED){
			setRunState((RunState)be.getContent());
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
