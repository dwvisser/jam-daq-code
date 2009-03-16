package jam.sort.control;

import jam.comm.FrontEndCommunication;
import jam.comm.ScalerCommunication;
import jam.data.Warehouse;
import jam.global.JamException;
import jam.global.JamStatus;
import jam.global.RunInfo;
import jam.global.RunState;
import jam.global.GoodThread.State;
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
import java.io.File;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.google.inject.Inject;
import com.google.inject.Singleton;

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
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser </a>
 */
@Singleton
public final class RunControl extends JDialog implements Controller,
		RunController {

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

	private static final Logger LOGGER = Logger.getLogger(RunControl.class
			.getPackage().getName());

	private transient final JamStatus status;

	private transient final Begin begin;

	private transient final JCheckBox cHistZero = new JCheckBox("Histograms",
			true);;

	private transient File dataPath, histPath;

	private transient Device device;

	private transient DiskDaemon diskDaemon;

	private transient final End end;

	/* daemon threads */
	private transient NetDaemon netDaemon;

	/**
	 * Are we currently in a run, saving event data.
	 */
	private boolean runningOnline = false;

	private transient SortDaemon sortDaemon;

	private transient final JTextField tRunNumber, textRunTitle, textExptName;

	private transient final FrontEndCommunication frontEnd;

	private transient final ScalerCommunication scaler;

	private transient final JCheckBox zeroScalers;

	private transient final HDFIO hdfio;

	private transient final Object syncObject = new Object();

	/**
	 * Creates the run control dialog box.
	 * 
	 * @param frame
	 *            parent frame
	 */
	@Inject
	protected RunControl(final Frame frame, final JamStatus status,
			final HDFIO hdfio, final FrontEndCommunication frontEnd,
			final ScalerCommunication scaler) {
		super(frame, "Run", false);
		this.hdfio = hdfio;
		this.status = status;
		this.frontEnd = frontEnd;
		this.scaler = scaler;
		RunInfo.getInstance().runNumber = 100;
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
		tRunNumber.setText(Integer.toString(RunInfo.getInstance().runNumber));
		pRunNumber.add(tRunNumber);
		final JPanel pRunTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0,
				0));
		pCenter.add(pRunTitle);
		textRunTitle = new JTextField("");
		textRunTitle.setColumns(40);
		pRunTitle.add(textRunTitle);
		/* Zero Panel */
		final JPanel pZero = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, -2));
		pZero.add(cHistZero);
		zeroScalers = new JCheckBox("Scalers", true);
		pZero.add(zeroScalers);
		pCenter.add(pZero);
		/* Panel for buttons */
		final JPanel pButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
		contents.add(pButtons, BorderLayout.SOUTH);
		final JPanel pGrid = new JPanel(new GridLayout(1, 0, 50, 5));
		pButtons.add(pGrid);
		begin = new Begin(this, this, textRunTitle);
		final JButton bbegin = new JButton(begin);
		pGrid.add(bbegin);
		end = new End(this);
		final JButton bend = new JButton(end);
		pGrid.add(bend);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
	}

	/**
	 * Only here for the controller interface.
	 */
	public void atSortEnd() {
		/* Nothing needed here at the moment. */
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
				final File dataFile = diskDaemon.getEventOutputFile();
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
	public void beginRun() throws JamException, SortException {
		try {// get run number and title
			RunInfo.getInstance().runNumber = Integer.parseInt(tRunNumber
					.getText().trim());
			RunInfo.getInstance().runTitle = textRunTitle.getText().trim();
			RunInfo.getInstance().runStartTime = new Date();
		} catch (NumberFormatException nfe) {
			throw new JamException("Run number not an integer.", nfe);
		}
		if (device == Device.DISK) {// saving to disk
			final String EVENT_EXT = ".evn";
			final String dataFileName = RunInfo.getInstance().experimentName
					+ RunInfo.getInstance().runNumber + EVENT_EXT;
			final File dataFile = new File(dataPath, dataFileName);
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
			jam.data.AbstractHistogram.setZeroAll();
		}
		if (zeroScalers.isSelected()) {// should we zero scalers
			scaler.clearScalers();
		}
		if (device != Device.FRONT_END) {
			// tell net daemon to write events to storage daemon
			netDaemon.setWriter(true);
		}
		// enable end button, display run number
		end.setEnabled(true);
		begin.setEnabled(false);
		setLockControls(true);
		status.setRunState(RunState.runOnline(RunInfo.getInstance().runNumber));
		if (device == Device.DISK) {
			LOGGER.info("Began run " + RunInfo.getInstance().runNumber
					+ ", events being written to file: "
					+ diskDaemon.getEventOutputFile().getPath());
		} else {
			LOGGER.info("Began run, events being written out be front end.");
		}
		setRunOn(true);
		netDaemon.setEmptyBefore(false);// fresh slate
		netDaemon.setState(State.RUN);
		frontEnd.startAcquisition();// VME start last because other thread have

		// higher priority
	}

	/**
	 * End a data taking run tell VME to end, which flushes buffer with a end of
	 * run marker When the storageDaemon gets end of run character, it will turn
	 * the netDaemon's eventWriter off which flushes and close event file.
	 * 
	 * sort calls back isEndRun when it sees the end of run marker and write out
	 * histogram, gates and scalers if requested
	 */
	public void endRun() {
		RunInfo.getInstance().runEndTime = new Date();
		frontEnd.end(); // stop acquisition, flush buffer
		scaler.readScalers(); // read scalers

		status.setRunState(RunState.ACQ_OFF);
		LOGGER.info("Ending run " + RunInfo.getInstance().runNumber
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
					frontEnd.end();
					scaler.readScalers();
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
		final String histFileName = RunInfo.getInstance().experimentName
				+ RunInfo.getInstance().runNumber + ".hdf";
		// only write a histogram file
		final File histFile = new File(histPath, histFileName);
		LOGGER.info("Sorting finished writing out histogram file: "
				+ histFile.getPath());
		hdfio
				.writeFile(histFile, Warehouse.getSortGroupGetter()
						.getSortGroup());
		RunInfo.getInstance().runNumber++;// increment run number
		tRunNumber.setText(Integer.toString(RunInfo.getInstance().runNumber));
		setRunOn(false);
		end.setEnabled(false); // toggle button states
		begin.setEnabled(true);// set begin button state for next run
		setLockControls(false);
	}

	/**
	 * flush the VME buffer
	 */
	public void flushAcq() {
		frontEnd.flush();
	}

	/**
	 * 
	 * @return whether Jam is in a run, i.e., saving event data to disk
	 */
	private boolean isRunOn() {
		synchronized (this.syncObject) {
			return this.runningOnline;
		}
	}

	/**
	 * Only called by beginRun (value=true) and endRun(value=false).
	 * 
	 * @param value
	 *            whether Jam is in a run, i.e., saving event data to disk
	 */
	private void setRunOn(final boolean value) {
		synchronized (this.syncObject) {
			this.runningOnline = value;
		}
	}

	private void setLockControls(final boolean state) {
		final boolean enable = !state;
		tRunNumber.setEditable(enable);
		textRunTitle.setEditable(enable);
		cHistZero.setEnabled(enable);
		zeroScalers.setEnabled(enable);
	}

	/**
	 * Setup for online acquisition.
	 * 
	 * @see jam.sort.control.SetupSortOn
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
		RunInfo.getInstance().experimentName = name;
		dataPath = datapath;
		histPath = histpath;
		sortDaemon = sortD;
		netDaemon = netD;
		netD.setEndRunAction(end);
		textExptName.setText(name);
		if (diskD == null) {// case if front end is taking care of storing
			// events
			device = Device.FRONT_END;
		} else {
			diskDaemon = diskD;
			device = Device.DISK;
		}
		begin.setEnabled(true);
	}

	/**
	 * Starts acquisition of data. Figure out if online or offline an run
	 * appropriate method.
	 */
	public void startAcq() {

		netDaemon.setState(State.RUN);
		frontEnd.startAcquisition();
		// if we are in a run, display run number
		if (isRunOn()) {// runOn is true if the current state is a run
			status.setRunState(RunState
					.runOnline(RunInfo.getInstance().runNumber));
			// see stopAcq() for reason for this next line.
			end.setEnabled(true);
			LOGGER.info("Started Acquisition, continuing Run #"
					+ RunInfo.getInstance().runNumber);
		} else {// just viewing events, not running to disk
			status.setRunState(RunState.ACQ_ON);
			begin.setEnabled(false);// don't want to try to begin run
			// while going
			LOGGER
					.info("Started Acquisition...to begin a run, first stop acquisition.");
		}
	}

	/**
	 * Tells VME to stop acquisition, and suspends the net listener.
	 */
	public void stopAcq() {
		this.frontEnd.stopAcquisition();
		this.status.setRunState(RunState.ACQ_OFF);

		/*
		 * done to avoid "last buffer in this run becomes first and last buffer
		 * in next run" problem
		 */
		this.end.setEnabled(false);

		if (this.isRunOn()) {
			LOGGER
					.info("Stopped acquisition during a run: you will need to start acquisition again before you can end the run.");
		} else {
			/*
			 * If not running to disk, "Begin" was disabled and needs to be
			 * re-enabled.
			 */
			this.begin.setEnabled(true);
		}
	}

	private boolean storageCaughtUp() {
		return device == Device.FRONT_END ? true : diskDaemon.caughtUpOnline();
	}
}
