/*
 * Created on Jun 10, 2004
 *
 */
package jam.plot;

import java.util.prefs.Preferences;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 10, 2004
 */
public interface PlotPrefs {
	final Preferences prefs=Preferences.systemNodeForPackage(PlotPrefs.class);
	final String AUTO_IGNORE_ZERO="AutoIgnoreZero";
	final String AUTO_IGNORE_FULL="AutoIgnoreFull";
	final String BLACK_BACKGROUND="BlackBackground";
	final String AUTO_PEAK_FIND = "AutoPeakFind";
	final String SMOOTH_COLOR_SCALE="ContinuousColorScale";
}
