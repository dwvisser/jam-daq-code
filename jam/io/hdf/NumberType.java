package jam.io.hdf;

/**
 * Class to represent a 32-bit java int HDF <em>NumberType</em> data object.
 * When constructed with argument <code>NumberType.INT</code>, creates the object indicating
 * <code>int</code> primitives.  When constructed with argument <code>NumberType.DOUBLE</code>,
 * creates the object indicating <code>double</code> primitives.
 *
 * @version	0.5 November 98
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @since       JDK1.1
 */
final class NumberType extends DataObject {

	/**
	 * First version of encoding
	 */
	private final static byte NT_VERSION = 1;

	/**
	 * Unsigned int 
	 */
	private final static byte DFNT_UINT32 = 25;

	/**
	 * int
	 */
	private final static byte DFNT_INT32 = 24;

	/**
	 * 64-bit floating point, meant for Java <code>double</code>
	 */
	private final static byte DFNT_FLOAT64 = 6;

	/**
	 * int width, in bits
	 */
	private final static byte IntWidth = 32;

	/**
	 * double width, in bits
	 */
	private final static byte DoubleWidth = 64;

	/**
	 * Motorola byte order, (same as Java), 
	 */
	private final static byte DFNT_MBO = 1;

	private byte numberType;
	
	/** Code for <code>int</code> number type. */
	static final byte INT = 0;

	/** Code for <code>double</code> number type. */
	static final byte DOUBLE = 1;
	
	static final byte INT_SIZE = 4;
	static final byte DOUBLE_SIZE = 8;

	/**
	 * @param fi HDF file we belong to
	 * @param type one of <code>INT</code> or <code>DOUBLE</code>, 
	 * @throws IllegalArgumentException if an invalid type is given
	 */
	NumberType(HDFile fi, byte type) {
		super(fi, DFTAG_NT); //sets tag
		if (type == INT) {
			bytes = new byte[] { NT_VERSION, DFNT_INT32, IntWidth, DFNT_MBO };
		} else if (type == DOUBLE) {
			bytes =
				new byte[] { NT_VERSION, DFNT_FLOAT64, DoubleWidth, DFNT_MBO };
		} else {
			throw new IllegalArgumentException("Invalid type for NumberType: "+type);
		}
	}

	NumberType(HDFile hdf, byte[] data, short t, short reference) {
		super(hdf, data, t,reference);
	}

	/**
	 * Implementation of <code>DataObject</code> abstract method.
	 *
	 * @exception HDFException thrown if there is a problem interpreting the bytes
	 */
	public void interpretBytes() throws HDFException {
		switch (bytes[1]) {
			case DFNT_UINT32 :
				numberType = INT;
				break;
			case DFNT_INT32 :
				numberType = INT;
				break;
			case DFNT_FLOAT64 :
				numberType = DOUBLE;
				break;
			default :
				throw new HDFException("NumberType.interpretBytes(): Unrecognized number type.");
		}
	}

	byte getType() {
		return numberType;
	}

}
