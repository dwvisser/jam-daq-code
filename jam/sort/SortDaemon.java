package jam.sort;

import jam.SortControl;
import jam.global.Beginner;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.Ender;
import jam.global.GoodThread;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.global.Sorter;
import jam.sort.stream.AbstractEventInputStream;
import jam.sort.stream.AbstractEventInputStream.EventInputStatus;

import java.util.Arrays;

/**
 * The daemon (background thread) which sorts data. It takes an
 * <code>EventInputStream</code>, and a <code>Sorter</code> class. It reads
 * events from the <code>EventInputStream</code> and gives these to the
 * <code>Sorter</code> method <code>sort(int [])</code>. Last modified 18
 * December 1999 to use RingInputStream KBS.
 * 
 * @author Ken Swartz
 * @author Dale Visser
 * @version 1.1
 * @since JDK 1.1
 */
public class SortDaemon extends GoodThread {

	/**
	 * Number of events to occur before updating counters.
	 */
	private final static int COUNT_UPDATE = 1000;

	/**
	 * The size of buffers to read in.
	 */
	final static int BUFFER_SIZE = 8 * 1024;

	private transient final Controller controller;

	private transient final MessageHandler msgHandler;

	private transient Sorter sorter;

	private transient AbstractEventInputStream eventInputStream;

	private static final Broadcaster BROADCASTER = Broadcaster
			.getSingletonInstance();

	/**
	 * Used for online only, holds data buffers from network.
	 */
	private transient RingBuffer ringBuffer;

	/* event information */
	private int eventSize;

	private int eventCount;

	private transient int eventSortedCount;

	private int bufferCount;

	/**
	 * Creates a new <code>SortDaemon</code> process.
	 * 
	 * @param con
	 *            the sort control process
	 * @param messageHandler
	 *            the console for writing out messages to the user
	 */
	public SortDaemon(Controller con, MessageHandler messageHandler) {
		super();
		controller = con;
		msgHandler = messageHandler;
		setName("Event Sorter");
	}

	/**
	 * setup the sort deamon tell it the mode and stream
	 * 
	 * @param eventInputStream
	 *            the source of event data
	 * @param eventSize
	 *            number of parameters per event
	 */
	public void setup(final AbstractEventInputStream eventInputStream,
			final int eventSize) {
		this.eventInputStream = eventInputStream;
		setEventSize(eventSize);
		/* Set the event size for the stream. */
		eventInputStream.setEventSize(eventSize);
		setEventCount(0);
		setPriority(ThreadPriorities.SORT);
		setDaemon(true);
	}

	/**
	 * Load the sorting class.
	 * 
	 * @param newSorter
	 *            an object capable of sorting event data
	 */
	public void setSorter(final Sorter newSorter) {
		sorter = newSorter;
	}

	/**
	 * Sets the ring buffer to pull event data from.
	 * 
	 * @param ringBuffer
	 *            the source of event data
	 */
	public void setRingBuffer(final RingBuffer ringBuffer) {
		this.ringBuffer = ringBuffer;
	}

	/**
	 * Sets the sort sample interval This is the frequeny of buffers sent to the
	 * sort routine. For example if this is set to 2 only every second buffer is
	 * sent to the sort routine.
	 * 
	 * @param sample
	 *            the sample interval
	 */
	public void setSortInterval(final int sample) {
		synchronized (this) {
			sortInterval = sample;
		}
	}

	private boolean callSort = true;

	private void setCallSort(final boolean state) {
		synchronized (this) {
			callSort = state;
		}
	}

	private boolean getCallSort() {
		synchronized (this) {
			return callSort;
		}
	}

	/**
	 * Sets whether to write selected events to disk.
	 * 
	 * @param state
	 *            whether writing of selected events to disk is enabled
	 */
	public void setWriteEnabled(final boolean state) {
		sorter.setWriteEnabled(state);
	}

	/**
	 * Sets the event size.
	 * 
	 * @param size
	 *            the number of parameters per event
	 */
	public void setEventSize(final int size) {
		eventSize = size;
	}

	/**
	 * Returns the event size.
	 * 
	 * @return the number of parameters per event
	 */
	public int getEventSize() {
		return eventSize;
	}

