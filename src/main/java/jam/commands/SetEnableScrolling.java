package jam.commands;

import jam.plot.PlotPreferences;

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
		prefsNode=PlotPreferences.PREFS;
		key=PlotPreferences.ENABLE_SCROLLING;
		defaultState=false;
	}
}

