/*
 * Created on Jun 10, 2004
 */
package jam.plot;

import java.util.prefs.Preferences;

/**
 * Holds reference to the preferences node affecting the 
 * <code>jam.plot</code> package, as well as the preference names.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 10, 2004
 * @see java.util.prefs.Preferences
 */
public interface PlotPrefs {
	Preferences PREFS=Preferences.userNodeForPackage(PlotPrefs.class);
	String AUTO_IGNORE_ZERO="AutoIgnoreZero";
	String AUTO_IGNORE_FULL="AutoIgnoreFull";
	String BLACK_BACKGROUND="BlackBackground";
	String AUTO_PEAK_FIND = "AutoPeakFind";
	String AUTO_ON_EXPAND="AutoOnExpand";
	String HIGHLIGHT_GATE_CHANNELS="HighlightGatedChannels";
	String ENABLE_SCROLLING="EnableScrolling";
	String DISPLAY_AXIS_LABELS="DisplayAxisLabels";
}
