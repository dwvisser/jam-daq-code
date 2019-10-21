/*
 * Created on Jun 4, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package jam.commands;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.Icon;

import com.google.inject.Inject;

import injection.GuiceInjector;
import jam.global.JamStatus;
import jam.global.QuerySortMode;
import jam.global.SortMode;
import jam.sort.control.RunControl;

/**
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version Jun 4, 2004
 */
@SuppressWarnings("serial")
final class ShowRunControl extends AbstractShowDialog implements PropertyChangeListener {

    @Inject
    ShowRunControl(final RunControl runControl) {
        super("Run\u2026");
        final Icon iRun = loadToolbarIcon("jam/ui/Run.png");
        putValue(Action.SMALL_ICON, iRun);
        putValue(Action.SHORT_DESCRIPTION, "Run Control.");
        dialog = runControl;
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
