package jam;
import jam.data.DataBase;
import jam.data.control.DataControl;
import jam.global.*;
import jam.sort.*;
import jam.sort.stream.EventInputStream;
import jam.sort.stream.EventOutputStream;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import javax.swing.*;

/**
 * Class to setup online sorting.
 * Loads a sort file and
 * creates the daemons:
 * <ul>
 * <li>net</li><li>sort</li><li>tape</li>
 * </ul>
 *
 * @author Ken Swartz
 * @author Dale Visser
 * @version 05 newest done 9-98
 * @see jam.sort.NetDaemon
 * @see jam.sort.StorageDaemon
 */
class SetupSortOn implements ActionListener, ItemListener {

	private JamMain jamMain;
	private RunControl runControl;
	private DisplayCounters displayCounters;
	private JamConsole jamConsole;
	private MessageHandler msgHandler;
	private FrontEndCommunication frontEnd;
	final Broadcaster broadcaster;

	private static final int FILE_TEXT_COLUMNS = 25;

	//stuff for dialog box
	private JDialog d;
	File eventDirectory;

	//strings of data entered
	String experimentName; 
	File sortDirectory;
	String histDirectory, dataDirectory;
	String logDirectory;
	// 1/fraction of events to sort
	private int sortInterval;
	String eventFile;

	//text fields
	private JTextField textExpName, textSortPath, textPathHist, textPathData;
	private JTextField textPathLog;

	/**
	 * Whether to handle event writing or delegate to front end.
	 */
	boolean storeEventsLocally;

	//buttons and check boxes
	private JToggleButton ctape, cdisk; //save events to tape, disk
	private JToggleButton defaultPath, specify;
	private JCheckBox clog; //create a log file
	private JTextField textSortInterval;
	private JButton bok;
	private JButton bapply;
	private JCheckBox checkLock;
	private JButton bbrowsef,bbrowseh,bbrowsed,bbrowsel;

	//browse ?
	private String diskPathData;
	private String tapePathData;
	private String logFile;
	private String separator;
	private JComboBox sortChoice;
	private File sortClassPath;
	private Class sortClass;

	//streams GUI
	JComboBox inStreamChooser, outStreamChooser;

	//sorting classes
	SortDaemon sortDaemon;
	NetDaemon netDaemon;
	DiskDaemon diskDaemon;
	TapeDaemon tapeDaemon;
	StorageDaemon storageDaemon;
	SortRoutine sortRoutine;

	//ring buffers
	RingBuffer sortingRing;
	RingBuffer storageRing;
	//streams to read and write events
	EventInputStream eventInputStream;
	EventOutputStream eventOutputStream;

	//tape or disk
	private boolean tapeMode = false;

	//default names
	private String defaultName,
		defaultSortRoutine,
		defaultSortPath,
		defaultEventInStream,
		defaultEvents,
		defaultSpectra;
	private String defaultTape, defaultEventOutStream;
	private String defaultLog;

