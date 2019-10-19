/*
 * Created on Jun 7, 2004
 */
package jam.commands;

import com.google.inject.Inject;
import injection.GuiceInjector;
import jam.global.JamStatus;
import jam.global.QuerySortMode;
import jam.global.SortMode;
import jam.sort.control.RunControl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;

/**
 * Stop data acquisition.
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version June 7, 2004
 */
final class StopAcquisition extends AbstractCommand implements PropertyChangeListener {

    private transient final RunControl control;

    @Inject
    StopAcquisition(final RunControl control) {
        super("stop");
        this.control = control;
        final Icon iPause = loadToolbarIcon("jam/ui/Pause.png");
        putValue(Action.SMALL_ICON, iPause);
        putValue(Action.SHORT_DESCRIPTION, "Pause data acquisition.");
        enable();
    }

    @Override
    protected void execute(final Object[] cmdParams) {
        this.control.stopAcq();
    }

    @Override
    protected void executeParse(final String[] cmdTokens) {
        execute(null);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        enable();
    }

    private void enable() {
        final QuerySortMode mode = GuiceInjector.getObjectInstance(
                JamStatus.class).getSortMode();
        setEnabled(mode == SortMode.ONLINE_DISK || mode == SortMode.ON_NO_DISK);
    }
}
