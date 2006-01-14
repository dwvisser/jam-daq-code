package jam.simulator;

import jam.JamException;
import jam.data.Scaler;
import jam.global.BroadcastEvent;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Simulator of front end
 *  start up with "java jam.simulator.SimulateFrontEnd"
 * 
 * @author Ken Swartz
 */
public final class SimulateFrontEnd  {
	
	/**
	 * Standard informational message.
	 */
	final int OK_MESSAGE=0;//standard message
	
	/**
	 * Error message.
	 */
	final int ERROR=1;//message indicating error condition
	
	/**
	 * Message containing scaler values.
	 */
	final int SCALER=2;//received from VME, contains scaler values
	
	/**
	 * Message containing CNAF commands.
	 */
	final int CNAF=3;//sent to VME, contains CNAF commands
	
	/**
	 * Message containing front end event and buffer counters.
	 */
	final int COUNTER=4;
	
	/**
	 * Message containing CAEN VME electronics configuration info.
	 */
	final int VME_ADDRESS=5;//sent to VME, contains VME addressing information
	
	/**
	 * Message containing the interval in seconds at which to 
	 * insert scalers in the event stream.
	 */
	final int INTERVAL=6;//sent to VME, contains interval to insert scalers in event stream
	

	private static final int MAX_PACKET_SIZE = 1024;
	
	final String LOCAL_HOST="localhost";
	final String HOST_IP="localhost";
	final String TARGET_IP="localhost"; 
	final int PORT_RECIEVE=6002;
	final int PORT_SEND=6003; 
	
	private transient DatagramSocket socketSend; 
	private transient DatagramSocket socketReceive;
	

	public SimulateFrontEnd()
	{
		System.out.println("Front End Simulator Started");
		
		createNetworkConnections();

		receiveCommands();	
	}
	
	private void createNetworkConnections()
	{
		InetAddress addressLocal=null;
		
		try {// ceate a ports to send and receive
			addressLocal = InetAddress.getByName(LOCAL_HOST);
		} catch (UnknownHostException ue) {			
			System.out.println( "Unknown local host " + LOCAL_HOST);
			return;
		}		
		try {
			socketSend = new DatagramSocket(PORT_SEND, addressLocal);
		} catch (BindException be) {
			System.out.println( "Problem binding send socket: ");			
			//System.out.println( "Problem binding send socket: "+be.getMessage());
			
		} catch (SocketException se) {
			System.out.println( "Problem creating send socket ");
			//System.out.println( "Problem creating send socket "+se.getMessage());			
		}
		try {
			socketReceive = new DatagramSocket(PORT_RECIEVE, addressLocal);
		} catch (BindException be) {
			System.out.println( "Problem binding receive socket: ");
			//System.out.println( "Problem binding receive socket: "+be.getMessage());			
		} catch (SocketException se) {
			System.out.println( "Problem creating receive socket ");
			//System.out.println( "Problem creating receive socket "+se.getMessage());			
		}		
		
	}
	
	/**
	 * Receive command
	 */
	public void receiveCommands() {
		System.out.println("Setup receive commands");
		final byte[] bufferIn = new byte[MAX_PACKET_SIZE];
		try {
			while (true) {// loop forever receiving packets
				final DatagramPacket packetIn = new DatagramPacket(bufferIn,
						bufferIn.length);
				socketReceive.receive(packetIn);
				System.out.println("Packet received");
				final ByteArrayInputStream messageBais = new ByteArrayInputStream(
						packetIn.getData());
				final DataInput messageDis = new DataInputStream(messageBais);
				final int status = messageDis.readInt();
				if (status == OK_MESSAGE) {
					System.out.println( "Mesage OK");						
				} else if (status == SCALER) {
					System.out.println( "Mesage Scaler");					
				} else if (status == COUNTER) {
					System.out.println( "Mesage Counter");					
				} else if (status == ERROR) {
					System.out.println( "Mesage Error");
				} else {
					System.out.println( "Mesage Unknown");
				}
			}// end of receive message forever loop
		} catch (IOException ioe) {
			System.out.println( "Error in receiving messages");
		}
	}
	
	/**
	 * Main method to run simulator
	 * 
	 * @param args
	 *            not used currently
	 */
	public static void main(final String args[]) {
		new SimulateFrontEnd();
	}
	
}