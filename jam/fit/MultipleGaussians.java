/*
 *
 */
package jam.fit;
import java.util.*;

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
public class MultipleGaussians extends NonLinearFit {
	/**
	 */

	/**
	 * Coefficient for proper normalization of gaussians.
	 */
	static private final double a = 2.0 * Math.sqrt(Math.log(2.0) / Math.PI);

	/**
	 * Factor in exponential terms to properly relate width to sigma.
	 */
	static private final double b = 4.0 * Math.log(2.0);

	/**
	 * Ratio of FWHM to sigma for a gaussian.
	 */
	static final double SIGMA_TO_FWHM = 2.0 * Math.sqrt(2.0 * Math.log(2.0));

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

	public static final String INDEP_WIDTH =
		"Vary Widths Independently (not yet implemented)";
	public static final String WIDTH_ESTIMATE = "Estimated width";

	private Parameter numPeaks;
	int npeak; //for local calculations

	public static final String NUM_PEAKS = "# Peaks to Fit";

	private static final int POSSIBLE_PEAKS = 5;
	public static String[] s_area = new String[POSSIBLE_PEAKS];
	public static String[] s_width = new String[POSSIBLE_PEAKS];
	public static String[] s_centroid = new String[POSSIBLE_PEAKS];

	private Parameter widthEstimate;
	private Parameter[] area = new Parameter[POSSIBLE_PEAKS];
	private Parameter[] width = new Parameter[POSSIBLE_PEAKS];
	private Parameter[] centroid = new Parameter[POSSIBLE_PEAKS];

	private double[] d_area = new double[POSSIBLE_PEAKS];
	private double[] d_width = new double[POSSIBLE_PEAKS];
	private double[] d_centroid = new double[POSSIBLE_PEAKS];

	/**
	 * used for calculations
	 */
	private double[] diff = new double[POSSIBLE_PEAKS];

	/**
	 * used for calculations
	 */
	private double[] exp = new double[POSSIBLE_PEAKS];

	/**
	 * Background terms calculated as powers of x-rangeCenter.
	 */
	private double rangeCenter, diffCenter;

	/**
	 * different components of channel count
	 */
	private double peakSum;

	/**
	 * Class constructor.
	 *
	 * @exception   FitException	    thrown if unrecoverable error occurs during instantiation
	 */
	public MultipleGaussians() throws FitException {
		super("MultipleGaussians");

		Parameter background = new Parameter("Background: ", Parameter.TEXT);
		background.setValue("A+B(x-Centroid)+C(x-Centroid)\u00b2");
		Parameter equation = new Parameter("Peak: ", Parameter.TEXT);
		equation.setValue(
			"2.354\u2219Area/(\u221a(2\u03c0)Width)\u2219exp[-2.354\u00b2(x-Centroid)\u00b2/(2 Width\u00b2)]");
		addParameter(background);
		addParameter(equation);
		this.widthEstimate =
			new Parameter(WIDTH_ESTIMATE, Parameter.DOUBLE, Parameter.KNOWN,Parameter.KNOWN);
		widthEstimate.setValue(10.0);
		addParameter(widthEstimate);
		independentWidths = new Parameter(INDEP_WIDTH, Parameter.BOOLEAN);
		independentWidths.setValue(true);
		numPeaks = new Parameter(NUM_PEAKS, Parameter.INT, Parameter.KNOWN);
		numPeaks.setValue(POSSIBLE_PEAKS);
		addParameter(numPeaks);
		addParameter(independentWidths);
		A =
			new Parameter(
				"A",
				Parameter.DOUBLE,
				Parameter.FIX,
				Parameter.ESTIMATE);
		A.setEstimate(true);
		B = new Parameter("B", Parameter.FIX);
		B.setFixed(true);
		C = new Parameter("C", Parameter.FIX);
		C.setFixed(true);
		addParameter(A);
		addParameter(B);
		addParameter(C);
		for (int i = 0; i < POSSIBLE_PEAKS; i++) {
			s_area[i] = "Area " + (i + 1);
			s_width[i] = "Width " + (i + 1);
			s_centroid[i] = "Centroid " + (i + 1);
			area[i] =
				new Parameter(
					s_area[i],
					Parameter.DOUBLE,
					Parameter.FIX,
					Parameter.ESTIMATE);
			area[i].setEstimate(true);
			centroid[i] =
				new Parameter(
					s_centroid[i],
					Parameter.DOUBLE,
					Parameter.FIX,
					Parameter.MOUSE);
			centroid[i].setValue(100.0);
			width[i] =
				new Parameter(s_width[i], Parameter.DOUBLE, Parameter.FIX, Parameter.ESTIMATE);
			width[i].setEstimate(true);
			width[i].setValue(10.0);
			addParameter(centroid[i]);
			addParameter(width[i]);
			addParameter(area[i]);
		}
	}

