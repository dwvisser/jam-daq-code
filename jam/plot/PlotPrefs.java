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
public final class PlotPrefs {
	
	private PlotPrefs(){
		super();
	}
    
    /**
     * Preferences node for the <code>jam.plot</code> package.
     */
	public static final Preferences PREFS=Preferences.userNodeForPackage(PlotPrefs.class);
	
	/**
	 * Preference for whether to ignore channel zero when auto-scaling.
	 */
	public static final String AUTO_IGNORE_ZERO="AutoIgnoreZero";
	
	/**
	 * Preference for whether to ignore the last channel when auto-scaling.
	 */
	public static final String AUTO_IGNORE_FULL="AutoIgnoreFull";
	
	/**
	 * Preference for whether to use a black background for the plots.
	 */
	public static final String BLACK_BACKGROUND="BlackBackground";
	
	/**
	 * Preference for whether to automatically find and mark peaks.
	 */
	public static final String AUTO_PEAK_FIND = "AutoPeakFind";
	
	/**
	 * Preference for whether to automatically rescale the counts
	 * axis on a zoom in or zoom out.
	 */
	public static final String AUTO_ON_EXPAND="AutoOnExpand";
	
	/**
	 * Preference for whether to highlight gated channels, or simply
	 * draw the gate shape around them.
	 */
	public static final String HIGHLIGHT_GATE="HighlightGatedChannels";
	
	/**
	 * Preference for whether to show scroll bars for tiled views.
	 */
	public static final String ENABLE_SCROLLING="EnableScrollingTiled";
	
	/**
	 * Preference for whether to draw axis labels.
	 */
	public static final String DISPLAY_LABELS="DisplayAxisLabels";
}
