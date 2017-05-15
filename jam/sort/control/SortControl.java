package jam.sort.control;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jam.global.*;
import jam.sort.AbstractStorageDaemon;
import jam.sort.OfflineController;
import jam.sort.SortDaemon;
import jam.sort.SortException;
import jam.ui.ExtensionFileFilter;
import jam.ui.Icons;
import jam.ui.MultipleFileChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.util.logging.Logger;

import static jam.global.PropertyKeys.*;
import static java.util.logging.Level.SEVERE;

/**
 * Class to control the offline sort process Allows you to enter the list of
 * files to sort an the output pre-sorted file if you have one
 * 
 * @author Dale Visser and Ken Swartz
 * @version 1.0
 */
@Singleton
public final class SortControl extends javax.swing.JDialog implements
		OfflineController {

	private static final Logger LOGGER = Logger.getLogger("jam");

	private transient final JamStatus STATUS;

	/**
	 * button to get file browser
	 */
	private transient final JButton bbrowse;

	private transient final Action beginAction, haltAction;

	/** check box for writing out events */
	private transient final JCheckBox cout;

	private transient File fileOut, lastFile, outDirectory;

	/* daemon threads */
	private transient AbstractStorageDaemon inputDaemon;

	private transient final MultipleFileChooser multiFile;

	private transient AbstractStorageDaemon outputDaemon;

	private transient SortDaemon sortDaemon;

	/** Text field for output file */
	private transient final JTextField textOutFile;

	private transient boolean writeEvents;

	private transient final JFrame frame;

	@Inject
	public SortControl(final JFrame frame, final JamStatus status,
			final Icons icons) {
		super(frame, "Sorting", false);
		this.frame = frame;
		this.STATUS = status;
		this.beginAction = this.createBeginAction(icons);
		this.haltAction = this.createHaltAction(icons);
		setResizable(true);// sometimes there are long paths to files
		setLocation(20, 50);
		/* GUI layout */
		final java.awt.Container contents = getContentPane();
		contents.setLayout(new BorderLayout(10, 10));
		/* Top Panel - just a label */
		final JPanel ptop = new JPanel(new FlowLayout(FlowLayout.CENTER));
		ptop.setBorder(new EmptyBorder(10, 0, 0, 0));
		contents.add(ptop, BorderLayout.NORTH);
		ptop.add(new javax.swing.JLabel("Event Files to Sort",
				javax.swing.SwingConstants.RIGHT));
		/* List Panel - center - MultipleFileChooser */
		multiFile = new MultipleFileChooser(frame, new File(
				JamProperties.getPropString(EVENT_INPATH)));
		multiFile.showListSaveLoadButtons(true);
		multiFile.setFileFilter(new ExtensionFileFilter("evn", "Event Files"));
		contents.add(multiFile, BorderLayout.CENTER);
		/* Bottom Panel - output file and buttons to begin and halt sorting */
		final JPanel pbottom = new JPanel(new GridLayout(0, 1, 5, 5));
		pbottom.setBorder(new EmptyBorder(0, 5, 0, 10));
		contents.add(pbottom, BorderLayout.SOUTH);
		/* panel for output file */
		final JPanel pout = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pbottom.add(pout);
		cout = new JCheckBox("Output Events to File:", false);
		cout.addItemListener(itemEvent -> setWriteEvents(cout.isSelected()));
		pout.add(cout);

		final String eventDefault = JamProperties.getPropString(EVENT_OUTPATH);
		final String outDefault = JamProperties.getPropString(EVENT_OUTFILE);
		textOutFile = new JTextField(eventDefault + File.separator + outDefault);
		textOutFile.setColumns(28);
		pout.add(textOutFile);

		bbrowse = new JButton("Browse..");
		bbrowse.addActionListener(actionEvent -> textOutFile.setText(getOutFile().getPath()));
		pout.add(bbrowse);

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
		lockFields(false);
		setWriteEvents(cout.isSelected());
	}

	private AbstractAction createBeginAction(final Icons icons) {
		return new AbstractAction() {
			{// NOPMD
				putValue(Action.NAME, "Begin");
				putValue(Action.SHORT_DESCRIPTION, "Begin sort of all files."
						+ " If a sort was halted, we start over.");
				putValue(Action.SMALL_ICON, icons.BEGIN);
				setEnabled(false);
			}

			public void actionPerformed(final ActionEvent actionEvent) {
				beginSort();
			}
		};
	}

	private AbstractAction createHaltAction(final Icons icons) {
		return new AbstractAction() {
			{// NOPMD
				putValue(Action.NAME, "Halt");
				putValue(Action.SHORT_DESCRIPTION, "Halt sort in process.");
				putValue(Action.SMALL_ICON, icons.END);
				setEnabled(false);
			}

			public void actionPerformed(final ActionEvent actionEvent) {
				endSort();
			}
		};
	}

	/**
	 * For scripting.
	 * 
	 * @param file
	 *            to add to files to sort, recurses if folder
	 * @return number of files added
	 */
	public int addEventFile(final File file) {
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

	/**
	 * Called back by sorter when sort encounters a end-run-marker. Tell
	 * StorageDaemon to close file. Tells user sorting is done and unlocks
	 * fields so that new files can be input to sort.
	 * 
	 */
	public void atSortEnd() {
		try {
			LOGGER.info("Sorting all done");
			STATUS.setRunState(RunState.ACQ_OFF);
			if (!inputDaemon.closeEventInputListFile()) {
				LOGGER.severe("Couldn't close file [SortControl]");
			}
			if (writeEvents) {
				outputDaemon.closeEventOutputFile();
				LOGGER.info("Closed pre-sorted file: " + fileOut.getPath());
			}
			beginAction.setEnabled(true);
			lockFields(false);
			/* let other thread (i.e., jam.Script) know we are finished */
			haltAction.setEnabled(false);
		} catch (SortException se) {
			LOGGER.log(SEVERE, "Unable to close event output file.", se);
		}
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
		final RunInfo runInfo = RunInfo.getInstance();
		runInfo.runNumber = 999;
		runInfo.runTitle = "Pre-sorted data";
		runInfo.runStartTime = new java.util.Date();
		if (writeEvents) {
			sortDaemon.setWriteEnabled(true);
			boolean openSuccess = true;
			try {
				outputDaemon.openEventOutputFile(fileOut);
			} catch (SortException e) {
				LOGGER.log(SEVERE,
						"Sort|Control.Begin: couldn't open event output file.",
						e);
				sortDaemon.setWriteEnabled(false);
				openSuccess = false;
			}
			if (openSuccess) {
				try {
					outputDaemon.writeHeader();
				} catch (Exception e) {
					LOGGER.log(
							SEVERE,
							"Sort|Control.Begin: couldn't write header to event output file.",
							e);
				}
			}
		} else {
			sortDaemon.setWriteEnabled(false);
		}
		LOGGER.info("Starting sorting from Disk");
		beginAction.setEnabled(false);
		haltAction.setEnabled(true);
		sortDaemon.setState(GoodThread.State.RUN);
		STATUS.setRunState(RunState.ACQ_ON);
	}

	/**
	 * 
	 * @return the number of events that have been sorted
	 */
	public int getEventsSorted() {
		return this.sortDaemon.getSortedCount();
	}

	public boolean openNextFile() {
		boolean sortNext = false;
		if (!inputDaemon.closeEventInputListFile()) {
			LOGGER.severe("Could not close file: "
					+ inputDaemon.getEventInputFileName());
		}
		if (inputDaemon.hasMoreFiles()) {
			if (inputDaemon.openEventInputListFile()) {
				LOGGER.info("Sorting next file: "
						+ inputDaemon.getEventInputFileName());
				final RunInfo runInfo = RunInfo.getInstance();
				LOGGER.info("  Run number: " + runInfo.runNumber + " title: "
						+ runInfo.runTitle);
			} else {
				LOGGER.severe("Could not open file: "
						+ inputDaemon.getEventInputFileName());
			}
			sortNext = true;// try next file no matter what
		}
		return sortNext;
	}

	/**
	 * Scripting. Reads a list of event files from a text file.
	 * 
	 * @param file
	 *            with list of event files
	 * @return number of files added
	 */
	public int readList(final File file) {
		int numFiles = 0;
		lastFile = file;
		try {
			final BufferedReader reader = new BufferedReader(
					new java.io.FileReader(lastFile));
			String listItem;
			do {
				listItem = reader.readLine();
				numFiles += addFile(listItem);
			} while (listItem != null);
			reader.close();
		} catch (java.io.IOException ioe) {
			LOGGER.log(SEVERE, "Jam|Sort...: Unable to load list from file "
					+ file, ioe);
		}
		return numFiles;
	}

	/**
	 * Remove all files from the event file list.
	 */
	public void removeAllFiles() {
		this.multiFile.removeAllFiles();
	}

	/**
	 * For scripting, sets an event output file for pre-sorting.
	 * 
	 * @param file
	 *            for pre-sorted events to go to
	 */
	public void setEventOutput(final File file) {
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
			final AbstractStorageDaemon fromDaemon,
			final AbstractStorageDaemon toDaemon) {
		this.sortDaemon = sortDaemon;
		this.inputDaemon = fromDaemon;
		this.outputDaemon = toDaemon;
		beginAction.setEnabled(true);
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
		for (File file : files) {
			if (fileFilter.accept(file)) {
				multiFile.addFile(file);
			}
			rval++;
		}
		return rval;
	}

	/**
	 * stop offline sorting
	 * 
	 */
	private void endSort() {
		sortDaemon.cancelOfflineSorting();
		if (!inputDaemon.closeEventInputListFile()) {
			LOGGER.severe("Closing sort input event file: "
					+ inputDaemon.getEventInputFileName());
		}
		if (writeEvents) {
			try {
				outputDaemon.closeEventOutputFile();
			} catch (SortException e) {
				LOGGER.log(SEVERE,
						"Sort|Control...: couldn't close event output file.", e);
			}
			LOGGER.info("Closed pre-sorted file: " + fileOut.getPath());
		}
		STATUS.setRunState(RunState.ACQ_OFF);
		LOGGER.warning("Ended offline sorting before reading all events.");
		beginAction.setEnabled(false);
	}

	/*
	 * non-javadoc: Is the Browse for the output file.
	 * 
	 * @return output file
	 */
	private File getOutFile() {
		File rval = new File(textOutFile.getText().trim()); // default return
		// value
		final JFileChooser fileChooser = new JFileChooser(outDirectory);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileFilter(new ExtensionFileFilter(
				new String[] { "evn" }, "Event Files (*.evn)"));
		final int option = fileChooser.showOpenDialog(this.frame);
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
		final java.util.List<File> fileList = multiFile.getFileList();
		/* tell storage daemon list of files */
		inputDaemon.setEventInputList(fileList);
		/* save output file */
		fileOut = new File(textOutFile.getText().trim());
		LOGGER.info("Loaded list of sort files");
	}

	/*
	 * non-javadoc: Lock the file and record input list while sorting This
	 * method is called when sorting is actived to lock fields again when done
	 * to unlock fields
	 */
	private void lockFields(final boolean lock) {
		final boolean notLock = !lock;
		multiFile.setLocked(lock);

		// textOutFile.setEditable(notLock);
		if (cout.isSelected()) {
			textOutFile.setEnabled(notLock);
			bbrowse.setEnabled(notLock);
		}
		cout.setEnabled(notLock);
	}

	private void setWriteEvents(final boolean state) {
		textOutFile.setEditable(state);
		textOutFile.setEnabled(state);
		bbrowse.setEnabled(state);
		writeEvents = state;
	}

	/**
	 * @return the file chooser being used, for testing purposes
	 */
	public MultipleFileChooser getFileChooser() {
		return this.multiFile;
	}
}
