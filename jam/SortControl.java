package jam;
import jam.global.GoodThread;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.global.RunInfo;
import jam.io.ExtensionFileFilter;
import jam.sort.Controller;
import jam.sort.SortDaemon;
import jam.sort.SortException;
import jam.sort.StorageDaemon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
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
import javax.swing.border.EmptyBorder;

/**
 * Class to control the offline sort process
 * Allows you to enter the list of files to sort
 * an the output pre-sorted file if you have one
 *
 * @author Dale Visser and Ken Swartz
 * @version 1.0
 */
public final class SortControl extends JDialog implements Controller{

	private final Frame jamMain;
	private final MessageHandler msgHandler;

	/* daemon threads */
	private StorageDaemon dataInpDaemon;
	private StorageDaemon dataOutDaemon;
	private SortDaemon sortDaemon;

	private File lastFile; //last file referred to in a JFileChooser
	private File fileOut; //file name for output
	
	private File outDirectory; //directory we last output files to.

	private boolean setupLock;
	private boolean writeEvents;

	/**
	 *  Dialog box widget
	 */
	//private final JDialog d;

	/**
	 * Text field for output file
	 */
	private final JTextField textOutFile;

	/** check box for writing out events */
	private final JCheckBox cout;

	private final JPanel pdiskfiles;

	private final JList listEventFiles;
	private final DefaultListModel eventFileModel;

	private final JButton addfile,
		addDir,
		loadlist,
		remove,
		removeAll,
		savelist;
		
	private final static JamStatus status=JamStatus.instance();

	/**
	  * button to get file brower
	  */
	private final JButton bbrowse;

	/**
	  *control button for begin sort
	  */
	private final JButton bbegin;

	/**
	  * control button for end sort
	  */
	private final JButton bend;

	String defaultEvents;
	String defaultOutputFile;

	private static SortControl instance=null;
	public static SortControl getSingletonInstance(){
		if (instance==null){
			instance=new SortControl();
		}
		return instance;
	}

