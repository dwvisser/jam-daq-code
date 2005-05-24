package jam.util;

/**
 * Utility class for file dialogs.
 * 
 * @author <a href=mailto:dale@visser.name>Dale Visser </a>
 */
public class FileUtilities {

	/**
	 * Take any existing extension and replace it with the given one.
	 */
	public static final int FORCE = 0;

	/**
	 * Append the given extension regardless.
	 */
	public static final int APPEND_ONLY = 1;

	/**
	 * Don't change the given filename.
	 */
	public static final int NO_CHANGE = 2;
	
	private static final FileUtilities instance = new FileUtilities();

	public static FileUtilities getInstance(){
		return instance;
	}

	/**
	 * Takes a file name and extension and makes the file name to have the
	 * extension. If the filename has no extension, the extension is appended.
	 * If it does have an extension, it is replaced. By extension, we mean the
	 * usual <code>DOS/Unix</code> filenaming convention:
	 * <P>
	 * <code><i>filename</i>.ext</code><br>
	 * where <code>ext</code> is usually three characters indicating filetype.
	 * 
	 * @param fileName
	 *            with or without an extension already
	 * @param extension
	 *            file extension; everything up to any decimal is thrown away
	 * @param mode
	 *            <code>FORCE</code>,<code>APPEND_ONLY</code> or
	 *            <code>NO_CHANGE</code>
	 * @return a filename with the desired extension
	 * @throws IllegalArgumentException
	 *             if an unrecognized mode is given
	 * @see #APPEND_ONLY
	 * @see #FORCE
	 * @see #NO_CHANGE
	 */
	public String changeExtension(final String fileName,
			final String extension, final int mode) {
		String extNoPeriod = extension;
		String rval = fileName;

		if (mode != NO_CHANGE) {
			if (mode != FORCE && mode != APPEND_ONLY) {
				throw new IllegalArgumentException(
						"Invalid call to setExtension().");
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
		}
		return rval;
	}

	/**
	 * Remove extension from file name
	 * 
	 * @param fileNameIn
	 *            file name in
	 * @return fileName without extension
	 */
	public String removeExtensionFileName(final String fileNameIn) {
		String fileName;
		int index;
		index = fileNameIn.lastIndexOf(".");
		//Extension 3 or less characters, index -1 if not found
		if (index > 0) {
			if (index >= fileNameIn.length() - 4) {
				fileName = fileNameIn.substring(0, index);
			} else {
				fileName = fileNameIn;
			}
		} else {
			fileName = fileNameIn;
		}

		return fileName;
	}
}
