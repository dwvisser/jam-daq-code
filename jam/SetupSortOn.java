package jam;

import jam.data.DataBase;
import jam.global.BroadcastEvent;
import jam.global.GoodThread;
import jam.global.JamProperties;
import jam.global.RTSI;
import jam.global.SortMode;
import jam.sort.DiskDaemon;
import jam.sort.NetDaemon;
import jam.sort.RingBuffer;
import jam.sort.SortDaemon;
import jam.sort.SortException;
import jam.sort.SortRoutine;
import jam.sort.VME_Map;
import jam.sort.stream.AbstractEventInputStream;
import jam.sort.stream.AbstractEventOutputStream;
import jam.ui.Console;
import jam.ui.PathBrowseButton;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
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
 * @see jam.sort.StorageDaemon
 */
public final class SetupSortOn extends AbstractSetup {

	private static SetupSortOn instance = null;

	/**
	 * Creates the only instance of this class.
	 * 
	 * @param console
	 *            the console to use
	 */
	public static void createInstance(final Console console) {
		if (instance == null) {
			instance = new SetupSortOn(console);
		} else {
			throw new IllegalStateException("Object already created.");
		}
	}

	/**
	 * Returns the only instance of this class.
	 * 
	 * @return the only instance of this class
	 */
	public static SetupSortOn getInstance() {
		if (instance == null) {
			throw new IllegalStateException("Object not created yet.");
		}
		return instance;
	}

	private transient final AbstractButton bok, bapply, checkLock, bbrowseh,
			bbrowsed, bbrowsel;

