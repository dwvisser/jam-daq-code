package jam.io.hdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class to represent a generic HDF data object.
 *
 * @version	0.5 November 98
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @since       JDK1.1
 */
public abstract class DataObject {

	/**
	 * List of objects in the file.
	 */
	private static List objectList=Collections.synchronizedList(new ArrayList());
	private static Map tagRefMap=Collections.synchronizedMap(new HashMap());
	
	static short refCount;
	
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
	
	/**
	 * Object version of <code>ref</code>.
	 * 
	 * @see #ref
	 */
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

	static List getDataObjectList() {
		return objectList;
	}
	static void clear() {
		objectList.clear();
		tagRefMap.clear();
		refCount =0;

	}
	/**
	 * <p>Adds the data object to the file.  The reference number is 
	 * implicitly assigned at this time as the index number in the 
	 * internal <code>Vector objectList</code>.  A typical call should 
	 * look like:</p>
	 * <blockquote><code>hdf = new hdfFile(outfile, "rw");<br>
	 * hdf.addDataObject(new DataObject(this));</code></blockquote>
	 * <p>Each call causes setOffsets to be called.</p>
	 *
	 * @param data data object
	 * @param useFileDefault	if true, automatically assigns ref number, 
	 * else lets object assign its own
	 * @see	#setOffsets()
	 */
	static void addDataObject(DataObject data, boolean useFileDefault) {
		if (useFileDefault) {
			data.setRef(getUniqueRef());
		}
		
		final Integer key =data.getKey();
		if (!tagRefMap.containsKey(key)){
			tagRefMap.put(key, data);
			objectList.add(data);
		}
	}
	/**
	 * @return object in file with the matching tag and ref
	 * @param t tag of HDF object
	 * @param r <em>unique</em> reference number in file
	 */
	public static DataObject getObject(short t, short r) {
		DataObject match = null;
		final Integer key =calculateKey(t, r);		
		if (tagRefMap.containsKey(key)){
			match=(DataObject)tagRefMap.get(key);
		}
		return match;
	}
	static void changeRefKey(DataObject d, short refOld) {
		
		short tag =d.getTag();
		short refNew=d.getRef();
		 Integer key = calculateKey(tag, refOld);
		 if (tagRefMap.containsKey(key)) {
		 	tagRefMap.remove(key);
		 }
		 Integer keyNew = calculateKey(tag, refNew);
		tagRefMap.put(key, d);
		/*
		final Short tag=d.getTagKey();
		final Map refs=(Map)tagRefMap.get(tag);
		if (refs.containsKey(old)){
			refs.remove(old);
		}
		// if old not there, we were just called as the object was being
		 // added to the file...no worries 
		final Short ref=d.getRefKey();
		if (!refs.containsKey(ref)){
			refs.put(ref,d);
		} else {
			throw new IllegalStateException("Trying to put: "+ref+
			"for tag:"+tag+" when one already exists.");
		}
		*/
	} 
	
	/**
	 * 
	 * The HDF standard only requires that for a particular tag type, each instance have a
	 * unique ref.
	 * ----------------NO TRUE ANY LONGER-----------------  
	 * Since our files are not expected to contain more than
	 * several dozen objects, 
	 * I take the simplest approach of simply
	 * assigning the index number + 1 from the objectList.
	 * ----------------NO TRUE ANY LONGER-----------------
	 * 
	 * Just adds one to ref Count
	 * 
	 * @return a reference number for the given HDF object
	 * @param refs the map for a given tag type
	 */
	static short getUniqueRef() {
		//Just add 1, set to 1 every time class created 
		return ++refCount;
		/*
		while (refs.containsKey(new Short(rval))){
			rval++;
		}
		return rval;
		*/
	}
	
	/**
	 * Creates a new HDF DataObject, belonging to the specified <code>HDFile</code>.  My approach is to have a 
	 * separate HDFile object for each physical HDF file on disk.  Each <code>HDFile</code> object
	 * handles the bookkeeping of the HDF File Header (see NCSA HDF: Specifications and Developer's Guide v3.2).
	 *
	 * @param	file	The file to contain the new object.
	 * @param	tag	The hdf tag of the new object.
	 */
	DataObject(HDFile file, short tag) {
		this.file = file;
		setTag(tag);
		addDataObject(this, true); //ref gets set in this call
	}

