package jam.io.hdf;
import java.io.*;

/**
 * Class to represent an HDF <em>Vdata description</em> data object.
 *
 * @version	0.5 November 98
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @since       JDK1.1
 */
public class Vdata extends DataObject {

	VdataDescription description;

	/**
	 * The vector of fields.  Contains the useful java representations of the objects.
	 */
	Object[][] cells;

	/**
	 * An <code>order[]</code> size array of objects corresponding to the appropriate primitive type.
	 * <dl>
	 * <dt>int</dt><dd>Integer objects</dd>
	 * <dt>short</dt><dd>Short objects</dd>
	 * <dt>char</dt><dd>Character objects</dd>
	 * </dl>
	 */
	Object[] data;

	int nvert;
	short[] order;
	short nfields;
	short ivsize;
	short[] types;
	short[] offsets;
	/**
	 * Constructor
	 */
	public Vdata(HDFile fi, VdataDescription vdd) {
		super(fi, DFTAG_VS); //sets tag
		description = vdd;
		nfields = description.getNumFields();
		nvert = description.getNumRows();
		cells = new Object[nfields][nvert];
		//System.out.println("Vdata with "+nfields+" columns and "+nvert+" rows created.");
		ivsize = description.getRowSize();
		order = description.getDimensions();
		types = description.getTypes();
		offsets = description.getDataOffsets();
		/* undocumented by HDF group--data needs same ref as description */
		setRef(description.getRef());
	}
	
	/**
	 * Constructor.
	 */
	public Vdata(HDFile hdf, byte[] data, short t, short reference) {
		super(hdf, data, t, reference);
		//tag = DFTAG_VS;
		description = (VdataDescription) (hdf.getObject(DFTAG_VH, reference));
		description.interpretBytes();
		nfields = description.getNumFields();
		nvert = description.getNumRows();
		cells = new Object[nfields][nvert];
		ivsize = description.getRowSize();
		order = description.getDimensions();
		types = description.getTypes();
		offsets = description.getDataOffsets();
	}

	public void addInteger(int column, int row, int indata) {
		Integer temp;

		if (description.getType(column) == VdataDescription.DFNT_INT32) {
			temp = new Integer(indata);
			addCell(column, row, temp);
		} else { //uh oh... not right type
			System.err.println("Wrong data type for column " + column + "!");
		}
	}

	public void addIntegers(int column, int row, int[] indata) {
		Integer[] temp;
		int i;

		if (description.getType(column) == VdataDescription.DFNT_INT32) {
			temp = new Integer[indata.length];
			for (i = 0; i < indata.length; i++) {
				temp[i] = new Integer(indata[i]);
			}
			addCell(column, row, temp);
		} else { //uh oh... not right type
			System.err.println("Wrong data type for column " + column + "!");
		}
	}

	public void addShort(int column, int row, short indata) {
		Short temp;

		if (description.getType(column) == VdataDescription.DFNT_INT16) {
			temp = new Short(indata);
			addCell(column, row, temp);
		} else { //uh oh... not right type
			System.err.println("Wrong data type for column " + column + "!");
		}
	}

	public void addShorts(int column, int row, short[] indata) {
		Short[] temp;
		int i;

		if (description.getType(column) == VdataDescription.DFNT_INT16) {
			temp = new Short[indata.length];
			for (i = 0; i < indata.length; i++) {
				temp[i] = new Short(indata[i]);
			}
			addCell(column, row, temp);
		} else { //uh oh... not right type
			System.err.println("Wrong data type for column " + column + "!");
		}
	}

	public void addFloat(int column, int row, float indata) {
		Float temp;

		if (description.getType(column) == VdataDescription.DFNT_FLT32) {
			temp = new Float(indata);
			addCell(column, row, temp);
		} else { //uh oh... not right type
			System.err.println("Wrong data type for column " + column + "!");
		}
	}

