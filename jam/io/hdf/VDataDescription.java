package jam.io.hdf;

import java.nio.ByteBuffer;
import java.util.List;

import static jam.io.hdf.Constants.DFTAG_VH;

/**
 * Class to represent an HDF <em>Vdata description</em> data object.
 * 
 * @version 0.5 November 98
 * @author Dale Visser
 * @since JDK1.1
 */
public final class VDataDescription extends AbstractData {

	/**
	 * Specifies how data records are interlaced in the Vdata record.
	 * 
	 * @see #INTERLACE
	 */
	private transient short interlace;

	/**
	 * Default, records are written with fields adjacent.
	 */
	private final static short INTERLACE = 0;

	/*
	 * private final static short NO_INTERLACE = 1; Data is written field by
	 * field. I.e., field_1 for record 1, record 2, etc., then field_2...
	 */

	/**
	 * Type for <code>short</code>.
	 */
	final static short DFNT_INT16 = 22;// NOPMD

	/**
	 * Type for <code>int</code>.
	 */
	final static short DFNT_INT32 = 24;// NOPMD

	/**
	 * Type for <code>char</code>.
	 */
	final static short DFNT_CHAR8 = 4;// NOPMD

	/**
	 * Type for <code>float</code>.
	 */
	final static short DFNT_FLT32 = 5;// NOPMD

	/**
	 * Type for <code>double</code>.
	 */
	final static short DFNT_DBL64 = 6;// NOPMD

	/**
	 * Number of entries in Vdata.
	 */
	private transient int nvert;

	/**
	 * Size in bytes of one Vdata entry.
	 */
	private transient short ivsize;

	/**
	 * Number of fields to a Vdata entry.
	 */
	private transient short nfields;

	/**
	 * Array of types of data.
	 */
	private transient short[] datatypes;

	/**
	 * Size in bytes of field.
	 */
	private transient short[] isize;

	/**
	 * Offset in bytes of field.
	 */
	private transient short[] fieldOffsets;

	/**
	 * Order (number of separate items of _type) of field.
	 */
	private transient short[] order;

	/**
	 * Name of field.
	 */
	private transient String[] fldnm;

	/**
	 * Name of Vdata.
	 */
	private transient String name;

	/**
	 * Name of Vdata type.
	 */
	private transient String dataTypeName;

	/**
	 * Version of VFTAG_VH format used.
	 */
	private final static short VH_VERSION = 3;

	VDataDescription(final String name, final String classtype, final int size,
			final String[] names, final short[] types, final short[] orders) {
		super(DFTAG_VH); // sets tag
		/* Double check dimensionality */
		if (names.length == 0 || (names.length != types.length)
				|| (names.length != orders.length)) {
			throw new IllegalArgumentException(
					"All array parameters must have same length != 0.");
		}
		interlace = INTERLACE;
		nfields = (short) (names.length);
		fldnm = new String[nfields];
		System.arraycopy(names, 0, fldnm, 0, nfields);
		datatypes = new short[nfields];
		System.arraycopy(types, 0, datatypes, 0, nfields);
		order = new short[nfields];
		System.arraycopy(orders, 0, order, 0, nfields);
		nvert = size;
		this.name = name;
		dataTypeName = classtype;
		isize = new short[nfields];
		fieldOffsets = new short[nfields];
		ivsize = 0;
		setSizes(types);
		fieldOffsets[0] = 0;
		// see p. 6-42 HDF 4.1r2 specs
		int byteLength = 23 + 10 * nfields + name.length()
				+ dataTypeName.length();
		byteLength += fldnm[0].length();
		byteLength = setOffsets(byteLength);
		bytes = ByteBuffer.allocate(byteLength);
		bytes.putShort(interlace);
		bytes.putInt(nvert);
		bytes.putShort(ivsize);
		bytes.putShort(nfields);
		for (int i = 0; i < nfields; i++) {
			bytes.putShort(datatypes[i]);
		}
		for (int i = 0; i < nfields; i++) {
			bytes.putShort(isize[i]);
		}
		for (int i = 0; i < nfields; i++) {
			bytes.putShort(fieldOffsets[i]);
		}
		for (int i = 0; i < nfields; i++) {
			bytes.putShort(order[i]);
		}
		for (int i = 0; i < nfields; i++) {
			bytes.putShort((short) fldnm[i].length());
			putString(fldnm[i]);
		}
		// write out data number type
		bytes.putShort((short) name.length());
		putString(name);
		bytes.putShort((short) dataTypeName.length());
		putString(dataTypeName);
		final short short0 = (short) 0;
		bytes.putShort(short0); // no extension
		bytes.putShort(short0); // no extension
		bytes.putShort(VH_VERSION);
		bytes.putShort(short0); // unused bytes
		bytes.put((byte) 0); // unused additional (undocumented) byte
	}

