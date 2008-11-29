package jam.comm;

import jam.data.Scaler;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.GoodThread;
import jam.global.JamProperties;
import jam.global.PropertyKeys;
import jam.sort.CamacCommands.CNAF;
import jam.util.StringUtilities;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Class to communicate with VME crate using UDP packets Two UDP sockets can be
 * setup with a daemon that receives packets and knows what to do with them The
 * first int of a packet indicates what type of packet it is.
 * 
 * @version 0.5 April 98
 * @author Ken Swartz and Dale Visser
 * @since JDK1.1
 */
@Singleton
final class VMECommunication extends GoodThread implements VmeSender {

	private transient final Broadcaster BROADCASTER;

	private static final Object LOCK = new Object();

	private static final StringUtilities STR_UTIL = StringUtilities
			.getInstance();

	private transient boolean active;

	private transient InetAddress addressVME;

	private transient DatagramSocket socketSend, socketReceive;

	private transient int vmePort;

	/**
	 * Creates the instance of this class for handling IP communications with
	 * the VME front end computer.
	 */
	@Inject
	protected VMECommunication(final Broadcaster broadcaster) {
		super();
		this.BROADCASTER = broadcaster;
		new CounterVMECommunicator(this, broadcaster);
		this.setName("Front End Communication");
		this.setDaemon(true);
		this.setPriority(jam.sort.ThreadPriorities.MESSAGING);
		this.setState(State.SUSPEND);
		this.start();
	}

	/**
	 * @param addressLocal
	 * @param localInternetAddress
	 * @param portSend
	 * @param portRecv
	 * @param vmeInternetAddress
	 * @throws CommunicationsException
	 */
	protected void bindSocketsAndSetActive(
			final String localInternetAddress,// NOPMD
			final int portSend, final int portRecv,
			final String vmeInternetAddress) throws CommunicationsException {// NOPMD
		this.setVMEport();
		if (!active) {
			InetAddress addressLocal;
			try {// create a ports to send and receive
				addressLocal = InetAddress.getByName(localInternetAddress);
			} catch (UnknownHostException ue) {
				throw new CommunicationsException("Unknown local host: "
						+ localInternetAddress, ue);
			}
			try {
				addressVME = InetAddress.getByName(vmeInternetAddress);
			} catch (UnknownHostException ue) {
				throw new CommunicationsException("Unknown VME host: "
						+ vmeInternetAddress, ue);
			}
			try {
				socketSend = new DatagramSocket(portSend, addressLocal);
			} catch (BindException be) {
				throw new CommunicationsException(
						"Problem binding send socket. (Is another copy of Jam running online?)",
						be);
			} catch (SocketException se) {
				throw new CommunicationsException(
						"Problem creating send socket.", se);
			}
			try {
				socketReceive = new DatagramSocket(portRecv, addressLocal);
			} catch (BindException be) {
				throw new CommunicationsException(
						getClass().getName()
								+ "Problem binding receive socket. (Is another Jam running online?)",
						be);
			} catch (SocketException se) {
				throw new CommunicationsException(
						"Problem creating receive socket.", se);
			}
			// Setup and start receiving daemon.
			this.setState(State.RUN);
			active = true;
		}
	}

	protected void close() {
		this.setState(State.SUSPEND);

		if (null != this.socketReceive) {
			socketReceive.close();
		}

		if (null != this.socketSend) {
			this.socketSend.close();
		}

		active = false;
	}

	protected boolean isActive() {
		synchronized (LOCK) {
			return this.active;
		}
	}

	protected void log(final String message) {
		GoodThread.LOGGER.info(message);
	}

	/**
	 * Thread method: is a daemon for receiving packets from the VME crate. The
	 * first int of the packet is the status word. It determines how the packet
	 * is to be handled.
	 */
	@Override
	public void run() {
		final byte[] bufferIn = new byte[Constants.MAX_PACKET_SIZE];
		try {
			final DatagramPacket packetIn = new DatagramPacket(bufferIn,
					bufferIn.length);
			/* 8 is a typical scaler unit size */
			final List<Integer> unpackedValues = new ArrayList<Integer>(8);
			while (checkState()) {// loop forever receiving packets
				try {
					synchronized (LOCK) {
						socketReceive.receive(packetIn);
					}
					final ByteBuffer byteBuffer = ByteBuffer.wrap(packetIn
							.getData());
					final int status = byteBuffer.getInt();// .readInt();
					if (status == PacketTypes.OK_MESSAGE.intValue()) {
						LOGGER.info(getClass().getName() + ": "
								+ unPackMessage(byteBuffer));
					} else if (status == PacketTypes.SCALER.intValue()) {
						unPackCounters(byteBuffer, unpackedValues);
						Scaler.update(unpackedValues);
					} else if (status == PacketTypes.COUNTER.intValue()) {
						unPackCounters(byteBuffer, unpackedValues);
						BROADCASTER.broadcast(
								BroadcastEvent.Command.COUNTERS_UPDATE,
								unpackedValues);
					} else if (status == PacketTypes.ERROR.intValue()) {
						LOGGER.severe(getClass().getName() + ": "
								+ unPackMessage(byteBuffer));
					} else {
						LOGGER
								.severe(getClass().getName()
										+ ": packet with unknown message type received");
					}
				} catch (SocketException se) {
					LOGGER.info("Receive socket was closed.");
				}
			}// end of receive message forever loop
		} catch (IOException ioe) {
			LOGGER.log(Level.SEVERE, "Unable to read datagram status word.",
					ioe);
			LOGGER
					.warning("Network receive daemon stopped, need to restart Online.");
		}
	}

