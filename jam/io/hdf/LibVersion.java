package jam.io.hdf;
import jam.util.StringUtilities;

import java.nio.ByteBuffer;

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
	
	private static final StringUtilities UTIL=StringUtilities.instance();

	/**
     * Descriptive String
     */
    private static final String DESCRIPTION = UTIL.makeLength(
            "HDF 4.1r2 compliant. 12/31/98 Dale Visser", 80);

    /* DFTAG_VERSION seems to need to be 92(80=92-12) long */

	LibVersion() {
		super(DFTAG_VER); //sets tag
		final int byteLength = 12 + DESCRIPTION.length(); // 3 ints + string
		bytes = ByteBuffer.allocate(byteLength);
		bytes.putInt(MAJOR);
		bytes.putInt(MINOR);
		bytes.putInt(RELEASE);
		putString(DESCRIPTION);
	}

	public void interpretBytes() {
	    //nothing to do
	}
}
