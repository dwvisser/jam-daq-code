package jam.sort;
import jam.data.Gate;
import jam.data.Histogram;
import jam.data.Monitor;
import jam.global.Sorter;
import jam.sort.stream.EventException;
import jam.sort.stream.EventOutputStream;

/**
 * Abstract class for sort routines which users extend with their own specific
 * code.  Defines histograms, gates, scalers, monitors, and parameters. The user writes a
 *  sort class which must have the following methods:
 * <dl>
 * <dt>initialize</dt><dd>called when the sort process is initialized</dd>
 * <dt>sort</dt><dd>called for each event</dd>
 * <dt>monitor</dt><dd>called each time the monitors are updated</dd>
 * </dl>
 *
 * @author Ken Swartz
 * @version 1.0
 * @since JDK1.1
 */
public abstract class SortRoutine implements Sorter  {

    /**
     * constant to define a 1d histogram type int
     */
    protected final int HIST_1D=Histogram.ONE_DIM_INT;

    /**
     * constant to define a 2d histogram type int
     */
    protected final int HIST_2D=Histogram.TWO_DIM_INT;

    /**
     * constant to define a 1d histogram type int
     */
    protected final int HIST_1D_INT=Histogram.ONE_DIM_INT;

    /**
     * constant to define a 2d histogram type int
     */
    protected final int HIST_2D_INT=Histogram.TWO_DIM_INT;

    /**
     * constant to define a 1d histogram type double
     */
    protected final int HIST_1D_DBL=Histogram.ONE_DIM_DOUBLE;

    /**
     * constant to define a 1d histogram type double
     */
    protected final int HIST_2D_DBL=Histogram.TWO_DIM_DOUBLE;

    /**
     * constant used to define a  1 d gate
     */
    protected final int GATE_1D=Gate.ONE_DIMENSION;      //gate types

    /**
     * constant used to define a 2 d gate
     */
    protected final int GATE_2D=Gate.TWO_DIMENSION;
    /**
     * constant used to define a scaler monitor
     * a monitor whose value is derived from a scaler
     */
    protected final int MONI_SCAL=Monitor.SCALER;
    /**
     * constant used to define a gate monitor
     * a monitor whose value is derived from a gate
     */
    protected final int MONI_GATE=Monitor.GATE;
    /**
     * constant used to define a sort monitor
     * a monitor whose value is derived in the sort routine
     */
    protected final int MONI_SORT=Monitor.SORT;
    /**
     * Size of an event to be used for offline sorting.
     * The event size is the maximum number of paramters in an event.
     */
    private int eventSize;

    /** Indicates that the parameter count has not been set by any means.
     */
    static final int INIT_NO_MODE=871;
    
    /** Indicates that the parameter count has been set explicitly.
     */
    public static final int SET_EXPLICITLY=876;
    
    /** Indicates the parameter count has been set implicitly by using CNAF commands.
     */
    public static final int SET_BY_CNAF=395;
    
    /** Indicates the parameter count has been set implicitly by specifying a VME map.
     */
    public static final int SET_BY_VME_MAP=249;
    
    int eventSizeMode = INIT_NO_MODE;

    /**
     * Size of buffer to be used by event streams.
     */
    public int BUFFER_SIZE;

    /* 	There are two online modes now.  One is hybrid VME/CAMAC acquisition,
    and the other is totally VME bus.  In the first case, the user must set a list
    of CAMAC commands.  In the second, a list of addresses and thresholds for
    the VME bus.
     */

    /**
     * class which is given list of cnafs
     * init(c,n,a,f);
     * event(c,n,a,f);
     */
    protected CamacCommands cnafCommands;

    /**
     * Object which contains the VME addressing instructions.
     */
    protected VME_Map vmeMap;

    /**
     * Output stream to send pre-processed events to.
     */
    protected EventOutputStream eventOutputStream=null;
    /** Set to true when we're writing out events (pre-sorting feature)?
     */
    protected boolean writeOn;

    /**
     * Creates a new sort routine object.
     */
    public SortRoutine(){
        writeOn=false;
        //offlineEventSizeOn=false;
        cnafCommands=new CamacCommands(this);
        vmeMap = new VME_Map(this);
    }

    /**
     * Returns the object containing commands for the CAMAC controller.
     *
     * @return commands to be used while taking data from CAMAC bins
     */
    public final CamacCommands getCamacCommands() {
        return cnafCommands;
    }

    /** Hands Jam the object representing VME acquisition specifications.
     * @return the object containing directives for ADC's and TDC's
     */
    public final VME_Map getVME_Map(){
        return vmeMap;
    }

