package jam.sort.stream;

import jam.data.Scaler;

import java.io.EOFException;
import java.io.IOException;
import java.util.*;

/**
 * This class sorts the native data format of the CAEN V7x5 ADC's and TDC's.
 * Parameters are mapped from the slot number and unit channel # to indices in
 * the int array that gets passed to sort routines.
 * @version 18 Nov 2001
 * @author Dale Visser
 * @see AbstractEventInputStream
 * @since JDK1.1
 */
public class YaleCAEN_InputStream extends AbstractL002HeaderReader {

    public static final int[] ZEROS = new int[CAEN_StreamFields.NUM_CHANNELS];

    private enum BufferStatus {
        /**
         * Indicates the state where flushing of the remaining contents of the
         * buffer is occuring.
         */
        FIFO_ENDRUN_FLUSH,

        /**
         * State for when buffer is still filling and no output is available
         * yet.
         */
        FIFO_FILLING,

        /**
         * This is the state when the EventStream has characters in it
         * indicating that acquisition has been stopped or ended. In this
         * situation, all data has been read out from the ADC's and been sent to
         * Jam. So the stream needs to empty out it's remaining contents to the
         * sort routine.
         */
        FIFO_FLUSH,

        /**
         * In this state every new read from the stream requires that the oldest
         * event in the the "FIFO" buffer be pulled to be returned so as to make
         * room for a new event counter.
         */
        FIFO_FULL,

        /**
         * Indicates the state where we're reading through end-of-buffer padding
         * characters.
         */
        PADDING,

        /**
         * Indicates the state where a scaler block is being read.
         */
        SCALER
    }

    /**
     * Type of pointer in FIFO buffer.
     * @author Dale Visser
     */
    private enum FifoPointer {
        /**
         * where next get operation will be
         */
        GET,

        /**
         * where next put operation will be
         */
        PUT
    }

    static private final int END_COMPARE = 0x4000000;

    private static final short ENDRUN = (short) (CAEN_StreamFields.END_PAD & 0xffff);

    static private final int HEAD_COMPARE = 0x2000000;

    static private final int PARM_COMPARE = 0x0000000;

    static private final int TYPE_MASK = 0x7000000;

    // event counter/event should be retrieved
    // from
    private transient final int[] eventNumbers = new int[CAEN_StreamFields.BUFFER_DEPTH];

    /**
     * Keys are the event numbers, values are the array indices.
     */
    private transient final Map<Integer, Integer> eventNumMap = Collections
            .synchronizedMap(new HashMap<Integer, Integer>(
                    CAEN_StreamFields.BUFFER_DEPTH));

    private transient final int[][] fifo = new int[CAEN_StreamFields.BUFFER_DEPTH][CAEN_StreamFields.NUM_CHANNELS];

    private transient BufferStatus internalStat = BufferStatus.FIFO_FILLING;

    private transient FifoPointer lastIncr;// last incremented

    private transient int nScalrBlocks = 0; // for counting number of scaler

    private transient int posGet;// array index where next (i.e. oldest)

    private transient int posPut;// array index where next event counter is

    private transient final int[] tempData = new int[32];

    private transient final int[] tempParams = new int[32];

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

    /**
     * @see AbstractEventInputStream#AbstractEventInputStream(boolean)
     * @param console whether console exists
     */
    public YaleCAEN_InputStream(final boolean console) {
        super(console);
    }

    /**
     * @see AbstractEventInputStream#AbstractEventInputStream(boolean, int)
     * @param console whether console exists
     * @param eventSize the number of signals per event
     */
    public YaleCAEN_InputStream(final boolean console, final int eventSize) {
        super(console, eventSize);
    }

    @Override
    public String getFormatDescription() {
        return "Not an L002 format, but uses similar delimiters for special blocks.";
    }

    private void addEventIndex(final int eventNumber) {
        eventNumbers[posPut] = eventNumber;
        // automatically initialized to all zeros
        Arrays.fill(fifo[posPut], 0, CAEN_StreamFields.NUM_CHANNELS, 0);
        eventNumMap.put(eventNumber, posPut);
        incrementPut();
        if (fifoFull()) {
            internalStat = BufferStatus.FIFO_FULL;
        }
    }

    private boolean eventInFIFO(final int eventNumber) {
        return eventNumMap.containsKey(eventNumber);
    }

    private boolean fifoEmpty() {
        return posPut == posGet && lastIncr == FifoPointer.GET;
    }

    private boolean fifoFull() {
        return posPut == posGet && lastIncr == FifoPointer.PUT;
    }

