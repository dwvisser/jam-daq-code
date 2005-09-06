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
import jam.ui.MultipleFileChooser;

import java.awt.BorderLayout;
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
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

/**
 * Class to control the offline sort process Allows you to enter the list of
 * files to sort an the output pre-sorted file if you have one
 * 
 * @author Dale Visser and Ken Swartz
 * @version 1.0
 */
public final class SortControl extends JDialog implements Controller {

	private final static JamStatus STATUS = JamStatus.getSingletonInstance();

	private static final Frame frame = STATUS.getFrame();

	private static SortControl instance = null;

	private static final MessageHandler msgHandler = STATUS.getMessageHandler();

	/**
	 * 
	 * @return the only instance of this class
	 */
	public static SortControl getInstance() {
		if (instance == null) {
			instance = new SortControl();
		}
		return instance;
	}

	/**
	 * button to get file brower
	 */
	private transient final JButton bbrowse;

	private transient final Action beginAction = new AbstractAction() {
		{
			putValue(Action.NAME, "Begin");
			putValue(Action.SHORT_DESCRIPTION, "Begin sort of all files."
					+ " If a sort was halted, we start over.");
			final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
			final ImageIcon icon = new ImageIcon(classLoader
					.getResource("jam/begin.png"));
			putValue(Action.SMALL_ICON, icon);
			setEnabled(false);
		}

		public void actionPerformed(final ActionEvent actionEvent) {
			beginSort();
		}
	};

	/** check box for writing out events */
	private transient final JCheckBox cout;

	private transient File fileOut, lastFile, outDirectory;

	private transient final Action haltAction = new AbstractAction() {
		{
			putValue(Action.NAME, "Halt");
			putValue(Action.SHORT_DESCRIPTION, "Halt sort in process.");
			final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
			final ImageIcon icon = new ImageIcon(classLoader
					.getResource("jam/end.png"));
			putValue(Action.SMALL_ICON, icon);
			setEnabled(false);
		}

		public void actionPerformed(final ActionEvent actionEvent) {
			endSort();
		}
	};

	/* daemon threads */
	private transient StorageDaemon inputDaemon;

	private transient final MultipleFileChooser multiFile;

	private transient StorageDaemon outputDaemon;

	private transient SortDaemon sortDaemon;

	/**
	 * Text field for output file
	 */
	private transient final JTextField textOutFile;

	private transient boolean writeEvents;

