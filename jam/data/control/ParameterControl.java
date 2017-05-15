package jam.data.control;

import com.google.inject.Inject;
import jam.data.DataParameter;
import jam.global.Broadcaster;
import jam.ui.ExtensionFileFilter;
import jam.util.FileUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Sets and displays the Parameters (data.Parameters.class) used for sorting
 * 
 * @version 0.5 October 98
 * @author Ken Swartz
 * 
 */
public final class ParameterControl extends AbstractControl {

	private static final int BORDER_HEIGHT = 5;

	private static final String FILE_EXTENSION = "par";

	private transient String invalidNames;

	private transient JLabel[] labelParam;

	private transient File lastFile; // last file referred to in a JFileChooser

	private transient final JPanel pCenter;

	// widgets for each parameter

	private transient JTextField[] textParam;

	private transient final FileUtilities fileUtilities;

	/**
	 * Constructs a new parameter dialog.
	 * 
	 * @param frame
	 *            application frame
	 * @param broadcaster
	 *            broadcasts messages
	 * @param fileUtilities
	 *            the file utility object
	 */
	@Inject
	public ParameterControl(final Frame frame, final Broadcaster broadcaster,
			final FileUtilities fileUtilities) {
		super(frame, "Sort Parameters", true, broadcaster);
		this.fileUtilities = fileUtilities;
		/* dialog box to display Parameters */
		setResizable(true);
		setLocation(20, 50);
		final Container cddisp = getContentPane();
		cddisp.setLayout(new java.awt.BorderLayout());
		/* Central Panel */
		pCenter = new JPanel(new GridLayout(0, 1, BORDER_HEIGHT, 5));
		pCenter
				.setBorder(new EmptyBorder(BORDER_HEIGHT, 10, BORDER_HEIGHT, 10));
		/* Scroll Panel */
		final JScrollPane scrollPane = new JScrollPane(pCenter);
		scrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		cddisp.add(scrollPane, java.awt.BorderLayout.CENTER);
		/* Buttons for display dialog */
		final JPanel pLower = new JPanel(new GridLayout(0, 1, 0, 0));
		pLower.setBorder(new EmptyBorder(5, 0, 0, 0));
		cddisp.add(pLower, java.awt.BorderLayout.SOUTH);
		final JPanel pButtonsTop = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pLower.add(pButtonsTop);
		final JPanel pLoadSave = new JPanel(new GridLayout(1, 0, 5, 5));
		pButtonsTop.add(pLoadSave);
		final JButton bload = new JButton("Load\u2026");
		bload.addActionListener(event -> load());
		pLoadSave.add(bload);

		final JButton bsave = new JButton("Save\u2026");
		bsave.addActionListener(event -> save());
		pLoadSave.add(bsave);

		final JPanel pButtonsBottom = new JPanel(new FlowLayout(
				FlowLayout.CENTER));
		pLower.add(pButtonsBottom);
		final JPanel pOKApplyCancel = new JPanel(new GridLayout(1, 0, 5, 5));
		pButtonsBottom.add(pOKApplyCancel);

		final JButton brecall = new JButton("Recall");
		brecall.addActionListener(event -> read());
		pOKApplyCancel.add(brecall);
		final JButton bok = new JButton("OK");
		bok.addActionListener(event -> {
            set();
            dispose();
        });
		pOKApplyCancel.add(bok);
		final JButton bapply = new JButton("Apply");
		bapply.addActionListener(event -> set());
		pOKApplyCancel.add(bapply);
		final JButton bcancel = new JButton(new jam.ui.WindowCancelAction(this));
		pOKApplyCancel.add(bcancel);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent event) {
				dispose();
			}
		});
		doSetup();
	}

	/**
	 * @return whether all text fields are numbers
	 */
	private boolean checkTextAreNumbers() {
		String textValue;
		boolean allValid = true;
		// Check all fields are numbers
		invalidNames = "";
		for (int i = 0; i < textParam.length; i++) {
			try {
				textValue = textParam[i].getText().trim();
				if (textValue.length() == 0) {
					textValue = "0.0";
					textParam[i].setText(textValue);
				}
			} catch (NumberFormatException nfe) {
				allValid = false;
				if (invalidNames.length() == 0) {
					invalidNames = labelParam[i].getText();
				} else {
					invalidNames = invalidNames + ", "
							+ labelParam[i].getText();
				}
			}
		}
		return allValid;
	}

	private String copyPropertiesToParameters(final String name,
			final StringBuilder listNotLoaded, final Properties saveProperties) {
		// copy from properties to parameters
		String rval = name;
		for (DataParameter parameter : DataParameter.getParameterList()) {
			rval = parameter.getName().trim();
			if (saveProperties.containsKey(rval)) {
				final String valueString = (String) saveProperties.get(rval);
				final double valueDouble = Double.parseDouble(valueString);
				parameter.setValue(valueDouble);
			} else {
				if (listNotLoaded.length() > 0) {
					listNotLoaded.append(", ");
				}
				listNotLoaded.append(rval);
			}
		}
		return rval;
	}

	private JLabel createParameterLabel(final DataParameter currentParameter) {
		return new JLabel(currentParameter.getName().trim(),
				SwingConstants.RIGHT);
	}

	private JPanel createParameterPanel() {
		return new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
	}

	/**
	 */
	private JTextField createParameterText() {
		final JTextField rval = new JTextField("");
		rval.setColumns(10);
		rval.setEditable(true);
		return rval;
	}

	/**
	 * Setup the display dialog box.
	 * 
	 */
	@Override
	public void doSetup() {
		int count;
		final List<DataParameter> plist = DataParameter.getParameterList();
		final int numberParameters = plist.size();
		pCenter.removeAll();
		final JPanel[] pParam = new JPanel[numberParameters];
		// we have some elements in the parameter list
		if (numberParameters != 0) {
			// widgets for each parameter
			labelParam = new JLabel[numberParameters];
			textParam = new JTextField[numberParameters];
			count = 0;
			for (DataParameter currentParameter : plist) {
				pParam[count] = createParameterPanel();
				pCenter.add(pParam[count]);
				labelParam[count] = createParameterLabel(currentParameter);
				pParam[count].add(labelParam[count]);
				textParam[count] = createParameterText();
				pParam[count].add(textParam[count]);
				count++;
			}
		}
		pack();
		if (numberParameters > 0) {
			final Dimension dialogDim = calculateScrollDialogSize(this,
					pParam[0], BORDER_HEIGHT, numberParameters);
			setSize(dialogDim);
		}
	}

	private void load() {
		String name = "";
		final StringBuilder listNotLoaded = new StringBuilder();
		final JFileChooser fileDialog = new JFileChooser(lastFile);
		fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileDialog.setFileFilter(new ExtensionFileFilter(
				new String[] { FILE_EXTENSION }, "List Files (*."
						+ FILE_EXTENSION + ")"));
		final int option = fileDialog.showOpenDialog(null);
		// save current values
		if (option == JFileChooser.APPROVE_OPTION
				&& fileDialog.getSelectedFile() != null) {
			final File inputFile = fileDialog.getSelectedFile();
			FileInputStream fis = null;
			try {
				// Load properties from file
				final Properties saveProperties = new Properties();
				fis = new FileInputStream(inputFile);
				saveProperties.load(fis);
				name = copyPropertiesToParameters(name, listNotLoaded, // NOPMD
						saveProperties);// NOPMD
				read();
				LOGGER.info("Load Parameters from file " + inputFile.getName());
				if (listNotLoaded.length() > 0) {
					LOGGER.warning("Did not load parameter(s) " + listNotLoaded
							+ ".");
				}
				lastFile = inputFile;
			} catch (IOException ioe) {
				LOGGER.severe("Loading Parameters. Cannot write to file "
						+ inputFile.getName());
			} catch (NumberFormatException nfe) {
				LOGGER
						.severe("Loading Parameters. Cannot convert value for Parameter: "
								+ name);
			} finally {
				try {
					if (fis != null) {
						fis.close();
					}
				} catch (IOException ioe) {
					LOGGER.log(Level.SEVERE, ioe.getMessage(), ioe);
				}
			}

		}
	}

	/**
	 * Read the values from the Parameter Objects and display them.
	 */
	public void read() {
		if (DataParameter.getParameterList().size() != 0) {
			DataParameter.getParameterList().size();// number of parameters
			int count = 0;
			for (DataParameter currentParameter : DataParameter
					.getParameterList()) {
				textParam[count].setText(String.valueOf(currentParameter
						.getValue()));
				count++;
			}
		}
	}

	/**
	 * Save the parameters to a file
	 * 
	 */
	private void save() {
		set();
		final JFileChooser fileDialog = new JFileChooser(lastFile);
		fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileDialog.setFileFilter(new ExtensionFileFilter(
				new String[] { FILE_EXTENSION }, "List Files (*."
						+ FILE_EXTENSION + ")"));
		final int option = fileDialog.showSaveDialog(null);
		// save current values
		if (option == JFileChooser.APPROVE_OPTION
				&& fileDialog.getSelectedFile() != null) {
			final File selectFile = fileDialog.getSelectedFile();
			final File outputFile = this.fileUtilities.changeExtension(
					selectFile, FILE_EXTENSION,
					jam.util.FileUtilities.APPEND_ONLY);
			if (this.fileUtilities.overWriteExistsConfirm(outputFile)) {
				final Properties saveProperties = new Properties();
				for (DataParameter parameter : DataParameter.getParameterList()) {
					final String valueString = String.valueOf(parameter
							.getValue());
					saveProperties.put(parameter.getName().trim(), valueString);
				}
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(outputFile);
					saveProperties.store(fos, "Jam Sort Parameters");
					LOGGER.info("Saved Parameters to file "
							+ outputFile.getName());
					lastFile = outputFile;
				} catch (FileNotFoundException fnfe) {
					LOGGER.severe("Saving Parameters. Cannot create file "
							+ outputFile.getName());
				} catch (IOException ioe) {
					LOGGER.severe("Saving Parameters. Cannot write to file "
							+ outputFile.getName());
				} finally {
					try {
						if (fos != null) {
							fos.close();
						}
					} catch (IOException ioe) {
						LOGGER.log(Level.SEVERE, ioe.getMessage(), ioe);
					}
				}
			}
		}
	}

	/**
	 * Set the parameter values using the values in the text fields
	 * 
	 */
	public void set() {
		int count;
		String textValue;
		if (checkTextAreNumbers()) {
			try {
				count = 0;
				for (DataParameter currentParameter : DataParameter
						.getParameterList()) {
					textValue = textParam[count].getText().trim();
					currentParameter.setValue(Double.parseDouble(textValue));
					count++;
				}
				read();
			} catch (NumberFormatException nfe) {
				LOGGER.log(Level.SEVERE, "Not a valid number for a parameter.",
						nfe);
			}
		} else {
			if (invalidNames.length() > 0) {
				LOGGER
						.severe("Parameters not set, not a number for parameter(s) "
								+ invalidNames + ".");
			}
		}
	}

	@Override
	public void setVisible(final boolean state) {
		if (state) {
			read();
		}
		super.setVisible(state);
	}
}
