package jam;
import jam.data.DataBase;
import jam.data.control.DataControl;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.GoodThread;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.global.RTSI;
import jam.global.SortMode;
import jam.sort.DiskDaemon;
import jam.sort.NetDaemon;
import jam.sort.RingBuffer;
import jam.sort.SortDaemon;
import jam.sort.SortException;
import jam.sort.SortRoutine;
import jam.sort.VME_Map;
import jam.sort.stream.EventInputStream;
import jam.sort.stream.EventOutputStream;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

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
public final class SetupSortOn extends JDialog implements ActionListener, ItemListener {

	private final Frame frame;
	private final RunControl runControl;
	private final DisplayCounters displayCounters;
	private final JamConsole jamConsole;
	private final MessageHandler msgHandler;
	private final FrontEndCommunication frontEnd;
	private static final Broadcaster broadcaster=Broadcaster.getSingletonInstance();

	/* stuff for dialog box */
	private final JToggleButton cdisk; //save events to disk
	private final JToggleButton defaultPath, specify;
	private final JCheckBox clog; //create a log file
	private final JButton bok;
	private final JButton bapply;
	private final JCheckBox checkLock;
	private final JButton bbrowsef, bbrowseh, bbrowsed, bbrowsel;
	private final JComboBox sortChoice;
	private final JComboBox inStreamChooser, outStreamChooser;
	private final JTextField textExpName,
		textSortPath,
		textPathHist,
		textPathData;
	private final JTextField textPathLog;
	private final JSpinner sortIntervalSpinner;
	private static final JamStatus status=JamStatus.instance();

	/* strings of data entered */
	private String experimentName;
	private String histDirectory, dataDirectory;
	private String logDirectory;

	private File /*sortDirectory,*/ sortClassPath;
	private Class sortClass;
	/* 1/fraction of events to sort */
	private int sortInterval;

	/* sorting classes */
	private SortDaemon sortDaemon;
	private NetDaemon netDaemon;
	private DiskDaemon diskDaemon;
	//private StorageDaemon storageDaemon;
	private SortRoutine sortRoutine;

	/* streams to read and write events */
	private EventInputStream eventInputStream;
	private EventOutputStream eventOutputStream;
	
	private static SetupSortOn instance=null;
	public static SetupSortOn getSingletonInstance(){
		if (instance==null){
			throw new IllegalStateException("Object not created yet.");
		}
		return instance;
	}
	
	public static void createSingletonInstance(JamConsole jc){
		if (instance == null){
			instance=new SetupSortOn(jc);
		} else {
			throw new IllegalStateException("Object already created.");
		}
	}

