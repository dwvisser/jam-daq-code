package jam.plot;
import java.awt.Point;

/**
 * Class to simple fits such as area and centroid
 */
class PlotFit {

	static final double SIGMA_TO_FWHM = 2.354;
	public static final int X_AXIS = 1;
	public static final int Y_AXIS = 2;

	/**
	 * Get the area for a 1 d histogram
	 */
	public double getArea(double[] counts, Point p1, Point p2) {
		final int xmin=Math.min(p1.x,p2.x);
		final int xmax=Math.max(p1.x,p2.x);
		double area = 0;
		for (int i = xmin; i <= xmax; i++) {//sum up counts	
			area += counts[i];
		}
		return area;
	}

	/**
	 * Get the area for a 2 d histogram bounded by the rectangle
	 * x1, y1, x2, y2
	 */
	public double getArea(double[][] counts, Point p1, Point p2) {
		final int xmin=Math.min(p1.x,p2.x);
		final int xmax=Math.max(p1.x,p2.x);
		final int ymin=Math.min(p1.y,p2.y);
		final int ymax=Math.max(p1.y,p2.y);
		double area = 0;
		for (int i = xmin; i <= xmax; i++) {//sum up counts
			for (int j = ymin; j <= ymax; j++) {
				area += counts[i][j];
			}
		}
		return area;
	}

	/**
	 * method to calculate the centroid for a histogram given a bounded area
	 */
	public double getCentroid(
		double[] counts,
		Point p1,
		Point p2) {
		final int xmin=Math.min(p1.x,p2.x);
		final int xmax=Math.max(p1.x,p2.x);
		double area = 0;
		double darea;
		double centroid = 0;
		for (int i = xmin; i <= xmax; i++) {//sum up counts	
			area += counts[i];
		}
		darea = (double) area;
		// calculate weight
		if (area > 0) { // must have more than zero counts
			for (int i = xmin; i <= xmax; i++) {
				centroid += (double) (i * counts[i]) / darea;
			}
		} else {
			centroid = 0;
		}
		return centroid;
	}

	/**
	 * centroid for 2d not implemented full KBS
	 */
	public double getCentroid(
		double[][] counts,
		int x1,
		int y1,
		int x2,
		int y2,
		int axis) {

		int xmin;
		int xmax;
		int ymin;
		int ymax;
		double area = 0;
		double weightX = 0;
		double weightY = 0;
		double centroid = 0;
		double centroidX = 0;
		double centroidY = 0;

		// put limits in right order	
		if (x1 <= x2) {
			xmin = x1;
			xmax = x2;
		} else {
			xmin = x2;
			xmax = x1;
		}
		if (y1 < y2) {
			ymin = y1;
			ymax = y2;
		} else {
			ymin = y2;
			ymax = y1;
		}
		//sum up counts
		for (int i = xmin; i <= xmax; i++) {
			for (int j = ymin; j <= ymax; j++) {
				area += counts[i][j];
			}
		}
		// calculate weights
		if (area > 0) { // must have more than zero counts
			for (int i = xmin; i <= xmax; i++) {
				for (int j = xmin; j <= xmax; j++) {
					weightX += (double) (i * counts[i][j]);
					weightY += (double) (j * counts[i][j]);
				}
			}
			centroidX = weightX / ((double) area);
			centroidY = weightY / ((double) area);
		} else {
			centroidX = 0;
			centroidY = 0;
		}
		// decide which centroid to return
		if (axis == X_AXIS) {
			centroid = centroidX;
		} else {
			centroid = centroidY;
		}
		return centroid;
	}

	/**
	 * method to calculate the FWHM for a histogram given a bounded area
	 * done in such a way the we do not overflow.
	 * So we cant use SUM =Xi^2-(X^bar)^2
	 * does not yet take care of N-1 for denominatior of variance.
	 */
	public double getFWHM(double[] counts, Point p1, Point p2) {
		int xmin=Math.min(p1.x,p2.x);
		int xmax=Math.max(p1.x,p2.x);
		int area = 0;
		double darea;
		double distance;
		double centroid = 0;
		double sigma = 0;
		double variance = 0;
		double fwhm;

		//sum up counts	
		for (int i = xmin; i <= xmax; i++) {
			area += counts[i];
		}
		darea = (double) area;
		// calculate weights and then fwhm must have more than one counts 
		if (area > 2) {
			// calculate centroid
			for (int i = xmin; i <= xmax; i++) {
				centroid += (double) (i * counts[i]) / darea;
			}
			// calculate variance
			darea = darea - 1.0; //redo weighting
			for (int i = xmin; i <= xmax; i++) {
				distance = (double) (i) - centroid;
				variance += ((double) counts[i] / darea)
					* (distance * distance);
			}
			sigma = Math.sqrt(variance);
			fwhm = SIGMA_TO_FWHM * sigma;
		} else {
			fwhm = 0.0;
		}
		return fwhm;
	}

	public void getNetArea(
		double[] netArea,
		double[] netAreaError,
		double[] channelBackground,
		double[] fwhm,
		double[] centroid,
		double[] centroidError,
		Point [] clicks,
		double grossArea,
		int X,
		double[] counts) {
		double netBackground = 0;
		double [] channel = new double[X];
		double countsHigh = 0;
		double countsLow = 0;
		double area = 0;
		double variance = 0;
		double distance = 0;
		final int x1 = clicks[0].x;
		final int x2 = clicks[1].x;
		final int x3 = clicks[2].x;
		final int x4 = clicks[3].x;
		final int rx1=Math.min(clicks[4].x,clicks[5].x);
		final int rx2=Math.max(clicks[4].x,clicks[5].x);
		for (int n = x1; n <= x2; n++) {
			countsLow += counts[n];
		}
		for (int n = x3; n <= x4; n++) {
			countsHigh += counts[n];
		}
		double avLow = countsLow / (x2 - x1 + 1);
		double avHigh = countsHigh / (x4 - x3 + 1);
		double midLow = (x2 + x1) / 2;
		double midHigh = (x4 + x3) / 2;
		double gradient = (avHigh - avLow) / (midHigh - midLow);
		double intercept = avHigh - (gradient * midHigh);
		/* sum counts between region - background at each channel */
		for (int p = rx1; p <= rx2; p++) {
			area += counts[p];
			channel[p] = p + 0.5;
			channelBackground[p] = gradient * p + intercept;
			netArea[0] += counts[p] - channelBackground[p];
			netBackground += channelBackground[p];
		}
		for (int n = x1; n <= x4 + 1; n++) {
			channelBackground[n] = gradient * n + intercept;
		}
		netAreaError[0] = Math.pow(grossArea + netBackground, 0.5);
		double darea = (double) area;
		/* calculate weight */
		if (area > 0) { // must have more than zero counts
			for (int i = rx1; i <= rx2; i++) {
				centroid[0] += (double) (i * counts[i] / darea);
			}
		} else {
			centroid[0] = 0;
		}
		/* Calculation of Variance */
		for (int i = rx1; i <= rx2; i++) {
			distance = (double) Math.pow((i - centroid[0]), 2);
			variance += (double) counts[i] * distance / (darea - 1.0);

		}
		/* Error in Centroid position */
		centroidError[0] = Math.sqrt(variance) / Math.sqrt(rx2 - rx1 + 1);
		fwhm[0] = 2.354 * Math.sqrt(variance);
	}
}