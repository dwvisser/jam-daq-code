/*
 * Created on Jun 4, 2004
 */
package jam.commands;

import jam.global.JamStatus;
import jam.global.QuerySortMode;
import jam.global.SortMode;
import jam.sort.control.SetupSortOn;

import java.util.Observable;
import java.util.Observer;

import com.google.inject.Inject;

/**
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version Jun 4, 2004
 */
final class ShowSetupOnline extends AbstractShowDialog implements Observer {

	private transient final JamStatus status;

	@Inject
	ShowSetupOnline(final SetupSortOn setup, final JamStatus status) {
		super("Online sorting\u2026");
		this.status = status;
		dialog = setup.getDialog();
		enable();
	}

	private void enable() {
		final QuerySortMode mode = this.status.getSortMode();
		setEnabled(!(mode == SortMode.OFFLINE || mode == SortMode.REMOTE));
	}

	public void update(final Observable observe, final Object obj) {
		enable();
	}
}
