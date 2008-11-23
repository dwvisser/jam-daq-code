package jam.commands;

import jam.global.BroadcastEvent;
import jam.global.JamStatus;
import jam.global.QuerySortMode;
import jam.global.SortMode;
import jam.sort.control.DisplayCounters;

import java.util.Observable;
import java.util.Observer;

import com.google.inject.Inject;

/**
 * Show parameters dialog.
 * 
 * @author Dale Visser
 * 
 */
final class ShowDialogCounters extends AbstractShowDialog implements Observer {

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

	public void update(final Observable observe, final Object obj) {
		final BroadcastEvent event = (BroadcastEvent) obj;
		if (event.getCommand() == BroadcastEvent.Command.SORT_MODE_CHANGED) {
			final QuerySortMode mode = status.getSortMode();
			setEnabled(mode == SortMode.ONLINE_DISK
					|| mode == SortMode.ON_NO_DISK || mode == SortMode.OFFLINE);
		}
	}
}
