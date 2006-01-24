package jam.simulator;

import jam.PacketTypes;
import jam.global.GoodThread;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Simulator of front end start up with "java jam.simulator.SimulateFrontEnd"
 * 
 * @author Ken Swartz
 */
public final class SimulateFrontEnd extends GoodThread implements PacketTypes {

	private static final int MAX_MESSAGE_SIZE = 80;
	private static final int MAX_DATA_PACKET_SIZE = 8192; 

	private static final byte STRING_NULL = (byte) 0x0;

	private static final int MAX_PACKET_SIZE = 1024;

	final static String HOST_IP = "localhost";

	final static String FRONTEND_IP = "localhost";

	final static int PORT_RECIEVE = 6002;

	final static int PORT_SEND = 6003;

	final static int PORT_SEND_HOST = 5002;

	final static int PORT_DATA = 6005;

	final static int PORT_DATA_HOST = 10205;

	private transient InetAddress addressHost = null;//NOPMD

	private transient InetAddress addressFrontEnd = null;//NOPMD

	private transient DatagramSocket socketSend;

	private transient DatagramSocket socketReceive;

	private boolean runState = false;
	
	private boolean runEnded=false;
	
	private ByteBuffer currentByteBuffer;

	/**
	 * Constructor.
	 */
	public SimulateFrontEnd() {
		super();
		LOGGER.info("Front End Simulator Started");
	}

	private void startCommunication() {
		createNetworkConnections();
		LOGGER.info("Created Sockets");
		receiveCommands();
	}

	private void createNetworkConnections() {
		try {// ceate a ports to send and receive
			addressHost = InetAddress.getByName(HOST_IP);
			addressFrontEnd = InetAddress.getByName(FRONTEND_IP);
		} catch (UnknownHostException ue) {
			LOGGER.info("Unknown local host " + FRONTEND_IP);
			return;
		}
		try {
			socketSend = new DatagramSocket(PORT_SEND, addressHost);
		} catch (BindException be) {
			LOGGER.info("Problem binding send socket: " + be.getMessage());
		} catch (SocketException se) {
			LOGGER.info("Problem creating send socket " + se.getMessage());
		}
		try {
			socketReceive = new DatagramSocket(PORT_RECIEVE, addressHost);
		} catch (BindException be) {
			LOGGER.info("Problem binding receive socket: " + be.getMessage());
		} catch (SocketException se) {
			LOGGER.info("Problem creating receive socket " + se.getMessage());
		}

	}

	/**
	 * Receive command
	 */
	public void receiveCommands() {
		LOGGER.info("Setup receive commands");
		final byte[] bufferIn = new byte[MAX_PACKET_SIZE];
		try {
			final DatagramPacket packetIn = new DatagramPacket(bufferIn,
					bufferIn.length);
			while (true) {// loop forever receiving packets
				socketReceive.receive(packetIn);
				final ByteBuffer byteBuffer = ByteBuffer.wrap(packetIn
						.getData());
				final int status = byteBuffer.getInt();
				if (status == OK_MESSAGE) {
					final String text = unPackMessage(byteBuffer);
					LOGGER.info("Message: " + text);
					performTask(text);
				} else if (status == ERROR) {
					final String text = unPackMessage(byteBuffer);
					LOGGER.info("Error:" + text);
				} else if (status == SCALER) {
					LOGGER.info("Scaler:");
				} else if (status == CNAF) {
					LOGGER.info("CNAF:");
				} else if (status == COUNTER) {
					LOGGER.info("Counte:r");
				} else if (status == VME_ADDRESS) {
					LOGGER.info("VME Address:");
				} else if (status == INTERVAL) {
					LOGGER.info("Interval:");
				} else {
					LOGGER.info("Message Unknown:");
				}
			}// end of receive message forever loop
		} catch (IOException ioe) {
			LOGGER.info("Error in receiving messages");
		}
	}

	/**
	 * Unpack a datagram with a message. Message packets have an ASCII character
	 * array terminated with \0.
	 * 
	 * @param buffer
	 *            packet contents passed in readable form
	 * @return the string contained in the message
	 */
	private String unPackMessage(final ByteBuffer buffer) {
		final StringBuilder rval = new StringBuilder();
		char next;
		do {
			next = (char) buffer.get();
			rval.append(next);
		} while (next != '\0');
		final int len = rval.length() - 1;
		if (len > MAX_MESSAGE_SIZE) {// exclude null
			final IllegalArgumentException exception = new IllegalArgumentException(
					"Message length, " + len + ", greater than max allowed, "
							+ MAX_MESSAGE_SIZE + ".");
			throw exception;
		}
		return rval.substring(0, len);
	}

