package jam.fit;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.*;
import jam.plot.PlotMouseListener;
import jam.global.MessageHandler;
import jam.plot.Display;
import jam.data.DataException;
import jam.data.Histogram;

/**
 * This defines the necessary methods that need to be defined in order for a 
 * fit routine to work with <code>FitControl</code>.  It also contains the dialog box
 * that serves as the user interface.
 *
 * @author  Dale Visser, Ken Swartz
 * @version 0.5, 08/27/98
 *
 * @see	    NonLinearFit
 * @see	    GaussianFit
 */
public abstract class Fit
	implements ActionListener, ItemListener, PlotMouseListener {

	/*
	 * Displayed name of <code>Fit</code> routine.
	 */
	protected String NAME;

	/**
	 * Controlling frame for this dialog box.
	 */
	protected Frame frame;

	/**
	 * Jam main display window.
	 */
	protected Display display;

	/**
	 * Class to send output messages to.
	 */
	protected MessageHandler msgHandler;

	/**
	 * Specification for number output.
	 */
	//private NumberFormat numberFormat;      

	/**
	 * The ordered list of all <code>Parameter</code> objects..
	 *
	 * @see Parameter
	 */
	protected Vector parameters = new Vector();

	/**
	 * The list of <code>Parameter</code> objects accessible by name.
	 *
	 * @see Parameter
	 */
	protected Hashtable parameterTable = new Hashtable();

	/**
	 * <code>Enumeration</code> of parameters
	 */
	protected Enumeration parameterEnum;

	/**
	 * The histogram to be fitted.
	 */
	protected double[] counts;

	/**
	 * The errors associatied with <code>counts</code>.
	 */
	protected double[] errors;

	/**
	 * If these are set, they are used for display.
	 */
	int lowerLimit;

	/**
	 * 
	 */
	int upperLimit;

	/**
	 * Counter for mouse input.
	 */
	private int mouseClickCount;

	protected boolean residualOption = true;

	/**
	 * Dialog box
	 */
	private JDialog dfit;

	/**
	 * panel containing parameters
	 */
	private JPanel panelParam;

	/**
	 * Layout for <code>dfit</dfit>
	 */
	private GridBagLayout gridBag;

	/**
	 * Checkboxes for parameter fixing.
	 */
	private JCheckBox[] cFixValue;

	/**
	 * Checkboxes for finding initial estimates.
	 */
	private JCheckBox[] cEstimate;

	/**
	 * Checkboxes for miscellaneous options.
	 */
	private JCheckBox[] cOption;

	/**
	 * Text printed in dialog box.
	 */
	private JTextField[] textKnown;

	/**
	 * Data field values.
	 */
	private JTextField[] textData;

	/**
	 * Fit error field values.
	 */
	private JTextField[] textError;

	/**
	 * Parameter labels
	 */
	private JLabel[] text;

	/**
	 * Status message
	 */
	private JLabel status;

	/**
	 * Histogram name field
	 */
	private JTextField textHistName;

	/**
	 * Button to get mouse input.
	 */
	private JButton bMouseGet;

	/**
	 * Class constructor.
	 *
	 * @param	name	name for dialog box and menu item
	 */
	public Fit(String name) {
		this.NAME = name;
	}

	//-------------------------
	// List of abstract methods 
	//-------------------------

	/**
	 * Changes parameter values to calculated estimates.  Useful for <code>NonLinearFit</code>, which requires 
	 * reasonably close guesses in order to converge on a good chi-squared minimum.
	 *
	 * @exception   FitException    thrown if unrecoverable error occurs during estimation
	 * @see NonLinearFit
	 * @see GaussianFit#estimate
	 */
	public abstract void estimate() throws FitException;

	/**
	 * Sets the contents of <code>counts</code>.
	 * 
	 * @param	counts the new array of channel values
	 * @see	#counts 
	 */

	/**
	 * Performs calulations neccessary to find a best fit to the data.
	 *
	 * @return	<code>String</code> containing information about the fit.
	 * @exception   FitException	    thrown if unrecoverable error occurs during fit
	 */
	public abstract String doFit() throws FitException;

	/**
	 * Calculates function value for a specific channel.  Uses whatever the current values are for the parameters in 
	 * <code>parameters</code>.
	 *
	 * @param	channel channel to evaluate fit function at
	 * @return	double containing evaluation 
	 */
	public abstract double calculate(int channel);

	//------------------------
	//implemented dialog stuff
	//------------------------

	/**
	 * Creates user interface dialog box.
	 *
	 * @param	frame	    controlling frame
	 * @param	fitControl  controlling object
	 * @param	display	    Jam main class
	 * @param	msgHandler  object to send error messages to
	 * @exception   FitException	    thrown if unrecoverable error occurs during dialog creation
	 */
	public void createDialog(
		Frame frame,
		Display display,
		MessageHandler msgHandler)
		throws FitException {

		this.frame = frame;
		this.display = display;
		this.msgHandler = msgHandler;

		//set up vector of parameter objects        
		Parameter parameter;
		String parName;
		int parNumber;

		parameters = getParameters();
		parNumber = parameters.size();

		// make dialog box
		dfit = new JDialog(frame, NAME, false);
		Container cp = dfit.getContentPane();
		dfit.setForeground(Color.black);
		dfit.setBackground(Color.lightGray);
		dfit.setResizable(true);
		dfit.setLocation(20, 50);
		dfit.setSize(450, (4 + parNumber) * 33);
		cp.setLayout(new BorderLayout(1, 1));

		//top panel with histogram name
		JPanel pHistName = new JPanel();
		pHistName.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		cp.add(pHistName, BorderLayout.NORTH);

		JLabel histLabel = new JLabel("Fit Histogram: ");
		pHistName.add(histLabel);

		textHistName = new JTextField("     ", 25);
		textHistName.setBackground(Color.lightGray);
		textHistName.setForeground(Color.black);
		textHistName.setEditable(false);
		pHistName.add(textHistName);

		//bUpdateHistName = new JButton("Update");
		//bUpdateHistName.setActionCommand("updatehist");
		//bUpdateHistName.addActionListener(this);
		//pHistName.add(bUpdateHistName);	    

		//bottom panel with status and buttons
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(0, 1, 10, 5));
		cp.add(bottomPanel, BorderLayout.SOUTH);

		//status panel part of bottom panel		
		JPanel statusPanel = new JPanel();
		int hgap = 10;
		int vgap = 5;
		statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
		statusPanel.add(new JLabel("Status:"));
		status =
			new JLabel("OK                                                          ");
		statusPanel.add(status);
		bottomPanel.add(statusPanel);

		//button panel part of bottom panel
		JPanel pbut = new JPanel();
		vgap = 10;
		pbut.setLayout(new FlowLayout(FlowLayout.CENTER, hgap, vgap));
		bottomPanel.add(pbut);

		bMouseGet = new JButton("Get Mouse");
		bMouseGet.setActionCommand("get clicks");
		bMouseGet.addActionListener(this);
		pbut.add(bMouseGet);

		JButton bGo = new JButton(" Do Fit  ");
		bGo.setActionCommand("fitgo");
		bGo.addActionListener(this);
		pbut.add(bGo);

		JButton bDraw = new JButton("Draw Fit");
		bDraw.setActionCommand("draw fit");
		bDraw.addActionListener(this);
		pbut.add(bDraw);

		JButton bReset = new JButton(" Reset  ");
		bReset.setActionCommand("reset");
		bReset.addActionListener(this);
		pbut.add(bReset);

		JButton bCancel = new JButton("Cancel ");
		bCancel.setActionCommand("fitcancel");
		bCancel.addActionListener(this);
		pbut.add(bCancel);

		//Layout of parameter widgets 
		panelParam = new JPanel();
		gridBag = new GridBagLayout();
		panelParam.setLayout(gridBag);
		cp.add(panelParam, BorderLayout.CENTER);

		//arrays to hold widgets for each parameter (not accualy widgets)	     	    
		cFixValue = new JCheckBox[parNumber];
		cEstimate = new JCheckBox[parNumber];
		cOption = new JCheckBox[parNumber];
		textKnown = new JTextField[parNumber];
		textData = new JTextField[parNumber];
		textError = new JTextField[parNumber];
		text = new JLabel[parNumber];

		for (int i = 0; i < parameters.size(); i++) {

			parameter = (Parameter) parameters.elementAt(i);
			parName = parameter.getName();
			if (parameter.type == Parameter.DOUBLE) {
				textData[i] = new JTextField(formatValue(parameter), 8);
				textData[i].setBackground(Color.white);
				textData[i].setForeground(Color.black);
				textData[i].setEditable(true);
				addComponent(new Label(parName), 1, i + 1);
				addComponent(textData[i], 3, i + 1);

			} else if (parameter.type == Parameter.INT) {
				textData[i] = new JTextField(formatValue(parameter), 8);
				textData[i].setBackground(Color.white);
				textData[i].setForeground(Color.black);
				textData[i].setEditable(true);
				addComponent(new Label(parName), 1, i + 1);
				addComponent(textData[i], 3, i + 1);

			} else if (parameter.type == Parameter.TEXT) {
				text[i] = new JLabel(formatValue(parameter));
				//need to chang width more colums
				addComponent(text[i], 1, i + 1, 5, 1);

			} else if (parameter.type == Parameter.BOOLEAN) {
				cOption[i] =
					new JCheckBox(parName, parameter.getBooleanValue());
				cOption[i].addItemListener(this);
				addComponent(new Label(""), 1, i + 1);
				addComponent(cOption[i], 3, i + 1, 3, 1);

			}
			if (parameter.knownOption) {
				textKnown[i] = new JTextField("0.0", 8);
				textKnown[i].setBackground(Color.white);
				textKnown[i].setForeground(Color.black);
				textKnown[i].setEditable(true);
				addComponent(textKnown[i], 2, i + 1);
			}

			// take care of options.
			if (parameter.errorOption) {

				textError[i] = new JTextField(formatError(parameter), 8);
				textError[i].setBackground(Color.white);
				textError[i].setForeground(Color.black);
				textError[i].setEditable(true);
				addComponent(new Label("+/-"), 4, i + 1);
				addComponent(textError[i], 5, i + 1);

			}
			if (parameter.fixOption) {
				cFixValue[i] = new JCheckBox("Fixed", parameter.fix);
				cFixValue[i].addItemListener(this);
				addComponent(cFixValue[i], 6, i + 1);
			}
			if (parameter.estimateOption) {

				cEstimate[i] = new JCheckBox("Estimate", parameter.estimate);
				cEstimate[i].addItemListener(this);
				addComponent(cEstimate[i], 7, i + 1);
			}

			if (parameter.outputOption) {
				textData[i].setBackground(Color.lightGray);
				textData[i].setEditable(false);

			}
		}
		//loop for all parameters

		dfit.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {

				//System.out.println("dispose Fit");
				setMouseActive(false);

				dfit.dispose();

			}
			// window methods to 
			public void windowActivated(WindowEvent e) {
				updateHist();

			}
		});

		reset();
	}

	/** 
	 * Show fit dialog box.
	 */
	public void show() {
		setMouseActive(false);
		dfit.show();
	}

	/**
	 * Sets whether to receive mouse clicks from display or not.
	 */
	private void setMouseActive(boolean state) {
		if (state) {
			display.addPlotMouseListener(this);
		} else {
			display.removePlotMouseListener(this);
		}
	}

	/**
	 * Takes action when user initiates one.
	 *
	 * @param	ae  action message initiated by user
	 */
	public void actionPerformed(ActionEvent ae) {
		String command = ae.getActionCommand();

		try {

			if (command == "fitcancel") {
				setMouseActive(false);
				dfit.dispose();
			} else if (command == "fitgo") {
				setMouseActive(false);
				setParamValues();
				getCounts();
				status.setText("Estimating...");
				estimate();
				status.setText("Doing Fit...");
				status.setText(doFit());
				updateDisplay();
				drawFit();
			} else if (command == "draw fit") {
				drawFit();
			} else if (command == "updatehist") {
				updateHist();
			} else if (command == "reset") {
				reset();
			} else if (command == "get clicks") {
				setInit();
				setMouseActive(true);
			}
		} catch (FitException fe) {
			status.setText(fe.getMessage());
		}
	}

	/**
	 * Takes action when a checkbox is toggled.
	 *
	 * @param	ie  item which changed
	 */
	public void itemStateChanged(ItemEvent ie) {
		int i;
		Parameter par;

		try {
			for (i = 0; i < parameters.size(); i++) {
				par = (Parameter) (parameters.elementAt(i));

				if ((par.options & Parameter.FIX) != 0) {
					if (ie.getItemSelectable() == cFixValue[i]) {
						setFixed(par, i);
					}

				}

				if (par.canBeEstimated()) {
					if (ie.getItemSelectable() == cEstimate[i]) {
						setEstimate(par, i);
					}

				}

				if (par.type == Parameter.BOOLEAN) {
					if (ie.getItemSelectable() == cOption[i]) {
						par.setValue(cOption[i].isSelected());
					}
				}
			}
		} catch (FitException fe) {
			status.setText(fe.getMessage());
		}
	}

	/**
	 *
	 */
	public void plotMousePressed(Point p, Point pPixel) {

		Parameter parameter;

		while (parameterEnum.hasMoreElements()) {
			parameter = (Parameter) parameterEnum.nextElement();
			mouseClickCount++;
			if (parameter.mouseOption && (!parameter.isFix())) {
				textData[mouseClickCount - 1].setBackground(Color.white);
				textData[mouseClickCount - 1].setText("" + p.x);
				break;
			}
		}

		if (!parameterEnum.hasMoreElements()) {
			setMouseActive(false);
		}

	}

	/**
	 * Set the state to enter values using mouse
	 */
	private void setInit() {
		Parameter parameter;

		mouseClickCount = 0;

		for (int i = 0; i < parameters.size(); i++) {
			parameter = (Parameter) parameters.elementAt(i);
			if (parameter.isNumberField()) {
				if (parameter.mouseOption && (!parameter.isFix())) {
					textData[i].setBackground(Color.red);
					if (parameter.errorOption) {
						textError[i].setBackground(Color.white);
					}
				} else if (parameter.outputOption) {
					textData[i].setBackground(Color.lightGray);
					if (parameter.errorOption) {
						textError[i].setBackground(Color.lightGray);
					}
				} else {
					textData[i].setBackground(Color.white);
					if (parameter.errorOption) {
						if (parameter.isFix()) {
							textError[i].setBackground(Color.lightGray);
						} else {
							textError[i].setBackground(Color.white);
						}
					}
				}
				if (parameter.knownOption) {
					textKnown[i].setBackground(Color.white);
				}
			}
		}
		parameterEnum = parameters.elements();
	}

	/**
	 * Set the parameter values using input in the text fields
	 * just before a fit.
	 */
	private void setParamValues() throws FitException {
		Parameter parameter = null;
		try {
			for (int i = 0; i < parameters.size(); i++) {
				parameter = (Parameter) parameters.elementAt(i);
				if (parameter.type == Parameter.DOUBLE) {
					((Parameter) (parameters.elementAt(i))).setValue(
						Double
							.valueOf(textData[i].getText().trim())
							.doubleValue());
					if (parameter.errorOption) {
						((Parameter) (parameters.elementAt(i))).setError(
							Double
								.valueOf(textError[i].getText().trim())
								.doubleValue());
					}
					if (parameter.knownOption) {
						((Parameter) (parameters.elementAt(i))).setKnown(
							Double
								.valueOf(textKnown[i].getText().trim())
								.doubleValue());
					}
				} else if (parameter.getType() == Parameter.INT) {
					((Parameter) (parameters.elementAt(i))).setValue(
						Integer
							.valueOf(textData[i].getText().trim())
							.intValue());
					if (parameter.knownOption) {
						((Parameter) (parameters.elementAt(i))).setKnown(
							Double
								.valueOf(textKnown[i].getText().trim())
								.doubleValue());
					}

				} else if (parameter.isBoolean()) {
					((Parameter) (parameters.elementAt(i))).setValue(
						cOption[i].isSelected());
				}

			}
		} catch (NumberFormatException nfe) {
			clear();
			throw new FitException(
				"Invalid input, parameter: " + parameter.getName());
		}
	}

	/**
	 * Update all fields in the dialog after performing a fit.
	 */
	private void updateDisplay() throws FitException {

		Parameter parameter;

		updateHist();

		for (int i = 0; i < parameters.size(); i++) {
			parameter = (Parameter) parameters.elementAt(i);
			if (parameter.isTextField() && parameter.type != Parameter.TEXT) {
				textData[i].setText(formatValue(parameter));
				if (parameter.isFix()) {
					textData[i].setBackground(Color.lightGray);
				} else {
					textData[i].setBackground(Color.yellow);
				}
				if (parameter.errorOption) {
					textError[i].setText(formatError(parameter));
					if (parameter.isFix()) {
						textError[i].setBackground(Color.lightGray);
					} else {
						textError[i].setBackground(Color.yellow);
					}
				}
				if (parameter.knownOption) {
					textKnown[i].setBackground(Color.lightGray);
				}
			}
		}
	}

	/**
	 * Resets all the parameter to default values, which are zero for now.
	 *
	 * @exception   FitException	    thrown if unrecoverable error occurs during reset of dialog
	 */
	public void reset() throws FitException {
		Parameter parameter;

		for (int i = 0; i < parameters.size(); i++) {
			parameter = (Parameter) parameters.elementAt(i);
			if (parameter.type == Parameter.DOUBLE) {
				parameter.setValue(0.0);
				textData[i].setText(formatValue(parameter));
			} else if (parameter.type == Parameter.INT) {
				parameter.setValue(0);
				textData[i].setText(formatValue(parameter));
			}
			if (parameter.errorOption) {
				parameter.setError(0.0);
				textError[i].setText(formatError(parameter));
			}
		}
		clear();
	}

	private void clear() throws FitException {

		Parameter parameter;

		for (int i = 0; i < parameters.size(); i++) {
			parameter = (Parameter) parameters.elementAt(i);
			if (!parameter.isBoolean()) {
				// text field backgrounds				    	    		    
				if (parameter.outputOption) {
					textData[i].setBackground(Color.lightGray);
					if (parameter.errorOption) {
						textError[i].setBackground(Color.lightGray);
					}
				} else {
					if (parameter.type != Parameter.TEXT) {
						textData[i].setBackground(Color.white);
						if (parameter.errorOption) {
							if (parameter.isFix()) {
								textError[i].setBackground(Color.lightGray);
							} else {
								textError[i].setBackground(Color.white);
							}
						}
					}
				}
				if (parameter.knownOption) {
					textKnown[i].setBackground(Color.white);
				}
				if (parameter.fixOption) {
					setFixed(parameter, i);
				}
				if (parameter.estimateOption) {
					setEstimate(parameter, i);
				}
			}
			setMouseActive(false);
		}
	}

	/**
	 * Called when a fix checkbox is toggled.
	 */
	private void setFixed(Parameter param, int index) throws FitException {

		boolean state = cFixValue[index].isSelected();

		param.fix = state;

		// fixed value
		if (state) {
			textData[index].setBackground(Color.white);
			if (param.estimateOption) {
				cEstimate[index].setSelected(false);
				param.setEstimate(false);
				cEstimate[index].setEnabled(false);
			}
			if (param.errorOption) {
				param.setError(0.0);
				textError[index].setText(formatError(param));
				textError[index].setBackground(Color.lightGray);
				textError[index].setEditable(false);
			}
			//not a fixed value
		} else {
			textData[index].setBackground(Color.white);
			if (param.estimateOption) {
				cEstimate[index].setEnabled(true);
			}
			if (param.errorOption) {
				textError[index].setBackground(Color.white);
				textError[index].setEditable(true);
			}
		}
	}

	/**
	 * a estimate checkbox was toggled
	 */
	private void setEstimate(Parameter param, int index) {
		boolean state;
		state = cEstimate[index].isSelected();
		param.setEstimate(state);
	}

	/**
	 * Makes a <code>parameterTable</code> from <code>parameters</code>.
	 *
	 * @see #parameterTable
	 * @see #parameters
	 */
	protected void addParameter(Parameter newParameter) {
		parameters.addElement(newParameter);
		parameterTable.put(newParameter.getName(), newParameter);
	}

	/**
	 * Accesses a parameter in <code>parameterTable</code> by name.
	 * 
	 * @param	which contains the name of the parameter (same name displayed in dialog box)
	 * @return	the <code>Parameter</code> object going by that name
	 */
	protected Parameter getParameter(String which) {
		return (Parameter) (parameterTable.get(which));
	}

	/**
	 * Gets the contents of <code>parameters</code>. 
	 *
	 * @return	the contents of <code>parameters</code> 
	 * @see	#parameters
	 */
	Vector getParameters() {
		return parameters;
	}

	/**
	 * Update the name of the displayed histogram in the dialog box.
	 */
	private void updateHist() {
		Histogram h;
		int[] ia;
		int j;

		textHistName.setEditable(true);
		h = display.getHistogram();
		if (h.getDimensionality() == 1) {
			if (h.getType() == Histogram.ONE_DIM_INT) {
				ia = (int[]) h.getCounts();
				counts = new double[ia.length];
				for (j = 0; j < ia.length; j++) {
					counts[j] = ia[j];
				}
			} else if (h.getType() == Histogram.ONE_DIM_DOUBLE) {
				counts = (double[]) h.getCounts();
			}
			textHistName.setText(h.getName());
		} else { //2d
			textHistName.setText("Need 1D Hist!");
		}
		textHistName.setEditable(false);
	}

	/**
	 * Draw the fit with the values in the fields.
	 */
	private void drawFit() throws FitException {
		setParamValues();
		int numChannel = (display.getHistogram()).getSizeX();
		double[] evaluated = new double[numChannel];
		double[] residuals = new double[numChannel];
		for (int i = 0; i < numChannel; i++) {
			evaluated[i] = calculate(i);
			residuals[i] = counts[i] - evaluated[i];
		}
		if (residualOption) {
			display.displayFit(evaluated, residuals, lowerLimit, upperLimit);
		} else {
			display.displayFit(evaluated, lowerLimit, upperLimit);
		}
	}

	/**
	 * Gets counts from currently displayed <code>Histogram</code>
	 */
	private void getCounts() throws FitException {
		int[] ia;
		int j;

		try {
			Histogram h = display.getHistogram();
			if (h.getType() == Histogram.ONE_DIM_INT) {
				ia = (int[]) h.getCounts();
				counts = new double[ia.length];
				for (j = 0; j < ia.length; j++) {
					counts[j] = ia[j];
				}
			} else if (h.getType() == Histogram.ONE_DIM_DOUBLE) {
				counts = (double[]) h.getCounts();
			}
			this.errors = h.getErrors();
		} catch (DataException de) {
			throw new FitException(de.getMessage());
		}
	}

	/**
	 * Formats values in number text fields.
	 */
	private String formatValue(Parameter param) throws FitException {

		String temp = "Invalid Type";

		double value;
		double error;
		int integer, fraction;

		if (param.type == Parameter.DOUBLE) {
			value = param.valueDbl;
			error = param.errorDbl;
			if (param.errorOption) {
				return format(value, error)[0];
			} else {
				integer = (int) log10(Math.abs(value));
				integer = Math.max(integer, 1);
				fraction = Math.max(4 - integer, 0);
				return format(value, integer, fraction);
			}
		} else if (param.type == Parameter.INT) {
			temp = (new Integer(param.valueInt)).toString().trim();
		} else if (param.type == Parameter.TEXT) {
			temp = param.valueTxt;
		}
		return temp;
	}

	private String formatError(Parameter param) throws FitException {
		if (!param.errorOption)
			throw new FitException("No error term for this parameter.  Can't formatError().");
		return format(param.valueDbl, param.errorDbl)[1];
	}

	/**
	 * Helper method for GridBagConstains 
	 * S
	 * @return  <code>void</code> 
	 * @since Version 0.5
	 */

	private void addComponent(Component component, int gridx, int gridy) {

		addComponent(component, gridx, gridy, 1, 1);

	}

	/**
	 * Helper method for GridBagConstains 
	 * 
	 * @since Version 0.5
	 */

	private void addComponent(
		Component component,
		int gridx,
		int gridy,
		int width,
		int height) {

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gridx;

		gbc.gridy = gridy;
		gbc.ipadx = 0;

		gbc.ipady = 0;

		gbc.gridwidth = width;

		gbc.gridheight = height;

		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.anchor = GridBagConstraints.EAST;

		gridBag.setConstraints(component, gbc);

		panelParam.add(component);
	}

	private String format(double value, int integer, int fraction) {
		NumberFormat fval;
		fval = NumberFormat.getInstance();
		fval.setGroupingUsed(false);
		fval.setMinimumFractionDigits(fraction);
		fval.setMinimumFractionDigits(fraction);
		fval.setMinimumIntegerDigits(integer);
		fval.setMaximumIntegerDigits(integer);
		return fval.format(value);
	}

	/**
	 *
	 */
	private String[] format(double value, double err) throws FitException {
		String[] out;
		NumberFormat fval, ferr;
		int temp;

		out = new String[2];
		fval = NumberFormat.getInstance();
		fval.setGroupingUsed(false);
		ferr = NumberFormat.getInstance();
		ferr.setGroupingUsed(false);
		if (err < 0.0)
			throw new FitException("format() can't use negative error.");
		if (err > 0.0) {
			temp = fractionDigits(err);
			ferr.setMinimumFractionDigits(temp);
			ferr.setMaximumFractionDigits(temp);
			fval.setMinimumFractionDigits(temp);
			fval.setMaximumFractionDigits(temp);
			temp = integerDigits(err);
			ferr.setMinimumIntegerDigits(temp);
			//ferr.setMaximumIntegerDigits(temp);
			fval.setMinimumIntegerDigits(1);
		} else {
			ferr.setMinimumFractionDigits(1);
			ferr.setMaximumFractionDigits(1);
			ferr.setMinimumIntegerDigits(1);
			ferr.setMaximumIntegerDigits(1);
			fval.setMinimumFractionDigits(1);
			fval.setMinimumIntegerDigits(1);
		}
		out[0] = fval.format(value);
		out[1] = ferr.format(err);
		return out;
	}

	private double log10(double x) {
		return Math.log(x) / Math.log(10.0);
	}

	/**
	 * Given an error, determines the appropriat number of fraction digits to show.
	 */
	private int fractionDigits(double err) throws FitException {
		int out;

		if (err == 0.0)
			throw new FitException("fractionDigits called with 0.0");
		if (err >= 2.0) {
			out = 0;
		} else if (err >= 1.0) {
			out = 1;
		} else if (firstSigFig(err) == 1) {
			out = decimalPlaces(err, 2);
		} else { // firstSigFig > 1
			out = decimalPlaces(err, 1);
		}
		return out;
	}

	/**
	 * Given an error term determine the appropriate number of integer digits to display.
	 */
	private int integerDigits(double err) throws FitException {
		int out;

		if (err == 0.0)
			throw new FitException("integerDigits called with 0.0");
		if (err >= 1.0) {
			out = (int) Math.ceil(log10(err));
		} else {
			out = 1;
		}
		return out;
	}

	/**
	 * Given a double, returns the value of the first significant decimal digit.
	 */
	private int firstSigFig(double x) throws FitException {
		if (x <= 0.0)
			throw new FitException("Can't call firstSigFig with non-positive number.");
		return (int) Math.floor(x / Math.pow(10.0, Math.floor(log10(x))));
	}

	/**
	 * Given a double between zero and 1, and number of significant figures desired, return
	 * number of decimal fraction digits to display.
	 */
	private int decimalPlaces(double x, int sigfig) throws FitException {
		int out;
		int pos; //position of firstSigFig

		if (x <= 0.0 || x >= 1.0)
			throw new FitException("Must call decimalPlaces() with x in (0,1).");
		if (sigfig < 1)
			throw new FitException("Can't have zero significant figures.");
		pos = (int) Math.abs(Math.floor(log10(x)));
		out = pos + sigfig - 1;
		return out;
	}
}