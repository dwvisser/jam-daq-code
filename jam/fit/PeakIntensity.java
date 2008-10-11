/*
 *
 */
package jam.fit;

/**
 * Takes 4 channels as input...limits of background, and limits of peak. Returns
 * a background level and peak intensity.
 * 
 * @author Dale Visser
 * @author Carrie Rowland
 * @version 28 July 2002
 */
public class PeakIntensity extends AbstractFit {

	/**
	 * input <code>Parameter</code>
	 */
	private transient final Parameter<Integer> lowChannel = new Parameter<Integer>(
			"Low Channel", Parameter.MOUSE, Parameter.NO_OUTPUT, Parameter.INT,
			Parameter.KNOWN);

	private transient final Parameter<Integer> highChannel = new Parameter<Integer>(
			"High Channel", Parameter.MOUSE, Parameter.NO_OUTPUT,
			Parameter.INT, Parameter.KNOWN);

	private transient final Parameter<Integer> lowPeak = new Parameter<Integer>(
			"Low Peak", Parameter.MOUSE, Parameter.NO_OUTPUT, Parameter.INT,
			Parameter.KNOWN);

	private transient final Parameter<Integer> highPeak = new Parameter<Integer>(
			"High Peak", Parameter.MOUSE, Parameter.NO_OUTPUT, Parameter.INT,
			Parameter.KNOWN);

	/**
	 * function <code>Parameter</code>--constant background term
	 */
	private transient final Parameter<Double> paramA, paramB;

	private transient final Parameter<Double> paramPeakArea, paramPeakCentroid;

	/**
	 * Class constructor.
	 */
	public PeakIntensity() {
		super("Peak Intensity");
		final Parameter<String> comment = new Parameter<String>("Comment",
				Parameter.TEXT);
		comment.setValue("Checking \"Fixed\" on Slope fixes the value to 0.");
		parameters.add(lowChannel);
		parameters.add(highChannel);
		parameters.add(lowPeak);
		parameters.add(highPeak);
		paramA = new Parameter<Double>("Constant", Parameter.DOUBLE);
		parameters.add(paramA);
		paramB = new Parameter<Double>("Slope", Parameter.DOUBLE, Parameter.FIX);
		parameters.add(paramB);
		parameters.add(comment);
		paramPeakArea = new Parameter<Double>("Peak Area", Parameter.DOUBLE);
		parameters.add(paramPeakArea);
		paramPeakCentroid = new Parameter<Double>("Peak Centroid",
				Parameter.DOUBLE);
		parameters.add(paramPeakCentroid);
	}

	/**
	 * 
	 */
	public void estimate() {
		// not used
	}

	/**
	 * Performs the calibration fit.
	 * 
	 * @return message for fit dialog
	 * @exception FitException
	 *                thrown if something goes wrong in the fit
	 */
	public String doFit() throws FitException {
		double peakArea, totalArea, bkgdArea;
		int chLow = this.lowChannel.getValue();
		int chHigh = this.highChannel.getValue();
		int peakLow = this.lowPeak.getValue();
		int peakHigh = this.highPeak.getValue();
		final int[] before = { chLow, peakLow, peakHigh, chHigh };
		java.util.Arrays.sort(before);
		chLow = before[0];
		peakLow = before[1];
		peakHigh = before[2];
		chHigh = before[3];
		lowChannel.setValue(chLow);
		lowPeak.setValue(peakLow);
		highPeak.setValue(peakHigh);
		highChannel.setValue(chHigh);
		// sum = 0.0;
		// sumX = 0.0;
		// sumY = 0.0;
		// sumYY = 0.0;
		if (paramB.isFixed()) {// flat background
			fitFlatBackground(chLow, chHigh, peakLow, peakHigh);
		} else {// fit a regression line
			fitSlopedBackground(chLow, chHigh, peakLow, peakHigh);
		}
		totalArea = 0.0;
		bkgdArea = 0.0;
		for (int i = peakLow; i <= peakHigh; i++) {
			totalArea += counts[i];
			bkgdArea += calculate(i);
		}
		peakArea = totalArea - bkgdArea;
		double centroid = 0.0;
		for (int i = peakLow; i <= peakHigh; i++) {
			centroid += i * (counts[i] - calculate(i)) / peakArea;
		}
		final double peakError = Math.sqrt(totalArea);
		double variance = 0.0;
		for (int i = peakLow; i <= peakHigh; i++) {
			final double distance = i - centroid;
			variance += (counts[i] - calculate(i)) / peakArea * distance
					* distance;
		}
		variance /= peakHigh - peakLow + 1.0;
		paramPeakArea.setValue(peakArea, peakError);
		paramPeakCentroid.setValue(centroid, Math.sqrt(variance));
		lowerLimit = chLow;
		upperLimit = chHigh;
		residualOption = false;
		return "Done.";
	}

