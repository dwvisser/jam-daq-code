/*
 * Created on Jun 10, 2004
 */
package jam.plot.color;

import java.util.prefs.Preferences;

/**
 * Holds reference to the preferences node affecting the
 * <code>jam.plot.color</code> package, as well as the preference names.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser </a>
 * @version November 9, 2004
 * @see java.util.prefs.Preferences
 */
public final class ColorPrefs {
	
	private ColorPrefs(){
		super();
	}
	
    /**
     * The preferences node for this package.
     */
	static public final Preferences COLOR_PREFS = Preferences
			.userNodeForPackage(ColorPrefs.class);

	/**
	 * Whether to use a continuous gradient or discrete color scale.
	 */
	static public final String SMOOTH_SCALE = "ContinuousColorScale";
}

enum GradientSpecFieldsRGB {
	/**
	 * Centroid for red band.
	 */
	X0R,

	/**
	 * Centroid for green band.
	 */
	X0G,

	/**
	 * Centroid for blue band.
	 */
	X0B,

	/**
	 * Diffuseness of red band.
	 */
	ARED,

	/**
	 * Diffuseness of green band.
	 */
	AGREEN,

	/**
	 * Diffuseness of blue band.
	 */
	ABLUE
}