package jam.sort.stream;
import jam.data.Scaler;
import jam.global.MessageHandler;

import java.io.EOFException;
import java.io.IOException;
import java.util.Hashtable;

/**
 * This class sorts the native data format of the CAEN V7x5 ADC's and TDC's.
 * Parameters  are mapped from the slot number and unit channel # to indices
 * in the int array that gets passed to sort routines.
 *
 * @version	18 Nov 2001
 * @author 	Dale Visser
 * @see         EventInputStream
 * @since       JDK1.1
 */
public class YaleCAEN_InputStream extends L002HeaderReader implements L002Parameters {
    
    static final int NUM_V7X5_UNITS=19;//20 slots in a VME crate - 1 controller slot
    static final int NUM_CHANNELS = NUM_V7X5_UNITS*32;
    static final int NUM_EVENTS_TO_STORE = 100;
    private BufferStatus internal_status = BufferStatus.FIFO_FILLING;
    
    static final int STOP_PAD = 0x01DDDDDD;
    static final int END_PAD = 0x01EEEEEE;
    static final int BUFFER_PAD = 0x01FFFFFF;
    static final int SCALER_BLOCK = 0x01CCCCCC;
    static final int END_OF_BUFFER = 0x01bbbbbb;//at end of normal buffers
    int nscalerblock=0;	// for counting number of scaler blocks in the file
    
    private int[][] fifo=new int[NUM_EVENTS_TO_STORE][NUM_CHANNELS];
    private final int [] zeros = new int[NUM_CHANNELS];//automatically initialized to all zeros
    private int[] eventNumbers = new int[NUM_EVENTS_TO_STORE];
    
    private int posPut;//array index where next event counter is to be written to FIFO
    private int posGet;//array index where next (i.e. oldest) event counter/event should be retrieved from
    private int lastIncremented;
    private static final int PUT=0;
    private static final int GET=1;
    
    
    /**
     * Hashtable keys are the event numbers, objects are the array indices.
     */
    private Hashtable eventNumberTable=new Hashtable(NUM_EVENTS_TO_STORE);
    
    /**
     * Make sure to issue a setConsole() after using this constructor.
     * It is here to satisfy the requirements of Class.newInstance()
     */
    public YaleCAEN_InputStream(){
        super();
        posPut=0;
        posGet=0;
        lastIncremented=GET;//initially empty requires last incremented to be GET
    }
    
    private void incrementPut() {
        posPut++;
        if (posPut==eventNumbers.length) posPut=0;
        lastIncremented=PUT;
    }
    
    private void incrementGet() {
        posGet++;
        if (posGet==eventNumbers.length) posGet=0;
        lastIncremented=GET;
    }
    
    /**
     * Default constructor.
     */
    public YaleCAEN_InputStream(MessageHandler console) {
        super(console);
    }
    
    private boolean eventInFIFO(int eventNumber){
        return eventNumberTable.containsKey(new Integer(eventNumber));
    }
    
    private int getEventIndex(int eventNumber){
        return ((Integer)(eventNumberTable.get(new Integer(eventNumber)))).intValue();
    }
    
    private void addEventIndex(int eventNumber){
        eventNumbers[posPut] = eventNumber;
        System.arraycopy(zeros,0,fifo[posPut],0,zeros.length);
        eventNumberTable.put(new Integer(eventNumber), new Integer(posPut));
        incrementPut();
        if (fifoFull()) internal_status=BufferStatus.FIFO_FULL;
    }
    
    private boolean fifoFull() {
        return posPut==posGet && lastIncremented==PUT;
    }
    
    private boolean fifoEmpty() {
        return posPut==posGet && lastIncremented==GET;
    }
    
    private void getFirstEvent(int [] data){
        int eventNumber = eventNumbers[posGet];
        eventNumberTable.remove(new Integer(eventNumber));
        int [] rval = fifo[posGet];
        System.arraycopy(rval,0,data,0,data.length);
        incrementGet();
        if (!inFlushState()) internal_status = BufferStatus.FIFO_FILLING;
    }
    
    private boolean inFlushState(){
        return internal_status==BufferStatus.FIFO_FLUSH ||
        internal_status==BufferStatus.FIFO_ENDRUN_FLUSH;
    }
        
    /**
     * Creates the input stream given an event size.
     *
     * @param eventSize number of parameters per event.
     */
    public YaleCAEN_InputStream(MessageHandler console, int eventSize) {
        super(console, eventSize);
    }
    
