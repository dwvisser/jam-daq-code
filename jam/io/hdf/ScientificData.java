package jam.io.hdf;
import java.io.*;

/**
 * Class to represent an HDF <em>Scientific Data</em> data object.
 *
 * @version	0.5 November 98
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @since       JDK1.1
 */
public class ScientificData extends DataObject {

	/**
	 * The number of dimensions
	 */
	int rank;

	/**
	 * The size of the dimensions.
	 * //PROBLEM I have assumed identical x- and y- dimensions for
	 * 2-d spectra. In the process of fixing this.
	 */
	int sizeX;
	int sizeY;

	/**
	 * By default, is STORE which indicates normal mode of operation, where <code>bytes</code> is read in when
	 * instantizing,
	 * which can be a memory hog and cause problems.  Otherwise, is WAIT_AND_READ which only creates the internal
	 * array when the data is requested, and then dereferences it.
	 */
	private byte inputMode;

	private static final byte STORE = 0;
	private static final byte WAIT_AND_READ = 1;

	private int[] counts;
	private double[] countsD;
	private int[][] counts2d;
	private double[][] counts2dD;
	int byteLength;

	private byte numberType;

	private static final byte INT = NumberType.INT;
	private static final byte DOUBLE = NumberType.DOUBLE;
	private static final byte INT_SIZE = NumberType.INT_SIZE;
	private static final byte DOUBLE_SIZE = NumberType.DOUBLE_SIZE;

	public ScientificData(HDFile fi, int[] counts) {
		super(fi, DFTAG_SD); //sets tag
		numberType = INT;
		inputMode = STORE;
		rank = 1;
		sizeX = counts.length;
		byteLength = INT_SIZE * sizeX; // see p. 6-34 HDF 4.1r2 specs
		this.counts = counts;
	}

	public ScientificData(HDFile fi, double[] counts) {
		super(fi, DFTAG_SD); //sets tag
		numberType = DOUBLE;
		inputMode = STORE;
		rank = 1;
		sizeX = counts.length;
		byteLength = DOUBLE_SIZE * sizeX; // see p. 6-34 HDF 4.1r2 specs
		this.countsD = counts;
	}

	public ScientificData(HDFile fi, int[][] counts2d) {
		super(fi, DFTAG_SD); //sets tag
		numberType = INT;
		inputMode = STORE;
		rank = 2;
		sizeX = counts2d.length;
		sizeY = counts2d[0].length;
		byteLength = INT_SIZE * sizeX * sizeY; // see p. 6-34 HDF 4.1r2 specs
		this.counts2d = counts2d;
	}

	public ScientificData(HDFile fi, double[][] counts2d) {
		super(fi, DFTAG_SD); //sets tag
		numberType = DOUBLE;
		inputMode = STORE;
		rank = 2;
		sizeX = counts2d.length;
		sizeY = counts2d[0].length;
		byteLength = DOUBLE_SIZE * sizeX * sizeY;
		// see p. 6-34 HDF 4.1r2 specs
		this.counts2dD = counts2d;
	}

	public ScientificData(HDFile hdf, byte[] data, short t, short reference) {
		super(hdf, data, t, reference);
		inputMode = STORE;
	}

	/**
	 *
	 */
	public ScientificData(
		HDFile hdf,
		int offset,
		int length,
		short t,
		short reference) {
		super(hdf, offset, length, t, reference);
		inputMode = WAIT_AND_READ;
	}

	/**
	 *
	 */
	public void interpretBytes() { //requires associated SDD, NT, NDG records
	}

	/**
	 * @throws HDFException unrecoverable error
	 * @throws UnsupportedOperationException if this object doesn't represent 1d int
	 * @throws IllegalStateException if the input mode isn't recognized
	 */
	int[] getData1d(int size) throws HDFException { //assumes int type!
		final byte[] localBytes;
		if (numberType != INT || rank != 1) {
			throw new UnsupportedOperationException("getData1d called on wrong type of SD.");
		}
		switch (inputMode) {
			case STORE : //read data from internal array
				localBytes = bytes;
				break;
			case WAIT_AND_READ :
				localBytes = new byte[length];
				try {
					file.seek(offset);
					file.read(localBytes);
				} catch (IOException e) {
					throw new HDFException(
						"Problem getting 1d Data in SD: " + e.getMessage());
				}
				break;
			default :
				throw new IllegalStateException(
					"DataObject mode not properly set: Ref# " + ref);
		}
		final int[] output = new int[size];
		final DataInput di =
			new DataInputStream(new ByteArrayInputStream(localBytes));
		try {
			for (int i = 0; i < size; i++) {
				output[i] = di.readInt();
			}
		} catch (IOException e) {
			throw new HDFException(
				"Problem getting 1d Data in SD: " + e.getMessage());
		}
		return output;
	}

	/**
	 *  @exception HDFException unrecoverable error
	 */
	double[] getData1dD(int size) throws HDFException { //assumes int type!
		double[] output;
		int i;
		byte[] localBytes;

		if (numberType != DOUBLE || rank != 1) {
			throw new HDFException(
				"SD ref#" + getRef() + ": getData1dD() called on wrong type");
		}
		switch (inputMode) {
			case STORE : //read data from internal array
				localBytes = bytes;
				break;
			case WAIT_AND_READ :
				localBytes = new byte[length];
				try {
					file.seek(offset);
					file.read(localBytes);
				} catch (IOException e) {
					throw new HDFException(
						"Problem getting 1D data in SD: " + e.getMessage());
				}
				break;
			default :
				throw new HDFException(
					"DataObject mode not properly set: Ref# " + ref);
		}
		output = new double[size];
		final DataInput di =
			new DataInputStream(new ByteArrayInputStream(localBytes));
		try {
			for (i = 0; i < size; i++) {
				output[i] = di.readDouble();
			}
		} catch (IOException e) {
			throw new HDFException(
				"Problem getting 1D data in SD: " + e.getMessage());
		}
		return output;
	}

