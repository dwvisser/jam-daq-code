package jam.global;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * This is an class that is used to get and set the properties for Jam. As the
 * properties are loaded before a way to print out messages has been constructed
 * we save the messages so they can be later retrieved.
 * 
 * Diretory for loading configuration is give by define jam.home. jam.home
 * should be defined by -D parameter in command line.
 * 
 * For config we try jam.home which if null we try DEFAULT_USER_HOME For user we
 * try user.home defined in JamConfig.ini then jam.home defined on the command
 * line then DEFAULT_USER_HOME then Properties
 * <ul>
 * <li>jam.home
 * <li>host.ip
 * <li>host.IP=iotwo
 * <li>host.portSend=5003
 * <li>host.portRecv=5002
 * <li>target.IP=calvin
 * <li>target.port=5002
 * <li>host-data.IP=iotwo-data
 * <li>host-data.portRecv=10205
 * <li>sort.path=/home/jam/sortfiles
 * <li>sort.stream=jam.sort.L002InputStream
 * <li>cnaf.path=/JAM/CAMAC/CNAFS
 * <li>browser.path=netscape
 * <li>docs.path=documentation
 * <li>jam.home=/home/jam
 * <li>log.path assumed to be jam.home if not given
 * </ul>
 * 
 * @author Ken Swartz
 * @author Dale Visser
 * @version 1.4
 * @since 0.5 17 January 1999
 */
public final class JamProperties {
	private static final String classname = "jam.global.JamProperties";

	/**
	 * Set <code>true</code> to default to the classpath used to launch Jam
	 * instead of a given sort classpath.
	 */
	public final static String DEFAULT_SORT_CLASSPATH = "default";

	/**
	 * Default path to look in for event files to sort offline.
	 */
	public final static String EVENT_INPATH = "event.inpath";

	/**
	 * Fully qualified name of the default <code>EventInputStream</code>.
	 * 
	 * @see jam.sort.stream.AbstractEventInputStream
	 */
	public final static String EVENT_INSTREAM = "event.instream";

	/**
	 * Filename to use when writing a pre-sorted event file.
	 */
	public final static String EVENT_OUTFILE = "event.outfile";

	/**
	 * Default path to use for writing event files.
	 */
	public final static String EVENT_OUTPATH = "event.outpath";

	/**
	 * Fully qualified name of the default <code>EventOutputStream</code>.
	 * 
	 * @see jam.sort.stream.AbstractEventOutputStream
	 */
	public final static String EVENT_OUTSTREAM = "event.outstream";

	/**
	 * Default experiment name to use when naming data files.
	 */
	public final static String EXP_NAME = "exp.name";

	private static final String FALSE = Boolean.toString(false);

	/**
	 * Machine configuration file name
	 */
	static final String FILE_CONFIG = "JamConfig.ini";

	/**
	 * User configuration file name
	 */
	static final String FILE_USER = "JamUser.ini";

	/**
	 * Default path for saving HDF files.
	 * 
	 * @see jam.io.hdf.HDFIO
	 */
	public final static String HIST_PATH = "hist.path";

	/**
	 * IP address of the socket used to receive data from the front end.
	 */
	public final static String HOST_DATA_IP = "host-data.IP";

	/**
	 * Port number of the socket used to receive data from the front end.
	 */
	public final static String HOST_DATA_PORT_RECV = "host-data.portRecv";

	/**
	 * IP address of the socket used to communicate with the front end.
	 * 
	 * @see jam.FrontEndCommunication
	 */
	public final static String HOST_IP = "host.IP";

	/**
	 * Port number for recieving messages from the front end.
	 */
	public final static String HOST_PORT_RECV = "host.portRecv";

	/**
	 * Port number for sending messages to the front end.
	 */
	public final static String HOST_PORT_SEND = "host.portSend";

	/**
	 * Path to search for <code>JamConfig.ini</code>
	 */
	public final static String JAM_HOME = "jam.home";

	/**
	 * Default path to the folder for writing out the console log.
	 */
	public final static String LOG_PATH = "log.path";

	private static final Logger LOGGER = Logger.getLogger(JamProperties.class
			.getPackage().getName());

	private static boolean macosx = false;

	private final static String NO_ERRORS = "No error messages.";

	private final static String NO_WARNINGS = "No warning messages.";

	private static String operatingSystem = null;

	/**
	 * Jam properties
	 */
	private static Properties properties = new Properties();

	/**
	 * Path to search for and load sort routines.
	 */
	public final static String SORT_CLASSPATH = "sort.classpath";

	/**
	 * Fully qualified name of the default sort routine.
	 * 
	 * @see jam.sort.SortRoutine
	 */
	public final static String SORT_ROUTINE = "sort.routine";

	/**
	 * Front end's IP address for communicating with Jam.
	 */
	public final static String TARGET_IP = "target.IP";

