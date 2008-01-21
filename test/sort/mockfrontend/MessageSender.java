package test.sort.mockfrontend;

import jam.sort.RingBuffer;

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
			final SocketAddress jamData) {
		super();
		this.eventGenerator = new EventGenerator(events, buffers, console,
				localSocket, jamData);
	}

	private transient final EventGenerator eventGenerator;

	Future<?> startSendingEventData() {// NOPMD
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
		private transient final SocketAddress jamDataSocketAddress;

		EventGenerator(final Counter eventCounter, final Counter bufferCounter,
				final Console console, final DatagramSocket localSocket,
				final SocketAddress address) {
			this.eventCounter = eventCounter;
			this.bufferCounter = bufferCounter;
			this.console = console;
			this.socket = localSocket;
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
			DatagramPacket packetMessage = null;
			try {
				/* Associates this.buffer with packetMessage. */
				packetMessage = new DatagramPacket(buffer, buffer.length,
						this.jamDataSocketAddress);
			} catch (SocketException se) {
				console.errorOutln("Problem setting up packet: "
						+ se.getMessage());
			}
			boolean keepRunning = packetMessage != null;
			while (keepRunning) {
				fillBuffer(); // change contents of packetMessage's buffer
				try {// create and send packet
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
