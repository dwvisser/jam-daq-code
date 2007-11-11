package jam.io;

import jam.data.Group;
import jam.data.Histogram;
import jam.global.JamStatus;
import jam.util.FileUtilities;

import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * Abstract class for importing and exporting histograms. Gives you methods to
 * open files and returns either an input stream or an output stream. You write
 * a method <code>openFile</code>, which calls
 * <code>openFile(String msg,String ext)</code> which in turn calls your
 * implementation of <code>readHist()</code>.
 * 
 * @author Ken Swartz
 * @version 0.50
 * @see #openFile(File)
 */
public abstract class AbstractImpExp {

	/**
	 * For logging messages.
	 */
	protected static final Logger LOGGER = Logger
			.getLogger(AbstractImpExp.class.getPackage().getName());

	/**
	 * size of read/write buffers
	 */
	protected final static int BUFFER_SIZE = 256 * 256 * 4;

	/**
	 * load or save option on file dialogs
	 */
	private static final int LOAD = 137;

	private static final int SAVE = 314;

	private static final JamStatus STATUS = JamStatus.getSingletonInstance();

	/**
	 * so the programmer may set display options
	 */
	protected transient final Frame frame;

	private static final Object LASTFILE_MON = new Object();

	private transient final String lastFileKey;

	private static final Preferences PREFS = Preferences
			.userNodeForPackage(AbstractImpExp.class);

	/**
	 * the last file accessed, null goes to users home directory
	 */
	protected File lastFile;

	/**
	 * Group to import data into
	 */
	protected transient Group importGroup;

	/**
	 * Default constructor so that it may be launched dynamically for batch
	 * exports.
	 */
	public AbstractImpExp() {
		super();
		lastFileKey = getClass().getName() + "_LastValidFile";
		lastFile = new File(PREFS.get(lastFileKey, System
				.getProperty("user.dir")));
		frame = STATUS.getFrame();
	}

	/**
	 * if true, don't output messages to console
	 */
	protected transient boolean silent = false;

	/**
	 * Don't output messages to msgHandler
	 */
	public void setSilent() {
		silent = true;
	}

	/**
	 * Opens a file for reading. Subclasses generally should call
	 * <code>openFile(msg,ext)</code> which is already implemented in
	 * <code>ImpExp</code>.
	 * 
	 * @param file
	 *            to open, if null we provide a dialog
	 * @return <code>true</code> if successful
	 * @exception ImpExpException
	 *                all ImpExpExceptions go to the msgHandler
	 */
	public abstract boolean openFile(File file) throws ImpExpException;

	/**
	 * Saves a histogram or all histograms. Typically, the implementation calls
	 * the inherited <code>ImpExp</code> method
	 * <code>saveFile(String,String,Histogram)</code> with appropriate
	 * arguments.
	 * 
	 * @param hist
	 *            the histogram to be saved
	 * @exception ImpExpException
	 *                all exceptions given to <code>ImpExpException</code> go
	 *                to the msgHandler
	 */
	public abstract void saveFile(Histogram hist) throws ImpExpException;

	/**
	 * Gets a short description of the file format this class works with.
	 * 
	 * @return short description of the file format]
	 */
	public abstract String getFormatDescription();

	/**
	 * Reads data from the passed <code>InputStream</code>. The specific
	 * implementations <bold>may</bold> use this to read specific formats.
	 * 
	 * @param inStream
	 *            the stream to read the histogram from
	 * @exception ImpExpException
	 *                all exceptions given to <code>ImpExpException</code> go
	 *                to the msgHandler
	 */
	abstract protected void readData(InputStream inStream)
			throws ImpExpException;

	/**
	 * Writes the passed <code>Histogram</code> to the passed
	 * <code>OutputStream</code>. The specific implementations <bold>may</bold>
	 * use this to write specific formats.
	 * 
	 * @param outStream
	 *            the stream to write the histogram to
	 * @param hist
	 *            to write
	 * @exception ImpExpException
	 *                all exceptions given to <code>ImpExpException</code> go
	 *                to the msgHandler
	 */
	abstract protected void writeHist(OutputStream outStream, Histogram hist)
			throws ImpExpException;

	/**
	 * Opens a file with a specified dialog box title bar and file extension. It
	 * is usually called by <code>openFile</code> in subclasses of
	 * <code>ImpExp</code>.
	 * 
	 * @param file
	 *            the file to open
	 * @param msg
	 *            text to go on title bar of dialog box
	 * @return whether file was successfully read
	 */
	protected boolean openFile(final File file, final String msg) {
		boolean rval = false; // default return value
		BufferedInputStream inBuffStream = null;
		try {
			final File inFile = (file == null) ? getFileOpen(msg) : file;
			if (inFile != null) { // if Open file was not canceled
				// Create group
				final FileUtilities fileUtil = FileUtilities.getInstance();
				final String groupName = fileUtil
						.removeExtensionFileName(inFile.getName());
				importGroup = Group.createGroup(groupName, Group.Type.FILE);
				setLastFile(inFile);
				final FileInputStream inStream = new FileInputStream(inFile);
				inBuffStream = new BufferedInputStream(inStream, BUFFER_SIZE);
				if (!silent) {
					LOGGER.info(msg + " " + getFileName(inFile));
				}
				/* implementing class implement following method */
				readData(inBuffStream);
				if (!silent) {
					LOGGER.info("File import done.");
				}
				inBuffStream.close();
				rval = true;
			}
		} catch (IOException ioe) {
			LOGGER.log(Level.SEVERE, "Problem handling file: "
					+ ioe.getMessage(), ioe);
		} catch (ImpExpException iee) {
			LOGGER.log(Level.SEVERE, "Problem while importing or exporting: "
					+ iee.getMessage(), iee);
		} finally {
			if (inBuffStream != null) {
				try {
					inBuffStream.close();
				} catch (IOException ioe) {
					LOGGER.log(Level.SEVERE, "Problem closing file: "
							+ ioe.getMessage(), ioe);
				}
			}
		}
		return rval;
	}