	int[][] getData2d(int sizeX, int sizeY) throws HDFException {
		final byte[] localBytes;

		if (numberType != INT || rank != 2)
			throw new HDFException("getData2d called on wrong type of SD.");
		switch (inputMode) {
			case STORE : //read data from internal array
				localBytes = bytes;
				break;
			case WAIT_AND_READ :
				localBytes = new byte[length];
				try {
					file.seek(offset);
					file.read(localBytes);
				} catch (IOException e) {
					throw new HDFException(
						"Problem getting 2D data in SD: " + e.getMessage());
				}
				break;
			default :
				throw new HDFException(
					"DataObject mode not properly set: Ref# " + ref);
		}
		int[][] output = new int[sizeX][sizeY];
		final DataInput di =
			new DataInputStream(new ByteArrayInputStream(localBytes));
		try {
			for (int i = 0; i < sizeX; i++) {
				for (int j = 0; j < sizeY; j++) {
					output[i][j] = di.readInt();
				}
			}
		} catch (IOException e) {
			throw new HDFException(
				"Problem getting 2D data in SD: " + e.getMessage());
		}
		return output;
	}

	double[][] getData2dD(int sizeX, int sizeY) throws HDFException {
		final byte[] localBytes;

		if (numberType != DOUBLE || rank != 2) {
			throw new HDFException("getData2dD called on wrong type of SD.");
		}
		switch (inputMode) {
			case STORE : //read data from internal array
				localBytes = bytes;
				break;
			case WAIT_AND_READ :
				localBytes = new byte[length];
				try {
					file.seek(offset);
					file.read(localBytes);
					break;
				} catch (IOException e) {
					throw new HDFException(
						"Problem getting 2D data in SD: " + e.getMessage());
				}
			default :
				throw new HDFException(
					"DataObject mode not properly set: Ref# " + ref);
		}
		double[][] output = new double[sizeX][sizeY];
		final DataInput di =
			new DataInputStream(new ByteArrayInputStream(localBytes));
		try {
			for (int i = 0; i < sizeX; i++) {
				for (int j = 0; j < sizeY; j++) {
					output[i][j] = di.readDouble();
				}
			}
		} catch (IOException e) {
			throw new HDFException(
				"Problem getting 2D data in SD: " + e.getMessage());
		}
		return output;
	}

	/**
	 * Returns the byte representation to be written at <code>offset</code> in the file.
	 *
	 * @throws   HDFException  thrown if unrecoverable error occurs
	 * @throws IllegalStateException if the rank is not 1 or 2
	 */
	byte[] getBytes() throws HDFException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(byteLength);
			DataOutputStream dos = new DataOutputStream(baos);
			switch (rank) {
				case 1 :
					for (int i = 0; i < sizeX; i++) { // write out data type
						if (numberType == INT) {
							dos.writeInt(counts[i]);
						} else {
							dos.writeDouble(countsD[i]);
						}
					}
					break;
				case 2 :
					for (int i = 0; i < sizeX; i++) { // write out data type
						for (int j = 0; j < sizeY; j++) {
							if (numberType == INT) {
								dos.writeInt(counts2d[i][j]);
							} else {
								dos.writeDouble(counts2dD[i][j]);
							}
						}
					}
					bytes = baos.toByteArray();
					break;
				default :
					throw new IllegalStateException(
						"SD_" + tag + ", bad value for rank: " + rank);
			}
			return baos.toByteArray();
		} catch (IOException e) {
			throw new HDFException("Problem getting SD: " + e.getMessage());
		}
	}

	/**
	 * Returns the length of the byte array in the file for this data element.
	 */
	protected int getLength() {
		return byteLength;
	}

	void setNumberType(byte type) {
		synchronized (this) {
			numberType = type;
		}
	}

	int getNumberType() {
		return numberType;
	}

	void setRank(int r) {
		synchronized (this) {
			rank = r;
		}
	}

	/**
	 * Reads a signed 32-bit integer from a byte array. This
	 * method reads four bytes from the underlying input stream.
	 */
	/*private double readDouble(byte[] bytes, int position) {
		long ch1 = bytes[position] & 0xFF;
		long ch2 = bytes[position + 1] & 0xFF;
		long ch3 = bytes[position + 2] & 0xFF;
		long ch4 = bytes[position + 3] & 0xFF;
		long ch5 = bytes[position + 4] & 0xFF;
		long ch6 = bytes[position + 5] & 0xFF;
		long ch7 = bytes[position + 6] & 0xFF;
		long ch8 = bytes[position + 7] & 0xFF;
	
		long tempLong =
			((ch1 << 56)
				+ (ch2 << 48)
				+ (ch3 << 40)
				+ (ch4 << 32)
				+ (ch5 << 24)
				+ (ch6 << 16)
				+ (ch7 << 8)
				+ (ch8 << 0));
	
		return Double.longBitsToDouble(tempLong);
	}*/
}
