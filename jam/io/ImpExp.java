package jam.io;
import jam.data.Histogram;
import jam.global.MessageHandler;

import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
/** 
 * Abstract class for importing and exporting histograms.
 * Gives you methods to open files and returns either an input
 * stream or an output stream. 
 * You write a method <code>openFile</code>, which calls <code>openFile(String msg,String ext)</code>
 * which in turn calls your implementation of <code>readHist()</code>.
 *
 * @author  Ken Swartz
 * @version 0.50
 * @see	    #openFile
 */
public abstract class ImpExp {

	/**
	 * size of read/write buffers
	 */
	protected final static int BUFFER_SIZE = 256 * 256 * 4;

	/**
	 * load or save option on file dialogs
	 */
	private static final int LOAD = 137;
	private static final int SAVE = 314;

	/**
	 * so the programmer may set display options
	 */
	protected final Frame frame;

	/**
	 * to be used for printing messages to the <bold>jam</bold> msgHandler
	 */
	protected final MessageHandler msgHandler;

	/**
	 * the last file accessed, null goes to users home directory
	 */
	protected File lastFile = null;

	/**
	 * Class constructor.
	 *
	 * @param	frame	the <bold>jam</bold> display frame
	 * @param	msgHandler	the <bold>jam</bold> msgHandler
	 *
	 * @see	jam.global.MessageHandler
	 */
	public ImpExp(Frame frame, MessageHandler msgHandler) {
		this.frame = frame;
		this.msgHandler = msgHandler;
	}
	
	/**
	 * Default constructor so that it may be launched dynamically
	 * for batch exports.
	 */
	public ImpExp(){
		this(null, null);
	}

	/**
	 * Opens a file for reading. Subclasses generally should call <code>openFile(msg,ext)</code>
	 * which is already implemented in <code>ImpExp</code>.
	 *
	 * @exception   ImpExpException all ImpExpExceptions go to the msgHandler
	 * @param in file to open, if null we provide a dialog
	 */
	public abstract boolean openFile(File in) throws ImpExpException;

	/**
	  * Saves a histogram or all histograms.  Typically, the implementation calls
	  * the inherited <code>ImpExp</code> method <code>saveFile(String,String,Histogram)</code>
	  * with appropriate arguments.
	  *
	  * @param	    hist	    the histogram to be saved
	  * @exception  ImpExpException    all exceptions given to <code>ImpExpException</code> go to the msgHandler
	  */
	public abstract void saveFile(Histogram hist) throws ImpExpException;

	/**
	 * Gets a short description of the file format this class works with.
	 * 
	 * @return short description of the file format]
	 */
	public abstract String getFormatDescription();

	/**
	 * Reads data from the passed <code>InputStream</code>. The
	 * specific implementations <bold>may</bold> use this to read specific 
	 * formats.
	 *
	 * @param	    inStream	    the stream to read the histogram from
	 * @exception   ImpExpException    all exceptions given to <code>ImpExpException</code> go to the msgHandler
	 */
	abstract protected void readData(InputStream inStream)
		throws ImpExpException;

	/**
	 * Writes the passed <code>Histogram</code> to the passed <code>OutputStream</code>. The
	 * specific implementations <bold>may</bold> use this to write specific 
	 * formats.
	 *
	 * @param	    outStream	    the stream to write the histogram to
	 * @exception   ImpExpException    all exceptions given to <code>ImpExpException</code> go to the msgHandler
	 */
	abstract protected void writeHist(OutputStream outStream, Histogram hist)
		throws ImpExpException;

	/**
	 * Opens a file with a specified dialog box title bar and file extension.
	 * It is usually called by <code>openFile</code> in subclasses of <code>ImpExp</code>.
	 *
	 * @param	    msg		    text to go on title bar of dialog box
	 * @param	    extension	    file extension to suggest to user
	 * @return	whether file was successfully read
	 * @exception   ImpExpException    all exceptions given to <code>ImpExpException</code> go to the msgHandler
	 */
	protected boolean openFile(File in, String msg) {
		File inFile=in;
		boolean rval=false; //default return value
		try {
			/* open file dialog */    		
			if (in==null){
				inFile=getFileOpen(msg);
			}
			if (inFile != null) { // if Open file was  not canceled
				lastFile = inFile;
				FileInputStream inStream = new FileInputStream(inFile);
				BufferedInputStream inBuffStream = new BufferedInputStream(inStream, BUFFER_SIZE);
				if (msgHandler != null) msgHandler.messageOut(
					msg + " " + getFileName(inFile),
					MessageHandler.NEW);
				/* implementing class implement following method */
				readData(inBuffStream);
				if (msgHandler != null) msgHandler.messageOut(" done!", MessageHandler.END);
				inBuffStream.close();
				rval = true;
			}
		} catch (IOException ioe) {
			msgHandler.errorOutln("Problem handling file \""+inFile.getPath()+"\": "+ioe.getMessage());
		} catch (ImpExpException iee) {
			msgHandler.errorOutln("Problem while importing or exporting: "+iee.getMessage());
		}
		return rval;
	}

