/*
 * Created on Jun 10, 2004
 */
package jam.plot.color;

import java.util.prefs.Preferences;

/**
 * Holds reference to the preferences node affecting the
 * <code>jam.plot.color</code> package, as well as the preference names.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser </a>
 * @version November 9, 2004
 * @see java.util.prefs.Preferences
 */
public interface ColorPrefs {
    /**
     * The preferences node for this package.
     */
	final Preferences COLOR_PREFS = Preferences
			.userNodeForPackage(ColorPrefs.class);

	/**
	 * Centroid for red band.
	 */
	final String X0R = "X0R";

	/**
	 * Centroid for green band.
	 */
	final String X0G = "X0G";

	/**
	 * Centroid for blue band.
	 */
	final String X0B = "X0B";

	/**
	 * Diffuseness of red band.
	 */
	final String ARED = "ARED";

	/**
	 * Diffuseness of green band.
	 */
	final String AGREEN = "AGREEN";

	/**
	 * Diffuseness of blue band.
	 */
	final String ABLUE = "ABLUE";

	/**
	 * Whether to use a continuous gradient or discrete color scale.
	 */
	final String SMOOTH_COLOR_SCALE = "ContinuousColorScale";
}