	/**
	 * Reads events from the event stream.
	 */
	public void run() {
		try {
			if (JamStatus.getSingletonInstance().isOnline()) {// which type of
				// sort to do
				sortOnline();
			} else {
				sortOffline();
			}
		} catch (Exception e) {
			msgHandler.errorOutln("Sorter stopped Exception " + e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * Invokes begin() in the user's sort routine if it implements the
	 * <code>Beginner</code> interface.
	 * 
	 * @see jam.global.Beginner
	 */
	public void userBegin() {
		synchronized (this) {
			if (sorter instanceof Beginner) {
				((Beginner) sorter).begin();
			}
		}
	}

	/**
	 * Invokes end() in the user's sort routine if it implements the
	 * <code>Ender</code> interface.
	 * 
	 * @see jam.global.Ender
	 */
	public void userEnd() {
		synchronized (this) {
			if (sorter instanceof Ender) {
				((Ender) sorter).end();
			}
		}
	}

	/**
	 * Performs the online sorting until an end-of-run state is reached in the
	 * event stream.
	 * 
	 * @exception Exception
	 *                thrown if an unrecoverable error occurs during sorting
	 */
	public void sortOnline() throws Exception {
		final RingInputStream ringInputStream = new RingInputStream();
		final int[] eventData = new int[eventSize];
		final byte[] buffer = RingBuffer.freshBuffer();
		while (true) { // loop while acquisition on
			/* Get a new buffer and make an input stream out of it. */
			if (ringBuffer.isCloseToFull()) {
				increaseSortInterval();
				setCallSort(false);
			} else {
				setCallSort(true);
				if (ringBuffer.isEmpty()) {
					decreaseSortInterval();
				}
			}
			ringBuffer.getBuffer(buffer);
			ringInputStream.setBuffer(buffer);
			eventInputStream.setInputStream(ringInputStream);
			/* Zero event array. */
			Arrays.fill(eventData, 0);
			EventInputStatus status;
			while ((((status = eventInputStream.readEvent(eventData)) == EventInputStatus.EVENT)
					|| (status == EventInputStatus.SCALER_VALUE) || (status == EventInputStatus.IGNORE))) {
				if (status == EventInputStatus.EVENT) {
					/* Sort only the sortInterval'th events. */
					if (getCallSort()
							&& getEventCount() % getSortInterval() == 0) {
						sorter.sort(eventData);
						incrementSortedCount();
					}
					incrementEventCount();
					/* Zero event array and get ready for next event. */
					Arrays.fill(eventData, 0);
				} // else SCALER_VALUE, assume sort stream took care and move
				// on
			}
			/* We have reached the end of a buffer. */
			if (status == EventInputStatus.END_BUFFER) {
				incrementBufferCount();
				yield();
			} else if (status == EventInputStatus.END_RUN) {
				incrementBufferCount();
				yield();
			} else if (status == EventInputStatus.UNKNOWN_WORD) {
				msgHandler.warningOutln("Unknown word in event stream.");
			} else if (status == EventInputStatus.END_FILE) {
				msgHandler
						.warningOutln("Tried to read past end of event input stream.");
			} else {// we have unknown status
				/* unrecoverable error should not be here */
				throw new SortException(
						"Sorter stopped due to unknown status: " + status);
			}
			yield();
		}// end infinite loop
	}

	private transient boolean osc = false;

	private transient final Object offlineSortLock = new Object();

	/**
	 * Call to gracefully interrupt and end the current offline sort. This is
	 * used as one of the conditions to read in the next buffer
	 */
	public void cancelOfflineSorting() {
		synchronized (offlineSortLock) {
			osc = true;
		}
	}

	private void resumeOfflineSorting() {
		synchronized (offlineSortLock) {
			osc = false;
		}
	}

	private boolean offlineSortingCanceled() {
		synchronized (offlineSortLock) {
			return osc;
		}
	}

	/**
	 * Performs the offline sorting until an end-of-run state is reached in the
	 * event stream.
	 * 
	 * @exception Exception
	 *                thrown if an unrecoverable error occurs during sorting
	 */
	public void sortOffline() throws Exception {
		final SortControl sortControl = (SortControl) controller;
		final int[] eventData = new int[eventSize];
		EventInputStatus status = EventInputStatus.IGNORE;
		boolean atBuffer = false; // are we at a buffer word
		/*
		 * Next statement causes checkState() to immediately suspend this
		 * thread.
		 */
		sortControl.atSortStart();
		while (checkState()) {// checkstate loop
			/* suspends this thread when we're done sorting all files */
			sortControl.atSortStart();
			resumeOfflineSorting();// after we come out of suspend
			/* Loop for each new sort file. */
			while (!offlineSortingCanceled() && sortControl.openNextFile()) {
				boolean endSort = false;
				while (!offlineSortingCanceled() && !endSort) {// buffer loop
					/* Zero the event container. */
					Arrays.fill(eventData, 0);
					/* Loop to read & sort one event at a time. */
					while (!offlineSortingCanceled()
							&& (((status = eventInputStream
									.readEvent(eventData)) == EventInputStatus.EVENT)
									|| (status == EventInputStatus.SCALER_VALUE) || (status == EventInputStatus.IGNORE))) {
						if (offlineSortingCanceled()) {
							status = EventInputStatus.END_RUN;
						} else {
							if (status == EventInputStatus.EVENT) {
								sorter.sort(eventData);
								incrementEventCount();
								incrementSortedCount();
								/*
								 * Zero event array and get ready for next
								 * event.
								 */
								Arrays.fill(eventData, 0);
								atBuffer = false;
								if (getEventCount() % COUNT_UPDATE == 0) {
									updateCounters();
									yield();
								}
							}
						}
						/*
						 * else SCALER_VALUE, assume sort stream took care and
						 * move on or IGNORE which means something ignorable in
						 * the event stream
						 */
					}// end read&sort event-at-a-time loop
					/* we get to this point if status was not EVENT */
					if (status == EventInputStatus.END_BUFFER) {
						if (!atBuffer) {
							atBuffer = true;
							bufferCount++;
						}
						endSort = false;
					} else if (status == EventInputStatus.END_RUN) {
						updateCounters();
						endSort = true; // tell control we are done
					} else if (status == EventInputStatus.END_FILE) {
						msgHandler.messageOutln("End of file reached");
						updateCounters();
						endSort = true; // tell control we are done
					} else if (status == EventInputStatus.UNKNOWN_WORD) {
						msgHandler
								.warningOutln(getClass().getName()
										+ ".sortOffline(): Unknown word in event stream.");
					} else {
						updateCounters();
						endSort = true;
						if (!offlineSortingCanceled()) {
							throw new IllegalStateException(
									"Illegal post-readEvent() status = "
											+ status);
						}
					}
					yield();
				}// end buffer loop
			}// end isSortNext loop
			sortControl.atSortEnd();
		}// end checkstate loop
	}

	/**
	 * Returns whether we are caught up in the ring buffer.
	 * 
	 * @return <code>true</code> if there are no unsorted buffers in the ring
	 *         buffer
	 */
	public boolean caughtUp() {
		return ringBuffer.isEmpty();
	}

	/**
	 * Update the counters display.
	 */
	private void updateCounters() {
		BROADCASTER.broadcast(BroadcastEvent.Command.COUNTERS_UPDATE);
	}

	/**
	 * Returns the number of events processed.
	 * 
	 * @return the number of events processed
	 */
	public int getEventCount() {
		synchronized (this) {
			return eventCount;
		}
	}

	/**
	 * Returns the number of events actually sorted.
	 * 
	 * @return number of events actually sorted
	 */
	public int getSortedCount() {
		synchronized (this) {
			return eventSortedCount;
		}
	}

	/**
	 * Set the number of events actually sorted.
	 * 
	 * @param count
	 *            new value
	 */
	public synchronized void setSortedCount(int count) {
		eventSortedCount = count;
	}

	private synchronized void incrementSortedCount() {
		eventSortedCount++;
	}

	/**
	 * Sets the number of events processed.
	 * 
	 * @param count
	 *            the number of events processed
	 */
	public synchronized void setEventCount(int count) {
		eventCount = count;
	}

	private synchronized void incrementEventCount() {
		eventCount++;
	}

	/**
	 * Returns the number of buffers processed.
	 * 
	 * @return the number of buffers processed
	 */
	public synchronized int getBufferCount() {
		return bufferCount;
	}

	/**
	 * Sets the number of buffers processed.
	 * 
	 * @param count
	 *            the number of buffers processed
	 */
	public synchronized void setBufferCount(int count) {
		bufferCount = count;
	}

	private synchronized void incrementBufferCount() {
		bufferCount++;
	}

	private int sortInterval = 1;

	private synchronized void increaseSortInterval() {
		sortInterval++;
		msgHandler.warningOutln("Sorting ring buffer half-full."
				+ " Sort interval increased to " + sortInterval + ".");
	}

	private synchronized void decreaseSortInterval() {
		if (sortInterval > 1) {
			sortInterval--;
		}
	}

	/**
	 * Returns the sort sample interval.
	 * 
	 * @see #setSortInterval(int)
	 * @return the total number of packets sent
	 */
	public synchronized int getSortInterval() {
		return sortInterval;
	}

}