package jam.sort;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.GlobalException;
import jam.global.GoodThread;
import jam.global.MessageHandler;
import jam.sort.stream.EventInputStatus;
import jam.sort.stream.EventInputStream;

/**
 * The daemon (background thread) which sorts data.
 * It takes an <code>EventInputStream</code>, and a <code>Sorter</code> class.
 * It reads events from the <code>EventInputStream</code> and
 * gives these to the <code>Sorter</code> method <code>sort(int [])</code>.
 * Last modified 18 December 1999 to use RingInputStream KBS.

 * @author Ken Swartz
 * @author Dale Visser
 * @version 1.1
 * @since JDK 1.1
 */
public class SortDaemon extends GoodThread {

    /**
     * Mode setting if sorting online.
     */
    final public static int ONLINE=0;

    /**
     * Mode setting if sorting offline.
     */
    final public static int OFFLINE=1;

    /**
     * Number of events to occur before updating counters.
     */
    private final static int COUNT_UPDATE=1000;

    /**
     * The size of buffers to read in.
     */
    final static int BUFFER_SIZE=8*1024;

    //handles to classes
    private final Controller controller;
    private final MessageHandler msgHandler;

    private SortRoutine sortRoutine;
	private EventInputStream eventInputStream;

    /**
     * mode offline or online
     */
    int mode;

    /**
     * Used in offline only, whether to send events to an output file
     */
    private boolean observed=false;
    private Broadcaster broadCaster;

    /**
     *  Used for online only, ringbuffer buffers input from network
     */
    private RingBuffer ringBuffer;

    /**
     * class to convert input array to stream
     */
    private RingInputStream ringInputStream;
    private byte [] buffer;

    //event information
    private int eventSize;
    private int []eventData;
    private int [] eventDataZero;
    private int eventCount;
    private int bufferCount;

    //private static final boolean sortLoop=true;

    /**
     * Creates a new <code>SortDaemon</code> process.
     *
     * @param con the sort control process
     * @param mh the console for writing out messages to the user
     */
    public SortDaemon(Controller con, MessageHandler mh){
        controller=con;
        msgHandler=mh;
    }

    /**
     * setup the sort deamon tell it the mode and stream
     *
     * @param mode <code>ONLINE</code> or <code>OFFLINE</code>
     * @param eventInputStream the source of event data
     * @param eventSize number of parameters per event
     */
    public void setup(int mode, EventInputStream eventInputStream, int eventSize){
        this.mode=mode;
        this.eventInputStream=eventInputStream;
        this.eventSize=eventSize;
        eventInputStream.setEventSize(eventSize);//set the event size for the stream
        eventCount=0;
        eventData = new int[eventSize];
        eventDataZero=new int[eventSize];
        for (int i=0; i<eventSize;i++){//explicitly zero eventDataZero
            eventDataZero[i]=0;
        }
        if(mode==ONLINE){
            ringInputStream= new RingInputStream();//input stream converts array to stream
            this.setPriority(3);//one lower than display
        } else {//two lower than display
            this.setPriority(2);
        }
        this.setDaemon(true);
    }

    /**
     * Load the sorting class.
     *
     * @param sortRoutine an object capable of sorting event data
     */
    public void setSortRoutine(SortRoutine sortRoutine){
        this.sortRoutine=sortRoutine;
    }

    /**
     * Sets the ring buffer to pull event data from.
     *
     * @param ringBuffer the source of event data
     */
    public void setRingBuffer(RingBuffer ringBuffer){
        this.ringBuffer=ringBuffer;
    }

    /**
     * set the state of the event output
     */
    public void setWriteEnabled(boolean state){
        sortRoutine.setWriteEnabled(state);
    }

    /**
     * Sets the event size.
     *
     * @param size the number of parameters per event
     */
    public void setEventSize(int size){
        eventSize=size;
        eventData = new int[eventSize];
        for(int i=0;i<size;i++){//explicitly zero eventDataZero
            eventDataZero[i]=0;
        }
    }

    /**
     * Returns the event size.
     *
     * @return the number of parameters per event
     */
    public int getEventSize(){
        return eventSize;
    }

    /**
     * Sets this object to observe broadcasted messages.
     *
     * @param broadCaster the message sender
     */
    public void setObserver(Broadcaster broadCaster){
        observed=true;
        this.broadCaster=broadCaster;
    }

    /**
     * Stop receiving broadcasts.
     */
    public void removeObserver(){
        observed=false;
    }

