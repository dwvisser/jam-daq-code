package jam.sort;

import jam.data.Group;
import jam.data.HistInt1D;
import jam.data.HistInt2D;
import jam.data.Histogram;
import jam.data.Scaler;
import jam.global.Beginner;
import jam.global.Ender;
import jam.global.Sorter;
import jam.sort.stream.EventException;
import jam.sort.stream.EventOutputStream;

/**
 * <p>
 * Abstract class for sort routines which users extend with their own specific
 * code. Defines histograms, gates, scalers, monitors, and parameters. The user
 * writes a sort class which must have the following methods:
 * </p>
 * <dl>
 * <dt>initialize</dt>
 * <dd>called when the sort process is initialized</dd>
 * <dt>sort</dt>
 * <dd>called for each event</dd>
 * <dt>monitor</dt>
 * <dd>called each time the monitors are updated</dd>
 * </dl>
 * <p>
 * There are two online modes now. One is hybrid VME/CAMAC acquisition,and the
 * other is totally VME bus. In the first case, the user must set a list of
 * CAMAC commands. In the second, a list of addresses and thresholds for the VME
 * bus.
 * </p>
 * 
 * @author Ken Swartz
 * @author Dale Visser
 * @version 1.4
 * @since 1.0
 * @see VME_Map
 * @see CamacCommands
 * @see jam.data.Histogram
 * @see jam.data.Monitor
 * @see jam.data.Gate
 */
public abstract class SortRoutine implements Sorter, Beginner, Ender {

	/**
     * Encapsulates the different ways the event size can be specified.
     * 
     * @author <a href="mailto:dale@visser.name">Dale W Visser </a>
     */
    public static class EventSizeMode {
        private static final int SET_BY_CNAF = 0;

        private static final int SET_VME_MAP = 1;

        private static final int SET_EXPLICIT = 2;

        private static final int INIT_NO_MODE = 3;
        
        private static final boolean [] IS_SET={true,true,true,false};

        final int mode;

        private EventSizeMode(int value) {
            mode = value;
        }
        
        /**
         * Returns whether this event size mode represents a properly set event
         * size.
         * 
         * @return whether this event size mode represents a properly set event
         *         size
         */
        public boolean isSet() {
            return IS_SET[mode];
        }

        /**
         * Indicates the parameter count has been set implicitly by using CNAF
         * commands.
         */
        public static final EventSizeMode CNAF = new EventSizeMode(SET_BY_CNAF);

        /**
         * Indicates the parameter count has been set implicitly by specifying a
         * VME map.
         */
        public static final EventSizeMode VME_MAP = new EventSizeMode(
                SET_VME_MAP);

        /**
         * Indicates that the parameter count has been set explicitly.
         */
        public static final EventSizeMode EXPLICIT = new EventSizeMode(
                SET_EXPLICIT);

        /**
         * Indicates that the parameter count hasn't been set by any means.
         */
        public static final EventSizeMode INIT = new EventSizeMode(INIT_NO_MODE);
    }

	/**
	 * constant to define a 1d histogram type int
	 */
	protected final static Histogram.Type HIST_1D = Histogram.Type.ONE_DIM_INT;

	/**
	 * constant to define a 2d histogram type int
	 */
	protected final static Histogram.Type HIST_2D = Histogram.Type.TWO_DIM_INT;

	/**
	 * constant to define a 1d histogram type int
	 */
	protected final static Histogram.Type HIST_1D_INT = Histogram.Type.ONE_DIM_INT;

	/**
	 * constant to define a 2d histogram type int
	 */
	protected final static Histogram.Type HIST_2D_INT = Histogram.Type.TWO_DIM_INT;

	/**
	 * constant to define a 1d histogram type double
	 */
	protected final static Histogram.Type HIST_1D_DBL = Histogram.Type.ONE_D_DOUBLE;

