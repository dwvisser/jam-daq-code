package jam.simulator;


import jam.data.Scaler;
import jam.global.BroadcastEvent;
import jam.global.GoodThread;

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
import java.util.logging.Level;

import javax.swing.JOptionPane;

/**
 * Simulator of front end
 *  start up with "java jam.simulator.SimulateFrontEnd"
 * 
 * @author Ken Swartz
 */
public final class SimulateFrontEnd extends GoodThread {
	
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
	
	//final String LOCAL_HOST="localhost";
	
	final String HOST_IP="localhost";
	final String FRONTEND_IP="localhost"; 
	final int PORT_RECIEVE=6002;
	final int PORT_SEND=6003;
	final int PORT_SEND_HOST=6003;
	final int PORT_DATA=6005;	
	final int PORT_DATA_HOST=6006;
	
	private InetAddress addressHost=null;
	private InetAddress addressFrontEnd=null;	

	private transient DatagramSocket socketSend; 
	private transient DatagramSocket socketReceive;
	
	private Object isRun= new Object();
	private boolean runState=false;
	

	public SimulateFrontEnd()
	{
		System.out.println("Front End Simulator Started");		
		start();		
	}
	

	public void startCommunication()
	{
		createNetworkConnections();
		System.out.println("Created Sockets");
		
		receiveCommands();	
	}
	
	private void createNetworkConnections()
	{
		
		
		try {// ceate a ports to send and receive

			addressHost = InetAddress.getByName(HOST_IP);
			addressFrontEnd = InetAddress.getByName(FRONTEND_IP);			
		} catch (UnknownHostException ue) {			
			System.out.println( "Unknown local host " + FRONTEND_IP);
			return;
		}		
		try {
			socketSend = new DatagramSocket(PORT_SEND, addressHost);
		} catch (BindException be) {			
			System.out.println( "Problem binding send socket: "+be.getMessage());			
		} catch (SocketException se) {
			System.out.println( "Problem creating send socket "+se.getMessage());			
		}
		try {
			socketReceive = new DatagramSocket(PORT_RECIEVE, addressHost);
		} catch (BindException be) {
			System.out.println( "Problem binding receive socket: "+be.getMessage());			
		} catch (SocketException se) {
			System.out.println( "Problem creating receive socket "+se.getMessage());			
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
				final ByteBuffer byteBuffer = ByteBuffer.wrap(packetIn.getData());			
				//final DataInput messageDis = new DataInputStream(messageBais);
				final int status = byteBuffer.getInt(); 
				if (status == OK_MESSAGE) {					 
					final String text=unPackMessage(byteBuffer);		 			
					System.out.println( "Message: "+text );
					performTask(text);
				} else if (status == ERROR) {
					final String text=unPackMessage(byteBuffer);					
					System.out.println( "Error:"+text);					
				} else if (status == SCALER) {
					System.out.println( "Scaler:");	
				} else if (status == CNAF) {
					System.out.println( "CNAF:");
				} else if (status == COUNTER) {
					System.out.println( "Counte:r");
				} else if (status == VME_ADDRESS ){
					System.out.println( "VME Address:");
				} else if (status == INTERVAL) {
					System.out.println( "Interval:");					
				} else {
					System.out.println( "Message Unknown:");
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
	
	private void performTask(String task) {
		if (task.equals("START")) {
			setRunState(true);
		} else if (task.equals("STOP")) {
			setRunState(false);
		}else if (task.equals("END")) {
			setRunState(false);
		}
	}
	 
	
	/**
	 * Thread method: is a deamon for sending data packets.
	 */
	public void run() {
		System.out.println("Data Thread Started");
		DatagramSocket socketData=null; 
		
		try {
			socketData = new DatagramSocket(PORT_DATA, addressFrontEnd);
		} catch (BindException be) {
			System.out.println( "Problem binding data socket: "+be.getMessage());			
		} catch (SocketException se) {
			System.out.println( "Problem creating data socket "+se.getMessage());			
		}		
		
		final byte[] bufferSend = new byte[MAX_PACKET_SIZE];
		try {

			while(true) {// loop forever 				
				try {
								
					while (isRunState()) {
						System.out.println("Data Send");
						createData(bufferSend)	;
						final DatagramPacket packetIn = new DatagramPacket(bufferSend, bufferSend.length, addressHost, PORT_DATA_HOST);				
						socketData.send(packetIn);
						sleep(1000);	//sleep for a second
					}
				} catch (InterruptedException ie) { 
					System.out.println ("Thread Interruped");
				}
				
			}// end of send data loop
		} catch (IOException ioe) {
			System.out.println("Unable to read datagram data Exception:"+ ioe);
		} 
	}
	
	private void createData(byte [] buffer)	{
		buffer[0]=(byte)1;
		buffer[1]=(byte)2;
		buffer[2]=(byte)3;
		buffer[3]=(byte)4;		
	}
	
	private synchronized void setRunState(boolean state)
	{
		runState=state;
		
		if(runState) {
			notifyAll(); 	
		}
	}
	private synchronized boolean isRunState() {
		try {		
			if (!runState) {
				wait();//wait till start again
			}

		} catch (InterruptedException ie) {
			System.out.println ("Thread Interruped");
		}
		return runState;		
	}
	/**
	 * Main method to run simulator
	 * 
	 * @param args
	 *            not used currently
	 */
	public static void main(final String args[]) {
		SimulateFrontEnd sfe= new SimulateFrontEnd();
		
		sfe.startCommunication();

	}
	
}