package jam.sort.control;

import static jam.global.GoodThread.State.STOP;
import static java.util.logging.Level.SEVERE;
import static javax.swing.SwingConstants.RIGHT;
import jam.comm.CommunicationsException;
import jam.global.JamException;
import jam.global.JamProperties;
import jam.global.PropertyKeys;
import jam.global.QuerySortMode;
import jam.global.SortMode;
import jam.sort.DiskDaemon;
import jam.sort.EventSizeMode;
import jam.sort.NetDaemon;
import jam.sort.RingBuffer;
import jam.sort.SortDaemon;
import jam.sort.SortException;
import jam.sort.SortRoutine;
import jam.ui.ConsoleLog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Class to setup online sorting. Loads a sort file and creates the daemons:
 * <ul>
 * <li>net</li>
 * <li>sort</li>
 * <li>tape</li>
 * </ul>
 * 
 * @author Ken Swartz
 * @author Dale Visser
 * @version 05 newest done 9-98
 * @see jam.sort.NetDaemon
 * @see jam.sort.AbstractStorageDaemon
 */
public final class SetupSortOn extends AbstractSetup {

	private static SetupSortOn instance = null;

	private static final Object monitor = new Object();

	/**
	 * Creates the only instance of this class.
	 * 
	 * @param console
	 *            the console to use
	 */
	public static void createInstance(final ConsoleLog console) {
		synchronized (monitor) {
			if (instance == null) {
				instance = new SetupSortOn(console);
			} else {
				throw new IllegalStateException("Object already created.");
			}
		}
	}

	/**
	 * Returns the only instance of this class.
	 * 
	 * @return the only instance of this class
	 */
	public static SetupSortOn getInstance() {
		synchronized (monitor) {
			if (instance == null) {
				throw new IllegalStateException("Object not created yet.");
			}
			return instance;
		}
	}

	private transient final AbstractButton bbrowseh, bbrowsed, bbrowsel;

	/* stuff for dialog box */
	private transient final AbstractButton cdisk = new JCheckBox(
			"Events to Disk", true);

	private transient final AbstractButton checkLock = new JCheckBox(
			"Setup Locked", false);

	private transient final AbstractButton clog = new JCheckBox("Log Commands",
			false);

	private transient final ConsoleLog consoleLog;

	private transient File dataFolder, histFolder, logDirectory;

	private transient DiskDaemon diskDaemon;

	/* strings of data entered */
	private transient String exptName;

	private transient final jam.comm.FrontEndCommunication frontEnd = jam.comm.VMECommunication
			.getSingletonInstance();

	private transient final String hostDataIP;

	private transient final int hostDataPort;

	private transient NetDaemon netDaemon;

	private transient final RunControl runControl;

	/* sorting classes */
	private transient SortDaemon sortDaemon;

	private transient final JTextField textExpName, textPathHist, textPathData,
			textPathLog;

