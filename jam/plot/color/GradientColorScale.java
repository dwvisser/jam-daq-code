package jam.plot.color;

import static jam.plot.color.ColorPrefs.COLOR_PREFS;
import jam.plot.common.Scale;

import java.awt.Color;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

/**
 * Smoothly varying rainbow color scale.
 * 
 * @author Dale Visser
 */
public final class GradientColorScale implements ColorScale {

	private static final GradientColorScale LINEAR = new GradientColorScale(0,
			100, Scale.LINEAR);

	private static final GradientColorScale LOG = new GradientColorScale(0,
			100, Scale.LOG);

	static Color getRGB(final double level, final double x0R,
			final double sigR, final double x0G, final double sigG,
			final double x0B, final double sigB) {
		final float red = gaussExp(level, x0R, sigR);
		final float green = gaussExp(level, x0G, sigG);
		final float blue = gaussExp(level, x0B, sigB);
		return new Color(red, green, blue);
	}

	static private float gaussExp(final double level, final double center,
			final double sig) {
		final double offset = level - center;
		return (float) Math.exp(-offset * offset / (sig * sig));
	}

	/**
	 * Returns the appropriate gradient color scale for the given scale type.
	 * 
	 * @param scale
	 *            type of counts scale
	 * @return a gradient color scale
	 */
	static public GradientColorScale getScale(final Scale scale) {
		synchronized (GradientColorScale.class) {
			return scale == Scale.LINEAR ? LINEAR : LOG;
		}
	}

	private transient double blueCenter = COLOR_PREFS.getDouble(
			GradientSpecFieldsRGB.X0B.toString(), 0.2);

	private transient double blueSpread = COLOR_PREFS.getDouble(
			GradientSpecFieldsRGB.ABLUE.toString(), 0.09);

	private transient double greenCenter = COLOR_PREFS.getDouble(
			GradientSpecFieldsRGB.X0G.toString(), 0.6);

	private transient double greenSpread = COLOR_PREFS.getDouble(
			GradientSpecFieldsRGB.AGREEN.toString(), 0.16);

	private final transient boolean logScale;// linear if false, log if true

	private transient double min, max, constant;

	private transient boolean recalculate = true;

	private transient double redCenter = COLOR_PREFS.getDouble(
			GradientSpecFieldsRGB.X0R.toString(), 0.8);

	private transient double redSpread = COLOR_PREFS.getDouble(
			GradientSpecFieldsRGB.ARED.toString(), 0.25);

	{// NOPMD
		COLOR_PREFS.addPreferenceChangeListener(new PreferenceChangeListener() {
			public void preferenceChange(final PreferenceChangeEvent pce) {
				final String key = pce.getKey();
				if (!key.equals(ColorPrefs.SMOOTH_SCALE)) {
					final double newValue = Double.parseDouble(pce
							.getNewValue());
					if (GradientSpecFieldsRGB.ABLUE.toString().equals(key)) {
						blueSpread = newValue;
					} else if (GradientSpecFieldsRGB.AGREEN.toString().equals(
							key)) {
						greenSpread = newValue;
					} else if (GradientSpecFieldsRGB.ARED.toString()
							.equals(key)) {
						redSpread = newValue;
					} else if (GradientSpecFieldsRGB.X0B.toString().equals(key)) {
						blueCenter = newValue;
					} else if (GradientSpecFieldsRGB.X0R.toString().equals(key)) {
						redCenter = newValue;
					} else if (GradientSpecFieldsRGB.X0G.toString().equals(key)) {
						greenCenter = newValue;
					}
				}
			}
		});
	}

	/**
	 * Create a gradient color scale.
	 * 
	 * @param min
	 *            minimum counts for scale
	 * @param max
	 *            maximum counts for scale
	 * @param scale
	 *            whether linear or logarithmic
	 */
	private GradientColorScale(double min, double max, Scale scale) {
		super();
		if (min > max) {
			setMaxCounts(min);
			setMinCounts(max);
		} else {
			setMaxCounts(max);
			setMinCounts(min);
		}
		logScale = (scale == Scale.LOG);
	}

	private void calculateConstant() {
		if (logScale) {
			constant = 1.0 / Math.log(max / min);
		} else {
			constant = 1.0 / (max - min);
		}
		recalculate = false;
	}

	public Color getColor(final double counts) {
		return getRGB(getScaleValue(counts), redCenter, redSpread, greenCenter,
				greenSpread, blueCenter, blueSpread);
	}

	private double getScaleValue(final double counts) {
		double normValue = 0.0;
		if (counts != 0.0) {
			if (recalculate) {
				calculateConstant();
			}
			if (logScale) {
				normValue = constant * Math.log(counts);
			} else {
				normValue = constant * (counts - min);
			}
		}
		return normValue;
	}

	private void setMaxCounts(final double maxCounts) {
		max = maxCounts;
		recalculate = true;
	}

	private void setMinCounts(final double minCounts) {
		min = Math.max(1.0, minCounts);
		recalculate = true;
	}

	public void setRange(final int min, final int max) {
		synchronized (this) {
			setMinCounts(min);
			setMaxCounts(max);
		}
	}

}