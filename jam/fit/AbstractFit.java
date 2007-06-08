package jam.fit;

import static javax.swing.SwingConstants.RIGHT;
import jam.data.AbstractHist1D;
import jam.data.HistDouble1D;
import jam.data.HistInt1D;
import jam.data.Histogram;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.plot.Bin;
import jam.plot.PlotDisplay;
import jam.plot.PlotMouseListener;
import jam.ui.Canceller;
import jam.ui.SelectionTree;
import jam.ui.WindowCancelAction;
import jam.util.NumberUtilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

/**
 * This defines the necessary methods that need to be defined in order for a fit
 * routine to work with <code>FitControl</code>. It also contains the dialog
 * box that serves as the user interface.
 * 
 * @author Dale Visser
 * @author Ken Swartz
 * 
 * @see AbstractNonLinearFit
 * @see GaussianFit
 */
public abstract class AbstractFit implements PlotMouseListener {

	private static final JamStatus STATUS = JamStatus.getSingletonInstance();

	private static final ValueAndUncertaintyFormatter FORMAT = ValueAndUncertaintyFormatter
			.getSingletonInstance();

	/**
	 * Checkboxes for finding initial estimates.
	 */
	private transient Map<Parameter, JCheckBox> cEstimate;

	/**
	 * Checkboxes for parameter fixing.
	 */
	private transient Map<Parameter, JCheckBox> cFixValue;

	/**
	 * Checkboxes for miscellaneous options.
	 */
	private transient Map<Parameter, JCheckBox> cOption;

	/**
	 * The histogram to be fitted.
	 */
	protected transient double[] counts;

	/**
	 * Dialog box
	 */
	private transient JDialog dfit;

	/**
	 * Jam main display window.
	 */
	private transient PlotDisplay display;

	/**
	 * The errors associatied with <code>counts</code>.
	 */
	protected transient double[] errors;

	/**
	 * If these are set, they are used for display.
	 */
	protected transient int lowerLimit;

	/**
	 * Displayed name of <code>Fit</code> routine.
	 */
	protected transient String name;

	/**
	 * <code>Enumeration</code> of parameters
	 */
	protected transient Iterator<Parameter> parameterIter;

	/**
	 * The ordered list of all <code>Parameter</code> objects..
	 * 
	 * @see Parameter
	 */
	protected transient List<Parameter> parameters = new ArrayList<Parameter>();

	/**
	 * The list of <code>Parameter</code> objects accessible by name.
	 * 
	 * @see Parameter
	 */
	private transient final Map<String, Parameter> parameterTable = Collections
			.synchronizedMap(new HashMap<String, Parameter>());

	/**
	 * Whether to calculate residuals.
	 */
	protected transient boolean residualOption = true;

	/**
	 * Status message
	 */
	private transient JLabel status;

	/**
	 * Data field values.
	 */
	private transient Map<Parameter, JTextField> textData;

	/**
	 * Fit error field values.
	 */
	private transient Map<Parameter, JLabel> textError;

	/**
	 * Histogram name field
	 */
	private transient JLabel textHistName;

	/**
	 * The text display area for information the fit produces.
	 */
	protected transient FitConsole textInfo;

	protected transient int upperLimit;

	/**
	 * Class constructor.
	 * 
	 * @param name
	 *            name for dialog box and menu item
	 */
	public AbstractFit(String name) {
		super();
		this.name = name;
	}

	// -------------------------
	// List of abstract methods
	// -------------------------

	/**
	 * Adds the given parameter to the list.
	 * 
	 * @param newParameter
	 *            parameter to add
	 * @see #parameterTable
	 * @see #parameters
	 */
	protected final void addParameter(final Parameter newParameter) {
		parameters.add(newParameter);
		parameterTable.put(newParameter.getName(), newParameter);
	}