	private SetupSortOn(ConsoleLog console) {
		super("Setup Online");
		initCheckLock();
		initDiskCheckbox();
		final int fileTextCols = 25;
		final String defaultName = JamProperties
				.getPropString(PropertyKeys.EXP_NAME);
		dataFolder = new File(JamProperties
				.getPropString(PropertyKeys.EVENT_OUTPATH));
		histFolder = new File(JamProperties
				.getPropString(PropertyKeys.HIST_PATH));
		logDirectory = new File(JamProperties
				.getPropString(PropertyKeys.LOG_PATH));
		hostDataIP = JamProperties.getPropString(PropertyKeys.HOST_DATA_IP);
		hostDataPort = JamProperties.getPropInt(PropertyKeys.HOST_DATA_P_RECV);

		runControl = RunControl.getSingletonInstance();
		consoleLog = console;
		dialog.setResizable(false);
		dialog.setLocation(20, 50);
		final java.awt.Container dcp = dialog.getContentPane();
		dcp.setLayout(new BorderLayout(5, 5));
		final int gap = 5;
		final JPanel pLabels = new JPanel(new GridLayout(0, 1, gap, gap));
		final int topInset = 10;
		final int leftInset = 10;
		final int noSpace = 0;
		pLabels
				.setBorder(new EmptyBorder(topInset, leftInset, noSpace,
						noSpace));
		dcp.add(pLabels, BorderLayout.WEST);
		final JLabel expName = new JLabel("Experiment Name", RIGHT);
		pLabels.add(expName);
		final JLabel lsc = new JLabel("Sort classpath", RIGHT);
		pLabels.add(lsc);
		final JLabel lscs = new JLabel("Selected sort classpath", RIGHT);
		pLabels.add(lscs);
		final JLabel lSortRoutine = new JLabel("Sort Routine", RIGHT);
		pLabels.add(lSortRoutine);
		final JLabel leis = new JLabel("Event input stream", RIGHT);
		pLabels.add(leis);
		final JLabel leos = new JLabel("Event output stream", RIGHT);
		pLabels.add(leos);
		final JLabel lhdfp = new JLabel("HDF path", RIGHT);
		pLabels.add(lhdfp);
		final JLabel lep = new JLabel("Event path", RIGHT);
		pLabels.add(lep);
		final JLabel llfp = new JLabel("Log file path", RIGHT);
		pLabels.add(llfp);
		/* blank label balances out the grid */
		final JLabel lssf = new JLabel(/*
										 * "Sort sample fraction", RIGHT
										 */);
		pLabels.add(lssf);
		/* Entries Panel */
		final JPanel pEntries = new JPanel(new GridLayout(0, 1, gap, gap));
		pEntries
				.setBorder(new EmptyBorder(topInset, noSpace, noSpace, noSpace));
		dcp.add(pEntries, BorderLayout.CENTER);
		textExpName = new JTextField(defaultName);
		textExpName
				.setToolTipText("Used to name data files. Only 20 characters get written to event files.");
		textExpName.setColumns(20);
		pEntries.add(textExpName);
		/* Radio buttons for path */
		final JPanel pradio = new JPanel(new FlowLayout(FlowLayout.CENTER,
				noSpace, noSpace));
		pEntries.add(pradio);
		final ButtonGroup pathType = new ButtonGroup();
		pathType.add(btnDefaultPath);
		pradio.add(btnDefaultPath);
		pathType.add(btnSpecifyPath);
		pradio.add(btnSpecifyPath);

		/* Class path text */
		pEntries.add(textSortPath);

		pEntries.add(sortChooser);
		pEntries.add(inChooser);
		/* Output stream classes */
		pEntries.add(outChooser);
		textPathHist = new JTextField(histFolder.getPath());
		textPathHist.setColumns(fileTextCols);
		textPathHist
				.setToolTipText("Path to save HDF summary files at the end of each run.");
		textPathHist.setEditable(true);
		pEntries.add(textPathHist);
		textPathData = new JTextField();
		textPathData.setColumns(fileTextCols);
		textPathData.setToolTipText("Path to save event data.");
		textPathData.setEditable(true);
		pEntries.add(textPathData);
		textPathLog = new JTextField();
		textPathLog.setColumns(fileTextCols);
		textPathLog.setToolTipText("Path to save the console log.");
		textPathLog.setEditable(true);
		pEntries.add(textPathLog);
		final JPanel pInterval = new JPanel(new GridLayout(1, 2, 40, 0));
		pEntries.add(pInterval);
		pInterval.add(cdisk);
		clog.setSelected(true);
		/* Browse panel */
		final JPanel pBrowse = new JPanel(new GridLayout(0, 1, 5, 5));
		pBrowse.setBorder(new EmptyBorder(10, 0, 0, 10));
		dcp.add(pBrowse, BorderLayout.EAST);
		final Dimension dummyDim = new Dimension(10, 10);
		pBrowse.add(new Box.Filler(dummyDim, dummyDim, dummyDim));
		pBrowse.add(new Box.Filler(dummyDim, dummyDim, dummyDim));
		pBrowse.add(bbrowsef);
		pBrowse.add(new Box.Filler(dummyDim, dummyDim, dummyDim));
		pBrowse.add(new Box.Filler(dummyDim, dummyDim, dummyDim));
		pBrowse.add(new Box.Filler(dummyDim, dummyDim, dummyDim));
		bbrowseh = new PathBrowseButton(histFolder, textPathHist);
		pBrowse.add(bbrowseh);
		bbrowsed = new PathBrowseButton(dataFolder, textPathData);
		pBrowse.add(bbrowsed);
		bbrowsel = new PathBrowseButton(logDirectory, textPathLog);
		pBrowse.add(bbrowsel);
		pBrowse.add(new Box.Filler(dummyDim, dummyDim, dummyDim));
		/* panel for buttons */
		final JPanel pbutton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		dcp.add(pbutton, BorderLayout.SOUTH);
		final JPanel pBottom = new JPanel(new GridLayout(1, 4, 5, 5));
		pbutton.add(pBottom);
		pBottom.add(bok);
		pBottom.add(bapply);
		pBottom.add(new javax.swing.JButton(new jam.ui.WindowCancelAction(
				dialog)));
		pBottom.add(checkLock);

		dialog
				.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		dialog.pack();
	}

