/*
 *
 */
package jam.fit;
import java.util.Vector;

/**
 * This abstract class uses <code>NonLinearFit</code> to fit a single gaussian peak with 
 * a background..
 * The background is a polynomial up to a quadradic term if desired. (MaxCH-MinCH)/2 is the
 * term
 * the polynomial is expanded in.
 *
 * @author  Dale Visser
 * @version 0.5, 8/31/98
 *
 * @see	    NonLinearFit
 */
public abstract class GaussianFit3 extends NonLinearFit {
	/**
	 */

	/**
	 * magic number for calculating
	 */
	private final double a = 0.93911;

	/**
	 * magic number for calculating
	 */
	private final double b = 2.77066;

	/**
	 * magic number for calculating
	 */
	private final double c = 5.20391;

	/**
	 * magic number for calculating
	 */
	static final double SIGMA_TO_FWHM = 2.354;

	/**
	 * name of <code>Parameter</code>--centroid of peak
	 */
	public static final String CENTROID1 = "Centroid 1";
	private double cent1;

	/**
	 * name of <code>Parameter</code>--width of peak
	 */
	public static final String WIDTH1 = "Width 1";
	private double Dwidth1;

	/**
	 * name of <code>Parameter</code>--area of peak
	 */
	public static final String AREA1 = "Area 1";
	private double Darea1;

	/**
	 * name of <code>Parameter</code>--centroid of peak
	 */
	public static final String CENTROID2 = "Centroid 2";
	private double cent2;

	/**
	 * name of <code>Parameter</code>--width of peak
	 */
	public static final String WIDTH2 = "Width 2";
	private double Dwidth2;

	/**
	 * name of <code>Parameter</code>--area of peak
	 */
	public static final String AREA2 = "Area 2";
	private double Darea2;

	/**
	 * name of <code>Parameter</code>--centroid of peak
	 */
	public static final String CENTROID3 = "Centroid 3";
	private double cent3;

	/**
	 * name of <code>Parameter</code>--width of peak
	 */
	public static final String WIDTH3 = "Width 3";
	private double Dwidth3;

	/**
	 * name of <code>Parameter</code>--area of peak
	 */
	public static final String AREA3 = "Area 3";
	private double Darea3;

	/**
	 * function <code>Parameter</code>--area of peak
	 */
	private Parameter area1;

	/**
	 * function <code>Parameter</code>--centroid of peak
	 */
	private Parameter centroid1;

	/**
	 * function <code>Parameter</code>--wodth of peak
	 */
	private Parameter width1;
	/**
	 * function <code>Parameter</code>--area of peak
	 */
	private Parameter area2;

	/**
	 * function <code>Parameter</code>--centroid of peak
	 */
	private Parameter centroid2;

	/**
	 * function <code>Parameter</code>--wodth of peak
	 */
	private Parameter width2;
	/**
	 * function <code>Parameter</code>--area of peak
	 */
	private Parameter area3;

	/**
	 * function <code>Parameter</code>--centroid of peak
	 */
	private Parameter centroid3;

	/**
	 * function <code>Parameter</code>--wodth of peak
	 */
	private Parameter width3;

	/**
	 * function <code>Parameter</code>--constant background term
	 */
	private Parameter A;

	/**
	 * function <code>Parameter</code>--linear background term
	 */
	private Parameter B;

	/**
	 * function <code>Parameter</code>--quadratic background term
	 */
	private Parameter C;

	private Parameter independentWidths;

	public static final String INDEP_WIDTH = "Vary Widths Independently";

	private Parameter numPeaks;

	public static final String NUM_PEAKS = "# Peaks to Fit";

	/**
	 * used for calculations
	 */
	private double diff1;
	private double diff2;
	private double diff3;

	/**
	 * used for calculations
	 */
	private double exp1;
	private double exp2;
	private double exp3;

	private double rangeCenter;

