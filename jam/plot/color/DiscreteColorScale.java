package jam.plot.color;

import jam.plot.common.Constants;
import jam.plot.common.Scale;

import java.awt.Color;

/**
 * A color scale where ranges in counts are represented by individual, discrete
 * colors.
 * 
 * @author <a href="mailto:dale@visser.name">Dale W Visser </a>
 * @version 2004-11-09
 * @since 1.6.0
 */
public class DiscreteColorScale implements ColorScale {
	private static final Color[] B_ON_W = { new Color(0, 0, 127), // 0
			new Color(0, 0, 255), // 1
			new Color(128, 0, 255), // 2
			new Color(255, 0, 255), // 3
			new Color(255, 0, 128), // 4
			new Color(255, 0, 0), // 5
			new Color(255, 128, 0), // 6
			new Color(255, 255, 0), // 7
			new Color(0, 0, 0) // 9
	};

	private static final Color[] W_ON_B = { new Color(0, 0, 127), // 0
			new Color(0, 0, 255), // 1
			new Color(128, 0, 255), // 2
			new Color(255, 0, 255), // 3
			new Color(255, 0, 128), // 4
			new Color(255, 0, 0), // 5
			new Color(255, 128, 0), // 6
			new Color(255, 255, 0), // 7
			new Color(255, 255, 255), // 8
	};

	/* color map for printing */
	private static final Color[] GRAY = { new Color(0, 127, 0), // 0
			new Color(0, 0, 255), // 1
			new Color(128, 0, 255), // 2
			new Color(255, 0, 255), // 3
			new Color(255, 0, 128), // 4
			new Color(255, 0, 0), // 5
			new Color(255, 128, 0), // 6
			new Color(255, 255, 0), // 7
			new Color(255, 255, 255), // 8
	};

	public Color getColor(final double counts) {
		synchronized (this) {
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
	}

	/**
	 * Used for painting the key.
	 * 
	 * @param index
	 *            which color
	 * @return color for the given index
	 */
	public Color getColorByIndex(final int index) {
		synchronized (this) {
			return colors[index];
		}
	}

	private transient final Scale scale;

	private DiscreteColorScale(Scale newScale) {
		super();
		scale = newScale;
		setColors(Mode.B_ON_W);
	}

	private static final DiscreteColorScale LOG = new DiscreteColorScale(
			Scale.LOG);

	private static final DiscreteColorScale LINEAR = new DiscreteColorScale(
			Scale.LINEAR);

	/**
	 * Given the type of counts scale, return an appropriate discrete color
	 * scale.
	 * 
	 * @param scale
	 *            the counts scale
	 * @return the color scale
	 */
	public static DiscreteColorScale getScale(final Scale scale) {
		return scale == Scale.LINEAR ? LINEAR : LOG;
	}

	private transient int[] thresholds = new int[0];

	static private Color[] colors = B_ON_W;

	/**
	 * Sets the color thresholds.
	 * 
	 * @param lowerLimit
	 *            bottom of scale
	 * @param upperLimit
	 *            top of scale
	 */
	public void setRange(final int lowerLimit, final int upperLimit) {
		synchronized (this) {
			thresholds = scale == Scale.LINEAR ? colorThresholdsLin(lowerLimit,
					upperLimit) : colorThresholdsLog(lowerLimit, upperLimit);
		}
	}

	/**
	 * Returns the counts thresholds for the various colors. Used to paing the
	 * key.
	 * 
	 * @return array containing the counts thresholds
	 */
	public int[] getColorThresholds() {
		synchronized (this) {
			return thresholds.clone();
		}
	}

	static void setColors(final Mode mode) {
		synchronized (DiscreteColorScale.class) {
			if (mode == Mode.B_ON_W) {
				colors = B_ON_W;
			} else if (mode == Mode.W_ON_B) {
				colors = W_ON_B;
			} else if (mode == Mode.PRINT) {
				colors = GRAY;
			}
		}
	}

	/*
	 * non-javadoc: Linear thresholds for a color in 2d plots.
	 */
	private int[] colorThresholdsLin(final int lowerLimit, final int upperLimit) {
		final int len = colors.length;
		final int[] rval = new int[len];
		final int step = thresholdStep(lowerLimit, upperLimit);
		final int thresholdMin = getThresholdMin(lowerLimit, step);
		for (int i = 0; i < len; i++) {
			rval[i] = thresholdMin + i * step;
		}
		return rval;
	}

	/*
	 * non-javadoc: The step in threshold for colors for 2d plot
	 */
	private int thresholdStep(final int lowerLimit, final int upperLimit) {
		final int numberColors = colors.length;
		/* make display range 10% less than total range */
		final int range = 100 * (upperLimit - lowerLimit) / 120;
		int colorStep = 1;
		for (int i = 1; i < Constants.MAXIMUM_COUNTS; i *= 10) {
			colorStep = i;
			if ((colorStep * numberColors) >= range) {
				break;
			}
			colorStep = i * 2;
			if ((colorStep * numberColors) >= range) {
				break;
			}
			colorStep = i * 5;
			if ((colorStep * numberColors) >= range) {
				break;
			}
		}
		return colorStep;
	}

	/*
	 * non-javadoc: minimum thresold for linear 2d plot
	 */
	private int getThresholdMin(final int lowerLimit, final int step) {
		final int rval;
		if ((lowerLimit % step) == 0) { // lower limit is on a step
			rval = lowerLimit + step;
		} else { // threshold just above lower limit
			/* round down and add one */
			rval = (lowerLimit / step + 1) * step;
		}
		return rval;
	}

	/*
	 * non-javadoc: Color thresholds for a log 2d plot
	 */
	private int[] colorThresholdsLog(final int lowerLimit, final int upperLimit) {
		final int numberColors = colors.length;
		final int[] threshold = new int[numberColors];
		/* Find step that step^(number colors) is the maximum */
		int step = 1; // size of a step in color scale,initial step factor -1
		int max; // 2**NUMBER_COLORS
		do {
			step++;
			max = (int) Math.round(Math.pow(step, numberColors - 1.0));
		} while (max < upperLimit);
		/* set thresholds */
		int multiStep = 1;// multiple of step intermediate result
		for (int i = 0; i < numberColors; i++) {
			threshold[i] = multiStep + lowerLimit;
			multiStep *= step;
		}
		return threshold;
	}

}