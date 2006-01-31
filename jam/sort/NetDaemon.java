package jam.sort;

import jam.global.GoodThread;
import jam.global.JamStatus;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JOptionPane;

/**
 * <code>NetDeamon</code> receives packets from the network and sends the data
 * data into one or two pipes. One pipe to a <code>SortDeamon</code>, and the
 * other is a <code>TapeDaemon</code> or <code>DiskDaemon</code> if one is
 * activated.
 * 
 * @author Ken Swartz
 * @author Dale Visser
 * @version 0.5
 * @since JDK 1.1
 */
public final class NetDaemon extends GoodThread {

	private transient final DatagramSocket dataSocket;

	/**
	 * ring buffers for passing events to sorting
	 */
	private transient final RingBuffer sortingRing;

	/**
	 * ring buffers for passing events to storage
	 */
	private transient final RingBuffer storageRing;

	/**
	 * set if we want data to be put is writing pipe writting out data
	 */
	private transient boolean writerOn = false;

	/**
	 * number of packets counter
	 */
	private int packetCount = 0; // number of packets received

	private transient int notSortCount = 0; // number of packets not sorted

	private transient int notStorCount = 0;// number of packets not stored

	/**
	 * Constructor passed both storage and sorting pipes.
	 * 
	 * @param sortRing
	 *            buffer the events are sent to for sorting
	 * @param storeRing
	 *            buffer the events are sent to for storage
	 * @param msgHandle
	 *            where messages can be sent to the console
	 * @param host
	 *            front end IP address
	 * @param port
	 *            port to listen for front end on
	 * @exception SortException
	 *                thrown if there's a problem setting up the pipes
	 */
	public NetDaemon(RingBuffer sortRing, RingBuffer storeRing, String host,
			int port) throws SortException {
		super();
		sortingRing = sortRing;
		storageRing = storeRing;
		try {// ceate a port listener
			final InetAddress dataAddress = InetAddress.getByName(host);
			dataSocket = new DatagramSocket(port, dataAddress);
		} catch (UnknownHostException e) {
			throw new SortException(getClass().getName() + ": The host, "
					+ host + ", is unknown.");
		} catch (BindException be) {
			throw new SortException(
					getClass().getName()
							+ ": Could not bind to data socket. Are other copies of Jam running?");
		} catch (IOException e) {
			throw new SortException(getClass().getName()
					+ ": Could not create data socket.");
		}
		setPriority(ThreadPriorities.NET);
		setDaemon(true);// the user doesn't interact with this thread
		setName("UDP Data Receiver");
	}

	/**
	 * Calls <code>receiveLoop</code>.
	 * 
	 * @see #receiveLoop()
	 */
	public void run() {
		final StringBuffer message = new StringBuffer(getClass().getName())
				.append("--communication with acquisition halted, because of ");
		try {
			receiveLoop();
		} catch (Exception e) {
			message.append(e.getClass().getName()).append(':').append(
					e.getMessage());
			LOGGER.warning(message.toString());
		}
	}

	private boolean emptyBefore = false;

	/**
	 * Called when we've definitely lost a buffer we were meant to receive due
	 * to a full ringbuffer.
	 * 
	 * @param state
	 *            <code>true</code> if we lost a buffer, <code>false</code>
	 *            to reset
	 */
	public void setEmptyBefore(final boolean state) {
		synchronized (this) {
			if (!emptyBefore && state) {
				final StringBuffer mesg = new StringBuffer();
				mesg
						.append("The sorting process lost a buffer. Click 'OK' to\n");
				mesg
						.append("end the current run, 'Cancel' to have Jam attempt to\n");
				mesg
						.append("continue automatically sampling events in order to keep\n");
				mesg
						.append("up with the acquisition with no further warnings.\n");
				final boolean confirmed = JOptionPane.showConfirmDialog(
						JamStatus.getSingletonInstance().getFrame(), mesg
								.toString(), "Buffer lost. End run?",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION;
				if (confirmed) {
					new JButton(endAction).doClick();
				}
			}
			emptyBefore = state;
		}
	}

	boolean isEmptyBefore() {
		synchronized (this) {
			return emptyBefore;
		}
	}

	/**
	 * Runs in an infinite loop receiving data from the local net and stuffing
	 * it into a couple of pipes.
	 * 
	 * @exception IOException
	 *                if there's a problem storing the data
	 * @exception SortException
	 *                if there's a problem sorting the data
	 */
	public void receiveLoop() throws IOException, SortException {
		if (dataSocket == null) {
			throw new SortException(
					"Could not start netDeamon, socket null {NetDaemon]");
		}
		final byte[] bufferOut = RingBuffer.freshBuffer();
		final DatagramPacket dataIn = new DatagramPacket(bufferOut,
				bufferOut.length);
		while (checkState()) {// loop as long as state is RUN
			/* wait for packet */
			dataSocket.receive(dataIn);
			if (checkState()) {
				dataIn.getData();// data goes to bufferOut
				packetCount++;
				/* Put buffer into to sorting ring with sample fraction */
				try {
					sortingRing.putBuffer(bufferOut);
				} catch (RingFullException rfe) {
					notSortCount++;
					setEmptyBefore(true);
				}
				/* put buffer into to storage ring */
				if (writerOn) {
					try {
						storageRing.putBuffer(bufferOut);
					} catch (RingFullException rfe) {
						notStorCount++;
						LOGGER.severe("Storage Buffer "
								+ rfe.getMessage());
					}
				}
			} else {// received a packet while thread state not RUN
				LOGGER
						.warning("Warning: received buffer while NetDaemon thread"
								+ " state was not RUN: state=" + this);
			}
		}// end RUN loop
	}

	/**
	 * Sets whether to write out events to the storage pipe.
	 * 
	 * @param writerOn
	 *            <code>true</code> if write events, <code>false</code> if
	 *            not
	 */
	public void setWriter(final boolean writerOn) {
		if (storageRing.isNull()) {
			LOGGER.warning(getClass().getName()
					+ ": Can't write out events. There is no ring buffer.");
		} else {
			this.writerOn = writerOn;
		}
	}

	/**
	 * Closes the network connection.
	 */
	public void closeNet() {
		if (dataSocket != null) {
			dataSocket.close();
		}
	}

	/**
	 * Returns the total packets sent.
	 * 
	 * @return the total number of packets sent
	 */
	public int getPacketCount() {
		return packetCount;
	}

	/**
	 * Sets the packet count.
	 * 
	 * @param count
	 *            the total number of packets sent
	 */
	public void setPacketCount(final int count) {
		packetCount = count;
	}

	/**
	 * @return number of buffers received and stored to disk
	 */
	public int getStoredBuffers() {
		return packetCount - notStorCount;
	}

	/**
	 * @return number of buffers recieved and sorted
	 */
	public int getSortedBuffers() {
		return packetCount - notSortCount;
	}

	/**
	 * Reset all buffer counts.
	 */
	public void resetCounters() {
		synchronized (sortingRing) {
			packetCount = 0;
			notStorCount = 0;
			notSortCount = 0;
		}
	}

	private transient Action endAction;

	/**
	 * Called by RunControl to give NetDaemon access to the "end run" action.
	 * 
	 * @param action
	 *            the action which ends the current run
	 */
	public void setEndRunAction(final Action action) {
		endAction = action;
	}
}