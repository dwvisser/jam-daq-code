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
public class FileIdentifier extends DataObject {

	/**
	 * Object being labelled.
	 */
	DataObject object;

	String label;

	public FileIdentifier(HDFile hdf, String label) {
		super(hdf, DFTAG_FID); //sets tag
		this.label = label;
		int byteLength = label.length();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(byteLength);
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeBytes(label);
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(null,ioe.getMessage(),
			getClass().getName(),JOptionPane.ERROR_MESSAGE);
		}
		bytes = baos.toByteArray();
	}

	public FileIdentifier(HDFile hdf, byte[] data, short t, short reference) {
		super(hdf, data, t, reference);
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
			label = new String(temp);
		} catch (IOException e) {
			throw new HDFException(
				"Problem interpreting FID: " + e.getMessage());
		}
	}

	/**
	 * Returns the text contained.
	 */
	public String getLabel() {
		return label;
	}

}
