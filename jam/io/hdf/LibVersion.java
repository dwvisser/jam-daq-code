package jam.io.hdf;
import java.io.*;
import jam.util.*;

/**
 * Class to represent a 32-bit java int HDF <em>Library Version Number</em> data object for
 * 4.1r2.
 *
 * @version	0.5 November 98
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @since       JDK1.1
 */
public class LibVersion extends DataObject {

	/**
	 * Major version number
	 */
	static final private int majorv = 4;

	/**
	 * Minor version number
	 */
	static final private int minorv = 1;

	/**
	 * release serial number
	 */
	static final private int release = 2;

	/**
	 * Descriptive String
	 */
	String description;

	public LibVersion(HDFile fi) {
		super(fi, DFTAG_VERSION); //sets tag
		int byteLength = 12 + description.length(); // 3 ints + string
		ByteArrayOutputStream baos = new ByteArrayOutputStream(byteLength);
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeInt(majorv);
			dos.writeInt(minorv);
			dos.writeInt(release);
			dos.writeBytes(description);
		} catch (IOException ioe) {
			System.err.println(ioe);
		}
		bytes = baos.toByteArray();
		initDescription();
	}

	public LibVersion(HDFile hdf, byte[] data, short t,short reference) {
		super(hdf, data, t, reference);
		initDescription();
	}
	
	final void initDescription(){
		final StringUtilities su=StringUtilities.instance();
		su.makeLength(
			"HDF 4.1r2 compliant. 12/31/98 Dale Visser",
			80);
		/* DFTAG_VERSION seems to need to be 92(80=92-12) long */ 
	}

	/**
	 * Implementation of <code>DataObject</code> abstract method.
	 *
	 * @exception HDFException thrown if there is a problem interpreting the bytes
	 */
	public void interpretBytes() throws HDFException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		byte[] temp;

		temp = new byte[bytes.length - 12]; //array has 3 int's and a String
		try { //stuff coded as final so don't try to assign
			dis.readInt();
			dis.readInt();
			dis.readInt();
			dis.read(temp);
			description = new String(temp);
		} catch (IOException e) {
			throw new HDFException(
				"Problem interpreting VERSION: " + e.getMessage());
		}
	}
}
