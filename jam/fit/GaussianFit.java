/*
 *
 */
package jam.fit;

import java.util.Arrays;
import static jam.global.GaussianConstants.MAGIC_2AB;
import static jam.global.GaussianConstants.MAGIC_A;
import static jam.global.GaussianConstants.MAGIC_B;
import static jam.global.GaussianConstants.SIG_TO_FWHM;

/**
 * This abstract class uses <code>NonLinearFit</code> to fit a single gaussian
 * peak with a background.. The background is a polynomial up to a quadradic
 * term if desired. (Channel - Centroid) is the term the polynomial is expanded
 * in.
 * 
 * @author Dale Visser
 * @version 0.5, 8/31/98
 * 
 * @see AbstractNonLinearFit
 */
public final class GaussianFit extends AbstractNonLinearFit {

	/**
	 * name of <code>Parameter</code> --centroid of peak
	 */
	public static final String CENTROID = "Centroid";

	/**
	 * name of <code>Parameter</code> --width of peak
	 */
	public static final String WIDTH = "Width";

	/**
	 * name of <code>Parameter</code> --area of peak
	 */
	public static final String AREA = "Area";

	/**
	 * function <code>Parameter</code> --area of peak
	 */
	private transient final Parameter area;


	/**
	 * function <code>Parameter</code> --wodth of peak
	 */
	private transient final Parameter width;




	/**
	 * used for calculations
	 */
	// private double diff;
	/**
	 * used for calculations
	 */
	// private double exp;
	/**
	 * Class constructor.
	 */
	public GaussianFit() {
		super("GaussianFit");

		Parameter background = new Parameter("Background: ", Parameter.TEXT);
		background.setValue("A+B(x-Centroid)+C(x-Centroid)\u00b2");
		Parameter equation = new Parameter("Peak: ", Parameter.TEXT);
		equation
				.setValue("2.354\u2219Area/(\u221a(2\u03c0)Width)\u2219exp[-2.354\u00b2(x-Centroid)\u00b2/(2 Width\u00b2)]");
		area = new Parameter(AREA, Parameter.DOUBLE, Parameter.FIX,
				Parameter.ESTIMATE);
		area.setEstimate(true);
		/**
		 * function <code>Parameter</code> --centroid of peak
		 */
		final Parameter centroid = new Parameter(CENTROID, Parameter.DOUBLE, Parameter.FIX,
				Parameter.MOUSE);
		width = new Parameter(WIDTH, Parameter.DOUBLE, Parameter.FIX,
				Parameter.ESTIMATE);
		width.setEstimate(true);
		/**
		 * function <code>Parameter</code> --constant background term
		 */
		final Parameter paramA = new Parameter("A", Parameter.DOUBLE, Parameter.FIX,
				Parameter.ESTIMATE);
		paramA.setEstimate(true);
		/**
		 * function <code>Parameter</code> --linear background term
		 */
		final Parameter paramB = new Parameter("B", Parameter.FIX);
		paramB.setFixed(true);
		/**
		 * function <code>Parameter</code> --quadratic background term
		 */
		final Parameter paramC = new Parameter("C", Parameter.FIX);
		paramC.setFixed(true);

		addParameter(equation);
		addParameter(background);
		addParameter(area);
		addParameter(centroid);
		addParameter(width);
		addParameter(paramA);
		addParameter(paramB);
		addParameter(paramC);

	}

