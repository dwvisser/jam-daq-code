/**
 */
package jam.fit;

import injection.GuiceInjector;

/**
 * <p>
 * Parameters used to fit.
 * </p>
 * <dl>
 * <dt>INT</dt>
 * <dd>integer, such as Number of Peaks, or Minimum Channel</dd>
 * <dt>DOUBLE</dt>
 * <dd>standard variable fit parameter, includes a "fix value" checkbox</dd>
 * <dt>TEXT</dt>
 * <dd>field showing fit function and/or brief instructions</dd>
 * <dt>BOOLEAN</dt>
 * <dd>true/false option, such as Include Background, or Display Output (how
 * this would be implemented, I'm not sure)</dd>
 * </dl>
 * <p>
 * Options. You can use as many options as you want.
 * </p>
 * <dl>
 * <dt>(NO_)OUTPUT</dt>
 * <dd>is calculated and has no associated error bars (e.g. Chi-Squared)</dd>
 * <dt>(NO_)MOUSE</dt>
 * <dd>value can be obtained with mouse from screen</dd>
 * <dt>(NO_)ESTIMATE</dt>
 * <dd>can be estimated</dd>
 * <dt>(NO_)FIX</dt>
 * <dd>value is fixed..do not vary during fit</dd>
 * <dt>(NO_)KNOWN</dt>
 * <dd>input only, not varied by routine</dd>
 * </dl>
 */
final class Parameter<T> {

	/* used to determine the type */
	private final static int TYPE_MASK = 7;

	/**
	 * Default parameter type, a floating point number.
	 */
	public final static int DOUBLE = 0;

	/**
	 * Parameter is an integer number, e.g., a histogram channel number.
	 */
	final static int INT = 1;// NOPMD

	/**
	 * Parameter is a boolean value...displayed as a checkbox.
	 */
	final static int BOOLEAN = 2;// NOPMD

	/**
	 * Parameter is simply a text box.
	 */
	final static int TEXT = 4;// NOPMD

	/* Options */
	final static int FIX = 16;// NOPMD

	/* default value */
	final static int NO_FIX = 0; // NOPMD

	final static int ESTIMATE = 32;// NOPMD

	/* default value */
	final static int NO_ESTIMATE = 0; // NOPMD

	final static int MOUSE = 64;// NOPMD

	/* default value */
	final static int NO_MOUSE = 0; // NOPMD

	final static int OUTPUT = 128;// NOPMD

	/* default value */
	final static int NO_OUTPUT = 0; // NOPMD

	final static int KNOWN = 256;// NOPMD

	/* default value */
	final static int NO_KNOWN = 0; // NOPMD

	/***************************************************************************
	 * type contains the parameter type. Its value is available via getType().
	 * The types are: boolean,double,text, or int input/output or output only
	 * clickable estimatable
	 */
	private transient int options;

	private transient final int type;

	// int index; //not currently used

	/* the storage for the options and their default values */

	/* whether there is an error bar */
	private transient boolean errorOption = true;

	/* whether the parameter has been fixed */
	private transient boolean fixOption = false;

	/* whether the parameter can be set by clicking */
	private transient boolean mouseOption = false;

	/* whether the parameter is output only */
	private transient boolean outputOption = false;

	/* not sure */
	private transient boolean knownOption = false;

	// parameter name
	private transient final String name;

	private transient T value;

	// error field
	private transient double errorDbl;

	/**
	 * Whether or not the parameter is currently fixed.
	 */
	private transient boolean fix;

	/**
	 * Whether or not this parameter should be estimated automatically before
	 * doing fit.
	 */
	public boolean estimate;

	private transient final Object monitor = new Object();

	private static final ValueAndUncertaintyFormatter FORMATTER = GuiceInjector
			.getValueAndUncertaintyFormatter();

	Parameter(final String name, final int options) {
		this.name = name;
		this.options = options;
		type = options & TYPE_MASK;
		setInitialValue();
		fix = false;
		estimate = false;

		if (type == BOOLEAN) {
			errorOption = false;
		}
		if ((this.options & Parameter.FIX) != 0) {
			fixOption = true;
		}
		if ((this.options & Parameter.MOUSE) != 0) {
			mouseOption = true;
		}
		if ((this.options & Parameter.OUTPUT) != 0) {
			outputOption = true;
		}
		if ((this.options & Parameter.KNOWN) != 0) {
			knownOption = true;
			errorOption = false;// default=true
		}
	}

	@SuppressWarnings("unchecked")
	private void setInitialValue() {
		if (type == Parameter.TEXT) {
			this.setValue((T) "");
			this.options |= KNOWN; // change default for TEXT
		} else if (type == Parameter.DOUBLE) {
			this.setValue(0.0, 0.0);
		} else if (type == Parameter.INT) {
			this.setValue((T) Integer.valueOf(0));
		}
	}

