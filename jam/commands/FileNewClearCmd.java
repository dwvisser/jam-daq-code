package jam.commands;

import jam.data.DataBase;
import jam.global.BroadcastEvent;
import jam.global.JamStatus;
import jam.global.QuerySortMode;
import jam.global.SortMode;

import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.google.inject.Inject;

/**
 * Command for file menu new also clears
 * 
 * @author Ken Swartz
 * 
 */
final class FileNewClearCmd extends AbstractCommand implements Observer {

	private transient final JFrame frame;
	private transient final JamStatus status;

	@Inject
	FileNewClearCmd(final JFrame frame, final JamStatus status) {
		super("Clear data\u2026");
		this.frame = frame;
		this.status = status;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N,
				CTRL_MASK));
		enable();
	}

	/**
	 * Execute command
	 * 
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	@Override
	protected void execute(final Object[] cmdParams) {
		if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(frame,
				"Erase all current data?", "New", JOptionPane.YES_NO_OPTION)) {
			this.status.setSortMode(SortMode.NO_SORT, "Data Cleared");
			DataBase.getInstance().clearAllLists();
			BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_NEW);
			BROADCASTER
					.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, null);
		}

	}

	@Override
	protected void executeParse(final String[] cmdTokens) {
		execute(null);
	}

	public void update(final Observable observe, final Object obj) {
		final BroadcastEvent event = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = event.getCommand();
		if (command == BroadcastEvent.Command.SORT_MODE_CHANGED) {
			enable();
		}
	}

	private void enable() {
		final QuerySortMode mode = this.status.getSortMode();
		setEnabled(mode == SortMode.FILE || mode == SortMode.NO_SORT);
	}

}