	/**
	 * If so requested, estimates A, Area, and Width.
	 */
	public void estimate() {
		orderParameters();
		final int lowChan = getParameter(FIT_LOW).getIntValue();
		final int highChan = getParameter(FIT_HIGH).getIntValue();
		final double center = getParameter(CENTROID).getDoubleValue();
		double peakWidth = getParameter(WIDTH).getDoubleValue();
		double backLevel = getParameter("A").getDoubleValue();
		double intensity = getParameter(AREA).getDoubleValue();
		/* estimated level of background */
		if (getParameter("A").isEstimate()) {
			backLevel = (counts[lowChan] + counts[highChan]) * 0.5;
			getParameter("A").setValue(backLevel);
			textInfo.messageOutln("Estimated A = " + backLevel);
		}
		/* sum up counts */
		if (getParameter(AREA).isEstimate()) {
			intensity = 0.0;
			for (int i = lowChan; i <= highChan; i++) {
				intensity += counts[i] - backLevel;
			}
			getParameter(AREA).setValue(intensity);
			textInfo.messageOutln("Estimated area = " + intensity);
		}
		/* find width */
		double variance = 0.0;
		if (getParameter(WIDTH).isEstimate()) {
			for (int i = lowChan; i <= highChan; i++) {
				final double distance = i - center;
				variance += (counts[i] / intensity) * (distance * distance);
			}
			final double sigma = Math.sqrt(variance);
			peakWidth = SIG_TO_FWHM * sigma;
			getParameter(WIDTH).setValue(peakWidth);
			textInfo.messageOutln("Estimated width = " + peakWidth);
		}
	}

	/**
	 * Overrides normal setParameters to make sure channels are in proper order.
	 * This Allows the fit limits and centroids to be clicked in any order.
	 */
	private void orderParameters() {
		final double[] sortMe = { getParameter(FIT_LOW).getIntValue(),
				getParameter(CENTROID).getDoubleValue(),
				getParameter(FIT_HIGH).getIntValue() };
		Arrays.sort(sortMe);
		getParameter(FIT_LOW).setValue((int) sortMe[0]);
		getParameter(CENTROID).setValue(sortMe[1]);
		getParameter(FIT_HIGH).setValue((int) sortMe[2]);
	}

	/**
	 * Calculates the gaussian with background at a given x.
	 * 
	 * @param val
	 *            value to calculate at
	 * @return value of function at x
	 */
	public double valueAt(final double val) {
		final double diff = diff(val);
		final double temp = getValue("A") + getValue("B") * diff + getValue("C") * diff * diff
				+ getValue(AREA) / getValue(WIDTH) * MAGIC_A * exp(diff);
		return temp;
	}

	int getNumberOfSignals() {
		return 1;
	}

	double calculateSignal(final int sig, final int channel) {
		return sig == 0 ? area.getDoubleValue() / width.getDoubleValue()
				* MAGIC_A * exp(diff(channel)) : 0.0;
	}

	private double diff(final double val) {
		return val - getValue(CENTROID);
	}

	private double exp(final double diff) {
		return Math.exp(-MAGIC_B * diff * diff / (getValue(WIDTH) * getValue(WIDTH)));
	}

	boolean hasBackground() {
		return true;
	}

	double calculateBackground(final int channel) {
		final double diff = diff(channel);
		return getValue("A") + getValue("B") * diff + getValue("C") * diff * diff;
	}

	/**
	 * Evaluates derivative with respect to <code>parameterName</code> at
	 * <code>x</code>.
	 * 
	 * @param parName
	 *            the name of the parameter to differentiate with respect to
	 * @param val
	 *            value to evalueate at
	 * @return df( <code>x</code> )/d( <code>parameterName</code>) at x
	 */
	public double derivative(final double val, final String parName) {
		final double rval;
		final double diff = diff(val);
		final double exp = exp(diff);
		if (parName.equals(AREA)) {
			rval = MAGIC_A / getValue(WIDTH) * exp;
		} else if (parName.equals(CENTROID)) {
			rval = MAGIC_2AB * getValue(AREA) * exp * diff
					/ (getValue(WIDTH) * getValue(WIDTH) * getValue(WIDTH)) - getValue("B") - 2 * getValue("C")
					* diff;
		} else if (parName.equals(WIDTH)) {
			final double temp = -MAGIC_A * getValue(AREA) * exp
					/ (getValue(WIDTH) * getValue(WIDTH));
			rval = temp + MAGIC_2AB * getValue(AREA) * exp * diff * diff
					/ (getValue(WIDTH) * getValue(WIDTH) * getValue(WIDTH) * getValue(WIDTH));
		} else if ("A".equals(parName)) {
			rval = 1.0;
		} else if ("B".equals(parName)) {
			rval = diff;
		} else if ("C".equals(parName)) {
			rval = diff * diff;
		} else { // not valid
			throw new IllegalArgumentException("Invalid derivative argument: "
					+ parName);
		}
		return rval;
	}

}