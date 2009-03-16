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
final class SetBlackBackground extends AbstractSetBooleanPreference {

	SetBlackBackground(){
		super();
		putValue(NAME, "Invert foreground/background");
		putValue(SHORT_DESCRIPTION,
		"Check to use white-on-black color scheme.");
		prefsNode=PlotPreferences.PREFS;
		key=PlotPreferences.BLACK_BACKGROUND;
		defaultState=false;
	}
}