	/**
	 * If so requested, estimates A, Area, and Width.
	 *
	 * @exception   FitException	    thrown if unrecoverable error occurs during estimation
	 */
	public void estimate() throws FitException {
		/* First, sort clickable mouse entries */
		orderParameters();
		npeak = this.numPeaks.getIntValue();
		for (int i = npeak; i < POSSIBLE_PEAKS; i++) {
			area[i].setValue(0.0);
			area[i].setFixed(true);
			this.centroid[i].setFixed(true);
			this.width[i].setFixed(true);
		}
		/* Second, Process whether independent widths or not, and number of peaks. */
		if (!independentWidths.getBooleanValue()) { //not independent; 
			for (int i = 1; i < npeak; i++) {
				setParameter(s_width[i], width[1].getDoubleValue());
			}
		}
		int _minCH = lo.getIntValue();
		int _maxCH = hi.getIntValue();
		//double [] local_centroid=new double[POSSIBLE_PEAKS];
		for (int i = 0; i < npeak; i++) {
			d_centroid[i] = centroid[i].getDoubleValue();
			d_width[i] = width[i].getDoubleValue();
			d_area[i] = area[i].getDoubleValue();
		}
		/* estimated level of background */
		double backLevel = A.getDoubleValue();
		if (A.isEstimate()) {
			backLevel = (counts[_minCH] + counts[_maxCH]) * 0.5;
		}
		A.setValue(backLevel);
		System.out.println("Estimated A = " + backLevel);
		/* estimate areas */
		//double sigmaToScan = 3; //3 sigma gets 99.6% of peak
		for (int i = 0; i < npeak; i++) {
			if (width[i].isEstimate()){
				width[i].setValue(widthEstimate.getDoubleValue());
				d_width[i]=widthEstimate.getDoubleValue();
				System.out.println("Initial "+s_width[i]+" = "+d_width[i]);
			}
			if (area[i].isEstimate()) {
				d_area[i] = 0.0;
				double rangeOffset = d_width[i] * 0.5;
				int imin =
					Math.max((int) (d_centroid[i] - rangeOffset), _minCH);
				int imax =
					Math.min((int) (d_centroid[i] + rangeOffset), _maxCH);
				for (int j = imin; j <= imax; j++) {
					d_area[i] += (double) counts[j] - backLevel;
				}
				d_area[i] *= 1.3144;
				//inverse of area under fwhm standard normal
				area[i].setValue(d_area[i]);
				System.out.println(
					"Estimated " + s_area[i] + " = " + d_area[i]);
			}
		}
	}

	/**
	 * Overrides normal setParameters to make sure channels are in proper order.  This
	 * Allows the fit limits and centroids to be clicked in any order.
	 */
	private void orderParameters() throws FitException {
		SortedSet list = new TreeSet();
		list.add(new Double(lo.getIntValue()));
		list.add(new Double(hi.getIntValue()));
		for (int i = 0; i < POSSIBLE_PEAKS; i++) {
			list.add(new Double(centroid[i].getDoubleValue()));
		}
		Iterator it = list.iterator();
		lo.setValue(((Number) it.next()).intValue());
		for (int i = 0; i < POSSIBLE_PEAKS; i++) {
			centroid[i].setValue(((Number) it.next()).doubleValue());
		}
		hi.setValue(((Number) it.next()).intValue());
	}

