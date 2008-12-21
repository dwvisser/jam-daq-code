/*
 * Created on Jun 11, 2004
 *
 */
package jam.commands;
import jam.plot.PlotPreferences;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 11, 2004
 */
final class SetAutoScaleOnExpand extends AbstractSetBooleanPreference {

	SetAutoScaleOnExpand(){
		super();
		putValue(NAME, "Autoscale on Expand/Zoom");
		putValue(SHORT_DESCRIPTION,
		"Automatically scale counts when the plot's viewport changes.");
		prefsNode=PlotPreferences.PREFS;
		key=PlotPreferences.AUTO_ON_EXPAND;
		defaultState=true;
	}
}
