/*
 */
package jam.global;

/**
 * A event that is broadcast.
 * 
 * @author Ken Swartz
 */
public class BroadcastEvent {

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

		public static final Command SCALERS_CLEAR = new Command(21);

		public static final Command SCALERS_UPDATE = new Command(22);

		public static final Command MONITORS_ENABLED = new Command(25); 

		public static final Command MONITORS_DISABLED = new Command(26); 

		/** Please update the monitors. */
		public static final Command MONITORS_UPDATE = new Command(27); 

		/** A gate has been selected for display. */
		public static final Command GATE_SELECT = new Command(30); 

		/** A gate has been added. */
		public static final Command GATE_ADD = new Command(31); 

		public static final Command GATE_SET_ON = new Command(32);

		public static final Command GATE_SET_OFF = new Command(33);

		public static final Command GATE_SET_SAVE = new Command(34);

		public static final Command GATE_SET_POINT = new Command(35);

		public static final Command GATE_SET_ADD = new Command(36);

		public static final Command GATE_SET_REMOVE = new Command(37);

		public static final Command COUNTERS_UPDATE = new Command(40);

		public static final Command COUNTERS_READ = new Command(41);

		public static final Command COUNTERS_ZERO = new Command(42);

		public static final Command SORT_MODE_CHANGED = new Command(50);

		public static final Command RUN_STATE_CHANGED = new Command(51);

		public static final Command FIT_NEW = new Command(60);

		private final int command;

		private Command(int i) {
			command = i;
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
	 * the command that is send see broadcaster for types
	 */
	public Command getCommand() {
		return command;
	}

	public Object getContent() {
		return content;
	}
}