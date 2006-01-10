package jam.sort.stream;

import jam.data.Scaler;
import jam.global.MessageHandler;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class sorts the native data format of the CAEN V7x5 ADC's and TDC's.
 * Parameters are mapped from the slot number and unit channel # to indices in
 * the int array that gets passed to sort routines.
 * 
 * @version 18 Nov 2001
 * @author Dale Visser
 * @see AbstractEventInputStream
 * @since JDK1.1
 */
public class YaleCAEN_InputStream extends AbstractL002HeaderReader implements
		CAEN_StreamFields {

	private enum BufferStatus {
	    /**
	     * State for when buffer is still filling and no output is available yet.
	     */
	    FIFO_FILLING,
	    
	    /**
	     * In this state every new read from the stream requires that the oldest 
	     * event in the the "FIFO" buffer be pulled to be returned so as to make room
	     * for a new event counter.
	     */
	    FIFO_FULL,
	    
	    /**
	     * This is the state when the EventStream has characters in it indicating that
	     * acquisition has been stopped or ended.  In this situation, all data has been 
	     * read out from the ADC's and been sent to Jam.  So the stream needs to empty out
	     * it's remaining contents to the sort routine.
	     */
	    FIFO_FLUSH,
	    
	    /**
	     * Indicates the state where flushing of the remaining contents of the
	     * buffer is occuring.
	     */
	    FIFO_ENDRUN_FLUSH,
	    
	    /**
	     * Indicates the state where a scaler block is being read.
	     */
	    SCALER,
	    
	    /**
	     * Indicates the state where we're reading through end-of-buffer
	     * padding characters.
	     */
	    PADDING
	}
	
	private transient BufferStatus internalStat = BufferStatus.FIFO_FILLING;

	private transient int nScalrBlocks = 0; // for counting number of scaler

	// blocks in the file

	private transient final int[][] fifo = new int[BUFFER_DEPTH][NUM_CHANNELS];

	private transient int[] eventNumbers = new int[BUFFER_DEPTH];

	private transient int posPut;// array index where next event counter is

	// to be written to FIFO

	private transient int posGet;// array index where next (i.e. oldest)

	// event counter/event should be retrieved
	// from

	private transient FifoPointer lastIncr;// last incremented

	/**
	 * Hashtable keys are the event numbers, objects are the array indices.
	 */
	private transient final Map<Integer, Integer> eventNumMap = Collections
			.synchronizedMap(new HashMap<Integer, Integer>(BUFFER_DEPTH));

	/**
	 * Make sure to issue a setConsole() after using this constructor. It is
	 * here to satisfy the requirements of Class.newInstance()
	 */
	public YaleCAEN_InputStream() {
		super();
		posPut = 0;
		posGet = 0;
		lastIncr = FifoPointer.GET;// initially empty requires last incremented
									// to be GET
	}

	private void incrementPut() {
		posPut++;
		if (posPut == eventNumbers.length) {
			posPut = 0;
		}
		lastIncr = FifoPointer.PUT;
	}

	private void incrementGet() {
		posGet++;
		if (posGet == eventNumbers.length) {
			posGet = 0;
		}
		lastIncr = FifoPointer.GET;
	}

	/**
	 * @see AbstractEventInputStream#AbstractEventInputStream(MessageHandler)
	 */
	public YaleCAEN_InputStream(MessageHandler console) {
		super(console);
	}

	private boolean eventInFIFO(final int eventNumber) {
		return eventNumMap.containsKey(eventNumber);
	}

	private int getEventIndex(final int eventNumber) {
		return eventNumMap.get(eventNumber);
	}

	private void addEventIndex(final int eventNumber) {
		eventNumbers[posPut] = eventNumber;
		// automatically initialized to all zeros
		final int[] zeros = new int[NUM_CHANNELS];
		System.arraycopy(zeros, 0, fifo[posPut], 0, zeros.length);
		eventNumMap.put(eventNumber, posPut);
		incrementPut();
		if (fifoFull()) {
			internalStat = BufferStatus.FIFO_FULL;
		}
	}

	private boolean fifoFull() {
		return posPut == posGet && lastIncr == FifoPointer.PUT;
	}

	private boolean fifoEmpty() {
		return posPut == posGet && lastIncr == FifoPointer.GET;
	}

	private void getFirstEvent(final int[] data) {
		final int eventNumber = eventNumbers[posGet];
		eventNumMap.remove(eventNumber);
		final int[] rval = fifo[posGet];
		System.arraycopy(rval, 0, data, 0, data.length);
		incrementGet();
		if (!inFlushState()) {
			internalStat = BufferStatus.FIFO_FILLING;
		}
	}

	private boolean inFlushState() {
		return internalStat == BufferStatus.FIFO_FLUSH
				|| internalStat == BufferStatus.FIFO_ENDRUN_FLUSH;
	}

	/**
	 * @see AbstractEventInputStream#AbstractEventInputStream(MessageHandler, int)
	 */
	public YaleCAEN_InputStream(MessageHandler console, int eventSize) {
		super(console, eventSize);
	}

	private transient final int[] tempParams = new int[32];

	private transient final int[] tempData = new int[32];

	/**
	 * Reads an event from the input stream Expects the stream position to be
	 * the beginning of an event. It is up to the user to ensure this.
	 * 
	 * @exception EventException
	 *                thrown for errors in the event stream
	 */
	public EventInputStatus readEvent(final int[] data) throws EventException {
		synchronized (this) {
			EventInputStatus rval = EventInputStatus.EVENT;
			int parameter = 0;
			int endblock = 0;
			final List<Integer> tval = new ArrayList<Integer>(32); // temporary array for scaler
			// values, up
			// to a max of 32
			try {
				/*
				 * internal_status may also be in a "flush" mode in which case
				 * we skip this read loop and go straight to flushing out
				 * another event
				 */
				while (internalStat == BufferStatus.FIFO_FILLING) {
					/*
					 * this loop may finish if status changes to "fifo full"
					 * mode when an event index gets added below
					 */
					final int header = dataInput.readInt();
					if (isHeader(header)) {
						/* ADC's & TDC's in slots 2-31 */
						final int slot = (header >>> 27) & 0x1f;
						boolean keepGoing = true;
						int paramIndex = 0;
						int numParams = 0;
						while (keepGoing) {
							parameter = dataInput.readInt();
							if (isParameter(parameter)) {
								numParams++;
								final int channel = (parameter >>> 16) & 0x3f;
								tempParams[paramIndex] = 32 * (slot - 2)
										+ channel;
								tempData[paramIndex] = parameter & 0xfff;
								paramIndex++;
							} else if (isEndBlock(parameter)) {
								endblock = parameter;
								keepGoing = false;
							} else {
								throw new EventException(
										getClass().getName()
												+ ".readEvent(): didn't get a Parameter or End-of-Block when expected, int datum = 0x"
												+ Integer
														.toHexString(parameter));
							}
						}
						handleEndBlock(endblock, numParams);
					} else if (header == SCALER_BLOCK) {// read and ignore
						// scaler
						// values
						final int numScalers = dataInput.readInt();
						nScalrBlocks++;
						for (int i = 0; i < numScalers; i++) {
							tval.add(dataInput.readInt());
						}
						Scaler.update(tval);
						rval = EventInputStatus.SCALER_VALUE;
						internalStat = BufferStatus.SCALER;
					} else {
						rval = handleSpecialHeaders(header, rval);
					}
				}// end of while loop
				rval = readWhenNotFilling(data, rval);
			} catch (EOFException eofe) {// we got to the end of a file or
				// stream
				rval = EventInputStatus.END_FILE;
				console
						.warningOutln(getClass().getName()
								+ ".readEvent(): End of File reached...file may be corrupted, or run not ended properly.");
			} catch (IOException ioe) {// we got to the end of a file or stream
				rval = EventInputStatus.UNKNOWN_WORD;
				console.warningOutln(getClass().getName()
						+ ".readEvent(): Problem reading integer from stream.");
			} catch (EventException e) {
				rval = EventInputStatus.UNKNOWN_WORD;
				throw new EventException(getClass().getName()
						+ ".readEvent() parameter = " + parameter, e);
			}
			return rval;
		}
	}

	/*
	 * We've dropped out of the while loop, which means either that the internal
	 * status is not FIFO_FILLING, or that eventReady is set to true (i.e.
	 * encountered buffer pad or scaler) The first case is handled here, if it's
	 * true.
	 */
	private EventInputStatus readWhenNotFilling(final int[] data,
			final EventInputStatus init) {
		EventInputStatus rval = init;
		if (inFlushState()) {// in one of the 2 flush states
			if (fifoEmpty()) {
				if (internalStat == BufferStatus.FIFO_FLUSH) {
					rval = EventInputStatus.END_BUFFER;
				} else {// internal status must be "endrun flush"
					rval = EventInputStatus.END_RUN;
				}
				internalStat = BufferStatus.FIFO_FILLING;
			} else {// all events flushed, make ready for next event
				getFirstEvent(data);
				rval = EventInputStatus.EVENT;
			}
			/*
			 * The other possibility is that the FIFO is full and we need to
			 * output an event.
			 */
		} else if (internalStat == BufferStatus.FIFO_FULL) {
			getFirstEvent(data);// routine retrieves data and updates
			// tracking variables
			rval = EventInputStatus.EVENT;
		} else {// internal status=SCALER or PADDING
			/* set to FIFO_FILLING so next call will enter loop */
			internalStat = BufferStatus.FIFO_FILLING;
		}
		return rval;
	}

	/*
	 * If we really have end-of-block like we should, stick event data in the
	 * appropriate space in our FIFO.
	 */
	private void handleEndBlock(final int endblock, final int numParams)
			throws EventException {
		if (isEndBlock(endblock)) {
			final int eventNumber = endblock & 0xffffff;
			if (!eventInFIFO(eventNumber)) {// Event # not in
				// FIFO,
				// so need to add it.
				addEventIndex(eventNumber);// can change
				// internal
				// state to FIFO_FULL
			}
			final int arrayIndex = getEventIndex(eventNumber);
			/* copy data in, item by item */
			for (int i = 0; i < numParams; i++) {
				fifo[arrayIndex][tempParams[i]] = tempData[i];
			}
		} else {
			throw new EventException(
					getClass().getName()
							+ ".readEvent(): didn't get a end of block when expected, int datum = 0x"
							+ Integer.toHexString(endblock));
		}
	}

	private EventInputStatus handleSpecialHeaders(final int header,
			final EventInputStatus init) {
		EventInputStatus rval = init;
		if (header == BUFFER_END) {// return end of buffer
			// to
			// SortDaemon
			/* no need to flush here */
			rval = EventInputStatus.END_BUFFER;
			internalStat = BufferStatus.PADDING;
		} else if (header == BUFFER_PAD) {
			rval = EventInputStatus.IGNORE;
			internalStat = BufferStatus.PADDING;
		} else if (header == STOP_PAD) {
			internalStat = BufferStatus.FIFO_FLUSH;
		} else if (header == END_PAD) {
			internalStat = BufferStatus.FIFO_ENDRUN_FLUSH;
			showMessage("Scaler blocks in file =" + nScalrBlocks);
			nScalrBlocks = 0;
		} else {
			/* using IGNORE since UNKNOWN WORD causes annoying beeps */
			rval = EventInputStatus.IGNORE;
			internalStat = BufferStatus.PADDING;
		}
		return rval;
	}

	static private final int TYPE_MASK = 0x7000000;

	static private final int PARM_COMPARE = 0x0000000;

	/*
	 * non-javadoc Checks whether the word type is for an event data word
	 */
	private boolean isParameter(final int data) {
		return (data & TYPE_MASK) == PARM_COMPARE;
	}

	static private final int HEAD_COMPARE = 0x2000000;

	/*
	 * non-javadoc Checks whether the word type is for an event header.
	 */
	private boolean isHeader(final int data) {
		return (data & TYPE_MASK) == HEAD_COMPARE;
	}

	static private final int END_COMPARE = 0x4000000;

	/*
	 * non-javadoc Checks whether the word type is for an event end-of-block
	 */
	private boolean isEndBlock(final int data) {
		return (data & TYPE_MASK) == END_COMPARE;
	}

	private static final short ENDRUN = (short) (END_PAD & 0xffff);

	public boolean isEndRun(final short dataWord) {
		return (ENDRUN == dataWord);
	}
}