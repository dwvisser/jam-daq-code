package jam.io.hdf;

import java.nio.ByteBuffer;

import static jam.io.hdf.Constants.DFTAG_VS;

/**
 * Class to represent an HDF <em>Vdata description</em> data object.
 * 
 * @version 0.5 November 98
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser </a>
 * @since JDK1.1
 */
public final class VData extends AbstractData {

	private static final String VS_STRING = "VS_";

	/**
	 * The vector of fields. Contains the useful java representations of the
	 * objects.
	 */
	private transient Object[][] cells;

	private transient VDataDescription description;

	private transient short ivsize;

	private transient short nfields;

	private transient int nvert;

	private transient short[] offsets;

	private transient short[] order;

	private transient short[] types;

	VData() {
		super(DFTAG_VS);
	}

	VData(final VDataDescription vdd) {
		super(DFTAG_VS); // sets tag
		description = vdd;
		nfields = description.getNumFields();
		nvert = description.getNumRows();
		cells = new Object[nfields][nvert];
		ivsize = description.getRowSize();
		order = description.getDimensions();
		types = description.getTypes();
		offsets = description.getDataOffsets();
		/* undocumented by HDF group--data needs same ref as description */
		setRef(description.getRef());
	}

	private static final String WDFC = "Wrong data type for column ";

	protected void addChars(final int column, final int row, final String indata) {
		if (description.getType(column) == VDataDescription.DFNT_CHAR8) {
			setCell(column, row, indata);
		} else { // uh oh... not right type
			throw new IllegalStateException(WDFC + column + "!");
		}
	}

	protected void addDouble(final int column, final int row,
			final double indata) {
		if (description.getType(column) == VDataDescription.DFNT_DBL64) {
			final Object temp = indata;
			setCell(column, row, temp);
		} else { // uh oh... not right type
			throw new IllegalStateException(WDFC + column + "!");
		}
	}

	protected void addFloat(final int column, final int row, final float indata) {
		if (description.getType(column) == VDataDescription.DFNT_FLT32) {
			final Object temp = indata;
			setCell(column, row, temp);
		} else { // uh oh... not right type
			throw new IllegalStateException(WDFC + column + "!");
		}
	}

	protected void addInteger(final int column, final int row, final int indata) {
		if (description.getType(column) == VDataDescription.DFNT_INT32) {
			final Object temp = indata;
			setCell(column, row, temp);
		} else { // uh oh... not right type
			throw new UnsupportedOperationException(WDFC + column + "!");
		}
	}

	private void putShortLoop(final ByteBuffer out, final int row, final int col) {
		for (int i = 0; i < order[col]; i++) {
			final short shortValue = (Short) (cells[col][row]);
			out.putShort(shortValue);
		}
	}

	private void putIntLoop(final ByteBuffer out, final int row, final int col) {
		for (int i = 0; i < order[col]; i++) {
			final int intValue = (Integer) (cells[col][row]);
			out.putInt(intValue);
		}
	}

	/*
	 * non-javadoc: Returns the byte representation for the cell indicated,
	 * assuming that an object resides at that cell already.
	 * 
	 * @param row record to retrieve from @param col column in record to
	 * Retrieve from
	 */
	private byte[] getBytes(final int row, final int col) {
		final ByteBuffer out = ByteBuffer.allocate(VDataDescription
				.getColumnByteLength(types[col], order[col]));
		switch (types[col]) {
		case VDataDescription.DFNT_INT16:
			putShortLoop(out, row, col);
			break;
		case VDataDescription.DFNT_INT32:
			putIntLoop(out, row, col);
			break;
		case VDataDescription.DFNT_FLT32:
			for (int i = 0; i < order[col]; i++) {
				final float floatValue = (Float) (cells[col][row]);
				out.putFloat(floatValue);
			}
			break;
		case VDataDescription.DFNT_DBL64:
			for (int i = 0; i < order[col]; i++) {
				final double doubleValue = (Double) (cells[col][row]);
				out.putDouble(doubleValue);
			}
			break;
		case VDataDescription.DFNT_CHAR8:
			final String string = (String) (cells[col][row]);
			out.put(STRING_UTIL.getASCIIarray(string));
			break;
		default:
			throw new IllegalStateException("Vdata.getBytes(" + row + "," + col
					+ ") data type not good.");
		}
		return out.array();
	}

	/*
	 * non-javadoc: Get the double in the specified cell. Of course, there'd
	 * better actually be a float there!
	 */
	protected Double getDouble(final int row, final int col) {
		Double out;
		if (types[col] == VDataDescription.DFNT_DBL64) {
			final int location = row * ivsize + offsets[col];
			out = bytes.getDouble(location);
		} else {
			throw new IllegalStateException(VS_STRING + getTag() + "/"
					+ getRef() + ".getFloat(" + row + "," + col
					+ "): cell not float!");
		}
		return out;
	}

	/*
	 * non-javadoc: Get the float in the specified cell. Of course, there'd
	 * better actually be a float there!
	 */
	protected Float getFloat(final int row, final int col) {
		Float out;
		if (types[col] == VDataDescription.DFNT_FLT32) {
			final int location = row * ivsize + offsets[col];
			out = bytes.getFloat(location);
		} else {
			throw new IllegalStateException(VS_STRING + getTag() + "/"
					+ getRef() + ".getFloat(" + row + "," + col
					+ "): cell not float!");
		}
		return out;
	}

