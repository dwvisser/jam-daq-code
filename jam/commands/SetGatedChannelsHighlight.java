/*
 * Created on Jun 11, 2004
 *
 */
package jam.commands;
import jam.plot.PlotPrefs;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 11, 2004
 */
final class SetGatedChannelsHighlight extends AbstractSetBooleanPreference {

	SetGatedChannelsHighlight(){
		super();
		putValue(NAME, "Highlight gate channels");
		putValue(SHORT_DESCRIPTION,
		"When showing gates, highlight all channels in the gate.");
		prefsNode=PlotPrefs.PREFS;
		key=PlotPrefs.HIGHLIGHT_GATE_CHANNELS;
		defaultState=true;
	}
}
