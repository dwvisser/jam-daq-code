package jam.fit;

import jam.util.NumberUtilities;

import java.text.NumberFormat;

final class ValueAndUncertaintyFormatter {

	private static final ValueAndUncertaintyFormatter INSTANCE = new ValueAndUncertaintyFormatter();

	protected static ValueAndUncertaintyFormatter getSingletonInstance() {
		return INSTANCE;
	}

	private ValueAndUncertaintyFormatter() {
		// nothing to do
	}

	private static NumberUtilities util = NumberUtilities.getInstance();

	/*
	 * non-javadoc: Given an error, determines the appropriat number of fraction
	 * digits to show.
	 */
	private int fractionDigits(final double err) {
		int out;

		if (err == 0.0) {
			throw new IllegalArgumentException("fractionDigits called with 0.0");
		}
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

	/*
	 * non-javadoc: Given a double, returns the value of the first significant
	 * decimal digit.
	 */
	private int firstSigFig(final double value) {
		if (value <= 0.0) {
			throw new IllegalArgumentException(
					"Can't call firstSigFig with non-positive number.");
		}
		return (int) Math.floor(value
				/ Math.pow(10.0, Math.floor(util.log10(value))));
	}

	/*
	 * non-javadoc: Given a double between zero and 1, and number of significant
	 * figures desired, return number of decimal fraction digits to display.
	 */
	private int decimalPlaces(final double value, final int sigfig) {
		int out;
		int pos; // position of firstSigFig

		if (value <= 0.0 || value >= 1.0) {
			throw new IllegalArgumentException(
					"Must call decimalPlaces() with value in (0,1).");
		}
		if (sigfig < 1) {
			throw new IllegalArgumentException(
					"Can't have zero significant figures.");
		}
		pos = (int) Math.abs(Math.floor(util.log10(value)));
		out = pos + sigfig - 1;
		return out;
	}

	protected String[] format(final double value, final double err) {
		String[] out;
		NumberFormat fval, ferr;
		int temp;

		out = new String[2];
		fval = NumberFormat.getInstance();
		fval.setGroupingUsed(false);
		ferr = NumberFormat.getInstance();
		ferr.setGroupingUsed(false);
		if (err < 0.0) {
			throw new IllegalArgumentException(
					"format() can't use negative error.");
		}
		if (err > 0.0) {
			temp = fractionDigits(err);
			ferr.setMinimumFractionDigits(temp);
			ferr.setMaximumFractionDigits(temp);
			fval.setMinimumFractionDigits(temp);
			fval.setMaximumFractionDigits(temp);
			temp = integerDigits(err);
			ferr.setMinimumIntegerDigits(temp);
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

	/*
	 * non-javadoc: Given an error term determine the appropriate number of
	 * integer digits to display.
	 */
	private int integerDigits(final double err) {
		int out;

		if (err == 0.0) {
			throw new IllegalArgumentException("integerDigits called with 0.0");
		}
		if (err >= 1.0) {
			out = (int) Math.ceil(util.log10(err));
		} else {
			out = 1;
		}
		return out;
	}

	protected String format(final double value, final int fraction) {
		NumberFormat fval;
		fval = NumberFormat.getInstance();
		fval.setGroupingUsed(false);
		fval.setMinimumFractionDigits(fraction);
		fval.setMinimumFractionDigits(fraction);
		return fval.format(value);
	}

}
