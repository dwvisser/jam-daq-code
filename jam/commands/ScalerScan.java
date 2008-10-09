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
package jam.commands;

import static jam.io.hdf.JamFileFields.SCALER_SECT;
import static javax.swing.SwingConstants.RIGHT;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.global.PropertyKeys;
import jam.io.hdf.AbstractData;
import jam.io.hdf.HDFException;
import jam.io.hdf.HDFile;
import jam.io.hdf.VData;
import jam.io.hdf.VDataDescription;
import jam.ui.PanelOKApplyCancelButtons;
import jam.util.TextDisplayDialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.border.EmptyBorder;

/**
 * Scans HDF files for scaler values.
 * 
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 */
public final class ScalerScan {

	private static final Logger LOGGER = Logger.getLogger(ScalerScan.class
			.getPackage().getName());

	private static final char TAB = '\t';

	private transient final JTextField txtFirst, txtLast;

	private transient ProgressMonitor pBstatus;

	private transient final Frame frame;

	private transient final JDialog dialog;

	private transient File pathToRuns = new File(JamProperties// NOPMD
			.getPropString(PropertyKeys.HIST_PATH));

	private transient final JTextField txtPath;

	private transient final JTextField txtRunName;

	private final static JamStatus STATUS = JamStatus.getSingletonInstance();

	private transient final PanelOKApplyCancelButtons buttons;

