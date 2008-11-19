package jam;

import jam.commands.CommandNames;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.QuerySortMode;
import jam.global.SortMode;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.google.inject.Inject;

final class ImportMenu implements Observer {
	final transient private JMenu menu = MenuBar.createMenu("Import",
			CommandNames.IMPORT_TEXT, CommandNames.IMPORT_SPE,
			CommandNames.IMPORT_DAMM, CommandNames.IMPORT_XSYS,
			CommandNames.IMPORT_BAN);
	private transient final JamStatus status;

	@Inject
	ImportMenu(final JamStatus status) {
		Broadcaster.getSingletonInstance().addObserver(this);
		this.status = status;
	}

	protected JMenuItem getMenu() {
		return menu;
	}

	private void sortModeChanged() {
		final QuerySortMode mode = this.status.getSortMode();
		final boolean file = mode == SortMode.FILE || mode == SortMode.NO_SORT;
		menu.setEnabled(file);
	}

	/**
	 * @see Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(final Observable observe, final Object obj) {
		final BroadcastEvent event = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = event.getCommand();
		if (command == BroadcastEvent.Command.SORT_MODE_CHANGED) {
			sortModeChanged();
		}
	}
}
