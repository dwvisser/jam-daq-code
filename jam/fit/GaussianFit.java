/*
 *
 */
package jam.fit;

/**
 * This abstract class uses <code>NonLinearFit</code> to fit a single gaussian
 * peak with a background.. The background is a polynomial up to a quadradic
 * term if desired. (Channel - Centroid) is the term the polynomial is expanded
 * in.
 * 
 * @author Dale Visser
 * @version 0.5, 8/31/98
 * 
 * @see NonLinearFit
 */
public class GaussianFit extends NonLinearFit implements GaussianConstants {

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
	private Parameter area;

	/**
	 * function <code>Parameter</code> --centroid of peak
	 */
	private Parameter centroid;

	/**
	 * function <code>Parameter</code> --wodth of peak
	 */
	private Parameter width;

	/**
	 * function <code>Parameter</code> --constant background term
	 */
	private Parameter paramA;

	/**
	 * function <code>Parameter</code> --linear background term
	 */
	private Parameter paramB;

	/**
	 * function <code>Parameter</code> --quadratic background term
	 */
	private Parameter paramC;

	/**
	 * used for calculations
	 */
	private double diff;

	/**
	 * used for calculations
	 */
	private double exp;

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
		centroid = new Parameter(CENTROID, Parameter.DOUBLE, Parameter.FIX,
				Parameter.MOUSE);
		width = new Parameter(WIDTH, Parameter.DOUBLE, Parameter.FIX,
				Parameter.ESTIMATE);
		width.setEstimate(true);
		paramA = new Parameter("A", Parameter.DOUBLE, Parameter.FIX,
				Parameter.ESTIMATE);
		paramA.setEstimate(true);
		paramB = new Parameter("B", Parameter.FIX);
		paramB.setFixed(true);
		paramC = new Parameter("C", Parameter.FIX);
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
	 * 
	 * @exception FitException
	 *                thrown if unrecoverable error occurs during estimation
	 */
	public void estimate() {

		orderParameters();

		int i, minCH, maxCH;
		double area, width, centroid, distance, sigma, variance, backLevel;

		minCH = getParameter(FIT_LOW).getIntValue();
		maxCH = getParameter(FIT_HIGH).getIntValue();
		centroid = getParameter(CENTROID).getDoubleValue();
		width = getParameter(WIDTH).getDoubleValue();
		backLevel = getParameter("A").getDoubleValue();
		area = getParameter(AREA).getDoubleValue();

		//estimated level of background
		if (getParameter("A").isEstimate()) {
			backLevel = ((double) counts[minCH] + (double) counts[maxCH]) * 0.5;
			getParameter("A").setValue(backLevel);
			textInfo.messageOutln("Estimated A = " + backLevel);
		}

		//sum up counts
		if (getParameter(AREA).isEstimate()) {
			area = 0.0;
			for (i = minCH; i <= (int) maxCH; i++) {

				area += (double) counts[i] - backLevel;

			}
			getParameter(AREA).setValue(area);
			textInfo.messageOutln("Estimated area = " + area);
		}

		//find width
		variance = 0.0;
		if (getParameter(WIDTH).isEstimate()) {
			for (i = minCH; i <= maxCH; i++) {
				distance = (double) (i) - centroid;

				variance += ((double) counts[i] / area) * (distance * distance);

			}
			sigma = Math.sqrt(variance);
			width = SIG_TO_FWHM * sigma;
			getParameter(WIDTH).setValue(width);
			textInfo.messageOutln("Estimated width = " + width);
		}
	}

	/**
	 * Overrides normal setParameters to make sure channels are in proper order.
	 * This Allows the fit limits and centroids to be clicked in any order.
	 */
	private void orderParameters() {
		final Matrix chVector = new Matrix(3, 1);
		chVector.element[0][0] = getParameter(FIT_LOW).getIntValue();
		chVector.element[1][0] = getParameter(CENTROID).getDoubleValue();
		chVector.element[2][0] = getParameter(FIT_HIGH).getIntValue();
		final Matrix sorted = chVector.sort();
		getParameter(FIT_LOW).setValue((int) sorted.element[0][0]);
		getParameter(CENTROID).setValue(sorted.element[1][0]);
		getParameter(FIT_HIGH).setValue((int) sorted.element[2][0]);
	}

	/**
	 * Calculates the gaussian with background at a given x.
	 * 
	 * @param x
	 *            value to calculate at
	 * @return value of function at x
	 */
	public double valueAt(double x) {
		diff = x - p(CENTROID);
		exp = Math.exp(-MAGIC_B * diff * diff / (p(WIDTH) * p(WIDTH)));

		double temp = p("A") + p("B") * diff + p("C") * diff * diff + p(AREA)
				/ p(WIDTH) * MAGIC_A * exp;
		return temp;
	}

	int getNumberOfSignals() {
		return 1;
	}

	double calculateSignal(int sig, int channel) {
		double rval = 0.0;

		if (sig == 0) {
			diff = channel - p(CENTROID);
			exp = Math.exp(-MAGIC_B * diff * diff / (p(WIDTH) * p(WIDTH)));
			rval = area.getDoubleValue() / width.getDoubleValue() * MAGIC_A
					* exp;
		}
		return rval;
	}

	boolean hasBackground() {
		return true;
	}

	double calculateBackground(int channel) {
		diff = channel - p(CENTROID);
		return p("A") + p("B") * diff + p("C") * diff * diff;
	}

	/**
	 * Evaluates derivative with respect to <code>parameterName</code> at
	 * <code>x</code>.
	 * 
	 * @param parName
	 *            the name of the parameter to differentiate with respect to
	 * @param x
	 *            value to evalueate at
	 * @return df( <code>x</code> )/d( <code>parameterName</code>) at x
	 */
	public double derivative(double x, String parName) {
		double temp;
		diff = x - p(CENTROID);
		exp = Math.exp(-MAGIC_B * diff * diff / (p(WIDTH) * p(WIDTH)));
		diff = x - p(CENTROID);
		if (parName.equals(AREA)) {
			temp = MAGIC_A / p(WIDTH) * exp;
		} else if (parName.equals(CENTROID)) {
			temp = MAGIC_2AB * p(AREA) * exp * diff
					/ (p(WIDTH) * p(WIDTH) * p(WIDTH)) - p("B") - 2 * p("C")
					* diff;
		} else if (parName.equals(WIDTH)) {
			temp = -MAGIC_A * p(AREA) * exp / (p(WIDTH) * p(WIDTH));
			temp = temp + MAGIC_2AB * p(AREA) * exp * diff * diff
					/ (p(WIDTH) * p(WIDTH) * p(WIDTH) * p(WIDTH));
		} else if (parName.equals("A")) {
			temp = 1.0;
		} else if (parName.equals("B")) {
			temp = diff;
		} else if (parName.equals("C")) {
			temp = diff * diff;
		} else { //not valid
			temp = 0.0;
			throw new IllegalArgumentException("Invalid derivative argument: "
					+ parName);
		}
		return temp;
	}

}