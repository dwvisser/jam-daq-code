package jam.io.hdf;

import static jam.io.hdf.Constants.DFTAG_FID;

import java.nio.ByteBuffer;

/**
 * Class to represent an HDF <em>File Identifier</em> data object. The label is
 * meant to be a user supplied title for the file.
 * 
 * @version 0.5 December 98
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @since JDK1.1
 */
final class FileIdentifier extends AbstractData {

	FileIdentifier() {
		super(DFTAG_FID); // sets tag
	}

	FileIdentifier(final String label) {
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