	/**
	 * Method to send a CNAF list to the VME crate
	 * 
	 * packet structure 16 bytes name 4 bytes number of CNAF's 4 bytes for each
	 * CNAF
	 * 
	 * @param listName
	 *            name of CNAF list to send
	 * @param cnafList
	 *            the CAMAC commands
	 * @throws IOException
	 *             if there's a problem
	 */
	protected void sendCNAFList(final String listName, final List<CNAF> cnafList)
			throws IOException {
		GoodThread.LOGGER.entering(VMECommunication.class.getName(),
				"sendCNAFList", listName);
		final int COMMAND_SIZE = 16;
		final int CNAF_SIZE = 9;
		if (listName.length() > (COMMAND_SIZE - 1)) {
			throw new IllegalArgumentException("Command list length too long.");
		}
		final byte[] byteMessage = new byte[4 + COMMAND_SIZE + 4
				+ cnafList.size() * CNAF_SIZE + 1];
		final ByteBuffer byteBuff = ByteBuffer.wrap(byteMessage);
		byteBuff.putInt(PacketTypes.CNAF.intValue());
		// put command string into packet
		final byte[] asciiListName = STR_UTIL.getASCIIarray(listName);
		byteBuff.put(asciiListName);
		for (int i = COMMAND_SIZE; i > asciiListName.length; i--) {
			byteBuff.put(Constants.STRING_NULL);
		}
		// put length of CNAF list in packet
		byteBuff.putInt(cnafList.size());
		// put list of CNAF commands into packet
		for (int i = 0; i < cnafList.size(); i++) {
			final CNAF cnaf = cnafList.get(i);
			byteBuff.put(cnaf.getParamID());
			byteBuff.put(cnaf.getCrate());
			byteBuff.put(cnaf.getNumber());
			byteBuff.put(cnaf.getAddress());
			byteBuff.put(cnaf.getFunction());
			byteBuff.putInt(cnaf.getData());
		}
		// add a null character
		byteBuff.put(Constants.STRING_NULL);
		final DatagramPacket packetMessage = new DatagramPacket(byteMessage,
				byteMessage.length, addressVME, vmePort);
		if (socketSend == null) {
			throw new IllegalStateException("Send socket not setup.");
		}
		socketSend.send(packetMessage);
		LOGGER.exiting(VMECommunication.class.getName(), "sendCNAFList");
	}

	/**
	 * Method which is used to send all packets containing a string to the VME
	 * crate.
	 * 
	 * @param status
	 *            one of OK, SCALER, ERROR, CNAF, COUNTER, VME_ADDRESSES or
	 *            SCALER_INTERVAL
	 * @param message
	 *            string to send
	 * @throws IllegalArgumentException
	 *             if an unrecognized status is given
	 * @throws IllegalStateException
	 *             if we haven't established a connection yet
	 */
	protected void sendToVME(final PacketTypes status, final String message) {
		final Object[] params = { status, message };
		LOGGER.entering(VMECommunication.class.getName(), "sendToVME", params);
		if (socketSend == null) {
			throw new IllegalStateException(
					"Attempted to send a message without a connection.");
		}
		final DatagramPacket packetMessage = PacketBuilder.getInstance()
				.message(status, message, addressVME, vmePort);
		try {// create and send packet
			socketSend.send(packetMessage);
		} catch (IOException e) {
			LOGGER
					.log(
							Level.SEVERE,
							"Jam encountered a network communication error attempting to send a packet.",
							e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.comm.VmeSender#sendToVME(java.lang.String)
	 */
	public void sendMessage(final String message) {
		sendToVME(PacketTypes.OK_MESSAGE, message);
	}

	private void setVMEport() {
		synchronized (LOCK) {
			final int PORT_VME_SEND = JamProperties
					.getPropInt(PropertyKeys.TARGET_PORT);
			vmePort = PORT_VME_SEND;
		}
	}

	/**
	 * Unpacks counters from udp packet. Packet format:
	 * <dl>
	 * <dt>int type
	 * <dd>SCALER or COUNTER (which is already read)
	 * <dt>int numScaler
	 * <dd>number of scalers
	 * <dt>int [] values
	 * <dd>scaler values
	 * </dl>
	 * 
	 * @param buffer
	 *            message in readable form
	 */
	private void unPackCounters(final ByteBuffer buffer,
			final List<Integer> destination) {
		synchronized (LOCK) {
			final int numCounter = buffer.getInt(); // number of
			// counters
			destination.clear();
			for (int i = 0; i < numCounter; i++) {
				destination.add(buffer.getInt());
			}
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
			LOGGER.throwing("VMECommunication", "unPackMessage", exception);
			throw exception;
		}
		return rval.substring(0, len);
	}
}
