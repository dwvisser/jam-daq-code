package jam.io.hdf;

import java.nio.ByteBuffer;

import static jam.io.hdf.Constants.DFTAG_MT;

/**
 * Specifies the format of numbers and chars used by Java. This should be
 * included in all files, so that there is no ambiguity in the encoding of
 * numbers and characters.
 * 
 * @version 0.5 November 98
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @since JDK1.1
 */
final class JavaMachineType extends AbstractData {

	/**
	 * BigEndian 32 bit Sun architecture output, not coincidentally the way Sun
	 * chose to have Java store things with the DataOutput interface. Each
	 * hexadecimal 1 means Big Endian 32-bit architecture with IEEE floats. For
	 * alternative DFMT_ values and interpretations, see <code>hdfi.h</code>
	 * in the HDF4.1r2 source code.
	 */
	private static final short DFMT_SUN = 0x1111;

	JavaMachineType() {
		super(DFTAG_MT); // sets tag
		bytes = ByteBuffer.allocate(2);
		bytes.putShort(DFMT_SUN);
	}

	public void interpretBytes() {
		// assumed only JAM hdf files read, so don't bother
	}
}
