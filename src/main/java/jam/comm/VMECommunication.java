package jam.comm;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jam.data.Scaler;
import jam.global.*;
import jam.sort.CamacCommands.CNAF;
import jam.util.StringUtilities;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Class to communicate with VME crate using UDP packets Two UDP sockets can be
 * setup with a daemon that receives packets and knows what to do with them The
 * first int of a packet indicates what type of packet it is.
 * @version 0.5 April 98
 * @author Ken Swartz and Dale Visser
 * @since JDK1.1
 */
@Singleton
final class VMECommunication extends GoodThread implements VmeSender {

    private final transient Broadcaster broadcaster;

    private static final Object LOCK = new Object();

    private static final String ANOTHER_JAM = " "
            + "(Is another copy of Jam running online?)";

    private static final String STOPPED_WARNING = "Network "
            + "receive daemon stopped, need to restart Online.";

    private static final String NETWORK_ERROR = "Jam"
            + " encountered a network communication error"
            + " attempting to send a packet.";

    private final transient StringUtilities stringUtilities;

    private transient boolean active;

    private transient InetAddress addressVME;

    private transient DatagramSocket socketSend, socketReceive;

    private transient int vmePort;

    private final transient PacketBuilder packetBuilder;

    /**
     * Creates the instance of this class for handling IP communications with
     * the VME front end computer.
     * @param broadcaster
     *            broadcasts counter updates
     * @param packetBuilder
     *            constructs message packets
     * @param stringUtilities
     *            converts strings to byte arrays with ASCII characters
     */
    @Inject
    protected VMECommunication(final Broadcaster broadcaster,
            final PacketBuilder packetBuilder,
            final StringUtilities stringUtilities) {
        super();
        this.broadcaster = broadcaster;
        this.packetBuilder = packetBuilder;
        this.stringUtilities = stringUtilities;
        new CounterVMECommunicator(this, broadcaster);
        this.setName("Front End Communication");
        this.setDaemon(true);
        this.setPriority(jam.sort.ThreadPriorities.MESSAGING);
        this.setState(State.SUSPEND);
        this.start();
    }

    /**
     * @param localInetAddress
     *            address of the machine this instance of Jam is running on
     * @param portSend
     *            port for sending
     * @param portRecv
     *            port for receiving
     * @param vmeInetAddress
     *            address of VME front end computer
     * @throws CommunicationsException
     *             if there is a problem communicating with the front end
     */
    protected void bindSocketsAndSetActive(final String localInetAddress,
            final int portSend, final int portRecv, final String vmeInetAddress)
            throws CommunicationsException {
        this.setVMEport();
        if (!active) {
            InetAddress addressLocal;
            try { // create a ports to send and receive
                addressLocal = InetAddress.getByName(localInetAddress);
            } catch (UnknownHostException ue) {
                throw new CommunicationsException("Unknown local host: "
                        + localInetAddress, ue);
            }
            try {
                addressVME = InetAddress.getByName(vmeInetAddress);
            } catch (UnknownHostException ue) {
                throw new CommunicationsException("Unknown VME host: "
                        + vmeInetAddress, ue);
            }
            try {
                socketSend = new DatagramSocket(portSend, addressLocal);
            } catch (BindException be) {
                throw new CommunicationsException(
                        "Problem binding send socket." + ANOTHER_JAM, be);
            } catch (SocketException se) {
                throw new CommunicationsException(
                        "Problem creating send socket.", se);
            }
            try {
                socketReceive = new DatagramSocket(portRecv, addressLocal);
            } catch (BindException be) {
                throw new CommunicationsException(
                        "Problem binding receive socket." + ANOTHER_JAM, be);
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
            final List<Integer> unpackedValues = new ArrayList<>(8);
            while (checkState()) { // loop forever receiving packets
                try {
                    synchronized (LOCK) {
                        socketReceive.receive(packetIn);
                    }
                    final ByteBuffer byteBuffer = ByteBuffer.wrap(packetIn
                            .getData());
                    final int status = byteBuffer.getInt();
                    unpackBuffer(unpackedValues, byteBuffer, status);
                } catch (SocketException se) {
                    LOGGER.info("Receive socket was closed.");
                }
            } // end of receive message forever loop
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "Unable to read datagram status word.",
                    ioe);
            LOGGER.warning(STOPPED_WARNING);
        }
    }

