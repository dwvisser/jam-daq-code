package jam.sort;

import jam.global.Beginner;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.Ender;
import jam.global.GoodThread;
import jam.global.MessageHandler;
import jam.global.SortMode;
import jam.sort.stream.EventInputStatus;
import jam.sort.stream.EventInputStream;

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

	private final Controller controller;

	private final MessageHandler msgHandler;

	private SortRoutine sortRoutine;

	private EventInputStream eventInputStream;

	/**
	 * mode offline or online
	 */
	private SortMode mode;

	private static final Broadcaster broadcaster=Broadcaster.getSingletonInstance();

	/**
	 * Used for online only, holds data buffers from network.
	 */
	private RingBuffer ringBuffer;
	
	/* event information */ 
	private int eventSize;

	private int[] eventData;

	private int[] eventDataZero;

	private int eventCount;

	private int eventSortedCount;

	private int bufferCount;

	/**
	 * Creates a new <code>SortDaemon</code> process.
	 * 
	 * @param con
	 *            the sort control process
	 * @param mh
	 *            the console for writing out messages to the user
	 */
	public SortDaemon(Controller con, MessageHandler mh) {
		controller = con;
		msgHandler = mh;
		setName("Event Sorter");
	}

	/**
	 * setup the sort deamon tell it the mode and stream
	 * 
	 * @param mode
	 *            <code>ONLINE</code> or <code>OFFLINE</code>
	 * @param eventInputStream
	 *            the source of event data
	 * @param eventSize
	 *            number of parameters per event
	 */
	public void setup(SortMode mode, EventInputStream eventInputStream, int eventSize) {
		this.mode = mode;
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
	 * @param sortRoutine
	 *            an object capable of sorting event data
	 */
	public void setSortRoutine(SortRoutine sortRoutine) {
		this.sortRoutine = sortRoutine;
	}

	/**
	 * Sets the ring buffer to pull event data from.
	 * 
	 * @param ringBuffer
	 *            the source of event data
	 */
	public void setRingBuffer(RingBuffer ringBuffer) {
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
	public synchronized void setSortInterval(int sample) {
		sortInterval = sample;
	}

	/**
	 * set the state of the event output
	 */
	public void setWriteEnabled(boolean state) {
		sortRoutine.setWriteEnabled(state);
	}

	/**
	 * Sets the event size.
	 * 
	 * @param size
	 *            the number of parameters per event
	 */
	public void setEventSize(int size) {
		eventSize = size;
		/* JVM spec--both arrays initialized w/ zeroes. */
		eventData = new int[eventSize];
		eventDataZero = new int[eventSize];
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
			if (mode.isOnline()) {//which type of sort to do
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
	 * Invokes begin() in the user's sort routine if it
	 * implements the <code>Beginner</code> interface.
	 *
	 * @see jam.global.Beginner
	 */
	public synchronized void userBegin(){
		if (sortRoutine instanceof Beginner){
			((Beginner)sortRoutine).begin();
		}
	}
	
	/**
	 * Invokes end() in the user's sort routine if it
	 * implements the <code>Ender</code> interface.
	 *
	 * @see jam.global.Ender
	 */
	public synchronized void userEnd(){
		if (sortRoutine instanceof Ender){
			((Ender)sortRoutine).end();
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
		final RingInputStream ringInputStream=new RingInputStream();
		byte[] buffer;
		while (true) { //loop while acquisition on
			//controller.atSortStart(); //does nothing for online
			/* Get a new buffer and make an input stream out of it. */
			if (ringBuffer.isHalfFull()) {
				increaseSortInterval();
			} else if (ringBuffer.isEmpty()){
				decreaseSortInterval();
			}
			buffer = ringBuffer.getBuffer();
			ringInputStream.setBuffer(buffer);
			eventInputStream.setInputStream(ringInputStream);
			/* Zero event array. */
			System.arraycopy(eventDataZero, 0, eventData, 0, eventSize);
			EventInputStatus status;
			while ((((status = eventInputStream.readEvent(eventData)) == EventInputStatus.EVENT)
					|| (status == EventInputStatus.SCALER_VALUE) || (status == EventInputStatus.IGNORE))) {
				if (status == EventInputStatus.EVENT) {
					/* Sort only the sortInterval'th events. */
					if (getEventCount() % getSortInterval() == 0) {
						sortRoutine.sort(eventData);
						incrementSortedCount();
					}
					incrementEventCount();
					/* Zero event array and get ready for next event. */
					System.arraycopy(eventDataZero, 0, eventData, 0, eventSize);
				} //else SCALER_VALUE, assume sort stream took care and move on
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
			} else {//we have unknown status
				/* unrecoverable error should not be here */
				throw new SortException(
						"Sorter stopped due to unknown status: " + status);
			}
			yield();
		}//end infinite loop
	}

	private boolean osc = false;

	private final Object offlineSortingLock = new Object();

	/**
	 * Call to gracefully interrupt and end the current offline sort. This is
	 * used as one of the conditions to read in the next buffer
	 */
	public void cancelOfflineSorting() {
		synchronized (offlineSortingLock) {
			osc = true;
		}
	}

	private void resumeOfflineSorting() {
		synchronized (offlineSortingLock) {
			osc = false;
		}
	}

	private boolean offlineSortingCanceled() {
		synchronized (offlineSortingLock) {
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
		EventInputStatus status = EventInputStatus.IGNORE;
		boolean atBuffer = false; //are we at a buffer word
		/*
		 * Next statement causes checkState() to immediately suspend this
		 * thread.
		 */
		controller.atSortStart();
		while (checkState()) {//checkstate loop
			/* suspends this thread when we're done sorting all files */
			controller.atSortStart();
			resumeOfflineSorting();//after we come out of suspend
			/* Loop for each new sort file. */
			while (!offlineSortingCanceled() && controller.isSortNext()) {
				boolean endSort = false;
				while (!offlineSortingCanceled() && !endSort) {//buffer loop
					/* Zero the event container. */
					System.arraycopy(eventDataZero, 0, eventData, 0, eventSize);
					/* Loop to read & sort one event at a time. */
					while (!offlineSortingCanceled()
							&& (((status = eventInputStream
									.readEvent(eventData)) == EventInputStatus.EVENT)
									|| (status == EventInputStatus.SCALER_VALUE) || (status == EventInputStatus.IGNORE))) {
						if (!offlineSortingCanceled()) {
							if (status == EventInputStatus.EVENT) {
								sortRoutine.sort(eventData);
								incrementEventCount();
								incrementSortedCount();
								/*
								 * Zero event array and get ready for next
								 * event.
								 */
								System.arraycopy(eventDataZero, 0, eventData,
										0, eventSize);
								atBuffer = false;
								if (getEventCount() % COUNT_UPDATE == 0) {
									updateCounters();
									yield();
								}
							}
						} else {//cause us to exit smoothly
							status = EventInputStatus.END_RUN;
						}
						/*
						 * else SCALER_VALUE, assume sort stream took care and
						 * move on or IGNORE which means something ignorable in
						 * the event stream
						 */
					}//end read&sort event-at-a-time loop
					/* we get to this point if status was not EVENT */
					if (status == EventInputStatus.END_BUFFER) {
						if (!atBuffer) {
							atBuffer = true;
							bufferCount++;
						}
						endSort = false;
					} else if (status == EventInputStatus.END_RUN) {
						updateCounters();
						endSort = true; //tell control we are done
					} else if (status == EventInputStatus.END_FILE) {
						msgHandler.messageOutln("End of file reached");
						updateCounters();
						endSort = true; //tell control we are done
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
				}//end buffer loop
			}//end isSortNext loop
			controller.atSortEnd();
		}//end checkstate loop
	}

	/**
	 * Are we caught up in the ring buffer. That is there are no unsorted
	 * buffers in the ring buffer.
	 */
	public boolean caughtUp() {
		return ringBuffer.isEmpty();
	}

	/**
	 * Update the counters display.
	 */
	private void updateCounters() {
		broadcaster.broadcast(BroadcastEvent.Command.COUNTERS_UPDATE);
	}

	/**
	 * Returns the number of events processed.
	 * 
	 * @return the number of events processed
	 */
	public synchronized int getEventCount() {
		return eventCount;
	}

	public synchronized int getSortedCount() {
		return eventSortedCount;
	}

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
	
	private synchronized void decreaseSortInterval(){
		if (sortInterval>1){
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