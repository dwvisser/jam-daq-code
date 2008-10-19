package jam.io.hdf;

import static jam.io.hdf.Constants.DFTAG_FD;

import java.nio.ByteBuffer;

/**
 * Class to represent an HDF <em>File Description</em> data object. The text is
 * meant to be a description of the contents of the file.
 * 
 * @version 0.5 December 98
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
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
