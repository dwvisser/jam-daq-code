package jam.io.hdf;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Class to represent an HDF <em>Scientific Data</em> data object.
 *
 * @version	0.5 November 98
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @since       JDK1.1
 */
final class ScientificData extends DataObject {

	/**
	 * The number of dimensions
	 */
	private int rank;

	/**
	 * The size of the dimensions.
	 * //PROBLEM I have assumed identical x- and y- dimensions for
	 * 2-d spectra. In the process of fixing this.
	 */
	private int sizeX;
	private int sizeY;

	/**
	 * By default, is STORE which indicates normal mode of operation, where <code>bytes</code> is read in when
	 * instantizing,
	 * which can be a memory hog and cause problems.  Otherwise, is WAIT_AND_READ which only creates the internal
	 * array when the data is requested, and then dereferences it.
	 */
	private byte inputMode;

	private static final byte STORE = 0;
	private static final byte WAIT_TO_READ = 1;
	private static final String REF_MSG = "DataObject mode not properly set: Ref# ";

	private int[] counts;
	private double[] countsD;
	private int[][] counts2d;
	private double[][] counts2dD;
	private int byteLength;

	private byte numberType;

	ScientificData(int[] counts) {
		super(DFTAG_SD); //sets tag
		numberType = NumberType.INT;
		inputMode = STORE;
		rank = 1;
		sizeX = counts.length;
		byteLength = NumberType.INT_SIZE * sizeX; // see p. 6-34 HDF 4.1r2 specs
		this.counts = counts;
	}

	ScientificData(double[] counts) {
		super(DFTAG_SD); //sets tag
		numberType = NumberType.DOUBLE;
		inputMode = STORE;
		rank = 1;
		sizeX = counts.length;
		byteLength = NumberType.DOUBLE_SIZE * sizeX; // see p. 6-34 HDF 4.1r2 specs
		this.countsD = counts;
	}

	ScientificData(int[][] counts2d) {
		super(DFTAG_SD); //sets tag
		numberType = NumberType.INT;
		inputMode = STORE;
		rank = 2;
		sizeX = counts2d.length;
		sizeY = counts2d[0].length;
		byteLength = NumberType.INT_SIZE * sizeX * sizeY; // see p. 6-34 HDF 4.1r2 specs
		this.counts2d = counts2d;
	}

	ScientificData(double[][] counts2d) {
		super(DFTAG_SD); //sets tag
		numberType = NumberType.DOUBLE;
		inputMode = STORE;
		rank = 2;
		sizeX = counts2d.length;
		sizeY = counts2d[0].length;
		byteLength = NumberType.DOUBLE_SIZE * sizeX * sizeY;
		// see p. 6-34 HDF 4.1r2 specs
		this.counts2dD = counts2d;
	}

	void init(int offset, int length, short tag, short reference) {
		super.init(offset, length, tag, reference);
		inputMode = WAIT_TO_READ;
	}
	
	ScientificData(){
	    super();
	}

	/**
	 * requires associated SDD, NT, NDG records
	 */
	public void interpretBytes() {
	}

	/* non-javadoc:
	 * @throws HDFException unrecoverable error
	 * @throws UnsupportedOperationException if this object doesn't represent 1d int
	 * @throws IllegalStateException if the input mode isn't recognized
	 */
	int[] getData1d(RandomAccessFile file, int size) throws HDFException { //assumes int type!
		final byte[] localBytes;
		if (numberType != NumberType.INT || rank != 1) {
			throw new UnsupportedOperationException("getData1d called on wrong type of SD.");
		}
		switch (inputMode) {
			case STORE : //read data from internal array
				localBytes = bytes;
				break;
			case WAIT_TO_READ :
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
					REF_MSG + ref);
		}
		final int[] output = new int[size];
		final DataInput dataInput =
			new DataInputStream(new ByteArrayInputStream(localBytes));
		try {
			for (int i = 0; i < size; i++) {
				output[i] = dataInput.readInt();
			}
		} catch (IOException e) {
			throw new HDFException(
				"Problem getting 1d Data in SD: " + e.getMessage());
		}
		return output;
	}

	double[] getData1dD(RandomAccessFile file, int size) throws HDFException { //assumes int type!
		double[] output;
		int i;
		byte[] localBytes;

		if (numberType != NumberType.DOUBLE || rank != 1) {
			throw new HDFException(
				"SD ref#" + getRef() + ": getData1dD() called on wrong type");
		}
		switch (inputMode) {
			case STORE : //read data from internal array
				localBytes = bytes;
				break;
			case WAIT_TO_READ :
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
					REF_MSG + ref);
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

	int[][] getData2d(RandomAccessFile file, int sizeX, int sizeY) throws HDFException {
		final byte[] localBytes;

		if (numberType != NumberType.INT || rank != 2)
			throw new HDFException("getData2d called on wrong type of SD.");
		switch (inputMode) {
			case STORE : //read data from internal array
				localBytes = bytes;
				break;
			case WAIT_TO_READ :
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
					REF_MSG + ref);
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

	double[][] getData2dD(RandomAccessFile file, int sizeX, int sizeY) throws HDFException {
		final byte[] localBytes;

		if (numberType != NumberType.DOUBLE || rank != 2) {
			throw new HDFException("getData2dD called on wrong type of SD.");
		}
		switch (inputMode) {
			case STORE : //read data from internal array
				localBytes = bytes;
				break;
			case WAIT_TO_READ :
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
					REF_MSG + ref);
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
	byte[] getBytes() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(byteLength);
			DataOutputStream dos = new DataOutputStream(baos);
			switch (rank) {
				case 1 :
					for (int i = 0; i < sizeX; i++) { // write out data type
						if (numberType == NumberType.INT) {
							dos.writeInt(counts[i]);
						} else {
							dos.writeDouble(countsD[i]);
						}
					}
					break;
				case 2 :
					for (int i = 0; i < sizeX; i++) { // write out data type
						for (int j = 0; j < sizeY; j++) {
							if (numberType == NumberType.INT) {
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
			throw new IllegalStateException("Problem getting SD: " + e.getMessage());
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
