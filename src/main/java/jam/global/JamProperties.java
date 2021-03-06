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
 * we save the messages so they can be later retrieved. Directory for loading
 * configuration is give by define jam.home. jam.home should be defined by -D
 * parameter in command line. For config we try jam.home which if null we try
 * DEFAULT_USER_HOME For user we try user.home defined in JamConfig.ini then
 * jam.home defined on the command line then DEFAULT_USER_HOME then Properties
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
	/**
	 * Set <code>SORT_CLASSPATH</code> to this to use the classpath used to
	 * launch Jam instead of a given sort classpath.
	 */
	public final static String DEFAULT_SORTPATH = "default";

	/**
	 * Machine configuration file name
	 */
	private static final String FILE_CONFIG = "JamConfig.ini";

	/**
	 * User configuration file name
	 */
	private static final String FILE_USER = "JamUser.ini";

	private static final Logger LOGGER = Logger.getLogger(JamProperties.class
			.getPackage().getName());

	private final static String NO_ERRORS = "No error messages.";

	private final static String NO_WARNINGS = "No warning messages.";

	/**
	 * Jam properties
	 */
	private static final Properties PROPERTIES = new Properties();

	private static final String USERHOME = System.getProperty("user.home");

	/**
	 * Get the value of a boolean property.
	 * 
	 * @param key
	 *            the name of the property
	 * @return <code>true</code> if equal to <code>Boolean.toString(true)</code>
	 */
	static public boolean getBooleanProperty(final String key) {
		return PROPERTIES.getProperty(key).equals(Boolean.TRUE.toString());
	}

	/**
	 * @return the properties for this Jam process
	 */
	public static Properties getProperties() {
		return PROPERTIES;
	}

	/**
	 * @param key
	 *            which property to retrieve
	 * @return the integer value of the property
	 */
	public static int getPropInt(final String key) {
		int rval = 0;// default return value
		final String classname = JamProperties.class.getName();
		try {
			if (PROPERTIES.getProperty(key) == null) {
				LOGGER.warning(classname + ".getPropInt(): property for " + key
						+ " is not defined.");
			} else {
				rval = Integer.parseInt(PROPERTIES.getProperty(key).trim());
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
		final String property = PROPERTIES.getProperty(key);
		if (property == null) {
			LOGGER.warning(JamProperties.class.getName()
					+ ".getPropString(): property for " + key
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
		final String operatingSystem = System.getProperty("os.name");
		return "Mac OS X".equals(operatingSystem);
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
		PROPERTIES.setProperty(key, val ? Boolean.TRUE.toString()
				: Boolean.FALSE.toString());
	}

	/**
	 * Set a boolean property.
	 * 
	 * @param key
	 *            the name of the property
	 * @param val
	 *            the new value
	 */
	static public void setProperty(final String key, final String val) {
		PROPERTIES.setProperty(key, val);
	}

	/** message for loading config */
	private transient String configLoadMessage;

	/** warning when loading config */
	private transient String configLoadWarning;

	private transient FileInputStream inputStream;

	private transient boolean jamHomeDefined;

	private transient String loadError;

	private transient final String userCurrentDir = System
			.getProperty("user.dir");

	private transient File userFile;

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
	 */
	private void loadConfig() {
		configLoadWarning = NO_WARNINGS;
		loadError = NO_ERRORS;
		String fileName = "";
		FileInputStream fis = null;
		boolean fileRead = false;
		try {
			// if jam.home is property given we must use it.
			if (jamHomeDefined) {
				final File file = new File(
						System.getProperty(PropertyKeys.JAM_HOME), FILE_CONFIG);
				fileName = file.getPath();
				if (file.exists()) {
					fis = new FileInputStream(file);
					PROPERTIES.load(fis);
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
					PROPERTIES.load(fis);
					configLoadMessage = "Read configuration properties from file: "
							+ configFile.getPath();
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
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException ioe) {
					loadError = "Problem closing file.";
					showErrorMessage(ioe, loadError);
				}
			}
		}
	}

	/**
	 * Load the local specific properties from files, using defaults if
	 * necessary.
	 */
	private void loadProperties() {
		jamHomeDefined = (System.getProperty(PropertyKeys.JAM_HOME) != null);
		setDefaultConfig();
		setDefaultUser();
		loadConfig();
		loadUser();
	}

	/**
	 * read in user jam properties try user.home from JamConfig.ini first
	 */
	private void loadUser() {
		userLoadWarning = NO_WARNINGS;
		loadError = NO_ERRORS;
		userFile = new File(USERHOME, FILE_USER);
		try {
			// try userHomeDir
			if (userFile.exists()) {
				inputStream = new FileInputStream(userFile);
				PROPERTIES.load(inputStream);
				userLoadMessage = "Read user configuration file: "
						+ userFile.getPath();
			} else { // try userCurrentDir
				loadUserPropertiesFromCurrentDirectory();
			}
			// try variable jam.home directory
			if (!userFile.exists() && jamHomeDefined) {
				userFile = new File(System.getProperty(PropertyKeys.JAM_HOME),
						FILE_USER);
				if (userFile.exists()) {
					inputStream = new FileInputStream(userFile);
					PROPERTIES.load(inputStream);
					userLoadMessage = "Read user configuration file: "
							+ userFile.getPath();
				}
			}
		} catch (FileNotFoundException fnfe) {
			loadError = "Jam user configuration file, " + userFile.getPath()
					+ ",  not found.";
			showErrorMessage(fnfe, loadError);
		} catch (IOException ioe) {
			loadError = "Could not read user configuration file, "
					+ userFile.getPath() + ".";
			showErrorMessage(ioe, loadError);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException ioe) {
				loadError = "Could not close " + userFile.getPath();
				showErrorMessage(ioe, loadError);
			}
		}
	}

	private void loadUserPropertiesFromCurrentDirectory()
			throws IOException {
		userFile = new File(userCurrentDir, FILE_USER);
		if (userFile.exists()) {
			inputStream = new FileInputStream(userFile);
			PROPERTIES.load(inputStream);
			userLoadWarning = "Cannot find user configuration file "
					+ FILE_USER + " in user home directory " + USERHOME;
			userLoadMessage = "Read user configuration from file "
					+ userFile.getPath();
			// use default properties
		} else {
			userLoadWarning = "Cannot find user configuration file "
					+ FILE_USER + ", in user home directory " + USERHOME
					+ " or in current directory " + userCurrentDir;
			userLoadMessage = "Using default user properties";
		}
	}

	/**
	 * Request to print output messages to console.
	 */
	public void outputMessages() {
		if (!jamHomeDefined) {
			LOGGER.warning("Jam home variable not defined, to define use -Djam.home=<directory>");
		}
		if (!NO_ERRORS.equals(loadError)) {
			LOGGER.warning(loadError);
		}
		if (!NO_WARNINGS.equals(configLoadWarning)) {
			LOGGER.warning(configLoadWarning);
		}
		LOGGER.info(configLoadMessage);
		if (!NO_WARNINGS.equals(userLoadWarning)) {
			LOGGER.warning(userLoadWarning);
		}
		LOGGER.info(userLoadMessage);
	}

	/**
	 * Load default configuration properties.
	 */
	private void setDefaultConfig() {
		PROPERTIES.setProperty(PropertyKeys.JAM_HOME,
				new File(userCurrentDir).getPath());
		PROPERTIES.setProperty(PropertyKeys.HOST_IP, "localhost");
		PROPERTIES.setProperty(PropertyKeys.HOST_PORT_SEND, "5002");
		PROPERTIES.setProperty(PropertyKeys.HOST_PORT_RECV, "5006");
		PROPERTIES.setProperty(PropertyKeys.TARGET_IP, "localhost");
		PROPERTIES.setProperty(PropertyKeys.TARGET_PORT, "5003");
		PROPERTIES.setProperty(PropertyKeys.HOST_DATA_IP, "localhost");
		PROPERTIES.setProperty(PropertyKeys.HOST_DATA_P_RECV, "10205");
	}

	/**
	 * Load default user properties.
	 */
	private void setDefaultUser() {
		PROPERTIES.setProperty(PropertyKeys.EXP_NAME, "default_");
		PROPERTIES.setProperty(PropertyKeys.SORT_ROUTINE, "jam.sort.Example");
		PROPERTIES.setProperty(PropertyKeys.SORT_CLASSPATH, DEFAULT_SORTPATH);
		PROPERTIES.setProperty(PropertyKeys.HIST_PATH, new File(USERHOME,
				"spectra").getPath());
		PROPERTIES.setProperty(PropertyKeys.EVENT_INPATH, new File(USERHOME,
				"events").getPath());
		PROPERTIES.setProperty(PropertyKeys.EVENT_OUTPATH, new File(USERHOME,
				"events").getPath());
		PROPERTIES.setProperty(PropertyKeys.EVENT_OUTFILE, "sortout.evn");
		PROPERTIES.setProperty(PropertyKeys.LOG_PATH,
				new File(USERHOME).getPath());
		PROPERTIES.setProperty(PropertyKeys.EVENT_INSTREAM,
				"jam.sort.stream.YaleCAEN_InputStream");
		PROPERTIES.setProperty(PropertyKeys.EVENT_OUTSTREAM,
				"jam.sort.stream.YaleOutputStream");
	}

	private void showErrorMessage(final Exception exception, final String extra) {
		final String message = exception.getMessage() + "; " + extra;
		LOGGER.severe(JamProperties.class.getName() + "--" + message);
	}
}
