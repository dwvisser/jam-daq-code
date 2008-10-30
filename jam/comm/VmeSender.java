package jam.comm;

interface VmeSender {

	/**
	 * Method which is used to send all packets containing a string to the VME
	 * crate.
	 * 
	 * @param message
	 *            string to send
	 */
	void sendMessage(final String message);

}