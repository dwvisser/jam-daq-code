package jam.io.hdf;

import static jam.io.hdf.Constants.DFTAG_SD;

import java.nio.ByteBuffer;

/**
 * Class to represent an HDF <em>Scientific Data</em> data object.
 * 
 * @version 0.5 November 98
 * @author <a href="mailto:dale@visser.name">Dale Visser </a>
 * @since JDK1.1
 */
final class ScientificData extends AbstractData {

	private static final String REF_MSG = "AbstractHData mode not properly set: Ref# ";

	private enum InputMode {
		/**
		 * indicates normal mode of operation, where <code>bytes</code> is read
		 * in when instantizing, which can be a memory hog and cause problems
		 */
		STORE,
		/**
		 * only creates the internal array when the data is requested, and then
		 * dereferences it
		 */
		WAIT_TO_READ
	}

	private transient int byteLength;

	private transient int[] counts;

	private transient int[][] counts2d;

	private transient double[][] counts2dD;

	private transient double[] countsD;

	private transient InputMode inputMode;

	private byte numberType;

	/**
	 * The number of dimensions
	 */
	private transient int rank;

	/**
	 * The size of the dimensions. //PROBLEM I have assumed identical x- and y-
	 * dimensions for 2-d spectra. In the process of fixing this.
	 */
	private transient int sizeX;

	private transient int sizeY;

	ScientificData() {
		super(DFTAG_SD);
	}

	ScientificData(double[] counts) {// NOPMD
		super(DFTAG_SD); // sets tag
		numberType = NumberType.DOUBLE;
		inputMode = InputMode.STORE;
		rank = 1;
		sizeX = counts.length;
		byteLength = NumberType.DOUBLE_SIZE * sizeX; // see p. 6-34 HDF 4.1r2
		// specs
		this.countsD = counts;
	}

	ScientificData(double[][] counts2d) {// NOPMD
		super(DFTAG_SD); // sets tag
		numberType = NumberType.DOUBLE;
		inputMode = InputMode.STORE;
		rank = 2;
		sizeX = counts2d.length;
		sizeY = counts2d[0].length;
		byteLength = NumberType.DOUBLE_SIZE * sizeX * sizeY;
		// see p. 6-34 HDF 4.1r2 specs
		this.counts2dD = counts2d;
	}

	ScientificData(int[] counts) {// NOPMD
		super(DFTAG_SD); // sets tag
		numberType = NumberType.INT;
		inputMode = InputMode.STORE;
		rank = 1;
		sizeX = counts.length;
		byteLength = NumberType.INT_SIZE * sizeX; // see p. 6-34 HDF 4.1r2
		// specs
		this.counts = counts;
	}

	ScientificData(int[][] counts2d) {// NOPMD
		super(DFTAG_SD); // sets tag
		numberType = NumberType.INT;
		inputMode = InputMode.STORE;
		rank = 2;
		sizeX = counts2d.length;
		sizeY = counts2d[0].length;
		byteLength = NumberType.INT_SIZE * sizeX * sizeY; // see p. 6-34 HDF
		// 4.1r2 specs
		this.counts2d = counts2d;
	}

	/**
	 * Returns the byte representation to be written at <code>offset</code> in
	 * the file.
	 * 
	 * @throws IllegalStateException
	 *             if the rank is not 1 or 2
	 */
	@Override
	protected ByteBuffer getBytes() {
		bytes = ByteBuffer.allocate(byteLength);
		switch (rank) {
		case 1:
			for (int i = 0; i < sizeX; i++) { // write out data type
				if (numberType == NumberType.INT) {
					bytes.putInt(counts[i]);
				} else {
					bytes.putDouble(countsD[i]);
				}
			}
			break;
		case 2:
			for (int i = 0; i < sizeX; i++) { // write out data type
				for (int j = 0; j < sizeY; j++) {
					if (numberType == NumberType.INT) {
						bytes.putInt(counts2d[i][j]);
					} else {
						bytes.putDouble(counts2dD[i][j]);
					}
				}
			}
			break;
		default:
			throw new IllegalStateException("SD_" + tag
					+ ", bad value for rank: " + rank);
		}
		return bytes;
	}

	/*
	 * non-javadoc: Late loading of data
	 */
	protected Object getData(final HDFile infile, final int histDim,
			final byte histNumType, final int xlen, final int ylen)
			throws HDFException {
		Object rval;
		if ((histDim == 1) && (histNumType == NumberType.INT)) {
			rval = getData1d(infile, xlen);
		} else if ((histDim == 1) && (histNumType == NumberType.DOUBLE)) {
			rval = getData1dD(infile, xlen);
		} else if ((histDim == 2) && (histNumType == NumberType.INT)) {
			rval = getData2d(infile, xlen, ylen);
		} else if ((histDim == 2) && (histNumType == NumberType.DOUBLE)) {
			rval = getData2dD(infile, xlen, ylen);
		} else {
			throw new HDFException("Unknown histogram data type");
		}
		return rval;

	}

