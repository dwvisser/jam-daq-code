/***************************************************************
 * Nuclear Simulation Java Class Libraries
 * Copyright (C) 2003 Yale University
 * 
 * Original Developer
 *     Dale Visser (dale@visser.name)
 * 
 * OSI Certified Open Source Software
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the University of Illinois/NCSA 
 * Open Source License.
 * 
 * This program is distributed in the hope that it will be 
 * useful, but without any warranty; without even the implied 
 * warranty of merchantability or fitness for a particular 
 * purpose. See the University of Illinois/NCSA Open Source 
 * License for more details.
 * 
 * You should have received a copy of the University of 
 * Illinois/NCSA Open Source License along with this program; if 
 * not, see http://www.opensource.org/
 **************************************************************/
package jam.util;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.io.hdf.DataObject;
import jam.io.hdf.HDFException;
import jam.io.hdf.HDFile;
import jam.io.hdf.JamHDFFields;
import jam.io.hdf.Vdata;
import jam.io.hdf.VdataDescription;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.border.EmptyBorder;

public class ScalerScan
	extends JDialog
	implements JamHDFFields, ActionListener {
	
	private static final char TAB = '\t';
	
	private final JTextField txtFirst, txtLast;
	private ProgressMonitor pBstatus;
	private final MessageHandler console;
	private final ScanAction sa;
	private final Frame frame;
	private final JButton bOK, bApply, bCancel;
	
	private File pathToRuns=new File(JamProperties.getPropString(
	JamProperties.HIST_PATH));
	private final JTextField txtPath;
	private final JTextField txtRunName;
	private final static JamStatus status=JamStatus.instance();

	/**
	 * Constructor.
	 */
	public ScalerScan() {
		super(status.getFrame(), "HDF Scaler Values Scan", false);
		frame = status.getFrame();
		console = status.getMessageHandler();
		sa = new ScanAction();
		final Container container = getContentPane();
		container.setLayout(new BorderLayout(10,5));
		
		final JPanel pLabels = new JPanel(new GridLayout(0,1,0,5));
		pLabels.setBorder(new EmptyBorder(10,10,0,0));
		container.add(pLabels, BorderLayout.WEST);
	
		final JLabel runlabel = new JLabel("Experiment Name", JLabel.RIGHT);
		pLabels.add(runlabel);
		final JLabel pathlabel = new JLabel("Path", JLabel.RIGHT);
		pLabels.add(pathlabel);
		final JLabel labelfirst = new JLabel("First Run", JLabel.RIGHT);
		pLabels.add(labelfirst);		
		final JLabel labellast = new JLabel("Last Run", JLabel.RIGHT);
		pLabels.add(labellast);						
						
		final JPanel pEntries = new JPanel(new GridLayout(0,1,5,5));		
		pEntries.setBorder(new EmptyBorder(10,0,0,5));		
		container.add(pEntries, BorderLayout.CENTER);		
		
		final JPanel pRunName = new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
		pEntries.add(pRunName);				
		txtRunName = new JTextField(10);
		txtRunName.setText(JamProperties.getPropString(JamProperties.EXP_NAME));
		pRunName.add(txtRunName);
				
		final JPanel pPath = new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
		pEntries.add(pPath);
				
		txtPath = new JTextField(30);
		txtPath.setText(pathToRuns.getAbsolutePath());
		pPath.add(txtPath);		
			
		final JButton browse = new JButton("Browse...");
		browse.setActionCommand("browse");
		browse.addActionListener(this);
		pPath.add(browse);
				
		final JPanel pFirst = new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
		pEntries.add(pFirst);
		txtFirst = new JTextField(4);
		pFirst.add(txtFirst);
		 
		final JPanel pLast = new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
		pEntries.add(pLast);
		txtLast = new JTextField(4);
		pLast.add(txtLast);
		
		final JPanel pLower = new JPanel();
		container.add(pLower, BorderLayout.SOUTH);
		final JPanel pButtons = new JPanel(new GridLayout(1,0,5,5));
		pLower.add(pButtons);		
		bOK = new JButton(OK);
		bOK.setActionCommand(OK);
		bOK.addActionListener(this);
		pButtons.add(bOK);
		bApply = new JButton(APPLY);
		bApply.setActionCommand(APPLY);
		bApply.addActionListener(this);
		pButtons.add(bApply);		
		bCancel = new JButton(CANCEL);
		bCancel.setActionCommand(CANCEL);
		bCancel.addActionListener(this);
		pButtons.add(bCancel);
		
		setResizable(false);				
		pack();
		
	}

	private static final String OK = "OK";
	private static final String CANCEL = "Cancel";
	private static final String APPLY = "Apply";
	
	private final void setButtonsEnable(boolean b){
		bOK.setEnabled(b);
		bApply.setEnabled(b);
		bCancel.setEnabled(b);
	}

	public void actionPerformed(ActionEvent e) {
		final String command = e.getActionCommand();
		final boolean ok = OK.equals(command);
		final boolean apply = ok || APPLY.equals(command);
		final boolean cancel = ok || CANCEL.equals(command);
		if (apply) {
			setButtonsEnable(false);
			final Runnable r=new Runnable(){
				public void run(){
					doIt();
					setButtonsEnable(true);
				}
			};
			final Thread t=new Thread(r);
			t.start();
		}
		if (cancel) {
			dispose();
		}
		if (e.getActionCommand().equals("browse")) {
			final File temp = getFile(true);
			if (temp != null) {
				txtPath.setText(temp.getAbsolutePath());
				pathToRuns=temp;
			}
		}
	}

	/**
	 * Browse for a file or directory.
	 * 
	 * @param dir select directories if true, files if false
	 * @return ref to file of interest, null if none selected
	 */
	public File getFile(boolean dir) {
		final JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(
			dir ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
		final boolean approved =
			chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION;
		return approved ? chooser.getSelectedFile() : null;
	}

	public void doIt() {
		final char cr = '\n';
		final StringBuffer outText = new StringBuffer();
		try {
			int firstRun = Integer.parseInt(txtFirst.getText().trim());
			int lastRun = Integer.parseInt(txtLast.getText().trim());
			pBstatus=new ProgressMonitor(frame, "Scanning HDF Files for scaler values", 
			"Initializing", firstRun, lastRun);
			if (pathToRuns.exists() && pathToRuns.isDirectory()) {
				for (int i = firstRun; i <= lastRun && !pBstatus.isCanceled(); i++) {
					String runText = txtRunName.getText().trim();
					String filename = runText + i + ".hdf";
					final File infile = new File(pathToRuns, filename);
					if (infile.exists()) {
						updateProgressBar("Processing " + infile.getName(),i);
						final HDFile in = new HDFile(infile, "r");
						in.seek(0);
						in.readObjects();
						if (i == firstRun) {
							outText.append("Run");
							String[] names = getScalerNames(in);
							for (int j = 0; j < names.length; j++) {
								outText.append(TAB).append(names[j]);
							}
							outText.append(cr);
						}
						outText.append(i);
						int[] values = getScalerValues(in);
						for (int j = 0; j < values.length; j++) {
							outText.append(TAB).append(values[j]);
						}
						outText.append(cr);
					} else {
						console.warningOutln(
							infile.getPath() + " does not exist.  Skipping.");
					}
				}
				if (!pBstatus.isCanceled()){
					final String title =
						txtRunName.getText()
							+ ", runs "
							+ txtFirst.getText()
							+ " to "
							+ txtLast.getText();
					new TextDisplayDialog(frame, title, false, outText.toString());
				}
				updateProgressBar("Done",lastRun);
			} 
		} catch (IOException e) {
			console.errorOutln(e.getMessage());
		} catch (HDFException e) {
			console.errorOutln(e.getMessage());
		}
	}

	private String[] getScalerNames(HDFile in) {
		String[] sname = null;
		final VdataDescription VH =
			VdataDescription.ofName(
				in.ofType(DataObject.DFTAG_VH),
				SCALER_SECTION_NAME);
		//only the "scalers" VH (only one element) in the file
		if (VH != null) {
			final Vdata VS =
				(Vdata) (in.getObject(DataObject.DFTAG_VS, VH.getRef()));
			final int numScalers = VH.getNumRows();
			sname = new String[numScalers];
			for (int i = 0; i < numScalers; i++) {
				sname[i] = VS.getString(i, 1);
				sname[i] = sname[i].trim();
				final char sp = ' ';
				while (sname[i].indexOf(sp) != -1) {
					final int tmp = sname[i].indexOf(sp);
					final String temp1 = sname[i].substring(0, tmp);
					final String temp2 = sname[i].substring(tmp + 1);
					sname[i] = temp1 + temp2;
				}
			}
		} else {
			console.warningOutln("No Scalers section in HDF file.");
		}
		return sname;
	}

	private int[] getScalerValues(HDFile in) {
		int[] values = null;
		final VdataDescription VH =
			VdataDescription.ofName(
				in.ofType(DataObject.DFTAG_VH),
				SCALER_SECTION_NAME);
		//only the "scalers" VH (only one element) in the file
		if (VH != null) {
			final Vdata VS =
				(Vdata) (in.getObject(DataObject.DFTAG_VS, VH.getRef()));
			//corresponding VS
			final int numScalers = VH.getNumRows();
			values = new int[numScalers];
			for (int i = 0; i < numScalers; i++) {
				values[i] = VS.getInteger(i, 2).intValue();
			}
		} else {
			console.warningOutln("No Scalers section in HDF file.");
		}
		return values;
	}

	public class ScanAction extends AbstractAction {
		public ScanAction() {
			super("Scan HDF files for scaler values...");
		}

		public void actionPerformed(ActionEvent e) {
			show();
		}
	}
	
	private void updateProgressBar(final String text, final int value){
		pBstatus.setNote(text);
		pBstatus.setProgress(value);
	}

	public AbstractAction getAction() {
		return sa;
	}
}
