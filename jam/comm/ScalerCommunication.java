package jam.comm;

/**
 * Methods for communicating scaler information with front end.
 * 
 * @author Dale Visser
 * 
 */
public interface ScalerCommunication {
	/**
	 * Tell the Front to clear the scalers sends a reply if OK or ERROR
	 */
	void clearScalers();

	/**
	 * Tell the VME to read the scalers send back to packets the packet with the
	 * scaler values and a packet if read OK or ERROR and message
	 */
	void readScalers();

	/**
	 * Send the number of milliseconds between blocks of scaler values in the
	 * event stream.
	 * 
	 * @param milliseconds
	 */
	void sendScalerInterval(int milliseconds);
}
