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
public class SetAutoPeakFind extends AbstractSetBooleanPreference {

	SetAutoPeakFind(){
		super();
		putValue(NAME, "Automatic peak find");
		putValue(SHORT_DESCRIPTION,
		"Automatically search for and mark peaks in 1d histograms.");
		prefsNode=PlotPrefs.prefs;
		key=PlotPrefs.AUTO_PEAK_FIND;
		defaultState=false;
	}
}
