package jam.io.control;

import com.google.inject.Inject;
import jam.data.AbstractHistogram;
import jam.data.Group;
import jam.data.control.AbstractControl;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFException;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;
import jam.io.hdf.HistogramAttributes;
import jam.ui.PanelOKApplyCancelButtons;
import jam.ui.SelectionTree;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads and displays list of the histograms for a user select hdf file. Then
 * loads the user selected histograms into the data base and appends the file
 * indicator to the histogram name
 * 
 * @author Ken
 * 
 */
public final class OpenSelectedHistogram implements HDFIO.AsyncListener {

	private static final Logger LOGGER = Logger
			.getLogger(OpenSelectedHistogram.class.getPackage().getName());

	private transient final JDialog dialog;

	private transient final JTextField txtFile;

	private transient final JList<String> histList;

	private transient final DefaultListModel<String> histListData;

	/**
	 * File to read histogram information from
	 */
	private transient File fileOpen;

	/** HDF file reader */
	private transient final HDFIO hdfio;

	private transient final Frame frame;

	private transient final JamStatus status;

	private transient final Broadcaster broadcaster;

	/**
	 * Constructs an object which uses a dialog to open a selected histogram out
	 * of an HDF file.
	 * 
	 * @param frame
	 *            parent frame
	 * @param hdfio
	 *            for getting HDF input
	 * @param status
	 *            application status
	 * @param broadcaster
	 *            broadcasts state changes
	 */
	@Inject
	public OpenSelectedHistogram(final Frame frame, final HDFIO hdfio,
			final JamStatus status, final Broadcaster broadcaster) {
		this.frame = frame;
		this.hdfio = hdfio;
		this.status = status;
		this.broadcaster = broadcaster;
		dialog = new JDialog(frame, "Open Selected Histograms", false);
		dialog.setLocation(frame.getLocation().x + 50,
				frame.getLocation().y + 50);
		final Container container = dialog.getContentPane();
		container.setLayout(new BorderLayout(10, 10));
		final JPanel pFileInd = new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JLabel filelabel = new JLabel("File: ", SwingConstants.RIGHT);
		pFileInd.add(filelabel);
		txtFile = new JTextField(20);
		txtFile.setEditable(false);
		pFileInd.add(txtFile);
		container.add(pFileInd, BorderLayout.NORTH);
		/* Selection list */
		histListData = new DefaultListModel<>();
		histList = new JList<>(histListData);
		histList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		histList.setSelectedIndex(0);
		histList.setVisibleRowCount(10);
		final JScrollPane listPane = new JScrollPane(histList);
		listPane.setBorder(new EmptyBorder(0, 10, 0, 10));
		container.add(listPane, BorderLayout.CENTER);
		/* Lower panel with buttons */
		final JPanel pLower = new JPanel();
		container.add(pLower, BorderLayout.SOUTH);
		final PanelOKApplyCancelButtons.Listener listener = new PanelOKApplyCancelButtons.Listener() {
			public void doOK() {
				apply();
				cancel();
			}

			/**
			 * Apply Button
			 * 
			 */
			public void apply() {
				loadHistograms();
			}

			/**
			 * Cancel Button
			 * 
			 */
			public void cancel() {
				/* Clear memory */
				histListData.clear();
				dialog.dispose();
			}
		};
		final PanelOKApplyCancelButtons pButtons = new PanelOKApplyCancelButtons(
				listener);
		pLower.add(pButtons.getComponent());
		dialog.setResizable(false);
		dialog.pack();
	}

	/**
	 * Entry points show the dialog if a file is chosen to open
	 * 
	 */
	public void open() {
		if (chooseFile() && loadHistNames(fileOpen)) {
			txtFile.setText(fileOpen.getAbsolutePath());
			dialog.setVisible(true);
		}
	}

	/*
	 * non-javadoc: Load name of histograms from the selected file.
	 */
	private boolean loadHistNames(final File fileSelect) {
		boolean loadState;
		/* Read in histogram names attributes */
		try {
			final List<HistogramAttributes> attrList = hdfio
					.readHistogramAttributes(fileSelect);
			for (HistogramAttributes histAtt : attrList) {
				histListData.addElement(histAtt.getFullName());
			}
			loadState = true;
		} catch (HDFException hdfe) {
			LOGGER.log(Level.SEVERE, hdfe.getMessage(), hdfe);
			loadState = false;
		}
		histList.clearSelection();

		return loadState;
	}

	/*
	 * non-javadoc: Load the histograms in the selected list.
	 */
	private void loadHistograms() {
		final List<String> selected = histList.getSelectedValuesList();
		final List<HistogramAttributes> histAttrList = new ArrayList<>();
		// No histograms selected
		if (selected.isEmpty()) {
			LOGGER.severe("No histograms selected");
		} else {
			/* Put selected histograms into a list */
			for (String histFullName : selected) {
				histAttrList.add(HistogramAttributes
						.getHistogramAttribute(histFullName));
			}
			/* Read in histograms */
			hdfio.setListener(this);
			hdfio.readFile(FileOpenMode.OPEN_MORE, fileOpen, histAttrList);
		}
	}

	/**
	 * Read in an unspecified file by opening up a dialog box.
	 * 
	 * @return <code>true</code> if successful
	 */
	private boolean chooseFile() {
		final JFileChooser jfile = new JFileChooser(fileOpen);
		jfile.setFileFilter(new HDFileFilter(true));
		final int option = jfile.showOpenDialog(frame);
		// Don't do anything if it was cancel
		final boolean openF = (option == JFileChooser.APPROVE_OPTION && jfile
				.getSelectedFile() != null);
		if (openF) {
			synchronized (this) {
				fileOpen = jfile.getSelectedFile();
			}
		}
		return openF;
	}

	private void notifyApp() {
		// Update app status
		AbstractControl.setupAll();
		broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		// Set the current histogram to the first opened histogram
		final Group firstGroup = hdfio.getFirstLoadGroup();
		if (firstGroup != null) {
			this.status.setCurrentGroup(firstGroup);
			/* Set the current histogram to the first opened histogram. */
			if (firstGroup.histograms.getList().size() > 0) {
				final AbstractHistogram firstHist = firstGroup.histograms
						.getList().get(0);
				SelectionTree.setCurrentHistogram(firstHist);
				broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT,
						firstHist);
			}
		}
	}

	/**
	 * Called by HDFIO when asynchronized IO is completed
	 */
	public void completedIO(final String message, final String errorMessage) {
		hdfio.removeListener();
		notifyApp();
	}
}