	Parameter(final String name, final int option1, final int option2) {
		this(name, option1 | option2);
	}

	Parameter(final String name, final int option1, final int option2,
			final int option3) {
		this(name, option1 | option2 | option3);
	}

	Parameter(final String name, final int option1, final int option2,
			final int option3, final int option4) {
		this(name, option1 | option2 | option3 | option4);
	}

	Parameter(final String name, final int option1, final int option2,
			final int option3, final int option4, final int option5) {
		this(name, option1 | option2 | option3 | option4 | option5);
	}

	Parameter(final String name, final int option1, final int option2,
			final int option3, final int option4, final int option5,
			final int option6) {
		this(name, option1 | option2 | option3 | option4 | option5 | option6);
	}

	// END OF CONSTRUCTORS

	protected String getName() {
		return name;
	}

	protected int getType() {
		return type;
	}

	protected void setFixed(final boolean state) {
		synchronized (monitor) {
			this.fix = state;
		}
	}

	/*
	 * non-javadoc: Tells whether the parameter is currently fixed.
	 */
	protected boolean isFixed() {
		synchronized (monitor) {
			return fix;
		}
	}

	protected boolean canBeFixed() {
		return fixOption;
	}

	protected boolean hasErrorBar() {
		return errorOption;
	}

	protected void setEstimate(final boolean state) {
		synchronized (monitor) {
			estimate = state;
		}
	}

	protected boolean isEstimate() {
		synchronized (monitor) {
			return (fix ? false : estimate);
		}
	}

	/*
	 * non-javadoc: Set the floating point value.
	 * 
	 * @throws UnsupportedOperationException if this is not a DOUBLE parameter
	 */
	protected void setValue(final T value) {
		synchronized (monitor) {
			this.value = value;
		}
	}

	/*
	 * non-javadoc: Set the floating point value.
	 * 
	 * @throws UnsupportedOperationException if this is not a DOUBLE parameter
	 * with error bar
	 */
	@SuppressWarnings("unchecked")
	protected void setValue(final double value, final double error) {
		if (errorOption && this.isDouble()) {
			synchronized (monitor) {
				this.value = (T) Double.valueOf(value);
				errorDbl = error;
			}
		} else {
			throw new UnsupportedOperationException("Parameter '" + name
					+ "' can't set a double value and error bar.");
		}
	}

	protected void setError(final double err) {
		if (errorOption && this.isDouble()) {
			synchronized (monitor) {
				errorDbl = err;
			}
		} else {
			throw new UnsupportedOperationException("Parameter '" + name
					+ "' can't set an error bar.");
		}
	}

	protected T getValue() {
		synchronized (monitor) {
			return value;
		}
	}

	protected double getDoubleError() {
		if (errorOption && this.isDouble()) {
			synchronized (monitor) {
				return errorDbl;
			}
		}
		throw new UnsupportedOperationException("Parameter '" + name
				+ "' can't get an error bar.");
	}

	protected boolean isBoolean() {
		return (type == BOOLEAN);
	}

	protected boolean isNumberField() {
		return (type == INT || type == DOUBLE);
	}

	protected boolean isDouble() {
		return type == DOUBLE;
	}

	protected boolean isInteger() {
		return type == INT;
	}

	/*
	 * non-javadoc: Returns true if the parameter is represented by a text field
	 * in the dialog box.
	 */
	protected boolean isText() {
		return (type == TEXT);
	}

	protected boolean canBeEstimated() {
		return ((options & ESTIMATE) != 0);
	}

	protected boolean isKnown() {
		return knownOption;
	}

	protected boolean isOutputOnly() {
		return outputOption;
	}

	protected boolean isMouseClickable() {
		return mouseOption;
	}

	protected String formatError() {
		if (!errorOption) {
			throw new IllegalArgumentException(
					"No error term for this parameter.");
		}
		return "\u00b1 "
				+ FORMATTER.format((Double) getValue(), getDoubleError())[1];
	}

	protected String formatValue() {
		String temp = "Invalid Type"; // default return value
		if (isDouble()) {
			final double doubleValue = (Double) getValue();
			if (hasErrorBar()) {
				final double error = getDoubleError();
				temp = FORMATTER.format(doubleValue, error)[0];
			} else {
				int integer = (int) GuiceInjector.getNumberUtilities().log10(
						Math.abs(doubleValue));
				integer = Math.max(integer, 1);
				final int fraction = Math.max(4 - integer, 0);
				temp = FORMATTER.format(doubleValue, fraction);
			}
		} else if (isInteger()) {
			temp = getValue().toString().trim();
		} else if (isText()) {
			temp = getValue().toString();
		}
		return temp;
	}
}