	/**
	 * constant to define a 1d histogram type double
	 */
	protected final static Histogram.Type HIST_2D_DBL = Histogram.Type.TWO_D_DOUBLE;


	/**
	 * Size of buffer to be used by event streams.
	 */
	private int bufferSize;

	/**
	 * Set to true when we're writing out events. (For presorting?)
	 */
	protected boolean writeOn;

	/**
	 * class which is given list of cnafs init(c,n,a,f); event(c,n,a,f);
	 */
	protected CamacCommands cnafCommands;

	/**
	 * Object which contains the VME addressing instructions.
	 */
	protected VME_Map vmeMap;

	/**
	 * Output stream to send pre-processed events to.
	 */
	private EventOutputStream eventOutput = null;

	private EventSizeMode evtSizeMode = EventSizeMode.INIT;

	/**
	 * Size of an event to be used for offline sorting. The event size is the
	 * maximum number of paramters in an event.
	 */
	private int eventSize;

	private final String classname = getClass().getName();

	private final static String COLON = ": ";

	private final static String ILLEGAL_MODE = "Illegal value for event size mode: ";
	
	/**
	 * User may optionally use this to read in gain calibrations.
	 */
	protected final GainCalibration gains;

	/**
	 * Creates a new sort routine object.
	 */
	public SortRoutine() {
		writeOn = false;
		cnafCommands = new CamacCommands(this);
		vmeMap = new VME_Map(this);
		gains=new GainCalibration(this);
	}

	/**
	 * Returns the object containing commands for the CAMAC controller.
	 * 
	 * @return commands to be used while taking data from CAMAC bins
	 */
	public final CamacCommands getCamacCommands() {
		return cnafCommands;
	}

	/**
	 * Hands Jam the object representing VME acquisition specifications.
	 * 
	 * @return the object containing directives for ADC's and TDC's
	 */
	public final VME_Map getVMEmap() {
		return vmeMap;
	}

	/**
	 * Explicitly sets the size of a event for offline sorting. Used in the
	 * absence of CAMAC or VME specification of parameters. The user has to know
	 * what detector each parameter ID represents.
	 * 
	 * @param size
	 *            number of parameters per event
	 * @throws SortException
	 *             in case this has been called inappropriately
	 */
	protected void setEventSize(int size) throws SortException {
		setEventSizeMode(EventSizeMode.EXPLICIT);
		synchronized (this) {
			eventSize = size;
		}
	}

	/**
     * Sets how the event size is determined. Generally not called explicitly by
     * subclasses.
     * 
     * @param mode
     *            how the event size is determined
     * @throws SortException
     *             if called inappropriately
     */
    void setEventSizeMode(EventSizeMode mode) throws SortException {
        final StringBuffer mess = new StringBuffer(classname).append(COLON);
        if ((evtSizeMode != mode) && (evtSizeMode != EventSizeMode.INIT)) {
            final String part1 = "Illegal attempt to set event size a second time. ";
            final String part2 = "Already set to ";
            final String part3 = ", and attempted to set to ";
            throw new SortException(mess.append(part1).append(part2).append(
                    evtSizeMode).append(part3).append(mode).append('.')
                    .toString());
        }
        if (mode == EventSizeMode.CNAF || mode == EventSizeMode.VME_MAP
                || mode == EventSizeMode.EXPLICIT) {
            synchronized (this) {
                evtSizeMode = mode;
            }
        } else {
            throw new SortException(mess.append(ILLEGAL_MODE).append(mode)
                    .toString());
        }
    }

	/**
	 * Returns the mode by which the event size was set.
	 * 
	 * @return whether event size was set explicitly, by CAMAC specs, or by VME
	 *         specs
	 */
	public EventSizeMode getEventSizeMode() {
		return evtSizeMode;
	}

