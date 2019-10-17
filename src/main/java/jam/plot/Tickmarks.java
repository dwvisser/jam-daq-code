package jam.plot;

import jam.plot.common.Scale;
import jam.plot.common.ScaleCalculator;

/**
 * Class to calculate tickmarks and scale. Need to clean up especially log part
 * error in log for 2d.
 * 
 * @version 0.5
 * @author Ken Swartz
 */
final class Tickmarks {

	Tickmarks() {
		super();
	}

	private static final int MIN_NUMBER_TICKS = 10;

	enum Type {
		/**
		 * for major divisions
		 */
		MAJOR,
		/**
		 * for minor divisions
		 */
		MINOR
	}

	// full number of decades to display
	private transient int countInDecadeMin;

	/*
	 * non-javadoc: Get an array indicating where the Tickmark should be given
	 * an lower limit, upper limit, and scale (Log or Linear) and histogram type
	 * either one or 2 d
	 */
	int[] getTicks(final int lowerLimit, final int upperLimit,
				   final Scale scale, final Type type) {
		int[] ticks = new int[0];
		if (scale == Scale.LINEAR) {
			// for now major and minor are the same
			if (type == Type.MAJOR) {
				ticks = ticksLinear(lowerLimit, upperLimit);
			} else if (type == Type.MINOR) {
				ticks = ticksLinear(lowerLimit, upperLimit);
			}
		} else if (scale == Scale.LOG) {
			if (type == Type.MAJOR) {
				ticks = ticksLogMajor(lowerLimit, upperLimit);
			} else if (type == Type.MINOR) {
				ticks = ticksLog(lowerLimit, upperLimit);
			}

		}
		return ticks;
	}

	/*
	 * non-javadoc: Figure out ticks for linear scale.
	 */
	private int[] ticksLinear(final int lowerLimit, final int upperLimit) {
		final int tickSpace = tickSpace(lowerLimit, upperLimit);
		final int tickMin = tickMin(lowerLimit, tickSpace);
		final int tickMax = tickMax(upperLimit, tickSpace);
		final int numTicks = (tickMax - tickMin) / tickSpace + 1;
		int tempTick[] = new int[numTicks];
		for (int i = 0; i < numTicks; i++) {
			tempTick[i] = tickMin + i * tickSpace;
		}
		return tempTick;
	}

	/*
	 * non-javadoc: Tick spacing for linear scale.
	 */
	private int tickSpace(final int lowerLimit, final int upperLimit) {
		final int range = upperLimit - lowerLimit + 1;
		final ScaleCalculator calculator = new ScaleCalculator(range,
				MIN_NUMBER_TICKS);
		return calculator.compute(lowerLimit, upperLimit);
	}

	/*
	 * non-javadoc: Placement of minimum tick for linear
	 */
	private int tickMin(final int lowerLimit, final int tickSpace) {
		int tempTickMin;
		if ((lowerLimit % tickSpace) == 0) { // lower limit is a tick
			tempTickMin = lowerLimit;
		} else { // tick just above lower limit
			tempTickMin = (lowerLimit / tickSpace + 1) * tickSpace;
			// round down and add one
		}

		if ((lowerLimit <= 0) && (tempTickMin > 0)) { // do we need to make
			// zero a tick
			tempTickMin = 0;
		}
		return tempTickMin;
	}

	/*
	 * non-javadoc: Placement of maximum tick for linear
	 */
	private int tickMax(final int upperLimit, final int tickSpace) {
		int tempTickMax;
		if ((upperLimit % tickSpace) == 0) { // upper limit is a tick
			tempTickMax = upperLimit;
		} else {
			tempTickMax = (upperLimit / tickSpace) * tickSpace;
			// round down for last tick
		}
		return tempTickMax;
	}

	/*
	 * non-javadoc: tick marks at new decades use ticksLog
	 */
	private int[] ticksLogMajor(final int lowerLimit, final int upperLimit) {
		int numberTicks;
		int[] ticks;
		int[] outTicks;
		int decade;

		ticks = ticksLog(lowerLimit, upperLimit);
		// scale only goes to 10 so all ticks can be major
		if (upperLimit <= 10) {
			outTicks = ticks;

		} else {
			// we can do the next two loops using Vector in the future
			// count number of tick points
			numberTicks = 0;
			decade = countInDecadeMin;
			for (int tick : ticks) {
				if (tick % (decade * 10) == 0) {
					numberTicks++;
					decade *= 10;
				}
			} // for we at exactly a power of 10
			// load number of ticks
			outTicks = new int[numberTicks + 1];
			decade = countInDecadeMin;
			int countTick = 0;
			for (int tick : ticks) {
				if (tick % (decade * 10) == 0) {
					outTicks[countTick] = tick;
					countTick++;
					decade *= 10;
				} // for we at exactly a power of 10
			}
		}
		return outTicks;
	}

	/*
	 * non-javadoc: Get tick placement for Log Scaler lower limit upperLimit
	 * 
	 * <table> <tr> <th> value</th><th>decade</th><th>countIn</th> </tr> <tr>
	 * <td>1 </td> <td> 0 </td> <td>1</td> </tr> <tr> <td>2 </td> <td> 0 </td>
	 * <td>1</td> </tr> <tr> <td>3 </td> <td> 0 </td> <td>1</td> </tr> <tr>
	 * ...</tr> <tr> <td>10</td> <td> 1 </td> <td>10</td> </tr>
	 * 
	 * </table>
	 */
	private int[] ticksLog(final int lowerLimit, final int upperLimit) {
		/*
		 * where to start putting ticks takes care of zero minimum check that
		 * min points are not zero (cannot take of zero) if so set to 1 find
		 * power of min and max values
		 */
		final int decadeMin = (lowerLimit == 0) ? 0 : getDecade(lowerLimit);
		// take the power of decadeMin and decadeMax;
		getCountInDecadeMin(decadeMin);
		// where to start possibly putting ticks
		final int startTick = (lowerLimit == 0) ? 1 : lowerLimit;
		// count number of ticks
		// go through the Y scale increase scale by 10 when we pass a new decade
		int numberTicks = 0;
		int decadeIncrement = countInDecadeMin;
		for (int i = startTick; i <= upperLimit; i += decadeIncrement) {
			numberTicks++;
			decadeIncrement = nextDecadeIfValueIsMultipleOfCurrent(
					decadeIncrement, i);
		}
		// load tick values
		// Yes, a using a List<Integer> could avoid 2 loops, but we'll
		// keep it this way because it's working and only allocates an
		// array.
		int[] ticks = new int[numberTicks];
		int tickIndex = 0;
		decadeIncrement = countInDecadeMin;
		for (int i = startTick; i <= upperLimit; i += decadeIncrement) {
			ticks[tickIndex] = i;
			tickIndex++;
			decadeIncrement = nextDecadeIfValueIsMultipleOfCurrent(
					decadeIncrement, i);
		}
		return ticks;
	}

	private int nextDecadeIfValueIsMultipleOfCurrent(final int decade,
			final int value) {
		int result = decade;
		if (value % (decade * 10) == 0) {
			result *= 10;
		} // for we at exactly a power of 10
		return result;
	}

	private void getCountInDecadeMin(final int decadeMin) {
		countInDecadeMin = 1;
		for (int i = 0; i < decadeMin; i++) {
			countInDecadeMin *= 10;
		}
	}

	private int getDecade(final double val) {
		final double logten = Math.log(10.0);
		return (int) (Math.log(val) / logten);
	}

}
