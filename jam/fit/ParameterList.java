package jam.fit;

import jam.plot.Bin;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.*;
import java.util.List;

import static javax.swing.SwingConstants.RIGHT;

/**
 * Holds parameters for Fit.
 * 
 * @author Dale Visser
 * 
 */
public class ParameterList implements Iterable<Parameter<?>> {

	/**
	 * The ordered list of all <code>Parameter</code> objects..
	 * 
	 * @see Parameter
	 */
	private transient final List<Parameter<?>> parameters = new ArrayList<>();
	/**
	 * The list of <code>Parameter</code> objects accessible by name.
	 * 
	 * @see Parameter
	 */
	private transient final Map<String, Parameter<?>> parameterTable = Collections
			.synchronizedMap(new HashMap<String, Parameter<?>>());
	/**
	 * Data field values.
	 */
	private transient final Map<Parameter<?>, JTextComponent> textData = new HashMap<>();
	/**
	 * Fit error field values.
	 */
	private transient final Map<Parameter<?>, JLabel> textError = new HashMap<>();
	/**
	 * Checkboxes for finding initial estimates.
	 */
	private transient final Map<Parameter<?>, JCheckBox> cEstimate = new HashMap<>();
	/**
	 * Checkboxes for parameter fixing.
	 */
	private transient final Map<Parameter<?>, JCheckBox> cFixValue = new HashMap<>();
	/**
	 * Checkboxes for miscellaneous options.
	 */
	private transient final Map<Parameter<?>, JCheckBox> cOption = new HashMap<>();

	/**
	 * @param newParameter
	 *            added to list
	 */
	protected void add(final Parameter<?> newParameter) {
		this.parameters.add(newParameter);
		this.parameterTable.put(newParameter.getName(), newParameter);
	}

	/**
	 * @return a readonly iterator over the list
	 */
	public Iterator<Parameter<?>> iterator() {
		return Collections.unmodifiableList(this.parameters).iterator();
	}

	/**
	 * @return the number of parameters
	 */
	protected int size() {
		return this.parameters.size();
	}

	/**
	 * @param which
	 *            name of parameter to get
	 * @return the parameter, or null if no parameter with the given name exists
	 */
	protected Parameter<?> get(final String which) {
		return this.parameterTable.get(which);
	}

	/**
	 * @param parameter
	 *            the parameter
	 * @param data
	 *            the text field associated with it
	 */
	protected void associateTextField(final Parameter<?> parameter,
			final JTextComponent data) {
		this.textData.put(parameter, data);
	}

	protected void highlightFields(final JLabel statusOut) {
		final List<Parameter<?>> tempList = new ArrayList<>();
		for (Parameter<?> parameter : this.parameters) {
			if (parameter.isMouseClickable() && (!parameter.isFixed())) {
				this.textData.get(parameter).setForeground(Color.RED);
				tempList.add(parameter);
			}
		}
		if (!tempList.isEmpty()) {
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
			statusOut.setText(temp.toString());
		}
	}

	/**
	 * @param parameter
	 *            to associate error label with
	 * @param error
	 *            the label
	 */
	protected void associateErrorLabel(final Parameter<Double> parameter,
			final JLabel error) {
		this.textError.put(parameter, error);
	}

	/**
	 * @param parameter
	 *            to associate estimate checkbox with
	 * @param estimate
	 *            the estimate checkbox
	 */
	protected void associateEstimateCheckbox(final Parameter<Double> parameter,
			final JCheckBox estimate) {
		this.cEstimate.put(parameter, estimate);

	}

	protected void setEstimate(final Parameter<Double> param) {
		final boolean state = this.cEstimate.get(param).isSelected();
		param.setEstimate(state);
	}

	/**
	 * @param parameter
	 *            to associate the fix checkbox with
	 * @param fixed
	 *            the fix checkbox
	 */
	protected void associateFixCheckbox(final Parameter<Double> parameter,
			final JCheckBox fixed) {
		this.cFixValue.put(parameter, fixed);
	}

