package jam;
import jam.global.GlobalException;
import jam.global.GoodThread;
import jam.global.JamProperties;
import jam.global.MessageHandler;
import jam.global.RunInfo;
import jam.io.ExtensionFileFilter;
import jam.sort.Controller;
import jam.sort.SortDaemon;
import jam.sort.SortException;
import jam.sort.StorageDaemon;
import jam.sort.stream.EventException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

/**
 * Class to control the offline sort process
 * Allows you to enter the list of files to sort
 * an the output pre-sorted file if you have one
 *
 * @author Dale Visser and Ken Swartz
 * @version 1.0
 */
class SortControl implements Controller, ActionListener, ItemListener {
	//public static final int DISK = 0;
	//public static final int TAPE = 1;

	//private static final String DISKFILES = "Disk Files";
	//private static final String TAPERECORDS = "Tape Records";
	//private static final String DISKBUTTONS = "Disk Buttons";
	//private static final String TAPEBUTTONS = "Tape Buttons";

	private final JamMain jamMain;
	private final MessageHandler msgHandler;
	//private SetupSortOff setupSort;

	// daemon threads
	private StorageDaemon dataInpDaemon;
	private StorageDaemon dataOutDaemon;
	private SortDaemon sortDaemon;
	/**
	 * Device to use either DISK or TAPE     
	 */
	//private int device;
	/**
	 * The tape device eg. /dev/rmt/1
	 */
	private String deviceName;

	private File lastFile; //last file referred to in a JFileChooser
	private File fileOut; //file name for output
	private File outDirectory; //directory we last output files to.

	private boolean setupLock;
	private boolean writeEvents;

	/**
	 *  Dialog box widget
	 */
	private JDialog d;

	/**
	 * Text field to display tape device
	 */
	//private JTextField textDev, textInitRecord, textFinalRecord;

	/**
	 * Text field for output file
	 */
	private JTextField textOutFile;

	/** check box for writing out events */
	private JCheckBox cout;

	//private CardLayout centerCardLayout;
	//private CardLayout westCardLayout;

	private JPanel /*pwest, pcenter, ptaperecords,*/ pdiskfiles;

	/** 
	  * list for disk 
	  */
	private JList listEventFiles;
	private DefaultListModel eventFileModel;

	/** 
	  * list for tape 
	  */
	/*private JList listTapeRecords;
	private DefaultListModel tapeRecordModel;*/

	private JButton addfile,
		addDir,
		loadlist,
		remove,
		removeAll,
		savelist;
		//addrun,
		//addrange,
		//removeruns,
		//loadrunlist,
		//saverunlist;

	/** 
	  * button to get file brower 
	  */
	private JButton bbrowse;

	/** 
	  *control button for begin sort 
	  */
	private JButton bbegin;

	/** 
	  * control button for end sort 
	  */
	private JButton bend;

	String defaultEvents;
	String defaultOutputFile;

