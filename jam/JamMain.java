package jam;
import jam.data.DataException;
import jam.data.control.DataControl;
import jam.global.AcquisitionStatus;
import jam.global.Broadcaster;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.plot.Display;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Launcher and main window for Jam.
 *
 * @version  0.5 April 98
 * @author   Ken Swartz
 * @since    JDK1.1
 */
public class JamMain extends JFrame {

	/**
	 * Sort Mode--No sort file loaded.
	 */
	static public final int NO_SORT = 0;

	/**
	 * Sort Mode--Set to sort online data to disk.
	 */
	static public final int ONLINE_DISK = 1;

	/**
	 * Sort Mode--Set to sort online data to tape.
	 */
	static public final int ONLINE_TAPE = 2;

	/**
	 * Sort Mode--Set to sort offline data from disk.
	 */
	static public final int OFFLINE_DISK = 3;

	/**
	 * Sort Mode--Set to sort offline data from tape.
	 */
	static public final int OFFLINE_TAPE = 4;

	/**
	 * Sort Mode--Acting as a client to a remote Jam process.
	 */
	static public final int REMOTE = 5;

	/**
	 * Sort Mode--Just read in a data file.
	 */
	static public final int FILE = 6; //we have read in a file
	
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

	private int sortMode;
	private final String classname;
	private RunState runState=RunState.NO_ACQ;
	private String openFileName;