	/**
	 * Save a specific histogram using a file dialog box. The programmer
	 * supplies a file dialog box title, a file extension, and the histogram to
	 * save. Implementers should call this from <code>saveFile(Histogram)</code>.
	 * 
	 * @param msg
	 *            text to go on title bar of dialog box
	 * @param hist
	 *            histogram to be saved
	 * @see #saveFile(Histogram)
	 * @exception ImpExpException
	 *                all exceptions given to <code>ImpExpException</code>
	 *                display on the msgHandler
	 */
	protected void saveFile(final String msg, final Histogram hist)
			throws ImpExpException {
		final File outFile = getFileSave(msg);
		try {// open file dialog
			if (outFile != null) { // if Save file dialog was not canceled
				setLastFile(outFile);
				final FileOutputStream outStream = new FileOutputStream(outFile);
				final BufferedOutputStream buffStream = new BufferedOutputStream(
						outStream, BUFFER_SIZE);
				if (!silent) {
					LOGGER.info(msg + " " + getFileName(outFile));
				}
				writeHist(buffStream, hist);
				buffStream.flush();
				outStream.flush();
				outStream.close();
				if (!silent) {
					LOGGER.info("File save done.");
				}
			}
		} catch (IOException io) {
			throw new ImpExpException("Creating file [ImpExp]", io);
		}
	}

	/**
	 * Save the given histogram to the given file.
	 * 
	 * @param outFile
	 * @param hist
	 * @throws ImpExpException
	 */
	public void saveFile(final File outFile, final Histogram hist)
			throws ImpExpException {
		try {
			final FileOutputStream outStream = new FileOutputStream(outFile);
			final BufferedOutputStream buffStream = new BufferedOutputStream(
					outStream, BUFFER_SIZE);
			writeHist(buffStream, hist);
			buffStream.flush();
			outStream.close();
			if (!silent) {
				LOGGER.info("File save done.");
			}
		} catch (IOException io) {
			throw new ImpExpException("Creating file [ImpExp]", io);
		}
	}

	/**
	 * Open a file to read from.
	 * 
	 * @param msg
	 *            text to go on title bar of dialog box
	 * @return a <code>File</code> to read from
	 */
	protected File getFileOpen(final String msg) {
		return getFile(msg, AbstractImpExp.LOAD);
	}

	/**
	 * Get a file to save data to.
	 * 
	 * @param msg
	 *            text to go on title bar of dialog box
	 * @return a <code>File</code> to save to
	 */
	protected File getFileSave(final String msg) {
		return getFile(msg, AbstractImpExp.SAVE);
	}

	/**
	 * Get a file from a file dialog box. See Java 1.1 AWT Reference
	 * (J.Zukowski) page 245 and Nutshell Java Examples page 162.
	 * 
	 * @param msg
	 *            text to go on title bar of dialog box
	 * @param state
	 *            <code>ImpExp.LOAD</code> or <code>ImpExp.SAVE</code>
	 * @return a <code>File</code> chosen by the user, null if dialog
	 *         cancelled
	 */
	protected File getFile(final String msg, final int state) {
		File file = null;
		int option;
		final JFileChooser jfile = new JFileChooser(getLastFile());
		jfile.setDialogTitle(msg);
		jfile.setFileFilter(getFileFilter());
		if (state == LOAD) {
			option = jfile.showOpenDialog(frame);
		} else if (state == SAVE) {
			option = jfile.showSaveDialog(frame);
		} else {
			throw new IllegalArgumentException(getClass().getName()
					+ "getFile() called with state = " + state);
		}
		/* don't do anything if it was cancel */
		if (option == JFileChooser.APPROVE_OPTION) {
			file = jfile.getSelectedFile();
		}
		return file;
	}

	/**
	 * Defines the file filter to be used by the file dialogs.
	 * 
	 * @return file filter used by dialogs
	 */
	abstract protected FileFilter getFileFilter();

	/**
	 * Defines a default file extension for the files we want to access.
	 * 
	 * @return default file extension
	 */
	abstract protected String getDefaultExtension();

	/**
	 * Return the name of the file that was entered using the
	 * <code>FileDialog</code> box.
	 * 
	 * @param file
	 *            to get name of
	 * @return name of file or <code>null</code> if it doesn't exist
	 * @see #getFile
	 */
	protected String getFileName(final File file) {
		String rval = null;// default return value
		if (file != null) {
			rval = file.getName();
		}
		return rval;
	}

	/**
	 * Get the last file accessed by this instance.
	 * 
	 * @return the last file accessed
	 */
	public File getLastFile() {
		synchronized (LASTFILE_MON) {
			return lastFile;
		}
	}

	/**
	 * Set the last file accessed.
	 * 
	 * @param file
	 *            last file accessed
	 */
	protected void setLastFile(final File file) {
		synchronized (LASTFILE_MON) {
			lastFile = file;
			PREFS.put(lastFileKey, file.getAbsolutePath());
		}
	}

	/**
	 * Returns whether this class can export files to disk.
	 * 
	 * @return whether exporting is possible
	 */
	public abstract boolean canExport();

	/**
	 * Returns whether this class is appropriate to use for the batch export
	 * dialog.
	 * 
	 * @return whether batch export is allowed
	 */
	abstract boolean batchExportAllowed();
}
