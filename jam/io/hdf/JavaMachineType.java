package jam.io.hdf;
import java.io.*;

/**
 * Specifies the format of numbers and chars used by Java.
 * This should be included in all files, so that there is no ambiguity in the
 * encoding of numbers and characters.
 *
 * @version	0.5 November 98
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @since       JDK1.1
 */
public class JavaMachineType extends DataObject {

	/**
	 * BigEndian 32 bit Sun architecture output, not coincidentally the way Sun chose to have
	 * Java store things with the DataOutput interface.  Each hexadecimal 1 means Big Endian 32-bit architecture
	 * with IEEE floats.  For alternative DFMT_ values and interpretations, see <code>hdfi.h</code> in the
	 * HDF4.1r2 source code.  
	 */
	private short DFMT_SUN = 0x1111;

	public JavaMachineType(HDFile fi) {
		super(fi, DFTAG_MT); //sets tag
		ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeShort(DFMT_SUN);
		} catch (IOException ioe) {
			System.err.println("JavaMachineType(HDFile) IOException: " + ioe);
		}
		bytes = baos.toByteArray();
	}

	public JavaMachineType(HDFile hdf, byte[] data, short t, short reference) {
		super(hdf, data, t, reference);
	}

	public void interpretBytes() { 
		// assumed only JAM hdf files read, so don't bother
	}
}
