package jam.io.control;

import jam.data.Histogram;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;
import jam.ui.WindowCancelAction;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

/**
 * Save a selection of histograms
 * 
 * @author Ken Swartz
 * 
 */
public final class SaveSelectedHistogram {

	private transient final Frame frame;

	private transient final JDialog dialog;

	private transient final JList listHist;

	/**
	 * Constructs a dialog to save a selection of histograms out of an HDF file.
	 * 
	 * @param frame
	 *            parent frame
	 */
	public SaveSelectedHistogram(final Frame frame) {
		super();
		this.frame = frame;
		dialog = new JDialog(frame, "Save Selected Histograms", false);
		dialog.setLocation(frame.getLocation().x + 50,
				frame.getLocation().y + 50);
		final Container container = dialog.getContentPane();
		container.setLayout(new BorderLayout(10, 10));
		/* Selection list */
		final DefaultListModel listModel = new DefaultListModel();
		listHist = new JList(listModel);
		listHist
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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
		bSave.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				doSave();
			}
		});
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
		final List<Histogram> histList = Histogram.getHistogramList();
		final String[] histNames = new String[histList.size()];
		int index = 0;
		for (Histogram hist : histList) {
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
		final HDFIO hdfio = new HDFIO(frame);
		final List<Histogram> listSelected = new ArrayList<Histogram>();
		File file = null;
		/* Add selected histgrams to a list. */
		final Object[] selected = listHist.getSelectedValues();
		for (int i = 0; i < selected.length; i++) {
			listSelected.add(Histogram.getHistogram((String) selected[i]));
		}
		/* Select file */
		final JFileChooser chooser = new JFileChooser(HDFIO.getLastValidFile());
		chooser.setFileFilter(new HDFileFilter(true));
		final int option = chooser.showSaveDialog(frame);
		/* don't do anything if it was cancel */
		if (option == JFileChooser.APPROVE_OPTION
				&& chooser.getSelectedFile() != null) {
			file = chooser.getSelectedFile();
			/* write out histograms */
			hdfio.writeFile(file, listSelected);
		}
	}
}