	/**
	 * Constructor.
	 */
	public ScalerScan() {
		super();
		dialog = new JDialog(STATUS.getFrame(), "HDF Scaler Values Scan", false);
		frame = STATUS.getFrame();
		final Container container = dialog.getContentPane();
		container.setLayout(new BorderLayout(10, 5));

		final JPanel pLabels = new JPanel(new GridLayout(0, 1, 0, 5));
		pLabels.setBorder(new EmptyBorder(10, 10, 0, 0));
		container.add(pLabels, BorderLayout.WEST);

		final JLabel runlabel = new JLabel("Experiment Name", RIGHT);
		pLabels.add(runlabel);
		final JLabel pathlabel = new JLabel("Path", RIGHT);
		pLabels.add(pathlabel);
		final JLabel labelfirst = new JLabel("First Run", RIGHT);
		pLabels.add(labelfirst);
		final JLabel labellast = new JLabel("Last Run", RIGHT);
		pLabels.add(labellast);

		final JPanel pEntries = new JPanel(new GridLayout(0, 1, 5, 5));
		pEntries.setBorder(new EmptyBorder(10, 0, 0, 5));
		container.add(pEntries, BorderLayout.CENTER);

		final JPanel pRunName = new JPanel(
				new FlowLayout(FlowLayout.LEFT, 5, 0));
		pEntries.add(pRunName);
		txtRunName = new JTextField(10);
		txtRunName.setText(JamProperties.getPropString(PropertyKeys.EXP_NAME));
		pRunName.add(txtRunName);

		final JPanel pPath = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		pEntries.add(pPath);

		txtPath = new JTextField(30);
		txtPath.setText(pathToRuns.getAbsolutePath());
		pPath.add(txtPath);

		final JButton browse = new JButton("Browse...");
		browse.setActionCommand("browse");
		browse.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				final String command = event.getActionCommand();
				if ("browse".equals(command)) {
					final File temp = getFile(true);
					if (temp != null) {
						txtPath.setText(temp.getAbsolutePath());
						pathToRuns = temp;
					}
				}
			}
		});
		pPath.add(browse);

		final JPanel pFirst = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		pEntries.add(pFirst);
		txtFirst = new JTextField(4);
		pFirst.add(txtFirst);

		final JPanel pLast = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		pEntries.add(pLast);
		txtLast = new JTextField(4);
		pLast.add(txtLast);
		buttons = new PanelOKApplyCancelButtons(
				new PanelOKApplyCancelButtons.AbstractListener(dialog) {
					public void apply() {
						setButtonsEnable(false);
						final Runnable runnable = new Runnable() {
							public void run() {
								doIt();
								setButtonsEnable(true);
							}
						};
						final Thread thread = new Thread(runnable);
						thread.start();
					}
				});
		container.add(buttons.getComponent(), BorderLayout.SOUTH);
		dialog.setResizable(false);
		dialog.pack();
	}

	private void setButtonsEnable(final boolean enable) {
		buttons.setButtonsEnabled(enable, enable, enable);
	}

	/**
	 * Browse for a file or directory.
	 * 
	 * @param dir
	 *            select directories if true, files if false
	 * @return ref to file of interest, null if none selected
	 */
	private File getFile(final boolean dir) {
		final JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(dir ? JFileChooser.DIRECTORIES_ONLY
				: JFileChooser.FILES_ONLY);
		final boolean approved = chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION;
		File rval = null;
		if (approved) {
			rval = chooser.getSelectedFile();
		}
		return rval;
	}

	private void doIt() {
		final char carriage = '\n';
		final StringBuffer outText = new StringBuffer();
		try {
			final int firstRun = Integer.parseInt(txtFirst.getText().trim());
			final int lastRun = Integer.parseInt(txtLast.getText().trim());
			pBstatus = new ProgressMonitor(frame,
					"Scanning HDF Files for scaler values", "Initializing",
					firstRun, lastRun);
			if (pathToRuns.exists() && pathToRuns.isDirectory()) {
				for (int i = firstRun; i <= lastRun && !pBstatus.isCanceled(); i++) {
					final String runText = txtRunName.getText().trim();
					final String filename = runText + i + ".hdf";
					final File infile = new File(pathToRuns, filename);// NOPMD
					if (infile.exists()) {
						processFile(carriage, outText, firstRun, i, infile);
					} else {
						LOGGER.warning(infile.getPath()
								+ " does not exist.  Skipping.");
					}
				}
				if (!pBstatus.isCanceled()) {
					final String title = txtRunName.getText() + ", runs "
							+ txtFirst.getText() + " to " + txtLast.getText();
					new TextDisplayDialog(frame, title, false, outText
							.toString());
				}
				updateProgressBar("Done", lastRun);
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (HDFException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	/**
	 * @param carriage
	 * @param outText
	 * @param firstRun
	 * @param index
	 * @param infile
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws HDFException
	 */
	private void processFile(final char carriage, final StringBuffer outText,
			final int firstRun, final int index, final File infile)
			throws FileNotFoundException, IOException, HDFException {
		updateProgressBar("Processing " + infile.getName(), index);
		final HDFile inHDF = new HDFile(infile, "r");// NOPMD
		inHDF.seek(0);
		inHDF.readFile();
		if (index == firstRun) {
			writeHeaderLine(carriage, outText);
		}
		outText.append(index);
		final int[] values = getScalerValues();
		for (int j = 0; j < values.length; j++) {
			outText.append(TAB).append(values[j]);
		}
		outText.append(carriage);
	}

	/**
	 * @param carriage
	 * @param outText
	 */
	private void writeHeaderLine(final char carriage, final StringBuffer outText) {
		outText.append("Run");
		final String[] names = getScalerNames();
		for (int j = 0; j < names.length; j++) {
			outText.append(TAB).append(names[j]);
		}
		outText.append(carriage);
	}

	private String[] getScalerNames() {
		String[] sname = null;
		final VDataDescription dataDesc = VDataDescription.ofName(AbstractData
				.ofType(VDataDescription.class), SCALER_SECT);
		// only the "scalers" VH (only one element) in the file
		if (dataDesc == null) {
			LOGGER.warning("No Scalers section in HDF file.");
		} else {
			final VData data = AbstractData.getObject(VData.class, dataDesc
					.getRef());
			final int numScalers = dataDesc.getNumRows();
			sname = new String[numScalers];
			for (int i = 0; i < numScalers; i++) {
				sname[i] = data.getString(i, 1);
				sname[i] = sname[i].trim();
				final char space = ' ';
				while (sname[i].indexOf(space) != -1) {
					final int tmp = sname[i].indexOf(space);
					final String temp1 = sname[i].substring(0, tmp);
					final String temp2 = sname[i].substring(tmp + 1);
					sname[i] = temp1 + temp2;
				}
			}
		}
		return sname;
	}

	private int[] getScalerValues() {
		int[] values = null;
		final VDataDescription dataDesc = VDataDescription.ofName(AbstractData
				.ofType(VDataDescription.class), SCALER_SECT);
		// only the "scalers" VH (only one element) in the file
		if (dataDesc == null) {
			LOGGER.warning("No Scalers section in HDF file.");
		} else {
			final VData data = AbstractData.getObject(VData.class, dataDesc
					.getRef());
			// corresponding VS
			final int numScalers = dataDesc.getNumRows();
			values = new int[numScalers];
			for (int i = 0; i < numScalers; i++) {
				values[i] = data.getInteger(i, 2);
			}
		}
		return values;
	}

	private void updateProgressBar(final String text, final int value) {
		pBstatus.setNote(text);
		pBstatus.setProgress(value);
	}

	/**
	 * Returns the dialog.
	 * 
	 * @return the dialog
	 */
	public JDialog getDialog() {
		return dialog;
	}

}// NOPMD