	/**
	 * Front end's port number for communciating with Jam.
	 */
	public final static String TARGET_PORT = "target.port";

	private static final String TRUE = Boolean.toString(true);

	private static final String userHomeDir = System.getProperty("user.home");

	/**
	 * Get the value of a boolean property.
	 * 
	 * @param key
	 *            the name of the property
	 * @return <code>true</code> if equal to
	 *         <code>Boolean.toString(true)</code>
	 */
	static public boolean getBooleanProperty(final String key) {
		return properties.getProperty(key).equals(TRUE);
	}

	/**
	 * @return the properties for this Jam process
	 */
	public static Properties getProperties() {
		return properties;
	}

	/**
	 * @param key
	 *            which property to retrieve
	 * @return the integer value of the property
	 */
	public static int getPropInt(final String key) {
		int rval = 0;// default return value
		try {
			if (properties.getProperty(key) == null) {
				LOGGER.warning(classname + ".getPropInt(): property for " + key
						+ " is not defined.");
			} else {
				rval = Integer.parseInt(properties.getProperty(key).trim());
			}
		} catch (NumberFormatException nfe) {
			LOGGER.throwing(classname, "getPropInt", nfe);
			LOGGER.severe(classname + ".getPropInt(): property for " + key
					+ " is not an integer.");
		}
		return rval;
	}

	/**
	 * @param key
	 *            which property to retrieve
	 * @return the value for the given property
	 */
	public static String getPropString(final String key) {
		String rval = "undefined";// default return value
		final String property = properties.getProperty(key);
		if (property == null) {
			LOGGER.warning(classname + ".getPropString(): property for " + key
					+ " is not defined.");
		} else {
			rval = property.trim();
		}
		return rval;
	}

	/**
	 * Returns whether we are running on MacOS X.
	 * 
	 * @return <code>true</code> if the operating system is MacOS X
	 */
	public static boolean isMacOSX() {
		if (operatingSystem == null) {
			operatingSystem = System.getProperty("os.name");
			macosx = operatingSystem.equals("Mac OS X");
		}
		return macosx;
	}

	/**
	 * Set a boolean property.
	 * 
	 * @param key
	 *            the name of the property
	 * @param val
	 *            the new value
	 */
	static public void setProperty(final String key, final boolean val) {
		properties.setProperty(key, val ? TRUE : FALSE);
	}

	/** message for loading config */
	private transient String configLoadMessage;

	/** warning when loading config */
	private transient String configLoadWarning;

	private transient boolean jamHomeDefined;

	private transient String loadError;

	transient final String userCurrentDir = System.getProperty("user.dir");

	/** message for loading user config */
	private transient String userLoadMessage;

	/** warning when loading user */
	private transient String userLoadWarning;

	/**
	 * Constructor
	 */
	public JamProperties() {
		super();
		loadProperties();
	}

	/**
	 * Read in jam config properties for normal use jam.home should be defined
	 * by -D parameter in command line
	 * 
	 */

	private void loadConfig() {

		configLoadWarning = NO_WARNINGS;
		loadError = NO_ERRORS;
		String fileName = "";
		FileInputStream fis;
		boolean fileRead = false;

		try {

			// if jam.home is property given we must use it.
			if (jamHomeDefined) {
				final File file = new File(System.getProperty(JAM_HOME),
						FILE_CONFIG);
				fileName = file.getPath();
				if (file.exists()) {
					fis = new FileInputStream(file);
					properties.load(fis);
					configLoadMessage = "Read configuration properties from file: "
							+ file.getPath();
					fileRead = true;
					// could not find default
				} else {
					configLoadWarning = "Cannot find machine configuration file: JamConfig.ini, in directory defined by jam.home = "
							+ System.getProperty("jam.home") + " ";
					configLoadMessage = "Using default configuration";
				}
			}
			// try userCurrentDir no jam.home defined
			if (!fileRead) {
				final File configFile = new File(userCurrentDir, FILE_CONFIG);
				fileName = configFile.getPath();
				if (configFile.exists()) {
					fis = new FileInputStream(configFile);
					properties.load(fis);
					configLoadMessage = "Read configuration properties from file: "
							+ configFile.getPath();
					fileRead = true;
				} else {
					configLoadWarning = "Cannot find machine configuration file "
							+ FILE_CONFIG
							+ ", in current directory "
							+ userCurrentDir;
					configLoadMessage = "Using default configuration";
				}
			}
		} catch (FileNotFoundException fnfe) {
			loadError = "Jam configuration file, " + fileName + ",  not found.";
			showErrorMessage(fnfe, loadError);
		} catch (IOException ioe) {
			loadError = "Could not read configuration file, " + fileName + ".";
			showErrorMessage(ioe, loadError);
		}

	}

