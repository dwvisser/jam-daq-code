/*
 *
 */
package jam.fit;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

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
public class MultipleGaussians extends NonLinearFit implements GaussianConstants{

	/**
	 * function <code>Parameter</code>--constant background term
	 */
	private Parameter paramA;

	/**
	 * function <code>Parameter</code>--linear background term
	 */
	private Parameter paramB;

	/**
	 * function <code>Parameter</code>--quadratic background term
	 */
	private Parameter paramC;

	private Parameter independentWidths;

	private static final String INDEP_WIDTH =
		"Vary Widths Independently (not yet implemented)";
	private static final String WIDTH_ESTIMATE = "Estimated width";

	private Parameter numPeaks;
	int npeak; //for local calculations

	private static final String NUM_PEAKS = "# Peaks to Fit";

	private static final int POSSIBLE_PEAKS = 5;
	private static final String[] S_AREA = new String[POSSIBLE_PEAKS];
	private static final String[] S_WIDTH = new String[POSSIBLE_PEAKS];
	private static final String[] S_CENTROID = new String[POSSIBLE_PEAKS];

	private Parameter widthEstimate;
	private Parameter[] area = new Parameter[POSSIBLE_PEAKS];
	private Parameter[] width = new Parameter[POSSIBLE_PEAKS];
	private Parameter[] centroid = new Parameter[POSSIBLE_PEAKS];

	private double[] dArea = new double[POSSIBLE_PEAKS];
	private double[] dWidth = new double[POSSIBLE_PEAKS];
	private double[] dCentroid = new double[POSSIBLE_PEAKS];

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
	 */
	public MultipleGaussians() {
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
		paramA =
			new Parameter(
				"A",
				Parameter.DOUBLE,
				Parameter.FIX,
				Parameter.ESTIMATE);
		paramA.setEstimate(true);
		paramB = new Parameter("B", Parameter.FIX);
		paramB.setFixed(true);
		paramC = new Parameter("C", Parameter.FIX);
		paramC.setFixed(true);
		addParameter(paramA);
		addParameter(paramB);
		addParameter(paramC);
		for (int i = 0; i < POSSIBLE_PEAKS; i++) {
			S_AREA[i] = "Area " + (i + 1);
			S_WIDTH[i] = "Width " + (i + 1);
			S_CENTROID[i] = "Centroid " + (i + 1);
			area[i] =
				new Parameter(
					S_AREA[i],
					Parameter.DOUBLE,
					Parameter.FIX,
					Parameter.ESTIMATE);
			area[i].setEstimate(true);
			centroid[i] =
				new Parameter(
					S_CENTROID[i],
					Parameter.DOUBLE,
					Parameter.FIX,
					Parameter.MOUSE);
			centroid[i].setValue(100.0);
			width[i] =
				new Parameter(S_WIDTH[i], Parameter.DOUBLE, Parameter.FIX, Parameter.ESTIMATE);
			width[i].setEstimate(true);
			width[i].setValue(10.0);
			addParameter(centroid[i]);
			addParameter(width[i]);
			addParameter(area[i]);
		}
	}

	/**
	 * If so requested, estimates A, Area, and Width.
	 */
	public void estimate()  {
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
				setParameter(S_WIDTH[i], width[1].getDoubleValue());
			}
		}
		final int _minCH = lo.getIntValue();
		final int _maxCH = hi.getIntValue();
		for (int i = 0; i < npeak; i++) {
			dCentroid[i] = centroid[i].getDoubleValue();
			dWidth[i] = width[i].getDoubleValue();
			dArea[i] = area[i].getDoubleValue();
		}
		/* estimated level of background */
		double backLevel = paramA.getDoubleValue();
		if (paramA.isEstimate()) {
			backLevel = (counts[_minCH] + counts[_maxCH]) * 0.5;
		}
		paramA.setValue(backLevel);
		textInfo.messageOutln("Estimated A = " + backLevel);
		/* estimate areas */
		for (int i = 0; i < npeak; i++) {
			if (width[i].isEstimate()){
				width[i].setValue(widthEstimate.getDoubleValue());
				dWidth[i]=widthEstimate.getDoubleValue();
				textInfo.messageOutln("Initial "+S_WIDTH[i]+" = "+dWidth[i]);
			}
			if (area[i].isEstimate()) {
				dArea[i] = 0.0;
				double rangeOffset = dWidth[i] * 0.5;
				int imin =
					Math.max((int) (dCentroid[i] - rangeOffset), _minCH);
				int imax =
					Math.min((int) (dCentroid[i] + rangeOffset), _maxCH);
				for (int j = imin; j <= imax; j++) {
					dArea[i] += counts[j] - backLevel;
				}
				dArea[i] *= 1.3144;
				//inverse of area under fwhm standard normal
				area[i].setValue(dArea[i]);
				textInfo.messageOutln(
					"Estimated " + S_AREA[i] + " = " + dArea[i]);
			}
		}
	}

	/**
	 * Overrides normal setParameters to make sure channels are in proper order.  This
	 * Allows the fit limits and centroids to be clicked in any order.
	 */
	private void orderParameters()  {
		final SortedSet list = new TreeSet();
		/* must use Double so Comparator interface works */
		list.add(new Double(lo.getIntValue()));
		list.add(new Double(hi.getIntValue()));
		final int n=numPeaks.getIntValue();
		for (int i = 0; i < n; i++) {
			list.add(new Double(centroid[i].getDoubleValue()));
		}
		final Iterator it = list.iterator();
		lo.setValue(((Number) it.next()).intValue());
		for (int i = 0; i < n; i++) {
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
		return dArea[signal] / dWidth[signal] * exp[signal];
	}

	private double lastSet = -1.0;
	private void setGlobalNumbers(double x) {
		if (x != lastSet) {
			rangeCenter = 0.5 * (lo.getDoubleValue() + hi.getDoubleValue());
			diffCenter = x - rangeCenter;
			npeak = numPeaks.getIntValue();
			for (int i = 0; i < npeak; i++) {
				dWidth[i] = width[i].getDoubleValue();
				dArea[i] = area[i].getDoubleValue();
				dCentroid[i] = centroid[i].getDoubleValue();
			}
			for (int i = 0; i < npeak; i++) {
				diff[i] = x - centroid[i].getDoubleValue();
			}
			peakSum = 0.0;
			for (int i = 0; i < npeak; i++) {
				exp[i] =
					Math.exp(
						-MAGIC_B * diff[i] * diff[i] / (dWidth[i] * dWidth[i]));
				peakSum += dArea[i] / dWidth[i] * exp[i];
			}
		}
	}

	/**
	 * Evaluates derivative with respect to <code>parameterName</code> at <code>x</code>.
	 *
	 * @param   parName   the name of the parameter to differentiate with respect to
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
				if (parName.equals(S_AREA[i])) {
					rval = MAGIC_A * exp[i] / dWidth[i];
					break;
				} else if (parName.equals(S_WIDTH[i])) {
					rval = MAGIC_A * dArea[i] * exp[i] / (dWidth[i] * dWidth[i]);
					rval *= 2
						* MAGIC_B
						* diff[i]
						* diff[i]
						/ (dWidth[i] * dWidth[i])
						- 1;
					break;
				} else if (parName.equals(S_CENTROID[i])) {
					rval =
						2
							* MAGIC_A
							* MAGIC_B
							* dArea[i]
							/ (dWidth[i] * dWidth[i] * dWidth[i])
							* diff[i]
							* exp[i];
					break;
				}
			}
		}
		return rval;
	}
}
