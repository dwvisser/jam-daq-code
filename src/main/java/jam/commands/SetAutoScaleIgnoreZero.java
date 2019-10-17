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
final class SetAutoScaleIgnoreZero extends AbstractSetBooleanPreference {

	SetAutoScaleIgnoreZero(){
		super();
		putValue(NAME, "Autoscale-Ignore Zero");
		putValue(SHORT_DESCRIPTION,
		"Ignore the counts in channel zero when autoscaling.");
		prefsNode=PlotPreferences.PREFS;
		key=PlotPreferences.AUTO_IGNORE_ZERO;
		defaultState=true;
	}
}
