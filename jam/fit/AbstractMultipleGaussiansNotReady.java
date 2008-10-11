/*
 *
 */
package jam.fit;

import static jam.data.peaks.GaussianConstants.MAGIC_A;
import static jam.data.peaks.GaussianConstants.MAGIC_B;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This abstract class uses <code>NonLinearFit</code> to fit a single gaussian
 * peak with a background.. The background is a polynomial up to a quadradic
 * term if desired. (MaxCH-MinCH)/2 is the term the polynomial is expanded in.
 * 
 * @author Dale Visser
 * @version 0.5, 8/31/98
 * 
 * @see AbstractNonLinearFit
 */
@SuppressWarnings("unchecked")
public abstract class AbstractMultipleGaussiansNotReady extends AbstractNonLinearFit {

	/**
	 * function <code>Parameter</code>--constant background term
	 */
	private transient final Parameter<Double> paramA;

	private transient final Parameter<Boolean> independentWidths;

	private static final String INDEP_WIDTH = "Vary Widths Independently (not yet implemented)";
	private static final String WIDTH_ESTIMATE = "Estimated width";

	private transient final Parameter<Integer> numPeaks;
	private transient int npeak; // for local calculations

	private static final String NUM_PEAKS = "# Peaks to Fit";

	private static final int POSSIBLE_PEAKS = 5;
	private static final String[] S_AREA = new String[POSSIBLE_PEAKS];
	private static final String[] S_WIDTH = new String[POSSIBLE_PEAKS];
	private static final String[] S_CENTROID = new String[POSSIBLE_PEAKS];

	private transient final Parameter<Double> widthEstimate;
	private transient final Parameter<Double>[] area = new Parameter[POSSIBLE_PEAKS];
	private transient final Parameter<Double>[] width = new Parameter[POSSIBLE_PEAKS];
	private transient final Parameter<Double>[] centroid = new Parameter[POSSIBLE_PEAKS];

	private transient final double[] dArea = new double[POSSIBLE_PEAKS];
	private transient final double[] dWidth = new double[POSSIBLE_PEAKS];
	private transient final double[] dCentroid = new double[POSSIBLE_PEAKS];

	/**
	 * used for calculations
	 */
	private transient final double[] diff = new double[POSSIBLE_PEAKS];

	/**
	 * used for calculations
	 */
	private transient final double[] exp = new double[POSSIBLE_PEAKS];

	/**
	 * Background terms calculated as powers of x-rangeCenter.
	 */
	private transient double diffCenter;

	/**
	 * different components of channel count
	 */
	private transient double peakSum;

	/**
	 * Class constructor.
	 */
	public AbstractMultipleGaussiansNotReady() {
		super("MultipleGaussians");
		final Parameter background = new Parameter("Background: ",
				Parameter.TEXT);
		background.setValue("A+B(x-Centroid)+C(x-Centroid)\u00b2");
		final Parameter equation = new Parameter("Peak: ", Parameter.TEXT);
		equation
				.setValue("2.354\u2219Area/(\u221a(2\u03c0)Width)\u2219exp[-2.354\u00b2(x-Centroid)\u00b2/(2 Width\u00b2)]");
		parameters.add(background);
		parameters.add(equation);
		this.widthEstimate = new Parameter(WIDTH_ESTIMATE, Parameter.DOUBLE,
				Parameter.KNOWN, Parameter.KNOWN);
		widthEstimate.setValue(10.0);
		parameters.add(widthEstimate);
		independentWidths = new Parameter(INDEP_WIDTH, Parameter.BOOLEAN);
		independentWidths.setValue(true);
		numPeaks = new Parameter(NUM_PEAKS, Parameter.INT, Parameter.KNOWN);
		numPeaks.setValue(POSSIBLE_PEAKS);
		parameters.add(numPeaks);
		parameters.add(independentWidths);
		paramA = new Parameter("A", Parameter.DOUBLE, Parameter.FIX,
				Parameter.ESTIMATE);
		paramA.setEstimate(true);
		/**
		 * function <code>Parameter</code>--linear background term
		 */
		final Parameter paramB = new Parameter("B", Parameter.FIX);
		paramB.setFixed(true);
		/**
		 * function <code>Parameter</code>--quadratic background term
		 */
		final Parameter paramC = new Parameter("C", Parameter.FIX);
		paramC.setFixed(true);
		parameters.add(paramA);
		parameters.add(paramB);
		parameters.add(paramC);
		for (int i = 0; i < POSSIBLE_PEAKS; i++) {
			addPeakParameters(i);
		}
	}

