package jam.global;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * This is an class that is used to get and set the properties for Jam.
 * As the properties are loaded before a way to print out messages has
 * been constructed we save the messages so they can be later retrieved.
 *
 * For config we try jam.home which if null we try DEFAULT_JAM_HOME
 * For user we try user.home then jam.home then DEFAULT_JAM_HOME then 
 * Properties
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
 * @version 0.5 1-17-99
 */
public class JamProperties {

	static final String FILE_CONFIG = "JamConfig.ini";
	static final String FILE_USER = "JamUser.ini";
	static final String DEFAULT_JAM_HOME = /*"/home/jam"*/System.getProperty("user.home");

	public final static String JAM_HOME = "jam.home";
	public final static String HOST_IP = "host.IP";
	public final static String HOST_PORT_SEND = "host.portSend";
	public final static String HOST_PORT_RECV = "host.portRecv";
	public final static String TARGET_IP = "target.IP";
	public final static String TARGET_PORT = "target.port";
	public final static String HOST_DATA_IP = "host-data.IP";
	public final static String HOST_DATA_PORT_RECV = "host-data.portRecv";
	public final static String EXP_NAME = "exp.name";
	public final static String SORT_ROUTINE = "sort.routine";
	public final static String SORT_CLASSPATH = "sort.classpath";
	public final static String DEFAULT_SORT_CLASSPATH = "default";	
	public final static String EVENT_INSTREAM = "event.instream";
	public final static String EVENT_OUTSTREAM = "event.outstream";
	public final static String EVENT_INPATH = "event.inpath";
	public final static String EVENT_OUTPATH = "event.outpath";
	public final static String EVENT_OUTFILE = "event.outfile";
	public final static String HIST_PATH = "hist.path";
	public final static String TAPE_DEV = "tape.dev";
	public final static String LOG_PATH = "log.path";
	public final static String FRONTEND_VERBOSE = "frontend.verbose";
	public final static String FRONTEND_DEBUG = "frontend.debug";
	public final static String NO_FILL_2D = "graph.nofill2d";
	public final static String GRADIENT_SCALE = "graph.gradientScale";
	private final static String NO_ERRORS="No error messages.";
	private final static String NO_WARNINGS="No warning messages.";
	
	private final static String TRUE="true";
	private final static String FALSE="false";
	
	String END_LINE = System.getProperty("line.separator");
	private String fileSep = System.getProperty("file.separator");
	private String userHome = System.getProperty("user.home");
	private static String os=null;
	private static boolean macosx=false;

	/**
	 * Jam properties
	 */
	private static Properties jamProperties;

	/** file jam config properties read from */
	private File configFile;

	/** file user properties read from */
	private File userFile;
	/** */
	private String configLoadMessage;
	/** */
	private String userLoadMessage;
	/** warning when loading config */
	private String configLoadWarning;
	/** warning when loading user */
	private String userLoadWarning;

	private String loadError;

	/** */
	private static MessageHandler msgHandler;
	
	/**
	 * Constructor
	 */
	public JamProperties() {
		jamProperties = new Properties();
		loadProperties();
	}
	
	public static boolean isMacOSX(){
		if (os==null){
			os=System.getProperty("os.name");
			macosx=os.equals("Mac OS X");			
		}
		return macosx;
	}
	
	public void setMessageHandler(MessageHandler msgHandler) {
		JamProperties.msgHandler = msgHandler;
	}
	
	/**
	 * get the properties for this Jam process
	 */
	public static Properties getProperties() {
		return jamProperties;
	}

	/**
	 * get a property for this Jam process
	 */
	public static String getProperty(String key) {
		return jamProperties.getProperty(key);
	}

	/**
	 * get a String property for this Jam process
	 * 
	 */
	public static String getPropString(String key) {
		String rval = "undefined";//default return value
		if (jamProperties.getProperty(key) != null) {
			rval = (String) (jamProperties.getProperty(key)).trim();
		} else {
			msgHandler.warningOutln(
				"Property " + key + " not defined [JamProperties]");
		}
		return rval;
	}

