package jam.commands;

import jam.global.JamStatus;
import jam.global.QuerySortMode;
import jam.global.SortMode;
import jam.sort.control.SortControl;

import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.Icon;

import com.google.inject.Inject;

/**
 * Show the sort control dialog.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 2004-06-04
 */
final class ShowSortControl extends AbstractShowDialog implements Observer {

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

	public void update(final Observable observe, final Object obj) {
		enable();
	}

}
