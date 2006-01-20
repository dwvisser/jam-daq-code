package jam;

import jam.data.Scaler;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.GoodThread;
import jam.global.JamProperties;
import jam.sort.CamacCommands;
import jam.sort.VME_Channel;
import jam.sort.VME_Map;
import jam.sort.CamacCommands.CNAF;

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
 * Class to communicate with VME crate using udp packets Two udp sockets can be
 * setup with a daemon that receives packets and knows what to do with them The
 * first int of a packet indicates what type of packe it is.
 * 
 * @version 0.5 April 98
 * @author Ken Swartz and Dale Visser
 * @since JDK1.1
 */
public class VMECommunication extends GoodThread implements
		FrontEndCommunication {

	private static final int MAX_PACKET_SIZE = 1024;

	private static final int MAX_MESSAGE_SIZE = 80;

	private static final byte STRING_NULL = (byte) 0x0;

	private static final Broadcaster BROADCASTER = Broadcaster
			.getSingletonInstance();

	// private transient final MessageHandler console;

	private transient InetAddress addressVME;

	private transient int vmePort;

	private transient DatagramSocket socketSend, socketReceive;

	private transient boolean active; //

	// scaler values, loaded when a scaler packet is received.
	private transient final List<Integer> scalerValues = new ArrayList<Integer>();

	// counter values, loaded when a counter packet is received.
	private transient final List<Integer> counterValues = new ArrayList<Integer>();

	/**
	 * Creates the instance of this class for handling IP communications with
	 * the VME front end computer.
	 */
	public VMECommunication() {
		super();
		active = false;
		setName("Network Messenger");
	}

	/**
	 * Sets up networking. Two udp sockets are created: one for receiving and
	 * one for sending. A daemon is also set up for receiving.
	 * 
	 * @throws JamException
	 *             if something goes wrong
	 */
	public void setupAcquisition() throws JamException {
		final InetAddress addressLocal;

		synchronized (this) {
			final String LOCAL_IP = JamProperties
					.getPropString(JamProperties.HOST_IP);
			final int portSend = JamProperties
					.getPropInt(JamProperties.HOST_PORT_SEND);
			final int portRecv = JamProperties
					.getPropInt(JamProperties.HOST_PORT_RECV);
			final String VME_IP = JamProperties
					.getPropString(JamProperties.TARGET_IP);
			final int PORT_VME_SEND = JamProperties
					.getPropInt(JamProperties.TARGET_PORT);
			vmePort = PORT_VME_SEND;
			if (!active) {
				try {// ceate a ports to send and receive
					addressLocal = InetAddress.getByName(LOCAL_IP);
				} catch (UnknownHostException ue) {
					throw new JamException(getClass().getName()
							+ ": Unknown local host " + LOCAL_IP);
				}
				try {
					addressVME = InetAddress.getByName(VME_IP);
				} catch (UnknownHostException ue) {
					throw new JamException(getClass().getName()
							+ ": Unknown VME host " + VME_IP);
				}
				try {
					socketSend = new DatagramSocket(portSend, addressLocal);
				} catch (BindException be) {
					throw new JamException(
							getClass().getName()
									+ ": Problem binding send socket (another Jam online running)");
				} catch (SocketException se) {
					throw new JamException(getClass().getName()
							+ ": problem creating send socket");
				}
				try {
					socketReceive = new DatagramSocket(portRecv, addressLocal);
				} catch (BindException be) {
					throw new JamException(
							getClass().getName()
									+ ": Problem binding receive socket (another Jam online running)");
				} catch (SocketException se) {
					throw new JamException(getClass().getName()
							+ ": Problem creating receive socket");
				}
				// setup and start receiving deamon
				setDaemon(true);
				setPriority(jam.sort.ThreadPriorities.MESSAGING);
				start();
				active = true;
				final Preferences prefs = JamPrefs.PREFS;
				debug(prefs.getBoolean(JamPrefs.DEBUG, false));
				verbose(prefs.getBoolean(JamPrefs.VERBOSE, false));
			}
		}
	}

	/**
	 * Recieves distributed events. Can listen for broadcasted event.
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
	 * Tell the VME to read the scalers, and send back two packets: the packet
	 * with the scaler values, and a status packet (read OK or ERROR) with a
	 * message.
	 */
	public void readScalers() {
		final String RUN_SCALER = "list scaler";
		this.sendToVME(RUN_SCALER);
	}

	/**
	 * Tells the VME to clear the scalers and send a reply: OK or ERROR.
	 */
	public void clearScalers() {
		final String RUN_CLEAR = "list clear";
		sendToVME(RUN_CLEAR);
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
	 * Tells the VME to zero its counters and send a reply: OK or ERROR.
	 */
	public void zeroCounters() {
		final String COUNT_ZERO = "count zero";
		sendToVME(COUNT_ZERO);
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
	 * New version uploads CAMAC CNAF commands with udp pakets, and sets up the
	 * camac crate.
	 * 
	 * @param camacCommands
	 *            object containing CAMAC CNAF commands
	 * @throws JamException
	 *             if there's a problem
	 */
	public void setupCamac(final CamacCommands camacCommands)
			throws JamException {
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
	 * Send the interval in seconds between scaler blocks in the event stream.
	 * 
	 * @param seconds
	 *            the interval between scaler blocks
	 */
	public void sendScalerInterval(final int seconds) {
		final String message = seconds + "\n\0";
		sendToVME(INTERVAL, message);
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
	private void sendToVME(final int status, final String message) {
		final DatagramPacket packetMessage;
		/* byte arrays initialized with zeros by definition */
		final byte[] byteMessage = new byte[message.length() + 5];
		if (!validStatus(status)) {
			throw new IllegalArgumentException(getClass().getName()
					+ ".vmeSend() with invalid status: " + status);
		}
		byteMessage[3] = (byte) status;// first four bytes interpreted together
		// as this number
		System.arraycopy(message.getBytes(), 0, byteMessage, 4, message
				.length());
		byteMessage[byteMessage.length - 1] = STRING_NULL;
		packetMessage = new DatagramPacket(byteMessage, byteMessage.length,
				addressVME, vmePort);
		if (socketSend == null) {
			throw new IllegalStateException(
					"Attempted to send a message without a connection.");
		}
		try {// create and send packet
			socketSend.send(packetMessage);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE,
					"Jam encountered a network communication error attempting to send a packet: "
							+ e.getMessage(), e);
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
	 * @throws JamException
	 *             if there's a problem
	 */
	private void sendCNAFList(final String listName, final List<CNAF> cnafList)
			throws JamException {
		final int COMMAND_SIZE = 16;
		final int CNAF_SIZE = 9;
		byte[] byteName = new byte[COMMAND_SIZE];
		if (listName.length() > (COMMAND_SIZE - 1)) {
			throw new JamException(
					"Command string length too long [VMECommunication]");
		}
		final byte[] byteMessage = new byte[4 + COMMAND_SIZE + 4
				+ cnafList.size() * CNAF_SIZE + 1];
		/* set first int to 3, first three bytes are zero already */
		byteMessage[3] = CNAF;
		// put command string into packet
		byteName = listName.getBytes();
		for (int i = 0; i < listName.length(); i++) {
			byteMessage[i + 4] = byteName[i];
		}
		// put length of cnaf list in packet
		for (int i = 20; i <= 23; i++) {
			byteMessage[i] = 0;
		}
		byteMessage[23] = (byte) cnafList.size();
		// put list of cnaf commands into packet
		for (int i = 0; i < cnafList.size(); i++) {
			final int offset = 4 + COMMAND_SIZE + 4 + CNAF_SIZE * i;
			final CNAF cnaf = cnafList.get(i);
			byteMessage[offset + 0] = cnaf.getParamID();
			byteMessage[offset + 1] = cnaf.getCrate();
			byteMessage[offset + 2] = cnaf.getNumber();
			byteMessage[offset + 3] = cnaf.getAddress();
			byteMessage[offset + 4] = cnaf.getFunction();
			final int data = cnaf.getData();
			// data byte msb
			byteMessage[offset + 5] = (byte) ((data >>> 24) & 0xFF);
			// data byte 1
			byteMessage[offset + 6] = (byte) ((data >>> 16) & 0xFF);
			// data byte 2
			byteMessage[offset + 7] = (byte) ((data >>> 8) & 0xFF);
			// data byte lsb
			byteMessage[offset + 8] = (byte) ((data >>> 0) & 0xFF);
		}
		// add a null character
		byteMessage[byteMessage.length - 1] = STRING_NULL;
		sendPacket(byteMessage);// send it
	}

	/**
	 * Send out a packet of the given bytes.
	 * 
	 * @param byteMessage
	 *            the message to send
	 * @throws JamException
	 *             if there's a problem
	 */
	private void sendPacket(final byte[] byteMessage) throws JamException {
		try {// create and send packet
			final DatagramPacket packetMessage = new DatagramPacket(
					byteMessage, byteMessage.length, addressVME, vmePort);
			if (socketSend == null) {
				throw new JamException(getClass().getName()
						+ ": Send socket not setup.");
			}
			socketSend.send(packetMessage);
		} catch (IOException e) {
			throw new JamException(getClass().getName()
					+ ": Error while sending packet.", e);
		}
	}

	/**
	 * Thread method: is a deamon for receiving packets from the VME crate. The
	 * first int of the packet is the status word. It determines how the packet
	 * is to be handled.
	 */
	public void run() {
		final byte[] bufferIn = new byte[MAX_PACKET_SIZE];
		try {
			final DatagramPacket packetIn = new DatagramPacket(bufferIn,
					bufferIn.length);
			while (true) {// loop forever receiving packets
				socketReceive.receive(packetIn);
				final ByteBuffer byteBuffer = ByteBuffer.wrap(packetIn
						.getData());
				final int status = byteBuffer.getInt();// .readInt();
				if (status == OK_MESSAGE) {
					LOGGER.info(getClass().getName() + ": "
							+ unPackMessage(byteBuffer));
				} else if (status == SCALER) {
					unPackScalers(byteBuffer);
					Scaler.update(scalerValues);
				} else if (status == COUNTER) {
					unPackCounters(byteBuffer);
					BROADCASTER.broadcast(
							BroadcastEvent.Command.COUNTERS_UPDATE,
							counterValues);
				} else if (status == ERROR) {
					LOGGER.severe(getClass().getName() + ": "
							+ unPackMessage(byteBuffer));
				} else {
					LOGGER.severe(getClass().getName()
							+ ": packet with unknown message type received");
				}
			}// end of receive message forever loop
		} catch (IOException ioe) {
			LOGGER.log(Level.SEVERE,
					"Unable to read datagram status word [VMECommnunication]",
					ioe);
			LOGGER
					.warning("Network receive daemon stopped, need to restart Online [VMECommunication]");
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
			next = (char)buffer.get();
			rval.append(next);
		} while (next != '\0');
		final int len = rval.length()-1;
		if (len > MAX_MESSAGE_SIZE) {// exclude null
			final IllegalArgumentException exception = new IllegalArgumentException(
					"Message length, "+len+", greater than max allowed, "+MAX_MESSAGE_SIZE+".");
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
		synchronized (this) {
			final int numScaler = buffer.getInt();// number of scalers
			scalerValues.clear();
			for (int i = 0; i < numScaler; i++) {
				scalerValues.add(buffer.getInt());
			}
		}
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
		synchronized (this) {
			final int numCounter = buffer.getInt(); // number of
			// counters
			counterValues.clear();
			for (int i = 0; i < numCounter; i++) {
				counterValues.add(buffer.getInt());
			}
		}
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
	 * Checks whether the parameter is a valid value of a message status.
	 * 
	 * @param status
	 *            to be checked
	 * @return true if valid, false if not
	 */
	private boolean validStatus(final int status) {
		return (status == OK_MESSAGE || status == ERROR || status == SCALER
				|| status == CNAF || status == COUNTER || status == VME_ADDRESS || status == INTERVAL);
	}

	/**
	 * @see java.util.prefs.PreferenceChangeListener#preferenceChange(java.util.prefs.PreferenceChangeEvent)
	 */
	public void preferenceChange(final PreferenceChangeEvent pce) {
		final String key = pce.getKey();
		final String newValue = pce.getNewValue();
		final boolean state = Boolean.valueOf(newValue).booleanValue();
		if (key.equals(JamPrefs.DEBUG)) {
			debug(state);
		} else if (key.equals(JamPrefs.VERBOSE)) {
			verbose(state);
		}
	}
}