	{
		checkLock = new JCheckBox("Setup Locked", false);
		checkLock.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent itemEvent) {
				if (!checkLock.isSelected()) {
					try {
						/* kill daemons, clear data areas */
						resetAcq(true);
						/* unlock sort mode */
						lockMode(false);
						jamConsole.closeLogFile();
					} catch (Exception e) {
						jamConsole.errorOutln(e.getMessage());
						e.printStackTrace();
					}
				}
			}
		});
		checkLock.setEnabled(false);
	}

	/* stuff for dialog box */
	private transient final AbstractButton cdisk, clog;

	{
		cdisk = new JCheckBox("Events to Disk", true);
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

	private transient final DisplayCounters counters;

	private transient File dataFolder, histFolder, logDirectory;

	private transient DiskDaemon diskDaemon;

	/* strings of data entered */
	private transient String exptName;

	private transient final FrontEndCommunication frontEnd;

	private transient final JComboBox inChooser, outChooser;

	/* streams to read and write events */
	private transient AbstractEventInputStream inStream;

	private transient final Console jamConsole;

	private transient NetDaemon netDaemon;

	private transient AbstractEventOutputStream outStream;

	private transient final RunControl runControl;

	/* sorting classes */
	private transient SortDaemon sortDaemon;

	private transient final JTextField textExpName, textPathHist, textPathData,
			textPathLog;

	private SetupSortOn(Console console) {
		super("Setup Online");
		final int fileTextCols = 25;
		final String defaultName = JamProperties
				.getPropString(JamProperties.EXP_NAME);
		final String defaultRoutine = JamProperties
				.getPropString(JamProperties.SORT_ROUTINE);
		final String defaultSortPath = JamProperties
				.getPropString(JamProperties.SORT_CLASSPATH);
		final String defaultInStream = JamProperties
				.getPropString(JamProperties.EVENT_INSTREAM);
		final String defaultOutStream = JamProperties
				.getPropString(JamProperties.EVENT_OUTSTREAM);
		dataFolder = new File(JamProperties
				.getPropString(JamProperties.EVENT_OUTPATH));
		histFolder = new File(JamProperties
				.getPropString(JamProperties.HIST_PATH));
		logDirectory = new File(JamProperties
				.getPropString(JamProperties.LOG_PATH));
		boolean useDefaultPath = (defaultSortPath == JamProperties.DEFAULT_SORT_CLASSPATH);
		runControl = RunControl.getSingletonInstance();
		counters = DisplayCounters.getSingletonInstance();
		jamConsole = console;
		frontEnd = STATUS.getFrontEndCommunication();
		dialog.setResizable(false);
		dialog.setLocation(20, 50);
		final Container dcp = dialog.getContentPane();
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
		final JLabel expName = new JLabel("Experiment Name",
				SwingConstants.RIGHT);
		pLabels.add(expName);
		final JLabel lsc = new JLabel("Sort classpath", SwingConstants.RIGHT);
		pLabels.add(lsc);
		final JLabel lscs = new JLabel("Selected sort classpath",
				SwingConstants.RIGHT);
		pLabels.add(lscs);
		final JLabel lSortRoutine = new JLabel("Sort Routine",
				SwingConstants.RIGHT);
		pLabels.add(lSortRoutine);
		final JLabel leis = new JLabel("Event input stream",
				SwingConstants.RIGHT);
		pLabels.add(leis);
		final JLabel leos = new JLabel("Event output stream",
				SwingConstants.RIGHT);
		pLabels.add(leos);
		final JLabel lhdfp = new JLabel("HDF path", SwingConstants.RIGHT);
		pLabels.add(lhdfp);
		final JLabel lep = new JLabel("Event path", SwingConstants.RIGHT);
		pLabels.add(lep);
		final JLabel llfp = new JLabel("Log file path", SwingConstants.RIGHT);
		pLabels.add(llfp);
		/* blank label balances out the grid */
		final JLabel lssf = new JLabel(/*
										 * "Sort sample fraction",
										 * SwingConstants.RIGHT
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
		JPanel pradio = new JPanel(new FlowLayout(FlowLayout.CENTER, noSpace,
				noSpace));
		pEntries.add(pradio);
		ButtonGroup pathType = new ButtonGroup();
		pathType.add(defaultPath);
		pradio.add(defaultPath);
		pathType.add(specify);
		pradio.add(specify);
		/* Class path text */
		pEntries.add(textSortPath);
		/* Sort classes chooser */
		Iterator<Class<?>> iterator = setChooserDefault(useDefaultPath)
				.iterator();
		boolean done = false;
		while (!done && iterator.hasNext()) {
			final Class clazz = iterator.next();
			final String name = clazz.getName();
			done = name.equals(defaultRoutine);
			if (done) {
				sortChoice.setSelectedItem(clazz);
			}
		}
		pEntries.add(sortChoice);
		/* Input stream classes */
		Set<Class<?>> lhs = new LinkedHashSet<Class<?>>(RTSI.find(
				"jam.sort.stream", AbstractEventInputStream.class, false));
		lhs.remove(AbstractEventInputStream.class);
		inChooser = new JComboBox(lhs.toArray());
		inChooser
				.setToolTipText("Select the reader for your event data format.");
		selectName(inChooser, lhs, defaultInStream);
		pEntries.add(inChooser);
		/* Output stream classes */
		lhs = new LinkedHashSet<Class<?>>(RTSI.find("jam.sort.stream",
				AbstractEventOutputStream.class, false));
		lhs.remove(AbstractEventOutputStream.class);
		outChooser = new JComboBox(lhs.toArray());
		outChooser
				.setToolTipText("Select the writer for your output event format.");
		selectName(outChooser, lhs, defaultOutStream);
		pEntries.add(outChooser);
		textPathHist = new JTextField(histFolder.getPath());
		textPathHist.setColumns(fileTextCols);
		textPathHist
				.setToolTipText("Path to save HDF summary files at the end of each run.");
		textPathHist.setEditable(false);
		pEntries.add(textPathHist);
		textPathData = new JTextField();
		textPathData.setColumns(fileTextCols);
		textPathData.setToolTipText("Path to save event data.");
		textPathData.setEditable(false);
		pEntries.add(textPathData);
		textPathLog = new JTextField();
		textPathLog.setColumns(fileTextCols);
		textPathLog.setToolTipText("Path to save the console log.");
		textPathLog.setEditable(false);
		pEntries.add(textPathLog);
		JPanel pInterval = new JPanel(new GridLayout(1, 2, 40, 0));
		pEntries.add(pInterval);
		pInterval.add(cdisk);
		clog = new JCheckBox("Log Commands", false);
		clog.setSelected(true);
		/* Browse panel */
		JPanel pBrowse = new JPanel(new GridLayout(0, 1, 5, 5));
		pBrowse.setBorder(new EmptyBorder(10, 0, 0, 10));
		dcp.add(pBrowse, BorderLayout.EAST);
		final Dimension dummyDim = new Dimension(10, 10);
		pBrowse.add(new Box.Filler(dummyDim, dummyDim, dummyDim));
		pBrowse.add(new Box.Filler(dummyDim, dummyDim, dummyDim));
		pBrowse.add(bbrowsef);
		pBrowse.add(new Box.Filler(dummyDim, dummyDim, dummyDim));
		pBrowse.add(new Box.Filler(dummyDim, dummyDim, dummyDim));
		pBrowse.add(new Box.Filler(dummyDim, dummyDim, dummyDim));
		bbrowseh = new PathBrowseButton(histFolder,textPathHist);
		pBrowse.add(bbrowseh);
		bbrowsed = new PathBrowseButton(dataFolder,textPathData);
		pBrowse.add(bbrowsed);
		bbrowsel = new PathBrowseButton(logDirectory,textPathLog);
		pBrowse.add(bbrowsel);
		pBrowse.add(new Box.Filler(dummyDim, dummyDim, dummyDim));
		/* panel for buttons */
		JPanel pbutton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		dcp.add(pbutton, BorderLayout.SOUTH);
		final JPanel pBottom = new JPanel(new GridLayout(1, 4, 5, 5));
		pbutton.add(pBottom);
		bok = new JButton("OK");
		bok.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				doApply(true);
			}
		});
		pBottom.add(bok);
		bapply = new JButton("Apply");
		bapply.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				doApply(false);
			}
		});
		pBottom.add(bapply);
		final JButton bcancel = new JButton("Cancel");
		pBottom.add(bcancel);
		bcancel.setActionCommand("cancel");
		bcancel.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				dialog.dispose();
			}
		});
		pBottom.add(checkLock);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.pack();
	}

	protected void doApply(final boolean dispose) {
		try {
			/* lock setup so fields cant be edited */
			if (STATUS.canSetup()) {
				loadNames();
				if (clog.isSelected()) { // if needed start logging to file
					final String logFile = jamConsole
							.setLogFileName(logDirectory + exptName);
					jamConsole.messageOutln("Logging to file: " + logFile);
					jamConsole.setLogFileOn(true);
				} else {
					jamConsole.setLogFileOn(false);
				}
				jamConsole
						.messageOutln("Setup Online Data Acquisition,  Experiment Name: "
								+ exptName);
				/* Kill all existing Daemons and clear data areas */
				resetAcq(false);
				loadSorter(); // load sorting routine
				if (sortRoutine != null) {
					lockMode(true);
					setupSort(); // create daemons
					jamConsole.messageOutln("Loaded "
							+ sortRoutine.getClass().getName() + ", "
							+ inStream.getClass().getName() + " and "
							+ outStream.getClass().getName());
					jamConsole
							.messageOutln("Communications and processing daemons successfully initiated.");
					if (sortRoutine.getEventSizeMode() == SortRoutine.EventSizeMode.CNAF) {
						setupCamac(); // set the camac crate
						jamConsole.messageOutln("CAMAC command lists sent.");
					} else if (sortRoutine.getEventSizeMode() == SortRoutine.EventSizeMode.VME_MAP) {
						setupVMEmap();
						jamConsole.messageOutln("VME map sent.");
					}
				}
				selectFirstSortHistogram();
				if (dispose) {
					dialog.dispose();
				}
			} else {
				throw new JamException("Can't setup sorting, mode locked ");
			}
		} catch (SortException je) {
			jamConsole.errorOutln(je.getMessage());
		} catch (JamException je) {
			jamConsole.errorOutln(je.getMessage());
		} catch (Exception e) {
			jamConsole.errorOutln(e.getMessage());
		}
	}


	/**
	 * Save the names of the experiment, the sort file and the event and
	 * histogram directories.
	 */
	private void loadNames() {
		exptName = textExpName.getText().trim();
	}

	protected void lockMode(final boolean lock) {
		final boolean notlock = !lock;
		checkLock.setEnabled(lock);
		textExpName.setEnabled(notlock);
		inChooser.setEnabled(notlock);
		outChooser.setEnabled(notlock);
		sortChoice.setEnabled(notlock);
		bok.setEnabled(notlock);
		bapply.setEnabled(notlock);
		bbrowseh.setEnabled(notlock);
		bbrowsel.setEnabled(notlock);
		bbrowsed.setEnabled(notlock);
		specify.setEnabled(notlock);
		defaultPath.setEnabled(notlock);
		final SortMode sortMode = notlock ? SortMode.NO_SORT : (cdisk
				.isSelected() ? SortMode.ONLINE_DISK : SortMode.ON_NO_DISK);
		final String name = sortRoutine == null ? "No Data" : sortRoutine
				.getClass().getName();
		STATUS.setSortMode(sortMode, name);
		bbrowsef.setEnabled(notlock && specify.isSelected());
		checkLock.setSelected(lock);
	}

	/*
	 * non-javadoc: reset online data Aquisition kill all daemons closes data
	 * network clear all data areas Histograms, Gates, Scalers, Monitors,
	 * Parameters
	 */
	private void resetAcq(final boolean killSort) {
		if (diskDaemon != null) {
			diskDaemon.setState(GoodThread.State.STOP);
		}
		if (sortDaemon != null) {
			sortDaemon.setSorter(null);
			// make sure sorter Daemon does not have a handle to sortClass
			sortDaemon.setState(GoodThread.State.STOP);
			// this line should be sufficient but above line is needed
		}
		if (netDaemon != null) {
			netDaemon.setState(GoodThread.State.STOP);
			netDaemon.closeNet();
		}
		if (killSort) {
			sortRoutine = null;
		}
		DataBase.getInstance().clearAllLists();
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_NEW);
	}

	private void setupCamac() throws JamException {
		frontEnd.setupAcquisition();
		frontEnd.setupCamac(sortRoutine.getCamacCommands());
	}

	protected void setupSort() throws SortException, JamException {
		initializeSorter();
		/* interprocess buffering between daemons */
		final RingBuffer sortingRing = new RingBuffer();
		// if disk not selected than storage ring is made in "null/empty" state
		final RingBuffer storageRing = new RingBuffer(!cdisk.isSelected());
		/* typical setup of event streams */
		try { // create new event input stream class
			inStream = (AbstractEventInputStream) ((Class) inChooser
					.getSelectedItem()).newInstance();
			inStream.setConsole(jamConsole);
		} catch (InstantiationException ie) {
			// eventInputStream=null;
			ie.printStackTrace();
			throw new JamException(getClass().getName()
					+ ": can't instantiate EventInputStream: "
					+ inChooser.getSelectedItem());
		} catch (IllegalAccessException iae) {
			// eventInputStream=null;
			throw new JamException(getClass().getName()
					+ ": illegal access to EventInputStream: "
					+ inChooser.getSelectedItem());
		}
		try { // create new event input stream class
			outStream = (AbstractEventOutputStream) ((Class) outChooser
					.getSelectedItem()).newInstance();
			outStream.setEventSize(sortRoutine.getEventSize());
		} catch (InstantiationException ie) {
			// eventOutputStream=null;
			ie.printStackTrace();
			throw new JamException(getClass().getName()
					+ ": can't instantiate EventOutputStream class: "
					+ outChooser.getSelectedItem());
		} catch (IllegalAccessException iae) {
			throw new JamException(getClass().getName()
					+ ": illegal access to EventOutputStream class: "
					+ outChooser.getSelectedItem());
		}
		// create sorter daemon
		sortDaemon = new SortDaemon(runControl, jamConsole);
		final boolean useDisk = cdisk.isSelected();
		sortDaemon.setup(inStream, sortRoutine.getEventSize());
		sortDaemon.setRingBuffer(sortingRing);
		sortDaemon.setSorter(sortRoutine);
		// create storage daemon
		if (cdisk.isSelected()) { // don't create storage daemon otherwise
			diskDaemon = new DiskDaemon(runControl, jamConsole);
			diskDaemon.setupOn(inStream, outStream);
			diskDaemon.setRingBuffer(storageRing);
		}
		/* Create the net daemon. */
		netDaemon = new NetDaemon(sortingRing, storageRing, jamConsole,
				JamProperties.getPropString(JamProperties.HOST_DATA_IP),
				JamProperties.getPropInt(JamProperties.HOST_DATA_PORT_RECV));
		/* Tell control about everything. */
		runControl.setupOn(exptName, dataFolder, histFolder, sortDaemon,
				netDaemon, diskDaemon);
		/* Tell the status dialog. */
		counters.setupOn(netDaemon, sortDaemon, diskDaemon);
		/* Startup the daemons. */
		if (useDisk) {
			diskDaemon.start();
		}
		sortDaemon.start();
		netDaemon.start();
	}

	private void setupVMEmap() throws JamException {
		frontEnd.setupAcquisition();
		final VME_Map map = sortRoutine.getVMEmap();
		frontEnd.setupVMEmap(map);
		frontEnd.sendScalerInterval(map.getScalerInterval());
	}
}
