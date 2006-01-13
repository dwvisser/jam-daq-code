package jam;

import jam.global.JamStatus;
import jam.global.RunInfo;
import jam.global.GoodThread.State;
import jam.io.DataIO;
import jam.sort.DiskDaemon;
import jam.sort.NetDaemon;
import jam.sort.SortDaemon;
import jam.sort.SortException;
import jam.ui.Icons;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
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
public class RunControl extends JDialog implements jam.sort.Controller {

	private static final Logger LOGGER = Logger.getLogger("jam");

	private static enum Device {
		/**
		 * Indicates running to or from disk.
		 */
		DISK,

		/**
		 * Indicates events being stored by front end.
		 */
		FRONT_END
	}

	final static String EVENT_EXT = ".evn";

	private static RunControl instance = null;

	private static final JamStatus STATUS = JamStatus.getSingletonInstance();

	/**
	 * @return the only instance of this class
	 */
	static public RunControl getSingletonInstance() {
		if (instance == null) {
			instance = new RunControl(STATUS.getFrame());
		}
		return instance;
	}

	private transient final Action beginAction = new AbstractAction() {
		{
			putValue(Action.NAME, "Begin Run");
			putValue(Action.SHORT_DESCRIPTION, "Begins the next run.");
			putValue(Action.SMALL_ICON, Icons.getInstance().BEGIN);
			setEnabled(false);
		}

		public void actionPerformed(final ActionEvent event) {
			runTitle = textRunTitle.getText().trim();
			final boolean confirm = (JOptionPane.showConfirmDialog(
					RunControl.this, "Is this title OK? :\n" + runTitle,
					"Run Title Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
			if (confirm) {
				try {
					beginRun();
				} catch (SortException se) {
					LOGGER.log(Level.SEVERE, se.getMessage(), se);
				} catch (JamException je) {
					LOGGER.log(Level.SEVERE, je.getMessage(), je);
				}

			}
		}
	};

	private transient final JCheckBox cHistZero = new JCheckBox("Histograms",
			true);

	private transient File dataFile, dataPath;

	private transient final DataIO dataio;

	private transient Device device;

	private transient DiskDaemon diskDaemon;

	private transient final Action endAction = new AbstractAction() {
		{
			putValue(Action.NAME, "End Run");
			putValue(Action.SHORT_DESCRIPTION, "Ends the current run.");
			putValue(Action.SMALL_ICON, Icons.getInstance().END);
			setEnabled(false);
		}

		public void actionPerformed(final ActionEvent event) {
			endRun();
		}
	};

	/**
	 * event file information
	 */
	private transient String exptName;

	/**
	 * histogram file information
	 */
	private transient File histFilePath;

	/* daemon threads */
	private transient NetDaemon netDaemon;

	/**
	 * run Number, is append to experiment name to create event file
	 */
	private transient int runNumber;

	/**
	 * Are we currently in a run, saving event data
	 */
	private boolean runOn = false;

	/**
	 * run Title
	 */
	private transient String runTitle;

	private transient SortDaemon sortDaemon;

	private transient final JTextField tRunNumber, textRunTitle, textExptName;

	private transient final FrontEndCommunication vmeComm;

	private transient final JCheckBox zeroScalers;

	/**
	 * Creates the run control dialog box.
	 * 
	 * @param frame
	 *            parent frame
	 */
	private RunControl(Frame frame) {
		super(frame, "Run", false);
		vmeComm = STATUS.getFrontEndCommunication();
		final Frame jamMain = STATUS.getFrame();
		this.dataio = new jam.io.hdf.HDFIO(jamMain);
		runNumber = 100;
		setResizable(false);
		setLocation(20, 50);
		setSize(400, 250);
		final Container contents = getContentPane();
		contents.setLayout(new BorderLayout(10, 0));
		/* Labels Panel */
		final JPanel pLabels = new JPanel(new GridLayout(0, 1, 5, 5));
		pLabels.setBorder(new EmptyBorder(10, 10, 10, 0));
		contents.add(pLabels, BorderLayout.WEST);
		final JLabel len = new JLabel("Experiment Name", SwingConstants.RIGHT);
		pLabels.add(len);
		final JLabel lrn = new JLabel("Run", SwingConstants.RIGHT);
		pLabels.add(lrn);
		final JLabel lTitle = new JLabel("Title", SwingConstants.RIGHT);
		pLabels.add(lTitle);
		final JLabel lCheck = new JLabel("Zero on Begin?", SwingConstants.RIGHT);
		pLabels.add(lCheck);
		/* panel for text fields */
		final JPanel pCenter = new JPanel(new GridLayout(0, 1, 5, 5));
		pCenter.setBorder(new EmptyBorder(10, 0, 10, 10));
		contents.add(pCenter, BorderLayout.CENTER);
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
		tRunNumber = new JTextField("");
		tRunNumber.setColumns(3);
		tRunNumber.setText(Integer.toString(runNumber));
		pRunNumber.add(tRunNumber);
		final JPanel pRunTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0,
				0));
		pCenter.add(pRunTitle);
		textRunTitle = new JTextField("");
		textRunTitle.setColumns(40);
		pRunTitle.add(textRunTitle);
		/* Zero Panel */
		JPanel pZero = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, -2));
		pZero.add(cHistZero);
		zeroScalers = new JCheckBox("Scalers", true);
		pZero.add(zeroScalers);
		pCenter.add(pZero);
		/* Panel for buttons */
		final JPanel pButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		contents.add(pButtons, BorderLayout.SOUTH);
		final JPanel pGrid = new JPanel(new GridLayout(1, 0, 50, 5));
		pButtons.add(pGrid);
		final JButton bbegin = new JButton(beginAction);
		pGrid.add(bbegin);
		final JButton bend = new JButton(endAction);
		pGrid.add(bend);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
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
	 * Called by sorter when it starts up. Nnot used for online data taking.
	 */
	public void atSortStart() {
		/* does nothing for on line */
	}

	/**
	 * Method called back from sort package when we are done writing out the
	 * event data and have closed the event file.
	 * 
	 * @throws IllegalStateException
	 *             if the device is not an expected value
	 */
	public void atWriteEnd() {
		if (device != Device.FRONT_END) {
			netDaemon.setWriter(false);
		}
		try {
			if (device == Device.DISK) {
				diskDaemon.closeEventOutputFile();
				LOGGER.info("Event file closed " + dataFile.getPath());
			} else if (device == Device.FRONT_END) {
				LOGGER.severe(getClass().getName() + ".atWriteEnd()"
						+ " device=FRONT_END not implemented");
				// **** send message to indicate end of run file? ****
			} else {
				throw new IllegalStateException(
						"Expect device to be DISK or FRONT_END.");
			}
		} catch (SortException je) {
			LOGGER.log(Level.SEVERE, je.getMessage(), je);
		}
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
	 * @throws SortException
	 *             if there's a problem while sorting
	 */
	private void beginRun() throws JamException, SortException {
		try {// get run number and title
			runNumber = Integer.parseInt(tRunNumber.getText().trim());
			runTitle = textRunTitle.getText().trim();
			RunInfo.runNumber = runNumber;
			RunInfo.runTitle = runTitle;
			RunInfo.runStartTime = new Date();
		} catch (NumberFormatException nfe) {
			throw new JamException("Run number not an integer [RunControl]");
		}
		if (device == Device.DISK) {// saving to disk
			final String dataFileName = exptName + runNumber + EVENT_EXT;
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
		if (cHistZero.isSelected()) {// should we zero histograms
			jam.data.Histogram.setZeroAll();
		}
		if (zeroScalers.isSelected()) {// should we zero scalers
			vmeComm.clearScalers();
		}
		if (device != Device.FRONT_END) {
			// tell net daemon to write events to storage daemon
			netDaemon.setWriter(true);
		}
		// enable end button, display run number
		endAction.setEnabled(true);
		beginAction.setEnabled(false);
		STATUS.setRunState(RunState.runOnline(runNumber));
		if (device == Device.DISK) {
			LOGGER.info("Began run " + runNumber
					+ ", events being written to file: " + dataFile.getPath());
		} else {
			LOGGER.info("Began run, events being written out be front end.");
		}
		setRunOn(true);
		netDaemon.setEmptyBefore(false);// fresh slate
		netDaemon.setState(State.RUN);
		vmeComm.startAcquisition();// VME start last because other thread have
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
		RunInfo.runEndTime = new Date();
		vmeComm.end(); // stop Acq. flush buffer
		vmeComm.readScalers(); // read scalers
		endAction.setEnabled(false); // toggle button states
		STATUS.setRunState(RunState.ACQ_OFF);
		LOGGER.info("Ending run " + runNumber
				+ ", waiting for sorting to finish.");
		int numSeconds = 0;
		do {// wait for sort to catch up
			try {
				Thread.sleep(1000); // sleep 1 second
				numSeconds++;
				if (numSeconds % 3 == 0) {
					LOGGER
							.warning("Waited "
									+ numSeconds
									+ " seconds for "
									+ "sorter and file writer to finish. Sending commands to "
									+ "front end again.");
					vmeComm.end();
					vmeComm.readScalers();
				}
			} catch (InterruptedException ie) {
				LOGGER.log(Level.SEVERE, getClass().getName()
						+ ".endRun(), Error: Interrupted while"
						+ " waiting for sort to finish.", ie);
			}
		} while (!sortDaemon.caughtUp() && !storageCaughtUp());
		diskDaemon.resetReachedRunEnd();
		netDaemon.setState(State.SUSPEND);
		sortDaemon.userEnd();
		// histogram file name constructed using run name and number
		final String histFileName = exptName + runNumber + ".hdf";
		// only write a histogram file
		final File histFile = new File(histFilePath, histFileName);
		LOGGER.info("Sorting finished writing out histogram file: "
				+ histFile.getPath());
		dataio.writeFile(histFile, jam.data.Group.getSortGroup());
		runNumber++;// increment run number
		tRunNumber.setText(Integer.toString(runNumber));
		setRunOn(false);
		beginAction.setEnabled(true);// set begin button state for next run
	}

	/**
	 * flush the vme buffer
	 */
	public void flushAcq() {
		vmeComm.flush();
	}

	private boolean isRunOn() {
		synchronized (this) {
			return runOn;
		}
	}

	private void setRunOn(final boolean val) {
		synchronized (this) {
			runOn = val;
		}
	}

	/**
	 * Setup for online acquisition.
	 * 
	 * @see jam.SetupSortOn
	 * @param name
	 *            name of the current experiment
	 * @param datapath
	 *            path to event files
	 * @param histpath
	 *            path to HDF files
	 * @param sortD
	 *            the sorter thread
	 * @param netD
	 *            the network communication thread
	 * @param diskD
	 *            the storage thread
	 */
	public void setupOn(final String name, final File datapath,
			final File histpath, final SortDaemon sortD, final NetDaemon netD,
			final DiskDaemon diskD) {
		exptName = name;
		dataPath = datapath;
		histFilePath = histpath;
		sortDaemon = sortD;
		netDaemon = netD;
		netD.setEndRunAction(endAction);
		textExptName.setText(name);
		if (diskD == null) {// case if front end is taking care of storing
			// events
			device = Device.FRONT_END;
		} else {
			diskDaemon = diskD;
			device = Device.DISK;
		}
		beginAction.setEnabled(true);
	}

	/**
	 * Starts acquisition of data. Figure out if online or offline an run
	 * appropriate method.
	 */
	public void startAcq() {
		netDaemon.setState(State.RUN);
		vmeComm.startAcquisition();
		// if we are in a run, display run number
		if (isRunOn()) {// runOn is true if the current state is a run
			STATUS.setRunState(RunState.runOnline(runNumber));
			// see stopAcq() for reason for this next line.
			endAction.setEnabled(true);
			LOGGER.info("Started Acquisition, continuing Run #" + runNumber);
		} else {// just viewing events, not running to disk
			STATUS.setRunState(RunState.ACQ_ON);
			beginAction.setEnabled(false);// don't want to try to begin run
			// while going
			LOGGER.info("Started Acquisition...to begin a run, first stop acquisition.");
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
		STATUS.setRunState(RunState.ACQ_OFF);
		/*
		 * done to avoid "last buffer in this run becomes first and last buffer
		 * in next run" problem
		 */
		endAction.setEnabled(false);
		if (!isRunOn()) {// not running to disk
			beginAction.setEnabled(true);// since it was disabled during
			// start
		}
		LOGGER.warning("Stopped Acquisition...if you are doing a run, "
				+ "you will need to start again before clicking \"End Run\".");
	}

	private boolean storageCaughtUp() {
		final boolean rval = device == Device.FRONT_END ? true : diskDaemon
				.caughtUpOnline();
		return rval;
	}
}
