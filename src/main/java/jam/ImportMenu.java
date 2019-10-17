package jam;

import com.google.inject.Inject;
import jam.commands.CommandManager;
import jam.commands.CommandNames;
import jam.global.*;

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;

final class ImportMenu implements Observer {
	final transient private JMenu menu;
	private transient final JamStatus status;

	@Inject
	ImportMenu(final JamStatus status, final Broadcaster broadcaster,
			final CommandManager commandManager) {
		broadcaster.addObserver(this);
		this.status = status;
		menu = commandManager.createMenu("Import", CommandNames.IMPORT_TEXT,
				CommandNames.IMPORT_SPE, CommandNames.IMPORT_DAMM,
				CommandNames.IMPORT_XSYS, CommandNames.IMPORT_BAN);
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
