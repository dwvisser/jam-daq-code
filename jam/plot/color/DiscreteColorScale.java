/*
 * Created on Nov 9, 2004
 */
package jam.plot.color;

import jam.plot.Scale;
import jam.plot.Tickmarks;

import java.awt.Color;

/**
 * @author <a href="mailto:dale@visser.name">Dale W Visser </a>
 */
public class DiscreteColorScale implements ColorScale {
	private static final Color[] colorScaleBonW = { new Color(0, 0, 127), //0
			new Color(0, 0, 255), //1
			new Color(128, 0, 255), //2
			new Color(255, 0, 255), //3
			new Color(255, 0, 128), //4
			new Color(255, 0, 0), //5
			new Color(255, 128, 0), //6
			new Color(255, 255, 0), //7
			new Color(0, 0, 0) //9
	};

	private static final Color[] colorScaleWonB = { new Color(0, 0, 127), //0
			new Color(0, 0, 255), //1
			new Color(128, 0, 255), //2
			new Color(255, 0, 255), //3
			new Color(255, 0, 128), //4
			new Color(255, 0, 0), //5
			new Color(255, 128, 0), //6
			new Color(255, 255, 0), //7
			new Color(255, 255, 255), //8
	};

	/* color map for printing */
	private static Color[] colorScaleGray = { new Color(0, 127, 0), //0
			new Color(0, 0, 255), //1
			new Color(128, 0, 255), //2
			new Color(255, 0, 255), //3
			new Color(255, 0, 128), //4
			new Color(255, 0, 0), //5
			new Color(255, 128, 0), //6
			new Color(255, 255, 0), //7
			new Color(255, 255, 255), //8
	};

	public synchronized Color getColor(double counts) {
		Color rval = null;
		final int numberColors = colors.length;
		paintChannel: for (int k = 0; k < numberColors; k++) {
			/* check for min counts first as these are most likely */
			if (counts <= thresholds[k]) {
				rval = colors[k];
				/* inline for speed */
				break paintChannel;
			}
		}
		/* go here on break. */
		/* check if greater than all thresholds */
		if (counts > thresholds[numberColors - 1]) {
			rval = colors[numberColors - 1];
		}
		/* end of loop for each point */
		return rval;
	}

	public synchronized Color getColorByIndex(int index) {
		return colors[index];
	}

	private final Scale scale;

	private DiscreteColorScale(Scale s) {
		scale = s;
		setColors(PlotColorMap.BLACK_ON_WHITE);
	}

	private static final DiscreteColorScale LOG = new DiscreteColorScale(
			Scale.LOG);

	private static final DiscreteColorScale LINEAR = new DiscreteColorScale(
			Scale.LINEAR);

	public static DiscreteColorScale getScale(Scale s) {
		return s == Scale.LINEAR ? LINEAR : LOG;
	}

	private int[] thresholds = new int[0];

	static private Color[] colors;

	/**
	 * Sets the color thresholds.
	 * 
	 * @param lowerLimit
	 *            bottom of scale
	 * @param upperLimit
	 *            top of scale
	 * @param numberColors
	 *            the total number of colors used
	 */
	public synchronized void setRange(int lowerLimit, int upperLimit) {
		thresholds = scale == Scale.LINEAR ? colorThresholdsLin(lowerLimit,
				upperLimit) : colorThresholdsLog(lowerLimit, upperLimit);
	}

	public synchronized int[] getColorThresholds() {
		return thresholds;
	}

	static synchronized void setColors(int mode) {
		if (mode == PlotColorMap.BLACK_ON_WHITE) {
			colors = colorScaleBonW;
		} else if (mode == PlotColorMap.WHITE_ON_BLACK) {
			colors = colorScaleWonB;
		} else if (mode == PlotColorMap.PRINT) {
			colors = colorScaleGray;
		} else {
			throw new IllegalArgumentException(
					"Given mode doesn't refer to an existing color scheme.");
		}
	}

	/**
	 * Linear thresholds for a color in 2d plots.
	 */
	private int[] colorThresholdsLin(int lowerLimit, int upperLimit) {
		final int len = colors.length;
		final int[] thresholdTemp = new int[len];
		final int thresholdStep = thresholdStep(lowerLimit, upperLimit);
		final int thresholdMin = getThresholdMin(lowerLimit, thresholdStep);
		for (int i = 0; i < len; i++) {
			thresholdTemp[i] = thresholdMin + (i) * thresholdStep;
		}
		return thresholdTemp;
	}

	/**
	 * The step in threshold for colors for 2d plot
	 */
	private int thresholdStep(int lowerLimit, int upperLimit) {
		final int numberColors = colors.length;
		/* make display range 10% less than total range */
		final int range = 100 * (upperLimit - lowerLimit) / 120;
		int colorStep = 1;
		for (int i = 1; i < Tickmarks.MAXIMUM_COUNTS; i *= 10) {
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
	private int getThresholdMin(int lowerLimit, int thresholdStep) {
		final int rval;
		if ((lowerLimit % thresholdStep) == 0) { //lower limit is on a step
			rval = lowerLimit + thresholdStep;
		} else { //threshold just above lower limit
			/* round down and add one */
			rval = (lowerLimit / thresholdStep + 1) * thresholdStep;
		}
		return rval;
	}

	/**
	 * Color thresholds for a log 2d plot
	 */
	private int[] colorThresholdsLog(int lowerLimit, int upperLimit) {
		final int numberColors = colors.length;
		final int[] threshold = new int[numberColors];
		/* Find step that step^(number colors) is the maximum */
		int step = 1; //size of a step in color scale,initial step factor -1
		int max; //2**NUMBER_COLORS
		do {
			step++;
			max = (int) Math.round(Math.pow(step, numberColors - 1.0));
		} while (max < upperLimit);
		/* set thresholds */
		int multiStep = 1;//multiple of step intermediate result
		for (int i = 0; i < numberColors; i++) {
			threshold[i] = multiStep + lowerLimit;
			multiStep *= step;
		}
		return threshold;
	}

}