	public SortControl(JamMain jamMain, MessageHandler msgHandler) {
		this.jamMain = jamMain;
		this.msgHandler = msgHandler;
		defaultEvents =
			JamProperties.getPropString(JamProperties.EVENT_OUTPATH);
		defaultOutputFile =
			JamProperties.getPropString(JamProperties.EVENT_OUTFILE);
		d = new JDialog(jamMain, "Sorting ", false);
		//d.setForeground(Color.black);
		//d.setBackground(Color.lightGray);
		d.setResizable(true);
		d.setLocation(20, 50);
		//d.setSize(500, 350);
		Container cd = d.getContentPane();
		cd.setLayout(new BorderLayout(10, 10));
		JPanel ptop = new JPanel();
		ptop.setLayout(new GridLayout(0, 1, 5, 5));
		cd.add(ptop, BorderLayout.NORTH);
		//JPanel ptape = new JPanel();
		/*ptape.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		ptop.add(ptape);*/
		/*JLabel ltd = new JLabel("Device:");
		textDev = new JTextField("    ");
		textDev.setColumns(12);
		textDev.setEditable(false);*/
		//textDev.setBackground(Color.lightGray);
		//textDev.setForeground(Color.black);
		/*ptape.add(ltd);
		ptape.add(textDev);
		JLabel lfrom = new JLabel("Run(s)");
		ptape.add(lfrom);*/
		/*textInitRecord = new JTextField("");
		textInitRecord.setColumns(5);*/
		//textInitRecord.setBackground(Color.white);
		//textInitRecord.setForeground(Color.black);
		/*ptape.add(textInitRecord);
		JLabel lto = new JLabel("to");
		ptape.add(lto);*/
		/*textFinalRecord = new JTextField("");
		textFinalRecord.setColumns(5);*/
		//textFinalRecord.setBackground(Color.white);
		//textFinalRecord.setForeground(Color.black);
		//ptape.add(textFinalRecord);

		//Center panel--card layout
		//centerCardLayout = new CardLayout();
		//pcenter = new JPanel(centerCardLayout);
		pdiskfiles = new JPanel();
		pdiskfiles.setLayout(new BorderLayout());
		eventFileModel = new DefaultListModel();
		listEventFiles = new JList(eventFileModel);
		listEventFiles.setSelectionMode(
			ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		//listEventFiles.setBackground(Color.white);
		//listEventFiles.setForeground(Color.black);
		pdiskfiles.add(new JLabel("Event Files to Sort",JLabel.RIGHT), 
		BorderLayout.NORTH);
		pdiskfiles.add(new JScrollPane(listEventFiles), BorderLayout.CENTER);
		/*ptaperecords = new JPanel();
		ptaperecords.setLayout(new GridLayout());
		tapeRecordModel = new DefaultListModel();
		listTapeRecords = new JList(tapeRecordModel);
		listTapeRecords.setSelectionMode(
			ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);*/
		//listTapeRecords.setBackground(Color.white);
		//listTapeRecords.setForeground(Color.black);
		//ptaperecords.add(new JScrollPane(listTapeRecords));
		//pcenter.add(pdiskfiles, DISKFILES);
		//pcenter.add(ptaperecords, TAPERECORDS);
		//centerCardLayout.show(pcenter, DISKFILES);
		cd.add(pdiskfiles, BorderLayout.CENTER);

		//westCardLayout = new CardLayout();
		//pwest = new JPanel(westCardLayout);

		JPanel ef = new JPanel(new GridLayout(0, 1));

		addfile = new JButton("Add File");
		addfile.setActionCommand("addfile");
		addfile.addActionListener(this);
		ef.add(addfile);

		addDir = new JButton("Add Directory");
		addDir.setActionCommand("addDir");
		addDir.addActionListener(this);
		ef.add(addDir);

		remove = new JButton("Remove File");
		remove.setActionCommand("remove");
		remove.addActionListener(this);
		ef.add(remove);

		removeAll = new JButton("Remove All");
		removeAll.setActionCommand("removeall");
		removeAll.addActionListener(this);
		ef.add(removeAll);

		loadlist = new JButton("Load List");
		loadlist.setActionCommand("loadlist");
		loadlist.addActionListener(this);
		ef.add(loadlist);

		savelist = new JButton("Save List");
		savelist.setActionCommand("savelist");
		savelist.addActionListener(this);
		ef.add(savelist);

		//JPanel tr = new JPanel(new GridLayout(0, 1, 5, 2));

		/*addrun = new JButton("Add Run");
		addrun.setActionCommand("addrun");
		addrun.addActionListener(this);
		tr.add(addrun);

		addrange = new JButton("Add Range");
		addrange.setActionCommand("addrange");
		addrange.addActionListener(this);
		tr.add(addrange);

		removeruns = new JButton("Remove Run(s)");
		removeruns.setActionCommand("removeruns");
		removeruns.addActionListener(this);
		tr.add(removeruns);

		loadrunlist = new JButton("Load List");
		loadrunlist.setActionCommand("loadlist");
		loadrunlist.addActionListener(this);
		tr.add(loadrunlist);

		saverunlist = new JButton("Save List");
		saverunlist.setActionCommand("savelist");
		saverunlist.addActionListener(this);
		tr.add(saverunlist);*/

		//pwest.add(ef, DISKBUTTONS);
		//pwest.add(tr, TAPEBUTTONS);
		//westCardLayout.show(pwest, DISKBUTTONS);
		cd.add(ef, BorderLayout.WEST);

		JPanel ple = new JPanel();
		ple.setLayout(new GridLayout(0, 1, 5, 5));
		cd.add(ple, BorderLayout.EAST);

		JPanel pbottom = new JPanel();
		pbottom.setLayout(new GridLayout(0, 1, 5, 2));
		cd.add(pbottom, BorderLayout.SOUTH);

		// panel for output file
		JPanel pout = new JPanel(new BorderLayout());
		pbottom.add(pout);
		cout = new JCheckBox("Output Events to File:", false);
		cout.addItemListener(this);
		pout.add(cout,BorderLayout.WEST);

		//JLabel lh = new JLabel("File:", JLabel.LEFT);
		//pout.add(lh);

		textOutFile =
			new JTextField(defaultEvents + File.separator + defaultOutputFile);
		textOutFile.setColumns(28);
		//textOutFile.setBackground(Color.lightGray);
		//textOutFile.setForeground(Color.black);
		textOutFile.setEnabled(false);
		pout.add(textOutFile,BorderLayout.CENTER);

		bbrowse = new JButton("Browse");
		bbrowse.setActionCommand("bfileout");
		bbrowse.addActionListener(this);
		bbrowse.setEnabled(false);
		pout.add(bbrowse,BorderLayout.EAST);

		//panel with begin and end bottoms	
		JPanel pb = new JPanel();
		pb.setLayout(new GridLayout(1,0));

		bbegin = new JButton("Begin");
		bbegin.setActionCommand("begin");
		bbegin.setBackground(Color.green);
		bbegin.setEnabled(false);
		bbegin.addActionListener(this);
		pb.add(bbegin);

		bend = new JButton("End/Cancel");
		bend.setActionCommand("end");
		bend.setBackground(Color.red);
		bend.addActionListener(this);
		pb.add(bend);

		pbottom.add(pb);

		d.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				d.dispose();
			}
		});
		lastFile = new File(defaultEvents); //default directory
		//call to private version to avoid breaking if subclassed
		//setDevice(DISK); //initial mode is from disk
		writeEvents = false; //don't write out events
		d.pack();
	}

	/**
	 * method to show dialog box
	 */
	public void show() {
		d.show();
	}

	/**
	 * action performed on widget in dialog box
	 *
	 */
	public void actionPerformed(ActionEvent ae) {
		final String command = ae.getActionCommand();
		try {
			if (command == "addfile") {
				addEventFile();
			/*} else if (command == "addrun") {
				addRunNumber();*/
			} else if (command == "addDir") {
				addDirectory();
			} else if (command == "remove") {
				removeItem();
			} else if (command == "removeall") {
				removeAllItems();
			} else if (command == "savelist") {
				Savelist();
			} else if (command == "loadlist") {
				Loadlist();
			/*} else if (command == "addrange") {
				addRunNumberRange();
			} else if (command == "removeruns") {
				removeRuns();*/
			} else if (command == "begin") {
				loadNames();
				lockFields(true);
				beginSort();
			} else if (command == "end") {
				endSort();
				msgHandler.warningOutln(
					"Ended offline sorting before reading all events.");
				//lockFields(false);
			} else if (command == "bfileout") {
				textOutFile.setText(getOutFile().getPath());
			}
		} catch (SortException se) {
			msgHandler.errorOutln(se.getMessage());
		} catch (EventException ee) {
			msgHandler.errorOutln(ee.getMessage());
		} catch (JamException je) {
			msgHandler.errorOutln(je.getMessage());
		} catch (GlobalException ge) {
			msgHandler.errorOutln(ge.getMessage());
		}
	}

	/**
	 * Recieves events from the check box.
	 *
	 */
	public void itemStateChanged(ItemEvent ie) {

		if (ie.getItemSelectable() == cout) {
			if (cout.isSelected()) {
				textOutFile.setEnabled(true);
				//textOutFile.setBackground(Color.white);
				bbrowse.setEnabled(true);
				writeEvents = true;
			} else {
				textOutFile.setEnabled(false);
				//textOutFile.setBackground(Color.lightGray);
				bbrowse.setEnabled(false);
				writeEvents = false;
			}
		}
	}

	/**
	 * Setup up called by SetupSortOff  
	 *
	 */
	public void setup(
		SortDaemon sortDaemon,
		StorageDaemon fromDaemon,
		StorageDaemon toDaemon,
		String devName) {
		//this.setupSort = setupSort;
		this.sortDaemon = sortDaemon;
		this.dataInpDaemon = fromDaemon;
		this.dataOutDaemon = toDaemon;
		this.deviceName = devName;
		//textDev.setText(deviceName);
		bbegin.setEnabled(true);
		/*if (dataInpDaemon instanceof DiskDaemon) {
			setDevice(DISK);*/
			/*} else if (dataInpDaemon instanceof TapeDaemon) {
				setDevice(TAPE);*/
		/*} else {
			System.err.println(
				"Error unknown storageDaemon type " + dataInpDaemon);
		}*/
	}

	/**
	 * Load the name of objects entered in dialog box
	 * give the list to storage deamon
	 */
	private void loadNames() throws JamException, SortException {
		//int count = 0;
		/*if (device == TAPE) { //if sorting from tape
			Vector runList = new Vector(tapeRecordModel.getSize());
			try {
				for (count = 0; count < tapeRecordModel.getSize(); count++) {
					//is it an integer
					int numberRun =
						Integer.parseInt(
							((String) (tapeRecordModel.getElementAt(count)))
								.trim());
					runList.addElement(new Integer(numberRun));
				}
			} catch (NumberFormatException nfe) {
				throw new JamException(
					"Run number not a integer "
						+ ((String) (tapeRecordModel.getElementAt(count)))
							.trim());
			}
			dataInpDaemon.setEventInputList(runList);
			//tell storage daemon list of run
		} else { //if sorting from disk*/
		Vector fileList = new Vector(eventFileModel.getSize());
		for (int count = 0; count < eventFileModel.getSize(); count++) {
			String fileName =
				((String) (eventFileModel.getElementAt(count))).trim();
			fileList.addElement(fileName); //add to list		
		}
		//tell storage daemon list of files
		dataInpDaemon.setEventInputList(fileList);
		//}
		//save output file
		fileOut = new File(textOutFile.getText().trim());
		msgHandler.messageOutln("Loaded list of sort files");
	}

	/**
	 * start sorting offline
	 */
	public void beginSort()
		throws SortException, EventException, GlobalException {
		RunInfo.runNumber = 999;
		RunInfo.runTitle = "Pre-sorted data";
		RunInfo.runStartTime = new java.util.Date();
		//are we writing out events
		if (writeEvents) {
			sortDaemon.setWriteEnabled(true);
			dataOutDaemon.openEventOutputFile(fileOut);
			dataOutDaemon.writeHeader();
		} else {
			sortDaemon.setWriteEnabled(false);
		}
		//offline from disk
		//if (device == DISK) {
			msgHandler.messageOutln("Starting sorting from Disk");
			//offline from disk		
		/*} else {
			msgHandler.messageOutln("Starting sorting from Tape");
		}*/
		bbegin.setEnabled(false);
		sortDaemon.setState(GoodThread.RUN);
		jamMain.setRunState(RunState.ACQ_ON);
	}

	/**
	 * stop offline sorting
	 *
	 */
	private void endSort()
		throws JamException, SortException, GlobalException {
		sortDaemon.cancelOfflineSorting();
		if (!dataInpDaemon.closeEventInputListFile()) {
			msgHandler.errorOutln(
				"Closing sort input event file: "
					+ dataInpDaemon.getEventInputFileName());
		}
		if (writeEvents) {
			dataOutDaemon.closeEventOutputFile();
			msgHandler.messageOutln(
				"Closed pre-sorted file: " + fileOut.getPath());
		}
		//setupSort.resetSort();
		//if (device == DISK) {
			msgHandler.warningOutln(
				"Stopped sorting from disk before all events were read.");
		/*} else { //from tape
			msgHandler.messageOutln(
				"Stopped sorting from tape before all events were read.");
		}*/
		jamMain.setRunState(RunState.ACQ_OFF);
	}

	/**
	 * Called at the start of a new sort thread by
	 * the sort thread. All it does is suspend the
	 * <code>SortDaemon</code> thread, to make the
	 * offline sorting loop wait at its beginning for
	 * the thread to be resumed when the user requests
	 * the sort to begin.
	 */
	public void atSortStart() throws GlobalException {
		sortDaemon.setState(GoodThread.SUSPEND);
	}

	/**
	 * Called by sorter after startup looking to see if there is a next file
	 * to sort. If there is a nex file we tell <code>StorageDaemon</code> to open it
	 * if <code>storageDaemon</code> can open it we return true.
	 *
	 * @return <code>true</code> if there is a next event file to sort
	 */
	public boolean isSortNext() {
		boolean sortNext = false;
		if (!dataInpDaemon.closeEventInputListFile()) {
			msgHandler.errorOutln(
				"Could not close file: "
					+ dataInpDaemon.getEventInputFileName());
		}
		//if (device == DISK) {
			if (dataInpDaemon.hasMoreFiles()) {
				if (dataInpDaemon.openEventInputListFile()) {
					msgHandler.messageOutln(
						"Sorting next file: "
							+ dataInpDaemon.getEventInputFileName());
					msgHandler.messageOutln(
						"  Run number: "
							+ RunInfo.runNumber
							+ " title: "
							+ RunInfo.runTitle);
					sortNext = true;

				} else {
					msgHandler.errorOutln(
						"Could not open file: "
							+ dataInpDaemon.getEventInputFileName());
					sortNext = true; //try next file anyway
				}
			}
		/*} else { //sorting from tape
			if (dataInpDaemon.hasMoreFiles()) {
				msgHandler.messageOutln("Looking for runs ...");
				if (dataInpDaemon.openEventInputListFile()) {
					msgHandler.messageOutln(
						"Found run " + dataInpDaemon.getEventInputFileName());
					msgHandler.messageOutln(
						"  Run number: "
							+ RunInfo.runNumber
							+ " title: "
							+ RunInfo.runTitle);
					sortNext = true;
				} else { //return sortNext=false
					msgHandler.messageOutln(
						"Could not find run: "
							+ dataInpDaemon.getEventInputFileName());
				}
			}
		}*/
		return sortNext;
	}

	/**
	 * Called back by sorter when sort encounters a end-run-marker.
	 * Tell StorageDaemon to close file. 
	 * Tells user sorting is done and unlocks fields so that 
	 * new files can be input to sort.
	 * 
	 */
	public void atSortEnd() {
		try {
			msgHandler.messageOutln("Sorting all done");
			jamMain.setRunState(RunState.ACQ_OFF);
			if (!dataInpDaemon.closeEventInputListFile()) {
				msgHandler.errorOutln("Couldn't close file [SortControl]");
			}
			if (writeEvents) {
				dataOutDaemon.closeEventOutputFile();
				msgHandler.messageOutln(
					"Closed pre-sorted file: " + fileOut.getPath());
			}
			bbegin.setEnabled(true);
			//Toolkit.getDefaultToolkit().beep();
			lockFields(false);
		} catch (SortException se) {
			msgHandler.errorOutln(
				"Unable to close event output file [SortControl]");
		}
	}

	/**
	 * Method not implement for SortControl,
	 * called by sorting when at end of writing file.
	 * implemented in RunControl
	 */
	public void atWriteEnd() {
		/* does nothing  */
	}

	/**
	 * Sets TAPE or DISK mode.
	 * 
	 * @ device <code>SortControl.TAPE</code> or <code>SortControl.DISK</code>
	 */
	//public final void setDevice(int device) {
		//this.device = device;
		/*if (device == TAPE) {
			centerCardLayout.show(pcenter, TAPERECORDS);
			westCardLayout.show(pwest, TAPEBUTTONS);
			textInitRecord.setEditable(true);
			//textInitRecord.setBackground(Color.white);
			textFinalRecord.setEditable(true);
			//textFinalRecord.setBackground(Color.white);
			textDev.setEnabled(true);
		} else if (device == DISK) {*/
			//centerCardLayout.show(pcenter, DISKFILES);
			//westCardLayout.show(pwest, DISKBUTTONS);
			/*textInitRecord.setEditable(false);
			//textInitRecord.setBackground(Color.lightGray);
			textInitRecord.setText("");
			textFinalRecord.setEditable(false);
			//textFinalRecord.setBackground(Color.lightGray);
			textFinalRecord.setText("");
			textDev.setEnabled(true);*/
		//}
	//}

	/**
	 * Sets TAPE or DISK mode.
	 * 
	 * @ device <code>SortControl.TAPE</code> or <code>SortControl.DISK</code>
	 */
	//public void setDevice(int device) {
	/* delegate to private method to allow constructor
	 * to avoid a call to an overridable method
	 */
	/*	_setDevice(device);
	}*/

	/**
	 * browse for event files
	 */
	private void addEventFile() {
		JFileChooser fd = new JFileChooser(lastFile);
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.setFileFilter(
			new ExtensionFileFilter(
				new String[] { "evn" },
				"Event Files (*.evn)"));
		int option = fd.showOpenDialog(jamMain);
		//save current values
		if (option == JFileChooser.APPROVE_OPTION
			&& fd.getSelectedFile() != null) {
			lastFile = fd.getSelectedFile(); //save current directory
			eventFileModel.addElement(fd.getSelectedFile().getPath());
		}
	}

	/**
	 * add run numbers
	 */
	/*private void addRunNumber() throws JamException {
		try {
			Integer j =
				new Integer(Integer.parseInt(textInitRecord.getText().trim()));
			tapeRecordModel.addElement(j.toString());
		} catch (NumberFormatException nfe) {
			throw new JamException(
				"Run number not a integer " + textInitRecord.getText());
		}

	}*/
	/**
	 * add a range of run numbers
	 */
	/*private void addRunNumberRange() throws JamException {
		;

		Integer j;
		int ri, rf;
		//make sure inputs are integers
		try {
			ri = Integer.parseInt(textInitRecord.getText().trim());

		} catch (NumberFormatException nfe) {
			throw new JamException(
				"Run number not a integer " + textInitRecord.getText());
		}
		try {
			rf = Integer.parseInt(textFinalRecord.getText().trim());
		} catch (NumberFormatException nfe) {
			throw new JamException(
				"Run number not a integer " + textFinalRecord.getText());
		}

		for (int i = ri; i <= rf; i++) {
			j = new Integer(i);
			tapeRecordModel.addElement(j.toString());
		}
	}*/
	/**
	 * add all files in a directory to sort
	 *
	 */
	private void addDirectory() {
		JFileChooser fd = new JFileChooser(lastFile);
		fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		ExtensionFileFilter ff =
			new ExtensionFileFilter(
				new String[] { "evn" },
				"Event Files (*.evn)");
		int option = fd.showOpenDialog(jamMain);
		//save current values
		if (option == JFileChooser.APPROVE_OPTION
			&& fd.getSelectedFile() != null) {
			lastFile = fd.getSelectedFile(); //save current directory
			if (lastFile != null) {
				File[] dirArray = fd.getSelectedFile().listFiles();
				for (int i = 0; i < dirArray.length; i++) {
					if (ff.accept(dirArray[i]))
						eventFileModel.addElement(dirArray[i].getPath());
				}
			}
		}
	}

	/**
	 * remove a item from sort list
	 */
	private void removeItem() {
		Object[] removeList = listEventFiles.getSelectedValues();
		for (int i = 0; i < removeList.length; i++) {
			eventFileModel.removeElement(removeList[i]);
		}
	}

	/**
	 * remove all items from sort list 
	 *
	 */
	private void removeAllItems() {
		eventFileModel.removeAllElements();
	}

	/**
	 * remove a run from the list
	 */
	/*private void removeRuns() {
		Object[] removeList = listTapeRecords.getSelectedValues();
		for (int i = 0; i < removeList.length; i++) {
			tapeRecordModel.removeElement(removeList[i]);
		}
	}*/

	/**
	 * save list of items to sort
	 */
	private void Savelist() throws JamException {
		JFileChooser fd = new JFileChooser(lastFile);
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.setFileFilter(
			new ExtensionFileFilter(
				new String[] { "lst" },
				"List Files (*.lst)"));
		int option = fd.showSaveDialog(jamMain);
		//save current values
		if (option == JFileChooser.APPROVE_OPTION
			&& fd.getSelectedFile() != null) {
			lastFile = fd.getSelectedFile(); //save current directory
		}
		try {
			/*if (device == TAPE) {
				FileWriter saveStream = new FileWriter(fd.getSelectedFile());
				for (int i = 0; i < tapeRecordModel.size(); i++) {
					saveStream.write(tapeRecordModel.elementAt(i) + "\n");
				}
				saveStream.close();
			} else {*/
				FileWriter saveStream = new FileWriter(fd.getSelectedFile());
				for (int i = 0; i < eventFileModel.size(); i++) {
					saveStream.write(eventFileModel.elementAt(i) + "\n");
				}
				saveStream.close();
			//}
		} catch (IOException ioe) {
			throw new JamException("Unable to save list to file, open file [SortControl]");
		}
	}

	/**
	 * load a list of items to sort from a file
	 *
	 */
	private void Loadlist() throws JamException {
		String listItem;

		JFileChooser fd = new JFileChooser(lastFile);
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.setFileFilter(
			new ExtensionFileFilter(
				new String[] { "lst" },
				"List Files (*.lst)"));
		int option = fd.showOpenDialog(jamMain);
		//save current values
		if (option == JFileChooser.APPROVE_OPTION
			&& fd.getSelectedFile() != null) {
			lastFile = fd.getSelectedFile(); //save current directory
			DefaultListModel dlm;
			/*if (device == TAPE) {
				dlm = tapeRecordModel;
			} else { //DISK*/
				dlm = eventFileModel;
			//}
			try {
				BufferedReader br =
					new BufferedReader(new FileReader(lastFile));
				do {
					listItem = br.readLine();
					if (listItem != null) {
						dlm.addElement(listItem);
					}
				} while (listItem != null);
				br.close();
			} catch (IOException ioe) {
				throw new JamException("Unable to load list, open file [SortControl]");
			}
		}
	}

	/**
	 * Is the Browse for the File Output Name 
	 *
	 */
	private File getOutFile() {
		File rval = null; //default return value
		JFileChooser fd = new JFileChooser(outDirectory);
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.setFileFilter(
			new ExtensionFileFilter(
				new String[] { "evn" },
				"Event Files (*.evn)"));
		int option = fd.showOpenDialog(jamMain);
		//save current values
		if (option == JFileChooser.APPROVE_OPTION
			&& fd.getSelectedFile() != null) {
			outDirectory = fd.getSelectedFile(); //save current directory
			rval = outDirectory;
		}
		return rval;
	}

	/** 
	 * Lock the file and record input list while sorting
	 * This method is called when sorting is actived to lock fields
	 * again when done to unlock fields
	 */
	private void lockFields(boolean lock) {
		//if (lock) {
		setupLock = lock;
		final boolean notLock = !lock;
		addfile.setEnabled(notLock);
		addDir.setEnabled(notLock);
		remove.setEnabled(notLock);
		loadlist.setEnabled(notLock);
		savelist.setEnabled(notLock);
//		addrun.setEnabled(notLock);
//		addrange.setEnabled(notLock);
//		removeruns.setEnabled(notLock);
		removeAll.setEnabled(notLock);
//		loadrunlist.setEnabled(notLock);
//		saverunlist.setEnabled(notLock);
		//textInitRecord.setEnabled(notLock);
		//textFinalRecord.setEnabled(notLock);
		textOutFile.setEditable(notLock);
		cout.setEnabled(notLock);
		/*} else {
			setupLock = false;
			addfile.setEnabled(true);
			addDir.setEnabled(true);
			remove.setEnabled(true);
			loadlist.setEnabled(true);
			savelist.setEnabled(true);
			addrun.setEnabled(true);
			addrange.setEnabled(true);
			removeruns.setEnabled(true);
			removeAll.setEnabled(true);
			loadrunlist.setEnabled(true);
			saverunlist.setEnabled(false);
			textInitRecord.setEnabled(true);
			textFinalRecord.setEnabled(true);
			textOutFile.setEditable(true);
			cout.setEnabled(true);
		}*/
	}
}