    private int [] tempParams=new int[32];
    private int [] tempData = new int[32];
    /**
     * Reads an event from the input stream
     * Expects the stream position to be the beginning of an event.
     * It is up to the user to ensure this.
     *
     * @exception   EventException    thrown for errors in the event stream
     */
    public synchronized EventInputStatus readEvent(int[] data) throws  EventException {
        EventInputStatus rval=EventInputStatus.EVENT;
        int parameter=0;
        int endblock=0;
        final int [] tval =new int[32];	//temporary array for scaler values, up to a max of 32
        try {
            /* internal_status may also be in a "flush" mode in which case
             * we skip this read loop and go straight to flushing out another event */ 
            while(internal_status==BufferStatus.FIFO_FILLING){
            /* this loop may finish if status changes to "fifo full" mode 
             * when an event index gets added below */
                int header = dataInput.readInt();
                if (isHeader(header)) {
                    /* ADC's & TDC's in slots 2-31 */
                    int slot = (header >>> 27) & 0x1f;
                    boolean keepGoing=true;
                    int paramIndex=0;
                    int numParameters=0;
                    while (keepGoing){                        
                        parameter = dataInput.readInt();
                        if (isParameter(parameter)) {
                            numParameters++;
                            int channel = (parameter >>> 16) & 0x3f;
                            tempParams[paramIndex] = 32*(slot-2)+channel;
                            tempData[paramIndex] = parameter & 0xfff;
                            paramIndex++;
                        } else if (isEndBlock(parameter)){
                            endblock = parameter;
                            keepGoing = false;
                        } else {
                            throw new EventException(getClass().getName()+
                            ".readEvent(): didn't get a Parameter or End-of-Block when expected, int datum = 0x"+
                            Integer.toHexString(parameter));
                        }
                    }
                    /* If we really have end-of-block like we should, stick event
                     * data in the appropriate space in our FIFO. */
                    if (isEndBlock(endblock)){
                        int eventNumber = endblock & 0xffffff;
                        if (!eventInFIFO(eventNumber)){//Event # not in FIFO, so need to add it.
                            addEventIndex(eventNumber);//can change internal state to FIFO_FULL
                        }
                        int arrayIndex = getEventIndex(eventNumber);
                        /* copy data in, item by item */
                        for (int i=0; i<numParameters; i++) {
                            fifo[arrayIndex][tempParams[i]]=tempData[i];
                        }
                    } else {
                        throw new EventException(getClass().getName()+
                        ".readEvent(): didn't get a end of block when expected, int datum = 0x"+
                        Integer.toHexString(endblock));
                    }
                } else if (header==SCALER_BLOCK) {//read and ignore scaler values
                    int numScalers = dataInput.readInt();
                    nscalerblock++;
                    for (int i=0; i<numScalers; i++) {
                    	tval[i]=dataInput.readInt();
                    }
                	Scaler.update(tval);
                    rval=EventInputStatus.SCALER_VALUE;
                    internal_status=BufferStatus.SCALER;
                } else if (header==END_OF_BUFFER){//return end of buffer to SortDaemon
                    /* no need to flush here */
                    rval=EventInputStatus.END_BUFFER;
                    internal_status=BufferStatus.PADDING;
                } else if (header==BUFFER_PAD) {
                    rval=EventInputStatus.IGNORE;
                    internal_status=BufferStatus.PADDING;
                } else if (header==STOP_PAD) {
                    internal_status = BufferStatus.FIFO_FLUSH;
                } else if (header==END_PAD) {
                    internal_status = BufferStatus.FIFO_ENDRUN_FLUSH;
                    showMessage("Scaler blocks in file ="+nscalerblock);
                    nscalerblock=0;
                } else {
                    /* using IGNORE since UNKNOWN WORD causes annoying beeps */
                    rval = EventInputStatus.IGNORE;
                    internal_status=BufferStatus.PADDING;
                }
            }// end of while loop
            /* We've dropped out of the while loop, which means either that 
             * the internal status is not FIFO_FILLING, or that
             * eventReady is set to true (i.e. encountered buffer pad or scaler)
             * The first case is handled here, if it's true. */
            if (inFlushState()) {//in one of the 2 flush states
                if (!fifoEmpty()) {//FIFO not empty
                    getFirstEvent(data);
                    rval=EventInputStatus.EVENT;
                } else {//all events flushed, make ready for next event
                    if (internal_status==BufferStatus.FIFO_FLUSH){
                        rval = EventInputStatus.END_BUFFER;
                    } else {//internal status must be "endrun flush"
                        rval = EventInputStatus.END_RUN;
                    }
                    internal_status=BufferStatus.FIFO_FILLING;
                }
            /* The other possibility is that the FIFO is full and we need to 
             * output an event. */
            } else if (internal_status==BufferStatus.FIFO_FULL) {
                getFirstEvent(data);//routine retrieves data and updates tracking variables
                rval = EventInputStatus.EVENT;
            } else {//internal status=SCALER or PADDING
                /* set to FIFO_FILLING so next call will enter loop */
                internal_status=BufferStatus.FIFO_FILLING;
            }
        } catch (EOFException eofe) {// we got to the end of a file or stream
            rval=EventInputStatus.END_FILE;
            console.warningOutln(getClass().getName()+
            ".readEvent(): End of File reached...file may be corrupted, or run not ended properly.");
        } catch (IOException ioe) {// we got to the end of a file or stream
            rval=EventInputStatus.UNKNOWN_WORD;
            console.warningOutln(getClass().getName()+
            ".readEvent(): Problem reading integer from stream.");
        } catch (EventException e){
            rval=EventInputStatus.UNKNOWN_WORD;
            throw new EventException(getClass().getName()+".readEvent() parameter = "+parameter+" Exception: "+e.toString());
        }
        return rval ;
    }
    
	static private final int TYPE_MASK   = 0x7000000;
	static private final int PARAM_COMPARE=0x0000000;
    /**
     * Checks whether the word type is for an event data word 
     */
    private boolean isParameter(int data){
        return (data & TYPE_MASK) == PARAM_COMPARE;
    }
    
	static private final int HEADER_COMPARE = 0x2000000;
    /**
     * Checks whether the word type is for an event header.
     */
    private boolean isHeader(int data){
        return (data & TYPE_MASK) == HEADER_COMPARE;
    }
    
	static private final int END_COMPARE = 0x4000000;
    /**
     * Checks whether the word type is for an event end-of-block 
     */
    private boolean isEndBlock(int data){
        return (data & TYPE_MASK) == END_COMPARE;
    }
        
    private static final short ENDRUN = (short)(END_PAD & 0xffff);
    /**
     * Check for end-of-run word.
     */
    public boolean isEndRun(short dataWord){
		return (ENDRUN==dataWord);
    }
}