package jam;

import jam.data.Histogram;
import jam.global.GoodThread;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.global.RunInfo;
import jam.io.DataIO;
import jam.io.hdf.HDFIO;
import jam.sort.Controller;
import jam.sort.DiskDaemon;
import jam.sort.NetDaemon;
import jam.sort.SortDaemon;
import jam.sort.SortException;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Class for data acquistion and run control. This class
 * <ul>
 * <li>starts and stops acquisition,
 * <li>begins and ends runs
 * </ul>
 * <p>
 * <b>begin run </b>:
 * </p>
 * <ul>
 * <li>starts acquisition</li>
 * <li>opens event file</li>
 * </ul>
 * <p>
 * <b>end run </b>:
 * </p>
 * <ul>
 * <li>stops acquisition</li>
 * <li>closes event file</li>
 * <li>writes out summary data file</li>
 * </ul>
 * 
 * @author Ken Swartz
 * @author <a href="mailto:dale@visser.name">Dale Visser </a>
 */
public class RunControl extends JDialog implements Controller {

	/**
	 * Indicates running to or from disk.
	 */
	private static final int DISK = 0;

	/**
	 * Indicates events being stored by front end.
	 */
	private static final int FRONT_END = 2;

	final static String EVENT_FILE_EXTENSION = ".evn";

	private final DataIO dataio;

	private final FrontEndCommunication vmeComm;

	private final MessageHandler console;

	private static final JamStatus status = JamStatus.instance();

	private final Frame jamMain = status.getFrame();

	private int device;

	/* daemon threads */
	private NetDaemon netDaemon;

	private DiskDaemon diskDaemon;

	private SortDaemon sortDaemon;

	/**
	 * event file information
	 */
	private String experimentName;

	private File dataPath;

	private File dataFile;

	/**
	 * histogram file information
	 */
	private File histFilePath;

	private File histFile;

	/**
	 * run Number, is append to experiment name to create event file
	 */
	private int runNumber;

	/**
	 * run Title
	 */
	private String runTitle;

	/**
	 * Are we currently in a run, saving event data
	 */
	private boolean runOn = false;

	private final JTextField textRunNumber, textRunTitle, textExptName;

	private final JCheckBox checkHistogramZero;

	private final JCheckBox zeroScalers;

	private static RunControl instance = null;

	static public RunControl getSingletonInstance() {
		if (instance == null) {
			instance = new RunControl(status.getFrame());
		}
		return instance;
	}

