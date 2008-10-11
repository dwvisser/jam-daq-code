package jam.sort;

import jam.data.DataParameter;
import jam.data.Group;
import jam.data.HistInt1D;
import jam.data.HistInt2D;
import jam.data.Histogram;
import jam.data.Scaler;
import jam.data.Sorter;
import jam.sort.stream.EventWriter;

import java.io.IOException;

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
public abstract class SortRoutine implements Sorter, Beginner, Ender,
		EventSizeModeClient {

	/**
	 * constant to define a 1d histogram type int
	 */
	protected final static Histogram.Type HIST_1D = Histogram.Type.ONE_DIM_INT;

	/**
	 * constant to define a 1d histogram type double
	 */
	protected final static Histogram.Type HIST_1D_DBL = Histogram.Type.ONE_D_DOUBLE;

	/**
	 * constant to define a 1d histogram type int
	 */
	protected final static Histogram.Type HIST_1D_INT = Histogram.Type.ONE_DIM_INT;

	/**
	 * constant to define a 2d histogram type int
	 */
	protected final static Histogram.Type HIST_2D = Histogram.Type.TWO_DIM_INT;

	/**
	 * constant to define a 1d histogram type double
	 */
	protected final static Histogram.Type HIST_2D_DBL = Histogram.Type.TWO_D_DOUBLE;

	/**
	 * constant to define a 2d histogram type int
	 */
	protected final static Histogram.Type HIST_2D_INT = Histogram.Type.TWO_DIM_INT;

	/**
	 * Creates a one-dimensional, integer-valued, histogram.
	 * 
	 * @param numCh
	 *            number of bins
	 * @param name
	 *            unique name
	 * @return a newly allocated histogram
	 */
	protected static HistInt1D createHist1D(final int numCh, final String name) {
		final Group sortGroup = Group.getSortGroup();
		return (HistInt1D) sortGroup.createHistogram(new int[numCh], name);
	}

	/**
	 * Creates a one-dimensional, integer-valued, histogram.
	 * 
	 * @param numCh
	 *            number of bins
	 * @param name
	 *            unique name
	 * @param title
	 *            verbose title
	 * @return a newly allocated histogram
	 */
	protected static HistInt1D createHist1D(final int numCh, final String name,
			final String title) {
		final Group sortGroup = Group.getSortGroup();
		return (HistInt1D) sortGroup.createHistogram(new int[numCh], name,
				title);
	}

	/**
	 * Creates a one-dimensional, integer-valued, histogram.
	 * 
	 * @param numCh
	 *            number of bins
	 * @param name
	 *            unique name
	 * @param title
	 *            verbose title
	 * @param labelX
	 *            x-axis label
	 * @param labelY
	 *            y-axis label
	 * @return a newly allocated histogram
	 */
	protected static HistInt1D createHist1D(final int numCh, final String name,
			final String title, final String labelX, final String labelY) {
		final Group sortGroup = Group.getSortGroup();
		return (HistInt1D) sortGroup.createHistogram(new int[numCh], name,
				title, labelX, labelY);
	}

	/**
	 * Creates a two-dimensional, integer-valued, histogram.
	 * 
	 * @param chX
	 *            number of bins along the horizontal axis
	 * @param chY
	 *            number of bins along the vertical axis
	 * @param name
	 *            unique name
	 * @return a newly allocated histogram
	 */
	protected static HistInt2D createHist2D(final int chX, final int chY,
			final String name) {
		final Group sortGroup = Group.getSortGroup();
		return (HistInt2D) sortGroup.createHistogram(new int[chX][chY], name);
	}

	/**
	 * Creates a two-dimensional, integer-valued, histogram.
	 * 
	 * @param chX
	 *            number of bins along the horizontal axis
	 * @param chY
	 *            number of bins along the vertical axis
	 * @param name
	 *            unique name
	 * @param title
	 *            verbose title
	 * @return a newly allocated histogram
	 */
	protected static HistInt2D createHist2D(final int chX, final int chY,
			final String name, final String title) {
		final Group sortGroup = Group.getSortGroup();
		return (HistInt2D) sortGroup.createHistogram(new int[chX][chY], name,
				title);
	}

	/**
	 * Creates a two-dimensional, integer-valued, histogram.
	 * 
	 * @param chX
	 *            number of bins along the horizontal axis
	 * @param chY
	 *            number of bins along the vertical axis
	 * @param name
	 *            unique name
	 * @param title
	 *            verbose title
	 * @param labelX
	 *            x-axis label
	 * @param labelY
	 *            y-axis label
	 * @return a newly allocated histogram
	 */
	protected static HistInt2D createHist2D(final int chX, final int chY,
			final String name, final String title, final String labelX,
			final String labelY) {
		final Group sortGroup = Group.getSortGroup();
		return (HistInt2D) sortGroup.createHistogram(new int[chX][chY], name,
				title, labelX, labelY);
	}

	/**
	 * Creates a two-dimensional, integer-valued, histogram.
	 * 
	 * @param chans
	 *            number of bins along the horizontal and vertical axes
	 * @param name
	 *            unique name
	 * @return a newly allocated histogram
	 */
	protected static HistInt2D createHist2D(final int chans, final String name) {
		final Group sortGroup = Group.getSortGroup();
		return (HistInt2D) sortGroup.createHistogram(new int[chans][chans],
				name);
	}

	/**
	 * Creates a two-dimensional, integer-valued, histogram.
	 * 
	 * @param chans
	 *            number of bins along the horizontal and vertical axes
	 * @param name
	 *            unique name
	 * @param title
	 *            verbose title
	 * @return a newly allocated histogram
	 */
	protected static HistInt2D createHist2D(final int chans, final String name,
			final String title) {
		final Group sortGroup = Group.getSortGroup();
		return (HistInt2D) sortGroup.createHistogram(new int[chans][chans],
				name, title);
	}

	/**
	 * Creates a two-dimensional, integer-valued, histogram.
	 * 
	 * @param chans
	 *            number of bins along the horizontal and vertical axes
	 * @param name
	 *            unique name
	 * @param title
	 *            verbose title
	 * @param labelX
	 *            x-axis label
	 * @param labelY
	 *            y-axis label
	 * @return a newly allocated histogram
	 */
	protected static HistInt2D createHist2D(final int chans, final String name,
			final String title, final String labelX, final String labelY) {
		final Group sortGroup = Group.getSortGroup();
		return (HistInt2D) sortGroup.createHistogram(new int[chans][chans],
				name, title, labelX, labelY);
	}

	/**
	 * Creates a data parameter
	 * 
	 * @param name
	 *            unique name
	 * @return a newly allocated parameter
	 */
	protected static DataParameter createParameter(final String name) {
		return new DataParameter(name);
	}

	/**
	 * Creates a scaler
	 * 
	 * @param name
	 *            unique name
	 * @param number
	 *            unique number
	 * @return a newly allocated scaler
	 */
	protected static Scaler createScaler(final String name, final int number) {
		final Group sortGroup = Group.getSortGroup();
		return sortGroup.createScaler(name, number);
	}

	/**
	 * Size of buffer to be used by event streams.
	 */
	private int bufferSize;

	/**
	 * class which is given list of cnafs init(c,n,a,f); event(c,n,a,f);
	 */
	protected transient CamacCommands cnafCommands;

	/**
	 * Output stream to send pre-processed events to.
	 */
	private transient EventWriter eventOutput = null;

	/**
	 * Size of an event to be used for offline sorting. The event size is the
	 * maximum number of paramters in an event.
	 */
	private int eventSize;

	private transient EventSizeMode evtSizeMode = EventSizeMode.INIT;

	/**
	 * User may optionally use this to read in gain calibrations.
	 */
	protected transient final GainCalibration gains;

	/**
	 * Object which contains the VME addressing instructions.
	 */
	protected transient VME_Map vmeMap;

	/**
	 * Set to true when we're writing out events. (For presorting?)
	 */
	private transient boolean writeOn;

	/**
	 * Creates a new sort routine object.
	 */
	public SortRoutine() {
		super();
		setWriteEnabled(false);
		cnafCommands = new CamacCommands(this);
		vmeMap = new VME_Map(this);
		gains = new GainCalibration();
	}

	/**
	 * @see Beginner#begin()
	 */
	public void begin() {
		/* default begin() does nothing */
	}

	/**
	 * @see Ender#end()
	 */
	public void end() {
		/* default end() does nothing */
	}

	/**
	 * Gets the buffer size in bytes.
	 * 
	 * @return size in bytes of event buffers
	 */
	public int getBufferSize() {
		return bufferSize;
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
	 * Returns size of a event.
	 * 
	 * @return the size of the events
	 * @throws SortException
	 *             when there is no event size yet
	 */
	public int getEventSize() {
		final int rval;
		if (evtSizeMode.isSet()) {
			if (evtSizeMode == EventSizeMode.CNAF) {
				rval = cnafCommands.getEventSize();
			} else if (evtSizeMode == EventSizeMode.VME_MAP) {
				rval = vmeMap.getEventSize();
			} else {// ==EXPLICIT
				rval = eventSize;
			}
		} else {
			throw new IllegalStateException("Event Size Unkown");
		}
		return rval;
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
	 * Hands Jam the object representing VME acquisition specifications.
	 * 
	 * @return the object containing directives for ADC's and TDC's
	 */
	public final VME_Map getVMEmap() {
		return vmeMap;
	}

	private boolean isWriteEnabled() {
		synchronized (this) {
			return writeOn;
		}
	}

	/**
	 * @see jam.data.Sorter#initialize()
	 */
	public abstract void initialize() throws Exception;// NOPMD

	/**
	 * Required by the <code>Sorter</code> interface. As written always returns
	 * zero, and should be overwritten whenever using monitors.
	 * 
	 * @param name
	 *            name of monitor value to calculate
	 * @see jam.data.Monitor
	 * @see jam.data.Sorter#monitor(String)
	 * @return floating point value of the monitor
	 */
	public double monitor(final String name) {
		return 0.0;
	}

	/**
	 * Sets the buffer size in bytes to use.
	 * 
	 * @param size
	 *            in bytes of event buffers
	 */
	protected void setBufferSize(final int size) {
		bufferSize = size;
	}

	/**
	 * Set the event stream to use to write out events.
	 * 
	 * @param out
	 *            stream to which presorted event output will go
	 */
	public final void setEventOutputStream(final EventWriter out) {
		synchronized (this) {
			eventOutput = out;
		}
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
	protected void setEventSize(final int size) throws SortException {
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
	public void setEventSizeMode(final EventSizeMode mode) throws SortException {
		final StringBuffer mess = new StringBuffer();
		if ((!evtSizeMode.equals(mode))
				&& (!evtSizeMode.equals(EventSizeMode.INIT))) {
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
			throw new SortException(mess.append(
					"Illegal value for event size mode: ").append(mode)
					.toString());
		}
	}

	/**
	 * set the state to write out events
	 * 
	 * @param state
	 *            true to write out events.
	 */
	public final void setWriteEnabled(final boolean state) {
		synchronized (this) {
			writeOn = state;
		}
	}

	/**
	 * @see jam.data.Sorter#sort(int[])
	 */
	public abstract void sort(int[] dataWords) throws Exception;// NOPMD

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
	public final void writeEvent(final int[] event) throws SortException {
		if (isWriteEnabled()) {
			try {
				synchronized (eventOutput) {
					eventOutput.writeEvent(event);
				}
			} catch (IOException e) {
				throw new SortException(e);
			}
		}
	}

}