	/**
	 * Get an integer property for this Jam process.
	 *
	 * @return the integer value of the property
	 */
	public static int getPropInt(String key) {
		int rval=0;//default return value
		try {
			if (jamProperties.getProperty(key) != null) {
				rval = Integer.parseInt(jamProperties.getProperty(key).trim());
			} else {
				msgHandler.warningOutln(
					"Property " + key + " not defined [JamProperties]");
			}
		} catch (NumberFormatException nfe) {
			msgHandler.errorOutln(
				"Property " + key + " not an integer [JamProperties]");
		}
		return rval;
	}

	/**
	 * Load the local specific properties from files, using defaults if 
	 * necessary.
	 */
	private final void loadProperties() {
		String fileName = "";
		FileInputStream fis;
		configLoadWarning = NO_WARNINGS;
		userLoadWarning = NO_WARNINGS;
		loadError = NO_ERRORS;
		try {
			//load default configuration
			loadDefaultConfig();
			loadDefaultUser();
			/* read in jam config properties 
			 * for normal use jam.home should be defined by -D parameter in command line
			 * try jam.home */
			if (System.getProperty(JAM_HOME) != null) {
				final File configFile = new File(
					System.getProperty(JAM_HOME),FILE_CONFIG);
				fileName = configFile.getPath();
				if (configFile.exists()) {
					fis = new FileInputStream(configFile);
					jamProperties.load(fis);
					configLoadMessage =
						"Read configuration properties from file: "
							+ configFile.getPath();
					//could not find default		    		
				} else {
					configLoadWarning =
						"Cannot find file JamConfig.ini, in directory defined by jam.home = "
							+ System.getProperty("jam.home")
							+ " ";
					configLoadMessage = "Using default configuration";
				}
			} else {//try DEFAULT_JAM_HOME no jam.home defined
				configFile =
					new File(DEFAULT_JAM_HOME,FILE_CONFIG);
				fileName = configFile.getPath();
				if (configFile.exists()) {
					fis = new FileInputStream(configFile);
					jamProperties.load(fis);
					configLoadWarning = "Property " + JAM_HOME + " not defined";
					configLoadMessage =
						"Read configuration properties from file: "
							+ configFile.getPath();
				} else {
					configLoadWarning =
						"Cannot find file "
							+ FILE_CONFIG
							+ ", in default directory "
							+ DEFAULT_JAM_HOME
							+ " ";
					configLoadMessage = "Using default configuration";
				}
			}
			//read in user jam properties
			//try user.home	
			try {
				userFile = new File(
					userHome,FILE_USER);
				fileName = userFile.getPath();
			} catch (Exception e) {
				fileName =
					JAM_HOME + fileSep + FILE_USER;
			}

			if (userFile.exists()) {
				fis = new FileInputStream(userFile);
				jamProperties.load(fis);
				userLoadMessage =
					"Read user properties from file: " + 
					userFile.getPath();
			} else {
				//try jam.home directory
				if (System.getProperty(JAM_HOME) != null) {
					userFile =new File(
						System.getProperty(JAM_HOME),FILE_USER);
					fileName = userFile.getPath();
					if (userFile.exists()) {
						fis = new FileInputStream(userFile);
						jamProperties.load(fis);
						userLoadWarning =
							"Cannot find file "
								+ FILE_USER
								+ " file in directory user.home = "
								+ userHome;
						userLoadMessage =
							"Read user properties from file " + userFile.getPath();
					}
					//try  DEFAULT_JAM_HOME		    
				} else {
					userFile = new File(
						DEFAULT_JAM_HOME,FILE_USER);
					fileName = userFile.getPath();
					if (userFile.exists()) {
						fis = new FileInputStream(userFile);
						jamProperties.load(fis);
						userLoadWarning =
							"Cannot find file "
								+ FILE_USER
								+ " file in user home and jam.home not defined";
						userLoadMessage =
							"Read user properties from file " + userFile.getPath();
						//use default properties			   
					} else {
						userLoadWarning =
							"Cannot find user config file "
								+ FILE_USER
								+ ", in jam home or user home directories";
						userLoadMessage = "Using default user properties";
					}
				}
			}
		} catch (FileNotFoundException fnfe) {
			loadError =
				"Jam Configuration file not found "
					+ fileName
					+ " [JamProperties]";
			if (msgHandler != null) {
				msgHandler.errorOutln(loadError);
			} else {
				//FIXME are these print out needed
				System.err.println("Error: " + loadError);
				System.err.println("Exception: " + fnfe.getMessage());
			}
		} catch (IOException ioe) {
			loadError =
				"Could not read Configuration file "
					+ fileName
					+ " [JamProperties]";
			if (msgHandler != null) {
				msgHandler.errorOutln(loadError);
			} else {
				//FIXME are these print out needed	    
				System.err.println("Error: " + loadError);
				System.err.println("Exception: " + ioe.getMessage());
			}
		}
	}

