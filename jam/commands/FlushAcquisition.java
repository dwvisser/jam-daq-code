/*
 * Created on Jun 7, 2004
 */
package jam.commands;

import jam.global.BroadcastEvent;
import jam.global.JamStatus;
import jam.global.QuerySortMode;
import jam.global.RunState;
import jam.global.SortMode;
import jam.sort.control.RunControl;

import java.util.Observable;
import java.util.Observer;

import com.google.inject.Inject;

/**
 * Flush the acquisition's currently filling buffer to Jam.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version June 7, 2004
 */
final class FlushAcquisition extends AbstractCommand implements Observer {

	private transient final RunControl control;
	private final JamStatus status;

	@Inject
	FlushAcquisition(final RunControl control, final JamStatus status) {
		super("Flush");
		this.control = control;
		this.status = status;
		putValue(SHORT_DESCRIPTION,
				"Flush the current data acquisition buffer.");
		setEnabled(false);
	}

	/**
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	@Override
	protected void execute(final Object[] cmdParams) {
		control.flushAcq();
	}

	/**
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	@Override
	protected void executeParse(final String[] cmdTokens) {
		execute(null);
	}

	/**
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(final Observable observable, final Object arg) {
		final BroadcastEvent event = (BroadcastEvent) arg;
		final BroadcastEvent.Command command = event.getCommand();
		boolean enable = online();
		if (command == BroadcastEvent.Command.RUN_STATE_CHANGED) {
			final RunState state = (RunState) event.getContent();
			enable &= state.isAcqOn();
		}
		setEnabled(enable);
	}

	private boolean online() {
		final QuerySortMode mode = this.status.getSortMode();
		return mode == SortMode.ONLINE_DISK || mode == SortMode.ON_NO_DISK;
	}
}
