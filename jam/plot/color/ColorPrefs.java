/*
 * Created on Jun 10, 2004
 */
package jam.plot.color;

import java.util.prefs.Preferences;

/**
 * Holds reference to the preferences node affecting the 
 * <code>jam.plot.color</code> package, as well as the preference names.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version November 9, 2004
 * @see java.util.prefs.Preferences
 */
public interface ColorPrefs {
	final Preferences colorPrefs=Preferences.userNodeForPackage(ColorPrefs.class);
	final String X0R = "X0R";

	final String X0G = "X0G";

	final String X0B = "X0B";

	final String ARED = "ARED";

	final String AGREEN = "AGREEN";

	final String ABLUE = "ABLUE";
	final String SMOOTH_COLOR_SCALE="ContinuousColorScale";
}
