package jam.sort.stream;

import jam.data.Scaler;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class knows how to handle Uconn Be7. It extends EventInputStream, adding
 * methods for reading events and returning them as int arrays which the sorter
 * can handle.
 * @version 0.5 April 98
 * @author Ken Swartz
 * @author Jim McDonald
 * @see AbstractEventInputStream
 * @since JDK1.1
 */
public final class UconnInputStream extends AbstractEventInputStream {

    private transient int blockCurrSize;

    private transient int blockFullSize;

    private transient int blockNumEvnt;

    /**
     * Internal event counter, needs persistence beyond the readEvent() methos.
     */
    private transient int countEvent = 0;// NOPMD

    private transient short eventNumWord;

    private transient short eventState;

    private transient boolean newBlock = true;// NOPMD

    /**
     * @see AbstractEventInputStream#AbstractEventInputStream(boolean)
     */
    public UconnInputStream(final boolean console) {
        super(console);
    }

    /**
     * @see AbstractEventInputStream#AbstractEventInputStream(boolean, int)
     */
    public UconnInputStream(final boolean console, final int eventSize) {
        super(console, eventSize);
    }

    /**
     * Is the word a end-of-run word
     */
    @Override
    public boolean isEndRun(final short dataWord) {
        /* no end run marker */
        return false;
    }

    /**
     * Reads a block from the input stream Expects the stream to be at the
     * beginning of a block It is up to user to ensure this
     * @return <code>true</code> if successful
     * @exception EventException
     *                thrown for errors in event stream
     */
    private boolean readBlockHeader() throws EventException {
        boolean rval = false;
        try {
            blockFullSize = dataInput.readInt();
            blockCurrSize = dataInput.readInt();
            final int blockNumber = dataInput.readInt();
            blockNumEvnt = dataInput.readInt();
            showMessage("Block fullsize  " + blockFullSize + " currsize "
                    + blockCurrSize + " number " + blockNumber
                    + "number event " + blockNumEvnt);
            /* read in scalers */
            final List<Integer> scalerValues = new ArrayList<>(
                    UconnStreamConstants.NUMBER_SCALERS);
            for (int i = 0; i < UconnStreamConstants.NUMBER_SCALERS; i++) {
                scalerValues.add(dataInput.readInt()
                        & UconnStreamConstants.SCALER_MASK);
            }
            Scaler.update(scalerValues);
            rval = true;
        } catch (EOFException eof) {
            showMessage("end of file readBlockHeader");
        } catch (IOException ioe) {
            throw new EventException("Reading Block header.", ioe);
        }
        return rval;
    }

    /**
     * One allocation instead of many on readEvent() calls.
     */
    private transient EventInputStatus eventInputStatus;// NOPMD

    /**
     * Reads an event from the input stream Expects the stream position to be
     * the beginning of an event. It is up to the user to ensure this.
     * @exception EventException
     *                thrown for errors in the event stream
     */
    @Override
    public EventInputStatus readEvent(int[] input) throws EventException {
        synchronized (this) {
            try {
                eventInputStatus = EventInputStatus.ERROR;
                // if a new block read in block header
                if (newBlock) {
                    if (readBlockHeader()) {
                        newBlock = false;
                        countEvent = 0;
                    } else {
                        eventInputStatus = EventInputStatus.END_FILE;
                    }
                    // check if we are done with this buffer
                } else if (countEvent == blockNumEvnt) {
                    // are we done with this block
                    final long numSkip = blockFullSize - blockCurrSize;
                    final long skipped = dataInput.skip(numSkip);
                    if (skipped < numSkip) {
                        throw new EventException("Tried to skip " + numSkip
                                + "bytes. Was only able to skip " + skipped
                                + ".");
                    }
                    newBlock = true;
                    eventInputStatus = EventInputStatus.END_BUFFER;
                } else {
                    // read in the event header
                    readEventHeader();
                    unpackData(input);
                    // flush out rest of event
                    final long numSkip = eventSize - 2
                            * UconnStreamConstants.HEAD_SIZE - 2
                            * eventNumWord;
                    final long skipped = dataInput.skip(numSkip);
                    if (skipped < numSkip) {
                        throw new EventException("Tried to skip " + numSkip
                                + "bytes. Was only able to skip " + skipped
                                + ".");
                    }
                    // event state
                    input[64] = eventState;
                    countEvent++;
                    eventInputStatus = EventInputStatus.EVENT;
                    // we got to the end of a file or stream
                }
            } catch (IOException io) {
                eventInputStatus = EventInputStatus.END_FILE;
                throw new EventException("Reading event.", io);
            }
            return eventInputStatus;
        }
    }

    /*
     * non-javadoc: read a event header
     */
    private void readEventHeader() throws EventException {
        try {
            final int eventId = dataInput.readInt();
            eventSize = dataInput.readShort();
            eventState = dataInput.readShort();
            eventNumWord = dataInput.readShort();
            showMessage("Event id " + eventId + " size " + eventSize
                    + " state " + eventState + " numWord " + eventNumWord);

        } catch (IOException ioe) {
            throw new EventException("Reading Event header,"
                    + ioe.getMessage() + " [UconnnInputStream]", ioe);
        }
    }

    /**
     * Read an event stream header.
     * @exception EventException
     *                thrown for errors in the event stream
     */
    @Override
    public boolean readHeader() throws EventException {
        return true;
    }

    /*
     * non-javadoc: we are at an event so unpack it
     */
    private void unpackData(int[] input) throws IOException, EventException {
        int vsn = 0;
        /* while there are words left in the event */
        for (int i = 0; i < eventNumWord; i++) {
            final short dataWord = dataInput.readShort();// read word
            // if new event check we start with a vsn
            if (i == 0 && (dataWord & UconnStreamConstants.VSN_MARKER) == 0) {
                throw new EventException(
                        " Event not started with vsn [UconnInputStream]");
            }
            /* we have vsn */
            if ((dataWord & UconnStreamConstants.VSN_MARKER) == 0) {// data word
                int chan, data;
                // if vsn of adc 1
                if (vsn < UconnStreamConstants.VSN_TDC) {
                    chan = (dataWord >> UconnStreamConstants.ADC_CHAN_SHFT)
                            & UconnStreamConstants.ADC_CHAN_MASK;
                    data = dataWord & UconnStreamConstants.ADC_DATA_MASK;
                    /* offset data by ADC_OFFSET */
                    input[vsn * UconnStreamConstants.ADC_OFFSET + chan] = data;
                } else {// if tdc
                    chan = (dataWord >> UconnStreamConstants.TDC_CHAN_SHFT)
                            & UconnStreamConstants.TDC_CHAN_MASK;
                    data = dataWord & UconnStreamConstants.TDC_DATA_MASK;
                    input[UconnStreamConstants.TDC_OFFSET + chan] = data;
                }
                showMessage("ch " + chan + " data " + data);
                /* end data sort */
            } else {// we have vsn
                vsn = dataWord & UconnStreamConstants.VSN_MASK;
                showMessage(" vsn  " + vsn);
            }
        }
    }
}
