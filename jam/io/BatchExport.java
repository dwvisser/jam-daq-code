package jam.io;

import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.JamStatus;
import jam.util.CollectionsUtil;
import jam.util.FileUtilities;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

/**
 * Dialog for exporting lists of histograms. Searches <code>jam.io</code> for
 * all classes extending <code>jam.io.ImpExp</code>.
 * 
 * @author <a href=mailto:dale@visser.name>Dale Visser </a>
 */
public class BatchExport extends JDialog implements Observer {

	private static final Logger LOGGER = Logger.getLogger(BatchExport.class
			.getPackage().getName());

	private class SelectHistogramDialog {

		private transient final JDialog dialog;

		private transient final JList histList;

		SelectHistogramDialog() {
			super();
			final java.awt.Frame frame = JamStatus.getSingletonInstance()
					.getFrame();
			dialog = new JDialog(frame, "Selected Histograms", false);
			dialog.setLocation(frame.getLocation().x + 50,
					frame.getLocation().y + 50);
			final Container container = dialog.getContentPane();
			container.setLayout(new BorderLayout(10, 10));
			/* Selection list */
			final DefaultListModel histListData = new DefaultListModel();
			histList = new JList(histListData);
			histList
					.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			histList.setSelectedIndex(0);
			histList.setVisibleRowCount(10);
			final JScrollPane listPane = new JScrollPane(histList);
			listPane.setBorder(new EmptyBorder(10, 10, 0, 10));
			container.add(listPane, BorderLayout.CENTER);
			/* Lower panel with buttons */
			final JPanel pLower = new JPanel(new FlowLayout());
			container.add(pLower, BorderLayout.SOUTH);
			final JButton bButton = new JButton("OK");
			pLower.add(bButton);
			bButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent actionEvent) {
					addToSelection();
					dialog.dispose();
				}
			});
			dialog.setResizable(false);
			dialog.pack();
		}

		void addToSelection() {
			final Object[] selected = histList.getSelectedValues();
			final HashSet<Object> histFullSet = new HashSet<Object>();
			/* now combine this with stuff already in list. */
			final ListModel model = lstHists.getModel();
			for (int i = 0; i < model.getSize(); i++) {
				histFullSet.add(model.getElementAt(i));
			}
			for (int i = 0; i < selected.length; i++) {
				if (!histFullSet.contains(selected[i])) {
					histFullSet.add(selected[i]);
				}
			}
			lstHists.setListData(histFullSet.toArray());
		}

		void show() {
			final Set<Histogram> histSet = new HashSet<Histogram>();
			CollectionsUtil.getSingletonInstance().addConditional(
					Histogram.getHistogramList(), histSet, HIST_COND_1D);
			histList.setListData(histSet.toArray());
			dialog.setVisible(true);
		}
	}

	private static final CollectionsUtil.Condition<Histogram> HIST_COND_1D = new CollectionsUtil.Condition<Histogram>() {
		public boolean accept(final Histogram hist) {
			return hist.getDimensionality() == 1;
		}
	};

	private transient final JButton bExport = new JButton("Export");

	private transient final JComboBox cbHist = new JComboBox();

	private transient final ActionListener cbHistListen = new ActionListener() {// NOPMD
		public void actionPerformed(final ActionEvent actionEvent) {
			addSelectedHist();
			setExportEnable();
		}
	};

	private transient final Map<AbstractButton, AbstractImpExp> exportMap = java.util.Collections
			.synchronizedMap(new java.util.HashMap<AbstractButton, AbstractImpExp>());

	private transient File lastListFile = null;

	private transient final JList lstHists = new JList(new DefaultListModel());

	private transient final SelectHistogramDialog selectHistDlg;

	private transient final JTextField txtDirectory = new JTextField(System
			.getProperty("user.home"), 40);

	/**
	 * Constructs a new batch histogram exporter.
	 */
	public BatchExport() {

		super(JamStatus.getSingletonInstance().getFrame(),
				"Batch Histogram Export");
		jam.global.Broadcaster.getSingletonInstance().addObserver(this);
		buildGUI();
		setupHistChooser();
		selectHistDlg = new SelectHistogramDialog();
	}

	/**
	 * Add all 1 d histograms to the list
	 * 
	 */
	private void addAllHists() {
		final Set<Histogram> histSet = new HashSet<Histogram>();
		CollectionsUtil.getSingletonInstance().addConditional(
				Histogram.getHistogramList(), histSet, HIST_COND_1D);
		lstHists.setListData(histSet.toArray());
	}

	/**
	 * Add a selected histogram
	 * 
	 */
	private void addSelectedHist() {
		final String name = cbHist.getSelectedItem().toString();
		final HashSet<Object> histSet = new HashSet<Object>();
		/* now combine this with stuff already in list. */
		final ListModel model = lstHists.getModel();
		for (int i = 0; i < model.getSize(); i++) {
			histSet.add(model.getElementAt(i));
		}
		histSet.add(name);
		lstHists.setListData(histSet.toArray());
	}

	/**
	 * add all files in a directory to sort
	 * 
	 */
	private void browseForDir() {
		final JFileChooser chooser = new JFileChooser(new File(txtDirectory
				.getText().trim()));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		final int option = chooser.showOpenDialog(this);
		/* save current values */
		if (option == JFileChooser.APPROVE_OPTION
				&& chooser.getSelectedFile() != null) {
			txtDirectory.setText(chooser.getSelectedFile().getPath());
		}
	}

	/**
	 * Construct the GUI
	 */
	private void buildGUI() {
		final Container contents = getContentPane();
		contents.setLayout(new BorderLayout());

		/* Button panel on left ("west"). */
		contents.add(getButtonPanel(), BorderLayout.WEST);
		/* List of histograms */

		final JPanel pList = new JPanel(new GridLayout(1, 1));
		pList.setBorder(new EmptyBorder(10, 0, 10, 10));
		pList.setToolTipText("List of histograms to export.");
		lstHists
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		pList.add(new JScrollPane(lstHists));
		contents.add(pList, BorderLayout.CENTER);
		/* Lower button panel */
		final JPanel pBottom = new JPanel(new GridLayout(0, 1, 5, 5));
		contents.add(pBottom, BorderLayout.SOUTH);
		/* Options panel */
		final JPanel pOptions = new JPanel(new FlowLayout(FlowLayout.CENTER,
				10, 0));
		pBottom.add(pOptions);
		final JPanel pBtnOpn = new JPanel(new GridLayout(1, 0, 10, 0));
		pOptions.add(pBtnOpn);
		final javax.swing.ButtonGroup options = new javax.swing.ButtonGroup();
		final List<AbstractImpExp> exportClassList = createExportList();
		for (AbstractImpExp impExp : exportClassList) {
			final AbstractButton exportChoice = getButton(impExp);
			options.add(exportChoice);
			pBtnOpn.add(exportChoice);
			exportMap.put(exportChoice, impExp);
		}
		/* Directory panel */
		final JPanel pDirectory = new JPanel(new FlowLayout(FlowLayout.CENTER,
				10, 0));
		pDirectory.add(new javax.swing.JLabel("Directory"));
		txtDirectory
				.setToolTipText("Directory to write exported histogram files.");
		pDirectory.add(txtDirectory);
		final JButton bBrowse = new JButton("Browse\u2026");
		bBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				browseForDir();
			}
		});
		pDirectory.add(bBrowse);
		pBottom.add(pDirectory);
		/* Button panel */
		final JPanel pButton = new JPanel(new FlowLayout(FlowLayout.CENTER, 10,
				5));
		bExport.setToolTipText("Export selected histograms.");
		bExport.setEnabled(false);
		bExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				export();
			}
		});
		pButton.add(bExport);
		final JButton bCancel = new JButton(new jam.ui.WindowCancelAction(this));
		pButton.add(bCancel);
		pBottom.add(pButton);
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowActivated(java.awt.event.WindowEvent windowEvent) {
				setupHistChooser();
			}
		});
		pack();
		setResizable(false);
	}

	private List<AbstractImpExp> createExportList() {
		final List<AbstractImpExp> rval = new ArrayList<AbstractImpExp>();
		final String here = getClass().getName() + ".getClasses(): ";
		final Set<Class<?>> set = jam.global.RuntimeSubclassIdentifier.getSingletonInstance().find(
				"jam.io", AbstractImpExp.class, false);
		set.remove(AbstractImpExp.class);
		for (Class<?> temp : set) {
			try {
				final AbstractImpExp impExp = (AbstractImpExp) temp
						.newInstance();
				if (impExp.batchExportAllowed()) {
					rval.add(impExp);
					impExp.setSilent();
				}
			} catch (InstantiationException e) {
				LOGGER.log(Level.SEVERE, here + e.getMessage(), e);
			} catch (IllegalAccessException e) {
				LOGGER.log(Level.SEVERE, here + e.getMessage(), e);
			}

		}
		return rval;
	}

	private boolean createExportDir(final String exportDir) {
		boolean status = true;
		final File exportDirFile = new File(exportDir);
		// Create export dir if it does not exits
		if (!exportDirFile.exists()) {// NOPMD
			// Cannot create export dir
			if (!exportDirFile.mkdirs()) {
				LOGGER.severe("Could not create the directory "
						+ exportDirFile.getPath() + " .");
				status = false;
			}
			// Export dir exists, check it is a directory.
		} else if (!exportDirFile.isDirectory()) {
			LOGGER.severe("The path " + exportDirFile.getPath()
					+ " is not a directory.");
			status = false;
		}
		return status;
	}

	private File createExportFile(final String dir, final String groupName,
			final String histName, final String extension) {
		final FileUtilities fileUtil = FileUtilities.getInstance();
		final String fullFileName = fileUtil.changeExtension(histName.trim(),
				extension, FileUtilities.APPEND_ONLY);
		return new File(dir + File.separator + groupName, fullFileName);
	}

	/**
	 * Export the histograms
	 */
	private void export() {
		boolean status;
		boolean already = false;
		final List<String> exportDirList = new ArrayList<String>();
		/* select the format */
		final AbstractImpExp exportFormat = selectedExportFormat();
		// Check or create export dir
		final String exportDir = txtDirectory.getText().trim();
		// final File exportDirFile = new File(exportDir);
		// Create list of export histograms and files
		final ListModel model = lstHists.getModel();
		Histogram[] exportHistograms = new Histogram[model.getSize()];
		File[] exportFiles = new File[model.getSize()];
		// Create array of histograms and files
		for (int i = 0; i < exportHistograms.length; i++) {
			final jam.data.AbstractHist1D hist1D = (jam.data.AbstractHist1D) model
					.getElementAt(i);
			exportHistograms[i] = hist1D;
			final String groupName = hist1D.getGroup().getName();
			final String histName = hist1D.getName().trim();
			exportFiles[i] = createExportFile(exportDir, groupName, histName,
					exportFormat.getDefaultExtension());
			final String exportGroupDir = exportDir + File.separator
					+ groupName;
			if (!exportDirList.contains(exportGroupDir)) {
				exportDirList.add(exportGroupDir);
			}
			already |= exportFiles[i].exists();
		}
		status = true;
		// Root directory
		status = createExportDir(exportDir);
		status = checkFileOverwrite(status, already);
		// create group directories
		if (status) {
			for (int i = 0; i < exportDirList.size(); i++) {
				final boolean statusTemp = createExportDir(exportDirList.get(i));
				status = status && statusTemp;
			}
		}
		// write out histograms
		if (status) {
			LOGGER.info("Exporting to " + exportDir + ": ");
			for (int i = 0; i < exportFiles.length; i++) {
				LOGGER.info("\t" + exportFiles[i].getName());
				try {
					exportFormat.saveFile(exportFiles[i], exportHistograms[i]);
				} catch (ImpExpException e) {
					LOGGER.log(Level.SEVERE, "Exporting file: "
							+ exportFiles[i].getPath() + " " + e.getMessage(),
							e);
				}
			}
			LOGGER.info("Exporting complete.");
		}
	}

	/**
	 * @param status
	 * @param already
	 * @return
	 */
	private boolean checkFileOverwrite(final boolean status,
			final boolean already) {
		boolean rval = status;
		// Check for overwrite
		if (status && already) {
			final int optionPaneRely = JOptionPane.showConfirmDialog(JamStatus
					.getSingletonInstance().getFrame(),
					"Overwrite existing files? \n", "File Exists",
					JOptionPane.YES_NO_OPTION);
			if (optionPaneRely == JOptionPane.NO_OPTION) {
				rval = false;
			}
		}
		return rval;
	}

	private AbstractButton getButton(final AbstractImpExp impExp) {
		final String desc = impExp.getFormatDescription();
		final AbstractButton rval = new javax.swing.JRadioButton(desc);
		rval.setToolTipText("Select to export in " + desc + " format.");
		rval.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				setExportEnable();
			}
		});

		return rval;
	}

	private Component getButtonPanel() {
		final JPanel pButtons = new JPanel(new GridLayout(0, 1, 5, 2));
		pButtons.setBorder(new EmptyBorder(10, 10, 10, 10));

		final JButton bAddHist = new JButton("Add");
		bAddHist.setToolTipText("Adds selected 1 dimension histograms.");
		bAddHist.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				selectHistDlg.show();
			}
		});
		pButtons.add(bAddHist);

		final JButton bAddAllHist = new JButton("Add All");
		bAddAllHist.setToolTipText("Adds all 1 dimension histograms.");
		bAddAllHist.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				addAllHists();
			}
		});
		pButtons.add(bAddAllHist);

		final JButton bRemoveName = new JButton("Remove");
		bRemoveName.setToolTipText("Removes selected histograms");
		bRemoveName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				removeSelectedHist();
			}
		});
		pButtons.add(bRemoveName);

		final JButton bRemoveAll = new JButton("Remove All");
		bRemoveAll.setToolTipText("Remove all histograms.");
		bRemoveAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				removeAllHists();
			}
		});
		pButtons.add(bRemoveAll);

		final JButton bLoadList = new JButton("Load List");
		bLoadList.setToolTipText("Load list of histograms from file.");
		bLoadList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				loadList();
			}
		});
		pButtons.add(bLoadList);

		final JButton bSaveList = new JButton("Save List");
		bSaveList.setToolTipText("Save list of histograms to file.");
		bSaveList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				saveList();
			}
		});
		pButtons.add(bSaveList);

		return pButtons;
	}

	/**
	 * Load a list of histograms to export from a file.
	 * 
	 */
	private void loadList() {
		Histogram listItem;
		final JFileChooser chooser = new JFileChooser(lastListFile);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(new ExtensionFileFilter(new String[] { "lst" },
				"List Files (*.lst)"));
		final int option = chooser.showOpenDialog(this);
		// save current values
		if (option == JFileChooser.APPROVE_OPTION
				&& chooser.getSelectedFile() != null) {
			lastListFile = chooser.getSelectedFile(); // save current
			// directory
			final List<Histogram> list = new ArrayList<Histogram>();
			try {
				final java.io.BufferedReader reader = new java.io.BufferedReader(
						new java.io.FileReader(lastListFile));
				do {
					listItem = Histogram.getHistogram(reader.readLine());
					if (listItem != null) {
						list.add(listItem);
					}
				} while (listItem != null);
				reader.close();
			} catch (IOException ioe) {
				LOGGER.log(Level.SEVERE, ioe.getMessage(), ioe);
			}
			lstHists.setListData(list.toArray());
		}
	}

	/**
	 * remove all items from sort list
	 * 
	 */
	private void removeAllHists() {
		lstHists.setListData(new java.util.Vector());// NOPMD
	}

	/**
	 * remove a histogram from the list
	 */
	private void removeSelectedHist() {
		final Object[] removeList = lstHists.getSelectedValues();
		final ListModel model = lstHists.getModel();
		final List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < model.getSize(); i++) {
			list.add(model.getElementAt(i));
		}
		list.removeAll(java.util.Arrays.asList(removeList));
		lstHists.setListData(list.toArray());
	}

	/**
	 * Save list of items to export.
	 */
	private void saveList() {
		final JFileChooser chooser = new JFileChooser(lastListFile);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(new ExtensionFileFilter(new String[] { "lst" },
				"List Files (*.lst)"));
		final int option = chooser.showSaveDialog(this);
		/* save current values */
		if (option == JFileChooser.APPROVE_OPTION
				&& chooser.getSelectedFile() != null) {
			lastListFile = chooser.getSelectedFile(); // save current
			// directory

			try {
				final ListModel model = lstHists.getModel();
				final java.io.FileWriter saveStream = new java.io.FileWriter(
						chooser.getSelectedFile());
				for (int i = 0; i < model.getSize(); i++) {
					saveStream.write(model.getElementAt(i) + "\n");
				}
				saveStream.close();
			} catch (IOException ioe) {
				LOGGER.log(Level.SEVERE, ioe.getMessage(), ioe);
			}
		}
	}

	private AbstractImpExp selectedExportFormat() {
		AbstractImpExp out = null;
		for (AbstractButton button : exportMap.keySet()) {
			if (button.isSelected()) {
				out = exportMap.get(button);
			}
		}
		return out;
	}

	/**
	 * Enable export button
	 * 
	 */
	private void setExportEnable() {
		boolean selected = false;
		for (AbstractButton button : exportMap.keySet()) {
			selected |= button.isSelected();
		}
		selected &= lstHists.getModel().getSize() > 0;
		bExport.setEnabled(selected);
	}

	/**
	 * Setup histogram chooser.
	 */
	private void setupHistChooser() {
		cbHist.removeActionListener(cbHistListen);
		cbHist.removeAllItems();
		for (Histogram hist : Histogram.getHistogramList()) {
			if (hist.getDimensionality() == 1) {
				cbHist.addItem(hist.getFullName());
			}
		}
		cbHist.addActionListener(cbHistListen);
	}

	/**
	 * Implementation of Observable interface listeners for broadcast events.
	 * broadcast events where there are new histograms or histograms added.
	 */
	public void update(final Observable observable, final Object object) {
		final BroadcastEvent event = (BroadcastEvent) object;
		final BroadcastEvent.Command command = event.getCommand();
		if (command == BroadcastEvent.Command.HISTOGRAM_NEW
				|| command == BroadcastEvent.Command.HISTOGRAM_ADD) {
			setupHistChooser();
		}
	}
}
