package jam.io.hdf;

import jam.applet.HistApplet;
import jam.data.AbstractHist1D;
import jam.data.Group;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.io.ExtensionFileFilter;
import jam.ui.MultipleFileChooser;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
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
	private JDialog dialog;

	private JList histList;

	private DefaultListModel histListModel;
	
	private JTextField txtHistListFile;
	
	private JButton bOK;

	private JButton bApply;

	private JButton bCancel;

	private Frame frame;

	private final String OK = "OK";

	private final String CANCEL = "Cancel";

	private final String APPLY = "Apply";

	private File lastFile;
	/**
	 * File to read histogram information from
	 */
	private File fileOpen;

	private HDFile hdfFile;
	
	private HDFIO hdfio;
	
	MultipleFileChooser multipleFileChooser;

	/** Hash to store read histograms before loaded */
	private Map loadedHistograms;

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
		dialog = new JDialog(frame, "Open Multiple Files");
		dialog.setLocation(f.getLocation().x + 50, f.getLocation().y + 50);
		final Container container = dialog.getContentPane();
		
		hdfio = new HDFIO(frame, msgHandler);
		//container.setLayout(new GridLayout(1, 1));


		container.setLayout(new BorderLayout(10, 10));
		
		JTabbedPane tabPane = new JTabbedPane();
		container.add(tabPane, BorderLayout.CENTER);
		
		multipleFileChooser = new MultipleFileChooser(frame, msgHandler);
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
		/*
		final JPanel pFileInd = new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JLabel filelabel = new JLabel("File: ", JLabel.RIGHT);		
		pFileInd.add(filelabel);
		txtFile = new JTextField(20);
		txtFile.setEditable(false);
		pFileInd.add(txtFile);
		container.add(pFileInd, BorderLayout.NORTH);
		// Selection list
		DefaultListModel listModel = new DefaultListModel();
		histList = new JList(listModel);
		histList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		histList.setSelectedIndex(0);
		histList.setVisibleRowCount(10);
		JScrollPane listScrollPane = new JScrollPane(histList);
		listScrollPane.setBorder(new EmptyBorder(0, 10, 0, 10));
		container.add(listScrollPane, BorderLayout.CENTER);
		*/
		
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
		JLabel lFile = new JLabel("Histograms list File");
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
		//JCheckBox chkAllHists = new JCheckBox("All");
		//pOption.add(chkAllHists);		
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
		//Histogram firstHist=loadHistograms();
		//broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		//JamStatus.getSingletonInstance().setCurrentHistogram(firstHist);
		//broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, firstHist);
	}

	/**
	 * Cancel Button
	 *  
	 */
	private void doCancel() {

		//Clear memory
		dialog.dispose();
	}

	/**
	 * Entry points show the dialog if a file is chosen to open
	 *  
	 */
	public void open() {
		//openLoadNames();
	}
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
	 * 
	 * @param tabIndex
	 */
	public void changeSelectedTab(int tabIndex){
		if (tabIndex==1) {
			refreshHistList();
		}
			
	}
	/**
	 * 
	 *  
	 */
	private void loadHistNames(File fileOpen) {
		List histAttributes;
		List histNames;
		/* Read in histogram names */
		
		histAttributes= hdfio.readHistogramAttributes(fileOpen);
		Iterator iter = histAttributes.iterator(); 
		while (iter.hasNext()) {
			HDFIO.HistogramAttributes histAtt= (HDFIO.HistogramAttributes)iter.next();
			histListModel.addElement(histAtt.name);				
			/* Hash to store histograms */
		}
	}

	/* non-javadoc:
	 * Load the histograms in the selected list.
	 */
	/*
	private Histogram loadHistograms() {
		Object[] selected = histList.getSelectedValues();
		Histogram firstHist=null;
		int i;
		//No histograms selected
		if (selected.length > 0) {
			Group.createGroup(fileOpen.getName(), Group.Type.FILE);
			//Loop for each selected item
			for (i = 0; i < selected.length; i++) {
				if (loadedHistograms.containsKey(selected[i])) {
					HDFIO.HistogramAttributes histProp = (HDFIO.HistogramAttributes) loadedHistograms
							.get(selected[i]);
					//Histogram hist=createHistogram(histProp);
					if (i==0)
						firstHist=hist;

				}
			}
		} else {
			firstHist=null;
			msgHandler.errorOutln("No histograms selected");
		}
		return firstHist;
	}
	*/


	/* non-javadoc: 
	 * Create a histogram using HistProp
	 */
	/*
	private Histogram createHistogram(HistProp histProp) {
		String fileName = hdfFile.getFile().getName();
		int index = fileName.indexOf(".hdf");
		if (index > 0){
			fileName = fileName.substring(0, index);
		}
		final String name = histProp.name.trim();
		final String title = histProp.title;
		final Histogram hist=Histogram.createHistogram(histProp.dataArray, name, title);
		if (histProp.histDim == 1) {
			final AbstractHist1D hist1d=(AbstractHist1D)hist;
			if (histProp.errorArray != null) {
				hist1d.setErrors((double[])histProp.errorArray);
			}
		}
		return hist;
	}
	*/
}