	private void addParameterGUI(final Parameter parameter, final JPanel west,
			final JPanel center, final JPanel east) {
		final JPanel middle = new JPanel(new GridLayout(1, 3));
		center.add(middle);
		addParamWestMiddle(parameter, west, middle);
		/* Take care of options. */
		final JPanel right = new JPanel(new GridLayout(1, 0));
		east.add(right);
		if (parameter.hasErrorBar()) {
			final JLabel error = new JLabel(formatError(parameter));
			textError.put(parameter, error);
			error.setEnabled(true);
			middle.add(error);
		}
		if (parameter.canBeFixed()) {
			final JCheckBox fixed = new JCheckBox("Fixed", parameter.isFixed());
			cFixValue.put(parameter, fixed);
			fixed.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent event) {
					setFixed(parameter);
				}
			});
			right.add(fixed);
		}
		if (parameter.canBeEstimated()) {
			final JCheckBox estimate = new JCheckBox("Estimate",
					parameter.estimate);
			cEstimate.put(parameter, estimate);
			estimate.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent event) {
					setEstimate(parameter);
				}
			});
			right.add(estimate);
		}
		if (parameter.isOutputOnly()) {
			textData.get(parameter).setEnabled(false);
		}
	}

	private void addParamWestMiddle(final Parameter parameter,
			final JPanel west, final JPanel middle) {
		final String parName = parameter.getName();
		if (parameter.isDouble() || parameter.isInteger()) {
			final JTextField data = new JTextField(formatValue(parameter), 8);
			textData.put(parameter, data);
			data.setEnabled(true);
			west.add(new JLabel(parName, RIGHT));
			middle.add(data);
		} else if (parameter.isText()) {
			final JLabel text = new JLabel(formatValue(parameter));
			west.add(new JLabel(parName, RIGHT));
			middle.add(text);
		} else if (parameter.isBoolean()) {
			final JCheckBox option = new JCheckBox(parName, parameter
					.getBooleanValue());
			cOption.put(parameter, option);
			option.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent event) {
					parameter.setValue(option.isSelected());
				}
			});
			middle.add(option);
			west.add(new JPanel());
		}
	}

	/**
	 * Calculates function value for a specific channel. Uses whatever the
	 * current values are for the parameters in <code>parameters</code>.
	 * 
	 * @param channel
	 *            channel to evaluate fit function at
	 * @return double containing evaluation
	 */
	public abstract double calculate(int channel);

	abstract double calculateBackground(int channel);

	/**
	 * Returns the evaluation of a signal at one particular channel. Should
	 * return zero if the given signal doesn't exist.
	 * 
	 * @param signal
	 *            which signal
	 * @param channel
	 *            to calculate signal value for
	 * @return signal value for the given channel
	 */
	abstract double calculateSignal(int signal, int channel);

	private void clear() {
		for (Parameter parameter : parameters) {
			if (!parameter.isBoolean()) {
				// text field backgrounds
				if (parameter.isOutputOnly()) {
					textData.get(parameter).setEditable(false);
				} else {
					if (!parameter.isText()) {
						textData.get(parameter).setBackground(Color.white);
					}
				}
				if (parameter.isFixed()) {
					setFixed(parameter);
				}
				if (parameter.canBeEstimated()) {
					setEstimate(parameter);
				}
			}
			setMouseActive(false);
		}
	}

	/**
	 * Creates user interface dialog box.
	 * 
	 * @param parent
	 *            controlling frame
	 * @param plotDisplay
	 *            Jam main class
	 */
	public final void createDialog(final Frame parent,
			final PlotDisplay plotDisplay) {
		display = plotDisplay;
		final int parNumber = parameters.size();
		dfit = new JDialog(parent, name, false);
		final Container contents = dfit.getContentPane();
		dfit.setResizable(false);
		dfit.setLocation(20, 50);
		contents.setLayout(new BorderLayout());
		final JTabbedPane tabs = new JTabbedPane();
		contents.add(tabs, BorderLayout.CENTER);
		final JPanel main = new JPanel(new BorderLayout());
		tabs.addTab("Fit", null, main, "Setup parameters and do fit.");
		textInfo = new FitConsole(35 * parNumber);
		tabs.addTab("Information", null, textInfo,
				"Additional information output from the fits.");
		/* top panel with histogram name */
		final JPanel pHistName = createHistogramNamePanel();
		main.add(pHistName, BorderLayout.NORTH);
		/* bottom panel with status and buttons */
		final JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(0, 1));
		main.add(bottomPanel, BorderLayout.SOUTH);
		/* status panel part of bottom panel */
		final JPanel statusPanel = createStatusPanel();
		bottomPanel.add(statusPanel);
		/* button panel part of bottom panel */
		final JPanel pbut = new JPanel();
		pbut.setLayout(new GridLayout(1, 0));
		bottomPanel.add(pbut);
		final JButton bMouseGet = createGetMouseButton();
		pbut.add(bMouseGet);
		final JButton bGo = createDoFitButton();
		pbut.add(bGo);
		final JButton bDraw = new JButton("Draw Fit");
		bDraw.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					drawFit();
				} catch (FitException fe) {
					status.setText(fe.getMessage());
				}
			}
		});
		pbut.add(bDraw);
		final JButton bReset = new JButton("Reset");
		bReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				reset();
			}
		});
		pbut.add(bReset);
		final Canceller canceller = new Canceller() {
			public void cancel() {
				setMouseActive(false);
				dfit.dispose();
			}
		};
		final JButton bCancel = new JButton(new WindowCancelAction(canceller));
		pbut.add(bCancel);

		/* Layout of parameter widgets */
		final JPanel panelParam = new JPanel(new GridLayout(0, 1));
		main.add(panelParam, BorderLayout.CENTER);
		final JPanel west = new JPanel(new GridLayout(0, 1));
		final JPanel center = new JPanel(new GridLayout(0, 1));
		final JPanel east = new JPanel(new GridLayout(0, 1));
		main.add(west, BorderLayout.WEST);
		main.add(east, BorderLayout.EAST);
		main.add(center, BorderLayout.CENTER);
		cFixValue = new HashMap<Parameter, JCheckBox>();
		cEstimate = new HashMap<Parameter, JCheckBox>();
		cOption = new HashMap<Parameter, JCheckBox>();
		textData = new HashMap<Parameter, JTextField>();
		textError = new HashMap<Parameter, JLabel>();
		for (final Parameter parameter : parameters) {
			addParameterGUI(parameter, west, center, east);
		} // loop for all parameters
		dfit.addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent event) {
				updateHist();
			}

			public void windowClosing(WindowEvent event) {
				setMouseActive(false);
				dfit.dispose();
			}
		});
		dfit.pack();
	}

	/**
	 * @return
	 */
	private JButton createGetMouseButton() {
		final JButton bMouseGet = new JButton("Get Mouse");
		bMouseGet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeMouse();
				setMouseActive(true);
			}
		});
		return bMouseGet;
	}

	/**
	 * @return
	 */
	private JPanel createStatusPanel() {
		final JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BorderLayout());
		statusPanel.add(new JLabel("Status: ", RIGHT), BorderLayout.WEST);
		status = new JLabel(
				"OK                                                          ");
		statusPanel.add(status, BorderLayout.CENTER);
		return statusPanel;
	}

	/**
	 * @return
	 */
	private JPanel createHistogramNamePanel() {
		final JPanel pHistName = new JPanel(new BorderLayout());
		pHistName.setBorder(LineBorder.createBlackLineBorder());
		pHistName.add(new JLabel("Fit Histogram: ", RIGHT), BorderLayout.WEST);
		textHistName = new JLabel("     ");
		pHistName.add(textHistName, BorderLayout.CENTER);
		return pHistName;
	}

	/**
	 * @return
	 */
	private JButton createDoFitButton() {
		final JButton bGo = new JButton("Do Fit");
		bGo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					setMouseActive(false);
					updateParametersFromDialog();
					getCounts();
					status.setText("Estimating...");
					estimate();
					status.setText("Doing Fit...");
					status.setText(doFit());
					updateDisplay();
					drawFit();
				} catch (FitException fe) {
					status.setText(fe.getMessage());
				}
			}
		});
		return bGo;
	}

	/**
	 * Performs calulations neccessary to find a best fit to the data.
	 * 
	 * @return <code>String</code> containing information about the fit.
	 * @exception FitException
	 *                thrown if unrecoverable error occurs during fit
	 */
	public abstract String doFit() throws FitException;

	/*
	 * non-javadoc: Draw the fit with the values in the fields.
	 */
	private void drawFit() throws FitException {
		double[][] signals = null;
		double[] residuals = null;
		double[] background = null;
		updateParametersFromDialog();
		final int numChannel = upperLimit - lowerLimit + 1;
		if (getNumberOfSignals() > 0) {
			signals = new double[getNumberOfSignals()][numChannel];
			for (int sig = 0; sig < signals.length; sig++) {
				for (int i = 0; i < numChannel; i++) {
					signals[sig][i] = calculateSignal(sig, i + lowerLimit);
				}
			}
		}
		if (residualOption) {
			residuals = new double[numChannel];
			for (int i = 0; i < numChannel; i++) {
				final int chan = i + lowerLimit;
				residuals[i] = counts[chan] - calculate(chan);
			}
		}
		if (hasBackground()) {
			background = new double[numChannel];
			for (int i = 0; i < numChannel; i++) {
				background[i] = this.calculateBackground(i + lowerLimit);
			}
		}
		display.displayFit(signals, background, residuals, lowerLimit);
	}

	/**
	 * Changes parameter values to calculated estimates. Useful for
	 * <code>NonLinearFit</code>, which requires reasonably close guesses in
	 * order to converge on a good chi-squared minimum.
	 * 
	 * @exception FitException
	 *                thrown if unrecoverable error occurs during estimation
	 * @see AbstractNonLinearFit
	 * @see GaussianFit#estimate
	 */
	public abstract void estimate() throws FitException;

	private String formatError(final Parameter param) {
		if (!param.hasErrorBar()) {
			throw new IllegalArgumentException(
					"No error term for this parameter.  Can't formatError().");
		}
		return "\u00b1 "
				+ FORMAT.format(param.getDoubleValue(), param.getDoubleError())[1];
	}

	/*
	 * non-javadoc: Formats values in number text fields.
	 */
	private String formatValue(final Parameter param) {
		String temp = "Invalid Type"; // default return value
		if (param.isDouble()) {
			final double value = param.getDoubleValue();
			if (param.hasErrorBar()) {
				final double error = param.getDoubleError();
				temp = FORMAT.format(value, error)[0];
			} else {
				int integer = (int) NumberUtilities.getInstance().log10(
						Math.abs(value));
				integer = Math.max(integer, 1);
				final int fraction = Math.max(4 - integer, 0);
				temp = FORMAT.format(value, fraction);
			}
		} else if (param.isInteger()) {
			temp = (Integer.valueOf(param.getIntValue())).toString().trim();
		} else if (param.isText()) {
			temp = param.getText();
		}
		return temp;
	}

	/**
	 * Gets counts from currently displayed <code>Histogram</code>
	 */
	private void getCounts() {
		final AbstractHist1D hist1d = (AbstractHist1D) SelectionTree
				.getCurrentHistogram();
		if (hist1d.getType() == Histogram.Type.ONE_DIM_INT) {
			final int[] temp = ((HistInt1D) hist1d).getCounts();
			counts = NumberUtilities.getInstance().intToDoubleArray(temp);
		} else {
			counts = ((HistDouble1D) hist1d).getCounts();
		}
		errors = hist1d.getErrors();
	}

	/**
	 * Returns the number of signals modelled by the fit.
	 * 
	 * @return the number of signals modelled by the fit
	 */
	abstract int getNumberOfSignals();

	/**
	 * Accesses a parameter in <code>parameterTable</code> by name.
	 * 
	 * @param which
	 *            contains the name of the parameter (same name displayed in
	 *            dialog box)
	 * @return the <code>Parameter</code> object going by that name
	 */
	protected final Parameter getParameter(final String which) {
		return parameterTable.get(which);
	}

	/**
	 * Gets the contents of <code>parameters</code>.
	 * 
	 * @return the contents of <code>parameters</code>
	 * @see #parameters
	 */
	final List<Parameter> getParameters() {
		return parameters;
	}

	final MessageHandler getTextInfo() {
		return textInfo;
	}

	/**
	 * 
	 * @return whether fit function has a background component
	 */
	abstract boolean hasBackground();

	/**
	 * Set the state to enter values using mouse
	 */
	private void initializeMouse() {
		final List<Parameter> tempList = new ArrayList<Parameter>();
		for (int i = 0; i < parameters.size(); i++) {
			final Parameter parameter = parameters.get(i);
			if (parameter.isMouseClickable() && (!parameter.isFixed())) {
				textData.get(parameter).setForeground(Color.RED);
				tempList.add(parameter);
			}
		}
		if (tempList.size() > 0) {
			final StringBuilder temp = new StringBuilder(
					"Click spectrum to set: ");
			temp.append(tempList.get(0).getName());
			if (tempList.size() > 1) {
				final int len = tempList.size();
				for (int i = 1; i < len; i++) {
					final String pname = tempList.get(i).getName();
					if (i == (len - 1)) {
						temp.append(" and ");
					} else {
						temp.append(", ");
					}
					temp.append(pname);
				}
			}
			status.setText(temp.toString());
		}
		parameterIter = parameters.iterator();
	}

	public void plotMousePressed(final Bin bin, final Point pPixel) {
		while (parameterIter.hasNext()) {
			final Parameter parameter = parameterIter.next();
			if (parameter.isMouseClickable() && (!parameter.isFixed())) {
				final JTextField data = textData.get(parameter);
				data.setForeground(Color.BLACK);
				data.setText("" + bin.getX());
				break;
			}
		}
		if (!parameterIter.hasNext()) {
			setMouseActive(false);
		}
	}

	/**
	 * Resets all the parameter to default values, which are zero for now.
	 */
	private void reset() {
		for (Parameter parameter : parameters) {
			if (parameter.isDouble()) {
				parameter.setValue(0.0);
				textData.get(parameter).setText(formatValue(parameter));
			} else if (parameter.isInteger()) {
				parameter.setValue(0);
				textData.get(parameter).setText(formatValue(parameter));
			}
			if (parameter.hasErrorBar()) {
				parameter.setError(0.0);
				textError.get(parameter).setText(formatError(parameter));
			}
		}
		clear();
	}

	/*
	 * non-javadoc: a estimate checkbox was toggled
	 */
	private void setEstimate(final Parameter param) {
		final boolean state = cEstimate.get(param).isSelected();
		param.setEstimate(state);
	}

	/*
	 * non-javadoc: Called when a fix checkbox is toggled.
	 */
	private void setFixed(final Parameter param) {
		final boolean fixed = cFixValue.get(param).isSelected();
		param.setFixed(fixed);
		if (fixed) {
			if (param.canBeEstimated()) {
				final JCheckBox estimate = cEstimate.get(param);
				estimate.setSelected(false);
				param.setEstimate(false);
				estimate.setEnabled(false);
			}
			if (param.hasErrorBar()) {
				param.setError(0.0);
				textError.get(param).setText(formatError(param));
				textData.get(param).setEditable(!fixed);
			}
			// not a fixed value
		} else {
			if (param.canBeEstimated()) {
				cEstimate.get(param).setEnabled(true);
			}
			if (param.hasErrorBar()) {
				textData.get(param).setEditable(!fixed);
			}
		}
	}

	/*
	 * non-javadoc: Sets whether to receive mouse clicks from display or not.
	 */
	private void setMouseActive(final boolean state) {
		if (state) {
			display.addPlotMouseListener(this);
		} else {
			display.removePlotMouseListener(this);
		}
	}

	/**
	 * Show fit dialog box.
	 */
	final void show() {
		setMouseActive(false);
		dfit.setVisible(true);
	}

	/*
	 * non-javadoc: Update all fields in the dialog after performing a fit.
	 */
	private void updateDisplay() {
		updateHist();
		for (int i = 0; i < parameters.size(); i++) {
			final Parameter parameter = parameters.get(i);
			if (parameter.isDouble() || parameter.isInteger()) {
				textData.get(parameter).setText(formatValue(parameter));
				if (parameter.canBeFixed()) {
					cFixValue.get(parameter).setSelected(parameter.isFixed());
					setFixed(parameter);
				}
				if (parameter.hasErrorBar()) {
					textError.get(parameter).setText(formatError(parameter));
					if (!parameter.isFixed()) {
						textData.get(parameter).setBackground(Color.YELLOW);
					}
				}
			}
		}
	}

	/**
	 * Update the name of the displayed histogram in the dialog box.
	 */
	private void updateHist() {
		final Histogram histogram = (Histogram) SelectionTree
				.getCurrentHistogram();
		if (histogram != null && histogram.getDimensionality() == 1) {
			if (histogram.getType() == Histogram.Type.ONE_DIM_INT) {
				final int[] temp = ((HistInt1D) histogram).getCounts();
				counts = NumberUtilities.getInstance().intToDoubleArray(temp);
			} else if (histogram.getType() == Histogram.Type.ONE_D_DOUBLE) {
				counts = ((HistDouble1D) histogram).getCounts();
			}
			textHistName.setText(histogram.getFullName());
		} else { // 2d
			textHistName.setText("Need 1D Hist!");
		}
	}

	/*
	 * non-javadoc: Set the parameter values using input in the text fields just
	 * before a fit.
	 */
	private void updateParametersFromDialog() throws FitException {
		for (Parameter parameter : parameters) {
			try {
				if (parameter.isDouble()) {
					parameter.setValue(Double.valueOf(
							textData.get(parameter).getText().trim())
							.doubleValue());
					if (parameter.hasErrorBar()) {
						parameter.setError(Double.valueOf(
								textError.get(parameter).getText().substring(1)
										.trim()).doubleValue());
					}
				} else if (parameter.isInteger()) {
					parameter.setValue(Integer.valueOf(
							textData.get(parameter).getText().trim())
							.intValue());
				} else if (parameter.isBoolean()) {
					parameter.setValue(cOption.get(parameter).isSelected());
				}
			} catch (NumberFormatException nfe) {
				clear();
				throw new FitException("Invalid input, parameter: "
						+ parameter.getName(), nfe);
			}
		}
	}

}