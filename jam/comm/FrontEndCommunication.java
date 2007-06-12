package jam.comm;

import jam.sort.CamacCommands;
import jam.sort.VME_Map;

import java.io.IOException;
import java.util.Observer;
import java.util.prefs.PreferenceChangeListener;
 
/**
 * Interface that generalizes to network communicate 
 * with a front end using udp packets
 *
 * Two udp sockets can be setup with a daemon that
 * receives packets and knows what to do with them
 * The first int of a packet indicates what type of packe
 * it is.
 *
 * @version  0.5  August 2000
 * @author   Ken Swartz
 * @since       JDK1.1
 */
public interface FrontEndCommunication extends Observer, PreferenceChangeListener {

	/** 
     * Setup up the networking to the Front End.
     * Called when Online data taking is setup.
     * 
     * @throws JamException when something goes wrong
     */
    void setupAcquisition()  throws CommunicationsException;
    
    /**
     * Tell the Front End to start acquistion.
     */
    void startAcquisition();

    
    /**
     * Tell the Front End to stop acquisiton,
     * should also flushes out existing data in
     * the front end.
     */
    void stopAcquisition();
    
    /**
     * Tell the Front End to end a run.
     * should also flush out existing data 
     * in the front end and put a end of 
     * run marker at its end of the last buffer.
     */
    void end();
    
    /**
     * Tell the Front End to fluss out the 
     * data buffer
     */
    void flush();
    
    /**
     * Tell the VME to read the scalers
     * send back to packets the packet with the scaler values
     * and a packet if read OK or ERROR and message
     */
    void readScalers();
    
    /**
     * Tell the Front to clear the scalers
     * sends a reply if OK or ERROR
     */
    void clearScalers();
    
    /**
     * Tell the Front to read the counters
     * send back to packets the packet with the counter values
     * and a packet if read OK or ERROR and message
     */
    void readCounters();
    
    /**
     * Tell the Font to zero the counters
     * Front end should a reply with a OK or ERROR
     */
    void zeroCounters();
    
    /**
     * Tells the Front End to reply with verbose status messages, 
     * which can be output for the user to see.
     * @param state TRUE to set the front end in verbose mode     
     */
    void verbose(boolean state);
    
    /**
     * Tells the Front End that we want it to put it to
     * debug mode.
     * @param state TRUE to set the front end in debug mode
     */
     void debug(boolean state);
    
    /**
     * New version uploads CAMAC CNAF commands with udp pakets, and 
     * sets up the camac crate.
     *
     * @param commands object containing CAMAC CNAF commands
     * @throws JamException if there is a problem setting up
     */
    void setupCamac(CamacCommands commands) throws IOException;

    /**
     * Send the map of VME parameters to the front end.
     * 
     * @param vmeMap which channels to use in the electronics
     * @throws JamException if there is a problem setting up
     */
	void setupVMEmap(VME_Map vmeMap);
	
	/**
	 * Send the number of milliseconds between blocks of scaler
	 * values in the event stream.
	 * 
	 * @param milliseconds 
	 * @throws JamException if there's a problem sending
	 */
	void sendScalerInterval(int milliseconds);
    
    /** 
     * Method that is a deamon for receiving packets from the 
     * Front End.
     *
     * The first int of the packet is the status word
     * it determines how the packet is to be handled
     *
     */
    void run();
}      
