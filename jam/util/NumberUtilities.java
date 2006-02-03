package jam.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Utility class for turning sequences of bytes into java ints and shorts depending
 * on the byte order conventions specified.
 * 
 * @author Dale Visser
 *
 */
public final class NumberUtilities {

	private static final NumberUtilities instance = new NumberUtilities();

	private NumberUtilities() {
		super();
	}

	/**
	 * 
	 * @return the unique singleton instance of this class
	 */
	public static NumberUtilities getInstance() {
		return instance;
	}

	/**
	 * Pull an int out of a byte array.
	 * 
	 * @param array source
	 * @param offset starting point
	 * @param byteOrder used to interpret bytes
	 * @return the int represented by the bytes
	 */
	public int bytesToInt(final byte[] array, final int offset,
			final ByteOrder byteOrder) {
		final ByteBuffer byteBuffer = ByteBuffer.wrap(array, offset, 4);
		byteBuffer.order(byteOrder);
		return byteBuffer.getInt();
	}

	/**
	 * Pull a short out of a byte array.
	 * 
	 * @param array source
	 * @param offset starting point
	 * @param byteOrder used to interpret bytes
	 * @return the int represented by the bytes
	 */
	public short bytesToShort(final byte[] array, final int offset,
			final ByteOrder byteOrder) {
		final ByteBuffer byteBuffer = ByteBuffer.wrap(array, offset, 2);
		byteBuffer.order(byteOrder);
		return byteBuffer.getShort();
	}

}
