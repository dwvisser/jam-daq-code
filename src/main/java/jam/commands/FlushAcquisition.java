/*
 * Created on Jun 7, 2004
 */
package jam.commands;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.google.inject.Inject;

import jam.global.BroadcastEvent;
import jam.global.JamStatus;
import jam.global.QuerySortMode;
import jam.global.RunState;
import jam.global.SortMode;
import jam.sort.control.RunControl;

/**
 * Flush the acquisition's currently filling buffer to Jam.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version June 7, 2004
 */
@SuppressWarnings("serial")
final class FlushAcquisition extends AbstractCommand implements PropertyChangeListener {

	private transient final RunControl control;
	private transient final JamStatus status;

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

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		final BroadcastEvent event = (BroadcastEvent) evt;
		boolean enable = online();
		if (event.getCommand() == BroadcastEvent.Command.RUN_STATE_CHANGED) {
			enable &= ((RunState) event.getContent()).isAcqOn();
		}
		setEnabled(enable);
	}

	private boolean online() {
		final QuerySortMode mode = this.status.getSortMode();
		return mode == SortMode.ONLINE_DISK || mode == SortMode.ON_NO_DISK;
	}
}
