package jam.io.control;

import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.MessageHandler;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;
import jam.ui.MultipleFileChooser;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

/**
 * Class to open multiple files at the same time.
 * 
 * @author Ken Swartz
 *
 */
public class OpenMultipleFiles {

	//UI components
	private Frame frame;
	
	private JDialog dialog;

	private JList histList;

	private DefaultListModel histListModel;
	
	private JTextField txtHistListFile;
	
	private JButton bOK;

	private JButton bApply;

	private JButton bCancel;

	private final String OK = "OK";

	private final String CANCEL = "Cancel";

	private final String APPLY = "Apply";
		
	private MultipleFileChooser multipleFileChooser;
	
	/** HDF file reader */
	private HDFIO hdfio;
	/** Messages output */
	private MessageHandler msgHandler;
	/** Broadcaster */
	private Broadcaster broadcaster;

	/**
	 * Constructs an object which uses a dialog to open a selected histogram out of an
	 * HDF file.
	 *  
	 * @param f parent frame
	 * @param c where to print messages
	 */
	public OpenMultipleFiles(Frame f, MessageHandler m) {		
		frame = f;
		msgHandler = m;
		
		broadcaster= Broadcaster.getSingletonInstance();
		hdfio = new HDFIO(frame, msgHandler);
				
		dialog = new JDialog(frame, "Open Multiple Files");
		dialog.setLocation(f.getLocation().x + 50, f.getLocation().y + 50);
		final Container container = dialog.getContentPane();
		container.setLayout(new BorderLayout(10, 10));
		
		JTabbedPane tabPane = new JTabbedPane();
		container.add(tabPane, BorderLayout.CENTER);
		
		multipleFileChooser = new MultipleFileChooser(frame, msgHandler);
		multipleFileChooser.setFileExtension("hdf");
		//multipleFileChooser.activeListSaveLoadButtons(true);				
		tabPane.addTab("Files", null, multipleFileChooser, "Select Files to open");
		
		JPanel histPanel = createHistSelectPanel();
		tabPane.addTab("Histograms", null, histPanel, "Select Histograms to open");		
		
		tabPane.addChangeListener(new ChangeListener() {
	        // This method is called whenever the selected tab changes
	        public void stateChanged(ChangeEvent evt) {
	            JTabbedPane pane = (JTabbedPane)evt.getSource();
	            changeSelectedTab(pane.getSelectedIndex());
	        }
	    });		
		
		// Lower panel with buttons 
		final JPanel pLower = new JPanel();
		container.add(pLower, BorderLayout.SOUTH);
		final JPanel pButtons = new JPanel(new GridLayout(1, 0, 5, 5));
		pLower.add(pButtons);
		
		JButton bLoadlist = new JButton("Load List");
		pButtons.add(bLoadlist);
		bLoadlist.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				multipleFileChooser.loadList();
			}
		});
		
		JButton bSavelist = new JButton("Save List");
		pButtons.add(bSavelist);		
		bSavelist.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				multipleFileChooser.saveList();
			}
		});
		
		bOK = new JButton(OK);
		bOK.setActionCommand(OK);
		pButtons.add(bOK);		
		bOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				doOK();
			}
		});
		
		bApply = new JButton(APPLY);
		bApply.setActionCommand(APPLY);
		pButtons.add(bApply);		
		bApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				doApply();
			}
		});

		bCancel = new JButton(CANCEL);
		bCancel.setActionCommand(CANCEL);
		pButtons.add(bCancel);		
		bCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				doCancel();
			}
		});

		dialog.setResizable(false);
		dialog.pack();
	}
	
	/**
	 * Create the histogram selection Panel
	 * @return
	 */
	private JPanel createHistSelectPanel() {
		
		JPanel panel = new JPanel(new BorderLayout());
		
		histListModel = new DefaultListModel();
		histList = new JList(histListModel);
		histList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		histList.setSelectedIndex(0);
		histList.setVisibleRowCount(10);
		JScrollPane listScrollPane = new JScrollPane(histList);
		listScrollPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		panel.add(listScrollPane, BorderLayout.CENTER);
		
		JPanel pOption = new JPanel(new FlowLayout(FlowLayout.LEFT,5,5));
		JLabel lFile = new JLabel("Histograms list File:");
		pOption.add(lFile);
		
		txtHistListFile = new JTextField();
		txtHistListFile.setColumns(20);
		txtHistListFile.setEditable(false);
		pOption.add(txtHistListFile);		
		JButton bRefresh = new JButton("Refresh");
		bRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				refreshHistList();
			}
		});
		pOption.add(bRefresh);
		
		panel.add(pOption, BorderLayout.SOUTH);
		return panel;
		
	}	
	/**
	 * Show the dialog box
	 *
	 */
	public void show(){
		dialog.show();
	}
	/**
	 * OK button
	 *  
	 */
	private void doOK() {
		doApply();
		doCancel();
	}
	/**
	 * Apply Button
	 *  
	 */
	private void doApply() {
		loadFiles();
		broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		//JamStatus.getSingletonInstance().setCurrentHistogram(firstHist);
		//broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, firstHist);
	}

	/**
	 * Cancel Button
	 *  
	 */
	private void doCancel() { 
		dialog.dispose();
	}
	/**
	 * Change of selected tab
	 * @param tabIndex
	 */
	public void changeSelectedTab(int tabIndex){
		if (tabIndex==1) {
			refreshHistList();
		}			
	}	
	/**
	 * Refresh the histogram list
	 *
	 */
	public void refreshHistList() {
		
		File file =multipleFileChooser.getSelectedFile();
		histListModel.clear();
		txtHistListFile.setText("");
		if (file!=null){
			txtHistListFile.setText(file.getAbsolutePath());
			loadHistNames(file);
		} 		
		
	}	
	/**
	 * Load name of histograms from the selected file
	 *  
	 */
	private void loadHistNames(File fileSelect) {
		
		List histAttributes;

		// Read in histogram names attributes		
		histAttributes= hdfio.readHistogramAttributes(fileSelect);
		Iterator iter = histAttributes.iterator(); 
		while (iter.hasNext()) {
			HDFIO.HistogramAttributes histAtt= (HDFIO.HistogramAttributes)iter.next();
			histListModel.addElement(histAtt.getName());				
		}
		
		histList.clearSelection();
	}
	
	/**
	 * Load the histograms in the selected list from the
	 * selected files
	 */
	private void loadFiles() {

		List histogramNamesSelected=null;								
		Histogram firstHist=null;
		boolean isFirstFile;
		int i;
		
		Object[] selected = histList.getSelectedValues();
				
		//No histograms selected
		if (selected.length == 0) {
			msgHandler.errorOutln("No histograms selected");
			return;
		}
		
		//Put selected histograms into a list
		histogramNamesSelected =new ArrayList();
		for (i=0; i<selected.length;i++) {
			histogramNamesSelected.add((String)selected[i]);
		}
		
		//Loop for all file
		isFirstFile=true;
		Enumeration enumFile =multipleFileChooser.getFileElements();
		while (enumFile.hasMoreElements()) {
			File file = (File)enumFile.nextElement();
			if (isFirstFile) {	
				hdfio.readFile(FileOpenMode.OPEN, file, histogramNamesSelected);
				isFirstFile=false;
			}else {
				hdfio.readFile(FileOpenMode.OPEN_ADDITIONAL, file, histogramNamesSelected);
			}
		}

	}
}