	public void addFloats(int column, int row, float[] indata) {
		Float[] temp;
		int i;

		if (description.getType(column) == VdataDescription.DFNT_FLT32) {
			temp = new Float[indata.length];
			for (i = 0; i < indata.length; i++) {
				temp[i] = new Float(indata[i]);
			}
			addCell(column, row, temp);
		} else { //uh oh... not right type
			System.err.println("Wrong data type for column " + column + "!");
		}
	}
	/**
	 * Add a doubles
	 */
	public void addDouble(int column, int row, double indata) {
		Double temp;
		if (description.getType(column) == VdataDescription.DFNT_DBL64) {
			temp = new Double(indata);
			addCell(column, row, temp);
		} else { //uh oh... not right type
			System.err.println("Wrong data type for column " + column + "!");
		}
	}
	/**
	 * Add an array of doubles
	 */
	public void addDoubles(int column, int row, double[] indata) {
		Double[] temp;
		int i;

		if (description.getType(column) == VdataDescription.DFNT_DBL64) {
			temp = new Double[indata.length];
			for (i = 0; i < indata.length; i++) {
				temp[i] = new Double(indata[i]);
			}
			addCell(column, row, temp);
		} else { //uh oh... not right type
			System.err.println("Wrong data type for column " + column + "!");
		}
	}

	public void addChars(int column, int row, String indata) {
		if (description.getType(column) == VdataDescription.DFNT_CHAR8) {
			addCell(column, row, indata);
		} else { //uh oh... not right type
			System.err.println("Wrong data type for column " + column + "!");
		}
	}

	public void addChar(int column, int row, char indata) {
		Character temp;

		if (description.getType(column) == VdataDescription.DFNT_CHAR8) {
			temp = new Character(indata);
			addCell(column, row, temp);
		} else { //uh oh... not right type
			System.err.println("Wrong data type for column " + column + "!");
		}
	}

	public void addCell(int column, int row, Object[] indata) {
		cells[column][row] = indata;
	}

