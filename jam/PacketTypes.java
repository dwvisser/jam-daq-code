package jam;

/**
 * Numerical header byte for different kinds of Jam datagram packets.
 * 
 * @author Dale Visser
 * 
 */
public interface PacketTypes {

	/**
	 * Standard informational message.
	 */
	int OK_MESSAGE = 0;// standard message

	/**
	 * Error message.
	 */
	int ERROR = 1;// message indicating error condition

	/**
	 * Message containing scaler values.
	 */
	int SCALER = 2;// received from VME, contains scaler values

	/**
	 * Message containing CNAF commands.
	 */
	int CNAF = 3;// sent to VME, contains CNAF commands

	/**
	 * Message containing front end event and buffer counters.
	 */
	int COUNTER = 4;

	/**
	 * Message containing CAEN VME electronics configuration info.
	 */
	int VME_ADDRESS = 5;// sent to VME, contains VME addressing information

	/**
	 * Message containing the interval in seconds at which to insert scalers in
	 * the event stream.
	 */
	int INTERVAL = 6;// sent to VME, contains interval to insert scalers in
						// event stream

}