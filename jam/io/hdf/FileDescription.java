package jam.io.hdf;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Class to represent an HDF <em>File Description</em> data object.  The text is meant to be a description of
 * the contents of the file.
 *
 * @version	0.5 December 98
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @since       JDK1.1
 */
final class FileDescription extends DataObject {

	/**
	 * Object being labelled.
	 */
	DataObject object;

	FileDescription(String label) throws HDFException {
		super(DFTAG_FD); //sets tag
		int byteLength = label.length();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(byteLength);
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeBytes(label);
		} catch (IOException ioe) {
			throw new HDFException(
					"Creating FileDescription", ioe);			
		}
		bytes = baos.toByteArray();
	}

	/**
	 * Implementation of <code>DataObject</code> abstract method.
	 *
	 * @exception HDFException thrown if there is a problem interpreting the bytes
	 */
	public void interpretBytes() throws HDFException {
		byte[] temp;
		final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		final DataInputStream dis = new DataInputStream(bais);
		try {
			temp = new byte[bytes.length];
			dis.read(temp);
		} catch (IOException e) {
			throw new HDFException(
				"Problem interpreting FileDescription.",e);
		}
	}
}
