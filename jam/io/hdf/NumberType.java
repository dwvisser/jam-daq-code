package jam.io.hdf;

import static jam.io.hdf.Constants.DFTAG_NT;

import java.nio.ByteBuffer;

/**
 * Class to represent a 32-bit java int HDF <em>NumberType</em> data object.
 * When constructed with argument <code>NumberType.INT</code>, creates the
 * object indicating <code>int</code> primitives. When constructed with argument
 * <code>NumberType.DOUBLE</code>, creates the object indicating
 * <code>double</code> primitives.
 * 
 * @version 0.5 November 98
 * @author <a href="mailto:dale@visser.name">Dale Visser </a>
 * @since JDK1.1
 */
final class NumberType extends AbstractData {

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

	private transient byte nType;

	/** Code for <code>int</code> number type. */
	public static final byte INT = 0;

	/** Code for <code>double</code> number type. */
	public static final byte DOUBLE = 1;

	public static final byte INT_SIZE = 4;

	public static final byte DOUBLE_SIZE = 8;

	private static NumberType intNT;

	private static NumberType doubleNT;

	/**
	 * @return the double number type.
	 */
	protected static NumberType getDoubleType() {
		return doubleNT;
	}

	/**
	 * @return the int number type.
	 */
	protected static NumberType getIntType() {
		return intNT;
	}

	/**
	 * Almost all of Jam's number storage needs are satisfied by the type
	 * hard-coded into the class <code>NumberType</code>. This method creates
	 * the <code>NumberType</code> objects in the file that gets referred to
	 * repeatedly by the other data elements.
	 * 
	 * @see jam.io.hdf.NumberType
	 */
	protected static void createDefaultTypes() {
		intNT = new NumberType(NumberType.INT);
		doubleNT = new NumberType(NumberType.DOUBLE);
	}

	/**
	 * @param type
	 *            one of <code>INT</code> or <code>DOUBLE</code>,
	 * @throws IllegalArgumentException
	 *             if an invalid type is given
	 */
	NumberType(final byte type) {
		super(DFTAG_NT);
		createBytes(type);
	}

	NumberType() {
		super(DFTAG_NT);
	}

	private void createBytes(final byte type) {
		bytes = ByteBuffer.allocate(4);
		bytes.put(NT_VERSION);
		if (type == INT) {
			bytes.put(DFNT_INT32);
			bytes.put(INT_WIDTH);
		} else if (type == DOUBLE) {
			bytes.put(DFNT_FLOAT64);
			bytes.put(DOUBLEWIDTH);
		} else {
			throw new IllegalArgumentException("Invalid type for NumberType: "
					+ type);
		}
		bytes.put(DFNT_MBO);
	}

	/**
	 * Implementation of <code>DataObject</code> abstract method.
	 */
	@Override
	public void interpretBytes() {
		final byte type = bytes.get(1);
		switch (type) {
		case DFNT_UINT32:
			nType = INT;
			break;
		case DFNT_INT32:
			nType = INT;
			break;
		case DFNT_FLOAT64:
			nType = DOUBLE;
			break;
		default:
			throw new IllegalStateException(
					"NumberType.interpretBytes(): Unrecognized number type.");
		}
	}

	protected byte getType() {
		return nType;
	}
}