	/**
	 * @param index
	 */
	private void addPeakParameters(final int index) {
		S_AREA[index] = "Area " + (index + 1);
		S_WIDTH[index] = "Width " + (index + 1);
		S_CENTROID[index] = "Centroid " + (index + 1);
		area[index] = new Parameter(S_AREA[index], Parameter.DOUBLE,
				Parameter.FIX, Parameter.ESTIMATE);
		area[index].setEstimate(true);
		centroid[index] = new Parameter(S_CENTROID[index], Parameter.DOUBLE,
				Parameter.FIX, Parameter.MOUSE);
		centroid[index].setValue(100.0);
		width[index] = new Parameter(S_WIDTH[index], Parameter.DOUBLE,
				Parameter.FIX, Parameter.ESTIMATE);
		width[index].setEstimate(true);
		width[index].setValue(10.0);
		parameters.add(centroid[index]);
		parameters.add(width[index]);
		parameters.add(area[index]);
	}

	/**
	 * If so requested, estimates A, Area, and Width.
	 */
	public void estimate() {
		/* First, sort clickable mouse entries */
		orderParameters();
		npeak = this.numPeaks.getValue();
		fixPeaksToZero();
		/*
		 * Second, Process whether independent widths or not, and number of
		 * peaks.
		 */
		if (!independentWidths.getValue()) { // not independent;
			for (int i = 1; i < npeak; i++) {
				setParameter(S_WIDTH[i], width[1].getValue());
			}
		}
		final int _minCH = lowChannel.getValue();
		final int _maxCH = highChannel.getValue();
		for (int i = 0; i < npeak; i++) {
			dCentroid[i] = centroid[i].getValue();
			dWidth[i] = width[i].getValue();
			dArea[i] = area[i].getValue();
		}
		/* estimated level of background */
		double backLevel = paramA.getValue();
		if (paramA.isEstimate()) {
			backLevel = (counts[_minCH] + counts[_maxCH]) * 0.5;
		}
		paramA.setValue(backLevel);
		textInfo.messageOutln("Estimated A = " + backLevel);
		/* estimate areas */
		for (int i = 0; i < npeak; i++) {
			if (width[i].isEstimate()) {
				width[i].setValue(widthEstimate.getValue());
				dWidth[i] = widthEstimate.getValue();
				textInfo.messageOutln("Initial " + S_WIDTH[i] + " = "
						+ dWidth[i]);
			}
			if (area[i].isEstimate()) {
				dArea[i] = 0.0;
				final double rangeOffset = dWidth[i] * 0.5;
				final int imin = Math.max((int) (dCentroid[i] - rangeOffset),
						_minCH);
				final int imax = Math.min((int) (dCentroid[i] + rangeOffset),
						_maxCH);
				for (int j = imin; j <= imax; j++) {
					dArea[i] += counts[j] - backLevel;
				}
				dArea[i] *= 1.3144;
				// inverse of area under fwhm standard normal
				area[i].setValue(dArea[i]);
				textInfo.messageOutln("Estimated " + S_AREA[i] + " = "
						+ dArea[i]);
			}
		}
	}

	/**
	 * 
	 */
	private void fixPeaksToZero() {
		for (int i = npeak; i < POSSIBLE_PEAKS; i++) {
			area[i].setValue(0.0);
			area[i].setFixed(true);
			this.centroid[i].setFixed(true);
			this.width[i].setFixed(true);
		}
	}

