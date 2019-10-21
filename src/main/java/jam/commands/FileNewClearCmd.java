package jam.commands;

import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.google.inject.Inject;

import jam.data.DataBase;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.QuerySortMode;
import jam.global.SortMode;

/**
 * Command for file menu new also clears
 * 
 * @author Ken Swartz
 * 
 */
@SuppressWarnings("serial")
final class FileNewClearCmd extends AbstractCommand implements PropertyChangeListener {

	private transient final JFrame frame;
	private transient final JamStatus status;
	private transient final Broadcaster broadcaster;

	@Inject
	FileNewClearCmd(final JFrame frame, final JamStatus status,
			final Broadcaster broadcaster) {
		super("Clear data\u2026");
		this.frame = frame;
		this.status = status;
		this.broadcaster = broadcaster;
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
			this.broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_NEW);
			this.broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT,
					null);
		}

	}

	@Override
	protected void executeParse(final String[] cmdTokens) {
		execute(null);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (((BroadcastEvent) evt).getCommand() == BroadcastEvent.Command.SORT_MODE_CHANGED) {
			enable();
		}
	}

	private void enable() {
		final QuerySortMode mode = this.status.getSortMode();
		setEnabled(mode == SortMode.FILE || mode == SortMode.NO_SORT);
	}

}
