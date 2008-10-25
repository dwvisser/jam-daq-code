package jam;

import jam.global.AcquisitionStatus;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.QuerySortMode;
import jam.global.RunState;
import jam.global.SortMode;

import java.awt.Frame;
import java.util.Observable;
import java.util.Observer;

final class AcquisitionAndRunState implements Observer {
	private transient final Frame frame;
	private RunState runState = RunState.NO_ACQ;
	private transient final JamStatus status = JamStatus.getSingletonInstance();

	AcquisitionAndRunState(final Frame frame) {
		this.frame = frame;
		this.status.setAcqisitionStatus(new AcquisitionStatus() {
			public boolean isAcqOn() {
				return AcquisitionAndRunState.this.getRunState().isAcqOn();
			}
		});
		final Broadcaster broadcaster = Broadcaster.getSingletonInstance();
		broadcaster.addObserver(this);
	}

	/**
	 * <p>
	 * Sets run state when taking data online. The run state mostly determints
	 * the state of control JMenu items. This method uses imformation set by
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

	/**
	 * @see Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(final Observable event, final Object param) {
		final BroadcastEvent beParam = (BroadcastEvent) param;
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