	private SortControl() {
		super(status.getFrame(),"Sorting",false);
		msgHandler = status.getMessageHandler();
		jamMain = status.getFrame();
		defaultEvents =
			JamProperties.getPropString(JamProperties.EVENT_OUTPATH);
		defaultOutputFile =
			JamProperties.getPropString(JamProperties.EVENT_OUTFILE);
		setResizable(true);//sometimes there are long paths to files
		setLocation(20, 50);

		//GUI layout
		final Container cd = getContentPane();
		cd.setLayout(new BorderLayout(10, 10));

		//Top Panel
		final JPanel ptop = new JPanel(new FlowLayout(FlowLayout.CENTER));
		ptop.setBorder(new EmptyBorder(10,0,0,0));
		cd.add(ptop, BorderLayout.NORTH);
		ptop.add(new JLabel("Event Files to Sort",JLabel.RIGHT));

		//List Panel
		pdiskfiles = new JPanel(new BorderLayout(5,5));
		pdiskfiles.setBorder(new EmptyBorder(0,0,0,20));
		cd.add(pdiskfiles, BorderLayout.CENTER);

		eventFileModel = new DefaultListModel();
		listEventFiles = new JList(eventFileModel);
		listEventFiles.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		pdiskfiles.add(new JScrollPane(listEventFiles), BorderLayout.CENTER);


		//Commands Panel
		final JPanel ef = new JPanel(new GridLayout(0, 1,5,2));
		ef.setBorder(new EmptyBorder(0,10,0,0));
		cd.add(ef, BorderLayout.WEST);

		addfile = new JButton("Add File");
		addfile.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				addEventFile();
			}
		});
		ef.add(addfile);

		addDir = new JButton("Add Directory");
		addDir.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent ae){
			addDirectory();
		}
		});
		ef.add(addDir);

		remove = new JButton("Remove File");
		remove.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent ae){
			removeItem();
		}
		});
		ef.add(remove);

		removeAll = new JButton("Remove All");
		removeAll.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent ae){
			removeAllItems();
		}
		});
		ef.add(removeAll);

		loadlist = new JButton("Load List");
		loadlist.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent ae){
			Loadlist();
		}
		});
		ef.add(loadlist);

		savelist = new JButton("Save List");
		savelist.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent ae){
			Savelist();
		}
		});
		ef.add(savelist);

		//Bottom Panel
		final JPanel pbottom = new JPanel(new GridLayout(0, 1, 5, 5));
		pbottom.setBorder(new EmptyBorder(0,5,0,10));
		cd.add(pbottom, BorderLayout.SOUTH);

		// panel for output file
		final JPanel pout = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pbottom.add(pout);
		cout = new JCheckBox("Output Events to File:", false);
		cout.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				setWriteEvents(cout.isSelected());
			}
		});
		pout.add(cout);

		textOutFile =
			new JTextField(defaultEvents + File.separator + defaultOutputFile);
		textOutFile.setColumns(28);
		textOutFile.setEnabled(false);
		pout.add(textOutFile);

		bbrowse = new JButton("Browse..");
		bbrowse.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent ae){
			textOutFile.setText(getOutFile().getPath());
		}
		});
		bbrowse.setEnabled(false);
		pout.add(bbrowse);

		//panel with begin and end bottoms
		final JPanel pbutton = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pbottom.add(pbutton);
		final JPanel pb = new JPanel(new GridLayout(1,0,5,5));
		pbutton.add(pb);

		bbegin = new JButton("Begin");
		bbegin.setToolTipText("Begin sort of all files."
		+" If a sort was halted, we start over.");
		bbegin.setBackground(Color.GREEN);
		bbegin.setEnabled(false);
		bbegin.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent ae){
			beginSort();
		}
		});
		pb.add(bbegin);

		bend = new JButton("Halt");
		bend.setToolTipText("Halt sort in process.");
		bend.setBackground(Color.RED);
		bend.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent ae){
			endSort();
		}
		});
		bend.setEnabled(false);
		pb.add(bend);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		lastFile = new File(defaultEvents); //default directory
		writeEvents = false; //don't write out events
		pack();
	}

	void setWriteEvents(boolean state){
		textOutFile.setEnabled(state);
		bbrowse.setEnabled(state);
		writeEvents = state;
	}

	/**
	 * Setup up called by SetupSortOff
	 *
	 */
	public void setup(
		SortDaemon sortDaemon,
		StorageDaemon fromDaemon,
		StorageDaemon toDaemon) {
		this.sortDaemon = sortDaemon;
		this.dataInpDaemon = fromDaemon;
		this.dataOutDaemon = toDaemon;
		bbegin.setEnabled(true);
	}

	/**
	 * Load the name of objects entered in dialog box
	 * give the list to storage deamon
	 */
	private void loadNames() {
		final List fileList = new Vector(eventFileModel.getSize());
		for (int count = 0; count < eventFileModel.getSize(); count++) {
			final File f=(File)eventFileModel.get(count);
			fileList.add(f);
		}
		/* tell storage daemon list of files */
		dataInpDaemon.setEventInputList(fileList);
		/* save output file */
		fileOut = new File(textOutFile.getText().trim());
		msgHandler.messageOutln("Loaded list of sort files");
	}

	/**
	 * start sorting offline
	 *
	 * @param lockOnThis passed by script thread, which wants to know when
	 * sorting is done
	 */
	public void beginSort() {
		loadNames();
		lockFields(true);
		RunInfo.runNumber = 999;
		RunInfo.runTitle = "Pre-sorted data";
		RunInfo.runStartTime = new java.util.Date();
		if (writeEvents) {
			sortDaemon.setWriteEnabled(true);
			boolean openSuccess=true;
			try {
				dataOutDaemon.openEventOutputFile(fileOut);
			} catch (SortException e){
				msgHandler.errorOutln(
				"Sort|Control.Begin: couldn't open event output file.");
				sortDaemon.setWriteEnabled(false);
				openSuccess=false;
			}
			if (openSuccess){
				try {
					dataOutDaemon.writeHeader();
				} catch (Exception e){
					msgHandler.errorOutln(
					"Sort|Control.Begin: couldn't write header to event output file.");
				}
			}
		} else {
			sortDaemon.setWriteEnabled(false);
		}
		msgHandler.messageOutln("Starting sorting from Disk");
		bbegin.setEnabled(false);
		bend.setEnabled(true);
		sortDaemon.setState(GoodThread.RUN);
		status.setRunState(RunState.ACQ_ON);
	}

	/**
	 * stop offline sorting
	 *
	 */
	private void endSort() {
		sortDaemon.cancelOfflineSorting();
		if (!dataInpDaemon.closeEventInputListFile()) {
			msgHandler.errorOutln(
				"Closing sort input event file: "
					+ dataInpDaemon.getEventInputFileName());
		}
		if (writeEvents) {
			try {
				dataOutDaemon.closeEventOutputFile();
			} catch (SortException e){
				msgHandler.errorOutln(
				"Sort|Control...: couldn't close event output file.");
			}
			msgHandler.messageOutln(
				"Closed pre-sorted file: " + fileOut.getPath());
		}
		status.setRunState(RunState.ACQ_OFF);
		msgHandler.warningOutln(
			"Ended offline sorting before reading all events.");
		bend.setEnabled(false);
	}

	/**
	 * Called at the start of a new sort thread by
	 * the sort thread. All it does is suspend the
	 * <code>SortDaemon</code> thread, to make the
	 * offline sorting loop wait at its beginning for
	 * the thread to be resumed when the user requests
	 * the sort to begin.
	 */
	public void atSortStart() {
		sortDaemon.setState(GoodThread.SUSPEND);
	}

	/**
	 * Called by sorter after startup looking to see if there is a 
	 * next file to sort. If there is a next file, we tell 
	 * <code>StorageDaemon</code> to open it, and return 
	 * <ocde>true</code>.
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
			} else {
				msgHandler.errorOutln(
					"Could not open file: "
						+ dataInpDaemon.getEventInputFileName());
			}
			sortNext=true;//try next file no matter what
		}
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
			status.setRunState(RunState.ACQ_OFF);
			if (!dataInpDaemon.closeEventInputListFile()) {
				msgHandler.errorOutln("Couldn't close file [SortControl]");
			}
			if (writeEvents) {
				dataOutDaemon.closeEventOutputFile();
				msgHandler.messageOutln(
					"Closed pre-sorted file: " + fileOut.getPath());
			}
			bbegin.setEnabled(true);
			lockFields(false);
			/* let other thread (i.e., jam.Script) know we are finished */
			bend.setEnabled(false);
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
			eventFileModel.addElement(fd.getSelectedFile());
		}
	}

	/**
	 * add all files in a directory to sort
	 *
	 */
	private void addDirectory() {
		final JFileChooser fd = new JFileChooser(lastFile);
		fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		final int option = fd.showOpenDialog(jamMain);
		/* save current values */
		if (option == JFileChooser.APPROVE_OPTION
			&& fd.getSelectedFile() != null) {
			lastFile = fd.getSelectedFile(); //save current directory
			addEventFile(lastFile);
		}
	}

	int addEventFile(File f){
		int numFiles=0;
		if (f != null && f.exists()){
			final ExtensionFileFilter ff =
				new ExtensionFileFilter(
					new String[] { "evn" },
					"Event Files (*.evn)");
			if (f.isFile() && ff.accept(f)){
				eventFileModel.addElement(f);
				numFiles++;
			}
			if (f.isDirectory()){
				File[] dirArray = f.listFiles();
				for (int i = 0; i < dirArray.length; i++) {
					if (ff.accept(dirArray[i]))
						eventFileModel.addElement(dirArray[i]);
						numFiles++;
				}
			}
		}
		return numFiles;
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
	 * save list of items to sort
	 */
	private void Savelist() {
		JFileChooser fd = new JFileChooser(lastFile);
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.setFileFilter(
			new ExtensionFileFilter(
				new String[] { "lst" },
				"List Files (*.lst)"));
		int option = fd.showSaveDialog(jamMain);
		/* save current values */
		if (option == JFileChooser.APPROVE_OPTION
			&& fd.getSelectedFile() != null) {
			lastFile = fd.getSelectedFile(); //save current directory
			try {
					FileWriter saveStream = new FileWriter(lastFile);
					for (int i = 0; i < eventFileModel.size(); i++) {
						final File f=(File)eventFileModel.elementAt(i);
						saveStream.write(f.getAbsolutePath());
						saveStream.write("\n");
					}
					saveStream.close();
			} catch (IOException ioe) {
				msgHandler.errorOutln("Control|Sort...:Unable to save list to file "+lastFile.getName());
			}
		}
	}

	/**
	 * load a list of items to sort from a file
	 *
	 */
	private void Loadlist() {
		JFileChooser fd = new JFileChooser(lastFile);
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.setFileFilter(
			new ExtensionFileFilter(
				new String[] { "lst" },
				"List Files (*.lst)"));
		int option = fd.showOpenDialog(jamMain);
		if (option == JFileChooser.APPROVE_OPTION
			&& fd.getSelectedFile() != null) {
			readList(fd.getSelectedFile());
		}
	}

	int readList(File f) {
		int numFiles=0;
		lastFile=f;
		try {
			BufferedReader br =
				new BufferedReader(new FileReader(lastFile));
			String listItem;
			do {
				listItem = br.readLine();
				if (listItem != null) {
					final File fEvn=new File(listItem);
					eventFileModel.addElement(fEvn);
					numFiles++;
				}
			} while (listItem != null);
			br.close();
		} catch (IOException ioe) {
			msgHandler.errorOutln("Jam|Sort...: Unable to load list from file "+f);
		}
		return numFiles;
	}

	/**
	 * Is the Browse for the File Output Name
	 *
	 */
	private File getOutFile() {
		File rval = new File(textOutFile.getText().trim()); //default return value
		JFileChooser fd = new JFileChooser(outDirectory);
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.setFileFilter(
			new ExtensionFileFilter(
				new String[] { "evn" },
				"Event Files (*.evn)"));
		int option = fd.showOpenDialog(jamMain);
		/* save current values */
		if (option == JFileChooser.APPROVE_OPTION
			&& fd.getSelectedFile() != null) {
			outDirectory = fd.getSelectedFile(); //save current directory
			rval = outDirectory;
		}
		return rval;
	}

	void setEventOutput(File f){
		outDirectory=f;
		textOutFile.setText(f.getAbsolutePath());
		setWriteEvents(true);
	}

	/**
	 * Lock the file and record input list while sorting
	 * This method is called when sorting is actived to lock fields
	 * again when done to unlock fields
	 */
	private void lockFields(boolean lock) {
		setupLock = lock;
		final boolean notLock = !lock;
		addfile.setEnabled(notLock);
		addDir.setEnabled(notLock);
		remove.setEnabled(notLock);
		loadlist.setEnabled(notLock);
		savelist.setEnabled(notLock);
		removeAll.setEnabled(notLock);
		textOutFile.setEditable(notLock);
		cout.setEnabled(notLock);
	}
}