package jam.commands;
import jam.JamPrefs;
import jam.global.SortMode;

import java.util.Observable;
import java.util.Observer;

/**
 * Sets/unsets debug preference.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 2004-06-11
 */
final class SetDebug extends AbstractSetBooleanPreference implements 
Observer {

	SetDebug(){
		super();
		putValue(NAME, "Debug front end");
		putValue(SHORT_DESCRIPTION,
		"Preference for debugging messages from the front end process.");
		prefsNode=JamPrefs.PREFS;
		key=JamPrefs.DEBUG;
		defaultState=false;
		enable();
	}

	private final void enable() {
		final SortMode mode=status.getSortMode();
		setEnabled(mode == SortMode.ONLINE_DISK || 
		mode == SortMode.ON_NO_DISK);
	}

	public void update(Observable observe, Object obj){
		enable();
	}
}
