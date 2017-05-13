package jam.io.hdf;

import static jam.io.hdf.Constants.DFTAG_SDD;

import java.nio.ByteBuffer;

/**
 * Class to represent an HDF <em>Scientific Data Dimension</em> data object.
 * 
 * @version 0.5 November 98
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser </a>
 * @since JDK1.1
 */
final class ScientificDataDimension extends AbstractData {

	/**
	 * The number of dimensions
	 */
	private transient int rank;

	/**
	 * The size of the dimensions. I have assumed identical x- and y- dimensions
	 * for 2-d spectra.
	 */
	private transient int sizeX;

	private transient int sizeY;

	private transient byte numberType;

	protected static ScientificDataDimension create(final short rank,
			final int sizeX, final int sizeY, final byte numberType) {
		ScientificDataDimension rval = null;// return value
		for (ScientificDataDimension sdd : AbstractData
				.ofType(ScientificDataDimension.class)) {
			if ((sdd.getRank() == rank) && (sdd.getType() == numberType)
					&& (sdd.getSizeX() == sizeX) && (sdd.getSizeY() == sizeY)) {
				rval = sdd;
				break;// for quicker execution
			}
		}
		if (rval == null) {
			rval = new ScientificDataDimension(rank, sizeX, sizeY, numberType);
		}
		return rval;
	}

	private ScientificDataDimension(final short rank, final int sizeX,
			final int sizeY, final byte numberType) {
		super(DFTAG_SDD); // sets tag
		this.rank = rank;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.numberType = numberType;
		final int byteLength = 6 + 8 * rank; // see p. 6-33 HDF 4.1r2 specs
		bytes = ByteBuffer.allocate(byteLength);
		bytes.putShort(rank);
		/* next 2 lines write the dimensions of the ranks */
		bytes.putInt(sizeX);
		if (rank == 2) {
			bytes.putInt(sizeY);
		}
		/* write out data number type */
		final AbstractData itype = NumberType.getIntType();
		if (numberType == NumberType.DOUBLE) {
			final AbstractData dtype = NumberType.getDoubleType();
			bytes.putShort(dtype.getTag());
			bytes.putShort(dtype.getRef());
		} else {
			bytes.putShort(itype.getTag());
			bytes.putShort(itype.getRef());
		}
		for (int i = 0; i < rank; i++) { // write out scale number type
			bytes.putShort(itype.getTag());
			bytes.putShort(itype.getRef());
		}
		/*
		 * Create new data scales object to go with this. A reference variable
		 * is not needed.
		 */
		new ScientificDataScales(this);
	}

	ScientificDataDimension() {
		super(DFTAG_SDD);
	}

	@Override
	public void interpretBytes() {
		short numberTag, numberRef;
		bytes.position(0);
		rank = bytes.getShort();
		/* next 2 lines read dimensions of ranks */
		sizeX = bytes.getInt();
		sizeY = (rank == 2) ? bytes.getInt() : 0;
		numberTag = bytes.getShort();
		numberRef = bytes.getShort();
		numberType = ((NumberType) getObject(TYPES.get(numberTag), numberRef))
				.getType();
		/* We don't bother reading the scales */
	}

	protected int getRank() {
		return rank;
	}

	protected int getSizeX() {
		return sizeX;
	}

	protected int getSizeY() {
		return sizeY;
	}

	protected byte getType() {
		return numberType;
	}

	@Override
	public String toString() {
		final StringBuilder rval = new StringBuilder();
		final String type = numberType == NumberType.DOUBLE ? "Double"
				: "Integer";
		final String times = " x ";
		rval.append("SDD ").append(ref).append(": ").append(type).append(times)
				.append(sizeX);
		if (rank == 2) {
			rval.append(times).append(sizeY);
		}
		return rval.toString();
	}
}