	private void performTask(final String task) {
		if (task.equals("START")) {
			setRunState(true);
			replyMessage("Performed Start");
		} else if (task.equals("STOP")) {
			setRunState(false);
		} else if (task.equals("END")) {
			setRunState(false);
		}
	}

	private void replyMessage(final String message) {
		final DatagramPacket packetMessage;
		/* byte arrays initialized with zeros by definition */
		final byte[] byteMessage = new byte[message.length() + 5];
		byteMessage[3] = (byte) 0;// first four bytes interpreted together
		// as this number
		System.arraycopy(message.getBytes(), 0, byteMessage, 4, message
				.length());
		byteMessage[byteMessage.length - 1] = STRING_NULL;
		packetMessage = new DatagramPacket(byteMessage, byteMessage.length,
				addressHost, PORT_SEND_HOST);
		try {
			socketSend.send(packetMessage);
		} catch (IOException e) {
			LOGGER.info("Error sending packet " + e);
		}
	}

	/**
	 * Thread method: is a deamon for sending data packets.
	 */
	public void run() {
		LOGGER.info("Data Thread Started");
		DatagramSocket socketData = null;
		try {
			socketData = new DatagramSocket(PORT_DATA, addressFrontEnd);
		} catch (BindException be) {
			LOGGER.info("Problem binding data socket: " + be.getMessage());
		} catch (SocketException se) {
			LOGGER.info("Problem creating data socket " + se.getMessage());
		}
		final byte[] bufferSend = new byte[MAX_DATA_PACKET_SIZE];
		try {
			final DatagramPacket packetIn = new DatagramPacket(
					bufferSend, bufferSend.length, addressHost,
					PORT_DATA_HOST);
			while (true) {// loop forever
				try {
					while (waitRunState()) {						
						while(isRunState()) {
							LOGGER.info("Data Send");
							createDataPacket(bufferSend, false);
							packetIn.setData(bufferSend, 0, bufferSend.length);
							socketData.send(packetIn);
							sleep(1000); // sleep for a second
						}
						createDataPacket(bufferSend, true);						
					}

				} catch (InterruptedException ie) {
					LOGGER.info("Thread Interruped");
				}

			}// end of send data loop
		} catch (IOException ioe) {
			LOGGER.info("Unable to read datagram data Exception:" + ioe);
		}
	}

	private void createDataPacket(byte[] buffer, boolean lastBuffer) {

		currentByteBuffer= ByteBuffer.wrap(buffer);		
		writeSimpleLOO2Events(currentByteBuffer, lastBuffer);

	}
	
	private void writeSimpleLOO2Events(final ByteBuffer byteBuffer, boolean lastBuffer){
		short packWord;
		//Write a LOO2 event with 2 data values
		for (int i=0;i<400;i++) {
			byteBuffer.putShort((short)0x8001);
			byteBuffer.putShort((short)(500+i));
			byteBuffer.putShort((short)0x8002);
			byteBuffer.putShort((short)(500+i+4));
			byteBuffer.putShort((short)0xFFFF);
		}		
		byteBuffer.putShort((short)0xFFF0);
		byteBuffer.putShort((short)0xFFF0);
		
		//Pad buffer
		int pos = byteBuffer.position();		
		if (lastBuffer) {
			packWord=(short)0xFF03;
		}else {
			packWord=(short)0xFFF0;
		}
		for (int i=pos; i<MAX_DATA_PACKET_SIZE;i+=2){
			byteBuffer.putShort(packWord);
		}
		
		
	}
	//private void writeEvent(final ByteBuffer byteBuffer)

	private void setRunState(final boolean state) {
		synchronized (this) {
			runState = state;
			if (runState) {
				notifyAll(); 
			}
		}
	}

	private boolean waitRunState() {
		synchronized (this) {
			try {
				while (!runState) {					
					wait();// wait till start again
				}
			} catch (InterruptedException ie) {
				LOGGER.info("Thread Interruped");
			}
			return runState;
		}
	}
	private boolean isRunState() {
		synchronized (this) {
			return runState;
		}
	}
	/**
	 * Main method to run simulator
	 * 
	 * @param args
	 *            not used currently
	 */
	public static void main(final String args[]) {
		final SimulateFrontEnd sfe = new SimulateFrontEnd();
		sfe.start();
		sfe.startCommunication();
	}

}