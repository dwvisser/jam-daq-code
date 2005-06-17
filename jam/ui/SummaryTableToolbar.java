package jam.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

/**
 * Toolbar for summary table
 * 
 * @author Ken Swartz
 *
 */
class SummaryTableToolbar extends JToolBar {

	private JCheckBox chkShowScaler; 
	
	private JCheckBox chkShowHistogram;
	
	private JCheckBox chkShowGate;
	
	private SummaryTableModel summaryTableModel;
	
	SummaryTableToolbar(SummaryTableModel stm) {
		summaryTableModel=stm;		
		final Icon iUpdate = loadToolbarIcon("jam/plot/Update.png");
		final JButton bupdate = iUpdate == null ? 
				new JButton(getHTML("<u>U</u>pdate")) : new JButton(iUpdate);
		this.add(bupdate);				
		bupdate.setToolTipText(
			getHTML("<u>U</u>pdate display with most current data."));
		bupdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				refresh();
			}
		});
		addSeparator();
		chkShowScaler =new JCheckBox("Scalers");
		chkShowScaler.setSelected(true);
		chkShowScaler.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				refresh();
			}
		});
		add(chkShowScaler);		
		chkShowHistogram =new JCheckBox("Histograms");
		chkShowHistogram.setSelected(true);
		chkShowHistogram.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				refresh();
			}
		});
		add(chkShowHistogram);		
		chkShowGate =new JCheckBox("Gates");
		chkShowGate.setSelected(true);
		chkShowGate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				refresh();
			}
		});
		add(chkShowGate);		
	}
	
	/**
	 * Refresh display of table
	 *
	 */
	private void refresh() {
		boolean showScalers = chkShowScaler.isSelected();
		boolean showHistograms = chkShowHistogram.isSelected();
		boolean showGates =chkShowGate.isSelected();
		summaryTableModel.setOptions(showScalers, showHistograms, showGates);
		
	}
	/** 
	 * Load icons for tool bar
	 */
	private Icon loadToolbarIcon(String path) {
		final Icon toolbarIcon;
		final ClassLoader cl = this.getClass().getClassLoader();
		final URL urlResource = cl.getResource(path);
		if (!(urlResource == null)) {
			toolbarIcon = new ImageIcon(urlResource);
		} else { //instead use path, ugly but lets us see button
			JOptionPane.showMessageDialog(
				this,
				"Can't load resource: " + path,
				"Missing Icon",
				JOptionPane.ERROR_MESSAGE);
			toolbarIcon = null; //buttons initialized with text if icon==null
		}
		return toolbarIcon;
	}
	
	private String getHTML(String body) {
		final StringBuffer rval =
			new StringBuffer("<html><body>").append(body).append(
				"</html></body>");
		return rval.toString();
	}

}
