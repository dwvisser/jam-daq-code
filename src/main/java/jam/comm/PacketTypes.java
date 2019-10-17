package jam.comm;

/**
 * Numerical header byte for different kinds of Jam datagram packets.
 * 
 * @author Dale Visser
 * 
 */
public enum PacketTypes {

	/**
	 * Standard informational message.
	 */
	OK_MESSAGE {
		public int intValue() {
			return 0;
		}
	},

	/**
	 * Error message.
	 */
	ERROR {
		public int intValue() {
			return 1;
		}
	},

	/**
	 * Message from front end containing scaler values.
	 */
	SCALER {
		public int intValue() {
			return 2;
		}
	},

	/**
	 * Message to VME containing CNAF commands.
	 */
	CNAF {
		public int intValue() {
			return 3;
		}
	},

	/**
	 * Message containing front end event and buffer counters.
	 */
	COUNTER {
		public int intValue() {
			return 4;
		}
	},

	/**
	 * Message to VME containing CAEN VME electronics configuration info.
	 */
	VME_ADDRESS {
		public int intValue() {
			return 5;
		}
	},

	/**
	 * Message to VME containing the interval in seconds at which to insert
	 * scalers in the event stream.
	 */
	INTERVAL {
		public int intValue() {
			return 6;
		}
	};

	/**
	 * 
	 * @return the 4-byte integer value that actually begins the packets
	 */
	abstract public int intValue();

}