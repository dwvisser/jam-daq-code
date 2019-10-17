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
final class SetAutoScaleIgnoreFull extends AbstractSetBooleanPreference {

	SetAutoScaleIgnoreFull(){
		super();
		putValue(NAME, "Autoscale-Ignore Last Channel");
		putValue(SHORT_DESCRIPTION,
		"Ignore the counts in the last channel when autoscaling.");
		prefsNode=PlotPreferences.PREFS;
		key=PlotPreferences.AUTO_IGNORE_FULL;
		defaultState=true;
	}
}
