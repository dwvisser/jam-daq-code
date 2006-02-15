package jam.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Utility class for turning sequences of bytes into java ints and shorts
 * depending on the byte order conventions specified.
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
	 * @param array
	 *            source
	 * @param offset
	 *            starting point
	 * @param byteOrder
	 *            used to interpret bytes
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
	 * @param array
	 *            source
	 * @param offset
	 *            starting point
	 * @param byteOrder
	 *            used to interpret bytes
	 * @return the int represented by the bytes
	 */
	public short bytesToShort(final byte[] array, final int offset,
			final ByteOrder byteOrder) {
		final ByteBuffer byteBuffer = ByteBuffer.wrap(array, offset, 2);
		byteBuffer.order(byteOrder);
		return byteBuffer.getShort();
	}

	/**
	 * Converts int array to double array.
	 * 
	 * @param intArray
	 *            array to convert
	 * @return double array most closely approximating the given array
	 */
	public double[] intToDoubleArray(final int[] intArray) {
		final int len = intArray.length;
		final double[] out = new double[len];
		for (int i = 0; i < len; i++) {
			out[i] = intArray[i];
		}
		return out;
	}

	/**
	 * Converts double array to int array.
	 * 
	 * @param dArray
	 *            array to convert
	 * @return int array most closely approximating the given array
	 */
	public int[] doubleToIntArray(final double[] dArray) {
		final int len = dArray.length;
		final int[] out = new int[len];
		for (int i = 0; i < len; i++) {
			out[i] = (int) Math.round(dArray[i]);
		}
		return out;
	}

	/**
	 * Convert int 2 dim array to double 2 dim array.
	 * 
	 * @param int2D
	 *            array to convert
	 * @return double array most closely approximating the given array
	 */
	public double[][] intToDouble2DArray(final int[][] int2D) {
		final int lenX = int2D.length;
		final int lenY = int2D[0].length;
		double[][] rval = new double[lenX][lenY];
		for (int i = 0; i < lenX; i++) {
			for (int j = 0; j < lenY; j++) {
				rval[i][j] = int2D[i][j];
			}
		}
		return rval;
	}

}
