package jam.comm;

import jam.sort.CamacCommands;
import jam.sort.VME_Map;

import java.io.IOException;

/**
 * Interface that generalizes to network communicate with a front end using UDP
 * packets.
 * 
 * Two UDP sockets can be setup with a daemon that receives packets and knows
 * what to do with them The first int of a packet indicates what type of packet
 * it is.
 * 
 * @version 0.5 August 2000
 * @author Ken Swartz
 * @since JDK1.1
 */
public interface FrontEndCommunication {

	/**
	 * Tells the Front End that we want it to put it to debug mode.
	 * 
	 * @param state
	 *            TRUE to set the front end in debug mode
	 */
	void debug(boolean state);

	/**
	 * Tell the Front End to end a run. should also flush out existing data in
	 * the front end and put a end of run marker at its end of the last buffer.
	 */
	void end();

	/**
	 * Tell the Front End to flush out the data buffer
	 */
	void flush();

	/**
	 * Setup up the networking to the Front End. Called when Online data taking
	 * is setup.
	 * 
	 * @throws CommunicationsException
	 *             when something goes wrong
	 */
	void setupAcquisition() throws CommunicationsException;

	/**
	 * New version uploads CAMAC CNAF commands with udp pakets, and sets up the
	 * camac crate.
	 * 
	 * @param commands
	 *            object containing CAMAC CNAF commands
	 * @throws IOException
	 *             if there is a problem setting up
	 */
	void setupCamac(CamacCommands commands) throws IOException;

	/**
	 * Send the map of VME parameters to the front end.
	 * 
	 * @param vmeMap
	 *            which channels to use in the electronics
	 */
	void setupVMEmap(VME_Map vmeMap);

	/**
	 * Tell the Front End to start acquistion.
	 */
	void startAcquisition();

	/**
	 * Tell the Front End to stop acquisiton, should also flushes out existing
	 * data in the front end.
	 */
	void stopAcquisition();

	/**
	 * Tells the Front End to reply with verbose status messages, which can be
	 * output for the user to see.
	 * 
	 * @param state
	 *            TRUE to set the front end in verbose mode
	 */
	void verbose(boolean state);

	/**
	 * Closes any bound communications channels that are open.
	 */
	void close();
}
