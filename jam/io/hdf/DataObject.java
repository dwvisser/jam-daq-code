package jam.io.hdf;

/**
 * Abstract class to represent a generic HDF data object.
 *
 * @version	0.5 November 98
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @since       JDK1.1
 */
public abstract class DataObject {

	/**
	 * contains the 2-byte tag for the data type
	 */
	protected short tag;
	
	/**
	 * for use in Maps
	 */
	protected Short tagKey;

	/**
	 * Tag for machine type.  
	 *
	 * @see JavaMachineType
	 */
	public final static short DFTAG_MT = 107;

	/**
	 * HDF tag for Data identifier annotation
	 *
	 * @see DataIDAnnotation
	 */
	public final static short DFTAG_DIA = 105;

	/**
	 * HDF tag for Data identifier label.
	 *
	 * @see DataIDLabel
	 */
	public final static short DFTAG_DIL = 104;

	/**
	 * HDF tag for File Identifier.
	 *
	 * @see FileIdentifier
	 */
	public final static short DFTAG_FID = 100;

	/**
	 * HDF tag for File Description.
	 *
	 * @see FileDescription
	 */
	public final static short DFTAG_FD = 101;

	/**
	 * HDF tag for number type.
	 *
	 * @see NumberType
	 */
	public final static short DFTAG_NT = 0x006a;

	/**
	 * HDF tag for Library version number
	 *
	 * @see LibVersion
	 */
	public final static short DFTAG_VERSION = 30;

	/**
	 * HDF tag for Numerical Data Group
	 *
	 * @see NumericalDataGroup
	 */
	public final static short DFTAG_NDG = 720;

	/**
	 * HDF tag for Scientific Data
	 *
	 * @see ScientificData
	 */
	public final static short DFTAG_SD = 702;

	/**
	 * HDF tag for Scientific data dimension records
	 *
	 * @see ScientificDataDimension
	 */
	public final static short DFTAG_SDD = 701;

	/**
	 * HDF tag for Scientific data labels
	 *
	 * @see ScientificDataLabel
	 */
	public final static short DFTAG_SDL = 704;

	/**
	 * HDF tag for Scientific data scales
	 */
	public final static short DFTAG_SDS = 703;

	/**
	 * HDF tag for Vgroup
	 *
	 * @see VirtualGroup
	 */
	public final static short DFTAG_VG = 1965;

	/**
	 * HDF tag for Vdata description
	 *
	 * @see VdataDescription
	 */
	public final static short DFTAG_VH = 1962;

	/**
	 * HDF tag for Vdata
	 *
	 * @see Vdata
	 */
	public final static short DFTAG_VS = 1963;

	/**
	  * Unique reference number, in case several data elements with the same tag exist. Only makes sense in 
	  * the context of an HDF file.
	  */
	protected short ref;
	
	protected Short refKey;

	/**
	 * Offset from start of file.
	 */
	protected int offset;

	/**
	 * Actual bytes stored in HDF file.
	 */
	protected byte[] bytes;

	/**
	 * Before bytes is created, length of bytes.
	 */
	protected int length;

	/**
	 * Reference to the particular <code>HDFile</code> this data element resides in.
	 */
	protected HDFile file;

	/**
	 * Creates a new HDF DataObject, belonging to the specified <code>HDFile</code>.  My approach is to have a 
	 * separate HDFile object for each physical HDF file on disk.  Each <code>HDFile</code> object
	 * handles the bookkeeping of the HDF File Header (see NCSA HDF: Specifications and Developer's Guide v3.2).
	 *
	 * @param	file	The file to contain the new object.
	 * @param	tag	The hdf tag of the new object.
	 */
	public DataObject(HDFile file, short tag) {
		this.file = file;
		setTag(tag);
		file.addDataObject(this, true); //ref gets set in this call
	}

	/**
	 * Creates a new <code>DataObject</code> with the specified byte array as the data which will (or does already) 
	 * physically
	 * reside in the file.
	 *
	 * @param	f	    The file to contain the new object.
	 * @param	data	    The byte representation of the data.
	 * @param	r   The unique value specifying the type of data object.
	 */
	public DataObject(HDFile f, byte[] data, short t, short r) {
		this.file = f;
		setTag(t);
		setRef(r);
		this.bytes = data;
		file.addDataObject(this, false);
	}

	/**
	 * Creates a new <code>DataObject</code> pointing to the offset in the file where its data reside.  This
	 * option is for when you don't want to hog memory with a large byte array.
	 *
	 * @param	file	    The file to contain the new object.
	 * @param	offset	    The location in <code>file</code>
	 * @param	reference   The unique value specifying the type of data object.
	 */
	public DataObject(HDFile file, int offset, int length, short t, short reference) {
		this.file = file;
		setTag(t);
		setRef(reference);
		this.offset = offset;
		this.length = length;
		file.addDataObject(this, false);
	}
	
	protected final void setTag(short t){
		tag=t;
		tagKey=new Short(t);
	}

	/**
	 * Returns the byte representation to be written at <code>offset</code> in the file.
	 *
	 * @exception   HDFException	    thrown if unrecoverable error occurs
	 */
	public byte[] getBytes() throws HDFException {
		return this.bytes;
	}

	/**
	 * Simply sets the bytes, usually performed after a read from a file.
	 */
	public void setBytes(byte[] bytes) {
		this.bytes = new byte[bytes.length];
		System.arraycopy(bytes, 0, this.bytes, 0, bytes.length);
	}

	/**
	 * When bytes are read from file, sets the internal fields of the data object.
	 *
	 * @exception   HDFException	    thrown if unrecoverable error occurs
	 */
	protected abstract void interpretBytes() throws HDFException;

	/**
	 * Returns the length of the byte array in the file for this data element.
	 */
	protected int getLength() {
		return bytes.length;
	}

	/**
	 * Returns a 4-byte representation of the tag, usually defined in the HDF standard, of this item.  
	 * The tag is a 2-byte integer
	 * denoting a unique data object type.
	 */
	public final short getTag() {
		return tag;
	}
	
	public final Short getTagKey(){
		return tagKey;
	}

	/**
	 * Returns a 2-byte representation of the reference number, which is unique for any given
	 * tag type in an HDF file.  In my code, it is unique, period, but the HDF standard does not 
	 * expect or require this.
	 */
	public final short getRef() {
		return ref;
	}
	
	public final Short getRefKey(){
		return refKey; 
	}
	
	protected boolean haveNotSetRef=true;
	
	public final void setRef(short newref) {
		if ((haveNotSetRef) || (ref!=newref)){
			final Short oldref=refKey;
			ref = newref;
			refKey=new Short(newref);
			/* only call "change" if this isn't the first time */
			if (!haveNotSetRef){	
				file.changeRefKey(this,oldref);
			}
			haveNotSetRef=false;
		}
	}

	/**
	 * Called back by <code>HDFile</code> to set the offset information.
	 */
	public void setOffset(int off) {
		offset = off;
	}

	public int getOffset() {
		return offset;
	}

	/**
	 * Gives the handle to the file holding this object.
	 */
	public HDFile getFile() {
		return file;
	}

}