	/**
	 * Get the <code>Integer</code> in the specified cell. Of course, there
	 * better actually be an <code>Integer</code> there!
	 * 
	 * @param row
	 *            of cell
	 * @param col
	 *            column of cell
	 * @return <code>Integer</code> residing at cell
	 * @throws IllegalStateException
	 *             if cell doesn't contain an <code>Integer</code>
	 */
	public int getInteger(final int row, final int col) {
		int result;
		if (types[col] == VDataDescription.DFNT_INT32) {
			final int location = row * ivsize + offsets[col];
			result = bytes.getInt(location);
		} else {
			throw new IllegalStateException(VS_STRING + getTag() + "/"
					+ getRef() + ".getInt(" + row + "," + col
					+ "): cell not integer!");
		}
		return result;
	}

	private Short getShort(final int row, final int col) {
		Short result;
		if (types[col] == VDataDescription.DFNT_INT32) {
			final int location = row * ivsize + offsets[col];
			result = bytes.getShort(location);
		} else {
			throw new IllegalStateException(VS_STRING + getTag() + "/"
					+ getRef() + ".getShort(" + row + "," + col
					+ "): cell not short!");
		}
		return result;
	}

	/**
	 * Get the <code>String</code> in the specified cell. Of course, there
	 * better actually be an <code>String</code> there!
	 * 
	 * @param row
	 *            of cell
	 * @param col
	 *            column of cell
	 * @return <code>String</code> residing at cell
	 * @throws IllegalStateException
	 *             if cell doesn't contain an <code>String</code>
	 */
	public String getString(final int row, final int col) {
		String out;
		if (types[col] == VDataDescription.DFNT_CHAR8) {
			final int location = row * ivsize + offsets[col];
			bytes.position(location);
			final int byteLength = order[col];
			final byte[] temp = new byte[byteLength];
			bytes.get(temp);
			out = STRING_UTIL.getASCIIstring(temp);
		} else {
			throw new IllegalStateException(VS_STRING + getTag() + "/"
					+ getRef() + ".getString(" + row + "," + col
					+ "): cell not string!");
		}
		return out;
	}

	@Override
	protected void init(final byte[] data, final short reference) {
		super.init(data, reference);
		description = getObject(VDataDescription.class, reference);
		description.interpretBytes();
		nfields = description.getNumFields();
		nvert = description.getNumRows();
		cells = new Object[nfields][nvert];
		ivsize = description.getRowSize();
		order = description.getDimensions();
		types = description.getTypes();
		offsets = description.getDataOffsets();
	}

	@Override
	protected void interpretBytes() throws HDFException {
		for (int col = 0; col < nfields; col++) {
			interpretColumnBytes(col);
		}
	}

	private void interpretColumnBytes(final int col) {
		final boolean order1 = order[col] == 1;
		switch (types[col]) {
		case VDataDescription.DFNT_INT32:
			setIntColumn(col, order1);
			break;
		case VDataDescription.DFNT_INT16:
			setShortColumn(col, order1);
			break;
		case VDataDescription.DFNT_FLT32:
			setFloatColumn(col, order1);
			break;
		case VDataDescription.DFNT_DBL64:
			setDoubleColumn(col, order1);
			break;
		case VDataDescription.DFNT_CHAR8:
			for (int row = 0; row < nvert; row++) {
				setCell(col, row, getString(row, col));
			}
			break;
		default:
			throw new IllegalStateException("Unknown data type!");
		}
	}

	/**
	 * Once all the cells have been filled with objects, this should be called
	 * to set the byte representation. The workhorse of this method is calls
	 * made to the <em>protected</em> method <code>getBytes(row,col)</code>.
	 */
	@Override
	protected void refreshBytes() {
		final int numBytes = nvert * ivsize;
		bytes = ByteBuffer.allocate(numBytes);
		for (int i = 0; i < nvert; i++) {
			for (int j = 0; j < nfields; j++) {
				bytes.put(getBytes(i, j));
			}
		}
	}

	protected void setCell(final int column, final int row, final Object indata) {
		cells[column][row] = indata;
	}

	private void setDoubleColumn(final int col, final boolean order1) {
		for (int row = 0; row < nvert; row++) {
			setCell(col, row, order1 ? getDouble(row, col)
					: new Double[1]);// NOPMD
		}
	}

	private void setFloatColumn(final int col, final boolean order1) {
		for (int row = 0; row < nvert; row++) {
			setCell(col, row, order1 ? getFloat(row, col)
					: new Float[1]);// NOPMD
		}
	}

	private void setIntColumn(final int col, final boolean order1) {
		for (int row = 0; row < nvert; row++) {
			setCell(col, row, order1 ? (Object) getInteger(row, col)
					: new Integer[1]);// NOPMD
		}
	}

	private void setShortColumn(final int col, final boolean order1) {
		for (int row = 0; row < nvert; row++) {
			setCell(col, row, order1 ? getShort(row, col)
					: new Short[1]);// NOPMD
		}
	}

	@Override
	public String toString() {
		return "Data:" + description.toString();
	}
}