package jam.io;

import jam.data.AbstractHistogram;
import jam.util.CollectionsUtil;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import com.google.inject.Inject;

class SelectHistogramDialog {

	private transient final JDialog dialog;

	private transient final JList<AbstractHistogram> histList;

	private transient JList<AbstractHistogram> lstHists;

	@Inject
	SelectHistogramDialog(final JFrame frame) {
		super();
		dialog = new JDialog(frame, "Selected Histograms", false);
		dialog.setLocation(frame.getLocation().x + 50,
				frame.getLocation().y + 50);
		final Container container = dialog.getContentPane();
		container.setLayout(new BorderLayout(10, 10));
		/* Selection list */
		final DefaultListModel<AbstractHistogram> histListData = new DefaultListModel<>();
		histList = new JList<>(histListData);
		histList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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
		bButton.addActionListener(actionEvent -> {
            addToSelection();
            dialog.dispose();
        });
		dialog.setResizable(false);
		dialog.pack();
	}

	protected void setExternalList(final JList<AbstractHistogram> list) {
		this.lstHists = list;
	}

	private void addToSelection() {
		final List<AbstractHistogram> selected = histList
				.getSelectedValuesList();
		final HashSet<Object> histFullSet = new HashSet<>();
		/* now combine this with stuff already in list. */
		final ListModel<AbstractHistogram> model = lstHists.getModel();
		for (int i = 0; i < model.getSize(); i++) {
			histFullSet.add(model.getElementAt(i));
		}
		for (AbstractHistogram histogram : selected) {
			if (!histFullSet.contains(histogram)) {
				histFullSet.add(histogram);
			}
		}
		lstHists.setListData(histFullSet
				.toArray(new AbstractHistogram[histFullSet.size()]));
	}

	protected void show() {
		final Set<AbstractHistogram> histSet = new HashSet<>();
		CollectionsUtil.getSingletonInstance().addConditional(
				AbstractHistogram.getHistogramList(), histSet,
				BatchExport.HIST_COND_1D);
		histList.setListData(histSet.toArray(new AbstractHistogram[histSet
				.size()]));
		dialog.setVisible(true);
	}
}
