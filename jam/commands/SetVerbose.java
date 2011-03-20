package jam.commands;

import injection.GuiceInjector;
import jam.comm.CommunicationPreferences;
import jam.global.JamStatus;
import jam.global.QuerySortMode;
import jam.global.SortMode;

import java.util.Observable;
import java.util.Observer;

/**
 * Sets/unsets verbose preference.
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version 2004-06-11
 */
final class SetVerbose extends AbstractSetBooleanPreference implements
        Observer {

    SetVerbose() {
        super();
        putValue(NAME, "Verbose front end");
        putValue(SHORT_DESCRIPTION,
                "Preference for verbosity from the front end process.");
        prefsNode = CommunicationPreferences.PREFS;
        key = CommunicationPreferences.VERBOSE;
        defaultState = false;
        enable();
    }

    private void enable() {
        final QuerySortMode mode = GuiceInjector.getObjectInstance(
                JamStatus.class).getSortMode();
        setEnabled(mode == SortMode.ONLINE_DISK || mode == SortMode.ON_NO_DISK);
    }

    public void update(final Observable observe, final Object obj) {
        enable();
    }
}
