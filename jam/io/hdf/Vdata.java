package jam.io.hdf;
import jam.util.StringUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.swing.JOptionPane;

/**
 * Class to represent an HDF <em>Vdata description</em> data object.
 *
 * @version	0.5 November 98
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @since       JDK1.1
 */
public final class Vdata extends DataObject {

	private VdataDescription description;

	/**
	 * The vector of fields.  Contains the useful java representations of the objects.
	 */
	private final Object[][] cells;

	private final int nvert;
	private final short[] order;
	private final short nfields;
	private final short ivsize;
	private final short[] types;
	private final short[] offsets;
	
	Vdata(VdataDescription vdd) {
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
	
	Vdata(byte[] data, short t, short reference) throws HDFException {
		super(data, t, reference);
		description = (VdataDescription) (getObject(DFTAG_VH, reference));
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

		if (description.getType(column) == VdataDescription.DFNT_INT32) {
			temp = new Integer(indata);
			addCell(column, row, temp);
		} else { //uh oh... not right type
			throw new UnsupportedOperationException("Wrong data type for column " + column + "!");
		}
	}

	void addFloat(int column, int row, float indata) {
		Float temp;

		if (description.getType(column) == VdataDescription.DFNT_FLT32) {
			temp = new Float(indata);
			addCell(column, row, temp);
		} else { //uh oh... not right type
			throw new IllegalStateException("Wrong data type for column " + column + "!");
		}
	}

	void addChars(int column, int row, String indata) {
		if (description.getType(column) == VdataDescription.DFNT_CHAR8) {
			addCell(column, row, indata);
		} else { //uh oh... not right type
			throw new IllegalStateException("Wrong data type for column " + column + "!");
		}
	}

	void addCell(int column, int row, Object indata) {
		cells[column][row] = indata;
	}
	
	protected void interpretBytes() throws HDFException {
		int row;

		for (int col = 0; col < nfields; col++) {
			switch (types[col]) {
				case VdataDescription.DFNT_INT32 :
					for (row = 0; row < nvert; row++) {
						if (order[col] == 1) {
							cells[col][row] = getInteger(row, col);
						} else {
							cells[col][row] = new Integer[1];
						}
					}
					break;
				case VdataDescription.DFNT_INT16 :
					for (row = 0; row < nvert; row++) {
						if (order[col] == 1) {
							cells[col][row] = getShort(row, col);
						} else {
							cells[col][row] = new Short[1];
						}
					}
					break;
				case VdataDescription.DFNT_FLT32 :
					for (row = 0; row < nvert; row++) {
						if (order[col] == 1) {
							cells[col][row] = getFloat(row, col);
						} else {
							cells[col][row] = new Float[1];
						}
					}
					break;
				case VdataDescription.DFNT_DBL64 :
					for (row = 0; row < nvert; row++) {
						if (order[col] == 1) {
							cells[col][row] = getDouble(row, col);
						} else {
							cells[col][row] = new Double[1];
						}
					}
					break;

				case VdataDescription.DFNT_CHAR8 :
					for (row = 0; row < nvert; row++) {
						cells[col][row] = getString(row, col);
					}
					break;

				default :
				throw new IllegalStateException("Unknown data type!");
			}
		}
	}

	/**
	 * Once all the cells have been filled with objects, this should be called to set the byte representation.
	 * The workhorse of this method is calls made to the <it>protected</it>
	 * method <code>getBytes(row,col)</code>.
	 */
	void refreshBytes() throws HDFException {
		final int numBytes = nvert * ivsize;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(numBytes);
		final DataOutputStream dos = new DataOutputStream(baos);
		try {
			for (int i = 0; i < nvert; i++) {
				for (int j = 0; j < nfields; j++) {
					dos.write(this.getBytes(i, j));
				}
			}
		} catch (IOException ioe) {
			throw new HDFException("Writing VData ", ioe);
		}
		bytes = baos.toByteArray();
	}

	/* non-javadoc:
	 * Returns the byte representation for the cell indicated, 
	 * assuming that an object resides at that cell
	 * already.
	 *
	 * @param	row	record to retrieve from
	 * @param	col	column in record to retreive from
	 */
	private byte[] getBytes(int row, int col) {

		int numBytes;
		byte[] tempOut;
		int bOffset;
		byte[] out = null;
		int size;
		if (order[col] == 1) {
			switch (types[col]) {
				case VdataDescription.DFNT_INT16 :
					short shortValue = ((Short) (cells[col][row])).shortValue();
					out = shortToBytes(shortValue);
					break;
				case VdataDescription.DFNT_INT32 :
					int intValue = ((Integer) (cells[col][row])).intValue();
					out = intToBytes(intValue);
					break;
				case VdataDescription.DFNT_FLT32 :
					float floatValue = ((Float) (cells[col][row])).floatValue();
					out = floatToBytes(floatValue);
					break;
				case VdataDescription.DFNT_DBL64 :
					double doubleValue =
						((Double) (cells[col][row])).doubleValue();
					out = doubleToBytes(doubleValue);
					break;
				case VdataDescription.DFNT_CHAR8 :
					char charValue =
						((Character) (cells[col][row])).charValue();
					out = charToBytes(charValue);
					break;
				default :
					throw new IllegalStateException(
						"Vdata.getBytes("
							+ row
							+ ","
							+ col
							+ ") data type not good.");
			}
		} else { // order > 1
			switch (types[col]) {
				case VdataDescription.DFNT_INT16 :
					size = 2;
					numBytes = size * order[col];
					out = new byte[numBytes];
					tempOut = new byte[size];
					bOffset = 0;
					for (int i = 0; i < order[col]; i++) {
						short shortValue =
							((Short) (cells[col][row])).shortValue();
						tempOut = shortToBytes(shortValue);
						System.arraycopy(tempOut,0,out,bOffset,size);
						bOffset += size;
					}
					break;
				case VdataDescription.DFNT_INT32 :
					size = 4;
					numBytes = size * order[col];
					out = new byte[numBytes];
					tempOut = new byte[size];
					bOffset = 0;
					for (int i = 0; i < order[col]; i++) {
						short shortValue =
							((Short) (cells[col][row])).shortValue();
						tempOut = shortToBytes(shortValue);
						System.arraycopy(tempOut,0,out,bOffset,size);
						bOffset += size;
					}
					break;
				case VdataDescription.DFNT_FLT32 :
					size = 4;
					numBytes = size * order[col];
					out = new byte[numBytes];
					tempOut = new byte[size];
					bOffset = 0;
					for (int i = 0; i < order[col]; i++) {
						float floatValue =
							((Float) (cells[col][row])).floatValue();
						tempOut = floatToBytes(floatValue);
						System.arraycopy(tempOut,0,out,bOffset,size);
						bOffset += size;

					}
					break;
				case VdataDescription.DFNT_DBL64 :
					size = 8;
					numBytes = size * order[col];
					out = new byte[numBytes];
					tempOut = new byte[size];
					bOffset = 0;
					for (int i = 0; i < order[col]; i++) {
						double doubleValue =
							((Double) (cells[col][row])).doubleValue();
						tempOut = doubleToBytes(doubleValue);
						System.arraycopy(tempOut,0,out,bOffset,size);
						bOffset += size;
					}
					break;
				case VdataDescription.DFNT_CHAR8 :
					numBytes = order[col];
					out = new byte[numBytes];
					bOffset = 0;
					String s = (String) (cells[col][row]);

					for (int i = 0; i < order[col]; i++) {
						char charValue = s.charAt(i);
						tempOut = charToBytes(charValue);
						out[bOffset] = tempOut[0];
						bOffset++;
					}
					break;
				default :
					throw new IllegalStateException(
						"Vdata.getBytes("
							+ row
							+ ","
							+ col
							+ ") data type not good.");
			}
		}
		return out;
	}

	/**
	 * Get the <code>String</code> in the specified cell.  Of course, there better actually 
	 * be an <code>String</code> there!
	 * 
	 * @param row of cell
	 * @param col column of cell
	 * @return <code>String</code> residing at cell
	 * @throws IllegalStateException if cell doesn't contain an <code>String</code>
	 */
	public String getString(int row, int col) {
		int location;
		int length;
		byte[] temp;
		
		String out = null;
		if (types[col] == VdataDescription.DFNT_CHAR8) {
			location = row * ivsize + offsets[col];
			length = order[col];
			temp = new byte[length];
			System.arraycopy(bytes, location, temp, 0, length);
			out = StringUtilities.instance().getASCIIstring(temp);
		} else {
			throw new IllegalStateException(
				"VS_"
					+ getTag()
					+ "/"
					+ getRef()
					+ ".getString("
					+ row
					+ ","
					+ col
					+ "): cell not string!");
		}
		return out;
	}

	/**
	 * Get the <code>Integer</code> in the specified cell.  Of course, there better actually 
	 * be an <code>Integer</code> there!
	 * 
	 * @param row of cell
	 * @param col column of cell
	 * @return <code>Integer</code> residing at cell
	 * @throws IllegalStateException if cell doesn't contain an <code>Integer</code>
	 */
	public Integer getInteger(int row, int col) {
		int location;
		int length = 4;
		byte[] temp;
		int n;
		ByteArrayInputStream bis;
		DataInputStream dis;

		Integer out = null;
		if (types[col] == VdataDescription.DFNT_INT32) {
			location = row * ivsize + offsets[col];
			temp = new byte[length];
			System.arraycopy(bytes, location, temp, 0, length);
			bis = new ByteArrayInputStream(temp);
			dis = new DataInputStream(bis);
			n = 0;
			try {
				n = dis.readInt();
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(null,ioe.getMessage(),
				getClass().getName(),JOptionPane.ERROR_MESSAGE);
			}
			out = new Integer(n);
		} else {
			throw new IllegalStateException(
				"VS_"
					+ getTag()
					+ "/"
					+ getRef()
					+ ".getInt("
					+ row
					+ ","
					+ col
					+ "): cell not integer!");
		}
		return out;
	}
	
	private Short getShort(int row, int col) {
		Short rval = null;
		if (types[col] == VdataDescription.DFNT_INT32) {
			final int location = row * ivsize + offsets[col];
			final int shortLength = 2;
			final byte [] temp = new byte[shortLength];
			System.arraycopy(bytes, location, temp, 0, shortLength);
			final ByteArrayInputStream bis = new ByteArrayInputStream(temp);
			final DataInputStream dis = new DataInputStream(bis);
			short value = 0;
			try {
				value = dis.readShort();
				dis.close();
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(null,ioe.getMessage(),
				getClass().getName(),JOptionPane.ERROR_MESSAGE);
			}
			rval = new Short(value);
		} else {
			throw new IllegalStateException(
				"VS_"
					+ getTag()
					+ "/"
					+ getRef()
					+ ".getInt("
					+ row
					+ ","
					+ col
					+ "): cell not short!");
		}
		return rval;
	}

	/* non-javadoc:
	 * Get the float in the specified cell.  Of course, there'd better actually be a float there!
	 */
	Float getFloat(int row, int col) {
		int location;
		int length = 4;
		byte[] temp;
		float f;
		ByteArrayInputStream bis;
		DataInputStream dis;

		Float out = null;
		if (types[col] == VdataDescription.DFNT_FLT32) {
			location = row * ivsize + offsets[col];
			temp = new byte[length];
			System.arraycopy(bytes, location, temp, 0, length);
			bis = new ByteArrayInputStream(temp);
			dis = new DataInputStream(bis);
			f = 0.0f;
			try {
				f = dis.readFloat();
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(null,ioe.getMessage(),
				getClass().getName(),JOptionPane.ERROR_MESSAGE);
			}
			out = new Float(f);
		} else {
			throw new IllegalStateException(
				"VS_"
					+ getTag()
					+ "/"
					+ getRef()
					+ ".getFloat("
					+ row
					+ ","
					+ col
					+ "): cell not float!");
		}
		return out;
	}

	/* non-javadoc:
	 * Get the double in the specified cell.  Of course, there'd better actually be a float there!
	 */
	private Double getDouble(int row, int col) throws HDFException {
		int location;
		int length = 8;
		byte[] temp;
		double d;
		ByteArrayInputStream bis;
		DataInputStream dis;

		Double out = null;
		if (types[col] == VdataDescription.DFNT_DBL64) {
			location = row * ivsize + offsets[col];
			temp = new byte[length];
			System.arraycopy(bytes, location, temp, 0, length);
			bis = new ByteArrayInputStream(temp);
			dis = new DataInputStream(bis);
			d = 0.0;
			try {
				d = dis.readDouble();
			} catch (IOException ioe) {
				throw new HDFException ("VData", ioe);
			}
			out = new Double(d);
		} else {
			throw new IllegalStateException(
				"VS_"
					+ getTag()
					+ "/"
					+ getRef()
					+ ".getFloat("
					+ row
					+ ","
					+ col
					+ "): cell not float!");
		}
		return out;
	}

	/* non-javadoc:
	 * short to array of bytes
	 */
	private byte[] shortToBytes(short s) {
		final byte[] out = new byte[2];
		out[0] = (byte) ((s >>> 8) & 0xFF);
		out[1] = (byte) ((s >>> 0) & 0xFF);
		return out;
	}

	/* non-javadoc:
	 * int to array of bytes
	 */
	private byte[] intToBytes(int i) {
		final byte[] out = new byte[4];
		out[0] = (byte) ((i >>> 24) & 0xFF);
		out[1] = (byte) ((i >>> 16) & 0xFF);
		out[2] = (byte) ((i >>> 8) & 0xFF);
		out[3] = (byte) ((i >>> 0) & 0xFF);
		return out;
	}

	/* non-javadoc:
	 * long to array of bytes
	 */
	private byte[] longToBytes(long l) {
		final byte[] out = new byte[8];
		out[0] = (byte) ((l >>> 56) & 0xFF);
		out[1] = (byte) ((l >>> 48) & 0xFF);
		out[2] = (byte) ((l >>> 40) & 0xFF);
		out[3] = (byte) ((l >>> 32) & 0xFF);
		out[4] = (byte) ((l >>> 24) & 0xFF);
		out[5] = (byte) ((l >>> 16) & 0xFF);
		out[6] = (byte) ((l >>> 8) & 0xFF);
		out[7] = (byte) ((l >>> 0) & 0xFF);
		return out;
	}

	private byte[] floatToBytes(float f) {
		final int tempInt = Float.floatToIntBits(f);
		return intToBytes(tempInt);
	}

	private byte[] doubleToBytes(double d) {
		final long tempLong = Double.doubleToLongBits(d);
		return longToBytes(tempLong);
	}

	private byte[] charToBytes(char c) {
		final byte out[] = new byte[1];
		out[0] = (byte) c;
		return out;
	}
	
	public String toString(){
	    return "Data:"+description.toString();
	}
}