	/**
	 * Save a specific histogram using a file dialog box.  The programmer supplies a file dialog box title,
	 * a file extension, and the histogram to save.  Implementers should call this from
	 * <code>saveFile(Histogram)</code>.
	 * 
	 * @param	    msg		    text to go on title bar of dialog box
	 * @param	    extension	    file extension to suggest to user
	 * @param	    hist	    histogram to be saved
	 * @see	    #saveFile(Histogram)
	 * @exception   ImpExpException    all exceptions given to <code>ImpExpException</code> display on the msgHandler
	 */
	protected void saveFile(String msg, Histogram hist)
		throws ImpExpException {
		File outFile = getFileSave(msg);
		try {// open file dialog		 
			if (outFile != null) { // if Save file dialog was  not canceled
				lastFile = outFile;
				FileOutputStream outStream = new FileOutputStream(outFile);
				BufferedOutputStream outBuffStream =
					new BufferedOutputStream(outStream, BUFFER_SIZE);
				if (msgHandler != null) msgHandler.messageOut(
					msg + " " + getFileName(outFile),
					MessageHandler.NEW);
				writeHist(outBuffStream, hist);
				outBuffStream.flush();
				outStream.flush();
				outStream.close();
				if (msgHandler != null) msgHandler.messageOut(" done!", MessageHandler.END);
			}
		} catch (IOException io) {
			throw new ImpExpException("Creating file [ImpExp]");
		}
	}

	public void saveFile(File outFile, Histogram hist) throws ImpExpException {
		try {
			FileOutputStream outStream = new FileOutputStream(outFile);
			BufferedOutputStream outBuffStream =
				new BufferedOutputStream(outStream, BUFFER_SIZE);
			writeHist(outBuffStream, hist);
			outBuffStream.flush();
			outStream.close();
			if (msgHandler != null) msgHandler.messageOut(" done!", MessageHandler.END);
		} catch (IOException io) {
			throw new ImpExpException("Creating file [ImpExp]");
		}
	}

	/**
	 * Open a file to read from.
	 *
	 * @param	    msg		    text to go on title bar of dialog box
	 * @param	    extension	    file extension to suggest to user
	 * @return			    a <code>File</code> to read from
	 */
	protected File getFileOpen(String msg)
		throws ImpExpException {
		return getFile(msg, ImpExp.LOAD);
	}

	/**
	 * Get a file to save data to.
	 *
	 * @param	    msg		    text to go on title bar of dialog box
	 * @param	    extension	    file extension to suggest to user
	 * @return			    a <code>File</code> to save to
	*/
	protected File getFileSave(String msg)
		throws ImpExpException {
		return getFile(msg, ImpExp.SAVE);
	}

	/**
	 * Get a file from a file dialog box.
	 * See Java 1.1 AWT Reference (J.Zukowski) page 245 and
	 * Nutshell Java Examples page 162.
	 *
	 * @param	    msg		    text to go on title bar of dialog box
	 * @param	    extension	    file extension to suggest to user
	 * @param	    state	    <code>ImpExp.LOAD</code> or <code>ImpExp.SAVE</code>
	 * @return			    a <code>File</code> chosen by the user, null if dialog cancelled
	 */
	protected File getFile(String msg, int state)
		throws ImpExpException {
		File file = null;
		int option;
		JFileChooser jfile = new JFileChooser(lastFile);
		jfile.setDialogTitle(msg);
		jfile.setFileFilter(getFileFilter());
		if (state == LOAD) {
			option = jfile.showOpenDialog(frame);
		} else if (state == SAVE) {
			option = jfile.showSaveDialog(frame);
		} else {
			throw new ImpExpException(
				getClass().getName()
					+ "getFile() called with state = "
					+ state);
		}
		/* don't do anything if it was cancel */
		if (option == JFileChooser.APPROVE_OPTION) {
			file = jfile.getSelectedFile();
		}
		return file;
	}
	
	abstract protected FileFilter getFileFilter();
	abstract protected String getDefaultExtension();

	/**
	 * Return the name of the file that was entered using the <code>FileDialog</code> box.
	 *
	 * @see #getFile 
	 */
	protected String getFileName(File file) {
		String rval=null;//default return value
		if (file != null) {
			rval = file.getName();
		}
		return rval;
	}

	public File getLastFile() {
		return lastFile;
	}
	
	/**
	 * Returns whether this class can export files to disk.
	 * 
	 * @return whether exporting is possible
	 */
	public abstract boolean canExport();
	
	/**
	 * Returns whether this class is appropriate to use for
	 * the batch export dialog.
	 * 
	 * @return whether batch export is allowed 
	 */
	abstract boolean batchExportAllowed();
}
