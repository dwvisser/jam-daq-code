/*
 */
 package jam;
 import java.util.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.net.*;
 import jam.global.*;
 import jam.data.*;
 import jam.sort.*;
 
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
 interface FrontEndCommunication extends Observer{

    /** Setup up the networking to the Front End.
     * Called when Online data taking is setup.
     * @throws JamException when something goes wrong
     */
    public void setup()  throws JamException;
    
    /**
     * Reciever of distributed events
     * can listen for broadcasted event
     * Implementation of Observable interface
     */
    public void update(Observable observable, Object o);

    /**
     * Tell the Front End to start acquistion.
     */
    public void start();

    
    /**
     * Tell the Front End to stop acquisiton,
     * should also flushes out existing data in
     * the front end.
     */
    public void stop();
    
    /**
     * Tell the Front End to end a run.
     * should also flush out existing data 
     * in the front end and put a end of 
     * run marker at its end of the last buffer.
     */
    public void end();
    
    /**
     * Tell the Front End to fluss out the 
     * data buffer
     */
    public void flush();
    
    /**
     * Tell the VME to read the scalers
     * send back to packets the packet with the scaler values
     * and a packet if read OK or ERROR and message
     */
    public void readScalers();
    
    /**
     * Tell the Front to clear the scalers
     * sends a reply if OK or ERROR
     */
    public void clearScalers();
    
    /**
     * Tell the Front to read the counters
     * send back to packets the packet with the counter values
     * and a packet if read OK or ERROR and message
     */
    public void readCounters();
    
    /**
     * Tell the Font to zero the counters
     * Front end should a reply with a OK or ERROR
     */
    public void zeroCounters();
    
    /**
     * Tells the Front End to reply with verbose status messages, 
     * which can be output for the user to see.
     * @param state TRUE to set the front end in verbose mode     
     */
     public void verbose(boolean state);
    
    /**
     * Sets whether to receive front end replies and output
     * the messages to the console.
     */
     //public void setDisplayMessages(boolean state);
     
     /**
      * Gets whether front end replies are output to the console.
      */
     //public boolean getDisplayMessages();
     
    /**
     * Tells the Front End that we want it to put it to
     * debug mode.
     * @param state TRUE to set the front end in debug mode
     */
     public void debug(boolean state);
    
    /**
     * New version uploads CAMAC CNAF commands with udp pakets, and 
     * sets up the camac crate.
     *
     * @param camacCommands object containing CAMAC CNAF commands
     */
    public void setupCamac(CamacCommands camacCommands) throws JamException;

	public void setupVME_Map(VME_Map vmeMap) throws JamException;
	public void sendScalerInterval(int milliseconds) throws JamException;
    
    /** 
     * Method that is a deamon for receiving packets from the 
     * Front End.
     *
     * The first int of the packet is the status word
     * it determines how the packet is to be handled
     *
     */
    public void run();
}      
