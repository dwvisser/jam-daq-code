/**
 * 
 */
package jam.plot;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

final class AutoCounts implements PreferenceChangeListener {
	/* reference auto scale on expand */
	public transient boolean autoOnExpand = true;

	/**
	 * @see PreferenceChangeListener#preferenceChange(java.util.prefs.PreferenceChangeEvent)
	 */
	public void preferenceChange(final PreferenceChangeEvent pce) {
		final String key = pce.getKey();
		final String newValue = pce.getNewValue();
		if (key.equals(PlotPreferences.AUTO_ON_EXPAND)) {
			this.setAutoOnExpand(Boolean.parseBoolean(newValue));
		}
	}

	/**
	 * Sets whether expand/zoom also causes an auto-scale.
	 * 
	 * @param whether
	 *            <code>true</code> if auto-scale on expand or zoom is
	 *            desired
	 */
	private void setAutoOnExpand(final boolean whether) {
		synchronized (this) {
			this.autoOnExpand = whether;
		}
	}

	public void conditionalAutoCounts(final PlotContainer currentPlot) {
		if (this.autoOnExpand) {
			currentPlot.autoCounts();
		}
	}
}