	/**
	 * Overrides normal setParameters to make sure channels are in proper order.
	 * This Allows the fit limits and centroids to be clicked in any order.
	 */
	private void orderParameters() {
		final SortedSet<Double> list = new TreeSet<Double>();
		/* must use Double so Comparator interface works */
		list.add((double) lowChannel.getValue());
		list.add((double) highChannel.getValue());
		final int npeaks = numPeaks.getValue();
		for (int i = 0; i < npeaks; i++) {
			list.add(centroid[i].getValue());
		}
		final Iterator<Double> iter = list.iterator();
		lowChannel.setValue(iter.next().intValue());
		for (int i = 0; i < npeaks; i++) {
			centroid[i].setValue(iter.next());
		}
		highChannel.setValue(iter.next().intValue());
	}

	/**
	 * Calculates the gaussian with background at a given x.
	 * 
	 * @param xval
	 *            value to calculate at
	 * @return value of function at x
	 */
	@Override
	public double valueAt(final double xval) {
		setGlobalNumbers(xval);
		return getValue("A") + getValue("B") * diffCenter + getValue("C")
				* diffCenter * diffCenter + peakSum;
	}

	public boolean hasBackground() {
		return true;
	}

	public double calculateBackground(final int channel) {
		setGlobalNumbers(channel);
		return getValue("A") + getValue("B") * diffCenter + getValue("C")
				* diffCenter * diffCenter;
	}

	public int getNumberOfSignals() {
		return numPeaks.getValue();
	}

	public double calculateSignal(final int signal, final int channel) {
		setGlobalNumbers(channel);
		return dArea[signal] / dWidth[signal] * exp[signal];
	}

	private static final double LAST_SET = -1.0;

	private void setGlobalNumbers(final double xval) {
		if (xval != LAST_SET) {
			final double rangeCenter = 0.5 * (lowChannel.getValue() + highChannel
					.getValue());
			diffCenter = xval - rangeCenter;
			npeak = numPeaks.getValue();
			for (int i = 0; i < npeak; i++) {
				dWidth[i] = width[i].getValue();
				dArea[i] = area[i].getValue();
				dCentroid[i] = centroid[i].getValue();
			}
			for (int i = 0; i < npeak; i++) {
				diff[i] = xval - centroid[i].getValue();
			}
			peakSum = 0.0;
			for (int i = 0; i < npeak; i++) {
				exp[i] = Math.exp(-MAGIC_B * diff[i] * diff[i]
						/ (dWidth[i] * dWidth[i]));
				peakSum += dArea[i] / dWidth[i] * exp[i];
			}
		}
	}

	/**
	 * Evaluates derivative with respect to <code>parameterName</code> at
	 * <code>x</code>.
	 * 
	 * @param parName
	 *            the name of the parameter to differentiate with respect to
	 * @param xval
	 *            value to evaluate at
	 * @return df(<code>x</code>)/d(<code>parameterName</code>) at x
	 */
	@Override
	public double derivative(final double xval, final String parName) {
		double rval = 0.0;
		setGlobalNumbers(xval);
		if ("A".equals(parName)) {
			rval = 1.0;
		} else if ("B".equals(parName)) {
			rval = diffCenter;
		} else if ("C".equals(parName)) {
			rval = diffCenter * diffCenter;
		} else {
			for (int i = 0; i < numPeaks.getValue(); i++) {
				if (parName.equals(S_AREA[i])) {
					rval = MAGIC_A * exp[i] / dWidth[i];
					break;
				} else if (parName.equals(S_WIDTH[i])) {
					rval = MAGIC_A * dArea[i] * exp[i]
							/ (dWidth[i] * dWidth[i]);
					rval *= 2 * MAGIC_B * diff[i] * diff[i]
							/ (dWidth[i] * dWidth[i]) - 1;
					break;
				} else if (parName.equals(S_CENTROID[i])) {
					rval = 2 * MAGIC_A * MAGIC_B * dArea[i]
							/ (dWidth[i] * dWidth[i] * dWidth[i]) * diff[i]
							* exp[i];
					break;
				}
			}
		}
		return rval;
	}
}