	private int setOffsets(final int byteLength) {
		int rval = byteLength;
		for (int i = 1; i < nfields; i++) {
			fieldOffsets[i] = (short) (fieldOffsets[i - 1] + isize[i - 1]);
			rval += fldnm[i].length();
		}
		return rval;
	}

	private void setSizes(final short[] types) {
		for (int i = 0; i < nfields; i++) {// NOPMD
			isize[i] = getColumnByteLength(types[i], order[i]);
			ivsize += isize[i];
		}
	}

	/*
	 * given a data type and column length, return the number of bytes needed
	 * for storage
	 */
	protected static short getColumnByteLength(final short type,
			final short order) {
		final short rval;
		switch (type) {
		case DFNT_INT16:
			rval = (short) (order * 2);
			break;
		case DFNT_INT32:
			rval = (short) (order * 4);
			break;
		case DFNT_CHAR8:
			rval = order;
			break;
		case DFNT_FLT32:
			rval = (short) (order * 4);
			break;
		case DFNT_DBL64:
			rval = (short) (order * 8);
			break;
		default:
			throw new IllegalArgumentException("Unknown Vdata field type: "
					+ type);
		}
		return rval;
	}

	VDataDescription() {
		super(DFTAG_VH);
	}

	@Override
	public void interpretBytes() {
		bytes.rewind();
		interlace = bytes.getShort();
		nvert = bytes.getInt();
		ivsize = bytes.getShort();
		nfields = bytes.getShort();
		datatypes = new short[nfields];
		isize = new short[nfields];
		fieldOffsets = new short[nfields];
		order = new short[nfields];
		fldnm = new String[nfields];
		for (int i = 0; i < nfields; i++) {
			datatypes[i] = bytes.getShort();
		}
		for (int i = 0; i < nfields; i++) {
			isize[i] = bytes.getShort();
		}
		for (int i = 0; i < nfields; i++) {
			fieldOffsets[i] = bytes.getShort();
		}
		for (int i = 0; i < nfields; i++) {
			order[i] = bytes.getShort();
		}
		for (int i = 0; i < nfields; i++) {
			final short len = bytes.getShort();
			fldnm[i] = getString(len);
		}
		/* Write out data number type. */
		short len = bytes.getShort();
		name = getString(len);
		len = bytes.getShort();
		dataTypeName = getString(len);
		bytes.getShort(); // no extension
		bytes.getShort(); // no extension
		bytes.getShort(); // should be version(=VH_VERSION)
		bytes.getShort(); // no extension
	}

	protected short getNumFields() {
		return nfields;
	}

	/**
	 * Returns the number of rows in the table.
	 * 
	 * @return the number of rows in the table
	 */
	public int getNumRows() {
		return nvert;
	}

	protected short getType(final int field) {
		return datatypes[field];
	}

	protected short[] getDimensions() {
		final int len = order.length;
		final short[] rval = new short[len];
		System.arraycopy(order, 0, rval, 0, len);
		return rval;
	}

	protected short getRowSize() {
		return ivsize;
	}

	protected short[] getTypes() {
		final int len = datatypes.length;
		final short[] rval = new short[len];
		System.arraycopy(datatypes, 0, rval, 0, len);
		return rval;
	}

	protected String getName() {
		return name;
	}

	protected String getDataTypeName() {
		return dataTypeName;
	}

	/**
	 * Returns the <code>VdataDescription</code> with the name specified. Should
	 * only be called when the name is expected to be unique.
	 * 
	 * @param list
	 *            should contain only VdataDescription objects
	 * @param which
	 *            type string showing what kind of info is contained
	 * @return the data description with the given name
	 */
	static public VDataDescription ofName(final List<VDataDescription> list,
			final String which) {
		VDataDescription output = null;
		for (VDataDescription vdd : list) {
			if (vdd.getName().equals(which)) {
				output = vdd;
				break;
			}
		}
		return output;
	}

	protected short[] getDataOffsets() {
		final int len = fieldOffsets.length;
		final short[] rval = new short[len];
		System.arraycopy(fieldOffsets, 0, rval, 0, len);
		return rval;
	}

	@Override
	public String toString() {
		final char colon = ':';
		final String rval = dataTypeName + colon + name + colon + order.length
				+ " columns";
		return rval;
	}
}