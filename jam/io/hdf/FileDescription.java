package jam.io.hdf;
import java.io.*;

/**
 * Class to represent an HDF <em>File Description</em> data object.  The text is meant to be a description of
 * the contents of the file.
 *
 * @version	0.5 December 98
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @since       JDK1.1
 */
public class FileDescription extends DataObject {

	/**
	 * Object being labelled.
	 */
	DataObject object;

	String label;

	public FileDescription(HDFile hdf, String label) {
		super(hdf, DFTAG_FD); //sets tag
		this.label = label;
		int byteLength = label.length();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(byteLength);
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeBytes(label);
		} catch (IOException ioe) {
			System.err.println(ioe);
		}
		bytes = baos.toByteArray();
	}

	public FileDescription(HDFile hdf, byte[] data, short t, short reference) {
		super(hdf, data, t, reference);
		//tag=DFTAG_FD;
	}

	/**
	 * Implementation of <code>DataObject</code> abstract method.
	 *
	 * @exception HDFException thrown if there is a problem interpreting the bytes
	 */
	public void interpretBytes() throws HDFException {
		byte[] temp;
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);

		try {
			temp = new byte[bytes.length];
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
	public String getText() {
		return label;
	}

}
