/*
 * Created on Nov 6, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package jam.commands;

import jam.plot.PlotPreferences;

/**
 * 
 * Display Axis Labels
 * @author ken
 *
 */
public class SetAxisLabels extends AbstractSetBooleanPreference {

	SetAxisLabels(){
		super();
		putValue(NAME, "Axis Labels Single Plot");
		putValue(SHORT_DESCRIPTION,
		"Shows the axis labels and title for tiled plots.");
		prefsNode=PlotPreferences.PREFS;
		key=PlotPreferences.DISPLAY_LABELS;
		defaultState=true;
	}
	
}
