package jam.simulator;


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
import java.nio.ByteBuffer;

/**
 * Simulator of front end
 *  start up with "java jam.simulator.SimulateFrontEnd"
 * 
 * @author Ken Swartz
 */
public final class SimulateFrontEnd  {
	
	private static final int MAX_MESSAGE_SIZE = 80;
	
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
				final ByteArrayInputStream messageBais = new ByteArrayInputStream(packetIn.getData());
				final ByteBuffer byteBuffer = ByteBuffer.wrap(packetIn.getData());			
				//final DataInput messageDis = new DataInputStream(messageBais);
				final int status = byteBuffer.getInt(); 
				if (status == OK_MESSAGE) {					
					final String text=unPackMessage(byteBuffer);					
					System.out.println( "Message: "+text );
				} else if (status == ERROR) {
					final String text=unPackMessage(byteBuffer);					
					System.out.println( "Error:"+text);					
				} else if (status == SCALER) {
					System.out.println( "Scaler:");	
				} else if (status == CNAF) {
					System.out.println( "CNAF: ");
				} else if (status == COUNTER) {
					System.out.println( "Counter");
				} else if (status == VME_ADDRESS ){
					System.out.println( "VME Address");
				} else if (status == INTERVAL) {
					System.out.println( "Message Counter");					
				} else {
					System.out.println( "Mesage Unknown");
				}
			}// end of receive message forever loop
		} catch (IOException ioe) {
			System.out.println( "Error in receiving messages");
		}
	}
	/**
	 * Unpack a datagram with a message. Message packets have an ASCII character
	 * array terminated with \0.
	 * 
	 * @param buffer
	 *            packet contents passed in readable form
	 * @return the string contained in the message
	 * @throws JamException
	 *             if there's a problem
	 */
	private String unPackMessage(final ByteBuffer buffer) {
		final StringBuilder rval = new StringBuilder();
		char next;
		do {
			next = (char)buffer.get();
			rval.append(next);
		} while (next != '\0');
		final int len = rval.length()-1;
		if (len > MAX_MESSAGE_SIZE) {// exclude null
			final IllegalArgumentException exception = new IllegalArgumentException(
					"Message length, "+len+", greater than max allowed, "+MAX_MESSAGE_SIZE+".");
			throw exception;
		}
		return rval.substring(0, len);
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