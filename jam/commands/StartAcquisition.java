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

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;

/**
 * Start data acquisition.
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version June 7, 2004
 */
final class StartAcquisition extends AbstractCommand implements Observer {

    private transient final RunControl runControl;

    @Inject
    StartAcquisition(final RunControl runControl) {
        super("start");
        this.runControl = runControl;
        final Icon iStart = loadToolbarIcon("jam/ui/Start.png");
        putValue(Action.SMALL_ICON, iStart);
        putValue(SHORT_DESCRIPTION, "Start data acquisition.");

    }

    @Override
    protected void execute(final Object[] cmdParams) {
        this.runControl.startAcq();
    }

    @Override
    protected void executeParse(final String[] cmdTokens) {
        execute(null);
    }

    public void update(final Observable obs, final Object arg) {
        enable();
    }

    private void enable() {
        final QuerySortMode mode = GuiceInjector.getObjectInstance(
                JamStatus.class).getSortMode();
        setEnabled(mode == SortMode.ONLINE_DISK || mode == SortMode.ON_NO_DISK);
    }
}
