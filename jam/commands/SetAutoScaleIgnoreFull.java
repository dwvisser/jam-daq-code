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
final class SetAutoScaleIgnoreFull extends AbstractSetBooleanPreference {

	SetAutoScaleIgnoreFull(){
		super();
		putValue(NAME, "Autoscale-Ignore Last Channel");
		putValue(SHORT_DESCRIPTION,
		"Ignore the counts in the last channel when autoscaling.");
		prefsNode=PlotPrefs.prefs;
		key=PlotPrefs.AUTO_IGNORE_FULL;
		defaultState=true;
	}
}
