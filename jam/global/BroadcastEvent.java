package jam.global;

/**
 * A event that is broadcast.
 * 
 * @author Ken Swartz
 */
public final class BroadcastEvent {

	/**
	 * The possible commands for <code>BroadcastEvent</code>'s.
	 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
	 */
	public static class Command {

		/** Refresh the display. */
		public static final Command REFRESH = new Command(1);

		/** A new set of histograms has been defined. */
		public static final Command HISTOGRAM_NEW = new Command(10);

		/** A histogram has been added. */
		public static final Command HISTOGRAM_ADD = new Command(11);

		/** A histogram has been selected. */
		public static final Command HISTOGRAM_SELECT = new Command(12);
		
		/**
		 * For telling the gui that we want overlay mode off.
		 */
		public static final Command OVERLAY_OFF=new Command(13);

		/** Scalers have been read. */
		public static final Command SCALERS_READ = new Command(20);

		/**
		 * Clear all scaler registers.
		 */
		public static final Command SCALERS_CLEAR = new Command(21);

		/**
		 * Update the scaler dialog with the current values in memory.
		 */
		public static final Command SCALERS_UPDATE = new Command(22);

		/**
		 * The monitors have been enabled.
		 */
		public static final Command MONITORS_ENABLED = new Command(25); 

		/**
		 * The monitors have been disabled.
		 */
		public static final Command MONITORS_DISABLED = new Command(26); 

		/** Please update the monitors. */
		public static final Command MONITORS_UPDATE = new Command(27); 

		/** A gate has been selected for display. */
		public static final Command GATE_SELECT = new Command(30); 

		/** A gate has been added. */
		public static final Command GATE_ADD = new Command(31); 

		/**
		 * We are currently setting a gate.
		 */
		public static final Command GATE_SET_ON = new Command(32);

		/**
		 * We are no longer setting a gate.
		 */
		public static final Command GATE_SET_OFF = new Command(33);

		/**
		 * Gate has been completely set and saved.
		 */
		public static final Command GATE_SET_SAVE = new Command(34);

		/**
		 * A point has been added to the gate currently being set.
		 */
		public static final Command GATE_SET_POINT = new Command(35);

		/**
		 * A point has been added by typing channels to the gate
		 * currently being set.
		 */
		public static final Command GATE_SET_ADD = new Command(36);

		/**
		 * The last point has been removed from the gate currently
		 * being set.
		 */
		public static final Command GATE_SET_REMOVE = new Command(37);

		/**
		 * Counters have been received from the front end and should be updated.
		 * Message always includes <code>int []</code> as the message parameter.
		 */
		public static final Command COUNTERS_UPDATE = new Command(40);

		/**
		 * Requests counters to be sent from front end.
		 */
		public static final Command COUNTERS_READ = new Command(41);

		/**
		 * Requests counters to be zeroed.
		 */
		public static final Command COUNTERS_ZERO = new Command(42);

		/**
		 * Announces that the sort mode has changed.
		 * 
		 * @see jam.global.SortMode
		 */
		public static final Command SORT_MODE_CHANGED = new Command(50);

		/**
		 * Announces that the run state has changed.
		 * 
		 * @see jam.RunState
		 */
		public static final Command RUN_STATE_CHANGED = new Command(51);

		/**
		 * Announces a new fit dialog has been opened. The action associated with
		 * opening this dialog is passed as a parameter.
		 */
		public static final Command FIT_NEW = new Command(60);
		
		/**
		 * A view has been added or deleted.
		 * 
		 * @see jam.plot.View
		 */
		public static final Command VIEW_NEW = new Command(70);
		
		private final int command;

		private Command(int i) {
			command = i;
		}
		
		public boolean equals(Object o){
		    return o instanceof Command ? ((Command)o).command==command : false;
		}
	}

	private final Object content;

	private final Command command;

	/**
	 * Creates a message to broadcast
	 * 
	 * @param command
	 *            one of the many allowed commands stored in this class
	 * @param content
	 *            additional object along for the ride
	 */
	public BroadcastEvent(Command command, Object content) {
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