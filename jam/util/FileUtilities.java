package jam.util;

/**
 * Utility class for file dialogs.
 * 
 * @author <a href=mailto:dale@visser.name>Dale Visser</a>
 */
public class FileUtilities {

	public static final int FORCE = 0;
	public static final int APPEND_ONLY = 1;
	public static final int NO_CHANGE = 2;

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
	  * @throws IllegalArgumentException if an unrecognized mode is given
	  */
	public static String setExtension(
		String fileName,
		String extension,
		int mode) {
		String extNoPeriod = extension;
		String rval=fileName;

		if (mode == NO_CHANGE)
			return fileName;
		if (mode != FORCE && mode != APPEND_ONLY && mode != NO_CHANGE) {
			throw new IllegalArgumentException("Invalid call to setExtension().");
		}
		int index = extension.indexOf('.');
		if (index >= 0) { //period occurs in extension
			extNoPeriod = extension.substring(index + 1);
		}
		index = fileName.indexOf('.');
		if (index >= 0) { //period occurs in filename
			if (mode == FORCE) {
				rval = fileName.substring(0, index + 1) + extNoPeriod;
			} 
		} else { //add period
			rval = fileName + '.' + extNoPeriod;
		}
		return rval;
	}
}
