package jam.io.control;

import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.MessageHandler;
import jam.global.JamStatus;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;

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
public final class OpenSelectedHistogram {

	//UI components
	private JDialog dialog;

	private JTextField txtFile;

	private JList histList;
	
	private DefaultListModel histListModel;

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
	 * @param msgHandler where to print messages
	 */
	public OpenSelectedHistogram(Frame f, MessageHandler msgHandler) {
		
		frame = f;
		this.msgHandler = msgHandler;
		hdfio = new HDFIO(frame, msgHandler);
		
		broadcaster= Broadcaster.getSingletonInstance();
		dialog = new JDialog(f, "Open Selected Histograms", false);
		dialog.setLocation(f.getLocation().x + 50, f.getLocation().y + 50);
		final Container container = dialog.getContentPane();
		container.setLayout(new BorderLayout(10, 10));
		final JPanel pFileInd = new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JLabel filelabel = new JLabel("File: ", JLabel.RIGHT);		
		pFileInd.add(filelabel);
		txtFile = new JTextField(20);
		txtFile.setEditable(false);
		pFileInd.add(txtFile);
		container.add(pFileInd, BorderLayout.NORTH);
		/* Selection list */
		histListModel = new DefaultListModel();
		histList = new JList(histListModel);
		histList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		histList.setSelectedIndex(0);
		histList.setVisibleRowCount(10);
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
		String firstHistName=loadHistograms();
		broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		Histogram hist = Histogram.getHistogram(firstHistName);
		if (hist!=null) {
			JamStatus.getSingletonInstance().setCurrentHistogram(hist);
			broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, hist);
		}
	}

	/**
	 * Cancel Button
	 *  
	 */
	private void doCancel() {
		/* Clear memory */
		histListModel.clear();
		dialog.dispose();
	}

	/**
	 * Entry points show the dialog if a file is chosen to open
	 *  
	 */
	public void open() {
		if (openFile()) {
        	loadHistNames(fileOpen);					
        	txtFile.setText(fileOpen.getAbsolutePath());				
        	dialog.setVisible(true);
        }
	}
	
	/* non-javadoc:
	 * Load name of histograms from the selected file.
	 */
	private void loadHistNames(File fileSelect) {	
		/* Read in histogram names attributes */		
		final List histAttributes= hdfio.readHistogramAttributes(fileSelect);
		final Iterator iter = histAttributes.iterator(); 
		while (iter.hasNext()) {
			final HDFIO.HistogramAttributes histAtt= (HDFIO.HistogramAttributes)iter.next();
			histListModel.addElement(histAtt.getName());				
		}
		histList.clearSelection();
	}


	/* non-javadoc:
	 * Load the histograms in the selected list.
	 */
	private String loadHistograms() {
		final Object[] selected = histList.getSelectedValues();
		//No histograms selected
		if (selected.length == 0) {
			msgHandler.errorOutln("No histograms selected");
			return null;
		}
		/* Put selected histograms into a list */
		final List histogramNamesSelected =new ArrayList();
		String firstHistName=null;
		for (int i=0; i<selected.length;i++) {
			histogramNamesSelected.add(selected[i]);
			if (i==0) {
				firstHistName =(String)selected[i];
			}
		}
		/* Read in histograms */
		hdfio.readFile(FileOpenMode.OPEN_ADDITIONAL, fileOpen, null, histogramNamesSelected);
		return firstHistName;
	}

	/**
	 * Read in an unspecified file by opening up a dialog box.
	 * 
	 * @return <code>true</code> if successful
	 */
	private boolean openFile() {
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

}