package jam.plot;

import jam.global.GaussianConstants;

import java.util.Arrays;

/**
 * Class to perform simple fits such as area and centroid
 */
final class PlotFit {

	private static final PlotFit INSTANCE = new PlotFit();

	private PlotFit() {
		super();
	}

	static PlotFit getInstance() {
		return INSTANCE;
	}

	/*
	 * non-javadoc: Get the area for a 1 d histogram
	 */
	double getArea(final double[] counts, final Bin bin1, final Bin bin2) {
		final int xpos1 = bin1.getX();
		final int xpos2 = bin2.getX();
		final int xmin = Math.min(xpos1, xpos2);
		final int xmax = Math.max(xpos1, xpos2);
		double area = 0;
		for (int i = xmin; i <= xmax; i++) {// sum up counts
			area += counts[i];
		}
		return area;
	}

	/*
	 * non-javadoc: Get the area for a 2 d histogram bounded by the rectangle
	 * x1, y1, x2, y2
	 */
	double getArea(final double[][] counts, final Bin bin1, final Bin bin2) {
		final int xpos1 = bin1.getX();
		final int xpos2 = bin2.getX();
		final int ypos1 = bin1.getY();
		final int ypos2 = bin2.getY();
		final int xmin = Math.min(xpos1, xpos2);
		final int xmax = Math.max(xpos1, xpos2);
		final int ymin = Math.min(ypos1, ypos2);
		final int ymax = Math.max(ypos1, ypos2);
		double area = 0;
		for (int i = xmin; i <= xmax; i++) {// sum up counts
			for (int j = ymin; j <= ymax; j++) {
				area += counts[i][j];
			}
		}
		return area;
	}

	/*
	 * non-javadoc: method to calculate the centroid for a histogram given a
	 * bounded area
	 */
	double getCentroid(final double[] counts, final Bin bin1, final Bin bin2) {
		final int xpos1 = bin1.getX();
		final int xpos2 = bin2.getX();
		final int xmin = Math.min(xpos1, xpos2);
		final int xmax = Math.max(xpos1, xpos2);
		double area = 0;
		double centroid = 0;
		for (int i = xmin; i <= xmax; i++) {// sum up counts
			area += counts[i];
		}
		// calculate weight
		if (area > 0) { // must have more than zero counts
			for (int i = xmin; i <= xmax; i++) {
				centroid += (i * counts[i]) / area;
			}
		} else {
			centroid = 0;
		}
		return centroid;
	}

	/*
	 * non-javadoc: method to calculate the FWHM for a histogram given a bounded
	 * area done in such a way the we do not overflow. So we cant use SUM
	 * =Xi^2-(X^bar)^2 does not yet take care of N-1 for denominatior of
	 * variance.
	 */
	double getFWHM(final double[] counts, final Bin bin1, final Bin bin2) {
		final int xpos1 = bin1.getX();
		final int xpos2 = bin2.getX();
		final int xmin = Math.min(xpos1, xpos2);
		final int xmax = Math.max(xpos1, xpos2);
		int area = 0;
		double distance;
		double centroid = 0;
		double sigma = 0;
		double variance = 0;
		double fwhm = 0.0;// default
		/* sum up counts */
		for (int i = xmin; i <= xmax; i++) {
			area += counts[i];
		}
		double darea = area;
		// calculate weights and then fwhm must have more than one counts
		if (area > 2) {
			// calculate centroid
			for (int i = xmin; i <= xmax; i++) {
				centroid += i * counts[i] / darea;
			}
			// calculate variance
			darea = darea - 1.0; // redo weighting
			for (int i = xmin; i <= xmax; i++) {
				distance = i - centroid;
				variance += (counts[i] / darea) * (distance * distance);
			}
			sigma = Math.sqrt(variance);
			fwhm = GaussianConstants.SIG_TO_FWHM * sigma;
		}
		return fwhm;
	}

	void getNetArea(final double[] netArea, double[] netAreaError,
			double[] channelBkgd, double[] fwhm, double[] centroid,
			double[] centroidErr, final Bin[] clicks, final double grossArea,
			final int numChannels, final double[] counts) {
		double netBkgd = 0;
		double[] channel = new double[numChannels];
		double countsHigh = 0;
		double countsLow = 0;
		double area = 0;
		double variance = 0;
		double distance = 0;
		final int[] bgdX1 = { clicks[0].getX(), clicks[1].getX(),
				clicks[2].getX(), clicks[3].getX() };
		Arrays.sort(bgdX1);
		final int bkgd1 = bgdX1[0];
		final int bkgd2 = bgdX1[1];
		final int bkgd3 = bgdX1[2];
		final int bkgd4 = bgdX1[3];
		final int x5temp = clicks[4].getX();
		final int x6temp = clicks[5].getX();
		final int rx1 = Math.min(x5temp, x6temp);
		final int rx2 = Math.max(x5temp, x6temp);
		for (int n = bkgd1; n <= bkgd2; n++) {
			countsLow += counts[n];
		}
		for (int n = bkgd3; n <= bkgd4; n++) {
			countsHigh += counts[n];
		}
		final double avLow = countsLow / (bkgd2 - bkgd1 + 1);
		final double avHigh = countsHigh / (bkgd4 - bkgd3 + 1);
		final double midLow = (bkgd2 + bkgd1) / 2;
		final double midHigh = (bkgd4 + bkgd3) / 2;
		final double gradient = (avHigh - avLow) / (midHigh - midLow);
		final double intercept = avHigh - (gradient * midHigh);
		/* sum counts between region - background at each channel */
		for (int p = rx1; p <= rx2; p++) {
			area += counts[p];
			channel[p] = p + 0.5;
			channelBkgd[p] = gradient * p + intercept;
			netArea[0] += counts[p] - channelBkgd[p];
			netBkgd += channelBkgd[p];
		}
		for (int n = bkgd1; n <= bkgd4 + 1; n++) {
			channelBkgd[n] = gradient * n + intercept;
		}
		netAreaError[0] = Math.pow(grossArea + netBkgd, 0.5);
		/* calculate weight */
		if (area > 0) { // must have more than zero counts
			for (int i = rx1; i <= rx2; i++) {
				centroid[0] += (i * counts[i] / area);
			}
		} else {
			centroid[0] = 0;
		}
		/* Calculation of Variance */
		for (int i = rx1; i <= rx2; i++) {
			distance = Math.pow((i - centroid[0]), 2);
			variance += counts[i] * distance / (area - 1.0);

		}
		/* Error in Centroid position */
		centroidErr[0] = Math.sqrt(variance) / Math.sqrt(rx2 - rx1 + 1);
		fwhm[0] = GaussianConstants.SIG_TO_FWHM * Math.sqrt(variance);
	}
}