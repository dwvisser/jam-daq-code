/*
 * Copyright statement
 */
package jam;
import jam.data.DataException;
import jam.data.Histogram;
import jam.data.control.DataControl;
import jam.global.AcquisitionStatus;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.plot.Display;
import java.awt.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;
import javax.swing.border.*;

/**
 *
 * Main Class for Jam.
 * This class makes the pull down menu and
 *
 * It is implemented by <code>Jam</code>.
 *
 * @version  0.5 April 98
 * @author   Ken Swartz
 * @since    JDK1.1
 */
public class JamMain extends JFrame implements AcquisitionStatus, 
Observer {

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
	 * Run State--Acquisition not currently allowed.
	 */
	public final static int NO_ACQ = 0;

	/**
	 * Run State--Not currently acuiring data.
	 */
	public final static int ACQ_OFF = 1;

	/**
	 * Run State--Currently acuiring data.
	 */
	public final static int ACQ_ON = 2;

	/**
	 * Run State--Not currently taking run data
	 */
	public final static int RUN_OFF = 3;

	/**
	 * Run State--Currently acuiring run data.
	 */
	public final static int RUN_ON = 4;

	
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

	private JLabel lrunState; //run state label
	private JComboBox histogramChooser; //reference needed by command
	private JToggleButton boverLay; //button for overlay
	private JComboBox gateChooser; // reference needed by command


	private final Container me;
	private final MainMenuBar menubar;

	/**
	 * Sort mode
	 * ONLINE or OFFLINE
	 */
	private int sortMode;
	
	private final String classname;

	/**
	 * Run state can be ACQ_ON, ACQ_OFF ....
	 */
	private RunState runState;

	/**
	 * Name of file if used file|open to read a file
	 */
	private String openFileName;

	/**
	 * Construtor
	 * create Jam window
	 * console is used to output log to the user.
	 *
	 */
	private JamMain() {
		super("Jam");
		classname=getClass().getName()+"--";
		final int titleDisplayTime=10000; //milliseconds
		new SplashWindow(this, titleDisplayTime);
		me = this.getContentPane();
		jamProperties = new JamProperties(); //class that has properties
		jamProperties.loadProperties(); //load properties from file
		status = JamStatus.instance(); //class that is statically available
		status.setAcqisitionStatus(this);
		/* class to distrute events to all listeners */
		broadcaster = new Broadcaster();
		final int posxy=50;
		this.setLocation(posxy, posxy);
		this.setResizable(true);
		this.setBackground(Color.lightGray);
		this.setForeground(Color.black);
		me.setLayout(new BorderLayout());
		console = new JamConsole();
		me.add(console, BorderLayout.SOUTH);
		/* histogram displayer (needed by jamCommand) */
		display = new Display(broadcaster, console);
		me.add(display, BorderLayout.CENTER);
		/* create user command listener */
		jamCommand = new JamCommand(this, display, broadcaster, console);
		menubar=new MainMenuBar(this, jamCommand, display,console);
		this.setJMenuBar(menubar);
		/* add toolbar (needs jamCommand as item, action listener) */
		final Component pselect=addToolbarSelect();
		me.add(pselect, BorderLayout.NORTH);
		/* tool bar display (on left side) */
		display.addToolbarAction();
		/* list of loaded fit routines */
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
		gateChooser.setModel(new GateComboBoxModel());
		/* setup all other dialog boxes.
		   data control, gate set, histogram manipulate, project */
		DataControl.setupAll();
		try { //setting no sort does not throw an exception
			setSortMode(NO_SORT);
		} catch (JamException je) {
			console.errorOutln(classname+"Exception while setting sort mode: "+
			je.getMessage());
		}
		/* The pack() call and everything after it here should be executed in the 
		 * event dispatch thread. */
		final Runnable showWindow=new Runnable(){
			public void run(){ 
				pack();
				setChoosersToFirstItems();
				show();
				/* print out where config files were read from */
				jamProperties.setMessageHandler(console);
				jamProperties.outputMessages(console);
			}
		};
		SwingUtilities.invokeLater(showWindow);
	}

	/**
	 * Adds the tool bar the at the top of the plot.
	 *
	 * @since Version 0.5
	 * @return the selection toolbar component
	 */
	private Component addToolbarSelect() {
		final DefaultComboBoxModel noGateComboBoxModel = new DefaultComboBoxModel();
		noGateComboBoxModel.addElement("NO GATES");
		/* panel with selection and print etc. */
		final JToolBar pselect = new JToolBar("Selection",JToolBar.HORIZONTAL);
		pselect.setLayout(new BorderLayout());
		pselect.setBackground(Color.lightGray);
		pselect.setForeground(Color.black);
		//run status
		final JPanel pRunState = new JPanel(new GridLayout(1, 1));
		pRunState.setBorder(
			BorderFactory.createTitledBorder(
				new BevelBorder(BevelBorder.LOWERED),
				"Status",
				TitledBorder.CENTER,
				TitledBorder.TOP));
		lrunState = new JLabel("   Welcome   ", SwingConstants.CENTER);
		lrunState.setOpaque(true);
		lrunState.setForeground(Color.black);
		pRunState.add(lrunState);
		//histogram chooser
		final JPanel pCenter = new JPanel(new GridLayout(1, 0));
		histogramChooser = new JComboBox(new HistogramComboBoxModel());
		histogramChooser.setRenderer(new HistogramListCellRenderer());
		histogramChooser.setMaximumRowCount(30);
		histogramChooser.setSelectedIndex(0);
		histogramChooser.setToolTipText(
			"Choose histogram to display.");
		histogramChooser.setActionCommand("selecthistogram");
		histogramChooser.addActionListener(jamCommand);
		pCenter.add(histogramChooser);
		//overlay button
		synchronized (this) {
			boverLay = new JToggleButton("Overlay");
		}
		boverLay.setActionCommand("overlay");
		boverLay.setToolTipText("Click to overlay next histogram chosen.");
		boverLay.addActionListener(jamCommand);
		pCenter.add(boverLay);
		synchronized (this){
			gateChooser=new JComboBox(noGateComboBoxModel);
		}
		gateChooser.setToolTipText("Click to choose gate to display.");
		gateChooser.setActionCommand("selectgate");
		gateChooser.addActionListener(jamCommand);
		pCenter.add(gateChooser);
		pselect.add(pRunState, BorderLayout.WEST);
		pselect.add(pCenter, BorderLayout.CENTER);
		return pselect;
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
	 * Implementation of Observable interface.
	 * 
	 * @param observable the sender
	 * @param o the message
	 */
	public void update(Observable observable, Object o) {
			final BroadcastEvent be = (BroadcastEvent) o;
			final int command=be.getCommand();
			if (command==BroadcastEvent.HISTOGRAM_NEW) {
				final String lastHistName = status.getCurrentHistogramName();
				jamCommand.selectHistogram(
					Histogram.getHistogram(lastHistName));
			}
			if (command==BroadcastEvent.HISTOGRAM_ADD) {
				dataChanged();
			}
			if (command==BroadcastEvent.GATE_ADD) {
				final String lastHistName = status.getCurrentHistogramName();
				jamCommand.selectHistogram(
					Histogram.getHistogram(lastHistName));
				gatesChanged();
			}
	}

	/**
	 * Should be called whenever the lists of gates and histograms 
	 * change. It calls histogramsChanged() and gatesChanged(), 
	 * each of which add to the event stack, so that histograms will 
	 * be guaranteed (?) updated before gates get updated.
	 */
	void dataChanged() {
		histogramsChanged();
		gatesChanged();
	}

	void histogramsChanged() {
		histogramChooser.setSelectedIndex(0);
		histogramChooser.repaint();
	}

	void gatesChanged() {
		gateChooser.setSelectedIndex(0);
		gateChooser.repaint();
	}
	
	void setOverlayEnabled(boolean state){
		this.boverLay.setEnabled(state);
	}

	/**
	 * Determines the mode for sorting data
	 * Enables and disables JMenu items as appropriate.
	 * Gives the window a title for the sort Mode.
	 *
	 * @exception   JamException    sends a message to the console if 
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
		//online sort
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
			/* read in a file */
		} else if (mode == FILE) {
			setRunState(RunState.NO_ACQ);
			this.setTitle(title.append(openFileName).toString());
		} else if (mode == NO_SORT) {
			setRunState(RunState.NO_ACQ);
			title.append("sorting not enabled");
			this.setTitle(title.toString());
		}
	}

	/**
	 * Set the jam to be in sort mode file and gives
	 * it the file Name.
	 *
	 * @exception   JamException    sends a message to the console if 
	 * there is a problem
	 * @param fileName the file to be sorted?
	 */
	public void setSortModeFile(String fileName) throws JamException {
		synchronized (openFileName){
			this.openFileName = fileName;
		}
		setSortMode(FILE);
	}

	/**
	 * @return the current sort mode.
	 *
	 * @see #ONLINE_DISK
	 * @see #ONLINE_TAPE
	 * @see #OFFLINE_DISK
	 * @see #OFFLINE_TAPE
	 * @see #FILE
	 * @see #REMOTE
	 * @see #NO_ACQ
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
	 * @return true if Jam is in online acquisition mode
	 */
	public boolean isOnLine() {
		return ((sortMode == ONLINE_TAPE) || (sortMode == ONLINE_DISK));
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
	 * @param  rs    see the options for this just below
	 * @param  runNumber   serial number assigned the run in the run 
	 * control dialog box
	 * @see #NO_ACQ
	 * @see #ACQ_OFF
	 * @see #ACQ_ON
	 * @see #RUN_OFF
	 * @see #RUN_ON
	 */
	public void setRunState(RunState rs) {
//		final String welcome="   Welcome   ";
//		final String stopped="   Stopped   ";
//		final String started="   Started   ";
		menubar.setRunState(rs);
//		if (rs == NO_ACQ) {
//			lrunState.setBackground(Color.lightGray);
//			lrunState.setText(welcome);
//		} else if (rs == ACQ_OFF) {
//			lrunState.setBackground(Color.red);
//			lrunState.setText(stopped);
//		} else if (rs == ACQ_ON) {
//			lrunState.setBackground(Color.orange);
//			lrunState.setText(started);
//		} else if (rs == RUN_OFF) {
//			lrunState.setBackground(Color.red);
//			lrunState.setText(stopped);
//		} else if (rs == RUN_ON) {
//			lrunState.setBackground(Color.green);
//			final String runpre="   Run ";
//			final String runpost="   ";
//			lrunState.setText(runpre+runNumber+runpost);
//		} else {
//			console.errorOutln("Illegal run state: "+rs);
//		}
		lrunState.setBackground(rs.getColor());
		lrunState.setText(rs.getLabel());
		synchronized (this) {
			this.runState = rs;
		}
	}

	/**
	 * Sets the run state with out the run number specified
	 * see <code> setRunState(int runState, int runNumber) </code>
	 *
	 * @see #getRunState()
	 * @param rs one of six possible modes
	 */
	/*public void setRunState(int rs) {
		setRunState(rs, 0);
	}*/

	/**
	 * Gets the current run state of Jam.
	 *
	 * @see #NO_SORT
	 * @see #ACQ_OFF
	 * @see #ACQ_ON
	 * @see #RUN_OFF
	 * @see #RUN_ON
	 * @see #NO_ACQ
	 * @return one of the six possible modes
	 */
	public RunState getRunState() {
		return runState;
	}

	/**
	 * @return true if Jam is currently taking data.
	 * either just acquistion or a run.
	 */
	public boolean isAcqOn() {
		return runState.isAcquireOn();
	}

	/**
	 * @return whether histogram overlay mode is enabled
	 */
	public boolean overlaySelected() {
		return boverLay.isSelected();
	}

	/**
	 * De-select overlay mode.
	 */
	 public void deselectOverlay() {
		if (boverLay.isSelected()) {
			boverLay.doClick();
		}
	}
	
	/**
	 * @return a string representing the build version of Jam running
	 */
	static public String getVersion(){
		final StringBuffer rval=new StringBuffer(Version.JAM_VERSION);
		if (Version.VERSION_TYPE.length()>0){
			final String leftparen=" (";
			rval.append(leftparen);
			rval.append(Version.VERSION_TYPE);
			rval.append(')');
		}
		return rval.toString();
	}

	/**
	 * Selects first items in histogram and gate choosers.  Default 
	 * priveleges allows JamCommand to call this as well.
	 */
	private void setChoosersToFirstItems() {
		histogramChooser.setSelectedIndex(0);
		gateChooser.setSelectedIndex(0);
	}

	
	/**
	 * Main method that is run to start up full Jam process
	 * 
	 * @param args not used currently
	 */
	public static void main(String args[]) {
		//System.out.println("Launching Jam v" + getVersion());
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			final String title="Jam--error setting GUI appearance";
			JOptionPane.showMessageDialog(null,e.getMessage(),title,
			JOptionPane.WARNING_MESSAGE);
		}
		new JamMain();
	}
}
