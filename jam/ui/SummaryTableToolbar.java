package jam.ui;

import javax.swing.*;
import java.net.URL;

/**
 * Toolbar for summary table
 * 
 * @author Ken Swartz
 * 
 */
class SummaryTableToolbar extends JToolBar {

	private transient final JCheckBox chkShowScaler = new JCheckBox("Scalers",
			true);

	private transient final JCheckBox chkShowHistogram = new JCheckBox(
			"Histograms", true);

	private transient final JCheckBox chkShowGate = new JCheckBox("Gates", true);

	private transient final SummaryTableModel summaryTableModel;

	SummaryTableToolbar(final SummaryTableModel stm) {
		super();
		summaryTableModel = stm;
		final Icon iUpdate = loadToolbarIcon("jam/plot/Update.png");
		final JButton bupdate = (iUpdate == null) ? new JButton(
				getHTML("<u>U</u>pdate")) : new JButton(iUpdate);
		add(bupdate);
		bupdate
				.setToolTipText(getHTML("<u>U</u>pdate display with most current data."));
		bupdate.addActionListener(actionEvent -> refresh());
		addSeparator();
		chkShowScaler.addActionListener(actionEvent -> refresh());
		add(chkShowScaler);
		chkShowHistogram.addActionListener(actionEvent -> refresh());
		add(chkShowHistogram);
		chkShowGate.addActionListener(actionEvent -> refresh());
		add(chkShowGate);
	}

	/**
	 * Refresh display of table
	 * 
	 */
	private void refresh() {
		final boolean showScalers = chkShowScaler.isSelected();
		final boolean showHistograms = chkShowHistogram.isSelected();
		final boolean showGates = chkShowGate.isSelected();
		summaryTableModel.setOptions(showScalers, showHistograms, showGates);

	}

	/*
	 * non-javadoc: Load icons for tool bar.
	 */
	private Icon loadToolbarIcon(final String path) {
		Icon toolbarIcon = null;// no icon if URL doesn't exist
		final ClassLoader loader = this.getClass().getClassLoader();
		final URL urlResource = loader.getResource(path);
		if (urlResource == null) {
			JOptionPane.showMessageDialog(this, "Can't load resource: " + path,
					"Missing Icon", JOptionPane.ERROR_MESSAGE);
		} else {
			toolbarIcon = new ImageIcon(urlResource);
		}
		return toolbarIcon;
	}

	private String getHTML(final String body) {
		final StringBuilder rval = new StringBuilder("<html><body>").append(body)
				.append("</html></body>");
		return rval.toString();
	}

}
