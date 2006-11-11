package jam.io.hdf;

import jam.util.StringUtilities;

import java.nio.ByteBuffer;

/**
 * Class to represent an HDF <em>Vdata description</em> data object.
 * 
 * @version 0.5 November 98
 * @author <a href="mailto:dale@visser.name">Dale Visser </a>
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

	VData(VDataDescription vdd) {
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

	void addChars(final int column, final int row, final String indata) {
		if (description.getType(column) == VDataDescription.DFNT_CHAR8) {
			setCell(column, row, indata);
		} else { // uh oh... not right type
			throw new IllegalStateException(WDFC + column + "!");
		}
	}

	void addDouble(final int column, final int row, final double indata) {
		if (description.getType(column) == VDataDescription.DFNT_DBL64) {
			final Object temp = new Double(indata);
			setCell(column, row, temp);
		} else { // uh oh... not right type
			throw new IllegalStateException(WDFC + column + "!");
		}
	}

	void addFloat(final int column, final int row, final float indata) {
		if (description.getType(column) == VDataDescription.DFNT_FLT32) {
			final Object temp = new Float(indata);
			setCell(column, row, temp);
		} else { // uh oh... not right type
			throw new IllegalStateException(WDFC + column + "!");
		}
	}

	void addInteger(final int column, final int row, final int indata) {
		if (description.getType(column) == VDataDescription.DFNT_INT32) {
			final Object temp = Integer.valueOf(indata);
			setCell(column, row, temp);
		} else { // uh oh... not right type
			throw new UnsupportedOperationException(WDFC + column + "!");
		}
	}

	private void putShortLoop(final ByteBuffer out, final int row, final int col) {
		for (int i = 0; i < order[col]; i++) {
			final short shortValue = ((Short) (cells[col][row])).shortValue();
			out.putShort(shortValue);
		}
	}

	private void putIntLoop(final ByteBuffer out, final int row, final int col) {
		for (int i = 0; i < order[col]; i++) {
			final int intValue = ((Integer) (cells[col][row])).intValue();
			out.putInt(intValue);
		}
	}

	/*
	 * non-javadoc: Returns the byte representation for the cell indicated,
	 * assuming that an object resides at that cell already.
	 * 
	 * @param row record to retrieve from @param col column in record to
	 * retreive from
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
				final float floatValue = ((Float) (cells[col][row]))
						.floatValue();
				out.putFloat(floatValue);
			}
			break;
		case VDataDescription.DFNT_DBL64:
			for (int i = 0; i < order[col]; i++) {
				final double doubleValue = ((Double) (cells[col][row]))
						.doubleValue();
				out.putDouble(doubleValue);
			}
			break;
		case VDataDescription.DFNT_CHAR8:
			final String string = (String) (cells[col][row]);
			out.put(StringUtilities.getInstance().getASCIIarray(string));
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
	Double getDouble(final int row, final int col) {
		Double out = null;
		if (types[col] == VDataDescription.DFNT_DBL64) {
			final int location = row * ivsize + offsets[col];
			final double tempDouble = bytes.getDouble(location);// dis.readDouble();
			out = new Double(tempDouble);
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
	Float getFloat(final int row, final int col) {
		Float out = null;
		if (types[col] == VDataDescription.DFNT_FLT32) {
			final int location = row * ivsize + offsets[col];
			final float tempFloat = bytes.getFloat(location);
			out = new Float(tempFloat);
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
		int rval = 0;
		if (types[col] == VDataDescription.DFNT_INT32) {
			final int location = row * ivsize + offsets[col];
			rval = bytes.getInt(location);
		} else {
			throw new IllegalStateException(VS_STRING + getTag() + "/"
					+ getRef() + ".getInt(" + row + "," + col
					+ "): cell not integer!");
		}
		return rval;
	}

	private Short getShort(final int row, final int col) {
		Short rval = null;
		if (types[col] == VDataDescription.DFNT_INT32) {
			final int location = row * ivsize + offsets[col];
			final short value = bytes.getShort(location);
			rval = new Short(value);
		} else {
			throw new IllegalStateException(VS_STRING + getTag() + "/"
					+ getRef() + ".getShort(" + row + "," + col
					+ "): cell not short!");
		}
		return rval;
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
		String out = null;
		if (types[col] == VDataDescription.DFNT_CHAR8) {
			final int location = row * ivsize + offsets[col];
			bytes.position(location);
			final int byteLength = order[col];
			final byte[] temp = new byte[byteLength];
			bytes.get(temp);
			out = StringUtilities.getInstance().getASCIIstring(temp);
		} else {
			throw new IllegalStateException(VS_STRING + getTag() + "/"
					+ getRef() + ".getString(" + row + "," + col
					+ "): cell not string!");
		}
		return out;
	}

	void init(final byte[] data, final short reference) {
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
	 * made to the <it>protected </it> method <code>getBytes(row,col)</code>.
	 */
	void refreshBytes() {
		final int numBytes = nvert * ivsize;
		bytes = ByteBuffer.allocate(numBytes);
		for (int i = 0; i < nvert; i++) {
			for (int j = 0; j < nfields; j++) {
				bytes.put(getBytes(i, j));
			}
		}
	}

	void setCell(final int column, final int row, final Object indata) {
		cells[column][row] = indata;
	}

	private void setDoubleColumn(final int col, final boolean order1) {
		for (int row = 0; row < nvert; row++) {
			setCell(col, row, order1 ? (Object) getDouble(row, col)
					: new Double[1]);// NOPMD
		}
	}

	private void setFloatColumn(final int col, final boolean order1) {
		for (int row = 0; row < nvert; row++) {
			setCell(col, row, order1 ? (Object) getFloat(row, col)
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
			setCell(col, row, order1 ? (Object) getShort(row, col)
					: new Short[1]);// NOPMD
		}
	}

	public String toString() {
		return "Data:" + description.toString();
	}
}