package jam.commands;
import jam.JamPrefs;
import jam.global.SortMode;

import java.util.Observable;
import java.util.Observer;

/**
 * Sets/unsets verbose preference.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 2004-06-11
 */
final class SetVerbose extends AbstractSetBooleanPreference implements 
Observer {

	SetVerbose(){
		super();
		putValue(NAME, "Verbose front end");
		putValue(SHORT_DESCRIPTION,
		"Preference for verbosity from the front end process.");
		prefsNode=JamPrefs.PREFS;
		key=JamPrefs.VERBOSE;
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
