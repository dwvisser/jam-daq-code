package jam.io.hdf;
import jam.util.StringUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.swing.JOptionPane;

/**
 * Class to represent a 32-bit java int HDF <em>Library Version Number</em> data object for
 * 4.1r2.
 *
 * @version	0.5 November 98
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @since       JDK1.1
 */
final class LibVersion extends DataObject {

	/**
	 * Major version number
	 */
	static final private int MAJOR = 4;

	/**
	 * Minor version number
	 */
	static final private int MINOR = 1;

	/**
	 * release serial number
	 */
	static final private int RELEASE = 2;
	
	private final StringUtilities util=StringUtilities.instance();

	/**
     * Descriptive String
     */
    private String description = util.makeLength(
            "HDF 4.1r2 compliant. 12/31/98 Dale Visser", 80);

    /* DFTAG_VERSION seems to need to be 92(80=92-12) long */

	LibVersion(HDFile file) {
		super(file, DFTAG_VERSION); //sets tag
		final int byteLength = 12 + description.length(); // 3 ints + string
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(byteLength);
		final DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeInt(MAJOR);
			dos.writeInt(MINOR);
			dos.writeInt(RELEASE);
			dos.writeBytes(description);
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(null,ioe.getMessage(),
			getClass().getName(),JOptionPane.ERROR_MESSAGE);
		}
		bytes = baos.toByteArray();
	}

	LibVersion(HDFile hdf, byte[] data, short tag,short reference) {
		super(hdf, data, tag, reference);
	}
	
	public void interpretBytes() throws HDFException {
		final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		final DataInputStream dis = new DataInputStream(bais);
		final byte [] temp = new byte[bytes.length - 12]; //array has 3 int's and a String
		try { //stuff coded as final so don't try to assign
			dis.readInt();
			dis.readInt();
			dis.readInt();
			dis.read(temp);
			description = util.getASCIIstring(temp);
		} catch (IOException e) {
			throw new HDFException(
				"Problem interpreting VERSION.",e);
		}
	}
}
