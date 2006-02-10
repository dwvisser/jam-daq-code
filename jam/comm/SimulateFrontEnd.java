package jam.comm;

import static jam.comm.PacketTypes.CNAF;
import static jam.comm.PacketTypes.COUNTER;
import static jam.comm.PacketTypes.ERROR;
import static jam.comm.PacketTypes.INTERVAL;
import static jam.comm.PacketTypes.OK_MESSAGE;
import static jam.comm.PacketTypes.SCALER;
import static jam.comm.PacketTypes.VME_ADDRESS;
import jam.global.GoodThread;
import jam.sort.stream.L002Parameters;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

/**
 * Simulator of front end start up with "java
 * jam.comm.simulator.SimulateFrontEnd"
 * 
 * @author Ken Swartz
 */
public final class SimulateFrontEnd extends GoodThread {

	final static String HOST_IP = "localhost";

	final static String FRONTEND_IP = "localhost";

	final static int PORT_RECIEVE = 6002;

	final static int PORT_SEND = 6003;

	final static int PORT_SEND_HOST = 5002;

	final static int PORT_DATA = 6005;

	final static int PORT_DATA_HOST = 10205;

	private transient InetAddress addressHost = null;// NOPMD

	private transient InetAddress addressFrontEnd = null;// NOPMD

	private transient DatagramSocket socketSend;

	private transient DatagramSocket socketReceive;

	private boolean runState = false;

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
			socketSend = new DatagramSocket(PORT_SEND, addressFrontEnd);
		} catch (BindException be) {
			LOGGER.info("Problem binding send socket: " + be.getMessage());
		} catch (SocketException se) {
			LOGGER.info("Problem creating send socket " + se.getMessage());
		}
		try {
			socketReceive = new DatagramSocket(PORT_RECIEVE, addressFrontEnd);
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
		final byte[] bufferIn = new byte[Constants.MAX_PACKET_SIZE];
		try {
			final DatagramPacket packetIn = new DatagramPacket(bufferIn,
					bufferIn.length);
			while (true) {// loop forever receiving packets
				socketReceive.receive(packetIn);
				final ByteBuffer byteBuffer = ByteBuffer.wrap(packetIn
						.getData());
				final int status = byteBuffer.getInt();
				if (status == OK_MESSAGE.intValue()) {
					final String text = unPackMessage(byteBuffer);
					LOGGER.info("Message: " + text);
					performTask(text);
				} else if (status == ERROR.intValue()) {
					final String text = unPackMessage(byteBuffer);
					LOGGER.info("Error:" + text);
				} else if (status == SCALER.intValue()) {
					LOGGER.info("Scaler:");
				} else if (status == CNAF.intValue()) {
					LOGGER.info("CNAF:");
				} else if (status == COUNTER.intValue()) {
					LOGGER.info("Counte:r");
				} else if (status == VME_ADDRESS.intValue()) {
					LOGGER.info("VME Address:");
				} else if (status == INTERVAL.intValue()) {
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
		if (len > Constants.MAX_MESSAGE_SIZE) {// exclude null
			final IllegalArgumentException exception = new IllegalArgumentException(
					"Message length, " + len + ", greater than max allowed, "
							+ Constants.MAX_MESSAGE_SIZE + ".");
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
		final DatagramPacket packetMessage = PacketBuilder.getInstance()
				.message(OK_MESSAGE, message, addressHost, PORT_SEND_HOST);
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
		final byte[] bufferArray = new byte[Constants.MAX_DATA_SIZE];
		final ByteBuffer buffer = ByteBuffer.wrap(bufferArray);
		try {
			final DatagramPacket packetIn = new DatagramPacket(bufferArray,
					bufferArray.length, addressHost, PORT_DATA_HOST);
			while (true) {// loop forever
				try {
					while (waitRunState()) {
						while (isRunState()) {
							LOGGER.info("Data Send");
							createDataPacket(buffer, false);
							packetIn
									.setData(bufferArray, 0, bufferArray.length);
							socketData.send(packetIn);
							sleep(1000); // sleep for a second
						}
						createDataPacket(buffer, true);
					}

				} catch (InterruptedException ie) {
					LOGGER.info("Thread Interruped");
				}

			}// end of send data loop
		} catch (IOException ioe) {
			LOGGER.info("Unable to read datagram data Exception:" + ioe);
		}
	}

	private void createDataPacket(final ByteBuffer buffer,
			final boolean lastBuffer) {
		buffer.rewind();
		writeSimpleLOO2Events(buffer.asShortBuffer(), lastBuffer);
	}

	private void writeSimpleLOO2Events(final ShortBuffer buffer,
			final boolean lastBuffer) {
		// Write a LOO2 event with 2 data values
		final short param1 = L002Parameters.EVENT_PARAMETER & 1;
		final short param2 = L002Parameters.EVENT_PARAMETER & 2;
		for (int i = 0; i < 400; i++) {
			buffer.put(param1);
			buffer.put((short) (500 + i));
			buffer.put(param2);
			buffer.put((short) (500 + i + 4));
			buffer.put(L002Parameters.EVENT_END_MARKER);
		}
		buffer.put((short) 0xFFF0);
		buffer.put((short) 0xFFF0);
		// Pad buffer
		final short packWord = lastBuffer ? L002Parameters.RUN_END_MARKER
				: L002Parameters.BUFFER_END_MARKER;
		while (buffer.remaining() > 0) {
			buffer.put(packWord);
		}

	}

	// private void writeEvent(final ByteBuffer byteBuffer)

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