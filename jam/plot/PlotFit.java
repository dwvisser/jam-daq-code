package jam.plot;

import jam.data.peaks.GaussianConstants;

/**
 * Class to perform simple fits such as area and centroid
 */
final class PlotFit {

	private static final PlotFit INSTANCE = new PlotFit();

	static PlotFit getInstance() {
		return INSTANCE;
	}

	private PlotFit() {
		super();
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
}