	/**
	 * Constructor
	 */
	public SetupSortOn(
		JamMain jamMain,
		RunControl runControl,
		DisplayCounters displayCounters,
		FrontEndCommunication frontEnd,
		JamConsole jamConsole,
		Broadcaster b) {
		defaultName = JamProperties.getPropString(JamProperties.EXP_NAME);
		defaultSortRoutine =
			JamProperties.getPropString(JamProperties.SORT_ROUTINE);
		defaultSortPath =
			JamProperties.getPropString(JamProperties.SORT_CLASSPATH);
		defaultEventInStream =
			JamProperties.getPropString(JamProperties.EVENT_INSTREAM);
		defaultEventOutStream =
			JamProperties.getPropString(JamProperties.EVENT_OUTSTREAM);
		defaultEvents =
			JamProperties.getPropString(JamProperties.EVENT_OUTPATH);
		defaultSpectra = JamProperties.getPropString(JamProperties.HIST_PATH);
		defaultTape = JamProperties.getPropString(JamProperties.TAPE_DEV);
		defaultLog = JamProperties.getPropString(JamProperties.LOG_PATH);
		boolean useDefaultPath =
			(defaultSortPath == JamProperties.DEFAULT_SORT_CLASSPATH);
		if (!useDefaultPath) {
			sortDirectory = new File(defaultSortPath);
			sortClassPath = sortDirectory;
		}
		this.jamMain = jamMain;
		this.runControl = runControl;
		this.displayCounters = displayCounters;
		this.jamConsole = jamConsole;
		this.msgHandler = jamConsole;
		this.frontEnd = frontEnd;
		broadcaster=b;
		tapePathData = defaultTape;
		d = new JDialog(jamMain, "Setup Online ", false);
		LayoutManager verticalGrid = new GridLayout(0, 1, 5, 5);
		d.setForeground(Color.black);
		d.setBackground(Color.lightGray);
		d.setResizable(false);
		d.setLocation(20, 50);
		Container dcp = d.getContentPane();
		dcp.setLayout(new BorderLayout());
		//panel for experiment name
		JPanel pn = new JPanel();
		pn.setLayout(new BorderLayout());
		dcp.add(pn,BorderLayout.NORTH);

		JLabel ln = new JLabel("Experiment Name", JLabel.RIGHT);
		pn.add(ln,BorderLayout.WEST);

		textExpName = new JTextField(defaultName);
		textExpName.setToolTipText("Used to name data files. Only the first 20 characters get recorded in event file headers.");
		textExpName.setColumns(20);
		textExpName.setBackground(Color.white);
		pn.add(textExpName,BorderLayout.CENTER);

		JPanel pradio = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		ButtonGroup pathType = new ButtonGroup();
		defaultPath =
			new JRadioButton(
				"Use help.* and sort.* in default classpath",
				useDefaultPath);
		specify = new JRadioButton("Specify a classpath", !useDefaultPath);
		defaultPath.setToolTipText(
			"Don't include your sort routines in the default classpath if "
				+ "you want to be able to edit, recompile and reload them without first quitting Jam.");
		specify.setToolTipText(
			"Specify a classpath to dynamically load your sort routine from.");
		pathType.add(defaultPath);
		pathType.add(specify);
		defaultPath.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (defaultPath.isSelected()) {
					bbrowsef.setEnabled(false);
					setChooserDefault(true);
				}
			}
		});
		specify.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (specify.isSelected()) {
					bbrowsef.setEnabled(true);
					setChooserDefault(false);
				}
			}
		});
		pradio.add(defaultPath);
		pradio.add(specify);
		pn.add(pradio,BorderLayout.SOUTH);

		JPanel pCenter = new JPanel(new BorderLayout());
		dcp.add(pCenter, BorderLayout.CENTER);
		JPanel pf = new JPanel(new BorderLayout());
		pCenter.add(pf, BorderLayout.NORTH);

		JLabel lf = new JLabel("Sort classpath", JLabel.RIGHT);
		pf.add(lf, BorderLayout.WEST);
		textSortPath = new JTextField(defaultSortPath);
		textSortPath.setToolTipText(
			"Use Browse button to change. \nMay fail if classes have unresolvable references."
				+ "\n* use the sort.classpath property in your JamUser.ini file to set this automatically.");
		textSortPath.setColumns(35);
		textSortPath.setEnabled(false);
		pf.add(textSortPath, BorderLayout.CENTER);
		bbrowsef = new JButton("Browse");
		pf.add(bbrowsef, BorderLayout.EAST);
		bbrowsef.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				sortClassPath = getSortPath();
				sortChoice.setModel(new DefaultComboBoxModel(getSortClasses(sortClassPath)));
				sortChoice.setSelectedIndex(0);
				textSortPath.setText(sortClassPath.getAbsolutePath());
			}
		});

		JPanel pChooserArea = new JPanel(new BorderLayout());
		JPanel pChooserLabels = new JPanel(verticalGrid);
		pChooserArea.add(pChooserLabels, BorderLayout.WEST);
		JPanel pChoosers = new JPanel(verticalGrid);
		pChooserArea.add(pChoosers, BorderLayout.CENTER);
		pCenter.add(pChooserArea);

		pChooserLabels.add(
			new JLabel("Sort Routine", JLabel.RIGHT),
			BorderLayout.WEST);
		Vector v = getSortClasses(sortDirectory);
		sortChoice = new JComboBox(v);
		sortChoice.setToolTipText("Select a class to be your sort routine.");
		sortChoice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				sortClass = (Class) sortChoice.getSelectedItem();
			}
		});
		Iterator it = v.iterator();
		boolean notDone = it.hasNext();
		while (notDone) {
			Class c = (Class) it.next();
			String name = c.getName();
			boolean match = name.equals(defaultSortRoutine);
			if (match) {
				sortChoice.setSelectedItem(c);
			}
			notDone = (!match) & it.hasNext();
		}
		setChooserDefault(useDefaultPath);
		pChoosers.add(sortChoice);

		pChooserLabels.add(new JLabel("Event input stream", JLabel.RIGHT));

		Set lhs =
			new LinkedHashSet(
				RTSI.find("jam.sort.stream", EventInputStream.class, false));
		lhs.remove(EventInputStream.class);
		inStreamChooser = new JComboBox(new Vector(lhs));
		inStreamChooser.setToolTipText(
			"Select the reader for your event data format.");
		it = lhs.iterator();
		notDone = it.hasNext();
		while (notDone) {
			Class c = (Class) it.next();
			String name = c.getName();
			boolean match = name.equals(defaultEventInStream);
			if (match) {
				inStreamChooser.setSelectedItem(c);
			}
			notDone = (!match) & it.hasNext();
		}
		pChoosers.add(inStreamChooser);

		pChooserLabels.add(new JLabel("Event output stream", Label.RIGHT));

		lhs =
			new LinkedHashSet(
				RTSI.find("jam.sort.stream", EventOutputStream.class, false));
		lhs.remove(EventOutputStream.class);
		outStreamChooser = new JComboBox(new Vector(lhs));
		outStreamChooser.setToolTipText(
			"Select the writer for your output event format.");
		it = lhs.iterator();
		notDone = it.hasNext();
		while (notDone) {
			Class c = (Class) it.next();
			String name = c.getName();
			boolean match = name.equals(defaultEventOutStream);
			if (match) {
				outStreamChooser.setSelectedItem(c);
			}
			notDone = (!match) & it.hasNext();
		}
		pChoosers.add(outStreamChooser);

		JPanel pPathArea = new JPanel(new BorderLayout());
		JPanel pPathLabels = new JPanel(verticalGrid);
		JPanel pPathFields = new JPanel(verticalGrid);
		JPanel pPathButtons = new JPanel(verticalGrid);
		pPathArea.add(pPathLabels, BorderLayout.WEST);
		pPathArea.add(pPathFields, BorderLayout.CENTER);
		pPathArea.add(pPathButtons, BorderLayout.EAST);
		pCenter.add(pPathArea, BorderLayout.SOUTH);

		JLabel ltemp=new JLabel("HDF path", JLabel.RIGHT);
		pPathLabels.add(ltemp);		

		textPathHist = new JTextField(defaultSpectra);
		textPathHist.setColumns(FILE_TEXT_COLUMNS);
		textPathHist.setBackground(Color.white);
		textPathHist.setToolTipText("Path for storing HDF summary (spectra, gates, & scalers) files at the end of each run.");
		pPathFields.add(textPathHist);

		bbrowseh = new JButton("Browse");
		pPathButtons.add(bbrowseh);
		bbrowseh.setActionCommand("bhist");
		bbrowseh.addActionListener(this);

		ltemp=new JLabel("Event path", JLabel.RIGHT);
		pPathLabels.add(ltemp);

		textPathData = new JTextField(defaultEvents);
		textPathData.setColumns(FILE_TEXT_COLUMNS);
		textPathData.setBackground(Color.white);
		textPathData.setToolTipText("Path to store event data in.");
		pPathFields.add(textPathData);
		
		bbrowsed = new JButton("Browse");
		pPathButtons.add(bbrowsed);
		bbrowsed.setActionCommand("bdata");
		bbrowsed.addActionListener(this);

		ltemp=new JLabel("Log file path", JLabel.RIGHT);
		pPathLabels.add(ltemp);

		textPathLog = new JTextField(defaultLog);
		textPathLog.setColumns(FILE_TEXT_COLUMNS);
		textPathLog.setBackground(Color.white);
		textPathLog.setToolTipText("Path to store the console log in.");
		pPathFields.add(textPathLog);

		bbrowsel = new JButton("Browse");
		pPathButtons.add(bbrowsel);
		bbrowsel.setActionCommand("blog");
		bbrowsel.addActionListener(this);

		JPanel pt = new JPanel();
		pt.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		pPathArea.add(pt,BorderLayout.SOUTH);

		textSortInterval = new JTextField("1");
		textSortInterval.setToolTipText("Sort every n'th buffer. 1 means sort all events.");
		textSortInterval.setColumns(3);
		textSortInterval.setBackground(Color.white);
		pt.add(textSortInterval);

		JLabel lsi = new JLabel("Sort Sample", JLabel.LEFT);
		pt.add(lsi);

		ButtonGroup eventMode = new ButtonGroup();
		ctape = new JRadioButton("Events to Tape", false);
		ctape.setToolTipText("Send events to tape, not implemented.");
		ctape.setEnabled(false);
		eventMode.add(ctape);
		ctape.addItemListener(this);
		pt.add(ctape);

		cdisk = new JRadioButton("Events to Disk", true);
		cdisk.setToolTipText("Send events to disk.");
		eventMode.add(cdisk);
		cdisk.addItemListener(this);
		pt.add(cdisk);

		clog = new JCheckBox("Log Commands", false);
		clog.addItemListener(this);
		clog.setSelected(true);
		/* for the time being, don't bother with this option */
		//pt.add(clog);

		// panel for buttons
		JPanel pb = new JPanel(new GridLayout(1,4,5,5));
		dcp.add(pb,BorderLayout.SOUTH);

		bok = new JButton("OK");
		bok.setActionCommand("ok");
		bok.addActionListener(this);
		pb.add(bok);

		bapply = new JButton("Apply");
		bapply.setActionCommand("apply");
		bapply.addActionListener(this);
		pb.add(bapply);

		JButton bcancel = new JButton("Cancel");
		pb.add(bcancel);
		bcancel.setActionCommand("cancel");
		bcancel.addActionListener(this);

		checkLock = new JCheckBox("Setup Locked", false);
		checkLock.addItemListener(this);
		checkLock.setEnabled(false);
		pb.add(checkLock);

		d.pack();

		separator = "/"; //default path separator
		separator = System.getProperty("file.separator", separator);

		//Recieves events for closing the dialog box and closes it.
		d.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				d.dispose();
				d.pack();
			}
		});
	}

	private void setChooserDefault(boolean isDefault) {
		if (isDefault) {
			Set set = new LinkedHashSet();
			set.addAll(RTSI.find("help", SortRoutine.class, true));
			set.addAll(RTSI.find("sort", SortRoutine.class, true));
			Vector v = new Vector();
			v.addAll(set);
			sortChoice.setModel(new DefaultComboBoxModel(v));
		} else {
			sortChoice.setModel(
				new DefaultComboBoxModel(
					(Vector) getSortClasses(sortClassPath)));
		}
	}

	private Vector getSortClasses(File path) {
		return new Vector(RTSI.find(path, jam.sort.SortRoutine.class));
	}

	/**
	 * Browses for the sort file.
	 */
	private File getSortPath() {
		JFileChooser fd = new JFileChooser(sortDirectory);
		fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int option = fd.showOpenDialog(jamMain);
		//save current values
		if (option == JFileChooser.APPROVE_OPTION
			&& fd.getSelectedFile() != null) {
			sortDirectory = fd.getSelectedFile(); //save current directory
		}
		return sortDirectory;
	}

	/**
	 * Show online sorting dialog Box.
	 *
	 */
	public void show() {
		d.show();
	}

	/**
	 * Receives events from this dialog box.
	 *
	 * @Author Ken Swartz
	 *
	 */
	public void actionPerformed(ActionEvent ae) {
		String command = ae.getActionCommand();
		try {
			if (command == "blog") {
				String logDirectory = getPathLog();
				textPathLog.setText(logDirectory);
			} else if (command == "bhist") {
				histDirectory = getPathHist();
				textPathHist.setText(histDirectory);
			} else if (command == "bdata") {
				dataDirectory = getPathData();
				textPathData.setText(dataDirectory);
			} else if (command == "ok" || command == "apply") {
				//lock setup so fields cant be edited
				if (jamMain.canSetSortMode()) {
					loadNames();
					if (clog.isSelected()) { //if needed start logging to file
						logFile =
							JamProperties.getPropString(JamProperties.LOG_PATH)
								+ File.separator
								+ experimentName;
						logFile = jamConsole.setLogFileName(logFile);
						jamConsole.messageOutln("Logging to file: " + logFile);
						jamConsole.setLogFileOn(true);
					} else {
						jamConsole.setLogFileOn(false);
					}
					jamConsole.messageOutln(
						"Setup Online Data Acquisition,  Experiment Name: "
							+ experimentName);
					loadSorter(); //load sorting routine
					if (sortRoutine != null) {
						resetAcq();
						//Kill all existing Daemons and clear data areas
						setupAcq(); //create daemons
						jamConsole.messageOutln(
							"Loaded sort routine "
								+ sortRoutine.getClass().getName()
								+ ", input stream "
								+ eventInputStream.getClass().getName()
								+ " and output stream "
								+ eventOutputStream.getClass().getName());
						if (sortRoutine.getEventSizeMode()
							== SortRoutine.SET_BY_CNAF) {
							setupCamac(); //set the camac crate
						} else if (
							sortRoutine.getEventSizeMode()
								== SortRoutine.SET_BY_VME_MAP) {
							setupVME_Map();
						}
						lockMode(true);
						broadcaster.broadcast(BroadcastEvent.HISTOGRAM_ADD);
						jamConsole.messageOutln(
							"Setup data network, and Online sort daemons setup");
					}
					if (command == "ok") {
						d.dispose();
					}
				} else {
					throw new JamException("Can't setup sorting, mode locked ");
				}
			} else if (command == "cancel") {
				d.dispose();
			}
		} catch (SortException je) {
			msgHandler.errorOutln(je.getMessage());
			//je.printStackTrace();
		} catch (JamException je) {
			jamConsole.errorOutln(je.getMessage());
			//je.printStackTrace();
		} catch (GlobalException ge) {
			jamConsole.errorOutln(ge.getMessage());
			//ge.printStackTrace();
		}
	}

	/**
	 * Recieves events from this check box.
	 *
	 * @Author Ken Swartz
	 */
	public void itemStateChanged(ItemEvent ie) {
		try {
			if (ie.getItemSelectable() == checkLock) {
				if (!checkLock.isSelected()) {
					//kill daemons, clear data areas
					resetAcq();
					//unlock sort mode
					lockMode(false);
					jamConsole.closeLogFile();
				}
			} else if (ie.getItemSelectable() == ctape) {
				setTapeMode(ctape.isSelected());
			}
		} catch (JamException je) {
			jamConsole.errorOutln(je.getMessage());
		} catch (GlobalException ge) {
			jamConsole.errorOutln(ge.getMessage());
		}
	}

	/**
	 * Save the names of the experiment, the sort file
	 * and the event and histogram directories.
	 */
	private void loadNames() throws JamException {
		experimentName = textExpName.getText().trim();
		histDirectory = textPathHist.getText().trim() + separator;
		dataDirectory = textPathData.getText().trim() + separator;
		logDirectory = textPathLog.getText().trim() + separator;
		if (!tapeMode) {
			dataDirectory = dataDirectory + separator;
		}
		try {
			sortInterval = Integer.parseInt(textSortInterval.getText().trim());
		} catch (NumberFormatException nfe) {
			throw new JamException("Not a valid number for sort Interval");
		}
	}

	/**
	 * Load and instantize the sort file.
	 */
	private void loadSorter() throws JamException {
		try { // create sort class
			if (specify.isSelected()) {
				/* we call loadClass() in order to guarantee latest version */
				sortRoutine =
					(SortRoutine) RTSI
						.loadClass(sortClassPath, sortClass.getName())
						.newInstance();
				// create sort class
			} else { //use default loader
				sortRoutine = (SortRoutine) sortClass.newInstance();
			}
		} catch (InstantiationException ie) {
			//            sortClass=null;
			throw new JamException(
				"Cannot instantiate sort routine: " + sortClass.getName());
		} catch (IllegalAccessException iae) {
			//            sortClass=null;
			throw new JamException(
				" Cannot access sort routine: " + sortClass.getName());
		}
	}

	/**
	 * Sets up the online sort process.  Creates the necessary daemons and link pipes
	 * between the processes.
	 *
	 * @author Ken Swartz
	 * @author Dale Visser
	 */
	private void setupAcq() throws SortException, JamException {
		try { //allocate data areas
			sortRoutine.initialize();
		} catch (Exception e) {
			throw new JamException(
				"Exception in SortRoutine: "
					+ sortRoutine.getClass().getName()
					+ ".initialize(); Message= '"
					+ e.getClass().getName()
					+ ": "
					+ e.getMessage()
					+ "'");
		}
		DataControl.setupAll();
		//interprocess buffering between daemons
		sortingRing = new RingBuffer();
		if (storeEventsLocally)
			storageRing = new RingBuffer();
		//typical setup of event streams
		try { //create new event input stream class
			eventInputStream =
				(EventInputStream) ((Class) inStreamChooser.getSelectedItem())
					.newInstance();
			eventInputStream.setConsole(msgHandler);
		} catch (InstantiationException ie) {
			//            eventInputStream=null;
			ie.printStackTrace();
			throw new JamException(
				getClass().getName()
					+ ": can't instantiate EventInputStream: "
					+ inStreamChooser.getSelectedItem());
		} catch (IllegalAccessException iae) {
			//            eventInputStream=null;
			throw new JamException(
				getClass().getName()
					+ ": illegal access to EventInputStream: "
					+ inStreamChooser.getSelectedItem());
		}
		try { //create new event input stream class
			eventOutputStream =
				(EventOutputStream) ((Class) outStreamChooser
					.getSelectedItem())
					.newInstance();
			eventOutputStream.setEventSize(sortRoutine.getEventSize());
		} catch (InstantiationException ie) {
			//            eventOutputStream=null;
			ie.printStackTrace();
			throw new JamException(
				getClass().getName()
					+ ": can't instantiate EventOutputStream class: "
					+ outStreamChooser.getSelectedItem());
		} catch (IllegalAccessException iae) {
			//            eventOutputStream=null;
			throw new JamException(
				getClass().getName()
					+ ": illegal access to EventOutputStream class: "
					+ outStreamChooser.getSelectedItem());
		}
		//create sorter daemon
		sortDaemon = new SortDaemon(runControl, msgHandler);
		sortDaemon.setup(
			SortDaemon.ONLINE,
			eventInputStream,
			sortRoutine.getEventSize());
		sortDaemon.setRingBuffer(sortingRing);
		sortDaemon.load(sortRoutine);
		//create storage daemon
		if (storeEventsLocally) { // don't create storage daemon otherwise
			if (tapeMode) {
				tapeDaemon = new TapeDaemon(runControl, msgHandler);
				tapeDaemon.setDevice(dataDirectory);
				storageDaemon = tapeDaemon;
			} else {
				diskDaemon = new DiskDaemon(runControl, msgHandler);
				storageDaemon = diskDaemon;
			}
			storageDaemon.setupOn(eventInputStream, eventOutputStream);
			storageDaemon.setRingBuffer(storageRing);
		}
		//create net daemon
		netDaemon =
			new NetDaemon(
				sortingRing,
				storageRing,
				msgHandler,
				JamProperties.getPropString(JamProperties.HOST_DATA_IP),
				JamProperties.getPropInt(JamProperties.HOST_DATA_PORT_RECV));
		//set the fraction of buffers to give to the sort routine
		netDaemon.setSortInterval(sortInterval);
		//tell control about everything
		runControl.setupOn(
			experimentName,
			dataDirectory,
			histDirectory,
			sortDaemon,
			netDaemon,
			storageDaemon);
		//tell status
		displayCounters.setupOn(netDaemon, sortDaemon, storageDaemon);
		//startup daemons
		if (storeEventsLocally)
			storageDaemon.start();
		sortDaemon.start();
		netDaemon.start();
	}

	/**
	 *
	 */
	private void setupCamac() throws JamException {
		frontEnd.setup();
		frontEnd.setupCamac(sortRoutine.getCamacCommands());
		// tell vme to read files of list of cnafs
	}

	private void setupVME_Map() throws JamException, SortException {
		frontEnd.setup();
		VME_Map map = sortRoutine.getVME_Map();
		frontEnd.setupVME_Map(map);
		frontEnd.sendScalerInterval(map.getScalerInterval());
	}

	/**
	 * reset online data Aquisition
	 *      kill all daemons
	 *      closes data network
	 *      clear all data areas
	 *      Histograms, Gates, Scalers, Monitors, Parameters
	 */
	private void resetAcq() throws GlobalException {
		if (diskDaemon != null) {
			diskDaemon.setState(GoodThread.STOP);
		}
		if (sortDaemon != null) {
			sortDaemon.load(null);
			//make sure sorter Daemon does not have a handle to sortClass
			sortDaemon.setState(GoodThread.STOP);
			//this line should be sufficient but above line is needed
		}
		if (netDaemon != null) {
			netDaemon.setState(GoodThread.STOP);
			netDaemon.closeNet();
		}
		if (tapeDaemon != null) {
			tapeDaemon.setState(GoodThread.STOP);
		}
		DataBase.clearAllLists();
	}

	/**
	 * Makes the tape the device events will be saved to
	 *
	 * Author Dale Visser
	 */
	private void setTapeMode(boolean mode) {
		tapeMode = mode;
		if (mode) {
			diskPathData = textPathData.getText();
			textPathData.setText(tapePathData);
		} else {
			tapePathData = textPathData.getText();
			textPathData.setText(diskPathData);
		}
	}

	/**
	 * Is the Browse for the Path Name where the
	 * histogram file to be saved.
	 *
	 * @author Ken Swartz
	 * @author Dale Visser
	 */
	private String getPathHist() {
		JFileChooser fd = new JFileChooser(histDirectory);
		fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int option = fd.showOpenDialog(jamMain);
		//save current values
		if (option == JFileChooser.APPROVE_OPTION
			&& fd.getSelectedFile() != null) {
			histDirectory = fd.getSelectedFile().getPath();
			//save current directory
		}
		return histDirectory;
	}

	/**
	 * Is the Browse for the Path Name where the
	 * histogram file to be saved.
	 *
	 * @author Ken Swartz
	 * @author Dale Visser
	 */
	private String getPathLog() {
		JFileChooser fd = new JFileChooser(logDirectory);
		fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int option = fd.showOpenDialog(jamMain);
		//save current values
		if (option == JFileChooser.APPROVE_OPTION
			&& fd.getSelectedFile() != null) {
			logDirectory = fd.getSelectedFile().getPath();
			//save current directory
		}
		return logDirectory;
	}

	/**
	 * Is the Browse for the Path Name where
	 *  the events file will be saved.
	 *
	 * @author Ken Swartz
	 * @author Dale Visser
	 */
	private String getPathData() {
		JFileChooser fd = new JFileChooser(dataDirectory);
		fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int option = fd.showOpenDialog(jamMain);
		//save current values
		if (option == JFileChooser.APPROVE_OPTION
			&& fd.getSelectedFile() != null) {
			dataDirectory = fd.getSelectedFile().getPath();
			//save current directory
		}
		return dataDirectory;
	}

	/**
	 * Locks up the Online setup so the fields cannot be edited.
	 *
	 * @param  lock  is true if the fields are to be locked
	 */
	private void lockMode(boolean lock) throws JamException {
		if (lock) {
			if (tapeMode) {
				jamMain.setSortMode(JamMain.ONLINE_TAPE);
			} else {
				jamMain.setSortMode(JamMain.ONLINE_DISK);
			}
			checkLock.setSelected(true);
			checkLock.setEnabled(true);
			textExpName.setEnabled(false);
			inStreamChooser.setEnabled(false);
			outStreamChooser.setEnabled(false);
			textPathHist.setEnabled(false);
			textPathData.setEnabled(false);
			textPathLog.setEnabled(false);
			textSortInterval.setEnabled(false);
			bok.setEnabled(false);
			bapply.setEnabled(false);
			bbrowsef.setEnabled(false);
			bbrowseh.setEnabled(false);
			bbrowsed.setEnabled(false);
			bbrowsel.setEnabled(false);
			sortChoice.setEnabled(false);
			specify.setEnabled(false);
			defaultPath.setEnabled(false);
		} else {
			jamMain.setSortMode(JamMain.NO_SORT);
			checkLock.setEnabled(false);
			textExpName.setEnabled(true);
			inStreamChooser.setEnabled(true);
			outStreamChooser.setEnabled(true);
			textPathHist.setEnabled(true);
			textPathData.setEnabled(true);
			textPathLog.setEnabled(true);
			specify.setEnabled(true);
			defaultPath.setEnabled(true);
			textSortInterval.setEnabled(true);
			bok.setEnabled(true);
			bapply.setEnabled(true);
			bbrowsef.setEnabled(specify.isSelected());
			bbrowseh.setEnabled(true);
			bbrowsed.setEnabled(true);
			bbrowsel.setEnabled(true);
			sortChoice.setEnabled(true);
		}
	}
}