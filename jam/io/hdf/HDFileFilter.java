package jam.io.hdf;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static jam.io.hdf.Constants.HDF_HEADER;

/**
 * Filters only HDF files for file dialogs.
 * 
 * @author <a href=mailto:dwvisser@users.sourceforge.net>Dale Visser</a>
 */
public class HDFileFilter extends FileFilter implements java.io.FileFilter {

	private final transient boolean option;

	/**
	 * Constructs a filter for HDF files. It checks the first 4 bytes of the
	 * file.
	 * 
	 * @param showDir
	 *            whether to show directories as well
	 * @see Constants#HDF_HEADER
	 */
	public HDFileFilter(final boolean showDir) {
		super();
		option = showDir;
	}

	@Override
	public boolean accept(final File file) {
		boolean result;// default return value
		if (file.isDirectory()) {
			result = option;
		} else {
			try {
				final RandomAccessFile raf = new RandomAccessFile(file, "r");
				final int temp = raf.readInt();
				raf.close();
				result = (temp == HDF_HEADER);
			} catch (IOException e) {
				result = false;
			}
		}
		return result;
	}

	@Override
	public String getDescription() {
		return "Hierarchical Data  Format v4.1r2";
	}

}