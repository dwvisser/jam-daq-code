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
public class SetBlackBackground extends AbstractSetBooleanPreference {

	SetBlackBackground(){
		super();
		putValue(NAME, "Invert foreground/background");
		putValue(SHORT_DESCRIPTION,
		"Check to use white-on-black color scheme.");
		prefsNode=PlotPrefs.prefs;
		key=PlotPrefs.BLACK_BACKGROUND;
		defaultState=false;
	}
}
