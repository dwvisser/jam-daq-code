package jam.global;

/**
 * All the special property keys that Jam uses.
 */
public interface PropertyKeys {
	/**
	 * Default path to look in for event files to sort offline.
	 */
	String EVENT_INPATH = "event.inpath";

	/**
	 * Fully qualified name of the default <code>EventInputStream</code>.
	 * 
	 * @see jam.sort.stream.AbstractEventInputStream
	 */
	String EVENT_INSTREAM = "event.instream";

	/**
	 * Filename to use when writing a pre-sorted event file.
	 */
	String EVENT_OUTFILE = "event.outfile";

	/**
	 * Default path to use for writing event files.
	 */
	String EVENT_OUTPATH = "event.outpath";

	/**
	 * Fully qualified name of the default <code>EventOutputStream</code>.
	 * 
	 * @see jam.sort.stream.AbstractEventOutputStream
	 */
	String EVENT_OUTSTREAM = "event.outstream";

	/**
	 * Default experiment name to use when naming data files.
	 */
	String EXP_NAME = "exp.name";

	/**
	 * Default path for saving HDF files.
	 * 
	 * @see jam.io.hdf.HDFIO
	 */
	String HIST_PATH = "hist.path";

	/**
	 * IP address of the socket used to receive data from the front end.
	 */
	String HOST_DATA_IP = "host-data.IP";

	/**
	 * Port number of the socket used to receive data from the front end.
	 */
	String HOST_DATA_P_RECV = "host-data.portRecv";

	/**
	 * IP address of the socket used to communicate with the front end.
	 * 
	 * @see jam.FrontEndCommunication
	 */
	String HOST_IP = "host.IP";

	/**
	 * Port number for recieving messages from the front end.
	 */
	String HOST_PORT_RECV = "host.portRecv";

	/**
	 * Port number for sending messages to the front end.
	 */
	String HOST_PORT_SEND = "host.portSend";

	/**
	 * Path to search for <code>JamConfig.ini</code>
	 */
	String JAM_HOME = "jam.home";

	/**
	 * Default path to the folder for writing out the console log.
	 */
	String LOG_PATH = "log.path";

	/**
	 * Path to search for and load sort routines.
	 */
	String SORT_CLASSPATH = "sort.classpath";

	/**
	 * Fully qualified name of the default sort routine.
	 * 
	 * @see jam.sort.SortRoutine
	 */
	String SORT_ROUTINE = "sort.routine";

	/**
	 * Front end's IP address for communicating with Jam.
	 */
	String TARGET_IP = "target.IP";

	/**
	 * Front end's port number for communciating with Jam.
	 */
	String TARGET_PORT = "target.port";
	
	
}
