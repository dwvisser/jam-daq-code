/*
 * BufferStatus.java
 *
 * Created on November 19, 2001, 2:51 PM
 */

package jam.sort.stream;

/**
 * Container for the state of the the FIFO buffer in YaleCAEN_InputStream.
 *
 * @author  Dale Visser
 * @version 1.0
 */
public class BufferStatus {
    
    private int code;
    
    /**
     * State for when buffer is still filling and no output is available yet.
     */
    static public final BufferStatus FIFO_FILLING = new BufferStatus(0);
    
    /**
     * In this state every new read from the stream requires that the oldest 
     * event in the the "FIFO" buffer be pulled to be returned so as to make room
     * for a new event counter.
     */
    static public final BufferStatus FIFO_FULL = new BufferStatus(1);
    
    
    /**
     * This is the state when the EventStream has characters in it indicating that
     * acquisition has been stopped or ended.  In this situation, all data has been 
     * read out from the ADC's and been sent to Jam.  So the stream needs to empty out
     * it's remaining contents to the sort routine.
     */
    static public final BufferStatus FIFO_FLUSH = new BufferStatus(2);
    static public final BufferStatus FIFO_ENDRUN_FLUSH = new BufferStatus(3);
    
    static public final BufferStatus SCALER = new BufferStatus(4);
    static public final BufferStatus PADDING = new BufferStatus(5);

    /** Creates new BufferStatus */
    private BufferStatus(int code) {
        this.code=code;
    }
    
    public String toString(){
        switch (code){
            case 0: return "FIFO_FILLING";
            case 1: return "FIFO_FULL";
            case 2: return "FIFO_FLUSH";
            case 3: return "FIFO_ENDRUN_FLUSH";
            case 4: return "SCALER";
            case 5: return "PADDING";
            default: return "Unknown BufferStatus type.";
        }
    }

}
