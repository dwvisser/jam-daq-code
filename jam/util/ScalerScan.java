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
import jam.io.hdf.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ScalerScan
	extends JFrame
	implements JamHDFFields, ActionListener {
	static HDFile in;
	static final String sep = ",";

	Container container;
	JTextField runName, path, first, last, out;
	JLabel JLstatus;

	/**
	 * Constructor.
	 */
	public ScalerScan() {
		super("Scaler Values Scan");
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}
		});
		container = getContentPane();
		Box framebox = Box.createVerticalBox();
		JPanel JPrun = new JPanel();
		JPrun.setLayout(new BoxLayout(JPrun, BoxLayout.X_AXIS));
		runName = new JTextField(12);
		JLabel runlabel = new JLabel("Run Name");
		runlabel.setLabelFor(runName);
		JPrun.add(runlabel);
		JPrun.add(Box.createRigidArea(new Dimension(10, 0)));
		JPrun.add(runName);
		JPanel JPpath = new JPanel();
		JPpath.setLayout(new BoxLayout(JPpath, BoxLayout.X_AXIS));
		path = new JTextField(30);
		JButton browse = new JButton("Browse");
		browse.setActionCommand("browse");
		browse.addActionListener(this);
		JLabel pathlabel = new JLabel("Path");
		pathlabel.setLabelFor(path);
		JPpath.add(pathlabel);
		JPpath.add(Box.createRigidArea(new Dimension(10, 0)));
		JPpath.add(path);
		JPpath.add(Box.createRigidArea(new Dimension(10, 0)));
		JPpath.add(browse);
		JPanel JPfirst = new JPanel();
		JPfirst.setLayout(new BoxLayout(JPfirst, BoxLayout.X_AXIS));
		first = new JTextField(4);
		JLabel labelfirst = new JLabel("First Run");
		labelfirst.setLabelFor(first);
		JPfirst.add(labelfirst);
		JPfirst.add(Box.createRigidArea(new Dimension(10, 0)));
		JPfirst.add(first);
		JPanel JPlast = new JPanel();
		JPlast.setLayout(new BoxLayout(JPlast, BoxLayout.X_AXIS));
		last = new JTextField(4);
		JLabel labellast = new JLabel("Last Run");
		labellast.setLabelFor(last);
		JPlast.add(labellast);
		JPlast.add(Box.createRigidArea(new Dimension(10, 0)));
		JPlast.add(last);
		JPanel JPout = new JPanel();
		JPout.setLayout(new BoxLayout(JPout, BoxLayout.X_AXIS));
		out = new JTextField(30);
		JButton browse2 = new JButton("Browse");
		browse2.setActionCommand("browse2");
		browse2.addActionListener(this);
		JLabel outlabel = new JLabel("Ouput File");
		outlabel.setLabelFor(out);
		JPout.add(outlabel);
		JPout.add(Box.createRigidArea(new Dimension(10, 0)));
		JPout.add(out);
		JPout.add(Box.createRigidArea(new Dimension(10, 0)));
		JPout.add(browse2);

		JPanel JPstatus = new JPanel(new GridLayout(1, 1));
		JLstatus =
			new JLabel("Welcome to ScalerScan.  Enter the info on your .hdf files and a report destination.");
		JPstatus.add(JLstatus);
		JPanel JPbuttons = new JPanel();
		JPbuttons.setLayout(new BoxLayout(JPbuttons, BoxLayout.X_AXIS));
		JButton apply = new JButton("Execute");
		apply.setActionCommand("apply");
		apply.addActionListener(this);
		JButton cancel = new JButton("Quit");
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		JPbuttons.add(apply);
		JPbuttons.add(cancel);
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
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("apply")) {
			doIt();
		}
		if (e.getActionCommand().equals("cancel")) {
			dispose();
		}
		if (e.getActionCommand().equals("browse")) {
			String temp = getPath();
			if (temp != null)
				path.setText(temp);
		}
		if (e.getActionCommand().equals("browse2")) {
			String temp = getOutFile();
			if (temp != null)
				out.setText(temp);
		}
	}

	public String getPath() {
		JFileChooser chooser = new JFileChooser();
		//chooser.setFileFilter(new HDFileFilter(true));
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File temp = chooser.getSelectedFile();
			String temp2 = temp.getPath();
			return temp2.substring(0, temp2.lastIndexOf(temp.getName()));
		} else {
			return null;
		}
	}

	public String getOutFile() {
		JFileChooser chooser = new JFileChooser();
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile().getPath();
		} else {
			return null;
		}
	}

	public void doIt() {
		try {
			JLstatus.setText("starting");
			File outFile = new File(out.getText().trim());
			if (outFile.createNewFile() && outFile.canWrite()) {
				FileWriter writer = new FileWriter(outFile);
				int firstRun = Integer.parseInt(first.getText().trim());
				int lastRun = Integer.parseInt(last.getText().trim());
				File f_path = new File(path.getText().trim());
				if (f_path.exists() && f_path.isDirectory()) {
					for (int i = firstRun; i <= lastRun; i++) {
						System.out.println(i);
						String runText = runName.getText().trim();
						String filename = runText + i + ".hdf";
						File infile = new File(f_path, filename);
						if (infile.exists()) {
							JLstatus.setText("Processing " + infile.getPath());
							in = new HDFile(infile, "r");
							in.seek(0);
							in.readObjects();
							//reads file into set of DataObject's, sets their internal variables
							String temp = "Run";
							if (i == firstRun) {
								String[] names = getScalerNames();
								for (int j = 0; j < names.length; j++)
									temp = temp + sep + names[j];
								writer.write(temp + "\n");
							}
							temp = i + "";
							int[] values = getScalerValues();
							for (int j = 0; j < values.length; j++) {
								temp = temp + sep + values[j];
							}
							writer.write(temp + "\n");
						} else {
							System.err.println(
								infile.getPath()
									+ " does not exist.  Skipping.");
							JLstatus.setText(
								infile.getName()
									+ " does not exist.  Skipping.");
						}
					}
					writer.flush();
					writer.close();
					JLstatus.setText("Done.  Results are in "+outFile.getPath());
				} else {
					JLstatus.setText(
						f_path.getPath()
							+ " either does not exist or is not a directory. Try again.");
				}
			} else {
				JLstatus.setText(
					outFile.getPath()
						+ " either already exists or is not a writable output file.");
			}
		} catch (IOException e) {
			JLstatus.setText(e.getMessage());
		} catch (HDFException e) {
			JLstatus.setText(e.getMessage());
		}
	}

	static private String[] getScalerNames() {
		VdataDescription VH;
		Vdata VS;
		int i, numScalers;
		String[] sname;
		String temp1, temp2;
		int tmp;

		//VH=null;
		VH =
			VdataDescription.ofName(
				in.ofType(DataObject.DFTAG_VH),
				SCALER_SECTION_NAME);
		//only the "scalers" VH (only one element) in the file
		if (VH != null) {
			VS = (Vdata) (in.getObject(DataObject.DFTAG_VS, VH.getRef()));
			//corresponding VS
			numScalers = VH.getNumRows();
			sname = new String[numScalers];
			for (i = 0; i < numScalers; i++) {
				sname[i] = VS.getString(i, 1);
				sname[i] = sname[i].trim();
				while (sname[i].indexOf(' ') != -1) {
					tmp = sname[i].indexOf(' ');
					temp1 = sname[i].substring(0, tmp);
					temp2 = sname[i].substring(tmp + 1);
					sname[i] = temp1 + temp2;
					System.out.println(sname[i]);
				}
			}
			return sname;
		} else {
			System.out.println("No Scalers section in HDF file.");
			return null;
		}
	}

	static private int[] getScalerValues() {
		VdataDescription VH;
		Vdata VS;
		int i, numScalers;
		int[] values;

		//VH=null;
		VH =
			VdataDescription.ofName(
				in.ofType(DataObject.DFTAG_VH),
				SCALER_SECTION_NAME);
		//only the "scalers" VH (only one element) in the file
		if (VH != null) {
			VS = (Vdata) (in.getObject(DataObject.DFTAG_VS, VH.getRef()));
			//corresponding VS
			numScalers = VH.getNumRows();
			values = new int[numScalers];
			for (i = 0; i < numScalers; i++) {
				values[i] = VS.getInteger(i, 2).intValue();
			}
			return values;
		} else {
			System.out.println("No Scalers section in HDF file.");
			return null;
		}
	}

	public static void main(String args[]) throws Exception {
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println(e);
		}
		new ScalerScan();
	}
}
