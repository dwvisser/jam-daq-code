package jam.io.control;

import jam.data.DataBase;
import jam.data.Group;
import jam.data.Histogram;
import jam.data.control.AbstractControl;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.global.SortMode;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFException;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;
import jam.io.hdf.HistogramAttributes;
import jam.ui.MultipleFileChooser;
import jam.ui.PanelOKApplyCancelButtons;

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
public class OpenMultipleFiles implements HDFIO.AsyncListener{

	//UI components
	private final Frame frame;
	
	private final JDialog dialog;

	private JList histList;

	private DefaultListModel hListModel;
	
	private JTextField txtListFile;
	
	private JCheckBox chkBoxAdd;

	private final PanelOKApplyCancelButtons okApply;
		
	private final MultipleFileChooser multiChooser;
	
	private int [] prevSelected=null;
	
    private List histAttributesList = new ArrayList();
    
	/** HDF file reader */
	private final HDFIO hdfio;
	/** Messages output */
	private final MessageHandler msgHandler;
	/** Broadcaster */
	private final Broadcaster broadcaster;
	
	private final JamStatus STATUS=JamStatus.getSingletonInstance();

	/**
	 * Constructs an object which uses a dialog to open a selected histogram out of an
	 * HDF file.
	 *  
	 * @param parent parent frame
	 * @param console where to print messages
	 */
	public OpenMultipleFiles(Frame parent, MessageHandler console) {		
		frame = parent;
		msgHandler = console;
		broadcaster= Broadcaster.getSingletonInstance();
		hdfio = new HDFIO(frame, msgHandler);
		dialog = new JDialog (frame, "Open Multiple Files");
		dialog.setLocation(parent.getLocation().x + 50, parent.getLocation().y + 50);
		final Container container = dialog.getContentPane();
		container.setLayout(new BorderLayout(10, 10));
		JTabbedPane tabPane = new JTabbedPane();
		container.add(tabPane, BorderLayout.CENTER);
		multiChooser = new MultipleFileChooser(frame, msgHandler);
		multiChooser.setFileFilter(new HDFileFilter(true));
		tabPane.addTab("Files", null, multiChooser, "Select Files to open");		
		JPanel histPanel = createHistSelectPanel();
		tabPane.addTab("Histograms", null, histPanel, "Select Histograms to open");		
		tabPane.addChangeListener(new ChangeListener() {
	        // This method is called whenever the selected tab changes
	        public void stateChanged(ChangeEvent evt) {
	            final JTabbedPane pane = (JTabbedPane)evt.getSource();
	            changeSelectedTab(pane.getSelectedIndex());
	        }
	    });		
		/*  Lower panel with buttons */ 
		final JPanel pLower = new JPanel(new BorderLayout(5,5));
		container.add(pLower, BorderLayout.SOUTH);
		final JPanel pLoad = new JPanel(new FlowLayout(FlowLayout.CENTER, 10,0));
		pLower.add(pLoad, BorderLayout.NORTH);
		final JPanel pLoadButtons = new JPanel(new GridLayout(1, 0, 10, 0));		
		pLoad.add(pLoadButtons, BorderLayout.NORTH);
		JButton bLoadlist = new JButton("Load List");
		pLoadButtons.add(bLoadlist);
		bLoadlist.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				multiChooser.loadList();
			}
		});
		JButton bSavelist = new JButton("Save List");
		pLoadButtons.add(bSavelist);		
		bSavelist.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				multiChooser.saveList();
			}
		});
		okApply = new PanelOKApplyCancelButtons(new PanelOKApplyCancelButtons.DefaultListener(dialog){
			public void apply() {
				defaultSelection();
				loadFiles();
				broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
			}
		});
		pLower.add(okApply.getComponent(),BorderLayout.SOUTH);
		dialog.setResizable(false);
		dialog.pack();
	}
	
	/**
	 * Create the histogram selection Panel
	 * @return the histogram selection panel
	 */
	private JPanel createHistSelectPanel() {
		final JPanel panel = new JPanel(new BorderLayout());
		final JPanel pOption = new JPanel(new FlowLayout(FlowLayout.LEFT,5,5));
		final JLabel lFile = new JLabel("Histograms list File:");
		pOption.add(lFile);
		txtListFile = new JTextField();
		txtListFile.setColumns(20);
		txtListFile.setEditable(false);
		pOption.add(txtListFile);		
		chkBoxAdd = new JCheckBox("Sum Histograms");
		pOption.add(chkBoxAdd);
		panel.add(pOption, BorderLayout.NORTH);		
		hListModel = new DefaultListModel();
		histList = new JList(hListModel);
		histList.setSelectedIndex(0);
		histList.setVisibleRowCount(10);
		final JScrollPane listPane = new JScrollPane(histList);
		listPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		panel.add(listPane, BorderLayout.CENTER);		
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
		saveSelectedHistograms();
		final File file =multiChooser.getSelectedFile();
		hListModel.clear();
		txtListFile.setText("");
		if (file!=null){
			txtListFile.setText(file.getAbsolutePath());
			if (loadHistNames(file)){
				updateSelectedHistograms();
			}
		}
	}
	
	private void saveSelectedHistograms(){
		prevSelected = histList.getSelectedIndices();		
	}
	
	private void updateSelectedHistograms() {
		final ListModel listModel =histList.getModel();		
		int [] selected = histList.getSelectedIndices();
		if (prevSelected!=null) {
			selected=prevSelected;
		} 
        /* Non-selected select all */
		checkSelectionIsNone();
	}

	private void checkSelectionIsNone() {
		final ListModel listModel =histList.getModel();	
		int size =listModel.getSize();
		int [] selected = histList.getSelectedIndices();
		selected = histList.getSelectedIndices();
	    if (selected.length==0) {        	
	    	int [] indexs = new int [listModel.getSize()];
	    	for (int i=0; i<listModel.getSize(); i++) {
	    		indexs[i]=i;
	    	}
	    	histList.setSelectedIndices(indexs);
	    } 
	}
	/**
	 * Check there are histogram in the histogram list
	 *
	 */
	private void checkHistogramsLoaded() {
		final ListModel listModel =histList.getModel();	
		int size =listModel.getSize();
		if (size ==0) {
			refreshHistList();
		}		
	}
	
	/* non-javadoc:
	 * Load name of histograms from the selected file
	 *  
	 */
	private boolean loadHistNames(File fileSelect) {
		boolean loadState;
		/* Read in histogram names attributes */	
		try {

			/* Read in histogram names attributes */		
			final List attrList= hdfio.readHistogramAttributes(fileSelect);
			final Iterator iter = attrList.iterator(); 
			while (iter.hasNext()) {
				final HistogramAttributes histAtt= (HistogramAttributes)iter.next();
				hListModel.addElement(histAtt.getFullName());				
			}
		
			loadState=true;
		}catch (HDFException hdfe){
			msgHandler.errorOutln(hdfe.toString());
			loadState=false;
		}
		histList.clearSelection();
		
		return loadState;
		
	}
	
	/**
     * Load the histograms in the selected list from the selected files
     */
    private void loadFiles() {
        File file;
        checkHistogramsLoaded();
        final List selectHistAttributes = createSelectedHistogramNamesList();
        if (selectHistAttributes.size() == 0) {//No histograms selected
            msgHandler.errorOutln("No histograms selected");
            return;
        }

        Object [] objFiles = (Object [])multiChooser.getFileList().toArray();
        File [] files =new File[objFiles.length];
        for (int i=0;i<objFiles.length; i++) {
        	files[i] =(File)objFiles[i];
        }
        
        /* Sum counts */
        if (chkBoxAdd.isSelected()) {
            /* Create blank histograms */
            file = multiChooser.getSelectedFile();
            hdfio.setListener(this);
            hdfio.readFile(FileOpenMode.OPEN, files[0], selectHistAttributes);
            /* Rename group */
            final Group fileGroup = Group.getGroup(file.getName());
//            fileGroup.setName("Sum");
            Histogram.setZeroAll();
            hdfio.readFile(FileOpenMode.ADD, files, null, selectHistAttributes);
            //while (iter.hasNext()) {
            //	file = (File) iter.next();                
            //}
            /* Open multiple groups */
        } else {
            DataBase.getInstance().clearAllLists();
            hdfio.setListener(this);
            hdfio.readFile(FileOpenMode.OPEN_MORE, files, null, selectHistAttributes);
            STATUS.setSortMode(SortMode.FILE, "Multiple");            
        }
    }      
    
    private List createSelectedHistogramNamesList() {
    	//Set selection to all if none selected
    	checkSelectionIsNone();
        //final List selectNames = new ArrayList();
        final Object[] selected = histList.getSelectedValues();
        histAttributesList.clear();
        /* Put selected histograms into a list */
        for (int i = 0; i < selected.length; i++) {
        	//Get name from full name
        	String histogramFullName = (String)selected[i];
        	HistogramAttributes histAttributes =HistogramAttributes.getHistogramAttribute(histogramFullName);
        	histAttributesList.add(histAttributes);
        	
        	//String histName = histAttributes.getName();        	
            //selectNames.add(histName);
            
        }
        //HistogramAttributes.filterList(histFullNamesList);
    	return histAttributesList;
    }

    private void defaultSelection() { 
		final ListModel listModel =histList.getModel(); 
	    /* Non-selected select all */
		final int [] selectIndex = histList.getSelectedIndices();
	    if (selectIndex.length==0) {        	
	    	int [] indexs = new int [listModel.getSize()];
	    	for (int i=0; i<listModel.getSize(); i++) {
	    		indexs[i]=i;
	    	}
	    	histList.setSelectedIndices(indexs);
	    } 
    }
	private void notifyApp() {
		//Update app status		
		AbstractControl.setupAll();
		broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		
		//Set the current histogram to the first opened histogram
		List groupList = Group.getGroupList();
		if (groupList.size()>0) {
			Group firstGroup =(Group)Group.getGroupList().get(0); 
			STATUS.setCurrentGroup(firstGroup);
			List histList =firstGroup.getHistogramList();
			if (histList.size()>0) {
				Histogram firstHist = (Histogram)histList.get(0);
				if (histList!=null) {
					STATUS.setCurrentHistogram(firstHist);
					broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, firstHist);
				}
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

