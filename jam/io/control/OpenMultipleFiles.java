package jam.io.control;

import jam.data.DataBase;
import jam.data.Group;
import jam.data.Histogram;

import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.MessageHandler;
import jam.global.JamStatus;
import jam.global.SortMode;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;
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
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
	
	private JCheckBox chkBoxAdd;

	private final String OK = "OK";

	private final String CANCEL = "Cancel";

	private final String APPLY = "Apply";
		
	private MultipleFileChooser multipleFileChooser;
	
	private int [] previousSelectedHistograms=null;
	/** HDF file reader */
	private HDFIO hdfio;
	/** Messages output */
	private MessageHandler msgHandler;
	/** Broadcaster */
	private Broadcaster broadcaster;
	
	private JamStatus STATUS=JamStatus.getSingletonInstance();

	/**
	 * Constructs an object which uses a dialog to open a selected histogram out of an
	 * HDF file.
	 *  
	 * @param f parent frame
	 * @param m where to print messages
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
		multipleFileChooser.setFileFilter(new HDFileFilter(true));
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
	 * @return the histogram selection panel
	 */
	private JPanel createHistSelectPanel() {
		
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel pOption = new JPanel(new FlowLayout(FlowLayout.LEFT,5,5));
		JLabel lFile = new JLabel("Histograms list File:");
		pOption.add(lFile);
		
		txtHistListFile = new JTextField();
		txtHistListFile.setColumns(20);
		txtHistListFile.setEditable(false);
		pOption.add(txtHistListFile);		
		chkBoxAdd = new JCheckBox("Sum Histograms");
		pOption.add(chkBoxAdd);
		/*
		JButton bRefresh = new JButton("Refresh");
		bRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				refreshHistList();
			}
		});
		pOption.add(bRefresh);
		*/
		panel.add(pOption, BorderLayout.NORTH);
		
		histListModel = new DefaultListModel();
		histList = new JList(histListModel);
		//histList
		//		.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		histList.setSelectedIndex(0);
		histList.setVisibleRowCount(10);
		JScrollPane listScrollPane = new JScrollPane(histList);
		listScrollPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		panel.add(listScrollPane, BorderLayout.CENTER);
		
		histList.clearSelection();
		
		return panel;
		
	}	
	
	/**
	 * Show the dialog box.
	 */
	public void show(){
		dialog.setVisible(true);
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
		defaultSelection();
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
	private void refreshHistList() {
		
		//save Selection
		saveSelectedHistograms();
		
		File file =multipleFileChooser.getSelectedFile();
		histListModel.clear();
		txtHistListFile.setText("");
		if (file!=null){
			txtHistListFile.setText(file.getAbsolutePath());
			loadHistNames(file);
		}
		
		//update Selection
		updateSelectedHistograms();
	}
	
	private void saveSelectedHistograms(){
		previousSelectedHistograms = histList.getSelectedIndices();		
	}
	
	private void updateSelectedHistograms() {

		ListModel listModel =histList.getModel();		
		int [] selectedIndexs;
		
		selectedIndexs = histList.getSelectedIndices();
		
		//histList.clearSelection();
				
		if (previousSelectedHistograms!=null) {
			selectedIndexs=previousSelectedHistograms;
		} 
		
    	//histList.setSelectedIndices(selectedIndexs);
    	
        //Non-selected select all
		selectedIndexs = histList.getSelectedIndices();
        if (selectedIndexs.length==0) {        	
        	int [] indexs = new int [listModel.getSize()];
        	for (int i=0; i<listModel.getSize(); i++) {
        		indexs[i]=i;
        	}
        	histList.setSelectedIndices(indexs);
        } else {
        //	histList.setSelectedIndices(selectedIndexs);
        	
        	/*
        	for (int i=0;i<selectedIndexs.length;i++) {
            	int [] indexs = new int [listModel.getSize()];            	
        		if (selectedIndexs[i]<listModel.getSize()) {
            		histList.        			
        		}
        	}
        	*/
        }
        
        //histList.clearSelection();
	}
	
	
	/* non-javadoc:
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
		
	}
	
	/**
     * Load the histograms in the selected list from the selected files
     */
    private void loadFiles() {
    	
    	File file;
    	
    	List selectedHistogramNames = createSelectedHistogramNamesList();  
    	
        if (selectedHistogramNames.size() == 0) {//No histograms selected
            msgHandler.errorOutln("No histograms selected");
            return;
        }
        
        final Iterator iter = multipleFileChooser.getFileList().iterator();
        
        //Sum counts
    	if (chkBoxAdd.isSelected()) {
    		//Create blank histograms
    		file =multipleFileChooser.getSelectedFile();
            hdfio.readFile(FileOpenMode.OPEN, file, 
            		selectedHistogramNames);
            //Rename group
            Group fileGroup = Group.getGroup(file.getName());
            fileGroup.setName("Sum");
            Histogram.setZeroAll();

            while (iter.hasNext()) {
    			file = (File) iter.next();            	
        		hdfio.readFile(FileOpenMode.ADD, file,
        				selectedHistogramNames);
            }
            
        //Open multiple groups    
    	}else {
    	
    		/* Loop for all files */
    		//boolean isFirstFile = true;
    		
    		DataBase.getInstance().clearAllLists();
    		while (iter.hasNext()) {
    			file = (File) iter.next();
    			
        		hdfio.readFile(FileOpenMode.OPEN_ADDITIONAL, file,
						selectedHistogramNames);            		            		
        		STATUS.setSortMode(SortMode.FILE, "Multiple");
    			//if (isFirstFile) {
    			//	isFirstFile = false;
                //    hdfio.readFile(FileOpenMode.OPEN, file, 
                //    		selectedHistogramNames);            		
    			//} else {
            	//	hdfio.readFile(FileOpenMode.OPEN_ADDITIONAL, file,
            	//					selectedHistogramNames);            		            		
              //}
    		}
         }
    }        
    private List createSelectedHistogramNamesList() {
    	
        final List histogramNamesSelected = new ArrayList();
                
        Object[] selected = histList.getSelectedValues();        

        /* Put selected histograms into a list */
        for (int i = 0; i < selected.length; i++) {
            histogramNamesSelected.add(selected[i]);
        }
    	return histogramNamesSelected;
    }

    private void defaultSelection() { 
		ListModel listModel =histList.getModel();
 
	    //Non-selected select all
		int [] selectedIndexs = histList.getSelectedIndices();
	    if (selectedIndexs.length==0) {        	
	    	int [] indexs = new int [listModel.getSize()];
	    	for (int i=0; i<listModel.getSize(); i++) {
	    		indexs[i]=i;
	    	}
	    	histList.setSelectedIndices(indexs);
	    } 

    }
}
