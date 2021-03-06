package jam;

import com.google.inject.Inject;
import jam.global.*;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Updates the frame when run state or acquisition status changes.
 * 
 * @author Dale Visser
 * 
 */
final class AcquisitionAndRunState implements PropertyChangeListener {
	private transient final Frame frame;
	private RunState runState = RunState.NO_ACQ;
	private transient final JamStatus status;

	/**
	 * @param frame
	 *            application frame
	 */
	@Inject
	AcquisitionAndRunState(final Frame frame, final JamStatus status) {
		this.frame = frame;
		this.status = status;
		this.status.setAcquisitionStatus(() -> AcquisitionAndRunState.this.getRunState().isAcqOn());
	}

	/**
	 * <p>
	 * Sets run state when taking data online. The run state mostly determines
	 * the state of control JMenu items. This method uses information set by
	 * <code>setSortMode()</code>. In addition:
	 * </p>
	 * <ul>
	 * <li>Control JMenu items are enabled and disabled as appropriate.</li>
	 * <li>Control JMenu items are states are set and unset as appropriate.</li>
	 * <li>The JMenu bar is to show online sort.</li>
	 * <li>Updates display status label .</li>
	 * </ul>
	 * 
	 * @param state
	 *            one of the possible run states control dialog box
	 */
	private void setRunState(final RunState state) {
		synchronized (this.runState) {
			this.runState = state;
		}
	}

	/**
	 * @return the current run state
	 */
	private AcquisitionStatus getRunState() {
		synchronized (this.runState) {
			return this.runState;
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		final BroadcastEvent beParam = (BroadcastEvent) evt;
		final BroadcastEvent.Command command = beParam.getCommand();
		if (command == BroadcastEvent.Command.SORT_MODE_CHANGED) {
			this.sortModeChanged();
		} else if (command == BroadcastEvent.Command.RUN_STATE_CHANGED) {
			this.setRunState((RunState) beParam.getContent());
		}
	}

	/**
	 * Set the mode for sorting data, adjusting title and menu items as
	 * appropriate.
	 * 
	 * @see jam.global.SortMode
	 */
	private void sortModeChanged() {
		final StringBuilder title = new StringBuilder("Jam - ");
		final String disk = "disk";
		final QuerySortMode mode = this.status.getSortMode();
		if (mode == SortMode.ONLINE_DISK || mode == SortMode.ON_NO_DISK) {
			this.setRunState(RunState.ACQ_OFF);
			title.append("Online Sorting");
			if (mode == SortMode.ONLINE_DISK) {
				title.append(" TO ").append(disk);
			}
			this.frame.setTitle(title.toString());
		} else if (mode == SortMode.OFFLINE) {
			this.setRunState(RunState.ACQ_OFF);
			title.append("Offline Sorting");
			if (mode == SortMode.OFFLINE) {
				title.append(" FROM ").append(disk);
			}
			this.frame.setTitle(title.toString());
		} else if (mode == SortMode.REMOTE) { // remote display
			this.setRunState(RunState.NO_ACQ);
			this.frame.setTitle(title.append("Remote Mode").toString());
		} else if (mode == SortMode.FILE) { // just read in a file
			this.setRunState(RunState.NO_ACQ);
			this.frame.setTitle(title.append(status.getSortName()).toString());
		} else if (mode == SortMode.NO_SORT) {
			this.setRunState(RunState.NO_ACQ);
			title.append("sorting not enabled");
			this.frame.setTitle(title.toString());
		}
	}
}
