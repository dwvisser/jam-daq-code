package jam.io.control;

import com.google.inject.Inject;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HistogramAttributes;
import jam.ui.MultipleFileChooser;
import jam.ui.SelectionTree;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class to open multiple files at the same time.
 * 
 * @author Ken Swartz
 * 
 */
public final class OpenMultipleFiles implements HDFIO.AsyncListener {

	private static final Logger LOGGER = Logger
			.getLogger(OpenMultipleFiles.class.getPackage().getName());

	// UI components

	private transient final JDialog dialog;

	private transient JList<String> histList;

	private transient DefaultListModel<String> hListModel;

	private transient JTextField txtListFile;

	private transient final JCheckBox chkBoxAdd;

	private transient final MultipleFileChooser multiChooser;

	/** HDF file reader */
	private transient final HDFIO hdfio;

	/** Broadcaster */
	private transient final Broadcaster broadcaster;

	private transient final JamStatus status;

	/**
	 * Constructs an object which uses a dialog to open a selected histogram out
	 * of an HDF file.
	 * 
	 * @param parent
	 *            parent frame
	 * @param status
	 *            application status
	 * @param hdfio
	 *            for opening HDF files
	 * @param broadcaster
	 *            broadcasts state changes
	 */
	@Inject
	public OpenMultipleFiles(final java.awt.Frame parent,
			final JamStatus status, final HDFIO hdfio,
			final Broadcaster broadcaster) {
		this.broadcaster = broadcaster;
		this.hdfio = hdfio;
		this.status = status;
		dialog = new JDialog(parent, "Open Multiple Files");
		dialog.setLocation(parent.getLocation().x + 50,
				parent.getLocation().y + 50);
		final java.awt.Container container = dialog.getContentPane();
		container.setLayout(new BorderLayout(10, 10));
		final JTabbedPane tabPane = new JTabbedPane();
		container.add(tabPane, BorderLayout.CENTER);
		multiChooser = new MultipleFileChooser(parent);
		multiChooser.setFileFilter(new jam.io.hdf.HDFileFilter(true));
		tabPane.addTab("Files", null, multiChooser, "Select Files to open");
		final JPanel histPanel = createHistSelectPanel();
		tabPane.addTab("Histograms", null, histPanel,
				"Select Histograms to open");
        // This method is called whenever the selected tab changes
        tabPane.addChangeListener(evt -> {
            final JTabbedPane pane = (JTabbedPane) evt.getSource();
            changeSelectedTab(pane.getSelectedIndex());
        });
		/* Lower panel with buttons */
		final JPanel pLower = new JPanel(new BorderLayout(5, 5));
		container.add(pLower, BorderLayout.SOUTH);
		final JPanel pLoad = new JPanel(
				new FlowLayout(FlowLayout.CENTER, 30, 0));
		pLower.add(pLoad, BorderLayout.NORTH);
		final JPanel pLoadButtons = new JPanel(new GridLayout(1, 0, 10, 0));
		pLoad.add(pLoadButtons, BorderLayout.NORTH);
		final JButton bLoadlist = new JButton("Load List");
		pLoadButtons.add(bLoadlist);
		bLoadlist.addActionListener(actionEvent -> multiChooser.loadList());
		final JButton bSavelist = new JButton("Save List");
		pLoadButtons.add(bSavelist);
		chkBoxAdd = new JCheckBox("Sum Histograms");
		pLoad.add(chkBoxAdd);

		bSavelist.addActionListener(actionEvent -> multiChooser.saveList());
		final jam.ui.PanelOKApplyCancelButtons okApply = new jam.ui.PanelOKApplyCancelButtons(
				new jam.ui.PanelOKApplyCancelButtons.AbstractListener(dialog) {
					public void apply() {
						defaultSelection();
						loadFiles();
						broadcaster
								.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
					}
				});
		pLower.add(okApply.getComponent(), BorderLayout.SOUTH);
		dialog.setResizable(false);
		dialog.pack();
	}

	/**
	 * Create the histogram selection Panel
	 * 
	 * @return the histogram selection panel
	 */
	private JPanel createHistSelectPanel() {
		final JPanel panel = new JPanel(new BorderLayout());
		final JPanel pOption = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		final JLabel lFile = new JLabel("Histograms list File:");
		pOption.add(lFile);
		txtListFile = new JTextField();
		txtListFile.setColumns(20);
		txtListFile.setEditable(false);
		pOption.add(txtListFile);
		panel.add(pOption, BorderLayout.NORTH);
		hListModel = new DefaultListModel<>();
		histList = new JList<>(hListModel);
		histList.setSelectedIndex(0);
		histList.setVisibleRowCount(10);
		final JScrollPane listPane = new JScrollPane(histList);
		listPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		panel.add(listPane, BorderLayout.CENTER);
		histList.clearSelection();
		return panel;
	}