	/**
	 * Returns size of a event.
	 * 
	 * @return the size of the events
	 * @throws SortException
	 *             when there is no event size yet
	 */
	public int getEventSize() throws SortException {
		final int rval;
		final StringBuffer mess = new StringBuffer(classname).append(COLON);
		if (evtSizeMode.isSet()){
		    if (evtSizeMode == EventSizeMode.CNAF) {
				rval = cnafCommands.getEventSize();
			} else if (evtSizeMode == EventSizeMode.VME_MAP) {
				rval = vmeMap.getEventSize();
			} else {//==EXPLICIT
				rval = eventSize;
			}		    
		} else {
			final String sizeUnknown = "Event Size Unkown";
			throw new SortException(mess.append(sizeUnknown).toString());
		}
		return rval;
	}

	/**
	 * Set the event stream to use to write out events.
	 * 
	 * @param out
	 *            stream to which presorted event output will go
	 */
	public final void setEventOutputStream(EventOutputStream out) {
		synchronized (this) {
			eventOutput = out;
		}
	}

	/**
	 * set the state to write out events
	 * 
	 * @param state
	 *            true to write out events.
	 */
	public final void setWriteEnabled(boolean state) {
		synchronized (this) {
			writeOn = state;
		}
	}

	/**
	 * Writes an event to the event output stream. Used by the
	 * <code>sort()</code> method, if so desired.
	 * 
	 * @param event
	 *            event to write out
	 * @throws SortException
	 *             when an unnacceptable error condition occurs during sorting
	 * @see #sort(int[])
	 */
	public final void writeEvent(int[] event) throws SortException {
		if (writeOn) {
			try {
				eventOutput.writeEvent(event);
			} catch (EventException e) {
				throw new SortException(e.toString());
			}
		}
	}

	/**
	 * @see jam.global.Sorter#initialize()
	 */
	public abstract void initialize() throws Exception;

	/**
	 * @see jam.global.Sorter#sort(int[])
	 */
	public abstract void sort(int[] dataWords) throws Exception;

	/**
	 * Required by the <code>Sorter</code> interface. As written always
	 * returns zero, and should be overwritten whenever using monitors.
	 * 
	 * @param name
	 *            name of monitor value to calculate
	 * @see jam.data.Monitor
	 * @see jam.global.Sorter#monitor(String)
	 * @return floating point value of the monitor
	 */
	public double monitor(String name) {
		return 0.0;
	}
	
	/**
	 * @see Beginner#begin()
	 */
	public void begin(){
		/* default begin() does nothing */
	}
	
	/**
	 * @see Ender#end()
	 */
	public void end(){
		/* default end() does nothing */
	}
	
	/**
	 * Sets the buffer size in bytes to use.
	 * 
	 * @param size in bytes of event buffers
	 */
	protected void setBufferSize(int size){
		bufferSize=size;
	}
	
	/**
	 * Gets the buffer size in bytes.
	 * 
	 * @return size in bytes of event buffers
	 */
	public int getBufferSize(){
		return bufferSize;
	}
	
	/**
	 * Creates a one-dimensional, integer-valued, histogram.
	 * 
	 * @param numCh number of bins
	 * @param name unique name
	 * @param title verbose title
	 * @param labelX x-axis label
	 * @param labelY y-axis label
	 * @return a newly allocated histogram
	 */
	public static HistInt1D createHist1D(int numCh, String name, String title, String labelX,
			String labelY){
		final Group sortGroup = Group.getSortGroup();
		return (HistInt1D)Histogram.createHistogram(sortGroup, new int[numCh], name,title,labelX,labelY);
	}
	
	/**
	 * Creates a one-dimensional, integer-valued, histogram.
	 * 
	 * @param numCh number of bins
	 * @param name unique name
	 * @param title verbose title
	 * @return a newly allocated histogram
	 */	
	public static HistInt1D createHist1D(int numCh, String name, String title){
		final Group sortGroup = Group.getSortGroup();
		return (HistInt1D)Histogram.createHistogram(sortGroup, new int[numCh],name,title);
	}