	/**
	 * Output message from loading propreties
	 */
	public void outputMessages(MessageHandler msgHandler) {
		if (loadError != NO_ERRORS) {
			msgHandler.warningOutln(loadError);
		}
		if (configLoadWarning != NO_WARNINGS) {
			msgHandler.warningOutln(configLoadWarning);
		}
		msgHandler.messageOutln(configLoadMessage);
		if (userLoadWarning != NO_WARNINGS) {
			msgHandler.warningOutln(userLoadWarning);
		}
		msgHandler.messageOutln(userLoadMessage);
	}

	/**
	 * Message about where config was loaded from
	 */
	public String configLoadMessage() {
		return configLoadMessage;
	}

	/**
	 * Message about where user was loaded from
	 */
	public String userLoadMessage() {
		return userLoadMessage;
	}

	/**
	 * warning message from loading
	 */
	public String configLoadWarning() {
		return configLoadWarning;
	}

	/**
	 * warning message from loading
	 */
	public String userLoadWarning() {
		return userLoadWarning;
	}

	/**
	 * Error message from loading
	 */
	public String loadError() {
		return loadError;
	}

	/**
	 * Load default configuration properties.
	 */
	private void loadDefaultConfig() {
		jamProperties.setProperty(JAM_HOME,(new File(DEFAULT_JAM_HOME)).getPath());
		jamProperties.setProperty(HOST_IP,"calvin");
		jamProperties.setProperty(HOST_PORT_SEND,"5003");
		jamProperties.setProperty(HOST_PORT_SEND,"5002");
		jamProperties.setProperty(HOST_PORT_RECV,"5005");
		jamProperties.setProperty(TARGET_IP,"hobbes");
		jamProperties.setProperty(TARGET_PORT,"5002");
		jamProperties.setProperty(HOST_DATA_IP,"calvin-data");
		jamProperties.setProperty(HOST_DATA_PORT_RECV,"10205");
		setProperty(NO_FILL_2D,false);
	}

	/**
	 * Load default user properties.
	 */
	private void loadDefaultUser() {
		jamProperties.setProperty(EXP_NAME,"default_");
		jamProperties.setProperty(SORT_ROUTINE,"jam.sort.Example");
		jamProperties.setProperty(SORT_CLASSPATH,DEFAULT_SORT_CLASSPATH);
		jamProperties.setProperty(HIST_PATH,(new File(userHome,"spectra")).getPath());
		jamProperties.setProperty(EVENT_INPATH,
		(new File(userHome,"events")).getPath());
		jamProperties.setProperty(EVENT_OUTPATH,
		(new File(userHome,"presort")).getPath());
		jamProperties.setProperty(EVENT_OUTFILE,"sortout.evn");
		jamProperties.setProperty(TAPE_DEV,"/dev/rmt/0");
		jamProperties.setProperty(LOG_PATH,(new File(userHome)).getPath());
		jamProperties.setProperty(EVENT_INSTREAM,
		"jam.sort.stream.YaleCAEN_InputStream");
		jamProperties.setProperty(EVENT_OUTSTREAM,"jam.sort.stream.YaleOutputStream");
		jamProperties.setProperty(GRADIENT_SCALE,FALSE);
	}
	
	static public void setProperty(String key, boolean val){
		if (val){
			jamProperties.setProperty(key,TRUE);
		} else {
			jamProperties.setProperty(key,FALSE);
		}
	}
	
	static public boolean getBooleanProperty(String key){
		return jamProperties.getProperty(key).equals(TRUE);
	} 
}
