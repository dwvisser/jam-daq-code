package test.sort.mockfrontend;

import injection.GuiceInjector;
import jam.sort.RingBufferFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class for sending messages back to Jam.
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version Feb 16, 2004
 */
public class MessageSender {

    /**
     * Value sent for scaler.
     */
    public static final int SCALER_VALUE = 137;
    private transient final SocketAddress jamMessageReceive;
    private transient final Console console;
    private transient final DatagramSocket localSocket;

    /**
     * Creates a new message sender.
     * @param console
     *            for outputting messages to the screen
     */
    MessageSender(final Counter events, final Counter buffers,
            final Console console, final DatagramSocket localSocket,
            final SocketAddress jamData, final SocketAddress jamMessageReceive) {
        super();
        this.localSocket = localSocket;
        this.console = console;
        this.jamMessageReceive = jamMessageReceive;
        this.eventGenerator = new EventGenerator(events, buffers, jamData);
    }

    private transient final EventGenerator eventGenerator;

    Future<?> startSendingEventData() {// NOPMD
        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(
                1);
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 200L,
                TimeUnit.MILLISECONDS, queue);
        return executor.submit(eventGenerator);
    }

    class EventGenerator implements Runnable {
        private transient final byte[] buffer = GuiceInjector
                .getObjectInstance(RingBufferFactory.class).freshBuffer();
        private transient final byte[] parameter0 = {(byte) 0x80, 0x00 };
        private transient final byte[] parameter1 = {(byte) 0x80, 0x01 };
        private transient final byte[] eventEnd = {(byte) 0xff, (byte) 0xff };
        private transient final byte[] bufferPad = {(byte) 0xff, (byte) 0xf0 };
        private transient final Random random = new Random();
        private transient final Counter eventCounter, bufferCounter;
        private transient final SocketAddress jamDataSocketAddress;// NOPMD

        EventGenerator(final Counter eventCounter,
                final Counter bufferCounter, final SocketAddress address) {
            this.eventCounter = eventCounter;
            this.bufferCounter = bufferCounter;
            this.jamDataSocketAddress = address;
        }

        private void fillBuffer() {
            final int eventLength = ((parameter0.length + 2) * 2)
                    + eventEnd.length;
            final int count = buffer.length / eventLength;
            int currentIndex = 0;
            for (int i = 0; i < count; i++) {
                currentIndex = addParameter(currentIndex, parameter0);
                currentIndex = addParameter(currentIndex, parameter1);
                System.arraycopy(eventEnd, 0, buffer, currentIndex,
                        eventEnd.length);
                currentIndex += eventEnd.length;
                eventCounter.increment();
            }
            while (currentIndex < buffer.length) {
                System.arraycopy(bufferPad, 0, buffer, currentIndex,
                        bufferPad.length);
                currentIndex += bufferPad.length;
            }
        }

        private int addParameter(final int currentIndex, final byte[] parameter) {
            final int histLength = 128;
            int result = currentIndex;
            System.arraycopy(parameter, 0, buffer, result, parameter.length);
            result += parameter.length;
            buffer[result] = 0x00;
            result++;
            buffer[result] = (byte) random.nextInt(histLength);
            result++;
            return result;
        }

        public void run() {
            final DatagramPacket packetMessage = constructPacket(
                    this.jamDataSocketAddress, buffer);
            boolean keepRunning = true;
            while (keepRunning) {
                fillBuffer(); // change contents of packetMessage's buffer
                try {// create and send packet
                    sendPacket(packetMessage);
                    bufferCounter.increment();
                    Thread.sleep(100);
                } catch (final InterruptedException ie) {
                    console.messageOutln("Event generator interrupted.");
                    keepRunning = false;
                } catch (final Exception e) {
                    console.errorOutln(e.getMessage());
                    keepRunning = false;
                }
            }
        }
    }

    private DatagramPacket constructPacket(final SocketAddress socketAddress,
            final byte[] msgBuffer) {
        return new DatagramPacket(msgBuffer, msgBuffer.length,
                    socketAddress);
    }

    /**
     * Called by MessageReceiver when it gets "list scaler".
     */
    public void sendScalerValues() {
        /*
         * 3 4-byte integers: 2, which means scaler packet; 1, which is the
         * number of scaler values; SCALER_VALUE (137), which is the 1 scaler
         * value.
         */
        final byte[] message = {0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00,
                0x01, 0x00, 0x00, 0x00, (byte) SCALER_VALUE };
        final DatagramPacket packet = this.constructPacket(
                this.jamMessageReceive, message);
        sendPacket(packet);
    }

    private void sendPacket(final DatagramPacket packet) {
        try {
            this.localSocket.send(packet);
        } catch (IOException e) {
            console.errorOutln(getClass().getName()
                    + ".send(): "
                    + "Jam encountered a network communication error attempting to send a packet.");
        }
    }
}
