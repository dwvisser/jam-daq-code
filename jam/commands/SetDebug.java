package jam.commands;

import injection.GuiceInjector;
import jam.comm.CommunicationPreferences;
import jam.global.QuerySortMode;
import jam.global.SortMode;

import java.util.Observable;
import java.util.Observer;

/**
 * Sets/unsets debug preference.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 2004-06-11
 */
final class SetDebug extends AbstractSetBooleanPreference implements Observer {

	SetDebug() {
		super("Debug front end");
		putValue(SHORT_DESCRIPTION,
				"Preference for debugging messages from the front end process.");
		prefsNode = CommunicationPreferences.PREFS;
		key = CommunicationPreferences.DEBUG;
		defaultState = false;
		enable();
	}

	private void enable() {
		final QuerySortMode mode = GuiceInjector.getJamStatus().getSortMode();
		setEnabled(mode == SortMode.ONLINE_DISK || mode == SortMode.ON_NO_DISK);
	}

	public void update(final Observable observe, final Object obj) {
		enable();
	}
}
