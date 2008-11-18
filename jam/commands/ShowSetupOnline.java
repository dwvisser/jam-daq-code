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
final class ShowSetupOnline extends AbstractShowDialog implements Observer {

	ShowSetupOnline() {
		super("Online sorting\u2026");
		dialog = GuiceInjector.getSetupSortOn().getDialog();
		enable();
	}

	private void enable() {
		final QuerySortMode mode = GuiceInjector.getJamStatus().getSortMode();
		setEnabled(!(mode == SortMode.OFFLINE || mode == SortMode.REMOTE));
	}

	public void update(final Observable observe, final Object obj) {
		enable();
	}
}
