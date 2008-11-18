/*
 * Created on Jun 4, 2004
 */
package jam.commands;

import injection.GuiceInjector;
import jam.global.QuerySortMode;
import jam.global.SortMode;

import java.util.Observable;
import java.util.Observer;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 4, 2004
 */
final class ShowSetupOffline extends AbstractShowDialog implements Observer {

	ShowSetupOffline() {
		super("Offline sorting\u2026");
		dialog = GuiceInjector.getSetupSortOff().getDialog();
		enable();
	}

	private void enable() {
		final QuerySortMode mode = GuiceInjector.getJamStatus().getSortMode();
		setEnabled(!(mode == SortMode.ONLINE_DISK
				|| mode == SortMode.ON_NO_DISK || mode == SortMode.REMOTE));
	}

	public void update(final Observable observe, final Object obj) {
		enable();
	}

}