	/**
	 * Calculates the gaussian with background at a given x.
	 *
	 * @param	x   value to calculate at
	 * @return	    value of function at x
	 */
	public double valueAt(double x) {
		setGlobalNumbers(x);
		return p("A")
			+ p("B") * diffCenter
			+ p("C") * diffCenter * diffCenter
			+ peakSum;
	}

	boolean hasBackground() {
		return true;
	}

	double calculateBackground(int channel) {
		setGlobalNumbers(channel);
		return p("A") + p("B") * diffCenter + p("C") * diffCenter * diffCenter;
	}

	int getNumberOfSignals() {
		return numPeaks.getIntValue();
	}

	double calculateSignal(int signal, int channel) {
		setGlobalNumbers(channel);
		return d_area[signal] / d_width[signal] * exp[signal];
	}

	private double lastSet = -1.0;
	private void setGlobalNumbers(double x) {
		if (x != lastSet) {
			rangeCenter = 0.5 * (lo.getDoubleValue() + hi.getDoubleValue());
			diffCenter = x - rangeCenter;
			npeak = numPeaks.getIntValue();
			for (int i = 0; i < npeak; i++) {
				d_width[i] = width[i].getDoubleValue();
				d_area[i] = area[i].getDoubleValue();
				d_centroid[i] = centroid[i].getDoubleValue();
			}
			for (int i = 0; i < npeak; i++) {
				diff[i] = x - centroid[i].getDoubleValue();
			}
			peakSum = 0.0;
			for (int i = 0; i < npeak; i++) {
				exp[i] =
					Math.exp(
						-b * diff[i] * diff[i] / (d_width[i] * d_width[i]));
				peakSum += d_area[i] / d_width[i] * exp[i];
			}
		}
	}

	/**
	 * Evaluates derivative with respect to <code>parameterName</code> at <code>x</code>.
	 *
	 * @param   parameterName   the name of the parameter to differentiate with respect to
	 * @param   x		value to evalueate at
	 * @return			df(<code>x</code>)/d(<code>parameterName</code>) at x
	 */
	public double derivative(double x, String parName) {
		double rval = 0.0;
		setGlobalNumbers(x);
		if (parName.equals("A")) {
			rval = 1.0;
		} else if (parName.equals("B")) {
			rval = diffCenter;
		} else if (parName.equals("C")) {
			rval = diffCenter * diffCenter;
		} else {
			for (int i = 0; i < numPeaks.getIntValue(); i++) {
				if (parName.equals(s_area[i])) {
					rval = a * exp[i] / d_width[i];
					break;
				} else if (parName.equals(s_width[i])) {
					rval = a * d_area[i] * exp[i] / (d_width[i] * d_width[i]);
					rval *= 2
						* b
						* diff[i]
						* diff[i]
						/ (d_width[i] * d_width[i])
						- 1;
					break;
				} else if (parName.equals(s_centroid[i])) {
					rval =
						2
							* a
							* b
							* d_area[i]
							/ (d_width[i] * d_width[i] * d_width[i])
							* diff[i]
							* exp[i];
					break;
				}
			}
		}
		return rval;
	}

	/**
	 * Set a parameter designated by name to a new value.  Overrides method in <code>NonLinearFit</code> to account for 
	 * the <code>independentWidths</code) state.
	 *
	 * @param	which	the name of the parameter
	 * @param	value	the value to assign
	 * @exception   FitException	    thrown if unrecoverable error occurs
	 */
	/*public void setParameter(String which, double value) throws FitException {
		if (independentWidths.getBooleanValue()) {
			getParameter(which).setValue(value);
		} else { //widths not independent
			if (which.equals(WIDTH2)) {
				width2.setValue(width1.getDoubleValue());
			} else if (which.equals(WIDTH3)) {
				width3.setValue(width1.getDoubleValue());
			} else {
				//all others
				getParameter(which).setValue(value);
			}
		}
	}*/

}