	/**
	 * Show the dialog box.
	 */
	public void show() {
		dialog.setVisible(true);
	}

	/**
	 * Change of selected tab
	 * 
	 * @param tabIndex index of tab to select
	 */
	public void changeSelectedTab(final int tabIndex) {
		if (tabIndex == 1) {
			refreshHistList();
		}
	}

	/**
	 * Refresh the histogram list
	 * 
	 */
	private void refreshHistList() {
		final File file = multiChooser.getSelectedFile();
		hListModel.clear();
		txtListFile.setText("");
		if (file != null) {
			txtListFile.setText(file.getAbsolutePath());
			if (loadHistNames(file)) {
				checkSelectionIsNone();
			}
		}
	}

	private void checkSelectionIsNone() {
		final ListModel<String> listModel = histList.getModel();
		int[] selected = histList.getSelectedIndices();
		if (selected.length == 0) {
			int[] indexes = new int[listModel.getSize()];
			for (int i = 0; i < listModel.getSize(); i++) {
				indexes[i] = i;
			}
			histList.setSelectedIndices(indexes);
		}
	}

	/**
	 * Check there are histogram in the histogram list.
	 */
	private void checkHistogramsLoaded() {
		final ListModel<String> listModel = histList.getModel();
		final int size = listModel.getSize();
		if (size == 0) {
			refreshHistList();
		}
	}

	/*
	 * non-javadoc: Load name of histograms from the selected file
	 */
	private boolean loadHistNames(final File fileSelect) {
		boolean loadState;
		/* Read in histogram names attributes */
		try {

			/* Read in histogram names attributes */
			final List<HistogramAttributes> attrList = hdfio
					.readHistogramAttributes(fileSelect);
			for (HistogramAttributes histAtt : attrList) {
				hListModel.addElement(histAtt.getFullName());
			}
			loadState = true;
		} catch (jam.io.hdf.HDFException hdfe) {
			LOGGER.severe(hdfe.getMessage());
			loadState = false;
		}
		histList.clearSelection();

		return loadState;

	}

	/**
	 * Load the histograms in the selected list from the selected files.
	 */
	private void loadFiles() {
		checkHistogramsLoaded();
		final List<HistogramAttributes> selectAttrib = createSelectedHistogramNamesList();
		if (selectAttrib.isEmpty()) {// No histograms selected
			LOGGER.severe("No histograms selected");
			return;
		}
		final List<File> files = multiChooser.getFileList();
		jam.data.DataBase.getInstance().clearAllLists();
		broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_NEW);

		hdfio.setListener(this);
		/* Sum counts */
		if (chkBoxAdd.isSelected()) {
			hdfio.readFile(jam.io.FileOpenMode.ADD_OPEN_ONE, files, null,
					selectAttrib);
			status.setSortMode(jam.global.SortMode.FILE, "Multiple Sum");
		} else {
			hdfio.readFile(jam.io.FileOpenMode.OPEN_MORE, files, null,
					selectAttrib);
			status.setSortMode(jam.global.SortMode.FILE, "Multiple");
		}
	}

	private List<HistogramAttributes> createSelectedHistogramNamesList() {
		checkSelectionIsNone();
		final List<String> selected = histList.getSelectedValuesList();
		final List<HistogramAttributes> histAttrList = new ArrayList<>();
		/* Put selected histograms into a list */
		for (String histFullName : selected) {
			// Get name from full name
			final HistogramAttributes histAttrib = HistogramAttributes
					.getHistogramAttribute(histFullName);
			histAttrList.add(histAttrib);
		}
		return histAttrList;
	}

	private void defaultSelection() {
		final ListModel<String> listModel = histList.getModel();
		/* Non-selected select all */
		final int[] selectIndex = histList.getSelectedIndices();
		if (selectIndex.length == 0) {
			int[] indexs = new int[listModel.getSize()];
			for (int i = 0; i < listModel.getSize(); i++) {
				indexs[i] = i;
			}
			histList.setSelectedIndices(indexs);
		}
	}

	private void notifyApp() {
		/* Update app status. */
		jam.data.control.AbstractControl.setupAll();
		broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		/* Set the current histogram to the first opened histogram. */
		final List<jam.data.Group> groupList = jam.data.Warehouse
				.getGroupCollection().getList();
		if (!groupList.isEmpty()) {
			final jam.data.Group firstGroup = groupList.get(0);
			status.setCurrentGroup(firstGroup);
			final List<jam.data.AbstractHistogram> list = firstGroup.histograms
					.getList();
			if (!list.isEmpty()) {
				final jam.data.AbstractHistogram firstHist = list.get(0);
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
