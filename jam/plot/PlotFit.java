package jam.plot;

/**
 * Class to simple fits such as area and centroid
 */
class PlotFit {

	static final double SIGMA_TO_FWHM = 2.354;
	public static final int X_AXIS = 1;
	public static final int Y_AXIS = 2;
	/**
	 * constructor
	 */
	public PlotFit() {
	}

	/**
	 * Get the area for a 1 d histogram
	 */
	public double getArea(double[] counts, int x1, int x2) {
		int xmin;
		int xmax;
		double area = 0;

		// put limits in right order	
		if (x1 <= x2) {
			xmin = x1;
			xmax = x2;
		} else {
			xmin = x2;
			xmax = x1;
		}
		//sum up counts	
		for (int i = xmin; i <= xmax; i++) {
			area += counts[i];
		}
		return area;
	}
	
	/**
	 * Get the area for a 2 d histogram bounded by the rectangle
	 * x1, y1, x2, y2
	 */
	public double getArea(double[][] counts, int x1, int y1, int x2, int y2) {
		int xmin;
		int xmax;
		int ymin;
		int ymax;
		double area = 0;

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
		return area;
	}
	
	/**
	 * method to calculate the centroid for a histogram given a bounded area
	 */
	public double getCentroid(
		double[] counts,
		int x1,
		int y1,
		int x2,
		int y2) {
		int xmin, xmax, ymin, ymax;
		double area = 0;
		double darea;
		double centroid = 0;

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
	public double getFWHM(double[] counts, int x1, int y1, int x2, int y2) {
		int xmin;
		int xmax;
		int area = 0;
		double darea;
		double distance;
		double centroid = 0;
		double sigma = 0;
		double variance = 0;
		double fwhm;

		// put limits in right order	
		if (x1 <= x2) {
			xmin = x1;
			xmax = x2;
		} else {
			xmin = x2;
			xmax = x1;
		}
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
}