	protected void setFixed(final Parameter<Double> param) {
		final boolean fixed = this.cFixValue.get(param).isSelected();
		param.setFixed(fixed);
		final JTextComponent textField = this.textData.get(param);
		if (fixed) {
			if (param.canBeEstimated()) {
				final JCheckBox estimate = this.cEstimate.get(param);
				estimate.setSelected(false);
				param.setEstimate(false);
				estimate.setEnabled(false);
			}
			if (param.hasErrorBar()) {
				param.setError(0.0);
				this.textError.get(param).setText(param.formatError());
				textField.setEditable(!fixed);
			}
			// not a fixed value
		} else {
			if (param.canBeEstimated()) {
				this.cEstimate.get(param).setEnabled(true);
			}
			if (param.hasErrorBar()) {
				textField.setEditable(!fixed);
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void clearGUI() {
		for (Parameter<?> parameter : this.parameters) {
			if (!parameter.isBoolean()) {
				// text field backgrounds
				if (parameter.isOutputOnly()) {
					this.textData.get(parameter).setEditable(false);
				} else {
					if (!parameter.isText()) {
						this.textData.get(parameter).setBackground(Color.white);
					}
				}
				if (parameter.isFixed()) {
					setFixed((Parameter<Double>) parameter);
				}
				if (parameter.canBeEstimated()) {
					setEstimate((Parameter<Double>) parameter);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void updateGUI() {
		for (Parameter<?> parameter : this.parameters) {
			final JTextComponent textField = this.textData.get(parameter);
			if (parameter.isDouble() || parameter.isInteger()) {
				textField.setText(parameter.formatValue());
				if (parameter.canBeFixed()) {
					this.cFixValue.get(parameter).setSelected(
							parameter.isFixed());
					setFixed((Parameter<Double>) parameter);
				}
				if (parameter.hasErrorBar()) {
					this.textError.get(parameter).setText(
							parameter.formatError());
					if (!parameter.isFixed()) {
						textField.setBackground(Color.YELLOW);
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void resetGUI() {
		for (Parameter<?> parameter : this.parameters) {
			final JTextComponent textField = this.textData.get(parameter);
			if (parameter.isDouble()) {
				((Parameter<Double>) parameter).setValue(0.0);
				textField.setText(parameter.formatValue());
			} else if (parameter.isInteger()) {
				((Parameter<Integer>) parameter).setValue(0);
				textField.setText(parameter.formatValue());
			}
			if (parameter.hasErrorBar()) {
				parameter.setError(0.0);
				this.textError.get(parameter).setText(parameter.formatError());
			}
		}
	}

	protected void associateOptionCheckbox(final Parameter<Boolean> parameter,
			final JCheckBox option) {
		this.cOption.put(parameter, option);
	}

	@SuppressWarnings("unchecked")
	protected void updateParameterFromDialog(final Parameter<?> parameter) {
		if (parameter.isDouble()) {
			final Parameter<Double> paramDouble = (Parameter<Double>) parameter;
			paramDouble.setValue(Double.valueOf(this.textData.get(parameter)
					.getText().trim()));
			if (parameter.hasErrorBar()) {
				parameter.setError(Double.parseDouble(this.textError.get(
						parameter).getText().substring(1)));
			}
		} else if (parameter.isInteger()) {
			final Parameter<Integer> paramInt = (Parameter<Integer>) parameter;
			paramInt.setValue(Integer.valueOf(this.textData.get(parameter)
					.getText().trim()));
		} else if (parameter.isBoolean()) {
			final Parameter<Boolean> paramBool = (Parameter<Boolean>) parameter;
			paramBool.setValue(this.cOption.get(parameter).isSelected());
		}
	}

	@SuppressWarnings("unchecked")
	protected void addParamWestMiddle(final Parameter<?> parameter,
			final JPanel west, final JPanel middle) {
		final String parName = parameter.getName();
		if (parameter.isDouble() || parameter.isInteger()) {
			final JTextField data = new JTextField(parameter.formatValue(), 8);
			associateTextField(parameter, data);
			data.setEnabled(true);
			west.add(new JLabel(parName, RIGHT));
			middle.add(data);
		} else if (parameter.isBoolean()) {
			final Parameter<Boolean> boolParam = (Parameter<Boolean>) parameter;
			final JCheckBox option = new JCheckBox(boolParam.getName(),
					boolParam.getValue());
			associateOptionCheckbox(boolParam, option);
			option.addItemListener(event -> boolParam.setValue(option.isSelected()));
			middle.add(option);
			west.add(new JPanel());
		} else if (parameter.isText()) {
			final JLabel text = new JLabel(parameter.formatValue());
			west.add(new JLabel(parameter.getName(), RIGHT));
			middle.add(text);
		}
	}

	@SuppressWarnings("unchecked")
	protected void addParameterGUI(final Parameter<?> parameter,
			final JPanel west, final JPanel center, final JPanel east) {
		final JPanel middle = new JPanel(new GridLayout(1, 3));
		center.add(middle);
		addParamWestMiddle(parameter, west, middle);
		/* Take care of options. */
		final JPanel right = new JPanel(new GridLayout(1, 0));
		east.add(right);
		if (parameter.hasErrorBar()) {
			final JLabel error = new JLabel(parameter.formatError());
			associateErrorLabel((Parameter<Double>) parameter, error);
			error.setEnabled(true);
			middle.add(error);
		}
		if (parameter.canBeFixed()) {
			final JCheckBox fixed = new JCheckBox("Fixed", parameter.isFixed());
			final Parameter<Double> paramDouble = (Parameter<Double>) parameter;
			associateFixCheckbox(paramDouble, fixed);
			fixed.addItemListener(event -> setFixed(paramDouble));
			right.add(fixed);
		}
		if (parameter.canBeEstimated()) {
			final JCheckBox estimate = new JCheckBox("Estimate",
					parameter.estimate);
			final Parameter<Double> paramDouble = (Parameter<Double>) parameter;
			associateEstimateCheckbox(paramDouble, estimate);
			estimate.addItemListener(event -> setEstimate(paramDouble));
			right.add(estimate);
		}
		if (parameter.isOutputOnly()) {
			this.textData.get(parameter).setEnabled(false);
		}
	}

	protected void setParameterText(final Bin bin, final Parameter<?> parameter) {
		final JTextComponent data = this.textData.get(parameter);
		data.setForeground(Color.BLACK);
		data.setText(Integer.toString(bin.getX()));
	}
}