	private SortControl() {
		super(STATUS.getFrame(), "Sorting", false);
		final String eventDefault = JamProperties
				.getPropString(JamProperties.EVENT_OUTPATH);
		final String outDefault = JamProperties
				.getPropString(JamProperties.EVENT_OUTFILE);
		setResizable(true);// sometimes there are long paths to files
		setLocation(20, 50);
		/* GUI layout */
		final Container contents = getContentPane();
		contents.setLayout(new BorderLayout(10, 10));
		/* Top Panel */
		final JPanel ptop = new JPanel(new FlowLayout(FlowLayout.CENTER));
		ptop.setBorder(new EmptyBorder(10, 0, 0, 0));
		contents.add(ptop, BorderLayout.NORTH);
		ptop.add(new JLabel("Event Files to Sort", SwingConstants.RIGHT));
		/* List Panel */
		multiFile = new MultipleFileChooser(STATUS.getFrame(), msgHandler);
		multiFile.showListSaveLoadButtons(true);
		multiFile.setFileFilter("Event Files", "evn");
		contents.add(multiFile, BorderLayout.CENTER);
		/* Bottom Panel */
		final JPanel pbottom = new JPanel(new GridLayout(0, 1, 5, 5));
		pbottom.setBorder(new EmptyBorder(0, 5, 0, 10));
		contents.add(pbottom, BorderLayout.SOUTH);
		/* panel for output file */
		final JPanel pout = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pbottom.add(pout);
		cout = new JCheckBox("Output Events to File:", false);
		cout.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent itemEvent) {
				setWriteEvents(cout.isSelected());
			}
		});
		pout.add(cout);

		textOutFile = new JTextField(eventDefault + File.separator + outDefault);
		textOutFile.setColumns(28);
		textOutFile.setEnabled(false);
		pout.add(textOutFile);

		bbrowse = new JButton("Browse..");
		bbrowse.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				textOutFile.setText(getOutFile().getPath());
			}
		});
		bbrowse.setEnabled(false);
		pout.add(bbrowse);

		// panel with begin and end bottoms
		final JPanel pbutton = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pbottom.add(pbutton);
		final JPanel buttonGrid = new JPanel(new GridLayout(1, 0, 5, 5));
		pbutton.add(buttonGrid);
		buttonGrid.add(new JButton(beginAction));
		buttonGrid.add(new JButton(haltAction));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		lastFile = new File(eventDefault); // default directory
		writeEvents = false; // don't write out events
		pack();
	}

	/*
	 * non-javadoc: For scripting
	 */
	int addEventFile(final File file) {
		int numFiles = 0;
		if (file != null && file.exists()) {
			final ExtensionFileFilter fileFilter = new ExtensionFileFilter(
					new String[] { "evn" }, "Event Files (*.evn)");
			if (file.isFile() && fileFilter.accept(file)) {
				multiFile.addFile(file);
				numFiles++;
			} else if (file.isDirectory()) {
				final File[] dirArray = file.listFiles();
				numFiles += addFiles(fileFilter, dirArray);
			}
		}
		return numFiles;
	}

	private int addFile(final String fileName) {
		final int rval;
		if (fileName == null) {
			rval = 0;
		} else {
			final File fEvn = new File(fileName);
			multiFile.addFile(fEvn);
			rval = 1;
		}
		return rval;
	}

	private int addFiles(final FileFilter fileFilter, final File[] files) {
		int rval = 0;
		for (int i = 0; i < files.length; i++) {
			if (fileFilter.accept(files[i])) {
				multiFile.addFile(files[i]);
			}
			rval++;
		}
		return rval;
	}

	/**
	 * Called back by sorter when sort encounters a end-run-marker. Tell
	 * StorageDaemon to close file. Tells user sorting is done and unlocks
	 * fields so that new files can be input to sort.
	 * 
	 */
	public void atSortEnd() {
		try {
			msgHandler.messageOutln("Sorting all done");
			STATUS.setRunState(RunState.ACQ_OFF);
			if (!inputDaemon.closeEventInputListFile()) {
				msgHandler.errorOutln("Couldn't close file [SortControl]");
			}
			if (writeEvents) {
				outputDaemon.closeEventOutputFile();
				msgHandler.messageOutln("Closed pre-sorted file: "
						+ fileOut.getPath());
			}
			beginAction.setEnabled(true);
			lockFields(false);
			/* let other thread (i.e., jam.Script) know we are finished */
			haltAction.setEnabled(false);
		} catch (SortException se) {
			msgHandler
					.errorOutln("Unable to close event output file [SortControl]");
		}
	}

	/**
	 * Called at the start of a new sort thread by the sort thread. All it does
	 * is suspend the <code>SortDaemon</code> thread, to make the offline
	 * sorting loop wait at its beginning for the thread to be resumed when the
	 * user requests the sort to begin.
	 */
	public void atSortStart() {
		sortDaemon.setState(GoodThread.State.SUSPEND);
	}

	/**
	 * Method not implement for SortControl, called by sorting when at end of
	 * writing file. implemented in RunControl
	 */
	public void atWriteEnd() {
		/* does nothing */
	}

	/**
	 * Start sorting offline.
	 */
	public void beginSort() {
		loadNames();
		lockFields(true);
		RunInfo.runNumber = 999;
		RunInfo.runTitle = "Pre-sorted data";
		RunInfo.runStartTime = new java.util.Date();
		if (writeEvents) {
			sortDaemon.setWriteEnabled(true);
			boolean openSuccess = true;
			try {
				outputDaemon.openEventOutputFile(fileOut);
			} catch (SortException e) {
				msgHandler
						.errorOutln("Sort|Control.Begin: couldn't open event output file.");
				sortDaemon.setWriteEnabled(false);
				openSuccess = false;
			}
			if (openSuccess) {
				try {
					outputDaemon.writeHeader();
				} catch (Exception e) {
					msgHandler
							.errorOutln("Sort|Control.Begin: couldn't write header to event output file.");
				}
			}
		} else {
			sortDaemon.setWriteEnabled(false);
		}
		msgHandler.messageOutln("Starting sorting from Disk");
		beginAction.setEnabled(false);
		haltAction.setEnabled(true);
		sortDaemon.setState(GoodThread.State.RUN);
		STATUS.setRunState(RunState.ACQ_ON);
	}

	/**
	 * stop offline sorting
	 * 
	 */
	private void endSort() {
		sortDaemon.cancelOfflineSorting();
		if (!inputDaemon.closeEventInputListFile()) {
			msgHandler.errorOutln("Closing sort input event file: "
					+ inputDaemon.getEventInputFileName());
		}
		if (writeEvents) {
			try {
				outputDaemon.closeEventOutputFile();
			} catch (SortException e) {
				msgHandler
						.errorOutln("Sort|Control...: couldn't close event output file.");
			}
			msgHandler.messageOutln("Closed pre-sorted file: "
					+ fileOut.getPath());
		}
		STATUS.setRunState(RunState.ACQ_OFF);
		msgHandler
				.warningOutln("Ended offline sorting before reading all events.");
		beginAction.setEnabled(false);
	}

	/*
	 * non-javadoc: Is the Browse for the output file.
	 * 
	 * @return oubput file
	 */
	private File getOutFile() {
		File rval = new File(textOutFile.getText().trim()); // default return
		// value
		final JFileChooser fileChooser = new JFileChooser(outDirectory);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileFilter(new ExtensionFileFilter(
				new String[] { "evn" }, "Event Files (*.evn)"));
		final int option = fileChooser.showOpenDialog(frame);
		/* save current values */
		if (option == JFileChooser.APPROVE_OPTION
				&& fileChooser.getSelectedFile() != null) {
			outDirectory = fileChooser.getSelectedFile(); // save current
			// directory
			rval = outDirectory;
		}
		return rval;
	}

	/**
	 * Load the name of objects entered in dialog box give the list to storage
	 * deamon
	 */
	private void loadNames() {
		final List<File> fileList = multiFile.getFileList();
		/* tell storage daemon list of files */
		inputDaemon.setEventInputList(fileList);
		/* save output file */
		fileOut = new File(textOutFile.getText().trim());
		msgHandler.messageOutln("Loaded list of sort files");
	}

	/*
	 * non-javadoc: Lock the file and record input list while sorting This
	 * method is called when sorting is actived to lock fields again when done
	 * to unlock fields
	 */
	private void lockFields(final boolean lock) {
		multiFile.setLocked(lock);
		final boolean notLock = !lock;
		textOutFile.setEditable(notLock);
		cout.setEnabled(notLock);
	}

	/**
	 * Called by <code>SortDaemon</code> when it needs to start the next
	 * stream.
	 * 
	 * @return <code>true</code> if there was a next file and it's open now
	 */
	public boolean openNextFile() {
		boolean sortNext = false;
		if (!inputDaemon.closeEventInputListFile()) {
			msgHandler.errorOutln("Could not close file: "
					+ inputDaemon.getEventInputFileName());
		}
		if (inputDaemon.hasMoreFiles()) {
			if (inputDaemon.openEventInputListFile()) {
				msgHandler.messageOutln("Sorting next file: "
						+ inputDaemon.getEventInputFileName());
				msgHandler.messageOutln("  Run number: " + RunInfo.runNumber
						+ " title: " + RunInfo.runTitle);
			} else {
				msgHandler.errorOutln("Could not open file: "
						+ inputDaemon.getEventInputFileName());
			}
			sortNext = true;// try next file no matter what
		}
		return sortNext;
	}

	/*
	 * non-javadoc: For scripting.
	 */
	int readList(final File file) {
		int numFiles = 0;
		lastFile = file;
		try {
			final BufferedReader reader = new BufferedReader(new FileReader(
					lastFile));
			String listItem;
			do {
				listItem = reader.readLine();
				numFiles += addFile(listItem);
			} while (listItem != null);
			reader.close();
		} catch (IOException ioe) {
			msgHandler.errorOutln("Jam|Sort...: Unable to load list from file "
					+ file);
		}
		return numFiles;
	}

	void setEventOutput(final File file) {
		outDirectory = file;
		textOutFile.setText(file.getAbsolutePath());
		setWriteEvents(true);
	}

	/**
	 * Setup up called by SetupSortOff.
	 * 
	 * @param sortDaemon
	 *            the sorting process
	 * @param fromDaemon
	 *            the process feeding data from storage
	 * @param toDaemon
	 *            the process that accepts data for storage
	 */
	public void setup(final SortDaemon sortDaemon,
			final StorageDaemon fromDaemon, final StorageDaemon toDaemon) {
		this.sortDaemon = sortDaemon;
		this.inputDaemon = fromDaemon;
		this.outputDaemon = toDaemon;
		beginAction.setEnabled(true);
	}

	void setWriteEvents(final boolean state) {
		textOutFile.setEnabled(state);
		bbrowse.setEnabled(state);
		writeEvents = state;
	}
}