	/**
	 * Creates a one-dimensional, integer-valued, histogram.
	 * 
	 * @param numCh number of bins
	 * @param name unique name
	 * @return a newly allocated histogram
	 */
	public static HistInt1D createHist1D(int numCh, String name){
		final Group sortGroup = Group.getSortGroup();
		return (HistInt1D)Histogram.createHistogram(sortGroup, new int[numCh],name);
	}
	
	/**
	 * Creates a two-dimensional, integer-valued, histogram.
	 * 
	 * @param chX number of bins along the horizontal axis
	 * @param chY number of bins along the vertical axis
	 * @param name unique name
	 * @param title verbose title
	 * @param labelX x-axis label
	 * @param labelY y-axis label
	 * @return a newly allocated histogram
	 */
	public static HistInt2D createHist2D(int chX, int chY, String name, String title, String labelX,
			String labelY){
		final Group sortGroup = Group.getSortGroup();
		return (HistInt2D)Histogram.createHistogram(sortGroup, new int[chX][chY],name,title,labelX,labelY);
	}
	
	/**
	 * Creates a two-dimensional, integer-valued, histogram.
	 * 
	 * @param chX number of bins along the horizontal axis
	 * @param chY number of bins along the vertical axis
	 * @param name unique name
	 * @param title verbose title
	 * @return a newly allocated histogram
	 */
	public static HistInt2D createHist2D(int chX, int chY, String name, String title){
		final Group sortGroup = Group.getSortGroup();
		return (HistInt2D)Histogram.createHistogram(sortGroup, new int[chX][chY],name,title);
	}
	
	/**
	 * Creates a two-dimensional, integer-valued, histogram.
	 * 
	 * @param chX number of bins along the horizontal axis
	 * @param chY number of bins along the vertical axis
	 * @param name unique name
	 * @return a newly allocated histogram
	 */
	public static HistInt2D createHist2D(int chX, int chY, String name){
		final Group sortGroup = Group.getSortGroup();
		return (HistInt2D)Histogram.createHistogram(sortGroup, new int[chX][chY],name);
	}
	
	/**
	 * Creates a two-dimensional, integer-valued, histogram.
	 * 
	 * @param chans number of bins along the horizontal and vertical axes
	 * @param name unique name
	 * @param title verbose title
	 * @param labelX x-axis label
	 * @param labelY y-axis label
	 * @return a newly allocated histogram
	 */
	public static HistInt2D createHist2D(int chans, String name, String title, String labelX,
			String labelY){
		final Group sortGroup = Group.getSortGroup();
		return (HistInt2D)Histogram.createHistogram(sortGroup, new int[chans][chans],name,title,labelX,labelY);
	}
	
	/**
	 * Creates a two-dimensional, integer-valued, histogram.
	 * 
	 * @param chans number of bins along the horizontal and vertical axes
	 * @param name unique name
	 * @param title verbose title
	 * @return a newly allocated histogram
	 */
	public static HistInt2D createHist2D(int chans, String name, String title){
		final Group sortGroup = Group.getSortGroup();		
		return (HistInt2D)Histogram.createHistogram(sortGroup, new int[chans][chans],name,title);
	}
	
	/**
	 * Creates a two-dimensional, integer-valued, histogram.
	 * 
	 * @param chans number of bins along the horizontal and vertical axes
	 * @param name unique name
	 * @return a newly allocated histogram
	 */
	public static HistInt2D createHist2D(int chans, String name){
		final Group sortGroup = Group.getSortGroup();		
		return (HistInt2D)Histogram.createHistogram(sortGroup, new int[chans][chans],name);
	}
	
	/**
	 * Creates a two-dimensional, integer-valued, histogram.
	 * 
	 * @param name unique name
	 * @param number unique number
	 * @return a newly allocated histogram
	 */
	public static Scaler createScaler(String name, int number){
		final Group sortGroup = Group.getSortGroup();		
		return new Scaler(sortGroup, name, number);
	}
}