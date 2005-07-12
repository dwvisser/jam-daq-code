package jam.plot.color;

import jam.plot.Scale;

import java.awt.Color;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

/**
 * Smoothly varying rainbow color scale.
 * 
 * @author Dale Visser
 */
public final class GradientColorScale implements ColorScale, ColorPrefs {

	private static final GradientColorScale LINEAR = new GradientColorScale(0,
			100, Scale.LINEAR);

	private static final GradientColorScale LOG = new GradientColorScale(0,
			100, Scale.LOG);

	/**
	 * Returns the appropriate gradient color scale for the given
	 * scale type.
	 * 
	 * @param scale type of counts scale
	 * @return a gradient color scale
	 */
	static public synchronized GradientColorScale getScale(Scale scale) {
		return scale == Scale.LINEAR ? LINEAR : LOG;
	}

	private transient double blueCenter = COLOR_PREFS.getDouble(ColorPrefs.X0B,0.2);

	private transient double blueSpread = COLOR_PREFS.getDouble(ColorPrefs.ABLUE,0.09);

	private transient double greenCenter = COLOR_PREFS.getDouble(ColorPrefs.X0G,0.6);

	private transient double greenSpread = COLOR_PREFS.getDouble(ColorPrefs.AGREEN,0.16);

	private final transient boolean logScale;//linear if false, log if true

	private transient double min, max, constant;

	private transient boolean recalculate = true;

	private transient double redCenter = COLOR_PREFS.getDouble(ColorPrefs.X0R,0.8);

	private transient double redSpread = COLOR_PREFS.getDouble(ColorPrefs.ARED,0.25);

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
		if (min > max) {
			setMaxCounts(min);
			setMinCounts(max);
		} else {
			setMaxCounts(max);
			setMinCounts(min);
		}
		logScale = (scale == Scale.LOG);
		COLOR_PREFS.addPreferenceChangeListener(new PreferenceChangeListener() {
			public void preferenceChange(PreferenceChangeEvent pce) {
				final String key = pce.getKey();
				if (!key.equals(ColorPrefs.SMOOTH_SCALE)) {
					final double newValue = Double.parseDouble(pce
							.getNewValue());
					if (ColorPrefs.ABLUE.equals(key)) {
						blueSpread = newValue;
					} else if (ColorPrefs.AGREEN.equals(key)) {
						greenSpread = newValue;
					} else if (ColorPrefs.ARED.equals(key)) {
						redSpread = newValue;
					} else if (ColorPrefs.X0B.equals(key)) {
						blueCenter = newValue;
					} else if (ColorPrefs.X0R.equals(key)) {
						redCenter = newValue;
					} else if (ColorPrefs.X0G.equals(key)) {
						greenCenter = newValue;
					}
				}
			}
		});
	}

	private void calculateConstant() {
		if (logScale) {
			constant = 1.0 / Math.log(max / min);
		} else {
			constant = 1.0 / (max - min);
		}
		recalculate = false;
	}

	public Color getColor(double counts) {
		return returnRGB(getScaleValue(counts));
	}

	private double getScaleValue(double counts) {
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

	private Color returnRGB(double level) {
		final int red = (int) (255 * Math.exp(-(level - redCenter) * (level - redCenter) / redSpread));
		final int green = (int) (255 * Math.exp(-(level - greenCenter) * (level - greenCenter) / greenSpread));
		final int blue = (int) (255 * Math.exp(-(level - blueCenter) * (level - blueCenter) / blueSpread));
		return new Color(red, green, blue);
	}

	private void setMaxCounts(double maxCounts) {
		max = maxCounts;
		recalculate = true;
	}

	private void setMinCounts(double minCounts) {
		min = Math.max(1.0, minCounts);
		recalculate = true;
	}

	public synchronized void setRange(int min, int max) {
		setMinCounts(min);
		setMaxCounts(max);
	}

}