	/* non-javadoc:
	 * Creates a new <code>DataObject</code> with the specified byte array as the data which will (or does already) 
	 * physically
	 * reside in the file.
	 *
	 * @param	f	    The file to contain the new object.
	 * @param	data	    The byte representation of the data.
	 * @param	r   The unique value specifying the type of data object.
	 */
	DataObject(HDFile f, byte[] data, short t, short r) {
		this.file = f;
		setTag(t);
		setRef(r);
		this.bytes = data;
		addDataObject(this, false);
	}

	/* non-javadoc:
	 * Creates a new <code>DataObject</code> pointing to the offset in the file where its data reside.  This
	 * option is for when you don't want to hog memory with a large byte array.
	 *
	 * @param	file	    The file to contain the new object.
	 * @param	offset	    The location in <code>file</code>
	 * @param	reference   The unique value specifying the type of data object.
	 */
	DataObject(HDFile file, int offset, int length, short t, short reference) {
		this.file = file;
		setTag(t);
		setRef(reference);
		this.offset = offset;
		this.length = length;
		addDataObject(this, false);
	}
	
	private final void setTag(short t){
		tag=t;
		tagKey=new Short(t);
	}

	/* non-javadoc:
	 * Returns the byte representation to be written at <code>offset</code> in the file.
	 */
	byte[] getBytes()  {
		return bytes;
	}

	/**
	 * When bytes are read from file, sets the internal fields of the data object.
	 *
	 * @exception   HDFException	    thrown if unrecoverable error occurs
	 */
	protected abstract void interpretBytes() throws HDFException;


	/* non-javadoc:
	 * Returns a 4-byte representation of the tag, usually defined in the HDF standard, of this item.  
	 * The tag is a 2-byte integer
	 * denoting a unique data object type.
	 */
	final short getTag() {
		return tag;
	}
	
	final Short getTagKey(){
		return tagKey;
	}

	/**
	 * Returns a 2-byte representation of the reference number, which is unique for any given
	 * tag type in an HDF file.  In my code, it is unique, period, but the HDF standard does not 
	 * expect or require this.
	 * 
	 * @return reference number
	 */
	public final short getRef() {
		return ref;
	}
	
	final Short getRefKey(){
		return refKey; 
	}
	
	/**
	 * Set to false once the ref number is defined.
	 */
	protected boolean haveNotSetRef=true;
	
	final void setRef(short newref) {
		if ((haveNotSetRef) || (ref!=newref)){
			final short oldref=ref;
			final Short oldrefKey=refKey;

			ref = newref;
			refKey=new Short(newref);
			/* only call "change" if this isn't the first time */
			if (!haveNotSetRef){	
				changeRefKey(this,oldref);
			}
			haveNotSetRef=false;
		}
	}
	
	protected Integer getKey(){
		return new Integer(tagRef2unigueKey(tag, ref));
	}
	/* non-javadoc:
	 * Called back by <code>HDFile</code> to set the offset information.
	 */
	void setOffset(int off) {
		offset = off;
	}

	int getOffset() {
		return offset;
	}
	/**
	 * Returns the length of the byte array in the file for this data element.
	 * 
	 * @return he length of the byte array in the file for this data element
	 */
	protected int getLength() {
		return bytes.length;
	}

	/**
	 * Create a unique key given then tag an ref numbers
	 */
	private int tagRef2unigueKey(short tag, short ref) {
		int key= (((int)tag)<<16)+(int)ref;
		return key;
	}

	static Integer calculateKey(short tag, short ref){
		int key= (((int)tag)<<16)+(int)ref;
		
		return new Integer(key);
	}

	/* non-javadoc:
	 * Gives the handle to the file holding this object.
	 */
	HDFile getFile() {
		return file;
	}

}