	/**
	 * @param sum
	 * @param sumX
	 * @param sumY
	 * @param chLow
	 * @param chHigh
	 * @param peakLow
	 * @param peakHigh
	 */
	private void fitSlopedBackground(final int chLow, final int chHigh,
			final int peakLow, final int peakHigh) {
		double var;
		double sum = 0.0;
		double sumX = 0.0;
		double sumY = 0.0;
		for (int i = chLow; i <= chHigh; i++) {
			var = counts[i] > 0 ? counts[i] : 1.0;
			sum += 1.0 / var;
			sumX += i / var;
			sumY += counts[i] / var;
			if (i == peakLow - 1) {
				i = peakHigh;
			}
		}
		final double xbar = sumX / sum;
		double st2 = 0.0;
		double bkgdSlope = 0.0;
		for (int i = chLow; i <= chHigh; i++) {
			final double sigma = counts[i] > 0 ? Math.sqrt(counts[i]) : 1.0;
			final double chNorm = (i - xbar) / sigma;
			st2 += chNorm * chNorm;
			bkgdSlope += chNorm * counts[i] / sigma;
			if (i == peakLow - 1) {
				i = peakHigh;
			}
		}
		bkgdSlope /= st2;
		final double bkgdSlopeError = 1 / st2;
		final double bkgdConstError = Math.sqrt((1 + sumX * sumX / (sum * st2))
				/ sum);
		final double bkgdConst = (sumY - sumX * bkgdSlope) / sum;
		paramA.setValue(bkgdConst, bkgdConstError);
		paramB.setValue(bkgdSlope, bkgdSlopeError);
	}

	/**
	 * @param sumY
	 * @param sumYY
	 * @param chLow
	 * @param chHigh
	 * @param peakLow
	 * @param peakHigh
	 */
	private void fitFlatBackground(final int chLow, final int chHigh,
			final int peakLow, final int peakHigh) {
		double sumY = 0.0;
		for (int i = chLow; i <= chHigh; i++) {
			sumY += counts[i];
			if (i == peakLow - 1) {
				i = peakHigh;
			}
		}
		final int numBackgdChannels = peakLow - chLow + chHigh - peakHigh;
		sumY /= numBackgdChannels;
		double sumYY = 0.0;
		for (int i = chLow; i <= chHigh; i++) {
			sumYY += Math.pow(counts[i] - sumY, 2.0);
			if (i == peakLow - 1) {
				i = peakHigh;
			}
		}
		sumYY /= numBackgdChannels * (numBackgdChannels - 1);
		paramA.setValue(sumY, Math.sqrt(sumYY));
		paramB.setValue(0.0, 0.0);
	}

	public double calculate(final int channel) {
		return paramA.getValue() + paramB.getValue() * (channel);
	}

	public double calculateBackground(final int channel) {
		return calculate(channel);
	}

	public boolean hasBackground() {
		return true;
	}

	public int getNumberOfSignals() {
		return 0;
	}

	public double calculateSignal(final int signal, final int channel) {
		return 0.0;
	}

}