	/**
	 * Check a diretory exits
	 * 
	 * @param path
	 *            of directory
	 * @return true if directory exist
	 */
	private boolean checkDir(final File path) {
		boolean exists;
		if (path.exists() && path.isDirectory()) {
			exists = true;
		} else {
			if (path.isDirectory()) {
				exists = false;
			} else {
				final boolean confirm = (JOptionPane.showConfirmDialog(dialog,
						"Create Directory :\n" + path.getAbsolutePath(),
						"Directory does not Exist", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
				if (confirm) {
					path.mkdir();
					exists = true;
				} else {
					exists = false;
				}
			}
		}
		return exists;
	}

	private boolean checkDirectories() {

		histFolder = new File(textPathHist.getText());
		dataFolder = new File(textPathData.getText());
		logDirectory = new File(textPathLog.getText());

		return (checkDir(histFolder) && checkDir(dataFolder) && checkDir(logDirectory));

	}

	protected void doApply(final boolean dispose) {
		try {
			if (!loadNames()) {
				LOGGER.info("Cannot load sort as not all directories exist.");
				return;
			}

			/* lock setup so fields cant be edited */
			if (STATUS.canSetup()) {

				setup(dispose);
			} else {
				throw new JamException("Can't setup sorting, mode locked ");
			}
		} catch (JamException je) {
			LOGGER.log(SEVERE, je.getMessage(), je);
		} catch (Exception e) {
			LOGGER.log(SEVERE, e.getMessage(), e);
			resetAcq(true);
			lockMode(false);
		}
	}

	private void initCheckLock() {
		checkLock.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent itemEvent) {
				if (!checkLock.isSelected()) {
					try {
						/* kill daemons, clear data areas */
						resetAcq(true);
						/* unlock sort mode */
						lockMode(false);
						consoleLog.closeLogFile();
					} catch (Exception e) {
						LOGGER.log(SEVERE, e.getMessage(), e);
					}
				}
			}
		});
		checkLock.setEnabled(false);
	}

	private void initDiskCheckbox() {
		cdisk.setToolTipText("Send events to disk.");
		cdisk.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent itemEvent) {
				boolean store = cdisk.isSelected();
				if (!store) {
					final boolean oops = JOptionPane.showConfirmDialog(dialog,
							"De-selecting this checkbox means Jam won't store events to disk.\n"
									+ "Is this what you really want?",
							"Event Storage Disabled",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION;
					if (oops) {
						cdisk.setSelected(true);
						store = true;
					}
				}
				textPathData.setEnabled(store);
				bbrowsed.setEnabled(store);
			}
		});
	}

	/**
	 * Save the names of the experiment, the sort file and the event and
	 * histogram directories.
	 */
	private boolean loadNames() {
		exptName = textExpName.getText().trim();
		// Check directories exist
		return checkDirectories();

	}

	protected void lockMode(final boolean lock) {
		final boolean notlock = !lock;
		checkLock.setEnabled(lock);
		textExpName.setEnabled(notlock);

		inChooser.setEnabled(notlock);
		outChooser.setEnabled(notlock);
		sortChooser.setEnabled(notlock);
		textExpName.setEditable(notlock);

		textPathHist.setEditable(notlock);
		textPathData.setEditable(notlock);
		textPathLog.setEditable(notlock);
		textPathHist.setEnabled(notlock);
		textPathData.setEnabled(notlock);
		textPathLog.setEnabled(notlock);

		cdisk.setEnabled(notlock);
		bok.setEnabled(notlock);
		bapply.setEnabled(notlock);
		bbrowseh.setEnabled(notlock);
		bbrowsel.setEnabled(notlock);
		bbrowsed.setEnabled(notlock);
		btnSpecifyPath.setEnabled(notlock);
		btnDefaultPath.setEnabled(notlock);
		final QuerySortMode sortMode = notlock ? SortMode.NO_SORT : (cdisk
				.isSelected() ? SortMode.ONLINE_DISK : SortMode.ON_NO_DISK);
		final String name;
		final SortRoutine sortRoutine = sortChooser.getSortRoutine();
		if (sortRoutine == null) {
			// instead of conditional assign to avoid PMD warning
			name = "No Data";
		} else {
			name = sortRoutine.getClass().getName();
		}
		STATUS.setSortMode(sortMode, name);
		bbrowsef.setEnabled(notlock && btnSpecifyPath.isSelected());
		checkLock.setSelected(lock);
	}

	/*
	 * non-javadoc: reset online data Aquisition kill all daemons closes data
	 * network clear all data areas Histograms, Gates, Scalers, Monitors,
	 * Parameters
	 */
	private void resetAcq(final boolean killSort) {
		if (diskDaemon != null) {
			diskDaemon.setState(STOP);
		}
		if (sortDaemon != null) {
			sortDaemon.setSorter(null);
			// make sure sorter Daemon does not have a handle to sortClass
			sortDaemon.setState(STOP);
			// this line should be sufficient but above line is needed
		}
		if (netDaemon != null) {
			netDaemon.setState(STOP);
			netDaemon.closeNet();
		}
		if (killSort) {
			sortChooser.forgetSortRoutine();
		}
		jam.data.DataBase.getInstance().clearAllLists();
		jam.global.Broadcaster.getSingletonInstance().broadcast(
				jam.global.BroadcastEvent.Command.HISTOGRAM_NEW);
	}

	/**
	 * @param dispose
	 * @throws JamException
	 * @throws IOException
	 * @throws SortException
	 */
	private void setup(final boolean dispose) throws CommunicationsException,
			JamException, IOException, SortException {
		if (clog.isSelected()) { // if needed start logging to file
			final File logPathTry = new File(textPathLog.getText(), exptName);
			final String logFile = consoleLog.setLogFileName(logPathTry
					.getCanonicalPath());
			LOGGER.info("Logging to file: " + logFile);
			consoleLog.setLogFileOn(true);
		} else {
			consoleLog.setLogFileOn(false);
		}
		consoleLog
				.messageOutln("Setup Online Data Acquisition,  Experiment Name: "
						+ exptName);
		/* Kill all existing Daemons and clear data areas */
		resetAcq(false);
		sortChooser.loadSorter(btnSpecifyPath.isSelected()); // load
		// sorting
		// routine
		final SortRoutine sortRoutine = sortChooser.getSortRoutine();
		if (sortRoutine != null) {
			lockMode(true);
			setupSort(); // create daemons
			consoleLog.messageOutln("Loaded "
					+ sortRoutine.getClass().getName() + ", "
					+ inStream.getClass().getName() + " and "
					+ outStream.getClass().getName());
			consoleLog
					.messageOutln("Communications and processing daemons successfully initiated.");
			if (sortRoutine.getEventSizeMode() == EventSizeMode.CNAF) {
				setupCamac(); // set the camac crate
				consoleLog.messageOutln("CAMAC command lists sent.");
			} else if (sortRoutine.getEventSizeMode() == EventSizeMode.VME_MAP) {
				setupVMEmap();
				consoleLog.messageOutln("VME map sent.");
			}
		}
		selectFirstSortHistogram();
		if (dispose) {
			dialog.dispose();
		}
	}

	private void setupCamac() throws IOException, CommunicationsException {
		frontEnd.setupAcquisition();
		frontEnd.setupCamac(sortChooser.getSortRoutine().getCamacCommands());
	}

	protected void setupSort() throws jam.sort.SortException, JamException {
		initializeSorter();
		/* interprocess buffering between daemons */
		final RingBuffer sortingRing = new RingBuffer();
		// if disk not selected than storage ring is made in "null/empty" state
		final RingBuffer storageRing = new RingBuffer(!cdisk.isSelected());
		/* typical setup of event streams */
		try { // create new event input stream class
			inStream = (jam.sort.stream.AbstractEventInputStream) ((Class) inChooser
					.getSelectedItem()).newInstance();
			inStream.setConsoleExists(true);
		} catch (InstantiationException ie) {
			final String msg = getClass().getName()
					+ ": can't instantiate EventInputStream: "
					+ inChooser.getSelectedItem();
			LOGGER.log(SEVERE, msg, ie);
			throw new JamException(msg, ie);
		} catch (IllegalAccessException iae) {
			throw new JamException(getClass().getName()
					+ ": illegal access to EventInputStream: "
					+ inChooser.getSelectedItem(), iae);
		}
		final SortRoutine sortRoutine = sortChooser.getSortRoutine();
		try { // create new event input stream class
			outStream = (jam.sort.stream.AbstractEventOutputStream) ((Class) outChooser
					.getSelectedItem()).newInstance();
			outStream.setEventSize(sortRoutine.getEventSize());
		} catch (InstantiationException ie) {
			final String msg = getClass().getName()
					+ ": can't instantiate EventOutputStream class: "
					+ outChooser.getSelectedItem();
			LOGGER.log(SEVERE, msg, ie);
			throw new JamException(msg, ie);
		} catch (IllegalAccessException iae) {
			final String msg = getClass().getName()
					+ ": illegal access to EventOutputStream class: "
					+ outChooser.getSelectedItem();
			LOGGER.log(SEVERE, msg, iae);
			throw new JamException(msg, iae);
		}
		// create sorter daemon
		sortDaemon = new SortDaemon(runControl);
		final boolean useDisk = cdisk.isSelected();
		sortDaemon.setup(inStream, sortRoutine.getEventSize());
		sortDaemon.setRingBuffer(sortingRing);
		sortDaemon.setSorter(sortRoutine);
		// create storage daemon
		if (cdisk.isSelected()) { // don't create storage daemon otherwise
			diskDaemon = new DiskDaemon(runControl);
			diskDaemon.setupOn(inStream, outStream);
			diskDaemon.setRingBuffer(storageRing);
		}
		/* Create the net daemon. */
		netDaemon = new NetDaemon(sortingRing, storageRing, hostDataIP,
				hostDataPort);

		/* Tell control about everything. */
		runControl.setupOn(exptName, dataFolder, histFolder, sortDaemon,
				netDaemon, diskDaemon);
		/* Tell the status dialog. */
		DisplayCounters.getSingletonInstance().setupOn(netDaemon, sortDaemon,
				diskDaemon);
		/* Startup the daemons. */
		if (useDisk) {
			diskDaemon.start();
		}
		sortDaemon.start();
		netDaemon.start();
	}

	private void setupVMEmap() throws CommunicationsException {
		frontEnd.setupAcquisition();
		final jam.sort.VME_Map map = sortChooser.getSortRoutine().getVMEmap();
		frontEnd.setupVMEmap(map);
		frontEnd.sendScalerInterval(map.getScalerInterval());
	}
}
