package jam.io.control;

import com.google.inject.Inject;
import jam.data.AbstractHistogram;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;
import jam.ui.WindowCancelAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Save a selection of histograms
 * 
 * @author Ken Swartz
 * 
 */
public final class SaveSelectedHistogram {

	private transient final Frame frame;

	private transient final JDialog dialog;

	private transient final JList<String> listHist;

	private transient final HDFIO hdfio;

	/**
	 * Constructs a dialog to save a selection of histograms out of an HDF file.
	 * 
	 * @param frame
	 *            parent frame
	 * @param hdfio
	 *            for saving histograms
	 */
	@Inject
	public SaveSelectedHistogram(final Frame frame, final HDFIO hdfio) {
		super();
		this.frame = frame;
		this.hdfio = hdfio;
		dialog = new JDialog(frame, "Save Selected Histograms", false);
		dialog.setLocation(frame.getLocation().x + 50,
				frame.getLocation().y + 50);
		final Container container = dialog.getContentPane();
		container.setLayout(new BorderLayout(10, 10));
		/* Selection list */
		final DefaultListModel<String> listModel = new DefaultListModel<>();
		listHist = new JList<>(listModel);
		listHist.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		listHist.setSelectedIndex(0);
		listHist.setVisibleRowCount(10);
		final JScrollPane listPane = new JScrollPane(listHist);
		listPane.setBorder(new EmptyBorder(10, 10, 0, 10));
		container.add(listPane, BorderLayout.CENTER);
		/* Lower panel with buttons */
		final JPanel pLower = new JPanel();
		container.add(pLower, BorderLayout.SOUTH);
		final JPanel pButtons = new JPanel(new GridLayout(1, 0, 5, 5));
		pLower.add(pButtons);
		final AbstractButton bSave = new JButton("Save");
		bSave.addActionListener(actionEvent -> doSave());
		pButtons.add(bSave);
		final AbstractButton bCancel = new JButton(new WindowCancelAction(
				dialog));
		pButtons.add(bCancel);
		dialog.setResizable(false);
		dialog.pack();

	}

	/**
	 * Show the dialog.
	 * 
	 */
	public void show() {
		loadHistogramList();
		dialog.setVisible(true);
	}

	/**
	 * Save histogram list to a file.
	 */
	private void doSave() {
		saveHistListToFile();
		dialog.dispose();
	}

	/**
	 * Cancel Button
	 * 
	 */
	// private void doCancel() {
	// /* clear memory */
	// dialog.dispose();
	// }
	private void loadHistogramList() {
		final List<AbstractHistogram> histList = AbstractHistogram
				.getHistogramList();
		final String[] histNames = new String[histList.size()];
		int index = 0;
		for (AbstractHistogram hist : histList) {
			histNames[index] = hist.getFullName();
			index++;
		}
		listHist.setListData(histNames);
	}

	/**
	 * Save list to file
	 * 
	 */
	private void saveHistListToFile() {
		final List<AbstractHistogram> listSelected = new ArrayList<>();
		/* Add selected histograms to a list. */
		final List<String> selected = listHist.getSelectedValuesList();
		for (String name : selected) {
			listSelected.add(AbstractHistogram.getHistogram(name));
		}
		/* Select file */
		final JFileChooser chooser = new JFileChooser(HDFIO.getLastValidFile());
		chooser.setFileFilter(new HDFileFilter(true));
		final int option = chooser.showSaveDialog(frame);
		/* don't do anything if it was cancel */
		if (option == JFileChooser.APPROVE_OPTION
				&& chooser.getSelectedFile() != null) {
			/* write out histograms */
			hdfio.writeFile(chooser.getSelectedFile(), listSelected);
		}
	}
}
