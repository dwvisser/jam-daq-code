package jam.sort.stream;

import jam.global.MessageHandler;

import java.io.EOFException;

/**
 * This class knows how to handle Oak Ridge tape format.
 * 
 * @version 0.5 April 98
 * @author Dale Visser, Ken Swartz
 * @see AbstractEventInputStream
 * @since JDK1.1
 */
public class YaleInputStream extends AbstractL002HeaderReader implements
        L002Parameters {

    private transient EventInputStatus status;

    private transient int parameter;

    // make sure to issue a setConsole() after using this constructor
    // It is here to satisfy the requirements of Class.newInstance()
    /**
     * @see AbstractEventInputStream#AbstractEventInputStream()
     */
    public YaleInputStream() {
        super();
    }

    /**
     * @see AbstractEventInputStream#AbstractEventInputStream(MessageHandler)
     */
    public YaleInputStream(MessageHandler console) {
        super(console);
    }

    /**
     * @see AbstractEventInputStream#AbstractEventInputStream(MessageHandler,
     *      int)
     */
    public YaleInputStream(MessageHandler console, int eventSize) {
        super(console, eventSize);
    }

    /**
     * Reads an event from the input stream Expects the stream position to be
     * the beginning of an event. It is up to the user to ensure this.
     * 
     * @exception EventException
     *                thrown for errors in the event stream
     */
    public synchronized EventInputStatus readEvent(int[] input)
            throws EventException {
        try {
            while (isParameter(dataInput.readShort())) {// could be event or
                                                        // scaler parameter
                if (status == EventInputStatus.PARTIAL_EVENT) {
                    final int tempval = dataInput.readShort();
                    if (parameter < eventSize) {
                        /* only try to store if we won't go outside the array */
                        input[parameter] = tempval; // read event word
                    }
                } else if (status == EventInputStatus.SCALER_VALUE) {
                    dataInput.readInt();// throw away scaler value
                }
            }
        } catch (EOFException eofe) {// we got to the end of a file or stream
            status = EventInputStatus.END_FILE;
            console
                    .warningOutln(getClass().getName()
                            + ".readEvent(): End of File reached...file may be corrupted, or run not ended properly.");
        } catch (Exception e) {
            status = EventInputStatus.UNKNOWN_WORD;
            throw new EventException(getClass().getName()
                    + ".readEvent() parameter = " + parameter + " Exception: "
                    + e.toString());
        }
        return status;
    }

    /*
     * non-javadoc: Read an event parameter.
     */
    private boolean isParameter(short paramWord) {
        boolean rval;
        if (paramWord == EVENT_END_MARKER) {
            rval = false;
            status = EventInputStatus.EVENT;
        } else if (paramWord == BUFFER_END_MARKER) {
            rval = false;
            status = EventInputStatus.END_BUFFER;
        } else if (paramWord == RUN_END_MARKER) {
            rval = false;
            status = EventInputStatus.END_RUN;
        } else if ((paramWord & EVENT_PARAMETER) == 0) {
            parameter = paramWord;
            rval = false;
            status = EventInputStatus.UNKNOWN_WORD;
        } else {
            /* extract parameter id, too */
            final int paramNumber = paramWord & EVENT_MASK;
            if (paramNumber < 2048) {
                parameter = paramNumber;// parameter index used in array
                rval = true;
                status = EventInputStatus.PARTIAL_EVENT;
            } else {// 2048-4095 assumed
                rval = true;
                status = EventInputStatus.SCALER_VALUE;
            }
        }
        return rval;
    }

    /**
     * Check for end of run word
     */
    public synchronized boolean isEndRun(short dataWord) {
        return (dataWord == RUN_END_MARKER);
    }
}