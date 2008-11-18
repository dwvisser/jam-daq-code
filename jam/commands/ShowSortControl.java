package jam.commands;

import injection.GuiceInjector;
import jam.global.QuerySortMode;
import jam.global.SortMode;

import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.Icon;

/**
 * Show the sort control dialog.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 2004-06-04
 */
final class ShowSortControl extends AbstractShowDialog implements Observer {

	ShowSortControl() {
		super("Sort\u2026");
		final Icon iPlayBack = loadToolbarIcon("jam/ui/PlayBack.png");
		putValue(Action.SMALL_ICON, iPlayBack);
		putValue(Action.SHORT_DESCRIPTION, "Sort Control.");
		dialog = GuiceInjector.getSortControl();
		enable();
	}

	private void enable() {
		final QuerySortMode mode = GuiceInjector.getJamStatus().getSortMode();
		setEnabled(mode == SortMode.OFFLINE);
	}

	public void update(final Observable observe, final Object obj) {
		enable();
	}

}
