/***************************************************************
 * Nuclear Simulation Java Class Libraries
 * Copyright (C) 2003 Yale University
 * 
 * Original Developer
 *     Dale Visser (dwvisser@users.sourceforge.net)
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

import static javax.swing.SwingConstants.RIGHT;
import injection.GuiceInjector;
import jam.global.JamProperties;
import jam.global.PropertyKeys;
import jam.io.hdf.HDFException;
import jam.io.hdf.ProgressUpdater;
import jam.io.hdf.ScanForScalers;
import jam.ui.PanelOKApplyCancelButtons;
import jam.util.TextDisplayDialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.border.EmptyBorder;

import com.google.inject.Inject;

/**
 * Scans HDF files for scaler values.
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale W Visser</a>
 */
public final class ScalerScan implements ProgressUpdater {

    private static final Logger LOGGER = Logger.getLogger(ScalerScan.class
            .getPackage().getName());

    private final transient JTextField txtFirst, txtLast;

    private transient ProgressMonitor pBstatus;

    private final transient JFrame frame = GuiceInjector.getObjectInstance(JFrame.class);

    private final transient JDialog dialog;

    private transient File pathToRuns = new File(JamProperties// NOPMD
            .getPropString(PropertyKeys.HIST_PATH));

    private final transient JTextField txtPath;

    private final transient JTextField txtRunName;

    private final transient PanelOKApplyCancelButtons buttons;

    /**
     * Constructor.
     * @param executor
     *            executor service to allocate threads to tasks
     */
    @Inject
    public ScalerScan(final ExecutorService executor) {
        super();
        dialog = new JDialog(frame, "HDF Scaler Values Scan", false);
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

        final JPanel pRunName = new JPanel(new FlowLayout(FlowLayout.LEFT, 5,
                0));
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
                    if (temp.getPath().length() > 0) {
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
                        executor.submit(runnable);
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
     * @param dir
     *            select directories if true, files if false
     * @return reference to file of interest, null if none selected
     */
    private File getFile(final boolean dir) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(dir ? JFileChooser.DIRECTORIES_ONLY
                : JFileChooser.FILES_ONLY);
        final boolean approved = chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION;
        return approved ? chooser.getSelectedFile() : new File("");
    }

    private void doIt() {
        final StringBuffer outText = new StringBuffer();
        try {
            final int firstRun = Integer.parseInt(txtFirst.getText().trim());
            final int lastRun = Integer.parseInt(txtLast.getText().trim());
            pBstatus = new ProgressMonitor(frame,
                    "Scanning HDF Files for scaler values", "Initializing",
                    firstRun, lastRun);
            if (pathToRuns.exists() && pathToRuns.isDirectory()) {
                final ScanForScalers scanner = new ScanForScalers(this);
                for (int i = firstRun; i <= lastRun && !pBstatus.isCanceled(); i++) {
                    final String runText = txtRunName.getText().trim();
                    final String filename = runText + i + ".hdf";
                    scanFileIfItExists(scanner, firstRun, i, filename, outText);
                }
                if (!pBstatus.isCanceled()) {
                    final String title = txtRunName.getText() + ", runs "
                            + txtFirst.getText() + " to " + txtLast.getText();
                    new TextDisplayDialog(frame, title, outText.toString());
                }
                updateProgressBar("Done", lastRun);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (HDFException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void scanFileIfItExists(final ScanForScalers scanner,
            final int firstRun, final int run, final String filename,
            final StringBuffer outText) throws IOException, HDFException {
        final File infile = new File(pathToRuns, filename);
        if (infile.exists()) {
            final char carriage = '\n';
            scanner.processFile(carriage, outText, firstRun, run, infile);
        } else {
            LOGGER.warning(infile.getPath() + " does not exist.  Skipping.");
        }
    }

    /**
     * Update the progress bar.
     * @param text
     *            note text
     * @param value
     *            value
     */
    public void updateProgressBar(final String text, final int value) {
        pBstatus.setNote(text);
        pBstatus.setProgress(value);
    }

    /**
     * Returns the dialog.
     * @return the dialog
     */
    public JDialog getDialog() {
        return dialog;
    }

}// NOPMD
