package jam.commands;

import jam.plot.PlotPrefs;

/**
 * Enable scrolling on plots
 * 
 * @author ken
 */
public class SetEnableScrolling extends AbstractSetBooleanPreference {

	SetEnableScrolling(){
		super();
		putValue(NAME, "Plot Scrolling Tiled Plots");
		putValue(SHORT_DESCRIPTION,
		"Shows the scrollers allowing the scrolling on Plot.");
		prefsNode=PlotPrefs.prefs;
		key=PlotPrefs.ENABLE_SCROLLING;
		defaultState=true;
	}
}

