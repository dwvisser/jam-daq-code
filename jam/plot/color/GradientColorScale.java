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
		colorPrefs.addPreferenceChangeListener(new PreferenceChangeListener() {
			public void preferenceChange(PreferenceChangeEvent pce) {
				final String key = pce.getKey();
				if (!key.equals(ColorPrefs.SMOOTH_COLOR_SCALE)) {
					final double newValue = Double.parseDouble(pce
							.getNewValue());
					if (ColorPrefs.ABLUE.equals(key)) {
						ABLUE = newValue;
					} else if (ColorPrefs.AGREEN.equals(key)) {
						AGREEN = newValue;
					} else if (ColorPrefs.ARED.equals(key)) {
						ARED = newValue;
					} else if (ColorPrefs.X0B.equals(key)) {
						X0B = newValue;
					} else if (ColorPrefs.X0R.equals(key)) {
						X0R = newValue;
					} else if (ColorPrefs.X0G.equals(key)) {
						X0G = newValue;
					}
				}
			}
		});
	}

	private static final GradientColorScale LINEAR = new GradientColorScale(0,
			100, Scale.LINEAR);

	private static final GradientColorScale LOG = new GradientColorScale(0,
			100, Scale.LOG);

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

	private double X0R = colorPrefs.getDouble(ColorPrefs.X0R,0.8);

	private double X0G = colorPrefs.getDouble(ColorPrefs.X0G,0.6);

	private double X0B = colorPrefs.getDouble(ColorPrefs.X0B,0.2);

	private double ARED = colorPrefs.getDouble(ColorPrefs.ARED,0.25);

	private double AGREEN = colorPrefs.getDouble(ColorPrefs.AGREEN,0.16);

	private double ABLUE = colorPrefs.getDouble(ColorPrefs.ABLUE,0.09);

	private Color returnRGB(double x) {
		int red = (int) (255 * Math.exp(-(x - X0R) * (x - X0R) / ARED));
		int green = (int) (255 * Math.exp(-(x - X0G) * (x - X0G) / AGREEN));
		int blue = (int) (255 * Math.exp(-(x - X0B) * (x - X0B) / ABLUE));
		return new Color(red, green, blue);
	}

}