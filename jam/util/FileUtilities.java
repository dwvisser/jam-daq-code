package jam.util;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;

/**
 * AWT utility class for easy creation of file dialogs.
 * Maintains a memory of the last file and directory used.
 * 
 * @author <a href=mailto:dale@visser.name>Dale Visser</a>
 */
public class FileUtilities {

	private Frame frame;
	private String fileName;
	private String directoryName;
	public static final int FORCE = 0;
	public static final int APPEND_ONLY = 1;
	public static final int NO_CHANGE = 2;

	public FileUtilities(Frame frame) {
		this.frame = frame;
		fileName = null;
		directoryName = null;
	}

	public FileUtilities(Frame frame, String dname) {
		this.frame = frame;
		fileName = null;
		directoryName = dname;
	}

	/**
	 * Get a file, method also used by import and export
	 *	 See books AWT reference page 245 and
	 *		   Nutshell Java examples page 162
	 */
	public File getFile(String msg, String extension, int state, int mode)
		throws UtilException {

		File fileIn = null;

		FileDialog fd = new FileDialog(frame, msg, state);

		//use previous file and directory as default
		if ((fileName) != null) {
			fd.setFile(fileName);
		} else {
			fd.setFile(extension);
		}
		if (directoryName != null) {
			fd.setDirectory(directoryName);
		}

		//show file dialoge box to get file     	    
		fd.show();
		directoryName = fd.getDirectory(); //save current directory
		fileName = fd.getFile();
		fd.dispose();

		if (fileName != null) {

			fileName = setExtension(fileName, extension, mode);
			fileIn = new File(directoryName, fileName);
		} else {
			fileIn = null;
		}
		return fileIn;
	}

	/**
	 * Get a file, to open from
	 */
	public File getFileOpen(String msg, String extension, int mode)
		throws UtilException {
		return getFile(msg, extension, FileDialog.LOAD, mode);
	}

	/**
	 * Get a file, to save to
	 */

	public File getFileSave(String msg, String extension, int mode)
		throws UtilException {
		return getFile(msg, extension, FileDialog.SAVE, mode);
	}

	/**
	  * Takes a file name and extension and makes the file name to have the extension.
	  * If the filename has no extension, the extension is appended.  If it does have an
	  * extension, it is replaced.  By extension, we mean the usual <code>DOX/Unix</code>
	  * filenaming convention:<P>
	  * <code><i>filename</i>.ext</code><br>
	  * where <code>ext</code> is usually three characters indicating filetype.
	  * 
	  * @param	fileName    with or without an extension already
	  * @param	extension   file extension; everything up to any decimal is thrown away
	  * @param   mode	    FORCE if force a change, APPEND_ONLY if only append and not replace, NO_CHANGE if ignore
	  * @return		    a filename with the desired extension
	  */
	public static String setExtension(
		String fileName,
		String extension,
		int mode)
		throws UtilException {
		String ext;
		int index;

		if (mode == NO_CHANGE)
			return fileName;
		if (mode != FORCE && mode != APPEND_ONLY && mode != NO_CHANGE) {
			throw new UtilException("Invalid call to setExtension().");
		}
		index = extension.indexOf(".");
		if (index == -1) { //no period occurs in extension
			ext = extension;
		} else { //strip up to and including period
			ext = extension.substring(index + 1);
		}
		index = fileName.indexOf(".");
		if (index == -1) { //no period occurs in fileName
			return fileName + "." + ext;
		} else { //strip extension
			if (mode == FORCE) {
				return fileName.substring(0, index + 1) + ext;
			} else {
				return fileName;
			}
		}
	}
}
