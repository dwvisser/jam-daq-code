/*
 * CopyRight statement
 */
package jam.plot;

/**
* Class to calculate tickmarks and scale.
* Need to clean up especially log part error in log for 2d.
*
* @version 0.5
* @author Ken Swartz
*/
class Tickmarks {

	private static final int MIN_NUMBER_TICKS = 10;
	private static final int MAXIMUM_COUNTS = 1000000000;
	//dont go on for ever in loop

	static final int LINEAR = 0;
	static final int LOG = 1;
	static final int MAJOR = 0;
	static final int MINOR = 1;

	// full number of decades to display		
	private int countInDecadeMin;
	private int countInDecadeMax;

	/**
	 * Get an array indicating where the Tickmark should be given an
	 * lower limit, upper limit, and scale (Log or Linear) and  histogram 
	 * type either one or 2 d
	 */
	public int[] getTicks(
		int lowerLimit,
		int upperLimit,
	Limits.ScaleType scale,
		int type) {
		int [] ticks=new int[0];
		if (scale == Limits.ScaleType.LINEAR) {
			//for now major and minor are the same
			if (type == MAJOR) {
				ticks = ticksLinear(lowerLimit, upperLimit);
			} else if (type == MINOR) {
				ticks = ticksLinear(lowerLimit, upperLimit);
			}
		} else if (scale == Limits.ScaleType.LOG) {
			if (type == MAJOR) {
				ticks = ticksLogMajor(lowerLimit, upperLimit);
			} else if (type == MINOR) {
				ticks = ticksLog(lowerLimit, upperLimit);
			}

		}
		return ticks;
	}

	/** 
	 * Method for default minor ticks.
	 */
	public int[] getTicks(int lowerLimit, int upperLimit, 
	Limits.ScaleType scale) {
		return getTicks(lowerLimit, upperLimit, scale, MINOR);
	}
	
	/** 
	 * Figure out ticks for linear scale.    
	 */
	private int[] ticksLinear(int lowerLimit, int upperLimit) {
		final int tickSpace = tickSpace(lowerLimit, upperLimit);
		final int tickMin = tickMin(lowerLimit, tickSpace);
		final int tickMax = tickMax(upperLimit, tickSpace);
		int numTicks = (tickMax - tickMin) / tickSpace + 1;
		int tempTick[] = new int[numTicks];
		for (int i = 0; i < numTicks; i++) {
			tempTick[i] = tickMin + i * tickSpace;
		}
		return tempTick;
	}
	
	/** 
	 *  Tick spacing for linear scale.   
	 */
	private int tickSpace(int lowerLimit, int upperLimit) {
		final int range = upperLimit - lowerLimit + 1;
		int tickSpace = 1;
		//loop trying succesively bigger tick spacing
		for (int i = 1; i < MAXIMUM_COUNTS; i *= 10) {
			tickSpace = i;
			if ((tickSpace * MIN_NUMBER_TICKS) >= range){
				break;
			}
			tickSpace = i * 2;
			if ((tickSpace * MIN_NUMBER_TICKS) >= range){
				break;
			}
			tickSpace = i * 5;
			if ((tickSpace * MIN_NUMBER_TICKS) >= range){
				break;
			}
		}
		return tickSpace;
	}
	
	/** 
	 * Placement of minimum tick for linear         
	 */
	private int tickMin(int lowerLimit, int tickSpace) {
		int tempTickMin;
		if ((lowerLimit % tickSpace) == 0) { //lower limit is a tick
			tempTickMin = lowerLimit;
		} else { //tick just above lower limit
			tempTickMin = (lowerLimit / tickSpace + 1) * tickSpace;
			//round down and add one
		}

		if ((lowerLimit <= 0)
			&& (tempTickMin > 0)) { //do we need to make zero a tick
			tempTickMin = 0;
		}
		return tempTickMin;
	}
	
	/** 
	 * Placement of maximum tick for linear         
	 */
	private int tickMax(int upperLimit, int tickSpace) {
		int tempTickMax;
		if ((upperLimit % tickSpace) == 0) { //upper limit is a tick
			tempTickMax = upperLimit;
		} else {
			tempTickMax = (upperLimit / tickSpace) * tickSpace;
			//round down for last tick
		}
		return tempTickMax;
	}
	
	/**    
	 *  tick marks at new decades use ticksLog 
	 */
	private int[] ticksLogMajor(int lowerLimit, int upperLimit) {
		int numberTicks;
		int[] ticks;
		int[] outTicks;
		int decade;

		ticks = ticksLog(lowerLimit, upperLimit);
		//scale only goes to 10 so all ticks can be major
		if (upperLimit <= 10) {
			outTicks = ticks;

		} else {
			//we can do the next two loops using Vector in the future
			//count number of tick points
			numberTicks = 0;
			decade = countInDecadeMin;
			for (int i = 0; i < ticks.length; i++) {
				if (ticks[i] % (decade * 10) == 0) {
					numberTicks++;
					decade *= 10;
				}
			} // for we at exactly a power of 10
			//load number of ticks
			outTicks = new int[numberTicks + 1];
			decade = countInDecadeMin;
			int countTick = 0;
			for (int i = 0; i < ticks.length; i++) {
				if (ticks[i] % (decade * 10) == 0) {
					outTicks[countTick] = ticks[i];
					countTick++;
					decade *= 10;
				} // for we at exactly a power of 10
			}
		}
		return outTicks;
	}
	
