package jam.global;

/**
 * All the special property keys that Jam uses.
 */
public final class PropertyKeys {
	
	private PropertyKeys(){
		super();
	}
	
	/**
	 * Default path to look in for event files to sort offline.
	 */
	public static final String EVENT_INPATH = "event.inpath";

	/**
	 * Fully qualified name of the default <code>EventInputStream</code>.
	 * 
	 * @see jam.sort.stream.AbstractEventInputStream
	 */
	public static final String EVENT_INSTREAM = "event.instream";

	/**
	 * Filename to use when writing a pre-sorted event file.
	 */
	public static final String EVENT_OUTFILE = "event.outfile";

	/**
	 * Default path to use for writing event files.
	 */
	public static final String EVENT_OUTPATH = "event.outpath";

	/**
	 * Fully qualified name of the default <code>EventOutputStream</code>.
	 * 
	 * @see jam.sort.stream.AbstractEventOutputStream
	 */
	public static final String EVENT_OUTSTREAM = "event.outstream";

	/**
	 * Default experiment name to use when naming data files.
	 */
	public static final String EXP_NAME = "exp.name";

	/**
	 * Default path for saving HDF files.
	 * 
	 * @see jam.io.hdf.HDFIO
	 */
	public static final String HIST_PATH = "hist.path";

	/**
	 * IP address of the socket used to receive data from the front end.
	 */
	public static final String HOST_DATA_IP = "host-data.IP";

	/**
	 * Port number of the socket used to receive data from the front end.
	 */
	public static final String HOST_DATA_P_RECV = "host-data.portRecv";

	/**
	 * IP address of the socket used to communicate with the front end.
	 * 
	 * @see jam.comm.FrontEndCommunication
	 */
	public static final String HOST_IP = "host.IP";

	/**
	 * Port number for recieving messages from the front end.
	 */
	public static final String HOST_PORT_RECV = "host.portRecv";

	/**
	 * Port number for sending messages to the front end.
	 */
	public static final String HOST_PORT_SEND = "host.portSend";

	/**
	 * Path to search for <code>JamConfig.ini</code>
	 */
	public static final String JAM_HOME = "jam.home";

	/**
	 * Default path to the folder for writing out the console log.
	 */
	public static final String LOG_PATH = "log.path";

	/**
	 * Path to search for and load sort routines.
	 */
	public static final String SORT_CLASSPATH = "sort.classpath";

	/**
	 * Fully qualified name of the default sort routine.
	 * 
	 * @see jam.sort.AbstractSortRoutine
	 */
	public static final String SORT_ROUTINE = "sort.routine";

	/**
	 * Front end's IP address for communicating with Jam.
	 */
	public static final String TARGET_IP = "target.IP";

	/**
	 * Front end's port number for communciating with Jam.
	 */
	public static final String TARGET_PORT = "target.port";
}