	/*
	 * non-javadoc: @throws HDFException unrecoverable error @throws
	 * UnsupportedOperationException if this object doesn't represent 1d int
	 * 
	 * @throws IllegalStateException if the input mode isn't recognized
	 */
	protected int[] getData1d(final HDFile infile, final int size)
			throws HDFException {
		if (numberType != NumberType.INT || rank != 1) {
			throw new HDFException("getData1d called on wrong type of SD.");
		}
		final byte[] localBytes = getLocalBytes(infile);
		final int[] output = new int[size];
		final ByteBuffer buffer = ByteBuffer.wrap(localBytes);
		for (int i = 0; i < size; i++) {
			output[i] = buffer.getInt();
		}
		bytes = ByteBuffer.allocate(0); // empty it
		return output;
	}

	protected double[] getData1dD(final HDFile infile, final int size)
			throws HDFException {
		double[] output;
		if (numberType != NumberType.DOUBLE || rank != 1) {
			throw new HDFException("SD ref#" + getRef()
					+ ": getData1dD() called on wrong type");
		}
		final byte[] localBytes = getLocalBytes(infile);
		output = new double[size];
		final ByteBuffer buffer = ByteBuffer.wrap(localBytes);
		for (int i = 0; i < size; i++) {
			output[i] = buffer.getDouble();
		}
		bytes = ByteBuffer.allocate(0); // empty it
		return output;
	}

	protected int[][] getData2d(final HDFile infile, final int xlen,
			final int ylen) throws HDFException {
		if (numberType != NumberType.INT || rank != 2) {
			throw new HDFException("getData2d called on wrong type of SD.");
		}
		final byte[] localBytes = getLocalBytes(infile);
		int[][] output = new int[xlen][ylen];
		final ByteBuffer buffer = ByteBuffer.wrap(localBytes);
		for (int i = 0; i < xlen; i++) {
			for (int j = 0; j < ylen; j++) {
				output[i][j] = buffer.getInt();
			}
		}
		bytes = ByteBuffer.allocate(0); // empty it
		return output;
	}

	protected double[][] getData2dD(final HDFile infile, final int xlen,
			final int ylen) throws HDFException {
		if (numberType != NumberType.DOUBLE || rank != 2) {
			throw new HDFException("getData2dD called on wrong type of SD.");
		}
		final byte[] localBytes = getLocalBytes(infile);
		double[][] output = new double[xlen][ylen];
		final ByteBuffer buffer = ByteBuffer.wrap(localBytes);
		for (int i = 0; i < xlen; i++) {
			for (int j = 0; j < ylen; j++) {
				output[i][j] = buffer.getDouble();
			}
		}
		bytes = ByteBuffer.allocate(0); // empty it
		return output;
	}

	private byte[] getLocalBytes(final HDFile infile) throws HDFException {
		final byte[] localBytes;
		switch (inputMode) {
		case STORE: // read data from internal array
			localBytes = bytes.array();
			break;
		case WAIT_TO_READ:
			localBytes = infile.lazyReadData(this);
			break;
		default:
			throw new HDFException(REF_MSG + ref);
		}
		return localBytes;
	}

	protected int getNumberType() {
		return numberType;
	}

	@Override
	protected void init(final byte[] data, final short reference) {
		super.init(data, reference);
		inputMode = InputMode.STORE;
	}

	@Override
	protected void init(final int byteOffset, final int len,
			final short reference) {
		super.init(byteOffset, len, reference);
		inputMode = InputMode.WAIT_TO_READ;
	}

	/**
	 * requires associated SDD, NT, NDG records
	 */
	@Override
	public void interpretBytes() {
		// do-nothing
	}

	protected void setNumberType(final byte type) {
		synchronized (this) {
			numberType = type;
		}
	}

	protected void setRank(final int newRank) {
		synchronized (this) {
			rank = newRank;
		}
	}

	@Override
	public String toString() {
		final StringBuffer rval = new StringBuffer();
		final String type = numberType == NumberType.DOUBLE ? "Double"
				: "Integer";
		final String times = " x ";
		rval.append("SD ").append(ref).append(": ").append(type).append(times)
				.append(sizeX);
		if (rank == 2) {
			rval.append(times).append(sizeY);
		}
		return rval.toString();
	}
}