package jam.io.hdf;

import jam.util.StringUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Class to represent an HDF <em>Vdata description</em> data object.
 * 
 * @version 0.5 November 98
 * @author Dale Visser
 * @since JDK1.1
 */
public final class VdataDescription extends DataObject {

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

    VdataDescription(String name, String classtype, int size,
            String[] names, short[] types, short[] orders) throws HDFException {
        super(DFTAG_VH); //sets tag
        /* Double check dimensionality */
        if ((names.length != types.length) || (names.length != orders.length)) {
            throw new IllegalArgumentException(
                    "VdataDescription(): was not called with all same dimensions!");
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
            isize[i]=getIsize(types[i],order[i]);
            ivsize += isize[i];
        }
        offset[0] = 0;
        // see p. 6-42 HDF 4.1r2 specs
        int byteLength = 22 + 10 * nfields + name.length() + dataTypeName.length();
        for (int i = 1; i < nfields; i++) {
            offset[i] = (short) (offset[i - 1] + isize[i - 1]);
            byteLength += names[i].length();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(byteLength);
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeShort(interlace);
            dos.writeInt(nvert);
            dos.writeShort(ivsize);
            dos.writeShort(nfields);
            for (int i = 0; i < nfields; i++) {
                dos.writeShort(datatypes[i]);
            }
            for (int i = 0; i < nfields; i++) {
                dos.writeShort(isize[i]);
            }
            for (int i = 0; i < nfields; i++) {
                dos.writeShort(offset[i]);
            }
            for (int i = 0; i < nfields; i++) {
                dos.writeShort(order[i]);
            }
            for (int i = 0; i < nfields; i++) {
                dos.writeShort(fldnm[i].length());
                dos.writeBytes(fldnm[i]);
            }
            //write out data number type
            dos.writeShort(name.length());
            dos.writeBytes(name);
            dos.writeShort(dataTypeName.length());
            dos.writeBytes(dataTypeName);
            dos.writeShort(0); //no extension
            dos.writeShort(0); //no extension
            dos.writeShort(VH_VERSION);
            dos.writeShort(0); //unused bytes
            dos.writeByte(0); //unused additional (undocumented) byte
        } catch (IOException ioe) {
            throw new HDFException("Creating VDataDescription", ioe);
        }
        bytes = baos.toByteArray();
    }
    
    private short getIsize(short type, short order){
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
            throw new IllegalArgumentException(
                    "Unknown Vdata field type: "
                            + type);
        }
        return rval;
    }
    
    VdataDescription(){
        super();
    }

    public void interpretBytes() throws HDFException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final DataInputStream dis = new DataInputStream(bais);
        try {
            interlace = dis.readShort();
            nvert = dis.readInt();
            ivsize = dis.readShort();
            nfields = dis.readShort();
            datatypes = new short[nfields];
            isize = new short[nfields];
            offset = new short[nfields];
            order = new short[nfields];
            fldnm = new String[nfields];
            for (int i = 0; i < nfields; i++) {
                datatypes[i] = dis.readShort();
            }
            for (int i = 0; i < nfields; i++) {
                isize[i] = dis.readShort();
            }
            for (int i = 0; i < nfields; i++) {
                offset[i] = dis.readShort();
            }
            for (int i = 0; i < nfields; i++) {
                order[i] = dis.readShort();
            }
            final StringUtilities util = StringUtilities.instance();
            for (int i = 0; i < nfields; i++) {
                final short len = dis.readShort();
                /*final byte [] temp = new byte[len];
                dis.read(temp);*/
                fldnm[i] = readASCIIstring(dis,len);
            }
            /* Write out data number type. */
            short len = dis.readShort();
            byte [] temp = new byte[len];
            dis.read(temp);
            name = util.getASCIIstring(temp);
            len = dis.readShort();
            temp = new byte[len];
            dis.read(temp);
            dataTypeName = util.getASCIIstring(temp);
            dis.readShort(); //no extension
            dis.readShort(); //no extension
            dis.readShort(); //should be version(=VH_VERSION)
            dis.readShort(); //no extension
        } catch (IOException ioe) {
            throw new HDFException("Interpret VDataDescription", ioe);
        }
    }
    
    private String readASCIIstring(DataInputStream dataInput, int len)
    throws IOException {
        final StringUtilities util = StringUtilities.instance();
        final byte[] temp=new byte[len];
        dataInput.read(temp);
        return util.getASCIIstring(temp);
        
    }

    short getNumFields() {
        return nfields;
    }

    /**
     * Returns the number of rows in the table.
     * @return the number of rows in the table
     */
    public int getNumRows() {
        return nvert;
    }

    short getType(int field) {
        return datatypes[field];
    }

    short[] getDimensions() {
        final int len=order.length;
        final short [] rval=new short[len];
        System.arraycopy(order,0,rval,0,len);
        return rval;
    }

    short getRowSize() {
        return ivsize;
    }

    short[] getTypes() {
        final int len=datatypes.length;
        final short [] rval=new short[len];
        System.arraycopy(datatypes,0,rval,0,len);
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
        final int len=offset.length;
        final short [] rval=new short[len];
        System.arraycopy(offset,0,rval,0,len);
        return rval;
    }

    public String toString() {
        final char colon = ':';
        final String rval = dataTypeName + colon + name + colon + order.length
                + " columns";
        return rval;
    }
}