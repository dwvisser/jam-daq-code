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
final class SetAutoScaleIgnoreZero extends AbstractSetBooleanPreference {

	SetAutoScaleIgnoreZero(){
		super();
		putValue(NAME, "Autoscale-Ignore Zero");
		putValue(SHORT_DESCRIPTION,
		"Ignore the counts in channel zero when autoscaling.");
		prefsNode=PlotPrefs.PREFS;
		key=PlotPrefs.AUTO_IGNORE_ZERO;
		defaultState=true;
	}
}
