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

	private double min, max, constant;

	private boolean recalculateConstant = true;

	private boolean logScale;//linear if false, log if true

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
				if (!key.equals(ColorPrefs.SMOOTH_COLOR_SCALE)) {
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

	private static final GradientColorScale LINEAR = new GradientColorScale(0,
			100, Scale.LINEAR);

	private static final GradientColorScale LOG = new GradientColorScale(0,
			100, Scale.LOG);

	/**
	 * Returns the appropriate gradient color scale for the given
	 * scale type.
	 * 
	 * @param s type of counts scale
	 * @return a gradient color scale
	 */
	static public synchronized GradientColorScale getScale(Scale s) {
		return s == Scale.LINEAR ? LINEAR : LOG;
	}

	private void setMaxCounts(double maxCounts) {
		max = maxCounts;
		recalculateConstant = true;
	}

	private void setMinCounts(double minCounts) {
		min = Math.max(1.0, minCounts);
		recalculateConstant = true;
	}

	public synchronized void setRange(int min, int max) {
		setMinCounts(min);
		setMaxCounts(max);
	}

	public Color getColor(double counts) {
		return returnRGB(getScaleValue(counts));
	}

	private void calculateConstant() {
		if (logScale) {
			constant = 1.0 / Math.log(max / min);
		} else {
			constant = 1.0 / (max - min);
		}
		recalculateConstant = false;
	}

	private double getScaleValue(double counts) {
		double normValue = 0.0;
		if (counts != 0.0) {
			if (recalculateConstant) {
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

	private transient double redCenter = COLOR_PREFS.getDouble(ColorPrefs.X0R,0.8);

	private transient double greenCenter = COLOR_PREFS.getDouble(ColorPrefs.X0G,0.6);

	private transient double blueCenter = COLOR_PREFS.getDouble(ColorPrefs.X0B,0.2);

	private transient double redSpread = COLOR_PREFS.getDouble(ColorPrefs.ARED,0.25);

	private transient double greenSpread = COLOR_PREFS.getDouble(ColorPrefs.AGREEN,0.16);

	private transient double blueSpread = COLOR_PREFS.getDouble(ColorPrefs.ABLUE,0.09);

	private Color returnRGB(double level) {
		int red = (int) (255 * Math.exp(-(level - redCenter) * (level - redCenter) / redSpread));
		int green = (int) (255 * Math.exp(-(level - greenCenter) * (level - greenCenter) / greenSpread));
		int blue = (int) (255 * Math.exp(-(level - blueCenter) * (level - blueCenter) / blueSpread));
		return new Color(red, green, blue);
	}

}