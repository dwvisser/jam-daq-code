package jam.io.hdf;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

/**
 * Class to represent an HDF <em>Vdata description</em> data object.
 * 
 * @version 0.5 November 98
 * @author Dale Visser
 * @since JDK1.1
 */
public final class VdataDescription extends AbstractHData {

    /**
     * Specifies how data records are interlaced in the Vdata record.
     * 
     * @see #INTERLACE
     * @see #NO_INTERLACE
     */
    private short interlace;

    /**
     * Default, records are written with fields adjacent.
     */
    final static short INTERLACE = 0;

    /**
     * Data is written field by field. I.e., field_1 for record 1, record 2,
     * etc., then field_2...
     */
    final static short NO_INTERLACE = 1;

    /**
     * Type for <code>short</code>.
     */
    final static short DFNT_INT16 = 22;

    /**
     * Type for <code>int</code>.
     */
    final static short DFNT_INT32 = 24;

    /**
     * Type for <code>char</code>.
     */
    final static short DFNT_CHAR8 = 4;

    /**
     * Type for <code>float</code>.
     */
    final static short DFNT_FLT32 = 5;

    /**
     * Type for <code>double</code>.
     */
    final static short DFNT_DBL64 = 6;

    /**
     * Number of entries in Vdata.
     */
    private int nvert;

    /**
     * Size in bytes of one Vdata entry.
     */
    private short ivsize;

    /**
     * Number of fields to a Vdata entry.
     */
    private short nfields;

    /**
     * Array of types of data.
     */
    private short[] datatypes;

    /**
     * Size in bytes of field.
     */
    private short[] isize;

    /**
     * Offset in bytes of field.
     */
    private short[] offset;

    /**
     * Order (number of separate items of _type) of field.
     */
    private short[] order;

    /**
     * Name of field.
     */
    private String[] fldnm;

    /**
     * Name of Vdata.
     */
    private String name;

    /**
     * Name of Vdata type.
     */
    private String dataTypeName;

    /**
     * Version of VFTAG_VH format used.
     */
    private final static short VH_VERSION = 3;

    VdataDescription(String name, String classtype, int size, String[] names,
            short[] types, short[] orders) {
        super(DFTAG_VH); //sets tag
        /* Double check dimensionality */
        if (names.length==0 || (names.length != types.length) || (names.length != orders.length)) {
            throw new IllegalArgumentException(
                    "All array parameters must have same length != 0.");
        }
        interlace = INTERLACE;
        fldnm = names;
        datatypes = types;
        order = orders;
        nvert = size;
        nfields = (short) (names.length);
        this.name = name;
        dataTypeName = classtype;
        isize = new short[nfields];
        offset = new short[nfields];
        ivsize = 0;
        for (int i = 0; i < nfields; i++) {
            isize[i] = getColumnByteLength(types[i], order[i]);
            ivsize += isize[i];
        }
        offset[0] = 0;
        // see p. 6-42 HDF 4.1r2 specs
        int byteLength = 23 + 10 * nfields + name.length()
                + dataTypeName.length();
        byteLength += fldnm[0].length();
        for (int i = 1; i < nfields; i++) {
            offset[i] = (short) (offset[i - 1] + isize[i - 1]);
            byteLength += fldnm[i].length();
        }
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
            bytes.putShort(offset[i]);
        }
        for (int i = 0; i < nfields; i++) {
            bytes.putShort(order[i]);
        }
        for (int i = 0; i < nfields; i++) {
            bytes.putShort((short) fldnm[i].length());
            putString(fldnm[i]);
        }
        //write out data number type
        bytes.putShort((short) name.length());
        putString(name);
        bytes.putShort((short) dataTypeName.length());
        putString(dataTypeName);
        final short short0 = (short) 0;
        bytes.putShort(short0); //no extension
        bytes.putShort(short0); //no extension
        bytes.putShort(VH_VERSION);
        bytes.putShort(short0); //unused bytes
        bytes.put((byte) 0); //unused additional (undocumented) byte
    }

    /* given a data type and column length, return the
     * number of bytes needed for storage
     */
    static short getColumnByteLength(short type, short order) {
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

    VdataDescription() {
        super();
    }

    public void interpretBytes() {
        bytes.rewind();
        interlace = bytes.getShort();
        nvert = bytes.getInt();
        ivsize = bytes.getShort();
        nfields = bytes.getShort();
        datatypes = new short[nfields];
        isize = new short[nfields];
        offset = new short[nfields];
        order = new short[nfields];
        fldnm = new String[nfields];
        for (int i = 0; i < nfields; i++) {
            datatypes[i] = bytes.getShort();
        }
        for (int i = 0; i < nfields; i++) {
            isize[i] = bytes.getShort();
        }
        for (int i = 0; i < nfields; i++) {
            offset[i] = bytes.getShort();
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
        bytes.getShort(); //no extension
        bytes.getShort(); //no extension
        bytes.getShort(); //should be version(=VH_VERSION)
        bytes.getShort(); //no extension
    }

    short getNumFields() {
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

    short getType(int field) {
        return datatypes[field];
    }

    short[] getDimensions() {
        final int len = order.length;
        final short[] rval = new short[len];
        System.arraycopy(order, 0, rval, 0, len);
        return rval;
    }

    short getRowSize() {
        return ivsize;
    }

    short[] getTypes() {
        final int len = datatypes.length;
        final short[] rval = new short[len];
        System.arraycopy(datatypes, 0, rval, 0, len);
        return rval;
    }

    private String getName() {
        return name;
    }

    /**
     * Returns the <code>VdataDescription</code> with the name specified.
     * Should only be called when the name is expected to be unique.
     * 
     * @param list
     *            should contain only VdataDescription objects
     * @param which
     *            type string showing what kind of info is contained
     * @return the data description with the given name
     */
    static public VdataDescription ofName(List list, String which) {
        VdataDescription output = null;
        for (final Iterator temp = list.iterator(); temp.hasNext();) {
            final VdataDescription vdd = (VdataDescription) (temp.next());
            if (vdd.getName().equals(which)) {
                output = vdd;
            }
        }
        return output;
    }

    short[] getDataOffsets() {
        final int len = offset.length;
        final short[] rval = new short[len];
        System.arraycopy(offset, 0, rval, 0, len);
        return rval;
    }

    public String toString() {
        final char colon = ':';
        final String rval = dataTypeName + colon + name + colon + order.length
                + " columns";
        return rval;
    }
}