	/**
	 * Load default configuration properties.
	 */
	private void loadDefaultConfig() {
		properties.setProperty(JAM_HOME, (new File(userCurrentDir)).getPath());
		properties.setProperty(HOST_IP, "localhost");
		properties.setProperty(HOST_PORT_SEND, "5003");
		properties.setProperty(HOST_PORT_SEND, "5002");
		properties.setProperty(HOST_PORT_RECV, "5005");
		properties.setProperty(TARGET_IP, "frontend");
		properties.setProperty(TARGET_PORT, "5002");
		properties.setProperty(HOST_DATA_IP, "localhost-data");
		properties.setProperty(HOST_DATA_PORT_RECV, "10205");
	}

	/**
	 * Load default user properties.
	 */
	private void loadDefaultUser() {
		properties.setProperty(EXP_NAME, "default_");
		properties.setProperty(SORT_ROUTINE, "jam.sort.Example");
		properties.setProperty(SORT_CLASSPATH, DEFAULT_SORT_CLASSPATH);
		properties.setProperty(HIST_PATH, (new File(userHomeDir, "spectra"))
				.getPath());
		properties.setProperty(EVENT_INPATH, (new File(userHomeDir, "events"))
				.getPath());
		properties.setProperty(EVENT_OUTPATH, (new File(userHomeDir, "events"))
				.getPath());
		properties.setProperty(EVENT_OUTFILE, "sortout.evn");
		properties.setProperty(LOG_PATH, (new File(userHomeDir)).getPath());
		properties.setProperty(EVENT_INSTREAM,
				"jam.sort.stream.YaleCAEN_InputStream");
		properties.setProperty(EVENT_OUTSTREAM,
				"jam.sort.stream.YaleOutputStream");
	}

	/**
	 * Load the local specific properties from files, using defaults if
	 * necessary.
	 */
	private void loadProperties() {
		jamHomeDefined = (System.getProperty(JAM_HOME) != null);
		loadDefaultConfig();
		loadDefaultUser();
		loadConfig();
		loadUser();

	}

	/**
	 * read in user jam properties try user.home from JamConfig.ini first
	 * 
	 */
	private void loadUser() {
		userLoadWarning = NO_WARNINGS;
		loadError = NO_ERRORS;
		String fileName = "";
		FileInputStream fis;
		boolean fileRead = false;
		try {
			// try userHomeDir
			File userFile = new File(userHomeDir, FILE_USER);
			fileName = userFile.getPath();
			if (userFile.exists()) {
				fis = new FileInputStream(userFile);
				properties.load(fis);
				userLoadMessage = "Read user configuration file: "
						+ userFile.getPath();
				fileRead = true;

			}
			// try userCurrentDir
			if (!fileRead) {
				userFile = new File(userCurrentDir, FILE_USER);
				fileName = userFile.getPath();
				if (userFile.exists()) {
					fis = new FileInputStream(userFile);
					properties.load(fis);
					userLoadWarning = "Cannot find user configuration file "
							+ FILE_USER + " in user home directory "
							+ userHomeDir;
					userLoadMessage = "Read user configuration from file "
							+ userFile.getPath();
					fileRead = true;
					// use default properties
				} else {
					userLoadWarning = "Cannot find user configuration file "
							+ FILE_USER + ", in user home directory "
							+ userHomeDir + " or in current directory "
							+ userCurrentDir;
					userLoadMessage = "Using default user properties";
				}
			}
			// try variable jam.home directory
			if (!fileRead && jamHomeDefined) {
				userFile = new File(System.getProperty(JAM_HOME), FILE_USER);
				fileName = userFile.getPath();
				if (userFile.exists()) {
					fis = new FileInputStream(userFile);
					properties.load(fis);
					userLoadMessage = "Read user configuration file: "
							+ userFile.getPath();
					fileRead = true;
				}
			}
		} catch (FileNotFoundException fnfe) {
			loadError = "Jam user configuration file, " + fileName
					+ ",  not found.";
			showErrorMessage(fnfe, loadError);
		} catch (IOException ioe) {
			loadError = "Could not read user configuration file, " + fileName
					+ ".";
			showErrorMessage(ioe, loadError);
		}
	}

	/**
	 * Request to print output messages to console.
	 */
	public void outputMessages() {
		if (!jamHomeDefined) {
			LOGGER
					.warning("Jam home variable not defined, to define use -Djam.home=<directory>");
		}
		if (loadError != NO_ERRORS) {
			LOGGER.warning(loadError);
		}
		if (configLoadWarning != NO_WARNINGS) {
			LOGGER.warning(configLoadWarning);
		}
		LOGGER.info(configLoadMessage);
		if (userLoadWarning != NO_WARNINGS) {
			LOGGER.warning(userLoadWarning);
		}
		LOGGER.info(userLoadMessage);
	}

	private void showErrorMessage(final Exception exception, final String extra) {
		final String message = exception.getMessage() + "; " + extra;
		LOGGER.severe(classname + "--" + message);
	}
}