	private JamMain() {
		super("Jam");
		final int titleDisplayTime=10000; //milliseconds
		new SplashWindow(this, titleDisplayTime);
		final ClassLoader cl=getClass().getClassLoader();
		setIconImage((new ImageIcon(cl.getResource(
		"jam/nukeicon.png")).getImage()));
		classname=getClass().getName()+"--";
		me = this.getContentPane();
		jamProperties = new JamProperties(); //class that has properties
		status = JamStatus.instance(); //class that is statically available
		status.setAcqisitionStatus(new AcquisitionStatus(){
			public boolean isAcqOn(){
				return runState.isAcqOn();
			}
			
			public boolean isOnLine(){
				return ((sortMode == ONLINE_TAPE) || (sortMode == ONLINE_DISK));
			}
		});
		/* class to distrute events to all listeners */
		broadcaster = new Broadcaster();
		final int posxy=50;
		this.setLocation(posxy, posxy);
		this.setResizable(true);
		me.setLayout(new BorderLayout());
		console = new JamConsole();
		console.messageOutln("Welcome to Jam v" + Version.getName());
		me.add(console, BorderLayout.SOUTH);
		/* histogram displayer (needed by jamCommand) */
		display = new Display(broadcaster, console);
		me.add(display, BorderLayout.CENTER);
		/* create user command listener */
		jamCommand = new JamCommand(this, display, broadcaster, 
		console);
		menubar=new MainMenuBar(this, jamCommand, display,console);
		this.setJMenuBar(menubar);
		selectBar=new SelectionToolbar(console,status,broadcaster,display);
		broadcaster.addObserver(selectBar);
		me.add(selectBar, BorderLayout.NORTH);
		display.addToolbarAction();//the left-hand action toolbar
		/* operations to close window */
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				showExitDialog();
			}
			public void windowClosed(WindowEvent e) {
				showExitDialog();
			}
		});
		try { //create first histogams
			new InitialHistograms();
		} catch (DataException de) {
			console.errorOutln(de.getMessage());
		}
		DataControl.setupAll();//setup jam.data.control dialog boxes
		try { //setting no sort does not throw an exception
			setSortMode(NO_SORT);
		} catch (JamException je) {
			console.errorOutln(classname+"Exception while setting sort mode: "+
			je.getMessage());
		}
		/* Important to initially display in the AWT/Swing thread. */
		final Runnable showWindow=new Runnable(){
			public void run(){ 
				pack();
				selectBar.setChoosersToFirstItems();
				show();
				/* print out where config files were read from */
				jamProperties.setMessageHandler(console);
				jamProperties.outputMessages(console);
			}
		};
		SwingUtilities.invokeLater(showWindow);
	}

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

	/**
	 * Set the mode for sorting data, adjusting title and menu items as 
	 * appropriate.
	 *
	 * @exception JamException sends a message to the console if 
	 * there is an inappropriate call
	 * @see #ONLINE_DISK
	 * @see #ONLINE_TAPE
	 * @see #OFFLINE_DISK
	 * @see #OFFLINE_TAPE
	 * @see #FILE
	 * @see #REMOTE
	 * @see #NO_SORT
	 * @param mode the new mode for Jam to be in
	 */
	public void setSortMode(int mode) throws JamException {
		final StringBuffer title=new StringBuffer("Jam - ");
		final String disk="disk";
		final String tape="tape";
		if (!((mode == NO_SORT) || (mode == FILE))) {
			boolean error=true;
			final StringBuffer etext=new StringBuffer(
			"Can't setup, setup is locked for ");
			if (sortMode == ONLINE_DISK || sortMode == ONLINE_TAPE) {
				etext.append("online");
			} else if (sortMode == OFFLINE_DISK || sortMode == OFFLINE_TAPE) {
				etext.append("offline");
			} else if (sortMode == REMOTE) {
				etext.append("remote");
			} else {
				error=false;
			}
			if (error){
				throw new JamException(etext.toString());
			}
		}
		synchronized (this) {
			sortMode = mode;
		}
		menubar.setSortMode(mode);
		if (mode == ONLINE_DISK || mode == ONLINE_TAPE) {
			setRunState(RunState.ACQ_OFF);
			title.append("Online Sorting TO ");
			if (mode == ONLINE_DISK) {
				this.setTitle(title.append(disk).toString());
			} else {
				this.setTitle(title.append(tape).toString());
			}
		} else if (mode == OFFLINE_DISK || mode == OFFLINE_TAPE) {
			setRunState(RunState.ACQ_OFF);
			title.append("Offline Sorting FROM ");
			if (mode == OFFLINE_DISK) {
				this.setTitle(title.append(disk).toString());
			} else {
				this.setTitle(title.append(tape).toString());
			}
		} else if (mode == REMOTE) { //remote display
			setRunState(RunState.NO_ACQ);
			this.setTitle(title.append("Remote Mode").toString());
		} else if (mode == FILE) {//just read in a file
			setRunState(RunState.NO_ACQ);
			this.setTitle(title.append(openFileName).toString());
		} else if (mode == NO_SORT) {
			setRunState(RunState.NO_ACQ);
			title.append("sorting not enabled");
			this.setTitle(title.toString());
		} else {
			console.errorOutln("Invalid sort mode: "+mode);
		}
	}

	/**
	 * Sets the sort mode to FILE and gives it the file name.
	 *
	 * @exception JamException sends a message to the console if 
	 * there is a problem
	 * @param fileName the file to be sorted?
	 */
	public void setSortModeFile(String fileName) throws JamException {
		synchronized (this){
			this.openFileName = fileName;
		}
		setSortMode(FILE);
	}

	/**
	 * @return the current sort mode.
	 *
	 * @see #setSortMode(int)
	 * @see #setSortModeFile(String)
	 */
	public int getSortMode() {
		return sortMode;
	}
	
	/**
	 * @return true is the mode can be changed
	 */
	public boolean canSetSortMode() {
		return ((sortMode == NO_SORT) || (sortMode == FILE));
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
		return runState;
	}
	
	/**
	 * Main method that is run to start up full Jam process
	 * 
	 * @param args not used currently
	 */
	public static void main(String args[]) {
		final String linux="Linux";
		try {
			if (linux.equals(System.getProperty("os.name"))){
				UIManager.setLookAndFeel(new com.incors.plaf.kunststoff.KunststoffLookAndFeel());
			} else {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
		} catch (Exception e) {
			final String title="Jam--error setting GUI appearance";
			JOptionPane.showMessageDialog(null,e.getMessage(),title,
			JOptionPane.WARNING_MESSAGE);
		}
		new JamMain();
	}
}
