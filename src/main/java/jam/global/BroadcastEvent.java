package jam.global;

import java.beans.PropertyChangeEvent;

/**
 * A event that is broadcast.
 * 
 * @author Ken Swartz
 */
@SuppressWarnings("serial")
public final class BroadcastEvent extends PropertyChangeEvent {

	/**
	 * The possible commands for <code>BroadcastEvent</code>'s.
	 * 
	 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale W Visser</a>
	 */
	public enum Command {

		/** Refresh the display. */
		REFRESH,

		/**
		 * The root node has been selected
		 */
		ROOT_SELECT,

		/**
		 * A Group has been selected.
		 * 
		 * @see jam.plot.View
		 */
		GROUP_SELECT,

		/** A new set of histograms has been defined. */
		HISTOGRAM_NEW,

		/** A histogram has been added. */
		HISTOGRAM_ADD,

		/** A histogram has been selected. */
		HISTOGRAM_SELECT,

		/**
		 * For telling the gui that we want overlay mode off.
		 */
		OVERLAY_OFF,

		/** Scalers have been read. */
		SCALERS_READ,

		/**
		 * Clear all scaler registers.
		 */
		SCALERS_CLEAR,

		/**
		 * Update the scaler dialog with the current values in memory.
		 */
		SCALERS_UPDATE,

		/**
		 * The monitors have been enabled.
		 */
		MONITORS_ENABLED,

		/**
		 * The monitors have been disabled.
		 */
		MONITORS_DISABLED,

		/** Please update the monitors. */
		MONITORS_UPDATE,

		/** A gate has been selected for display. */
		GATE_SELECT,
		/** A gate has been added. */
		GATE_ADD,

		/**
		 * We are currently setting a gate.
		 */
		GATE_SET_ON,

		/**
		 * We are no longer setting a gate.
		 */
		GATE_SET_OFF,

		/**
		 * Gate has been completely set and saved.
		 */
		GATE_SET_SAVE,

		/**
		 * A point has been added to the gate currently being set.
		 */
		GATE_SET_POINT,

		/**
		 * A point has been added by typing channels to the gate currently being
		 * set.
		 */
		GATE_SET_ADD,

		/**
		 * The last point has been removed from the gate currently being set.
		 */
		GATE_SET_REMOVE,

		/**
		 * Counters have been received from the front end and should be updated.
		 * Message always includes <code>int []</code> as the message parameter.
		 */
		COUNTERS_UPDATE,

		/**
		 * Requests counters to be sent from front end.
		 */
		COUNTERS_READ,

		/**
		 * Requests counters to be zeroed.
		 */
		COUNTERS_ZERO,

		/**
		 * Announces that the sort mode has changed.
		 * 
		 * @see jam.global.SortMode
		 */
		SORT_MODE_CHANGED,

		/**
		 * Announces that the run state has changed.
		 * 
		 * @see jam.global.RunState
		 */
		RUN_STATE_CHANGED,

		/**
		 * Announces a new fit dialog has been opened. The action associated
		 * with opening this dialog is passed as a parameter.
		 */
		FIT_NEW,

		/**
		 * A view has been added or deleted.
		 * 
		 * @see jam.plot.View
		 */
		VIEW_NEW
	}

    private transient final Object content;

	private transient final Command command;

	/**
	 * Creates a message to broadcast
	 * 
	 * @param command
	 *            one of the many allowed commands stored in this class
	 * @param content
	 *            additional object along for the ride
	 */
	public BroadcastEvent(final Command command, final Object content) {
		super(command, "", null, content);
		this.command = command;
		this.content = content;
	}

	/**
	 * @return the command that is sent
	 */
	public Command getCommand() {
		return command;
	}

	/**
	 * 
	 * @return the additional message content
	 */
	public Object getContent() {
		return content;
	}
}