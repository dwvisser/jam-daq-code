package jam.io.hdf;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.swing.JOptionPane;

/**
 * Class to represent an HDF <em>File Identifier</em> data object.  The label is meant to be a user supplied 
 * title for the file.
 *
 * @version	0.5 December 98
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @since       JDK1.1
 */
final class FileIdentifier extends DataObject {

	FileIdentifier(String label) {
		super(DFTAG_FID); //sets tag
		int byteLength = label.length();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(byteLength);
		final DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeBytes(label);
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(null,ioe.getMessage(),
			getClass().getName(),JOptionPane.ERROR_MESSAGE);
		}
		bytes = baos.toByteArray();
	}

	/**
	 * Implementation of <code>DataObject</code> abstract method.
	 *
	 * @exception HDFException thrown if there is a problem interpreting the bytes
	 */
	public void interpretBytes() throws HDFException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		try {
			final byte [] temp = new byte[bytes.length];
			dis.read(temp);
		} catch (IOException e) {
			throw new HDFException(
				"Problem interpreting FID.",e);
		}
	}
}