	/**
	 * Class constructor.
	 *
	 * @exception   FitException	    thrown if unrecoverable error occurs during instantiation
	 */
	public GaussianFit3() throws FitException {
		super("GaussianFit3");

		//background=new Parameter("bg",Parameter.TEXT,"Background: A+B(x-Centroid)+C(x-Centroid)^2");
		//equation=new Parameter("eq",Parameter.TEXT,"Peak: 2.354*Area/(Sqrt(2 pi)Width)*exp[-2.354^2(x-Centroid)^2/2 Width^2]");
		independentWidths = new Parameter(INDEP_WIDTH, Parameter.BOOLEAN);
		independentWidths.setValue(true);
		//numPeaks = new Parameter(NUM_PEAKS,Parameter.INT,Parameter.NO_ERROR);
		//numPeaks.setValue(1);
		area1 =
			new Parameter(
				AREA1,
				Parameter.DOUBLE,
				Parameter.FIX,
				Parameter.ESTIMATE);
		area1.setEstimate(true);
		centroid1 =
			new Parameter(
				CENTROID1,
				Parameter.DOUBLE,
				Parameter.FIX,
				Parameter.MOUSE);
		centroid1.setValue(100.0);
		width1 = new Parameter(WIDTH1, Parameter.DOUBLE, Parameter.FIX);
		width1.setValue(5.0);
		area2 =
			new Parameter(
				AREA2,
				Parameter.DOUBLE,
				Parameter.FIX,
				Parameter.ESTIMATE);
		area2.setValue(0.0);
		area2.setFix(true);
		centroid2 =
			new Parameter(
				CENTROID2,
				Parameter.DOUBLE,
				Parameter.FIX,
				Parameter.MOUSE);
		width2 = new Parameter(WIDTH2, Parameter.DOUBLE, Parameter.FIX);
		width2.setValue(5.0);
		area3 =
			new Parameter(
				AREA3,
				Parameter.DOUBLE,
				Parameter.FIX,
				Parameter.ESTIMATE);
		area3.setValue(0.0);
		area3.setFix(true);
		centroid3 =
			new Parameter(
				CENTROID3,
				Parameter.DOUBLE,
				Parameter.FIX,
				Parameter.MOUSE);
		width3 = new Parameter(WIDTH3, Parameter.DOUBLE, Parameter.FIX);
		width3.setValue(5.0);
		A =
			new Parameter(
				"A",
				Parameter.DOUBLE,
				Parameter.FIX,
				Parameter.ESTIMATE);
		A.setEstimate(true);
		B = new Parameter("B", Parameter.FIX);
		B.setFix(true);
		C = new Parameter("C", Parameter.FIX);
		C.setFix(true);

		//addParameter(numPeaks);
		addParameter(independentWidths);
		addParameter(centroid1);
		addParameter(centroid2);
		addParameter(centroid3);
		addParameter(width1);
		addParameter(width2);
		addParameter(width3);
		addParameter(area1);
		addParameter(area2);
		addParameter(area3);
		addParameter(A);
		//addParameter(B);
		//addParameter(C);					
	}

