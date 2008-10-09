package jam.data.peaks;

import java.util.ArrayList;
import java.util.List;

/**
 * Given sensitivity and width parameters, finds peaks in a given spectrum.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 2001-05-11
 */
public final class PeakFinder {

	private static PeakFinder instance = new PeakFinder();

	/**
	 * 
	 * @return the only instance
	 */
	static public PeakFinder getInstance() {
		return instance;
	}

	private PeakFinder() {
		super();
	}

	private transient double[] spectrum;

	private transient double sensitivity, width, sigma;

	/**
	 * Maximum separation in sigma between peaks to count them as being in the
	 * same multiplet.
	 */
	private static final double MAX_SEP = 1.3;

	/**
	 * Given a spectrum and search parameters, performs a digital filter peak
	 * search as specified in V. Hnatowicz et al in Comp Phys Comm 60 (1990)
	 * 111-125. Setting the sensitivity to a typical value of 3 gives a 3%
	 * chance for any peak found to be false.
	 * 
	 * @param data
	 *            spectrum to be searched
	 * @param _sensitivity
	 *            larger numbers (typical=3) require better defined peaks
	 * @param _width
	 *            typical FWHM of peaks in spectrum
	 * @return array of centroids
	 */
	public List<Double> getCentroids(final double[] data,
			final double _sensitivity, final double _width) {
		spectrum = data.clone();
		sensitivity = _sensitivity;
		width = _width;
		sigma = width / GaussianConstants.SIG_TO_FWHM;
		return getCentroids();
	}

	/**
	 * Workhorse for peak-finding.
	 * 
	 * @return an array of the multiplets found in the spectrum
	 */
	private List<Multiplet> peakFind() {
		final Multiplet peaks = Multiplet.createMultiplet();
		/* defined by Java spec to be zeroes initially */
		double[] sum1 = new double[spectrum.length];
		double[] sum2 = new double[spectrum.length];
		/* gives filter at limit < 0.005 filter at center */
		final int filterLimit = (int) Math.ceil(1.5 * width);
		double[] filter = new double[2 * filterLimit + 1];
		/* will contain the squares of filter's elements */
		double[] filter2 = new double[2 * filterLimit + 1];
		/* Create the filter and its square. */
		for (int i = 0; i < filter.length; i++) {
			final int iPrime = i - filterLimit;
			filter[i] = 2 * (sigma * sigma - iPrime * iPrime)
					/ (Math.sqrt(Math.PI) * sigma * sigma * sigma)
					* Math.exp(-(iPrime * iPrime) / (2.0 * sigma * sigma));
			filter2[i] = filter[i] * filter[i];
		}
		/* Run the filter on the spectrum. (Eqns 2 in article) */
		for (int i = filterLimit; i < spectrum.length - filterLimit; i++) {
			for (int j = 0; j < filter.length; j++) {
				final int diff = j - filterLimit;
				sum1[i] += filter[j] * spectrum[i - diff];
				sum2[i] += filter2[j] * spectrum[i - diff];
			}
		}
		// Build list of peak candidates
		for (int i = filterLimit + 1; i < spectrum.length - filterLimit - 1; i++) {
			if (sum1[i] > sensitivity * Math.sqrt(sum2[i])
					&& sum1[i] > sum1[i - 1] && sum1[i] > sum1[i + 1]) {// conditions
				// met,
				// calculate
				// centroid
				final double posn = (sum1[i - 1] * (i - 1) + sum1[i] * i + sum1[i + 1]
						* (i + 1))
						/ (sum1[i - 1] + sum1[i] + sum1[i + 1]);
				peaks.add(Peak.createPeak(posn, sum1[i], width));
			}
		}
		return breakUp(peaks);
	}

	private List<Multiplet> breakUp(final Multiplet peaks) {
		final List<Multiplet> multiplets = new ArrayList<Multiplet>();
		Multiplet currMult = Multiplet.createMultiplet();
		for (int i = 0; i < peaks.size(); i++) {
			if (i == 0
					|| (peaks.get(i).getPosition() - peaks.get(i - 1)
							.getPosition()) > (MAX_SEP * width)) {
				currMult = Multiplet.createMultiplet();
				multiplets.add(currMult);
			}
			currMult.add(peaks.get(i));
		}
		// create return value array and correct peak positions within
		// multiplets
		final int len = multiplets.size();
		if (len > 1) {
			for (int i = 0; i < len; i++) {
				final Multiplet mult_i = multiplets.get(i);
				final int npeaks = mult_i.size();
				if (npeaks > 1) {
					for (int j = 0; j < npeaks; j++) {
						mult_i.set(j, correctPeak(mult_i, j));
					}
				}
			}
		}
		renormalize(multiplets);// now renormalize multiplet amplitudes
		return multiplets;
	}

	private void renormalize(final List<Multiplet> multiplets) {
		for (int m = 0; m < multiplets.size(); m++) {
			double trueArea = 0.0;
			double estArea = 0.0;
			final Multiplet currMult = multiplets.get(m);
			for (int p = 0; p < currMult.size(); p++) {
				estArea += currMult.get(p).getArea();
			}
			for (int ch = (int) Math.round(currMult.get(0).getPosition()
					- width * MAX_SEP); ch < (int) Math.round(currMult.get(
					currMult.size() - 1).getPosition()
					+ width * MAX_SEP); ch++) {
				trueArea += spectrum[ch];
			}
			final double factor = trueArea / estArea;
			for (int p = 0; p < currMult.size(); p++) {
				final Peak peak = currMult.get(p);
				peak.setArea(factor * peak.getArea());
			}
		}
	}

	private Peak correctPeak(final Multiplet src, final int index) {
		final Peak thisPeak = src.get(index);
		final double correction;
		if (index == 0) {
			final Peak nextPeak = src.get(index + 1);
			final double dNext = thisPeak.getPosition()
					- nextPeak.getPosition();
			final double kNext = getK(dNext);
			correction = nextPeak.getArea() * kNext / thisPeak.getArea();
		} else if (index == (src.size() - 1)) {
			final Peak lastPeak = src.get(index - 1);
			final double dLast = thisPeak.getPosition()
					- lastPeak.getPosition();
			final double kLast = getK(dLast);
			correction = lastPeak.getArea() * kLast / thisPeak.getArea();
		} else {// in the middle somewhere
			final Peak nextPeak = src.get(index + 1);
			final double dNext = thisPeak.getPosition()
					- nextPeak.getPosition();
			final double kNext = getK(dNext);
			final Peak lastPeak = src.get(index - 1);
			final double dLast = thisPeak.getPosition()
					- lastPeak.getPosition();
			final double kLast = getK(dLast);
			correction = (nextPeak.getArea() * kNext + lastPeak.getArea()
					* kLast)
					/ thisPeak.getArea();
		}
		return thisPeak.offset(correction);
	}

	private double getK(final double diff) {
		final double diffSq = diff * diff;
		final double sigmaSq = sigma * sigma;
		final double kval = diff * Math.exp(-diffSq / (4.0 * sigmaSq))
				* (1.0 - diffSq / (6.0 * sigmaSq));
		return kval;
	}

	private List<Double> getCentroids() {
		final List<Multiplet> multiplets = peakFind();
		final List<Double> rval = new ArrayList<Double>();
		for (Multiplet multiplet : multiplets) {
			rval.addAll(multiplet.getAllCentroids());
		}
		return rval;
	}

}