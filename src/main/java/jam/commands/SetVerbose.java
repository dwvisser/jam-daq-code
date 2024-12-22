package jam.commands;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import injection.GuiceInjector;
import jam.comm.CommunicationPreferences;
import jam.global.JamStatus;
import jam.global.QuerySortMode;
import jam.global.SortMode;

/**
 * Sets/unsets verbose preference.
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version 2004-06-11
 */

final class SetVerbose extends AbstractSetBooleanPreference implements
        PropertyChangeListener {

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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        enable();
    }
}
