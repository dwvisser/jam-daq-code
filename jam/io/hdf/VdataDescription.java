package jam.io.hdf;

import java.util.*;
import java.io.*;

/**
 * Class to represent an HDF <em>Vdata description</em> data object.
 *
 * @version	0.5 November 98
 * @author 	Dale Visser
 * @since       JDK1.1
 */
public class VdataDescription extends DataObject {

	/**
	 * Specifies how data records are interlaced in the Vdata record.
	 *
	 * @see #FULL_INTERLACE
	 * @see #NO_INTERLACE
	 */
	short interlace;

	/**
	 * Default, records are written with fields adjacent.
	 */
	public final static short FULL_INTERLACE = 0;

	/**
	 * Data is written field by field. I.e., field_1 for record 1, record 2, etc., then field_2...
	 */
	public final static short NO_INTERLACE = 1;

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
	int nvert;

	/**
	 * Size in bytes of one Vdata entry.
	 */
	short ivsize;

	/**
	 * Number of fields to a Vdata entry.
	 */
	short nfields;

	/**
	 * Array of types of data.
	 */
	short[] _type;

	/**
	 * Size in bytes of field.
	 */
	short[] isize;

	/**
	 * Offset in bytes of field.
	 */
	short[] offset;

	/**
	 * Order (number of separate items of _type) of field.
	 */
	short[] order;

	/**
	 * Name of field.
	 */
	String[] fldnm;

	/**
	 * Name of Vdata.
	 */
	String name;

	/**
	 * Name of Vdata type.
	 */
	String _class;

	/**
	 * Version of VFTAG_VH format used.
	 */
	final static short VH_VERSION = 3;

	/**
	 * 
	 */
	public VdataDescription(
		HDFile fi,
		String name,
		String classtype,
		int size,
		String[] names,
		short[] types,
		short[] orders) {
		super(fi, DFTAG_VH); //sets tag
		//System.out.println("VDD_"+tag+"/"+ref+"(out, '"+name+"', "+size+", names["+names.length+
		//"], types["+types.length+"], orders["+orders.length+"])");
		//Double check dimensionality
		if ((names.length != types.length)
			|| (names.length != orders.length)) {
			System.err.println(
				"VdataDescription(): was not called with all same dimensions!");
		} else { //OK
			interlace = FULL_INTERLACE;
			fldnm = names;
			_type = types;
			order = orders;
			nvert = size;
			nfields = (short) (names.length);
			this.name = name;
			_class = classtype;
			isize = new short[nfields];
			offset = new short[nfields];
			ivsize = 0;
			for (int i = 0; i < nfields; i++) {
				switch (types[i]) {
					case DFNT_INT16 :
						isize[i] = (short) (order[i] * 2);
						break;
					case DFNT_INT32 :
						isize[i] = (short) (order[i] * 4);
						break;
					case DFNT_CHAR8 :
						isize[i] = order[i];
						break;
					case DFNT_FLT32 :
						isize[i] = (short) (order[i] * 4);
						break;
					case DFNT_DBL64 :
						isize[i] = (short) (order[i] * 8);
						break;
					default :
						System.err.println(
							"Unknown Vdata field type!, field: "
								+ i
								+ ", type: "
								+ types[i]);
				}
				ivsize += isize[i];
			}
			offset[0] = 0;
			for (int i = 1; i < nfields; i++) {
				offset[i] = (short) (offset[i - 1] + isize[i - 1]);
			}

		}
		int byteLength = 22 + 10 * nfields + name.length() + _class.length();
		// see p. 6-42 HDF 4.1r2 specs
		for (int i = 0; i < nfields; i++) { // see p. 6-42 HDF 4.1r2 specs
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
				dos.writeShort(_type[i]);
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
			dos.writeShort(_class.length());
			dos.writeBytes(_class);
			dos.writeShort(0); //no extension
			dos.writeShort(0); //no extension
			dos.writeShort(VH_VERSION);
			dos.writeShort(0); //unused bytes
			dos.writeByte(0); //unused additional (undocumented) byte
		} catch (IOException ioe) {
			System.err.println(ioe);
		}
		bytes = baos.toByteArray();
	}

	public VdataDescription(HDFile hdf, byte[] data, short t, short reference) {
		super(hdf, data, t, reference);
		//tag = DFTAG_VH;
	}

	public void interpretBytes() {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		short len;
		byte[] temp;

		try {
			interlace = dis.readShort();
			nvert = dis.readInt();
			ivsize = dis.readShort();
			nfields = dis.readShort();
			_type = new short[nfields];
			isize = new short[nfields];
			offset = new short[nfields];
			order = new short[nfields];
			fldnm = new String[nfields];
			for (int i = 0; i < nfields; i++) {
				_type[i] = dis.readShort();
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
			for (int i = 0; i < nfields; i++) {
				len = dis.readShort();
				temp = new byte[len];
				dis.read(temp);
				fldnm[i] = new String(temp);
			}
			//write out data number type
			len = dis.readShort();
			temp = new byte[len];
			dis.read(temp);
			name = new String(temp);
			len = dis.readShort();
			temp = new byte[len];
			dis.read(temp);
			_class = new String(temp);
			dis.readShort(); //no extension
			dis.readShort(); //no extension
			dis.readShort(); //should be version(=VH_VERSION)
			dis.readShort(); //no extension
		} catch (IOException ioe) {
			System.err.println(ioe);
		}
	}

	public short getNumFields() {
		return nfields;
	}

	public int getNumRows() {
		return nvert;
	}

	public short getType(int field) {
		return _type[field];
	}

	public short[] getDimensions() {
		return order;
	}

	public short getRowSize() {
		return ivsize;
	}

	public short[] getTypes() {
		return _type;
	}

	public String getName() {
		return name;
	}

	/**
	 * Returns the <code>VdataDescription</code> with the 
	 * name specified.  Should only be called when the name is expected to be
	 * unique.
	 *
	 * @param in should contain only VdataDescription objects
	 * @param which type string showing what kind of info is contained
	 */
	static public VdataDescription ofName(List in, String which) {
		VdataDescription output = null;
		for (Iterator temp = in.iterator(); temp.hasNext();) {
			VdataDescription vdd = (VdataDescription) (temp.next());
			if (vdd.getName().equals(which)) {
				output = vdd;
			}
		}
		return output;
	}

	public short[] getDataOffsets() {
		return offset;
	}

}
