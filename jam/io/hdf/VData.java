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

    private VDataDescription description;

    /**
     * The vector of fields. Contains the useful java representations of the
     * objects.
     */
    private Object[][] cells;

    private int nvert;

    private short[] order;

    private short nfields;

    private short ivsize;

    private short[] types;

    private short[] offsets;

    private static final String VS_STRING = "VS_";

    VData(VDataDescription vdd) {
        super(DFTAG_VS); //sets tag
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

    VData() {
        super(DFTAG_VS);
    }

    void init(byte[] data, short tag, short reference) {
        super.init(data, tag, reference);
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

    void addInteger(int column, int row, int indata) {
        Integer temp;

        if (description.getType(column) == VDataDescription.DFNT_INT32) {
            temp = new Integer(indata);
            setCell(column, row, temp);
        } else { //uh oh... not right type
            throw new UnsupportedOperationException(
                    "Wrong data type for column " + column + "!");
        }
    }

    void addFloat(int column, int row, float indata) {
        Float temp;

        if (description.getType(column) == VDataDescription.DFNT_FLT32) {
            temp = new Float(indata);
            setCell(column, row, temp);
        } else { //uh oh... not right type
            throw new IllegalStateException("Wrong data type for column "
                    + column + "!");
        }
    }
    void addDouble(int column, int row, double indata) {
        Double temp;

        if (description.getType(column) == VDataDescription.DFNT_DBL64) {
            temp = new Double(indata);
            setCell(column, row, temp);
        } else { //uh oh... not right type
            throw new IllegalStateException("Wrong data type for column "
                    + column + "!");
        }
    }

    void addChars(int column, int row, String indata) {
        if (description.getType(column) == VDataDescription.DFNT_CHAR8) {
            setCell(column, row, indata);
        } else { //uh oh... not right type
            throw new IllegalStateException("Wrong data type for column "
                    + column + "!");
        }
    }

    void setCell(int column, int row, Object indata) {
        cells[column][row] = indata;
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
            setIntColumn(col,order1);
            break;
        case VDataDescription.DFNT_INT16:
            setShortColumn(col,order1);
            break;
        case VDataDescription.DFNT_FLT32:
            setFloatColumn(col,order1);
            break;
        case VDataDescription.DFNT_DBL64:
            setDoubleColumn(col,order1);
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
    
    private void setFloatColumn(int col, boolean order1) throws HDFException {
        for (int row = 0; row < nvert; row++) {
            setCell(col, row, order1 ? (Object) getFloat(row, col)
                    : new Float[1]);
        }
    }
    
    private void setDoubleColumn(int col, boolean order1) throws HDFException {
        for (int row = 0; row < nvert; row++) {
            setCell(col, row, order1 ? (Object) getDouble(row, col)
                    : new Double[1]);
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
    
    /*
     * non-javadoc: Returns the byte representation for the cell indicated,
     * assuming that an object resides at that cell already.
     * 
     * @param row record to retrieve from @param col column in record to
     * retreive from
     */
    private byte [] getBytes(int row, int col){
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
                final int intValue = ((Integer) (cells[col][row]))
                        .intValue();
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
            out.put(StringUtilities.instance().getASCIIarray(string));
            break;
        default:
            throw new IllegalStateException("Vdata.getBytes(" + row + ","
                    + col + ") data type not good.");
        }
        return out.array();
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
        int location;
        int length;
        byte[] temp;

        String out = null;
        if (types[col] == VDataDescription.DFNT_CHAR8) {
            location = row * ivsize + offsets[col];
            length = order[col];
            temp = new byte[length];
            System.arraycopy(bytes.array(), location, temp, 0, length);
            out = StringUtilities.instance().getASCIIstring(temp);
        } else {
            throw new IllegalStateException(VS_STRING + getTag() + "/"
                    + getRef() + ".getString(" + row + "," + col
                    + "): cell not string!");
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
     * @throws HDFException for probelm reading data
     */
    public Integer getInteger(int row, int col) throws HDFException {
        int location;
        final int length = 4;
        byte[] temp;
        ByteArrayInputStream bis;
        DataInputStream dis;

        Integer out = null;
        if (types[col] == VDataDescription.DFNT_INT32) {
            location = row * ivsize + offsets[col];
            temp = new byte[length];
            System.arraycopy(bytes.array(), location, temp, 0, length);
            bis = new ByteArrayInputStream(temp);
            dis = new DataInputStream(bis);
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

    /*
     * non-javadoc: Get the float in the specified cell. Of course, there'd
     * better actually be a float there!
     */
    Float getFloat(int row, int col) throws HDFException {
        int location;
        final int length = 4;
        byte[] temp;
        ByteArrayInputStream bis;
        DataInputStream dis;

        Float out = null;
        if (types[col] == VDataDescription.DFNT_FLT32) {
            location = row * ivsize + offsets[col];
            temp = new byte[length];
            System.arraycopy(bytes.array(), location, temp, 0, length);
            bis = new ByteArrayInputStream(temp);
            dis = new DataInputStream(bis);
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

    /*
     * non-javadoc: Get the double in the specified cell. Of course, there'd
     * better actually be a float there!
     */
    Double getDouble(int row, int col) throws HDFException {
        int location;
        final int length = 8;
        byte[] temp;
        ByteArrayInputStream bis;
        DataInputStream dis;

        Double out = null;
        if (types[col] == VDataDescription.DFNT_DBL64) {
            location = row * ivsize + offsets[col];
            temp = new byte[length];
            System.arraycopy(bytes.array(), location, temp, 0, length);
            bis = new ByteArrayInputStream(temp);
            dis = new DataInputStream(bis);
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
     * non-javadoc: short to array of bytes
     */
//    private byte[] shortToBytes(short num) {
//        final byte[] out = new byte[2];
//        out[0] = (byte) ((num >>> 8) & 0xFF);
//        out[1] = (byte) ((num >>> 0) & 0xFF);
//        return out;
//    }

    /*
     * non-javadoc: int to array of bytes
     */
//    private byte[] intToBytes(int num) {
//        final byte[] out = new byte[4];
//        out[0] = (byte) ((num >>> 24) & 0xFF);
//        out[1] = (byte) ((num >>> 16) & 0xFF);
//        out[2] = (byte) ((num >>> 8) & 0xFF);
//        out[3] = (byte) ((num >>> 0) & 0xFF);
//        return out;
//    }

    /*
     * non-javadoc: long to array of bytes
     */
//    private byte[] longToBytes(long num) {
//        final byte[] out = new byte[8];
//        out[0] = (byte) ((num >>> 56) & 0xFF);
//        out[1] = (byte) ((num >>> 48) & 0xFF);
//        out[2] = (byte) ((num >>> 40) & 0xFF);
//        out[3] = (byte) ((num >>> 32) & 0xFF);
//        out[4] = (byte) ((num >>> 24) & 0xFF);
//        out[5] = (byte) ((num >>> 16) & 0xFF);
//        out[6] = (byte) ((num >>> 8) & 0xFF);
//        out[7] = (byte) ((num >>> 0) & 0xFF);
//        return out;
//    }

//    private byte[] floatToBytes(float num) {
//        final int tempInt = Float.floatToIntBits(num);
//        return intToBytes(tempInt);
//    }

//    private byte[] doubleToBytes(double num) {
//        final long tempLong = Double.doubleToLongBits(num);
//        return longToBytes(tempLong);
//    }

//    private byte[] charToBytes(char character) {
//        final byte out[] = new byte[1];
//        out[0] = (byte) character;
//        return out;
//    }

    public String toString() {
        return "Data:" + description.toString();
    }
}