    /**
     * Reads events from the event stream.
     */
    public void run() {
        try{
            if (mode==ONLINE) {//which type of sort to do
                sortOnline();
            } else {
                sortOffline();
            }
        } catch (Exception e) {
            msgHandler.errorOutln("Sorter stopped Exception "+e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Performs the online sorting until an end-of-run state is reached in the event stream.
     *
     * @exception Exception thrown if an unrecoverable error occurs during sorting
     */
    public  void sortOnline() throws Exception {
    	EventInputStatus status;
    	
        while(true) {				//loop while acquisition on
            controller.atSortStart();  //does nothing for online
            //get a new buffer and make a input sream out of it
            buffer=ringBuffer.getBuffer();
            ringInputStream.setBuffer(buffer);
            eventInputStream.setInputStream(ringInputStream);
            System.arraycopy(eventDataZero, 0, eventData, 0, eventSize);//zero event array
            //read events until not a event
            while ((((status=eventInputStream.readEvent(eventData))
            == EventInputStatus.EVENT) || (status == EventInputStatus.SCALER_VALUE)
            || (status == EventInputStatus.IGNORE))) {
                if (status == EventInputStatus.EVENT) {
                    sortRoutine.sort(eventData);
                    eventCount++;
                    //zero event array and get ready for next event
                    System.arraycopy(eventDataZero, 0, eventData, 0, eventSize);
                } //else SCALER_VALUE, assume sort stream took care and move on
            }
            //we have reached the end of a buffer
            if (status==EventInputStatus.END_BUFFER) {
                bufferCount++;
                yield();
            } else if (status==EventInputStatus.END_RUN){
                bufferCount++;
                yield();
            } else if (status==EventInputStatus.UNKNOWN_WORD){
                //throw new SortException("Sorter stopped. Unknown word in event stream.");
                msgHandler.warningOutln("Unknown word in event stream.");
            } else if (status==EventInputStatus.END_FILE){
                msgHandler.warningOutln("Tried to read past end of event input stream.");
            } else {//we have unknown status
                /* unrecoverable error should not be here */
                throw new SortException("Sorter stopped due to unknown status: "+status);
            }
            yield();
        }//end infinite loop
    }

	private boolean offlineSortingCanceled=false;

	/**
	 * Call to gracefully interrupt and end the current offline sort.
	 *
	 */
	public void cancelOfflineSorting(){
		offlineSortingCanceled=true;
	}

    /**
     * Performs the offline sorting until an end-of-run state is reached in the event stream.
     *
     * @exception Exception thrown if an unrecoverable error occurs during sorting
     */
    public  void sortOffline() throws Exception {
    	EventInputStatus status; 
    	
        boolean atBuffer=false;    //are we at a buffer word
        controller.atSortStart(); //immediately suspends the thread at next statement
        while(checkState()){//infinite loop
            controller.atSortStart();//suspends this thread
            offlineSortingCanceled=false;//after we come out of suspend
            while(!offlineSortingCanceled && controller.isSortNext()) {//loop for each new sort file
                boolean endSort=false;
                while(!offlineSortingCanceled && !endSort) {//buffer loop
                    System.arraycopy(eventDataZero, 0, eventData, 0, eventSize);//zero event array
                    while ((((status=eventInputStream.readEvent(eventData))
                    == EventInputStatus.EVENT) || (status == EventInputStatus.SCALER_VALUE)|| 
                    (status == EventInputStatus.IGNORE))) {
                        if (!offlineSortingCanceled && status == EventInputStatus.EVENT) {
                            sortRoutine.sort(eventData);
                            eventCount++;
                            System.arraycopy(eventDataZero, 0, eventData, 0, eventSize);//zero event array and get ready for next event
                            atBuffer=false;
                            if (eventCount%COUNT_UPDATE==0){      //exactly divisible by COUNT_UPDATE
                                updateCounters();
                                yield();
                            }
                        } 
                        /* else SCALER_VALUE, assume sort stream 
                         * took care and move on or IGNORE which means
                         * something ignorable in the event stream */
                    }
                    /* we get to this point if status was not EVENT */
                    if (status==EventInputStatus.END_BUFFER) {//if we dont have event
                        if (!atBuffer) {
                            atBuffer=true;
                            bufferCount++;
                        }
                        endSort = false;
                    } else if (status==EventInputStatus.END_RUN) {
                        updateCounters();
                        endSort=true;    //tell control we are done
                    } else if (status==EventInputStatus.END_FILE) {
                        msgHandler.messageOutln("End of file reached");
                        updateCounters();
                        endSort=true;    //tell control we are done
                    } else if (status==EventInputStatus.UNKNOWN_WORD) {
                        msgHandler.warningOutln(getClass().getName()+
                        ".sortOffline(): Unknown word in event stream.");
					} else {
                        endSort=true;
                        //unrecoverable error
                        throw new SortException("Sorter, Unknown eventInput status = "+status);
                    }
                    yield();
                }//end buffer loop
            }//end isSortNext loop
            controller.atSortEnd();
        }//end infinite loop
    }

    /**
     * Are we caught up in the ring buffer.
     * That is there are no unsorted buffers in the
     * ring buffer.
     */
    public boolean caughtUp(){
        return ringBuffer.empty();
    }
    /**
     * update the counters display
     */
    private void updateCounters () throws GlobalException {
        if (observed) broadCaster.broadcast(BroadcastEvent.COUNTERS_UPDATE);
    }

    /**
     * Returns the number of events processed.
     *
     * @return the number of events processed
     */
    public int getEventCount(){
        return eventCount;
    }

    /**
     * Sets the number of events processed.
     *
     * @param count the number of events processed
     */
    public void setEventCount(int count){
        eventCount=count;
    }

    /**
     * Returns the number of buffers processed.
     *
     * @return the number of buffers processed
     */
    public int getBufferCount(){
        return bufferCount;
    }

    /**
     * Sets the number of buffers processed.
     *
     * @param count the number of buffers processed
     */
    public void setBufferCount(int count){
        bufferCount=count;
    }


}
