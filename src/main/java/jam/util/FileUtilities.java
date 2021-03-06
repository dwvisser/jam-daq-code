package jam.util;

import com.google.inject.Singleton;

import javax.swing.*;
import java.io.File;

/**
 * Utility class for file dialogs.
 * 
 * @author <a href=mailto:dwvisser@users.sourceforge.net>Dale Visser </a>
 */
@Singleton
public final class FileUtilities {

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

	/**
	 * Takes a file and extension and appends the extension to the file name. If
	 * the file has no extension, the extension is appended. If it does have an
	 * extension, it is replaced. By extension, we mean the usual
	 * <code>DOS/Unix</code> filenaming convention:
	 * 
	 * @param fileIn
	 *            the file to add extension to.
	 * @param extension
	 *            file extension; everything up to any decimal is thrown away
	 * @param mode
	 *            <code>FORCE</code>,<code>APPEND_ONLY</code> or
	 *            <code>NO_CHANGE</code>
	 * @return a filename with the desired extension
	 * @throws IllegalArgumentException
	 *             if an unrecognized mode is given
	 * 
	 */
	public File changeExtension(final File fileIn, final String extension,
			final int mode) {
		final String path = fileIn.getParent();
		final String fileName = changeExtension(fileIn.getName(), extension,
				mode);
		final String fileFullName = path + File.separator + fileName;
		return new File(fileFullName);
	}

	/**
	 * Takes a file name and extension and appends the extension to the file
	 * name. If the filename has no extension, the extension is appended. If it
	 * does have an extension, it is replaced. By extension, we mean the usual
	 * <code>DOS/Unix</code> filenaming convention:
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
			if (index >= 0) { // period occurs in extension
				extNoPeriod = extension.substring(index + 1);
			}
			index = fileName.indexOf('.');
			if (index >= 0) { // period occurs in filename
				if (mode == FORCE) {
					rval = fileName.substring(0, index + 1) + extNoPeriod;
				}
			} else { // add period
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
		index = fileNameIn.lastIndexOf('.');
		// Extension 3 or less characters, index -1 if not found
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

	/**
	 * Check if a file already exist and display dialog to confirm overwritting
	 * of the file
	 * 
	 * @param file
	 *            The file to check exists
	 * @return True if file does not exist or overwrite confirmed
	 */
	public boolean overWriteExistsConfirm(final File file) {
		final boolean writeConfirm = !file.exists() || JOptionPane.YES_OPTION == JOptionPane
                .showConfirmDialog(null, "Replace the existing file? \n"
                                + file.getName(), "Save " + file.getName(),
                        JOptionPane.YES_NO_OPTION);
		if (writeConfirm) {
			// we've confirmed overwrite with the user.
			file.delete();
		}

		return writeConfirm;
	}

	/**
	 * Get a directory corresponding to the given path string.
	 * @param dir path to get a directory for
	 * @return given file if a directory, parent directory otherwise, null if
	 *         given file doesn't exist
	 */
	File getDir(final String dir) {
		File result = new File(dir);
		if (result.exists()) {
			if (!result.isDirectory()) {
				result = result.getParentFile();
			}
		} else {
			result = null;// NOPMD
		}
		return result;
	}
}
