/*
 * Created on Jun 11, 2004
 *
 */
package jam.commands;
import jam.plot.PlotPreferences;

/**
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version Jun 11, 2004
 */

final class SetGatedChannelsHighlight extends AbstractSetBooleanPreference {

	SetGatedChannelsHighlight(){
		super();
		putValue(NAME, "Highlight gate channels");
		putValue(SHORT_DESCRIPTION,
		"When showing gates, highlight all channels in the gate.");
		prefsNode=PlotPreferences.PREFS;
		key=PlotPreferences.HIGHLIGHT_GATE;
		defaultState=true;
	}
}
