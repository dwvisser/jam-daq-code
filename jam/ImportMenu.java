package jam;

import injection.GuiceInjector;
import jam.commands.CommandNames;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.QuerySortMode;
import jam.global.SortMode;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

final class ImportMenu implements Observer {
	final transient private JMenu menu = MenuBar.createMenu("Import",
			CommandNames.IMPORT_TEXT, CommandNames.IMPORT_SPE,
			CommandNames.IMPORT_DAMM, CommandNames.IMPORT_XSYS,
			CommandNames.IMPORT_BAN);

	ImportMenu() {
		Broadcaster.getSingletonInstance().addObserver(this);
	}

	protected JMenuItem getMenu() {
		return menu;
	}

	private void sortModeChanged() {
		final QuerySortMode mode = GuiceInjector.getJamStatus().getSortMode();
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
