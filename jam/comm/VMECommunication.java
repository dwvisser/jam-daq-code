package jam.comm;

import jam.data.Scaler;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.GoodThread;
import jam.global.JamProperties;
import jam.global.PropertyKeys;
import jam.sort.CamacCommands;
import jam.sort.VME_Channel;
import jam.sort.VME_Map;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.logging.Level;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;

/**
 * Class to communicate with VME crate using UDP packets Two UDP sockets can be
 * setup with a daemon that receives packets and knows what to do with them The
 * first int of a packet indicates what type of packet it is.
 * 
 * @version 0.5 April 98
 * @author Ken Swartz and Dale Visser
 * @since JDK1.1
 */
public final class VMECommunication extends GoodThread implements
		FrontEndCommunication {

	private static final Broadcaster BROADCASTER = Broadcaster
			.getSingletonInstance();

	private static final VMECommunication INSTANCE = new VMECommunication();

	private static final Object LOCK = new Object();

	private static final StringUtilities STR_UTIL = StringUtilities
			.getInstance();

	/**
	 * 
	 * @return the one unique instance of this class
	 */
	public static VMECommunication getSingletonInstance() {
		return INSTANCE;
	}

	private transient boolean active;

	private transient InetAddress addressVME;

	// counter values, loaded when a counter packet is received.
	private transient final List<Integer> counterValues = new ArrayList<Integer>();

	// scaler values, loaded when a scaler packet is received.
	private transient final List<Integer> scalerValues = new ArrayList<Integer>();

	private transient DatagramSocket socketSend, socketReceive;

	private transient int vmePort;

	/**
	 * Creates the instance of this class for handling IP communications with
	 * the VME front end computer.
	 */
	private VMECommunication() {
		super();
		BROADCASTER.addObserver(this);
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
	private void bindSocketsAndSetActive(final String localInternetAddress,
			final int portSend, final int portRecv,
			final String vmeInternetAddress) throws CommunicationsException {
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
			throw new CommunicationsException("Problem creating send socket.",
					se);
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
		final Preferences prefs = CommunicationPreferences.PREFS;
		debug(prefs.getBoolean(CommunicationPreferences.DEBUG, false));
		verbose(prefs.getBoolean(CommunicationPreferences.VERBOSE, false));
	}

	/**
	 * Tells the VME to clear the scalers and send a reply: OK or ERROR.
	 */
	public void clearScalers() {
		final String RUN_CLEAR = "list clear";
		sendToVME(RUN_CLEAR);
	}

	/**
	 * Tells the VME whether to print out debugging statements.
	 * 
	 * @param state
	 *            true if we want debug messages from the VME
	 */
	public void debug(final boolean state) {
		final String DEBUG_ON = "debug on";
		final String DEBUG_OFF = "debug off";
		if (state) {
			sendToVME(DEBUG_ON);
		} else {
			sendToVME(DEBUG_OFF);
		}
	}

	/**
	 * Tells the front end to stop acquisiton and end the run, which flushes out
	 * the data buffer with an appended end-run marker.
	 */
	public void end() {
		final String END = "END";
		this.sendToVME(END);
	}

	/**
	 * Tells the front end to flush out the data buffer, and send the contents.
	 */
	public void flush() {
		final String FLUSH = "FLUSH";
		this.sendToVME(FLUSH);
	}

	/**
	 * Get the scaler values from the last read.
	 * 
	 * @return the values of the scalers
	 */
	public List<Integer> getScalers() {
		return Collections.unmodifiableList(scalerValues);
	}

	/**
	 * Method for opening a file for event storage on the VME host. (not used
	 * yet)
	 * 
	 * @param file
	 *            the filename to open
	 */
	public void openFile(final String file) {
		final String OPENFILE = "OPENFILE ";// add filename as an argument
		sendToVME(OPENFILE + file);
	}

	/**
	 * @see java.util.prefs.PreferenceChangeListener#preferenceChange(java.util.prefs.PreferenceChangeEvent)
	 */
	public void preferenceChange(final PreferenceChangeEvent pce) {
		final String key = pce.getKey();
		final String newValue = pce.getNewValue();
		final boolean state = Boolean.parseBoolean(newValue);
		if (key.equals(CommunicationPreferences.DEBUG)) {
			debug(state);
		} else if (key.equals(CommunicationPreferences.VERBOSE)) {
			verbose(state);
		}
	}

	/**
	 * Tells the VME to read the counters, and send back two packets: the packet
	 * with the counter values, and a status packet (read OK or ERROR) with a
	 * message.
	 */
	public void readCounters() {
		final String COUNT_READ = "count read";
		sendToVME(COUNT_READ);
	}

	/**
	 * Tell the VME to read the scalers, and send back two packets: the packet
	 * with the scaler values, and a status packet (read OK or ERROR) with a
	 * message.
	 */
	public void readScalers() {
		final String RUN_SCALER = "list scaler";
		this.sendToVME(RUN_SCALER);
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
						unPackScalers(byteBuffer);
						Scaler.update(scalerValues);
					} else if (status == PacketTypes.COUNTER.intValue()) {
						unPackCounters(byteBuffer);
						BROADCASTER.broadcast(
								BroadcastEvent.Command.COUNTERS_UPDATE,
								counterValues);
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
	 * Method to send a cnaf list to the VME crate
	 * 
	 * packet structure 16 bytes name 4 bytes number of cnafs 4 bytes for each
	 * cnaf
	 * 
	 * @param listName
	 *            name of cnaf list to send
	 * @param cnafList
	 *            the CAMAC commands
	 * @throws IOException
	 *             if there's a problem
	 */
	private void sendCNAFList(final String listName, final List<CNAF> cnafList)
			throws IOException {
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
		// put length of cnaf list in packet
		byteBuff.putInt(cnafList.size());
		// put list of cnaf commands into packet
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
		sendPacket(byteMessage);// send it
	}

	/**
	 * Send out a packet of the given bytes.
	 * 
	 * @param byteMessage
	 *            the message to send
	 * @throws IOException
	 *             if there's a problem
	 */
	private void sendPacket(final byte[] byteMessage) throws IOException {
		final DatagramPacket packetMessage = new DatagramPacket(byteMessage,
				byteMessage.length, addressVME, vmePort);
		if (socketSend == null) {
			throw new IllegalStateException("Send socket not setup.");
		}
		socketSend.send(packetMessage);
	}

	/**
	 * Send the interval in seconds between scaler blocks in the event stream.
	 * 
	 * @param seconds
	 *            the interval between scaler blocks
	 */
	public void sendScalerInterval(final int seconds) {
		final String message = seconds + "\n";
		sendToVME(PacketTypes.INTERVAL, message);
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
	private void sendToVME(final PacketTypes status, final String message) {
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

	/**
	 * Method which is used to send all packets containing a string to the VME
	 * crate.
	 * 
	 * @param message
	 *            string to send
	 */
	private void sendToVME(final String message) {
		sendToVME(PacketTypes.OK_MESSAGE, message);
	}

	/**
	 * Sets up networking. Two UDP sockets are created: one for receiving and
	 * one for sending. A daemon is also set up for receiving.
	 * 
	 * @throws CommunicationsException
	 *             if something goes wrong
	 */
	public void setupAcquisition() throws CommunicationsException {
		synchronized (LOCK) {
			final String LOCAL_IP = JamProperties
					.getPropString(PropertyKeys.HOST_IP);
			final int portSend = JamProperties
					.getPropInt(PropertyKeys.HOST_PORT_SEND);
			final int portRecv = JamProperties
					.getPropInt(PropertyKeys.HOST_PORT_RECV);
			final String VME_IP = JamProperties
					.getPropString(PropertyKeys.TARGET_IP);
			final int PORT_VME_SEND = JamProperties
					.getPropInt(PropertyKeys.TARGET_PORT);
			vmePort = PORT_VME_SEND;
			if (!active) {
				bindSocketsAndSetActive(LOCAL_IP, portSend, portRecv, VME_IP);
			}
		}
	}

	/**
	 * New version uploads CAMAC CNAF commands with udp pakets, and sets up the
	 * camac crate.
	 * 
	 * @param camacCommands
	 *            object containing CAMAC CNAF commands
	 * @throws IOException
	 *             if there's a problem
	 */
	public void setupCamac(final CamacCommands camacCommands)
			throws IOException {
		final String RUN_INIT = "list init";
		final String CNAF_INIT = "cnaf init";
		final String CNAF_EVENT = "cnaf event";
		final String CNAF_SCALER = "cnaf scaler";
		final String CNAF_CLEAR = "cnaf clear";
		// load CNAF's
		sendCNAFList(CNAF_INIT, camacCommands.getInitCommands());
		sendCNAFList(CNAF_EVENT, camacCommands.getEventCommands());
		sendCNAFList(CNAF_SCALER, camacCommands.getScalerCommands());
		sendCNAFList(CNAF_CLEAR, camacCommands.getClearCommands());
		this.sendToVME(RUN_INIT); // initialize camac
		LOGGER.info("Loaded CAMAC command lists, and initialized VME.");
	}

	/**
	 * Send the message specifying how to use ADC's and TDC's.
	 * 
	 * @param vmeMap
	 *            the map of channels to use and TDC ranges
	 * @throws IllegalStateException
	 *             if there are no parameters in the map
	 */
	public void setupVMEmap(final VME_Map vmeMap) {
		final StringBuffer temp = new StringBuffer();
		final List<VME_Channel> eventParams = vmeMap.getEventParameters();
		final Map<Integer, Byte> hRanges = vmeMap.getV775Ranges();
		if (eventParams.isEmpty()) {
			throw new IllegalStateException("No event parameters in map.");
		}
		final int totalParams = eventParams.size();
		final char endl = '\n';
		final char space = ' ';
		final String hex = "0x";
		temp.append(totalParams).append(endl);
		for (VME_Channel channel : eventParams) {
			temp.append(channel.getSlot()).append(space).append(hex).append(
					Integer.toHexString(channel.getBaseAddress()))
					.append(space).append(channel.getChannel()).append(space)
					.append(channel.getThreshold()).append(endl);
		}
		final int numRanges = hRanges.size();
		temp.append(numRanges).append(endl);
		if (numRanges > 0) {
			for (Map.Entry<Integer, Byte> entry : hRanges.entrySet()) {
				final int base = entry.getKey();
				temp.append(hex).append(Integer.toHexString(base))
						.append(space).append(entry.getValue()).append(endl);
			}
		}
		temp.append('\0');
		sendToVME(PacketTypes.VME_ADDRESS, temp.toString());
	}

	/**
	 * Tells the front end to start acquisiton.
	 */
	public void startAcquisition() {
		final String START = "START";
		sendToVME(START);
	}

	/**
	 * Tells the front end to stop acquisiton, which also flushes out a buffer.
	 */
	public void stopAcquisition() {
		final String STOPACQ = "STOP";
		sendToVME(STOPACQ);
	}

	/**
	 * Unpacks counters from udp packet. Packet format:
	 * <dl>
	 * <dt>int type
	 * <dd>SCALER (which is already read)
	 * <dt>int numScaler
	 * <dd>number of scalers
	 * <dt>int [] values
	 * <dd>scaler values
	 * </dl>
	 * 
	 * @param buffer
	 *            message in readable form
	 */
	private void unPackCounters(final ByteBuffer buffer) {
		synchronized (LOCK) {
			final int numCounter = buffer.getInt(); // number of
			// counters
			counterValues.clear();
			for (int i = 0; i < numCounter; i++) {
				counterValues.add(buffer.getInt());
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

	/**
	 * Unpack scalers from udp packet. Packet format:
	 * <ul>
	 * <li>int type SCALER (which is already read)
	 * <li>int numScaler number of scalers
	 * <li>int [] values scaler values
	 * </ul>
	 * 
	 * @param buffer
	 *            message in readable form
	 */
	private void unPackScalers(final ByteBuffer buffer) {
		synchronized (LOCK) {
			final int numScaler = buffer.getInt();// number of scalers
			scalerValues.clear();
			for (int i = 0; i < numScaler; i++) {
				scalerValues.add(buffer.getInt());
			}
		}
	}

	/**
	 * Receives distributed events. Can listen for broadcasted event.
	 * Implementation of Observable interface.
	 * 
	 * @param observable
	 *            object being observed
	 * @param message
	 *            additional parameter from <CODE>Observable</CODE> object
	 */
	public void update(final Observable observable, final Object message) {
		final BroadcastEvent event = (BroadcastEvent) message;
		final BroadcastEvent.Command command = event.getCommand();
		if (command == BroadcastEvent.Command.SCALERS_READ) {
			readScalers();
		} else if (command == BroadcastEvent.Command.SCALERS_CLEAR) {
			clearScalers();
		} else if (command == BroadcastEvent.Command.COUNTERS_READ) {
			readCounters();
		} else if (command == BroadcastEvent.Command.COUNTERS_ZERO) {
			zeroCounters();
		}
	}

	/**
	 * Tells the VME whether to send verbose verbose status messages, which Jam
	 * automatically prints on the console.
	 * 
	 * @param state
	 *            true if user wants VME to be verbose
	 */
	public void verbose(final boolean state) {
		final String VERBOSE_ON = "verbose on";
		final String VERBOSE_OFF = "verbose off";
		if (state) {
			sendToVME(VERBOSE_ON);
		} else {
			sendToVME(VERBOSE_OFF);
		}
	}

	/**
	 * Tells the VME to zero its counters and send a reply: OK or ERROR.
	 */
	public void zeroCounters() {
		final String COUNT_ZERO = "count zero";
		sendToVME(COUNT_ZERO);
	}

	public void close() {
		this.setState(State.SUSPEND);

		if (null != this.socketReceive) {
			socketReceive.close();
		}

		if (null != this.socketSend) {
			this.socketSend.close();
		}

		active = false;
	}
}