    private int getEventIndex(final int eventNumber) {
        return eventNumMap.get(eventNumber);
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
            for (int i = 0; i < numParams; i++) {// NOPMD
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
        if (header == CAEN_StreamFields.BUFFER_END) {// return end of buffer
            // to
            // SortDaemon
            /* no need to flush here */
            rval = EventInputStatus.END_BUFFER;
            internalStat = BufferStatus.PADDING;
        } else if (header == CAEN_StreamFields.BUFFER_PAD) {
            rval = EventInputStatus.IGNORE;
            internalStat = BufferStatus.PADDING;
        } else if (header == CAEN_StreamFields.STOP_PAD) {
            internalStat = BufferStatus.FIFO_FLUSH;
        } else if (header == CAEN_StreamFields.END_PAD) {
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

    private void incrementGet() {
        posGet++;
        if (posGet == eventNumbers.length) {
            posGet = 0;
        }
        lastIncr = FifoPointer.GET;
    }

    private void incrementPut() {
        posPut++;
        if (posPut == eventNumbers.length) {
            posPut = 0;
        }
        lastIncr = FifoPointer.PUT;
    }

    private boolean inFlushState() {
        return internalStat == BufferStatus.FIFO_FLUSH
                || internalStat == BufferStatus.FIFO_ENDRUN_FLUSH;
    }

    /*
     * non-javadoc Checks whether the word type is for an event end-of-block
     */
    private boolean isEndBlock(final int data) {
        return (data & TYPE_MASK) == END_COMPARE;
    }

    @Override
    public boolean isEndRun(final short dataWord) {
        return (ENDRUN == dataWord);
    }

    /*
     * non-javadoc Checks whether the word type is for an event header.
     */
    private boolean isHeader(final int data) {
        return (data & TYPE_MASK) == HEAD_COMPARE;
    }

    /*
     * non-javadoc Checks whether the word type is for an event data word
     */
    private boolean isParameter(final int data) {
        return (data & TYPE_MASK) == PARM_COMPARE;
    }

    /**
     * Reads an event from the input stream Expects the stream position to be
     * the beginning of an event. It is up to the user to ensure this.
     * @exception EventException
     *                thrown for errors in the event stream
     */
    @Override
    public EventInputStatus readEvent(final int[] data) throws EventException {
        synchronized (this) {
            EventInputStatus rval = EventInputStatus.EVENT;
            int lastParameterRead = 0;
            final List<Integer> scalerValues = new ArrayList<>(32);
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
                        lastParameterRead = readEventParameters(header);
                    } else if (header == CAEN_StreamFields.SCALER_BLOCK) {
                        /*
                         * Read and ignore scaler values.
                         */
                        final int numScalers = dataInput.readInt();
                        nScalrBlocks++;
                        scalerValues.clear();
                        for (int i = 0; i < numScalers; i++) {
                            scalerValues.add(dataInput.readInt());
                        }
                        Scaler.update(scalerValues);
                        rval = EventInputStatus.SCALER_VALUE;
                        internalStat = BufferStatus.SCALER;
                    } else {
                        rval = handleSpecialHeaders(header, rval);
                    }
                }// end of while loop
                rval = readWhenNotFilling(data, rval);
            } catch (EOFException eofe) {
                /*
                 * we got to the end of a file or stream
                 */
                rval = EventInputStatus.END_FILE;
                LOGGER.warning(getClass().getName()
                        + ".readEvent(): End of File reached...file may be corrupted, or run not ended properly.");
            } catch (IOException ioe) {// we got to the end of a file or stream
                rval = EventInputStatus.UNKNOWN_WORD;
                LOGGER.warning(getClass().getName()
                        + ".readEvent(): Problem reading integer from stream.");
            } catch (EventException e) {
//                rval = EventInputStatus.UNKNOWN_WORD;
                throw new EventException(getClass().getName()
                        + ".readEvent() parameter = " + lastParameterRead, e);
            }
            return rval;
        }
    }

    /**
     * Read in the event parameters associated with the given header.
     * @param header
     *            the header word that was before the parameters
     * @return the last parameter word that was read
     * @throws IOException
     *             if there is a problem reading data
     * @throws EventException
     *             if an unexpected word is read
     */
    private int readEventParameters(final int header) throws IOException,
            EventException {
        /* ADC's & TDC's in slots 2-31 */
        final int slot = (header >>> 27) & 0x1f;
        boolean keepGoing = true;
        int lastParameterRead = 0;
        int paramIndex = 0;
        int numParams = 0;
        int endblock = 0;
        while (keepGoing) {
            lastParameterRead = dataInput.readInt();
            if (isParameter(lastParameterRead)) {
                numParams++;
                final int channel = (lastParameterRead >>> 16) & 0x3f;
                tempParams[paramIndex] = 32 * (slot - 2) + channel;
                tempData[paramIndex] = lastParameterRead & 0xfff;
                paramIndex++;
            } else if (isEndBlock(lastParameterRead)) {
                endblock = lastParameterRead;
                keepGoing = false;
            } else {
                throw new EventException(
                        getClass().getName()
                                + ".readEvent(): didn't get a Parameter or End-of-Block when expected, int datum = 0x"
                                + Integer.toHexString(lastParameterRead));
            }
        }
        handleEndBlock(endblock, numParams);
        return lastParameterRead;
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
                } else {// internal status must be FIFO_ENDRUN_FLUSH
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
}