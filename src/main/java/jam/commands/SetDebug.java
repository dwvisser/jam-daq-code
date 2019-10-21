package jam.commands;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import injection.GuiceInjector;
import jam.comm.CommunicationPreferences;
import jam.global.JamStatus;
import jam.global.QuerySortMode;
import jam.global.SortMode;

/**
 * Sets/unsets debug preference.
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version 2004-06-11
 */
@SuppressWarnings("serial")
final class SetDebug extends AbstractSetBooleanPreference implements PropertyChangeListener {

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
        final QuerySortMode mode = GuiceInjector.getObjectInstance(
                JamStatus.class).getSortMode();
        setEnabled(mode == SortMode.ONLINE_DISK || mode == SortMode.ON_NO_DISK);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        enable();
    }
}
