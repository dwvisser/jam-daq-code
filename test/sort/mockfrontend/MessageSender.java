package test.sort.mockfrontend;

import jam.global.JamException;
import jam.sort.RingBuffer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class for sending messages back to Jam.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version Feb 16, 2004
 */
public class MessageSender {

	private transient final DatagramSocket socket;
	private transient final Console console;

	/**
	 * Creates a new message sender.
	 * 
	 * @param console
	 *            for outputting messages to the screen
	 * @param port
	 *            that we send on
	 * @param addr
	 *            that we send from
	 * @param jamport
	 *            that we send to
	 * @param jamaddr
	 *            that we send to
	 * @throws SocketException
	 *             if we can't bind to the socket
	 * @throws UnknownHostException
	 *             if an address is invalid
	 */
	MessageSender(final Counter events, final Counter buffers,
			final Console console, final DatagramSocket localSocket,
			final SocketAddress jamData) throws SocketException,
			UnknownHostException {
		super();
		this.console = console;
		this.socket = localSocket;
		this.eventGenerator = new EventGenerator(events, buffers, console,
				localSocket, jamData);
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
	 * @throws JamException
	 *             if there's a problem
	 */
	private void send(final int status, final byte[] message,
			final boolean terminate) throws IOException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream(
				message.length + 5);
		final DataOutputStream dos = new DataOutputStream(output);
		dos.writeInt(status);// 4-byte int
		dos.write(message);
		if (terminate) {
			dos.write(0);// 8-bit null termination
		}
		final byte[] byteMessage = output.toByteArray();
		dos.close();
		try {// create and send packet
			final DatagramPacket packetMessage = new DatagramPacket(
					byteMessage, byteMessage.length, socket
							.getRemoteSocketAddress());
			socket.send(packetMessage);
		} catch (final IOException e) {
			console
					.errorOutln(getClass().getName()
							+ ".send(): "
							+ "Jam encountered a network communication error attempting to send a packet.");
		}
	}

	public void sendMessage(final String message) throws IOException {
		// send(FrontEndCommunication.OK,message.getBytes(US_ASCII),true);
	}

	public void sendError(final String message) throws IOException {
		// send(FrontEndCommunication.ERROR,message.getBytes(US_ASCII),true);
	}

	public void sendCounters(final int[] values) throws IOException {
		// sendIntegers(FrontEndCommunication.COUNTER,values);
	}

	public void sendScalers(final int[] values) throws IOException {
		// sendIntegers(FrontEndCommunication.SCALER,values);
	}

	public void sendIntegers(final int status, final int[] values)
			throws IOException {
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream(
				4 * (values.length + 1));
		final DataOutputStream dos = new DataOutputStream(bytes);
		for (int i = 0; i < values.length; i++) {
			dos.writeInt(values[i]);
		}
		dos.close();
		send(status, bytes.toByteArray(), false);
	}

	private final EventGenerator eventGenerator;

	public Future<?> startSendingEventData() {
		final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(
				1);
		final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 200L,
				TimeUnit.MILLISECONDS, queue);
		return executor.submit(eventGenerator);
	}

	static class EventGenerator implements Runnable {
		private static final byte[] buffer = RingBuffer.freshBuffer();
		private static final byte[] parameter0 = { (byte) 0x80, 0x00 };
		private static final byte[] parameter1 = { (byte) 0x80, 0x01 };
		private static final byte[] eventEnd = { (byte) 0xff, (byte) 0xff };
		private static final byte[] bufferPad = { (byte) 0xff, (byte) 0xf0 };
		private transient final Random random = new Random();
		private transient final DatagramSocket socket;
		private transient final Console console;
		private transient final Counter eventCounter, bufferCounter;
		private transient final SocketAddress jamData;

		EventGenerator(final Counter eventCounter, final Counter bufferCounter,
				final Console console, final DatagramSocket localSocket,
				final SocketAddress address) throws SocketException {
			this.eventCounter = eventCounter;
			this.bufferCounter = bufferCounter;
			this.console = console;
			this.socket = localSocket;
			this.jamData = address;
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
			boolean keepRunning = true;
			while (keepRunning) {
				fillBuffer();
				try {// create and send packet
					final DatagramPacket packetMessage = new DatagramPacket(
							buffer, buffer.length, this.jamData);
					socket.send(packetMessage);
					bufferCounter.increment();
					Thread.sleep(100);
				} catch (final IOException e) {
					console
							.errorOutln(getClass().getName()
									+ ".send(): "
									+ "Jam encountered a network communication error attempting to send a packet.");
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

}
