package jam.io.hdf;

import jam.util.StringUtilities;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
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
	private Object[][] cells;

	private VDataDescription description;

	private short ivsize;

	private short nfields;

	private int nvert;

	private short[] offsets;

	private short[] order;

	private short[] types;

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

	void addChars(int column, int row, String indata) {
		if (description.getType(column) == VDataDescription.DFNT_CHAR8) {
			setCell(column, row, indata);
		} else { // uh oh... not right type
			throw new IllegalStateException("Wrong data type for column "
					+ column + "!");
		}
	}

	void addDouble(int column, int row, double indata) {
		Double temp;

		if (description.getType(column) == VDataDescription.DFNT_DBL64) {
			temp = new Double(indata);
			setCell(column, row, temp);
		} else { // uh oh... not right type
			throw new IllegalStateException("Wrong data type for column "
					+ column + "!");
		}
	}

	void addFloat(int column, int row, float indata) {
		Float temp;

		if (description.getType(column) == VDataDescription.DFNT_FLT32) {
			temp = new Float(indata);
			setCell(column, row, temp);
		} else { // uh oh... not right type
			throw new IllegalStateException("Wrong data type for column "
					+ column + "!");
		}
	}

	void addInteger(int column, int row, int indata) {
		Integer temp;

		if (description.getType(column) == VDataDescription.DFNT_INT32) {
			temp = new Integer(indata);
			setCell(column, row, temp);
		} else { // uh oh... not right type
			throw new UnsupportedOperationException(
					"Wrong data type for column " + column + "!");
		}
	}

	/*
	 * non-javadoc: Returns the byte representation for the cell indicated,
	 * assuming that an object resides at that cell already.
	 * 
	 * @param row record to retrieve from @param col column in record to
	 * retreive from
	 */
	private byte[] getBytes(int row, int col) {
		final ByteBuffer out = ByteBuffer.allocate(VDataDescription
				.getColumnByteLength(types[col], order[col]));
		switch (types[col]) {
		case VDataDescription.DFNT_INT16:
			for (int i = 0; i < order[col]; i++) {
				final short shortValue = ((Short) (cells[col][row]))
						.shortValue();
				out.putShort(shortValue);
			}
			break;
		case VDataDescription.DFNT_INT32:
			for (int i = 0; i < order[col]; i++) {
				final int intValue = ((Integer) (cells[col][row])).intValue();
				out.putInt(intValue);
			}
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
	Double getDouble(int row, int col) throws HDFException {
		Double out = null;
		if (types[col] == VDataDescription.DFNT_DBL64) {
			final int location = row * ivsize + offsets[col];
			final int len = 8;
			final byte[] temp = new byte[len];
			System.arraycopy(bytes.array(), location, temp, 0, len);
			final ByteArrayInputStream bis = new ByteArrayInputStream(temp);
			final DataInputStream dis = new DataInputStream(bis);
			double tempDouble = 0.0;
			try {
				tempDouble = dis.readDouble();
			} catch (IOException ioe) {
				throw new HDFException("VData", ioe);
			}
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
	Float getFloat(int row, int col) throws HDFException {
		Float out = null;
		if (types[col] == VDataDescription.DFNT_FLT32) {
			final int len = 4;
			final byte[] temp = new byte[len];
			final int location = row * ivsize + offsets[col];
			System.arraycopy(bytes.array(), location, temp, 0, len);
			final ByteArrayInputStream bis = new ByteArrayInputStream(temp);
			final DataInputStream dis = new DataInputStream(bis);
			float tempFloat = 0.0f;
			try {
				tempFloat = dis.readFloat();
			} catch (IOException ioe) {
				throw new HDFException("VData Read Float ", ioe);
			}
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
	 * @throws HDFException
	 *             for probelm reading data
	 */
	public Integer getInteger(int row, int col) throws HDFException {
		Integer out = null;
		if (types[col] == VDataDescription.DFNT_INT32) {
			final int location = row * ivsize + offsets[col];
			final int len = 4;
			final byte[] temp = new byte[len];
			System.arraycopy(bytes.array(), location, temp, 0, len);
			final ByteArrayInputStream bis = new ByteArrayInputStream(temp);
			final DataInputStream dis = new DataInputStream(bis);
			int tempN = 0;
			try {
				tempN = dis.readInt();
			} catch (IOException ioe) {
				throw new HDFException("VData Read Int ", ioe);
			}
			out = new Integer(tempN);
		} else {
			throw new IllegalStateException(VS_STRING + getTag() + "/"
					+ getRef() + ".getInt(" + row + "," + col
					+ "): cell not integer!");
		}
		return out;
	}

	private Short getShort(int row, int col) throws HDFException {
		Short rval = null;
		if (types[col] == VDataDescription.DFNT_INT32) {
			final int location = row * ivsize + offsets[col];
			final int shortLength = 2;
			final byte[] temp = new byte[shortLength];
			System.arraycopy(bytes.array(), location, temp, 0, shortLength);
			final ByteArrayInputStream bis = new ByteArrayInputStream(temp);
			final DataInputStream dis = new DataInputStream(bis);
			short value = 0;
			try {
				value = dis.readShort();
				dis.close();
			} catch (IOException ioe) {
				throw new HDFException("VData Read Short ", ioe);
			}
			rval = new Short(value);
		} else {
			throw new IllegalStateException(VS_STRING + getTag() + "/"
					+ getRef() + ".getInt(" + row + "," + col
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
	public String getString(int row, int col) {
		String out = null;
		if (types[col] == VDataDescription.DFNT_CHAR8) {
			final int location = row * ivsize + offsets[col];
			final int byteLength = order[col];
			final byte[] temp = new byte[byteLength];
			System.arraycopy(bytes.array(), location, temp, 0, byteLength);
			out = StringUtilities.getInstance().getASCIIstring(temp);
		} else {
			throw new IllegalStateException(VS_STRING + getTag() + "/"
					+ getRef() + ".getString(" + row + "," + col
					+ "): cell not string!");
		}
		return out;
	}

	void init(byte[] data, short tagType, short reference) {
		super.init(data, tagType, reference);
		description = (VDataDescription) (getObject(DFTAG_VH, reference));
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

	private void interpretColumnBytes(int col) throws HDFException {
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

	void setCell(int column, int row, Object indata) {
		cells[column][row] = indata;
	}

	private void setDoubleColumn(int col, boolean order1) throws HDFException {
		for (int row = 0; row < nvert; row++) {
			setCell(col, row, order1 ? (Object) getDouble(row, col)
					: new Double[1]);
		}
	}

	private void setFloatColumn(int col, boolean order1) throws HDFException {
		for (int row = 0; row < nvert; row++) {
			setCell(col, row, order1 ? (Object) getFloat(row, col)
					: new Float[1]);
		}
	}

	private void setIntColumn(int col, boolean order1) throws HDFException {
		for (int row = 0; row < nvert; row++) {
			setCell(col, row, order1 ? (Object) getInteger(row, col)
					: new Integer[1]);
		}
	}

	private void setShortColumn(int col, boolean order1) throws HDFException {
		for (int row = 0; row < nvert; row++) {
			setCell(col, row, order1 ? (Object) getShort(row, col)
					: new Short[1]);
		}
	}

	public String toString() {
		return "Data:" + description.toString();
	}
}