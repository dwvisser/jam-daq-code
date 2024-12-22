package jam.commands;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.Icon;

import com.google.inject.Inject;

import jam.global.JamStatus;
import jam.global.QuerySortMode;
import jam.global.SortMode;
import jam.sort.control.SortControl;

/**
 * Show the sort control dialog.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version 2004-06-04
 */

final class ShowSortControl extends AbstractShowDialog implements PropertyChangeListener {

	private transient final JamStatus status;

	@Inject
	ShowSortControl(final SortControl sortControl, final JamStatus status) {
		super("Sort\u2026");
		this.status = status;
		final Icon iPlayBack = loadToolbarIcon("jam/ui/PlayBack.png");
		putValue(Action.SMALL_ICON, iPlayBack);
		putValue(Action.SHORT_DESCRIPTION, "Sort Control.");
		dialog = sortControl;
		enable();
	}

	private void enable() {
		final QuerySortMode mode = this.status.getSortMode();
		setEnabled(mode == SortMode.OFFLINE);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		enable();
	}

}