	/**
	 * If so requested, estimates A, Area, and Width.
	 *
	 * @exception   FitException	    thrown if unrecoverable error occurs during estimation
	 */
	public void estimate() throws FitException {

		double rangeOffset;
		//channel diff +/- from centroid to sum over for area

		int i, minCH, maxCH, peakHigh, imin, imax;
		double area, width, centroid, distance, variance, backLevel, intWidth;

		// First, sort clickable mouse entries.
		orderParameters();

		// Second, Process whether independent widths or not, and number of peaks.
		if (!independentWidths.getBooleanValue()) { //not independent
			setParameter(WIDTH2, width1.getDoubleValue());
			setParameter(WIDTH3, width1.getDoubleValue());
		}

		minCH = getParameter(FIT_LOW).getIntValue();
		maxCH = getParameter(FIT_HIGH).getIntValue();
		cent1 = getParameter(CENTROID1).getDoubleValue();
		cent2 = getParameter(CENTROID2).getDoubleValue();
		cent3 = getParameter(CENTROID3).getDoubleValue();
		Dwidth1 = getParameter(WIDTH1).getDoubleValue();
		Dwidth2 = getParameter(WIDTH2).getDoubleValue();
		Dwidth3 = getParameter(WIDTH3).getDoubleValue();
		backLevel = getParameter("A").getDoubleValue();
		Darea1 = getParameter(AREA1).getDoubleValue();
		Darea2 = getParameter(AREA2).getDoubleValue();
		Darea3 = getParameter(AREA3).getDoubleValue();

		//estimated level of background
		if (getParameter("A").isEstimate()) {
			backLevel = ((double) counts[minCH] + (double) counts[maxCH]) * 0.5;
		}

		//sum up counts in peak 1
		if (getParameter(AREA1).isEstimate()) {
			Darea1 = 0.0;
			rangeOffset = Dwidth1 / SIGMA_TO_FWHM * 3;
			//3 sigma gets 99.6% of error
			imin = (int) (cent1 - rangeOffset);
			imax = (int) (cent1 + rangeOffset);
			for (i = imin; i <= imax; i++) {
				Darea1 += (double) counts[i] - backLevel;
			}
		}

		//sum up counts in peak 2
		if (getParameter(AREA2).isEstimate()) {
			Darea2 = 0.0;
			rangeOffset = Dwidth2 / SIGMA_TO_FWHM * 3;
			//3 sigma gets 99.6% of error
			imin = (int) (cent2 - rangeOffset);
			imax = (int) (cent2 + rangeOffset);
			for (i = imin; i <= imax; i++) {
				Darea2 += (double) counts[i] - backLevel;
			}
		}

		//sum up counts in peak 3
		if (getParameter(AREA3).isEstimate()) {
			Darea3 = 0.0;
			rangeOffset = Dwidth3 / SIGMA_TO_FWHM * 3;
			//3 sigma gets 99.6% of error
			imin = (int) (cent3 - rangeOffset);
			imax = (int) (cent3 + rangeOffset);
			for (i = imin; i <= imax; i++) {
				Darea3 += (double) counts[i] - backLevel;
			}
		}
		getParameter(AREA1).setValue(Darea1);
		System.out.println("Estimated area1 = " + Darea1);
		getParameter(AREA1).setValue(Darea2);
		System.out.println("Estimated area2 = " + Darea2);
		getParameter(AREA1).setValue(Darea3);
		System.out.println("Estimated area3 = " + Darea3);
		getParameter("A").setValue(backLevel);
		System.out.println("Estimated A = " + backLevel);
	}

	/**
	 * Overrides normal setParameters to make sure channels are in proper order.  This
	 * Allows the fit limits and centroids to be clicked in any order.
	 */
	private void orderParameters() throws FitException {
		int i, rows, cols;
		Matrix chVector; //=new Matrix(5,1);
		Matrix sorted;

		rows = numPeaks.getIntValue() + 2;
		cols = 1;
		chVector = new Matrix(rows, cols);
		chVector.element[0][0] = getParameter(FIT_HIGH).getIntValue();
		chVector.element[1][0] = getParameter(FIT_LOW).getIntValue();
		chVector.element[2][0] = getParameter(CENTROID1).getDoubleValue();
		if (numPeaks.getIntValue() > 1)
			chVector.element[3][0] = getParameter(CENTROID2).getDoubleValue();
		if (numPeaks.getIntValue() > 2)
			chVector.element[4][0] = getParameter(CENTROID3).getDoubleValue();

		sorted = chVector.sort();

		getParameter(FIT_LOW).setValue((int) sorted.element[0][0]);
		getParameter(CENTROID1).setValue(sorted.element[1][0]);
		i = 2;
		if (numPeaks.getIntValue() > 1) {
			getParameter(CENTROID2).setValue(sorted.element[2][0]);
			i = 3;
		}
		if (numPeaks.getIntValue() > 2) {
			getParameter(CENTROID3).setValue(sorted.element[3][0]);
			i = 4;
		}
		getParameter(FIT_HIGH).setValue((int) sorted.element[i][0]);
	}

	/**
	 * Calculates the gaussian with background at a given x.
	 *
	 * @param	x   value to calculate at
	 * @return	    value of function at x
	 */
	public double valueAt(double x) {
		double y;

		rangeCenter = p(FIT_LOW) + p(FIT_HIGH) / 2.0;
		diff1 = x - p(CENTROID1);
		diff2 = x - p(CENTROID2);
		diff3 = x - p(CENTROID3);
		Dwidth1 = p(WIDTH1);
		Dwidth2 = p(WIDTH2);
		Dwidth3 = p(WIDTH3);
		y = x - rangeCenter;
		exp1 = Math.exp(-b * diff1 * diff1 / (Dwidth1 * Dwidth1));
		exp2 = Math.exp(-b * diff2 * diff2 / (Dwidth2 * Dwidth2));
		exp3 = Math.exp(-b * diff3 * diff3 / (Dwidth3 * Dwidth3));

		return p("A")
			+ p("B") * y
			+ p("C") * y * y
			+ p(AREA1) / Dwidth1 * a * exp1
			+ p(AREA2) / Dwidth2 * a * exp2
			+ p(AREA3) / Dwidth3 * a * exp3;
	}