	/**
	 * Creates the run control dialog box.
	 * 
	 * @param jamMain
	 *            launching point of Jam application
	 * @param histogramControl
	 * @param scalerControl
	 *            dialog for reading and zeroing scalers
	 * @param vmeComm
	 *            object which sends and receives messages to/from the VME
	 *            computer
	 * @param dataio
	 *            object in control of reading/writing data to/from disk
	 * @param console
	 */
	private RunControl(Frame f) {
		super(f, "Run", false);
		console = status.getMessageHandler();
		vmeComm = status.getFrontEndCommunication();
		this.dataio = new HDFIO(jamMain, console);
		runNumber = 100;
		setResizable(false);
		setLocation(20, 50);
		setSize(400, 250);
		final Container cp = getContentPane();
		cp.setLayout(new BorderLayout(10, 0));
		/* Labels Panel */
		JPanel pLabels = new JPanel(new GridLayout(0, 1, 5, 5));
		pLabels.setBorder(new EmptyBorder(10, 10, 10, 0));
		cp.add(pLabels, BorderLayout.WEST);
		JLabel len = new JLabel("Experiment Name", JLabel.RIGHT);
		pLabels.add(len);
		JLabel lrn = new JLabel("Run", JLabel.RIGHT);
		pLabels.add(lrn);
		JLabel lt = new JLabel("Title", JLabel.RIGHT);
		pLabels.add(lt);
		JLabel lc = new JLabel("Zero on Begin?", JLabel.RIGHT);
		pLabels.add(lc);
		/* panel for text fields */
		final JPanel pCenter = new JPanel(new GridLayout(0, 1, 5, 5));
		pCenter.setBorder(new EmptyBorder(10, 0, 10, 10));
		cp.add(pCenter, BorderLayout.CENTER);
		final JPanel pExptName = new JPanel(new FlowLayout(FlowLayout.LEFT, 0,
				0));
		pCenter.add(pExptName);
		textExptName = new JTextField("");
		textExptName.setColumns(20);
		textExptName.setEditable(false);
		pExptName.add(textExptName);
		final JPanel pRunNumber = new JPanel(new FlowLayout(FlowLayout.LEFT, 0,
				0));
		pCenter.add(pRunNumber);
		textRunNumber = new JTextField("");
		textRunNumber.setColumns(3);
		textRunNumber.setText(Integer.toString(runNumber));
		pRunNumber.add(textRunNumber);
		final JPanel pRunTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0,
				0));
		pCenter.add(pRunTitle);
		textRunTitle = new JTextField("");
		textRunTitle.setColumns(40);
		pRunTitle.add(textRunTitle);
		/* Zero Panel */
		JPanel pZero = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, -2));
		checkHistogramZero = new JCheckBox("Histograms", true);
		pZero.add(checkHistogramZero);
		zeroScalers = new JCheckBox("Scalers", true);
		pZero.add(zeroScalers);
		pCenter.add(pZero);
		/* Panel for buttons */
		JPanel pButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		cp.add(pButtons, BorderLayout.SOUTH);
		JPanel pb = new JPanel(new GridLayout(1, 0, 50, 5));
		pButtons.add(pb);
		final JButton bbegin = new JButton(beginAction);
		pb.add(bbegin);
		final JButton bend = new JButton(endAction);
		pb.add(bend);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
	}

	private final Action endAction = new AbstractAction() {

		{
			putValue(Action.NAME, "End Run");
			putValue(Action.SHORT_DESCRIPTION, "Ends the current run.");
			final ClassLoader cl = ClassLoader.getSystemClassLoader();
			final ImageIcon icon = new ImageIcon(cl.getResource("jam/end.png"));
			putValue(Action.SMALL_ICON, icon);
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			endRun();
		}
	};

	private final Action beginAction = new AbstractAction() {

		{
			putValue(Action.NAME, "Begin Run");
			putValue(Action.SHORT_DESCRIPTION, "Begins the next run.");
			final ClassLoader cl = ClassLoader.getSystemClassLoader();
			final ImageIcon icon = new ImageIcon(cl
					.getResource("jam/begin.png"));
			putValue(Action.SMALL_ICON, icon);
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			runTitle = textRunTitle.getText().trim();
			boolean confirm = (JOptionPane.showConfirmDialog(RunControl.this,
					"Is this title OK? :\n" + runTitle,
					"Run Title Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
			if (confirm) {
				try {
					beginRun();
				} catch (SortException se) {
					console.errorOutln(se.getMessage());
				} catch (JamException je) {
					console.errorOutln(je.getMessage());
				}

			}
		}
	};

	/**
	 * Setup for online acquisition.
	 * 
	 * @see jam.SetupSortOn
	 * @param exptName
	 *            name of the current experiment
	 * @param datapath
	 *            path to event files
	 * @param histpath
	 *            path to HDF files
	 * @param sd
	 *            the sorter thread
	 * @param nd
	 *            the network communication thread
	 * @param dd
	 *            the storage thread
	 */
	public void setupOn(String exptName, File datapath, File histpath,
			SortDaemon sd, NetDaemon nd, DiskDaemon dd) {
		experimentName = exptName;
		dataPath = datapath;
		histFilePath = histpath;
		sortDaemon = sd;
		netDaemon = nd;
		nd.setEndRunAction(endAction);
		textExptName.setText(exptName);
		if (dd == null) {//case if front end is taking care of storing events
			device = FRONT_END;
		} else {
			diskDaemon = dd;
			device = DISK;
		}
		beginAction.setEnabled(true);
	}
	
	private synchronized boolean isRunOn(){
		return runOn;
	}
	
	private synchronized void setRunOn(boolean val){
		runOn=val;
	}

	/**
	 * Starts acquisition of data. Figure out if online or offline an run
	 * appropriate method.
	 * 
	 * @exception JamException
	 *                all exceptions given to <code>JamException</code> go to
	 *                the console
	 */
	public void startAcq() {
		netDaemon.setState(GoodThread.RUN);
		vmeComm.startAcquisition();
		// if we are in a run, display run number
		if (isRunOn()) {//runOn is true if the current state is a run
			status.setRunState(RunState.RUN_ON(runNumber));
			//see stopAcq() for reason for this next line.
			endAction.setEnabled(true);
			console.messageOutln("Started Acquisition, continuing Run #"
					+ runNumber);
		} else {//just viewing events, not running to disk
			status.setRunState(RunState.ACQ_ON);
			beginAction.setEnabled(false);//don't want to try to begin run
			// while going
			console
					.messageOutln("Started Acquisition...to begin a run, first stop acquisition.");
		}
	}

	/**
	 * Tells VME to stop acquisition, and suspends the net listener.
	 */
	public void stopAcq() {
		vmeComm.stopAcquisition();
		/*
		 * Commented out next line to see if this stops our problem of
		 * "leftover" buffers DWV 15 Nov 2001
		 */
		status.setRunState(RunState.ACQ_OFF);
		/*
		 * done to avoid "last buffer in this run becomes first and last buffer
		 * in next run" problem
		 */
		endAction.setEnabled(false);
		if (!isRunOn()) {//not running to disk
			beginAction.setEnabled(true);//since it was disabled during start
		}
		console.warningOutln("Stopped Acquisition...if you are doing a run, "
				+ "you will need to start again before clicking \"End Run\".");
	}

	/**
	 * flush the vme buffer
	 */
	public void flushAcq() {
		vmeComm.flush();
	}

	/**
	 * Begin taking data taking run.
	 * <OL>
	 * <LI>Get run number and title</LI>
	 * <LI>Open file</LI>
	 * <LI>Tell disk Daemon the file</LI>
	 * <LI>Tell disk Daemon to write header</LI>
	 * <LI>Start disk Daemon</LI>
	 * <LI>Tell net daemon to send events into pipe</LI>
	 * <LI>Tell vme to start</LI>
	 * </OL>
	 * 
	 * @exception JamException
	 *                all exceptions given to <code>JamException</code> go to
	 *                the console
	 */
	private void beginRun() throws JamException, SortException {
		try {//get run number and title
			runNumber = Integer.parseInt(textRunNumber.getText().trim());
			runTitle = textRunTitle.getText().trim();
			RunInfo.runNumber = runNumber;
			RunInfo.runTitle = runTitle;
			RunInfo.runStartTime = getTime();
		} catch (NumberFormatException nfe) {
			throw new JamException("Run number not an integer [RunControl]");
		}
		if (device == DISK) {//saving to disk
			final String dataFileName = experimentName + runNumber
					+ EVENT_FILE_EXTENSION;
			dataFile = new File(dataPath, dataFileName);
			if (dataFile.exists()) {// Do not allow file overwrite
				throw new JamException("Event file already exits, File: "
						+ dataFile.getPath()
						+ ", Jam Cannot overwrite. [RunControl]");
			}
			diskDaemon.openEventOutputFile(dataFile);
			diskDaemon.writeHeader();
		}
		sortDaemon.userBegin();
		if (checkHistogramZero.isSelected()) {// should we zero histograms
			Histogram.setZeroAll();
		}
		if (zeroScalers.isSelected()) {//should we zero scalers
			vmeComm.clearScalers();
		}
		if (device != FRONT_END) {//tell net daemon to write events to storage
			// daemon
			netDaemon.setWriter(true);
		}
		// enable end button, display run number
		endAction.setEnabled(true);
		beginAction.setEnabled(false);
		status.setRunState(RunState.RUN_ON(runNumber));
		if (device == DISK) {
			console.messageOutln("Began run " + runNumber
					+ ", events being written to file: " + dataFile.getPath());
		} else {
			console
					.messageOutln("Began run, events being written out be front end.");
		}
		setRunOn(true);
		netDaemon.setEmptyBefore(false);//fresh slate
		netDaemon.setState(GoodThread.RUN);
		vmeComm.startAcquisition();//VME start last because other thread have
		// higher priority
	}

	/**
	 * End a data taking run tell VME to end, which flushes buffer with a end of
	 * run marker When the storageDaemon gets end of run character, it will turn
	 * the netDaemon's eventWriter off which flushs and close event file.
	 * 
	 * sort calls back isEndRun when it sees the end of run marker and write out
	 * histogram, gates and scalers if requested
	 */
	private void endRun() {
		RunInfo.runEndTime = getTime();
		vmeComm.end(); //stop Acq. flush buffer
		vmeComm.readScalers(); //read scalers
		endAction.setEnabled(false); //toggle button states
		status.setRunState(RunState.ACQ_OFF);
		console.messageOutln("Ending run " + runNumber
				+ ", waiting for sorting to finish.");
		int numSeconds = 0;
		do {//wait for sort to catch up
			try {
				Thread.sleep(1000); //sleep 1 second
				numSeconds++;
				if (numSeconds % 3 == 0) {
					console
							.warningOutln("Waited "
									+ numSeconds
									+ " seconds for "
									+ "sorter and file writer to finish. Sending commands to "
									+ "front end again.");
					vmeComm.end();
					vmeComm.readScalers();
				}
			} catch (InterruptedException ie) {
				console.errorOutln(getClass().getName()
						+ ".endRun(), Error: Interrupted while"
						+ " waiting for sort to finish.");
			}
		} while (!sortDaemon.caughtUp() && !storageCaughtUp());
		diskDaemon.resetReachedRunEnd();
		netDaemon.setState(GoodThread.SUSPEND);
		sortDaemon.userEnd();
		// histogram file name constructed using run name and number
		final String histFileName = experimentName + runNumber + ".hdf";
		// only write a histogram file
		histFile = new File(histFilePath, histFileName);
		console.messageOutln("Sorting finished writing out histogram file: "
				+ histFile.getPath());
		dataio.writeFile(histFile);
		runNumber++;//increment run number
		textRunNumber.setText(Integer.toString(runNumber));
		setRunOn(false);
		beginAction.setEnabled(true);//set begin button state for next run
	}

	private boolean storageCaughtUp() {
		final boolean rval = device == FRONT_END ? true : diskDaemon
				.caughtUpOnline();
		return rval;
	}

	/**
	 * Called by sorter when it starts up. Nnot used for online data taking.
	 */
	public void atSortStart() {
		/* does nothing for on line */
	}

	/**
	 * Only here for the controller interface.
	 * 
	 * @deprecated
	 */
	public void atSortEnd() {
		/* Nothing needed here at the moment. */
	}

	/**
	 * Called by sorter for each new file not used for online data taking
	 */
	public boolean isSortNext() {
		/* does nothing for online */
		return true;
	}

	/**
	 * Method called back from sort package when we are done writing out the
	 * event data and have closed the event file.
	 * 
	 * @throws IllegalStateException
	 *             if the device is not an expected value
	 */
	public void atWriteEnd() {
		if (device != FRONT_END) {
			netDaemon.setWriter(false);
		}
		try {
			if (device == DISK) {
				diskDaemon.closeEventOutputFile();
				console.messageOutln("Event file closed " + dataFile.getPath());
			} else if (device == FRONT_END) {
				console.errorOutln(getClass().getName() + ".atWriteEnd()"
						+ " device=FRONT_END not implemented");
				// **** send message to indicate end of run file? ****
			} else {
				throw new IllegalStateException(
						"Expect device to be DISK or FRONT_END.");
			}
		} catch (SortException je) {
			console.errorOutln(je.getMessage());
		}
	}

	/**
	 * get current date and time
	 */
	private Date getTime() {
		return new java.util.Date();
	}
}