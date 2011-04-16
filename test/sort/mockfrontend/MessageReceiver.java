package test.sort.mockfrontend;

import injection.GuiceInjector;
import jam.comm.PacketTypes;
import jam.global.GoodThread;
import jam.sort.RingBuffer;
import jam.sort.RingBufferFactory;

import java.awt.Frame;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;

import javax.swing.JOptionPane;

/**
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version February 16, 2004
 */
public class MessageReceiver extends GoodThread {
    private transient final DatagramSocket socket;
    private transient final Frame frame;
    private transient final MessageSender sender;
    private transient final Console console;

    private transient boolean receivedLS = false;

    /**
     * Creates a new message receiver.
     * @param frame
     *            the GUI frame of this application
     * @param port
     *            that we receive on
     * @param addr
     *            that we receive at
     * @param sender
     *            used to send messages and data
     * @throws UnknownHostException
     *             if the host is invalid
     */
    MessageReceiver(final Frame frame, final Console console,
            final DatagramSocket localSocket, final MessageSender sender) {
        super();
        this.console = console;
        this.frame = frame;
        this.sender = sender;
        this.socket = localSocket;
    }

    /**
     * For OK_MESSAGE packets, always prints, and handles start and stop,
     * generating YaleInputStream event data. For VME_ADDRESS packets, prints
     * out nicely formatted.
     */
    @Override
    public void run() {
        final DatagramPacket packet = new DatagramPacket(GuiceInjector
                .getObjectInstance(RingBufferFactory.class).freshBuffer(),
                RingBuffer.BUFFER_SIZE);
        Future<?> eventGenerator = null;
        while (checkState()) {
            try {
                socket.receive(packet);
                final ByteBuffer byteBuffer = ByteBuffer
                        .wrap(packet.getData());
                final int status = byteBuffer.getInt();
                if (status == PacketTypes.OK_MESSAGE.intValue()) {
                    eventGenerator = handleNormalMessage(eventGenerator,
                            byteBuffer);
                } else if (status == PacketTypes.VME_ADDRESS.intValue()) {
                    this.console.messageOutln("VME Conguration Info:\n"
                            + this.unPackMessage(byteBuffer));
                } else if (status == PacketTypes.CNAF.intValue()) {
                    this.console.messageOutln("CNAF packet received.");
                }
            } catch (final IOException e) {
                JOptionPane.showMessageDialog(frame, e.getMessage(),
                        getClass().getName(), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Future<?> handleNormalMessage(final Future<?> eventGenerator,
            final ByteBuffer byteBuffer) {
        final String message = this.unPackMessage(byteBuffer);
        MessageReceiver.this.console.messageOutln("Recieved: " + message);
        Future<?> result = null;
        if ("start".equalsIgnoreCase(message)) {
            result = this.sender.startSendingEventData();
        } else if ((null != eventGenerator)
                && "stop".equalsIgnoreCase(message)) {
            stopEventGenerator(eventGenerator);
        } else if ("list scaler".equalsIgnoreCase(message)) {
            this.receivedLS = true;
            this.sender.sendScalerValues();
            result = eventGenerator;
        }
        return result;
    }

    private void stopEventGenerator(final Future<?> eventGenerator) {
        if (!eventGenerator.cancel(true)) {
            final String error = "Couldn't stop sending events.";
            LOGGER.severe(error);
            this.console.errorOutln(error);
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
        return rval.substring(0, len);
    }

    /**
     * @return whether a "list scaler" message has been received
     */
    public boolean hasReceivedListScaler() {
        return this.receivedLS;
    }

}