	/**
	 * Constructor
	 */
	private SetupSortOn(JamConsole jc) {
		super(status.getFrame(), "Setup Online ", false);
		final int fileTextColumns = 25;
		final String defaultName =
			JamProperties.getPropString(JamProperties.EXP_NAME);
		final String defaultSortRoutine =
			JamProperties.getPropString(JamProperties.SORT_ROUTINE);
		final String defaultSortPath =
			JamProperties.getPropString(JamProperties.SORT_CLASSPATH);
		final String defaultEventInStream =
			JamProperties.getPropString(JamProperties.EVENT_INSTREAM);
		final String defaultEventOutStream =
			JamProperties.getPropString(JamProperties.EVENT_OUTSTREAM);
		final String defaultEvents =
			JamProperties.getPropString(JamProperties.EVENT_OUTPATH);
		final String defaultSpectra =
			JamProperties.getPropString(JamProperties.HIST_PATH);
		final String defaultLog =
			JamProperties.getPropString(JamProperties.LOG_PATH);
		boolean useDefaultPath =
			(defaultSortPath == JamProperties.DEFAULT_SORT_CLASSPATH);
		if (!useDefaultPath) {
			sortClassPath = new File(defaultSortPath);
		}
		runControl = RunControl.getSingletonInstance();
		displayCounters = DisplayCounters.getSingletonInstance();
		jamConsole = jc;
		msgHandler = jc;
		frontEnd = status.getFrontEndCommunication();
		frame=status.getFrame();
		setResizable(false);
		setLocation(20, 50);

		final Container dcp = getContentPane();
		dcp.setLayout(new BorderLayout(5,5));

		final int gap=5;
		JPanel pLabels = new JPanel(new GridLayout(0,1,gap,gap));
		final int topInset=10;
		final int leftInset=10;
		final int noSpace=0;
		pLabels.setBorder(new EmptyBorder(topInset,leftInset,noSpace,noSpace));
		dcp.add(pLabels, BorderLayout.WEST);
		final JLabel ln = new JLabel("Experiment Name", JLabel.RIGHT);
		pLabels.add(ln);
		final JLabel lsc = new JLabel("Sort classpath", JLabel.RIGHT);
		pLabels.add(lsc);
		final JLabel lscs = new JLabel("Selected sort classpath", JLabel.RIGHT);
		pLabels.add(lscs);
		final JLabel ls = new JLabel("Sort Routine", JLabel.RIGHT);
		pLabels.add(ls);
		final JLabel leis = new JLabel("Event input stream", JLabel.RIGHT);
		pLabels.add(leis);
		final JLabel leos	= new JLabel("Event output stream", JLabel.RIGHT);
		pLabels.add(leos);
		final JLabel lhdfp = new JLabel("HDF path", JLabel.RIGHT);
		pLabels.add(lhdfp);
		final JLabel lep = new JLabel("Event path", JLabel.RIGHT);
		pLabels.add(lep);
		final JLabel llfp = new JLabel("Log file path", JLabel.RIGHT);
		pLabels.add(llfp);
		final JLabel lssf = new JLabel("Sort sample fraction", JLabel.RIGHT);
		pLabels.add(lssf);


		//Entries Panel
		final JPanel pEntries = new JPanel(new GridLayout(0,1,gap,gap));
		pEntries.setBorder(new EmptyBorder(topInset,noSpace,noSpace,noSpace));
		dcp.add(pEntries, BorderLayout.CENTER);
		textExpName = new JTextField(defaultName);
		textExpName.setToolTipText(
			"Used to name data files. Only 20 characters get written to event files.");
		textExpName.setColumns(20);
		pEntries.add(textExpName);

		//Radio buttons for path
		JPanel pradio = new JPanel(new FlowLayout(FlowLayout.CENTER, noSpace, noSpace));
		pEntries.add(pradio);
		ButtonGroup pathType = new ButtonGroup();
		defaultPath = new JRadioButton( "help.* and sort.* under defaults", useDefaultPath);
		defaultPath.setToolTipText(
				  "Don't include your sort routines in the default classpath if "
				+ "you want to be able to edit, recompile and reload them without first quitting Jam.");
		pathType.add(defaultPath);
		pradio.add(defaultPath);
		defaultPath.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (defaultPath.isSelected()) {
					bbrowsef.setEnabled(false);
					setChooserDefault(true);
				}
			}
		});
		specify = new JRadioButton("Select classpath", !useDefaultPath);
		specify.setToolTipText("Specify a classpath to dynamically load your sort routine from.");
		pathType.add(specify);
		pradio.add(specify);
		specify.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (specify.isSelected()) {
					bbrowsef.setEnabled(true);
					setChooserDefault(false);
				}
			}
		});

		//Class path text
		textSortPath = new JTextField(defaultSortPath);
		textSortPath.setToolTipText(
			"Use Browse button to change. \nMay fail if classes have unresolvable references."
				+ "\n* use the sort.classpath property in your JamUser.ini file to set this automatically.");
		textSortPath.setColumns(35);
		textSortPath.setEditable(false);
		pEntries.add(textSortPath);

		/* Sort classes chooser */
		sortChoice = new JComboBox();
		final java.util.List sortClassList=setChooserDefault(useDefaultPath);
		sortChoice.setToolTipText("Select a class to be your sort routine.");
		sortChoice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				sortClass = (Class) sortChoice.getSelectedItem();
			}
		});
		Iterator it = sortClassList.iterator();
		while (it.hasNext()) {
			final Class c = (Class) it.next();
			final String name = c.getName();
			if (name.equals(defaultSortRoutine)) {
				sortChoice.setSelectedItem(c);
				break;
			}
		}
		pEntries.add(sortChoice);

		// Input stream classes
		Set lhs = new LinkedHashSet(
				RTSI.find("jam.sort.stream", EventInputStream.class, false));
		lhs.remove(EventInputStream.class);
		inStreamChooser = new JComboBox(new Vector(lhs));
		inStreamChooser.setToolTipText(
			"Select the reader for your event data format.");
		it = lhs.iterator();
		while (it.hasNext()) {
			Class c = (Class) it.next();
			String name = c.getName();
			boolean match = name.equals(defaultEventInStream);
			if (match) {
				inStreamChooser.setSelectedItem(c);
				break;
			}
		}
		pEntries.add(inStreamChooser);

		//Output stream classes
		lhs = new LinkedHashSet(
				RTSI.find("jam.sort.stream", EventOutputStream.class, false));
		lhs.remove(EventOutputStream.class);
		outStreamChooser = new JComboBox(new Vector(lhs));
		outStreamChooser.setToolTipText(
			"Select the writer for your output event format.");
		it = lhs.iterator();
		while (it.hasNext()) {
			final Class c = (Class) it.next();
			final String name = c.getName();
			boolean match = name.equals(defaultEventOutStream);
			if (match) {
				outStreamChooser.setSelectedItem(c);
				break;
			}
		}
		pEntries.add(outStreamChooser);

		textPathHist = new JTextField(defaultSpectra);
		textPathHist.setColumns(fileTextColumns);
		textPathHist.setToolTipText(
			"Path to save HDF summary files at the end of each run.");
		textPathHist.setEditable(false);
		pEntries.add(textPathHist);

		textPathData = new JTextField(defaultEvents);
		textPathData.setColumns(fileTextColumns);
		textPathData.setToolTipText("Path to save event data.");
		textPathData.setEditable(false);
		pEntries.add(textPathData);

		textPathLog = new JTextField(defaultLog);
		textPathLog.setColumns(fileTextColumns);
		textPathLog.setToolTipText("Path to save the console log.");
		textPathLog.setEditable(false);
		pEntries.add(textPathLog);

		JPanel pSortInterval = new JPanel(new GridLayout(1,2, 40,0));
		pEntries.add(pSortInterval);
		final Integer one=new Integer(1);
		sortIntervalSpinner = new JSpinner(new SpinnerNumberModel(one,one,null,
		one));
		sortIntervalSpinner.setToolTipText(
			"Sort every n'th buffer. 1 means sort all events.");
		pSortInterval.add(sortIntervalSpinner);

		cdisk = new JCheckBox("Events to Disk", true);
		cdisk.setToolTipText("Send events to disk.");
		cdisk.addItemListener(this);
		pSortInterval.add(cdisk);
		clog = new JCheckBox("Log Commands", false);
		clog.addItemListener(this);
		clog.setSelected(true);


		//Browse panel
		JPanel pBrowse = new JPanel(new GridLayout(0,1,5,5));
		pBrowse.setBorder(new EmptyBorder(10,0,0,10));
		dcp.add(pBrowse, BorderLayout.EAST);
		Dimension dummyDim =new Dimension(10, 10);
		pBrowse.add(new Box.Filler(dummyDim, dummyDim,dummyDim ));
		pBrowse.add(new Box.Filler(dummyDim, dummyDim,dummyDim ));
		bbrowsef = new JButton("Browse...");
		bbrowsef.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				sortClassPath = getSortPath();
				sortChoice.setModel(new DefaultComboBoxModel(new Vector(
				getSortClasses(sortClassPath))));
				sortChoice.setSelectedIndex(0);
				textSortPath.setText(sortClassPath.getAbsolutePath());
			}
		});
		pBrowse.add(bbrowsef);
		pBrowse.add(new Box.Filler(dummyDim, dummyDim,dummyDim ));
		pBrowse.add(new Box.Filler(dummyDim, dummyDim,dummyDim ));
		pBrowse.add(new Box.Filler(dummyDim, dummyDim,dummyDim ));

		bbrowseh = new JButton("Browse...");
		bbrowseh.setActionCommand("bhist");
		bbrowseh.addActionListener(this);
		pBrowse.add(bbrowseh);

		bbrowsed = new JButton("Browse...");
		bbrowsed.setActionCommand("bdata");
		bbrowsed.addActionListener(this);
		pBrowse.add(bbrowsed);

		bbrowsel = new JButton("Browse...");
		bbrowsel.setActionCommand("blog");
		bbrowsel.addActionListener(this);
		pBrowse.add(bbrowsel);

		pBrowse.add(new Box.Filler(dummyDim, dummyDim,dummyDim ));

		// panel for buttons
		JPanel pbutton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		dcp.add(pbutton, BorderLayout.SOUTH);
		JPanel pb = new JPanel(new GridLayout(1, 4, 5, 5));
		pbutton.add(pb);

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
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
	}

	private java.util.List setChooserDefault(boolean isDefault) {
		final Set set;
		if (isDefault) {
			set = new LinkedHashSet();
			set.addAll(RTSI.find("help", SortRoutine.class, true));
			set.addAll(RTSI.find("sort", SortRoutine.class, true));
		} else {
			set = getSortClasses(sortClassPath);
		}
		final Vector v=new Vector(set);
		sortChoice.setModel(new DefaultComboBoxModel(v));
		return v;
	}

	private Set getSortClasses(File path) {
		return RTSI.find(path, jam.sort.SortRoutine.class);
	}

	/**
	 * Browses for the sort file.
	 */
	private File getSortPath() {
		JFileChooser fd = new JFileChooser(sortClassPath);
		fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int option = fd.showOpenDialog(frame);
		//save current values
		if (option == JFileChooser.APPROVE_OPTION
			&& fd.getSelectedFile() != null) {
			sortClassPath = fd.getSelectedFile(); //save current directory
		}
		return sortClassPath;
	}

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
				if (status.canSetup()) {
					loadNames();
					if (clog.isSelected()) { //if needed start logging to file
						final String logFile =
							jamConsole.setLogFileName(
								JamProperties.getPropString(
									JamProperties.LOG_PATH)
									+ File.separator
									+ experimentName);
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
						resetAcq(false);
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
						dispose();
					}
				} else {
					throw new JamException("Can't setup sorting, mode locked ");
				}
			} else if (command == "cancel") {
				dispose();
			}
		} catch (SortException je) {
			msgHandler.errorOutln(je.getMessage());
		} catch (JamException je) {
			jamConsole.errorOutln(je.getMessage());
		} catch (Exception e) {
			jamConsole.errorOutln(e.getMessage());
		}
	}

	public void itemStateChanged(ItemEvent ie) {
		final ItemSelectable item = ie.getItemSelectable();
		if (item == checkLock) {
			if (!checkLock.isSelected()) {
				try {
					//kill daemons, clear data areas
					resetAcq(true);
					//unlock sort mode
					lockMode(false);
					jamConsole.closeLogFile();
				} catch (Exception e) {
					jamConsole.errorOutln(e.getMessage());
				}
			}
		} else if (item == cdisk) {
			boolean store=cdisk.isSelected();
			if (!store){
				final boolean oops = JOptionPane.showConfirmDialog(this,
				"De-selecting this checkbox means Jam won't store events to disk.\n"+
				"Is this what you really want?", "Event Storage Disabled",
				JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)==
				JOptionPane.NO_OPTION;
				if (oops) {
					cdisk.setSelected(true);
					store=true;
				}
			}
			//setTapeMode(!store);
			textPathData.setEnabled(store);
			this.bbrowsed.setEnabled(store);
		}
	}

	/**
	 * Save the names of the experiment, the sort file
	 * and the event and histogram directories.
	 */
	private void loadNames() throws JamException {
		final String fileSeparator = System.getProperty("file.separator", "/");
		experimentName = textExpName.getText().trim();
		histDirectory = textPathHist.getText().trim() + fileSeparator;
		dataDirectory = textPathData.getText().trim() + fileSeparator;
		logDirectory = textPathLog.getText().trim() + fileSeparator;
		if (!cdisk.isSelected()) {
			dataDirectory = dataDirectory + fileSeparator;
		}
		try {
			//sortInterval = Integer.parseInt(textSortInterval.getText().trim());
			sortInterval = ((Integer)sortIntervalSpinner.getValue()).intValue();
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
		/* interprocess buffering between daemons */
		final RingBuffer sortingRing = new RingBuffer();
		final RingBuffer storageRing =
			cdisk.isSelected() ? new RingBuffer() : null;
		/*if (STORE_EVENTS_LOCAL) {
			storageRing = new RingBuffer();
		}*/
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
		sortDaemon.setSortRoutine(sortRoutine);
		//create storage daemon
		if (cdisk.isSelected()) { // don't create storage daemon otherwise
			diskDaemon = new DiskDaemon(runControl, msgHandler);
			//storageDaemon = diskDaemon;
			//}
			diskDaemon.setupOn(eventInputStream, eventOutputStream);
			diskDaemon.setRingBuffer(storageRing);
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
			diskDaemon);
		//tell status
		displayCounters.setupOn(netDaemon, sortDaemon, diskDaemon);
		//startup daemons
		if (cdisk.isSelected()) {
			diskDaemon.start();
		}
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
	private void resetAcq(boolean killSort) {
		if (diskDaemon != null) {
			diskDaemon.setState(GoodThread.STOP);
		}
		if (sortDaemon != null) {
			sortDaemon.setSortRoutine(null);
			//make sure sorter Daemon does not have a handle to sortClass
			sortDaemon.setState(GoodThread.STOP);
			//this line should be sufficient but above line is needed
		}
		if (netDaemon != null) {
			netDaemon.setState(GoodThread.STOP);
			netDaemon.closeNet();
		}
		if (killSort) {
			sortRoutine = null;
		}
		DataBase.getInstance().clearAllLists();
		broadcaster.broadcast(BroadcastEvent.HISTOGRAM_ADD);
	}

	/**
	 * Makes the tape the device events will be saved to
	 *
	 * Author Dale Visser
	 */
	/*private void setTapeMode(boolean mode) {
		textPathData.setEnabled(!mode);
		this.bbrowsed.setEnabled(!mode);
	}*/

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
		int option = fd.showOpenDialog(frame);
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
		int option = fd.showOpenDialog(frame);
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
		int option = fd.showOpenDialog(frame);
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
		final boolean notlock=!lock;
		checkLock.setEnabled(lock);
		textExpName.setEnabled(notlock);
		inStreamChooser.setEnabled(notlock);
		outStreamChooser.setEnabled(notlock);
		sortChoice.setEnabled(notlock);
		sortIntervalSpinner.setEnabled(notlock);
		bok.setEnabled(notlock);
		bapply.setEnabled(notlock);
		bbrowseh.setEnabled(notlock);
		bbrowsel.setEnabled(notlock);
		bbrowsed.setEnabled(notlock);
		specify.setEnabled(notlock);
		defaultPath.setEnabled(notlock);
		status.setSortMode(notlock ? SortMode.NO_SORT : 
			(cdisk.isSelected() ? SortMode.ONLINE_DISK : SortMode.ONLINE_NO_DISK));
		bbrowsef.setEnabled(notlock && specify.isSelected());
		checkLock.setSelected(lock);
	}
}