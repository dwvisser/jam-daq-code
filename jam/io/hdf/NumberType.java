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
	private final static byte INT_WIDTH = 32;

	/**
	 * double width, in bits
	 */
	private final static byte DOUBLEWIDTH = 64;

	/**
	 * Motorola byte order, (same as Java), 
	 */
	private final static byte DFNT_MBO = 1;

	private transient byte numberType;
	
	/** Code for <code>int</code> number type. */
	static final byte INT = 0;

	/** Code for <code>double</code> number type. */
	static final byte DOUBLE = 1;
	
	static final byte INT_SIZE = 4;
	
	static final byte DOUBLE_SIZE = 8;
	
	static NumberType intNT;
	
	static NumberType doubleNT;
	
	/**
	 * @return the double number type.
	 */
	static NumberType getDoubleType() {
		return doubleNT;
	}

	/**
	 * @return the int number type.
	 */
	static NumberType getIntType() {
		return intNT;
	}

	/**
	 * @param hdfile HDF file we belong to
	 * @param type one of <code>INT</code> or <code>DOUBLE</code>, 
	 * @throws IllegalArgumentException if an invalid type is given
	 */
	NumberType(HDFile hdfile, byte type) {
		super(hdfile, DFTAG_NT); //sets tag
		createBytes(type);
		setStatics(type);
	}
	
	NumberType(byte type){
		super(DFTAG_NT);
		createBytes(type);
		setStatics(type);
	}

	NumberType(HDFile hdf, byte[] data, short tag, short reference) {
		super(hdf, data, tag, reference);
	}
	
	private void createBytes(byte type) {
		if (type == INT) {
			bytes = new byte[] { NT_VERSION, DFNT_INT32, INT_WIDTH, DFNT_MBO };
		} else if (type == DOUBLE) {
			bytes =
				new byte[] { NT_VERSION, DFNT_FLOAT64, DOUBLEWIDTH, DFNT_MBO };
		} else {
			throw new IllegalArgumentException("Invalid type for NumberType: "+type);
		}
	}
	private void setStatics(byte type) {
		if (type == INT) {
			intNT =this;
		} else if (type == DOUBLE) {
			doubleNT=this;
		}
		
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
