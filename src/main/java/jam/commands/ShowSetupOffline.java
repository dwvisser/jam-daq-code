/*
 * Created on June 4, 2004
 */
package jam.commands;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.google.inject.Inject;

import injection.GuiceInjector;
import jam.global.JamStatus;
import jam.global.QuerySortMode;
import jam.global.SortMode;
import jam.sort.control.SetupSortOff;

/**
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version June 4, 2004
 */
@SuppressWarnings("serial")
final class ShowSetupOffline extends AbstractShowDialog implements PropertyChangeListener {

    @Inject
    ShowSetupOffline(final SetupSortOff setupSortOff) {
        super("Offline sorting\u2026");
        dialog = setupSortOff.getDialog();
        enable();
    }

    private void enable() {
        final QuerySortMode mode = GuiceInjector.getObjectInstance(
                JamStatus.class).getSortMode();
        setEnabled(!(mode == SortMode.ONLINE_DISK
                || mode == SortMode.ON_NO_DISK || mode == SortMode.REMOTE));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        enable();
    }

}
