package jam.io.hdf;

import jam.data.AbstractHist1D;
import jam.data.Histogram;
import jam.data.Group;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.MessageHandler;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

/**
 * Reads and displays list of the histograms for a user select hdf file. Then
 * loads the user selected histograms into the data base and appends the file
 * indicator to the histogram name
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

	private final String OK = "OK";

	private final String CANCEL = "Cancel";

	private final String APPLY = "Apply";

	/**
	 * File to read histogram information from
	 */
	private File fileOpen;

	private HDFile hdfFile;

	/** Hash to store read histograms before loaded */
	private Map loadedHistograms;

	/** Append to name to indicate file */
	private String fileIndicator;

	/** Messages output */
	private MessageHandler console;
	/** Broadcaster */
	private Broadcaster broadcaster;

	public OpenSelectedHistogram(Frame f, MessageHandler c) {
		frame = f;
		console = c;
		broadcaster= Broadcaster.getSingletonInstance();
		dialog = new JDialog(f, "Open Selected Histograms", false);
		dialog.setLocation(f.getLocation().x + 50, f.getLocation().y + 50);
		final Container container = dialog.getContentPane();
		container.setLayout(new BorderLayout(10, 10));
		/* Panel with file indicator */
		final JPanel pFileInd = new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JLabel runlabel = new JLabel("File Indicator", JLabel.RIGHT);
		pFileInd.add(runlabel);
		txtFileInd = new JTextField(10);
		txtFileInd
				.setToolTipText("Text added to histgram name to indicate the file of origin");
		pFileInd.add(txtFileInd);
		container.add(pFileInd, BorderLayout.NORTH);
		/* Selection list */
		DefaultListModel listModel = new DefaultListModel();
		histList = new JList(listModel);
		histList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		histList.setSelectedIndex(0);
		histList.setVisibleRowCount(5);
		JScrollPane listScrollPane = new JScrollPane(histList);
		listScrollPane.setBorder(new EmptyBorder(0, 10, 0, 10));
		container.add(listScrollPane, BorderLayout.CENTER);
		/* Lower panel with buttons */
		final JPanel pLower = new JPanel();
		container.add(pLower, BorderLayout.SOUTH);
		final JPanel pButtons = new JPanel(new GridLayout(1, 0, 5, 5));
		pLower.add(pButtons);
		bOK = new JButton(OK);
		bOK.setActionCommand(OK);
		bOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				doOK();
			}
		});
		pButtons.add(bOK);
		bApply = new JButton(APPLY);
		bApply.setActionCommand(APPLY);
		bApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				doApply();
			}
		});
		pButtons.add(bApply);
		bCancel = new JButton(CANCEL);
		bCancel.setActionCommand(CANCEL);
		bCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
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
		broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
	}

	/**
	 * Cancel Button
	 *  
	 */
	private void doCancel() {

		//Clear memory
		loadedHistograms.clear();
		loadedHistograms = null;

		dialog.dispose();
	}

	/**
	 * Entry points show the dialog if a file is chosen to open
	 *  
	 */
	public void open() {
		openLoadNames();
	}

	/**
	 * 
	 *  
	 */
	private void openLoadNames() {
		ArrayList histNames;
		try {
			if (openFile()) {
				/* Read in histogram names */
				hdfFile = new HDFile(fileOpen, "r");
				hdfFile.readObjects();
				/* Hash to store histograms */
				loadedHistograms = new HashMap();
				histNames = readHistograms();
				hdfFile.close();
				histList.setListData(histNames.toArray());
				dialog.setVisible(true);
			}
		} catch (IOException ioE) {
			console.messageOut("", MessageHandler.END);
			console.errorOutln("Error opening file: " + fileOpen.toString());
		} catch (HDFException hdfE) {
			console.messageOut("", MessageHandler.END);
			console.errorOutln("Error reading histograms " + hdfE.toString());
		}

	}

	/**
	 * Load the histograms in the selected list
	 *  
	 */
	private void loadHistograms() {

		Object[] selected = histList.getSelectedValues();
		int i;

		//No histograms selected
		if (selected.length > 0) {
			Group.setCurrentGroup(fileOpen.getName());
			//Loop for each selected item
			for (i = 0; i < selected.length; i++) {
				if (loadedHistograms.containsKey(selected[i])) {
					HistProp histProp = (HistProp) loadedHistograms
							.get(selected[i]);
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
	 * @return <code>true</code> if successful
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
	 * @exception HDFException
	 *                thrown if unrecoverable error occurs
	 */
	private ArrayList readHistograms() throws HDFException {
		final ArrayList histNames = new ArrayList();
		NumericalDataGroup ndg = null;
		/* I check ndgErr==null to determine if error bars exist */
		NumericalDataGroup ndgErr = null;
		/* get list of all VG's in file */
		final java.util.List groups = hdfFile.ofType(DataObject.DFTAG_VG);
		final VirtualGroup hists = VirtualGroup.ofName(groups,
				JamHDFFields.HIST_SECTION_NAME);
		/* only the "histograms" VG (only one element) */
		ScientificData sdErr = null;
		if (hists != null) {
			/* get list of all DIL's in file */
			final java.util.List labels = hdfFile.ofType(DataObject.DFTAG_DIL);
			/* get list of all DIA's in file */
			final java.util.List annotations = hdfFile
					.ofType(DataObject.DFTAG_DIA);
			final Iterator temp = hists.getObjects().iterator();
			while (temp.hasNext()) {
				final VirtualGroup current = (VirtualGroup) (temp.next());
				final java.util.List tempVec = hdfFile.ofType(current
						.getObjects(), DataObject.DFTAG_NDG);
				final NumericalDataGroup[] numbers = new NumericalDataGroup[tempVec
						.size()];
				tempVec.toArray(numbers);
				if (numbers.length == 1) {
					ndg = numbers[0]; //only one NDG -- the data
				} else if (numbers.length == 2) {
					if (DataIDLabel.withTagRef(labels, DataObject.DFTAG_NDG,
							numbers[0].getRef()).getLabel().equals(
							JamHDFFields.ERROR_LABEL)) {
						ndg = numbers[1];
						ndgErr = numbers[0];
					} else {
						ndg = numbers[0];
						ndgErr = numbers[1];
					}
				} else {
					throw new HDFException("Invalid number of data groups ("
							+ numbers.length + ") in NDG.");
				}
				final ScientificData sd = (ScientificData) (hdfFile.ofType(ndg
						.getObjects(), DataObject.DFTAG_SD).get(0));
				final ScientificDataDimension sdd = (ScientificDataDimension) (hdfFile
						.ofType(ndg.getObjects(), DataObject.DFTAG_SDD).get(0));
				final DataIDLabel numLabel = DataIDLabel.withTagRef(labels, ndg
						.getTag(), ndg.getRef());
				final int number = Integer.parseInt(numLabel.getLabel());
				final byte histNumType = sdd.getType();
				sd.setNumberType(histNumType);
				final int histDim = sdd.getRank();
				sd.setRank(histDim);
				final int sizeX = sdd.getSizeX();
				final int sizeY = (histDim == 2) ? sdd.getSizeY() : 0;
				final DataIDLabel templabel = DataIDLabel.withTagRef(labels,
						current.getTag(), current.getRef());
				final DataIDAnnotation tempnote = DataIDAnnotation.withTagRef(
						annotations, current.getTag(), current.getRef());
				final String name = templabel.getLabel();
				final String title = tempnote.getNote();
				if (ndgErr != null) {
					sdErr = (ScientificData) (hdfFile.ofType(ndgErr
							.getObjects(), DataObject.DFTAG_SD).get(0));
					sdErr.setRank(histDim);
					sdErr.setNumberType(NumberType.DOUBLE);
					/*
					 * final ScientificDataDimension sddErr
					 * =(ScientificDataDimension)(in.ofType(
					 * ndgErr.getObjects(),DataObject.DFTAG_SDD).get(0));
					 */
				}
				/* read in data */
				final Object dataArray;
				if (histDim == 1) {
					if (histNumType == NumberType.INT) {
						dataArray = sd.getData1d(sizeX);
					} else { //DOUBLE
						dataArray = sd.getData1dD(sizeX);
					}
					/*if (ndgErr != null) {
						histogram.setErrors(sdErr.getData1dD(sizeX));
					}*/
				} else { //2d
					if (histNumType == NumberType.INT) {
						dataArray = sd.getData2d(sizeX, sizeY);
					} else {
						dataArray = sd.getData2dD(sizeX, sizeY);
					}
				}
				/* Add histogram */
				HistProp histProp = new HistProp();
				histProp.name = name;
				histProp.title = title;
				histProp.number = number;
				histProp.sizeX = sizeX;
				histProp.sizeY = sizeY;
				histProp.histDim = histDim;
				histProp.histNumType = histNumType;
				histProp.dataArray = dataArray;
				if (ndgErr != null){
					histProp.errorArray=sdErr.getData1dD(sizeX);
				}
				loadedHistograms.put(name, histProp);
				histNames.add(name);
			}
		} //hist !=null
		return histNames;
	}

	/**
	 * Create a histogram using HistProp
	 * 
	 * @param histProp
	 */
	private void createHistogram(HistProp histProp) {
		String fileName = hdfFile.getFile().getName();
		int index = fileName.indexOf(".hdf");
		if (index > 0){
			fileName = fileName.substring(0, index);
		}
		final String name = histProp.name.trim() + fileIndicator;
		final String title = "File: " + fileName + " - " + histProp.title;
		byte histNumType = histProp.histNumType;
		if (histProp.histDim == 1) {
			final AbstractHist1D histogram;
			if (histNumType == NumberType.INT) {
				histogram=(AbstractHist1D)Histogram.createHistogram((int[]) histProp.dataArray, name, title);
			} else { //DOUBLE
				histogram=(AbstractHist1D)Histogram.createHistogram((double[]) histProp.dataArray, name, title);
			}
			if (histProp.errorArray != null) {
				histogram.setErrors((double[])histProp.errorArray);
			}
		} else { //2d
			if (histNumType == NumberType.INT) {
				Histogram.createHistogram((int[][]) histProp.dataArray, name, title);
			} else {
				Histogram.createHistogram((double[][]) histProp.dataArray, name, title);
			}
		}
	}

	/**
	 * Create file name indicator, appended to file name
	 *  
	 */
	private void createFileIndicator() {
		fileIndicator = " (" + txtFileInd.getText() + ")";
	}

	/**
	 * Class to hold histogram properties while we decide if we should load them
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

		Object dataArray; //generic data array
		Object errorArray;

		public HistProp() {
		}

	}
}