    /** Explicitly sets the size of a event for offline sorting. Used in the absence
     * of CAMAC or VME specification of parameters.  The user has to know what
     * detector each parameter ID represents.
     * @param eventSize number of parameters per event
     * @throws SortException in case this has been called inappropriately
     */
    protected void setEventSize(int eventSize) throws SortException {
        if (eventSizeMode == INIT_NO_MODE ) {
            eventSizeMode=SET_EXPLICITLY;
        } else {
            throw new SortException(getClass().getName()+".setEventSize() called when"+
            " event size had already been set by CAMAC or VME methods.");
        }
        this.eventSize=eventSize;
    }

    /** Sets how the event size is determined.  Generally not called explicitly
     * by subclasses.
     * @param mode how the event size is determined
     * @throws SortException if called inappropriately
     * @see #SET_EXPLICITLY
     * @see #SET_BY_CNAF
     * @see #SET_BY_VME_MAP
     */
    protected void setEventSizeMode(int mode) throws SortException {
        if ((eventSizeMode != mode) && (eventSizeMode != INIT_NO_MODE)) {
            throw new SortException(getClass().getName()+".setEventSizeMode() called with: "+
            mode+ " when event size mode had already been set to: "+eventSizeMode);
        }
        if (mode == SET_BY_CNAF || mode == SET_BY_VME_MAP) {
            eventSizeMode = mode;
        } else {
            throw new SortException(getClass().getName()+".setEventSizeMode() called with"+
            " invalid mode: "+mode);
        }
    }

    /** 
     * Returns the mode by which the event size was set.
     * 
     * @return whether event size was set explicitly, by CAMAC specs, or by VME specs
     */
    public int getEventSizeMode(){
        return eventSizeMode;
    }

    /** Returns size of a event.
     * @return the size of the events
     * @throws SortException when there is no event size yet
     */
    public int getEventSize() throws SortException {
        if(eventSizeMode == 0) {
            throw new SortException(getClass().getName()+".getEventSize() called before event"
            +" size could be set.");
        } else if (eventSizeMode != SET_BY_CNAF && eventSizeMode!=SET_BY_VME_MAP &&
        eventSizeMode != SET_EXPLICITLY) {
            throw new SortException(getClass().getName()+".getEventSize(): Unknown eventSizeMode: "+eventSizeMode);
        } else if (eventSizeMode==SET_BY_CNAF) {
            return cnafCommands.getEventSize();
        } else if (eventSizeMode==SET_BY_VME_MAP) {
            return vmeMap.getEventSize();
        } else {//SET_EXPLICITLY
            return eventSize;
        }
    }

    /**
     * Returns size of a event
     *
     * @return the size of the events
     */
    /*public final int getEventSize(int streamNumber) {
    if(offlineEventSizeOn){
    return eventSize;
    } else {
    return cnafCommands.getEventSize(streamNumber);
    }
    }*/

    /** Set the event stream to use to write out events.
     * @param out stream to which presorted event output will go
     */
    public final void setEventOutputStream(EventOutputStream out){
        eventOutputStream=out;
    }

    /**
     * set the state to write out events
     * @param state true to write out events.
     */
    public final void setWriteEnabled(boolean state){
        writeOn=state;
    }

    /** Writes an event to the event output stream.  Used by the <code>sort()</code> method, if
     * so desired.
     * @param event event to write out
     * @throws SortException when an unnacceptable error condition occurs during sorting
     * @see #sort(int[])
     */
    public final void writeEvent(int [] event) throws SortException {
        if(writeOn){
            try{
                eventOutputStream.writeEvent(event);
            } catch (EventException e) {
                throw new SortException(e.toString());
            }
        }
    }

    /**
     * Required by the <code>Sorter</code> interface.
     *
     * @exception Exception thrown up to the calling thread if an unrecoverable error occurs
     * @see Sorter
     */
    public abstract void initialize() throws Exception;

    /** 
     * Required by the <code>Sorter</code> interface.
     * 
     * @see jam.global.Sorter#initialize()
     * @param dataWords array containing event data
     * @exception Exception thrown up to the calling thread if an unrecoverable error occurs
     */
    public abstract void sort(int [] dataWords) throws Exception ;

    /** 
     * Required by the <code>Sorter</code> interface.  As written always 
     * returns zero, and should be overwritten
     * whenever using monitors.
     * 
     * @param name  name of monitor value to calculate
     * @see jam.data.Monitor
     * @see jam.global.Sorter#monitor(String)
     * @return floating point value of the monitor
     */
    public double monitor(String name){
        return 0.0;
    }

    /**
     * Used to reset Parameter values.
     */
    public void reset() {// FIXME still used???? //XXX
    }
}
