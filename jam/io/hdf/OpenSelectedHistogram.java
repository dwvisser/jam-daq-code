package jam.io.hdf;

import jam.global.MessageHandler;
import jam.util.StringUtilities;
import jam.data.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import java.util.*;


/**
 * Reads and displays list of the histograms 
 * for a user select hdf file. Then loads the
 * user selected histograms into the data base
 * and appends the file indicator to the histogram
 * name  
 *  
 * @author Ken
 *
 */
public class OpenSelectedHistogram {

	//UI components
	private JDialog dialog;
	private JTextField txtFileInd;
	private JList histList;
	private JButton bOK;
	private JButton bApply;
	private JButton bCancel;	
	private Frame frame;
	
	private  final String OK = "OK";
	private  final String CANCEL = "Cancel";
	private  final String APPLY = "Apply";
		
	/**
	 * File to read histogram information from
	 */
	private File fileOpen;	
	private HDFile hdfFile;
	/** Hash to store read histograms before loaded */
	private HashMap loadedHistograms;
	/** Append to name to indicate file */
	private String fileIndicator;
	/** Messages output */
	private MessageHandler console; 


	public OpenSelectedHistogram(Frame f, MessageHandler c){
		frame = f;
		console = c;
		
		dialog =new JDialog(f, "Open Selected Histograms", false);
		dialog.setLocation(f.getLocation().x+50, f.getLocation().y+50);
		//dialog.setLocation(f.getLocation());		
		//Container
		final Container container = dialog.getContentPane();
		container.setLayout(new BorderLayout(10,10));
		
		//Panel with file indicator
		final JPanel pFileInd = new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JLabel runlabel = new JLabel("File Indicator", JLabel.RIGHT);
		pFileInd.add(runlabel);
		txtFileInd = new JTextField(10);
		txtFileInd.setToolTipText("Text added to histgram name to indicate the file");
		pFileInd.add(txtFileInd);						
		container.add(pFileInd, BorderLayout.NORTH);				

		//Selection list				 
		DefaultListModel listModel = new DefaultListModel();		
		histList = new JList(listModel);		
		histList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		histList.setSelectedIndex(0);
		histList.setVisibleRowCount(5);		
		JScrollPane listScrollPane = new JScrollPane(histList);	
		listScrollPane.setBorder(new EmptyBorder(0,10,0,10));	
		container.add(listScrollPane, BorderLayout.CENTER);										
		
		//Lower panel with buttons
		final JPanel pLower = new JPanel();
		container.add(pLower, BorderLayout.SOUTH);
		final JPanel pButtons = new JPanel(new GridLayout(1,0,5,5));
		pLower.add(pButtons);		
		bOK = new JButton(OK);
		bOK.setActionCommand(OK);
		bOK.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				doOK();
			}
		});
		pButtons.add(bOK);
		bApply = new JButton(APPLY);
		bApply.setActionCommand(APPLY);
		bApply.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent ae){
				doApply();
			}
		});		
		pButtons.add(bApply);		
		bCancel = new JButton(CANCEL);
		bCancel.setActionCommand(CANCEL);
		bCancel.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent ae){
				doCancel();
			}
		});
		pButtons.add(bCancel);
		
		dialog.setResizable(false);				
		dialog.pack();
		
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
		createFileIndicator();
		loadHistograms(); 
	}
	/**
	 * Cancel Button
	 *
	 */
	private void doCancel() {
		
		//Clear memory
		loadedHistograms.clear();
		loadedHistograms=null;
		
		dialog.dispose();
	}
	
	/**
	 * Entry points show the dialog if a 
	 * file is chosen to open
	 *
	 */
	public void open(){
		openLoadNames();
	}
	/**
	 * 
	 *
	 */
	private void openLoadNames(){
		ArrayList histNames;
		try {
			if (openFile()){ 
				//Read in histogram names
				hdfFile= new HDFile(fileOpen, "r");
				hdfFile.readObjects();		
				//Hash to store histograms
				loadedHistograms = new HashMap();
				histNames=readHistograms();
				//readHistograms
				hdfFile.close();				
				histList.setListData(histNames.toArray()); 
				dialog.show();
			}	
		} catch (IOException ioE ) {
			console.messageOut("", MessageHandler.END);
			console.errorOutln("Error opening file: "+fileOpen.toString());			
		} catch (HDFException hdfE ) {
			console.messageOut("", MessageHandler.END);			
			console.errorOutln("Error reading histograms "+hdfE.toString());
		}
			
	}
	/**
	 * Load the histograms in the selected list
	 *
	 */
	private void loadHistograms() {
		
		Object[] selected= histList.getSelectedValues();
		int i;
				
		//No histograms selected
		if (selected.length >0) {
			//Loop for each selected item
			for (i=0;i<selected.length;i++) {
				if (loadedHistograms.containsKey(selected[i])) {
					HistProp histProp =(HistProp)loadedHistograms.get(selected[i]);				
						createHistogram(histProp);
										
				}
			}	
		} else {
			console.errorOutln("No histograms selected");			
		}		
	}
			


	/**
	 * Read in an unspecified file by opening up a dialog box.
	 *
	 * @param  mode  whether to open or reload
	 * @return  <code>true</code> if successful
	 */
	public boolean openFile() {
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
		} else { //dialog didn't return a file
			openF = false;
		}
		return openF;
	}
	

	/**
	 * Reads in the histogram and hold them in a tempory array
	 *
	 * @exception   HDFException  thrown if unrecoverable error occurs
	 */
	private ArrayList readHistograms() throws HDFException {
		
		ArrayList histNames = new ArrayList();
		
		String name;
		String title;
		int number;
		int sizeX;
		int sizeY;
		int histDim;
		byte histNumType;  
		Object dataArray;
		
		final StringUtilities su = StringUtilities.instance();
		NumericalDataGroup ndg = null;
		/* I check ndgErr==null to determine if error bars exist */
		NumericalDataGroup ndgErr = null;
		Histogram histogram;
		/* get list of all VG's in file */
		final java.util.List groups = hdfFile.ofType(DataObject.DFTAG_VG);
		final VirtualGroup hists =
			VirtualGroup.ofName(groups, JamHDFFields.HIST_SECTION_NAME);
		/* only the "histograms" VG (only one element) */
		ScientificData sdErr = null;
		if (hists != null) {
			/* clear if opening and there are histograms in file */
			/*if (mode==OPEN) {
				DataBase.getInstance().clearAllLists();
			}*/
			/* get list of all DIL's in file */
			final java.util.List labels = hdfFile.ofType(DataObject.DFTAG_DIL);
			/* get list of all DIA's in file */
			final java.util.List annotations = hdfFile.ofType(DataObject.DFTAG_DIA);
			final Iterator temp = hists.getObjects().iterator();
			while (temp.hasNext()) {
				final VirtualGroup current = (VirtualGroup) (temp.next());
				final java.util.List tempVec =
				hdfFile.ofType(current.getObjects(), DataObject.DFTAG_NDG);
				final NumericalDataGroup[] numbers =
					new NumericalDataGroup[tempVec.size()];
				tempVec.toArray(numbers);
				if (numbers.length == 1) {
					ndg = numbers[0]; //only one NDG -- the data
				} else if (numbers.length == 2) {
					if (DataIDLabel
						.withTagRef(
							labels,
							DataObject.DFTAG_NDG,
							numbers[0].getRef())
						.getLabel()
						.equals(JamHDFFields.ERROR_LABEL)) {
						ndg = numbers[1];
						ndgErr = numbers[0];
					} else {
						ndg = numbers[0];
						ndgErr = numbers[1];
					}
				} else {
					throw new HDFException(
						"Invalid number of data groups ("
							+ numbers.length
							+ ") in NDG.");
				}
				final ScientificData sd =
					(ScientificData) (hdfFile
						.ofType(ndg.getObjects(), DataObject.DFTAG_SD)
						.get(0));
				final ScientificDataDimension sdd =
					(ScientificDataDimension) (hdfFile
						.ofType(ndg.getObjects(), DataObject.DFTAG_SDD)
						.get(0));
				final DataIDLabel numLabel =
					DataIDLabel.withTagRef(labels, ndg.getTag(), ndg.getRef());
					
				number = Integer.parseInt(numLabel.getLabel());
				histNumType = sdd.getType();
				sd.setNumberType(histNumType);
				histDim = sdd.getRank();
				sd.setRank(histDim);
				sizeX = sdd.getSizeX();
				sizeY = 0;
				if (histDim == 2) {
					sizeY = sdd.getSizeY();
				}
				final DataIDLabel templabel =
					DataIDLabel.withTagRef(
						labels,
						current.getTag(),
						current.getRef());
				final DataIDAnnotation tempnote =
					DataIDAnnotation.withTagRef(
						annotations,
						current.getTag(),
						current.getRef());
				name = templabel.getLabel();
				title = tempnote.getNote();
				if (ndgErr != null) {
					sdErr =
						(ScientificData) (hdfFile
							.ofType(ndgErr.getObjects(), DataObject.DFTAG_SD)
							.get(0));
					sdErr.setRank(histDim);
					sdErr.setNumberType(NumberType.DOUBLE);
					/*final ScientificDataDimension sddErr =(ScientificDataDimension)(in.ofType(
					ndgErr.getObjects(),DataObject.DFTAG_SDD).get(0));*/
				}
				//read in data
				if (histDim == 1) {
					if (histNumType == NumberType.INT) {
						dataArray = sd.getData1d(sizeX);
					} else { //DOUBLE
						dataArray=sd.getData1dD(sizeX);
					}
					if (ndgErr != null) {
					//FIXME KBS	histogram.setErrors(sdErr.getData1dD(sizeX));
					}
				} else { //2d

					if (histNumType == NumberType.INT) {
						dataArray =	sd.getData2d(sizeX, sizeY);
					} else {
						dataArray =	sd.getData2dD(sizeX, sizeY);
					}
				}
				
				//Add histogram
				HistProp histProp = new HistProp();
				histProp.name=name;
				histProp.title=title;
				histProp.number=number;
				histProp.sizeX=sizeX;
				histProp.sizeY=sizeY;
				histProp.histDim=histDim;
				histProp.histNumType=histNumType;  
				histProp.dataArray=dataArray;
				loadedHistograms.put(name, histProp);
				
				histNames.add(name);
				//msgHandler.messageOut(". ", MessageHandler.CONTINUE);
			}
			
		} //hist !=null
		return histNames;
	}

	/**
	 * Create a histogram using HistProp 
	 * @param histProp
	 */
	private void createHistogram(HistProp histProp){
		Histogram histogram;
		
		//Unpack to local for convience
		String name=histProp.name.trim()+fileIndicator;
		String title=histProp.title;
		int number=histProp.number;
		int sizeX=histProp.sizeX;
		int sizeY=histProp.sizeY;
		int histDim=histProp.histDim;
		byte histNumType=histProp.histNumType;  
		Object dataArray=histProp.dataArray;
		

		if (histDim == 1) {
			if (histNumType == NumberType.INT) {
				histogram = new Histogram(name, title, (int [])dataArray);
			} else { //DOUBLE
				histogram =
					new Histogram(
						name,
						title,
					(double [])dataArray);
			}
			//FIXME KBS if (ndgErr != null) {
			//	histogram.setErrors(sdErr.getData1dD(sizeX));
			//}
		} else { //2d
			//System.out.println(" x "+sizeY+" channels");
			if (histNumType == NumberType.INT) {
				histogram =
					new Histogram(
						name,
						title,
				(int [][])dataArray);
			} else {
				histogram =
					new Histogram( 
						name,
						title,
							(double [][])dataArray);
			}
		}
		//FIXME KBS histogram.setNumber(number);
	}
	/**
	 * Create file name indicator, appended to file name
	 *
	 */
	private void createFileIndicator(){
		fileIndicator=" ("+txtFileInd.getText()+")";	
	}
	/**
	 * Class to hold histogram properties
	 * while we decide if we should load them
	 *
	 */
	private class HistProp {
		
		String name;
		String title;
		int number;
		int sizeX;
		int sizeY;		
		int histDim;
		byte histNumType;
		Object dataArray;	//generic data array		

		public HistProp(){
		}
						
	}		
}
