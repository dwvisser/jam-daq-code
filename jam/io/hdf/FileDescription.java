package jam.io.hdf;

import java.nio.ByteBuffer;

import static jam.io.hdf.Constants.DFTAG_FD;

/**
 * Class to represent an HDF <em>File Description</em> data object. The text is
 * meant to be a description of the contents of the file.
 * 
 * @version 0.5 December 98
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @since JDK1.1
 */
final class FileDescription extends AbstractData {

	FileDescription() {
		super(DFTAG_FD);
	}

	FileDescription(final String label) {
		this();
		final int byteLength = label.length();
		bytes = ByteBuffer.allocate(byteLength);
		putString(label);
	}

	/**
	 * Implementation of <code>DataObject</code> abstract method.
	 */
	@Override
	public void interpretBytes() {
		// nothing to do
	}
}
