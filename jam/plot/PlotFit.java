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


	 	    public double getNetArea( double  [] netArea,
				      double [] netAreaError,
				      double [] channelBackground,
				      double [] fwhm,			       
				      double [] centroid, 
				      double [] centroidError,
				      int [][] xyCursor, 
				      double grossArea, 
			      int X, double [] counts) {
    double darea;
    double area;
    double variance;
    double distance;
    double gradient, intercept;
    double netBackground = 0;
    int x1, x2, x3, x4;
    int rx1, rx2;
    double avLow, avHigh, countsLow, countsHigh;
    double midLow, midHigh;
    double [] channel;
    double [] countsdl;
    channel = new double[X];
    countsdl = new double[X];
    countsHigh = 0;
    countsLow = 0;
    area = 0;
    variance = 0;
    distance = 0;

    x1 = xyCursor[0][0];
    x2 = xyCursor[1][0];
    x3 = xyCursor[2][0];
    x4 = xyCursor[3][0];
    // Put markers in correct order
    if( xyCursor[4][0]< xyCursor[5][0] )
      {
       rx1 = xyCursor[4][0];
       rx2 = xyCursor[5][0]; 
      }
    else{
      rx1 = xyCursor[4][0];
      rx2 = xyCursor[5][0];
    }

    for (int n = x1; n<=x2 ; n++ ){
      countsLow = counts[n] + countsLow;
    }  
        for (int n = x3; n<=x4 ; n++ ){
      countsHigh = counts[n] + countsHigh;
    }  
	avLow = countsLow/(x2-x1+1);
	avHigh = countsHigh/(x4-x3+1);
	midLow = (x2+x1)/2;
	midHigh = (x4+x3)/2;

      gradient = (avHigh-avLow)/(midHigh-midLow);
      intercept = avHigh - (gradient*midHigh);
    
    // sum counts between region - background at each channel
    for( int p=rx1 ; p<=rx2; p++){
	area+=counts[p];
	channel[p] = p + 0.5;
	channelBackground[p] = gradient*p + intercept;
        netArea[0] += counts[p]-channelBackground[p];
      	netBackground += channelBackground[p];	  
    }
        for( int n=x1 ; n<=x4+1; n++){
      channelBackground[n] = gradient*n + intercept;
	}
    netAreaError[0] = Math.pow(grossArea+netBackground,0.5);
    darea=(double)area;
		// calculate weight
    		if (area>0){	    // must have more than zero counts
    			for (int i=rx1;i<=rx2;i++){
    				centroid[0]+=(double)(i*counts[i]/darea);
    			}
	    	} else {
			centroid[0]=0;
			return(0);
		}

		// Calculation of Variance
    		for (int i = rx1 ; i <= rx2 ; i++){
   		  distance = (double)Math.pow((i - centroid[0]),2);
    		  variance += (double)counts[i]*distance/(darea - 1.0);
    		  
    		}
		//Error in Centroid position
    		centroidError[0] = Math.sqrt(variance)/Math.sqrt(rx2-rx1+1);
		fwhm[0] = 2.354*Math.sqrt(variance);

	return (6);

    }
}