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
import jam.io.hdf.DataObject;
import jam.io.hdf.HDFException;
import jam.io.hdf.HDFile;
import jam.io.hdf.JamHDFFields;
import jam.io.hdf.Vdata;
import jam.io.hdf.VdataDescription;
import jam.global.MessageHandler;
import jam.global.JamProperties;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ScalerScan
	extends JDialog
	implements JamHDFFields, ActionListener {
	//private HDFile in;
	
	private static final char TAB = '\t';
	
	private final JTextField first, last;
	private final JProgressBar JLstatus;
	private final MessageHandler console;
	private final ScanAction sa;
	private final Frame frame;
	private final JButton bOK, bApply, bCancel;
	
	private File pathToRuns=new File(JamProperties.getPropString(
	JamProperties.HIST_PATH));
	private final JTextField path=new JTextField(
	pathToRuns.getAbsolutePath());
	private final JTextField runName= new JTextField(
	JamProperties.getPropString(JamProperties.EXP_NAME));

	/**
	 * Constructor.
	 */
	public ScalerScan(Frame f, MessageHandler mh) {
		super(f, "HDF Scaler Values Scan", false);
		frame = f;
		console = mh;
		sa = new ScanAction();
		final Container container = getContentPane();
		final Box framebox = Box.createVerticalBox();
		final JPanel JPrun = new JPanel();
		JPrun.setLayout(new BoxLayout(JPrun, BoxLayout.X_AXIS));
		final JLabel runlabel = new JLabel("Experiment Name");
		runlabel.setLabelFor(runName);
		JPrun.add(runlabel);
		JPrun.add(Box.createRigidArea(new Dimension(10, 0)));
		JPrun.add(runName);
		final JPanel JPpath = new JPanel();
		JPpath.setLayout(new BoxLayout(JPpath, BoxLayout.X_AXIS));
		final JButton browse = new JButton("Browse");
		browse.setActionCommand("browse");
		browse.addActionListener(this);
		final JLabel pathlabel = new JLabel("Path");
		pathlabel.setLabelFor(path);
		JPpath.add(pathlabel);
		JPpath.add(Box.createRigidArea(new Dimension(10, 0)));
		JPpath.add(path);
		JPpath.add(Box.createRigidArea(new Dimension(10, 0)));
		JPpath.add(browse);
		final JPanel JPfirst = new JPanel();
		JPfirst.setLayout(new BoxLayout(JPfirst, BoxLayout.X_AXIS));
		first = new JTextField(4);
		final JLabel labelfirst = new JLabel("First Run");
		labelfirst.setLabelFor(first);
		JPfirst.add(labelfirst);
		JPfirst.add(Box.createRigidArea(new Dimension(10, 0)));
		JPfirst.add(first);
		final JPanel JPlast = new JPanel();
		JPlast.setLayout(new BoxLayout(JPlast, BoxLayout.X_AXIS));
		last = new JTextField(4);
		final JLabel labellast = new JLabel("Last Run");
		labellast.setLabelFor(last);
		JPlast.add(labellast);
		JPlast.add(Box.createRigidArea(new Dimension(10, 0)));
		JPlast.add(last);
		JPanel JPout = new JPanel();
		JPout.setLayout(new BoxLayout(JPout, BoxLayout.X_AXIS));
		JPanel JPstatus = new JPanel(new GridLayout(1, 1));
		JLstatus =
			new JProgressBar(JProgressBar.HORIZONTAL);
		JLstatus.setString("Welcome to ScalerScan.");
		JLstatus.setStringPainted(true);
		JPstatus.add(JLstatus);
		final JPanel JPbuttons = new JPanel();
		JPbuttons.setLayout(new BoxLayout(JPbuttons, BoxLayout.X_AXIS));
		bOK = new JButton(OK);
		bOK.setActionCommand(OK);
		bOK.addActionListener(this);
		bApply = new JButton(APPLY);
		bApply.setActionCommand(APPLY);
		bApply.addActionListener(this);
		bCancel = new JButton(CANCEL);
		bCancel.setActionCommand(CANCEL);
		bCancel.addActionListener(this);
		JPbuttons.add(bOK);
		JPbuttons.add(bApply);
		JPbuttons.add(bCancel);
		framebox.add(JPrun);
		framebox.add(JPpath);
		framebox.add(JPfirst);
		framebox.add(JPlast);
		framebox.add(JPout);
		framebox.add(JPstatus);
		framebox.add(JPbuttons);
		container.add(framebox);
		pack();
		setResizable(false);
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
				path.setText(temp.getAbsolutePath());
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
			JLstatus.setString("starting");
			int firstRun = Integer.parseInt(first.getText().trim());
			int lastRun = Integer.parseInt(last.getText().trim());
			JLstatus.setMinimum(firstRun);
			JLstatus.setMaximum(lastRun);
			if (pathToRuns.exists() && pathToRuns.isDirectory()) {
				for (int i = firstRun; i <= lastRun; i++) {
					String runText = runName.getText().trim();
					String filename = runText + i + ".hdf";
					final File infile = new File(pathToRuns, filename);
					if (infile.exists()) {
						updateProgressBar("Processing " + infile.getName(),i);
						final HDFile in = new HDFile(infile, "r");
						in.seek(0);
						in.readObjects();
						//reads file into set of DataObject's, sets their internal variables
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
				updateProgressBar("Done",lastRun);
				final String title =
					runName.getText()
						+ ", runs "
						+ first.getText()
						+ " to "
						+ last.getText();
				new TextDisplayDialog(frame, title, false, outText.toString());
			} else {
				JLstatus.setString(
					pathToRuns.getPath()
						+ " either does not exist or is not a directory. Try again.");
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
		final Runnable r=new Runnable(){
			public void run(){
				JLstatus.setValue(value);
				JLstatus.setString(text);
			}
		};
		try{
			SwingUtilities.invokeAndWait(r);
		} catch (Exception e){
			console.errorOutln(e.getMessage());
		}
	}

	public AbstractAction getAction() {
		return sa;
	}
}