	/**
	 * Evaluates derivative with respect to <code>parameterName</code> at <code>x</code>.
	 *
	 * @param   parameterName   the name of the parameter to differentiate with respect to
	 * @param   x		value to evalueate at
	 * @return			df(<code>x</code>)/d(<code>parameterName</code>) at x
	 */
	public double derivative(double x, String parName) {
		double temp;
		double y;

		rangeCenter = p(FIT_LOW) + p(FIT_HIGH) / 2.0;
		diff1 = x - p(CENTROID1);
		diff2 = x - p(CENTROID2);
		diff3 = x - p(CENTROID3);
		Dwidth1 = p(WIDTH1);
		Dwidth2 = p(WIDTH2);
		Dwidth3 = p(WIDTH3);
		y = x - rangeCenter;
		exp1 = Math.exp(-b * diff1 * diff1 / (Dwidth1 * Dwidth1));
		exp2 = Math.exp(-b * diff2 * diff2 / (Dwidth2 * Dwidth2));
		exp3 = Math.exp(-b * diff3 * diff3 / (Dwidth3 * Dwidth3));

		if (parName.equals(AREA1)) {
			temp = a / Dwidth1 * exp1;
		} else if (parName.equals(AREA2)) {
			temp = a / Dwidth2 * exp2;
		} else if (parName.equals(AREA3)) {
			temp = a / Dwidth3 * exp3;
		} else if (parName.equals(CENTROID1)) {
			temp = c * p(AREA1) * exp1 * diff1 / (Dwidth1 * Dwidth1 * Dwidth1);
		} else if (parName.equals(CENTROID2)) {
			temp = c * p(AREA2) * exp2 * diff2 / (Dwidth2 * Dwidth2 * Dwidth2);
		} else if (parName.equals(CENTROID3)) {
			temp = c * p(AREA3) * exp3 * diff3 / (Dwidth3 * Dwidth3 * Dwidth3);
		} else if (parName.equals(WIDTH1)) {
			temp = -a * p(AREA1) * exp1 / (Dwidth1 * Dwidth1);
			temp =
				temp
					+ c
						* p(AREA1)
						* exp1
						* diff1
						* diff1
						/ (Dwidth1 * Dwidth1 * Dwidth1 * Dwidth1);
		} else if (parName.equals(WIDTH2)) {
			temp = -a * p(AREA2) * exp1 / (Dwidth2 * Dwidth2);
			temp =
				temp
					+ c
						* p(AREA2)
						* exp2
						* diff2
						* diff2
						/ (Dwidth2 * Dwidth2 * Dwidth2 * Dwidth2);
		} else if (parName.equals(WIDTH3)) {
			temp = -a * p(AREA3) * exp3 / (Dwidth3 * Dwidth3);
			temp =
				temp
					+ c
						* p(AREA3)
						* exp3
						* diff3
						* diff3
						/ (Dwidth3 * Dwidth3 * Dwidth3 * Dwidth3);
		} else if (parName.equals("A")) {
			temp = 1.0;
		} else if (parName.equals("B")) {
			temp = y;
		} else if (parName.equals("C")) {
			temp = y * y;
		} else { //not valid
			temp = 0.0;
			System.err.println("Invalid derivative argument: " + parName);
		}
		return temp;
	}

	/**
	 * Set a parameter designated by name to a new value.  Overrides method in <code>NonLinearFit</code> to account for 
	 * the <code>independentWidths</code) state.
	 *
	 * @param	which	the name of the parameter
	 * @param	value	the value to assign
	 * @exception   FitException	    thrown if unrecoverable error occurs
	 */
	public void setParameter(String which, double value) throws FitException {
		if (independentWidths.getBooleanValue()) {
			getParameter(which).setValue(value);
		} else { //widths not independent
			if (which.equals(WIDTH2)) {
				width2.setValue(width1.getDoubleValue());
			} else if (which.equals(WIDTH3)) {
				width3.setValue(width1.getDoubleValue());
			} else { //all others
				getParameter(which).setValue(value);
			}
		}
	}

}
