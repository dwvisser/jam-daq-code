package jam.io.control;

import jam.data.Group;
import jam.data.Histogram;
import jam.data.control.AbstractControl;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFException;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;
import jam.io.hdf.HistogramAttributes;
import jam.ui.PanelOKApplyCancelButtons;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Reads and displays list of the histograms for a user select hdf file. Then
 * loads the user selected histograms into the data base and appends the file
 * indicator to the histogram name
 * 
 * @author Ken
 * 
 */
public final class OpenSelectedHistogram implements HDFIO.AsyncListener {

	private final JDialog dialog;

	private final JTextField txtFile;

	private final JList histList;

	private final DefaultListModel histListData;

	/**
	 * File to read histogram information from
	 */
	private File fileOpen;

	private final List<HistogramAttributes> histAttrList = new ArrayList<HistogramAttributes>();

	/** HDF file reader */
	private final HDFIO hdfio;

	/** Messages output */
	private final MessageHandler msgHandler;

	private final Frame frame;

	private static final Broadcaster BROADCASTER = Broadcaster
			.getSingletonInstance();;

	private static final JamStatus STATUS = JamStatus.getSingletonInstance();;

	/**
	 * Constructs an object which uses a dialog to open a selected histogram out
	 * of an HDF file.
	 * 
	 * @param frame
	 *            parent frame
	 * @param msgHandler
	 *            where to print messages
	 */
	public OpenSelectedHistogram(Frame frame, MessageHandler msgHandler) {
		this.frame = frame;
		this.msgHandler = msgHandler;
		hdfio = new HDFIO(frame, msgHandler);

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
		histListData = new DefaultListModel();
		histList = new JList(histListData);
		histList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		histList.setSelectedIndex(0);
		histList.setVisibleRowCount(10);
		JScrollPane listPane = new JScrollPane(histList);
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
		if (chooseFile()) {
			if (loadHistNames(fileOpen)) {
				txtFile.setText(fileOpen.getAbsolutePath());
				dialog.setVisible(true);
			}
		}
	}

	/*
	 * non-javadoc: Load name of histograms from the selected file.
	 */
	private boolean loadHistNames(File fileSelect) {
		boolean loadState;
		/* Read in histogram names attributes */
		try {
			final List histAttr = hdfio.readHistogramAttributes(fileSelect);
			final Iterator iter = histAttr.iterator();
			while (iter.hasNext()) {
				final HistogramAttributes histAtt = (HistogramAttributes) iter
						.next();
				histListData.addElement(histAtt.getFullName());
			}
			loadState = true;
		} catch (HDFException hdfe) {
			msgHandler.errorOutln(hdfe.toString());
			loadState = false;
		}
		histList.clearSelection();

		return loadState;
	}

	/*
	 * non-javadoc: Load the histograms in the selected list.
	 */
	private void loadHistograms() {
		final Object[] selected = histList.getSelectedValues();
		histAttrList.clear();
		// No histograms selected
		if (selected.length == 0) {
			msgHandler.errorOutln("No histograms selected");
		} else {
			/* Put selected histograms into a list */
			final List<String> selectNames = new ArrayList<String>();
			for (int i = 0; i < selected.length; i++) {
				final String histFullName = (String) selected[i];
				final HistogramAttributes histAttrib = HistogramAttributes
						.getHistogramAttribute(histFullName);
				histAttrList.add(histAttrib);
				final String histName = histAttrib.getName();
				selectNames.add(histName);
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
		boolean openF = false;
		final JFileChooser jfile = new JFileChooser(fileOpen);
		jfile.setFileFilter(new HDFileFilter(true));
		final int option = jfile.showOpenDialog(frame);
		// dont do anything if it was cancel
		if (option == JFileChooser.APPROVE_OPTION
				&& jfile.getSelectedFile() != null) {
			synchronized (this) {
				fileOpen = jfile.getSelectedFile();
			}
			openF = true;
		} else { // dialog didn't return a file
			openF = false;
		}
		return openF;
	}

	private void notifyApp() {
		// Update app status
		AbstractControl.setupAll();
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		// Set the current histogram to the first opened histogram
		final Group firstGroup = hdfio.getFirstLoadGroup();
		if (firstGroup != null) {
			STATUS.setCurrentGroup(firstGroup);
			/* Set the current histogram to the first opened histogram. */
			if (firstGroup.getHistogramList().size() > 0) {
				final Histogram firstHist = firstGroup.getHistogramList().get(0);
				STATUS.setCurrentHistogram(firstHist);
				BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT,
						firstHist);
			}
		}
	}

	/**
	 * Called by HDFIO when asynchronized IO is completed
	 */
	public void completedIO(String message, String errorMessage) {
		hdfio.removeListener();
		notifyApp();
	}
}