	public void addCell(int column, int row, Object indata) {
		cells[column][row] = indata;
	}
	/**
	 *
	 */
	public void interpretBytes() {
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
					System.err.println("Unknown data type!");
			}
		}
	}

	/**
	 * Once all the cells have been filled with objects, this should be called to set the byte representation.
	 * The workhorse of this method is calls made to the <it>protected</it>
	 * method <code>getBytes(row,col)</code>.
	 */
	public void refreshBytes() {
		int i, j;
		int numBytes;
		ByteArrayOutputStream baos;
		DataOutputStream dos;

		numBytes = nvert * ivsize;
		baos = new ByteArrayOutputStream(numBytes);
		dos = new DataOutputStream(baos);
		try {
			for (i = 0; i < nvert; i++) {
				for (j = 0; j < nfields; j++) {
					dos.write(this.getBytes(i, j));
				}
			}
		} catch (IOException ioe) {
			System.err.println("Vdata.refreshBytes(): " + ioe);
		}
		bytes = baos.toByteArray();
	}

	/**
	 * Returns the byte representation for the cell indicated, 
	 * assuming that an object resides at that cell
	 * already.
	 *
	 * @param	row	record to retrieve from
	 * @param	col	column in record to retreive from
	 */
	protected byte[] getBytes(int row, int col) {

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
					System.err.println(
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
//						out[bOffset + 0] = tempOut[0];
//						out[bOffset + 1] = tempOut[1];
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
//						out[bOffset + 0] = tempOut[0];
//						out[bOffset + 1] = tempOut[1];
//						out[bOffset + 2] = tempOut[2];
//						out[bOffset + 3] = tempOut[3];
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
//						out[bOffset + 0] = tempOut[0];
//						out[bOffset + 1] = tempOut[1];
//						out[bOffset + 2] = tempOut[2];
//						out[bOffset + 3] = tempOut[3];
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
//						out[bOffset + 0] = tempOut[0];
//						out[bOffset + 1] = tempOut[1];
//						out[bOffset + 2] = tempOut[2];
//						out[bOffset + 3] = tempOut[3];
//						out[bOffset + 4] = tempOut[0];
//						out[bOffset + 5] = tempOut[1];
//						out[bOffset + 6] = tempOut[2];
//						out[bOffset + 7] = tempOut[3];
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
					System.err.println(
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
	 * Get the String in the specified cell.  Of course, there better actually be a String there!
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
			out = new String(temp);
		} else {
			System.err.println(
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
	 * Get the Integer in the specified cell.  Of course, there better actually be an Integer there!
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
				System.err.println("Vdata.getInteger(): " + ioe);
			}
			out = new Integer(n);
		} else {
			System.err.println(
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

	//FIXME 
	public Short getShort(int row, int col) {
		int location;
		int length = 2;
		byte[] temp;
		short n;
		ByteArrayInputStream bis;
		DataInputStream dis;

		Short out = null;
		if (types[col] == VdataDescription.DFNT_INT32) {
			location = row * ivsize + offsets[col];
			temp = new byte[length];
			System.arraycopy(bytes, location, temp, 0, length);
			bis = new ByteArrayInputStream(temp);
			dis = new DataInputStream(bis);
			n = 0;
			try {
				n = dis.readShort();
			} catch (IOException ioe) {
				System.err.println("Vdata.getShort(): " + ioe);
			}
			out = new Short(n);
		} else {
			System.err.println(
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
		return out;
	}

	/**
	 * Get the float in the specified cell.  Of course, there'd better actually be a float there!
	 */
	public Float getFloat(int row, int col) {
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
				System.err.println("Vdata.getFloat(): " + ioe);
			}
			out = new Float(f);
		} else {
			System.err.println(
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

	/**
	 * Get the double in the specified cell.  Of course, there'd better actually be a float there!
	 */
	public Double getDouble(int row, int col) {
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
				System.err.println("Vdata.getDouble(): " + ioe);
			}
			out = new Double(d);
		} else {
			System.err.println(
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

	/**
	 * Returns the offset in the in the byte array to interpret when constructing the specified cell.
	 * 
	 * @param	row	record to seek
	 * @param	col	element in record to seek
	 */
	protected int getCellOffset(int row, int col) {
		return row * ivsize + offsets[col];
	}

	/**
	 * short to array of bytes
	 */
	private byte[] shortToBytes(short s) {
		byte[] out = new byte[2];

		out[0] = (byte) ((s >>> 8) & 0xFF);
		out[1] = (byte) ((s >>> 0) & 0xFF);

		return out;
	}

	/**
	 * int to array of bytes
	 */
	private byte[] intToBytes(int i) {
		byte[] out = new byte[4];

		out[0] = (byte) ((i >>> 24) & 0xFF);
		out[1] = (byte) ((i >>> 16) & 0xFF);
		out[2] = (byte) ((i >>> 8) & 0xFF);
		out[3] = (byte) ((i >>> 0) & 0xFF);

		return out;
	}

	/**
	 * long to array of bytes
	 */
	private byte[] longToBytes(long l) {
		byte[] out = new byte[8];

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

	/**
	 * 
	 */
	private byte[] floatToBytes(float f) {
		int tempInt = Float.floatToIntBits(f);
		return intToBytes(tempInt);
	}
	/**
	 *
	 */
	private byte[] doubleToBytes(double d) {
		long tempLong = Double.doubleToLongBits(d);
		return longToBytes(tempLong);
	}
	/**
	 *
	 */
	private byte[] charToBytes(char c) {
		byte out[] = new byte[1];
		out[0] = (byte) c;
		//out = new byte [1];			
		//numBytes=1;
		//baos = new ByteArrayOutputStream(numBytes);
		//dos = new DataOutputStream(baos);
		//char [] carray = new char[1];
		//carray [0] = ((Character)(cells[col][row])).charValue();
		//dos.writeBytes(new String(carray));
		//out=baos.toByteArray();
		return out;

	}

}