	/**
	 * Get tick placement for Log Scaler
	 * lower limit upperLimit
	 *
	 *<table>
	 * <tr>
	 *	<th> value</th><th>decade</th><th>countIn</th>
	 * </tr>
	 *  <tr> <td>1 </td> <td> 0 </td> <td>1</td> </tr>
	 *  <tr> <td>2 </td> <td> 0 </td> <td>1</td> </tr>     
	 *  <tr> <td>3 </td> <td> 0 </td> <td>1</td> </tr>          
	 *  <tr> ...</tr>
	 *  <tr> <td>10</td> <td> 1 </td> <td>10</td> </tr>          
	 *	
	 *</table>
	 */
	private int[] ticksLog(int lowerLimit, int upperLimit) {
		int[] ticks;
		/* where to start putting ticks takes care of zero minimum
         * check that min points are not zero (cannot take of zero) if so set to 1
		 * find power of min and max values */
		final int decadeMin = (lowerLimit != 0) ? getDecade(lowerLimit) : 0;    
		final int decadeMax = (upperLimit != 0) ? getDecade(upperLimit) : 0;
		//take the power of decadeMin and decadeMax;
		countInDecadeMin = 1;
		for (int i = 0; i < decadeMin; i++) {
			countInDecadeMin *= 10;
		}
		countInDecadeMax = 1;
		for (int i = 0; i < decadeMax; i++) {
			countInDecadeMax *= 10;
		}
		//where to start possibly putting ticks
		final int startTick = (lowerLimit != 0) ? lowerLimit : 1;
		/*if (lowerLimit != 0) {
			startTick = lowerLimit;
		} else {
			startTick = 1;
		}*/
		//count number of ticks
		//go through the Y scale increase scale by 10 when we pass a new decade	 	 
		int numberTicks = 0;
		int decade = countInDecadeMin;
		for (int i = startTick; i <= upperLimit; i += decade) {
			numberTicks++;
			if (i % (decade * 10) == 0) {
				decade *= 10;
			} // for we at exactly a power of 10
		}
		//load tick values
		ticks = new int[numberTicks];
		int countTick = 0;
		decade = countInDecadeMin;
		for (int i = startTick; i <= upperLimit; i += decade) {
			ticks[countTick] = i;
			countTick++;
			if (i % (decade * 10) == 0) {
				decade *= 10;
			} // for we at exactly a power of 10 increase
		}
		return ticks;
	}
	
	private int getDecade(double x){
		final double logten=Math.log(10.0);
		final double rval =Math.log(x)/logten;
		return (int)rval;
	}
	
	/**
	 *
	 *  color scale for 2d plots
	 */
	public int[] getColorThresholds(
		int lowerLimit,
		int upperLimit,
		int numberColors,
	Limits.ScaleType scale) {
		int [] thresholds=new int[0];
		if (scale == Limits.ScaleType.LINEAR) {
			thresholds =
				colorThresholdsLin(lowerLimit, upperLimit, numberColors);
		} else if (scale == Limits.ScaleType.LOG) {
			thresholds =
				colorThresholdsLog(lowerLimit, upperLimit, numberColors);

		} 
		return thresholds;
	}
	
	/**
	 * Threshold for a color in 2d plots
	 * lowerLimit 
	 * upperLimit
	 *
	 */
	private int[] colorThresholdsLin(
		int lowerLimit,
		int upperLimit,
		int numberColors) {

		int thresholdStep = thresholdStep(lowerLimit, upperLimit, numberColors);
		int thresholdMin = thresholdMin(lowerLimit, thresholdStep);

		int[] thresholdTemp = new int[numberColors];

		for (int i = 0; i < numberColors; i++) {
			thresholdTemp[i] = thresholdMin + (i) * thresholdStep;
		}

		return (thresholdTemp);
	}
	
	/**
	 * The step in threshold for colors for 2d plot
	 */
	private int thresholdStep(
		int lowerLimit,
		int upperLimit,
		int numberColors) {

		int range = 100 * (upperLimit - lowerLimit) / 120;
		// make disply range 10% less then range
		int colorStep = 1;

		for (int i = 1; i < MAXIMUM_COUNTS; i *= 10) {
			colorStep = i;
			if ((colorStep * numberColors) >= range)
				break;
			colorStep = i * 2;
			if ((colorStep * numberColors) >= range)
				break;
			colorStep = i * 5;
			if ((colorStep * numberColors) >= range)
				break;
		}
		return colorStep;
	}
	
	/**
	 * minimum thresold for linear 2d plot
	 */
	private int thresholdMin(int lowerLimit, int thresholdStep) {
		int tempThreshold;
		if ((lowerLimit % thresholdStep) == 0) { //lower limit is on a step
			tempThreshold = lowerLimit + thresholdStep;
		} else { //threshold just above lower limit
			tempThreshold = (lowerLimit / thresholdStep + 1) * thresholdStep;
			//round down and add one
		}
		return tempThreshold;
	}
	
	/** 
	 * Color thresholds for a log 2d plot
	 */
	private int[] colorThresholdsLog(
		int lowerLimit,
		int upperLimit,
		int numberColors) {
		int[] threshold = new int[numberColors];
		int max; //2**NUMBER_COLORS
		int step; //size of a step in color scale
		int multiStep; //multiple of step intermediate result

		// Find step that  step^(number colors) is the maximum
		step = 1; //initial step factor -1	
		do {
			step++;
			max = (int) Math.round(Math.pow(step, numberColors - 1.0));
		} while (max < upperLimit);

		// set thresholds
		multiStep = 1;
		for (int i = 0; i < numberColors; i++) {
			threshold[i] = multiStep + lowerLimit;
			multiStep *= step;
		}
		return threshold;
	}
}