    private void unpackBuffer(final List<Integer> unpackedValues,
            final ByteBuffer byteBuffer, final int status) {
        if (status == PacketTypes.OK_MESSAGE.intValue()) {
            LOGGER.info(getClass().getName() + ": "
                    + unPackMessage(byteBuffer));
        } else if (status == PacketTypes.SCALER.intValue()) {
            unPackCounters(byteBuffer, unpackedValues);
            Scaler.update(unpackedValues);
        } else if (status == PacketTypes.COUNTER.intValue()) {
            unPackCounters(byteBuffer, unpackedValues);
            broadcaster.broadcast(BroadcastEvent.Command.COUNTERS_UPDATE,
                    unpackedValues);
        } else if (status == PacketTypes.ERROR.intValue()) {
            LOGGER.severe(getClass().getName() + ": "
                    + unPackMessage(byteBuffer));
        } else {
            LOGGER.severe(getClass().getName()
                    + ": packet with unknown message type received");
        }
    }

    /**
     * Method to send a CNAF list to the VME crate packet structure 16 bytes
     * name 4 bytes number of CNAF's 4 bytes for each CNAF.
     * @param listName
     *            name of CNAF list to send
     * @param cnafList
     *            the CAMAC commands
     * @throws IOException
     *             if there's a problem
     */
    protected void sendCNAFList(final String listName,
            final List<CNAF> cnafList) throws IOException {
        GoodThread.LOGGER.entering(VMECommunication.class.getName(),
                "sendCNAFList", listName);
        final int commandSize = 16;
        final int cnafSize = 9;
        if (listName.length() > (commandSize - 1)) {
            throw new IllegalArgumentException("Command list length too long.");
        }
        final int intSize = 4;
        final byte[] byteMessage = new byte[intSize + commandSize + intSize
                + cnafList.size() * cnafSize + 1];
        final ByteBuffer byteBuff = ByteBuffer.wrap(byteMessage);
        byteBuff.putInt(PacketTypes.CNAF.intValue());
        // put command string into packet
        final byte[] asciiListName = stringUtilities.getASCIIarray(listName);
        byteBuff.put(asciiListName);
        for (int i = commandSize; i > asciiListName.length; i--) {
            byteBuff.put(Constants.STRING_NULL);
        }
        // put length of CNAF list in packet
        byteBuff.putInt(cnafList.size());
        // put list of CNAF commands into packet
        for (final CNAF cnaf : cnafList) {
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
        final Object[] params = {status, message };
        LOGGER.entering(VMECommunication.class.getName(), "sendToVME", params);
        if (this.socketSend == null) {
            throw new IllegalStateException(
                    "Attempted to send a message without a connection.");
        }

        final DatagramPacket packetMessage = packetBuilder.message(status,
                message, addressVME, vmePort);
        try { // create and send packet
            socketSend.send(packetMessage);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, NETWORK_ERROR, e);
        }
    }

    public void sendMessage(final String message) {
        sendToVME(PacketTypes.OK_MESSAGE, message);
    }

    private void setVMEport() {
        synchronized (LOCK) {
            vmePort = JamProperties.getPropInt(PropertyKeys.TARGET_PORT);
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
        if (len > Constants.MAX_MESSAGE_SIZE) { // exclude null
            final IllegalArgumentException error = new IllegalArgumentException(
                    "Message length, " + len + ", greater than max allowed, "
                            + Constants.MAX_MESSAGE_SIZE + ".");
            LOGGER.throwing("VMECommunication", "unPackMessage", error);
            throw error;
        }
        return rval.substring(0, len);
    }
}
