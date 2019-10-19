package jam.commands;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.google.inject.Inject;
import jam.global.BroadcastEvent;
import jam.global.JamStatus;
import jam.global.QuerySortMode;
import jam.global.SortMode;
import jam.sort.control.DisplayCounters;

/**
 * Show parameters dialog.
 * 
 * @author Dale Visser
 * 
 */
final class ShowDialogCounters extends AbstractShowDialog implements PropertyChangeListener {

	private transient final JamStatus status;

	/**
	 * Initialize command
	 */
	@Inject
	ShowDialogCounters(final DisplayCounters displayCounters,
			final JamStatus status) {
		super("Buffer Counters\u2026");
		dialog = displayCounters;
		this.status = status;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (((BroadcastEvent) evt).getCommand() == BroadcastEvent.Command.SORT_MODE_CHANGED) {
			final QuerySortMode mode = status.getSortMode();
			setEnabled(mode == SortMode.ONLINE_DISK
					|| mode == SortMode.ON_NO_DISK || mode == SortMode.OFFLINE);
		}
	}
}
