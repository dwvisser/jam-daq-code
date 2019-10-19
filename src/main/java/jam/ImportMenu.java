package jam;

import com.google.inject.Inject;
import jam.commands.CommandManager;
import jam.commands.CommandNames;
import jam.global.*;

import javax.swing.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

final class ImportMenu implements PropertyChangeListener {
	final transient private JMenu menu;
	private transient final JamStatus status;

	@Inject
	ImportMenu(final JamStatus status, final Broadcaster broadcaster,
			final CommandManager commandManager) {
		broadcaster.addPropertyChangeListener(this);
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

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		final BroadcastEvent.Command command = ((BroadcastEvent) evt).getCommand();
		if (command == BroadcastEvent.Command.SORT_MODE_CHANGED) {
			sortModeChanged();
		}
	}
}
