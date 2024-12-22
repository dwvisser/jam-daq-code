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

final class SetAutoPeakFind extends AbstractSetBooleanPreference {

	SetAutoPeakFind(){
		super();
		putValue(NAME, "Automatic peak find");
		putValue(SHORT_DESCRIPTION,
		"Automatically search for and mark peaks in 1d histograms.");
		prefsNode=PlotPreferences.PREFS;
		key=PlotPreferences.AUTO_PEAK_FIND;
		defaultState=false;
	}
}
