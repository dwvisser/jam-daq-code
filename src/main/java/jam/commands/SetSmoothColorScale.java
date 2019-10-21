/*
 * Created on Jun 11, 2004
 *
 */
package jam.commands;
import jam.plot.color.ColorPrefs;

/**
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version Jun 11, 2004
 */
@SuppressWarnings("serial")
final class SetSmoothColorScale extends AbstractSetBooleanPreference {

	SetSmoothColorScale(){
		super();
		putValue(NAME, "Use gradient color scale");
		putValue(SHORT_DESCRIPTION,
		"Use a continuous rainbow color scale for 2d histograms.");
		prefsNode=ColorPrefs.COLOR_PREFS;
		key=ColorPrefs.SMOOTH_SCALE;
		defaultState=true;
	}
}
