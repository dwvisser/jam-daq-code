package jam.sort;

import jam.global.Beginner;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.Ender;
import jam.global.GoodThread;
import jam.global.JamStatus;
import jam.global.Sorter;
import jam.sort.control.Controller;
import jam.sort.control.SortControl;
import jam.sort.stream.AbstractEventInputStream;
import jam.sort.stream.EventException;
import jam.sort.stream.AbstractEventInputStream.EventInputStatus;

import java.util.Arrays;
import java.util.logging.Level;

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

	private static final Broadcaster BROADCASTER = Broadcaster
			.getSingletonInstance();

	private transient boolean atBuffer = false; // are we at a buffer word

	private int bufferCount;

	private boolean callSort = true;

	private transient final Controller controller;

	private transient boolean endSort;

	private int eventCount;

	private transient AbstractEventInputStream eventInputStream;

	/* event information */
	private int eventSize;

	private transient int eventSortedCount;

	private transient final Object offlineSortLock = new Object();

	private transient boolean osc = false;

	/**
	 * Used for online only, holds data buffers from network.
	 */
	private transient RingBuffer ringBuffer;

	private transient Sorter sorter;

	private int sortInterval = 1;

	/**
	 * Creates a new <code>SortDaemon</code> process.
	 * 
	 * @param con
	 *            the sort control process
	 */
	public SortDaemon(Controller con) {
		super();
		controller = con;
		setName("Event Sorter");
	}

	/**
	 * Call to gracefully interrupt and end the current offline sort. This is
	 * used as one of the conditions to read in the next buffer
	 */
	public void cancelOfflineSorting() {
		synchronized (offlineSortLock) {
			osc = true;
		}
	}

	/**
	 * Called to resume sort
	 */
	private void resumeOfflineSorting() {
		synchronized (offlineSortLock) {
			osc = false;
		}
	}

	/**
	 * Called to check if sort was canceled.
	 * 
	 * @return
	 */
	private boolean offlineSortingCanceled() {
		synchronized (offlineSortLock) {
			return osc;
		}
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

	private void decreaseSortInterval() {
		synchronized (this) {
			if (sortInterval > 1) {
				sortInterval--;
			}
		}
	}

	/**
	 * Returns the number of buffers processed.
	 * 
	 * @return the number of buffers processed
	 */
	public int getBufferCount() {
		synchronized (this) {
			return bufferCount;
		}
	}

	private boolean getCallSort() {
		synchronized (this) {
			return callSort;
		}
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
	 * Returns the event size.
	 * 
	 * @return the number of parameters per event
	 */
	public int getEventSize() {
		return eventSize;
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
	 * Returns the sort sample interval.
	 * 
	 * @see #setSortInterval(int)
	 * @return the total number of packets sent
	 */
	public int getSortInterval() {
		synchronized (this) {
			return sortInterval;
		}
	}

	private void handleStatusOffline(final EventInputStatus status) {
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
			LOGGER.info("End of file reached");
			updateCounters();
			endSort = true; // tell control we are done
		} else if (status == EventInputStatus.UNKNOWN_WORD) {
			LOGGER.warning(getClass().getName()
					+ ".sortOffline(): Unknown word in event stream.");
		} else {
			updateCounters();
			endSort = true;
			if (!offlineSortingCanceled()) {
				throw new IllegalStateException(
						"Illegal post-readEvent() status = " + status);
			}
		}
	}

	private void handleStatusOnline(final EventInputStatus status)
			throws SortException {
		if (status == EventInputStatus.END_BUFFER) {
			/* We have reached the end of a buffer. */
			incrementBufferCount();
			yield();
		} else if (status == EventInputStatus.END_RUN) {
			incrementBufferCount();
			yield();
		} else if (status == EventInputStatus.UNKNOWN_WORD) {
			LOGGER.warning("Unknown word in event stream.");
		} else if (status == EventInputStatus.END_FILE) {
			LOGGER.warning("Tried to read past end of event input stream.");
		} else {// we have unknown status
			/* unrecoverable error should not be here */
			throw new SortException("Sorter stopped due to unknown status: "
					+ status);
		}
	}

	private void increaseSortInterval() {
		synchronized (this) {
			sortInterval++;
			LOGGER.warning("Sorting ring buffer half-full."
					+ " Sort interval increased to " + sortInterval + ".");
		}
	}

	private void incrementBufferCount() {
		synchronized (this) {
			bufferCount++;
		}
	}

	private void incrementEventCount() {
		synchronized (this) {
			eventCount++;
		}
	}

	private void incrementSortedCount() {
		synchronized (this) {
			eventSortedCount++;
		}
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
			LOGGER.log(Level.SEVERE, "Sorter stopped due to exception.", e);
		}
	}

	/**
	 * Sets the number of buffers processed.
	 * 
	 * @param count
	 *            the number of buffers processed
	 */
	public void setBufferCount(final int count) {
		synchronized (this) {
			bufferCount = count;
		}
	}

	private void setCallSort(final boolean state) {
		synchronized (this) {
			callSort = state;
		}
	}

	/**
	 * Sets the number of events processed.
	 * 
	 * @param count
	 *            the number of events processed
	 */
	public void setEventCount(final int count) {
		synchronized (this) {
			eventCount = count;
		}
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
	 * Sets the ring buffer to pull event data from.
	 * 
	 * @param ringBuffer
	 *            the source of event data
	 */
	public void setRingBuffer(final RingBuffer ringBuffer) {
		this.ringBuffer = ringBuffer;
	}

	/**
	 * Set the number of events actually sorted.
	 * 
	 * @param count
	 *            new value
	 */
	public void setSortedCount(final int count) {
		synchronized (this) {
			eventSortedCount = count;
		}
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
	 * Sets whether to write selected events to disk.
	 * 
	 * @param state
	 *            whether writing of selected events to disk is enabled
	 */
	public void setWriteEnabled(final boolean state) {
		sorter.setWriteEnabled(state);
	}

	/**
	 * Performs the offline sorting until an end-of-run state is reached in the
	 * event stream.
	 * 
	 * @exception Exception
	 *                thrown if an unrecoverable error occurs during sorting
	 */
	public void sortOffline() throws Exception {// NOPMD
		final SortControl sortControl = (SortControl) controller;
		final int[] eventData = new int[eventSize];
		EventInputStatus status = EventInputStatus.IGNORE;
		/*
		 * Next statement causes checkState() to immediately suspend this
		 * thread.
		 */
		setState(GoodThread.State.SUSPEND);
		while (checkState()) {// checkstate loop
			endSort = false;
			/* suspends this thread when we're done sorting all files */
			setState(GoodThread.State.SUSPEND);
			resumeOfflineSorting();// after we come out of suspend
			/* Loop for each new sort file. */
			while (!offlineSortingCanceled() && sortControl.openNextFile()) {
				while (!offlineSortingCanceled() && !endSort) {// buffer loop
					status = sortEventsInFile(eventData);
					/* we get to this point if status was not EVENT */
					handleStatusOffline(status);
					yield();
				}// end buffer loop
			}// end isSortNext loop
			sortControl.atSortEnd();
		}// end checkstate loop
	}

	/**
	 * @param eventData
	 * @return
	 * @throws EventException
	 * @throws Exception
	 */
	private EventInputStatus sortEventsInFile(final int[] eventData)//NOPMD
			throws EventException, Exception {//NOPMD
		EventInputStatus status;
		/* Zero the event container. */
		Arrays.fill(eventData, 0);
		/* Loop to read & sort one event at a time. */
		status = eventInputStream.readEvent(eventData);
		while (!offlineSortingCanceled()
				&& (status == EventInputStatus.EVENT
						|| status == EventInputStatus.SCALER_VALUE || status == EventInputStatus.IGNORE)) {
			status = sortEvent(eventData, status);
		}// end read&sort event-at-a-time loop
		return status;
	}

	/**
	 * @param eventData
	 * @param status
	 * @return
	 * @throws Exception
	 * @throws EventException
	 */
	private EventInputStatus sortEvent(final int[] eventData,
			final EventInputStatus status) throws Exception, EventException {//NOPMD
		EventInputStatus rval = status;
		if (offlineSortingCanceled()) {
			rval = EventInputStatus.END_RUN;
		} else if (rval == EventInputStatus.EVENT) {
			sorter.sort(eventData);
			incrementEventCount();
			incrementSortedCount();
			/*
			 * Zero event array and get ready for next event.
			 */
			Arrays.fill(eventData, 0);
			atBuffer = false;
			periodicallyUpdateCounters();
		}
		/*
		 * else SCALER_VALUE, assume sort stream took care and move on or IGNORE
		 * which means something ignorable in the event stream
		 */
		if (!offlineSortingCanceled()) {
			rval = eventInputStream.readEvent(eventData);
		}
		return rval;
	}

	/**
	 * 
	 */
	private void periodicallyUpdateCounters() {
		// Number of events to occur before updating counters.
		final int COUNT_UPDATE = 1000;
		if (getEventCount() % COUNT_UPDATE == 0) {
			updateCounters();
			yield();
		}
	}

	/**
	 * Performs the online sorting until an end-of-run state is reached in the
	 * event stream.
	 * 
	 * @exception Exception
	 *                thrown if an unrecoverable error occurs during sorting
	 */
	public void sortOnline() throws Exception {// NOPMD
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
			status = eventInputStream.readEvent(eventData);
			while (((status == EventInputStatus.EVENT)
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
				}
				// else SCALER_VALUE, assume sort stream took care and move on
				status = eventInputStream.readEvent(eventData);
			}
			handleStatusOnline(status);
			yield();
		}// end infinite loop
	}

	/**
	 * Update the counters display.
	 */
	private void updateCounters() {
		BROADCASTER.broadcast(BroadcastEvent.Command.COUNTERS_UPDATE);
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

}