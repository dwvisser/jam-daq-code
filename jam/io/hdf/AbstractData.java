package jam.io.hdf;

import jam.util.StringUtilities;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class to represent a generic HDF data object.
 *
 * @version	0.5 November 98
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @since       JDK1.1
 */
public abstract class AbstractData implements Constants {

	/**
	 * List of Data Objects in the file.
	 */
	private static List objectList=Collections.synchronizedList(new ArrayList());
	/** 
	 * Map of Data Objects in file with tag/ref as key 
	 */
	private static Map tagRefMap=Collections.synchronizedMap(new HashMap());	
	/** 
	 * Count for references, to make sure references are unique 
	 *  Reference count, starts at 1
	 */
	static short refCount;	

	
	private static final Set ALL_TYPES = new HashSet(); 
	static {
	    ALL_TYPES.add(new Short(DFTAG_MT));
	    ALL_TYPES.add(new Short(DFTAG_DIA));
	    ALL_TYPES.add(new Short(DFTAG_DIL));
	    ALL_TYPES.add(new Short(DFTAG_FID));
	    ALL_TYPES.add(new Short(DFTAG_FD));
	    ALL_TYPES.add(new Short(DFTAG_NT));
	    ALL_TYPES.add(new Short(DFTAG_VER));
	    ALL_TYPES.add(new Short(DFTAG_NDG));
	    ALL_TYPES.add(new Short(DFTAG_SD));
	    ALL_TYPES.add(new Short(DFTAG_SDD));
	    ALL_TYPES.add(new Short(DFTAG_SDL));
	    ALL_TYPES.add(new Short(DFTAG_SDS));
	    ALL_TYPES.add(new Short(DFTAG_VG));
	    ALL_TYPES.add(new Short(DFTAG_VH));
	    ALL_TYPES.add(new Short(DFTAG_VS));
	}
	
	private static final Map INITABLE = new HashMap();
	static {
	    INITABLE.put(new Short(DFTAG_DIA),DataIDAnnotation.class);
	    INITABLE.put(new Short(DFTAG_DIL),DataIDLabel.class);
	    INITABLE.put(new Short(DFTAG_VER),LibVersion.class);
	    INITABLE.put(new Short(DFTAG_NT),NumberType.class);
	    INITABLE.put(new Short(DFTAG_NDG),NumericalDataGroup.class);
	    INITABLE.put(new Short(DFTAG_SDD),ScientificDataDimension.class);
	    INITABLE.put(new Short(DFTAG_SD),ScientificData.class);
	    INITABLE.put(new Short(DFTAG_SDL),ScientificDataLabel.class);
	    INITABLE.put(new Short(DFTAG_VG),VirtualGroup.class);
	    INITABLE.put(new Short(DFTAG_VH),VDataDescription.class);
	    INITABLE.put(new Short(DFTAG_VS),VData.class);
	}
	//Instance members
	
	/**
	 * contains the 2-byte tag for the data type
	 */
	protected short tag;
		
	/**
	  * Unique reference number, in case several data elements with the same tag exist. Only makes sense in 
	  * the context of an HDF file.
	  */
	protected short ref;
	
	/**
	 * Offset from start of file.
	 */
	protected int offset;
	
	/**
	 * Before bytes is created, length of bytes.
	 */
	protected int length;
	
	/**
	 * Actual bytes stored in HDF file.
	 */
	protected ByteBuffer bytes;
	
	private static final byte[] CLEARBYTES=new byte[0];

	/**
	 * Set to false once the ref number is defined.
	 */
	protected boolean refNotSet=true;
	
	/**
	 * Get the list of all data objects.
	 * 
	 * @return list of all objects
	 */
	static List getDataObjectList() {
		return Collections.unmodifiableList(objectList);
	}
	
	/**
	 * Clear the lists of all data objects.
	 */	
	static void clearAll() {
		//set all object data to none
		for (final Iterator it=objectList.iterator(); it.hasNext();){
			final AbstractData dataObject=(AbstractData)it.next();
			dataObject.bytes=ByteBuffer.wrap(CLEARBYTES);
		}
		objectList.clear();
		tagRefMap.clear();
		refCount =0;		
	}
	
	/**
	 * <p>Adds the data object to the list of objects.
	 * --- FIXME KBS remove --  
	 * The reference number is 
	 * implicitly assigned at this time as the index number in the 
	 * internal <code>Vector objectList</code>.  A typical call should 
	 * look like:</p>
	 * <blockquote><code>hdf = new hdfFile(outfile, "rw");<br>
	 * hdf.addDataObject(new DataObject(this));</code></blockquote>
	 * <p>Each call causes setOffsets to be called.</p>
	 * ---
	 *
	 * @param data data object
	 * @see	#setOffsets()
	 */
	static void addDataObjectToList(AbstractData data) {
		final Integer key =data.getKey();
		if (!tagRefMap.containsKey(key)){
			tagRefMap.put(key, data);
			objectList.add(data);
		}
	}
	/**
	 * @return object in file with the matching tag and ref
	 * @param tag tag of HDF object
	 * @param ref <em>unique</em> reference number in file
	 */
	public static AbstractData getObject(short tag, short ref) {
		AbstractData match = null;
		final Integer key =calculateKey(tag, ref);		
		if (tagRefMap.containsKey(key)){
			match=(AbstractData)tagRefMap.get(key);
		}
		return match;
	}
	
	/**
	 * @return a subset of the given list of <code>DataObject</code>'s of
	 * the specified type 
	 * @param collection the list to search
	 * @param tagType the type to return
	 */
	static List ofType(Collection collection, short tagType) {
		final Set ssin=new HashSet();
		final Iterator iter = collection.iterator();
		while(iter.hasNext()){
			final AbstractData dataObject=(AbstractData)iter.next();
			if (tagType == dataObject.getTag()){
				ssin.add(dataObject);
			}
		}
		return new ArrayList(ssin);
	}

	/**
	 * @return a list of all <code>DataObject</code> of the
	 * given type
	 * @param tagType the type to return 
	 */
	public static List ofType(final short tagType) {
		final List rval=new ArrayList();
		final List objectList = getDataObjectList();
		final Iterator iter = objectList.iterator();
		while(iter.hasNext()){
			final AbstractData dataObject=(AbstractData)iter.next();
			if (tagType == dataObject.getTag()){
				rval.add(dataObject);
			}
		}
		return rval;
	}
	
	static boolean isValidType(short type){
	    return ALL_TYPES.contains(new Short(type));
	}
	
	static final AbstractData create(byte[] bytes, short tag, short ref) throws HDFException {
        AbstractData dataObject = null;
        if (isValidType(tag)) {
            dataObject = createDataObject(tag);
            if (dataObject != null) {//Only create necessary objects
                dataObject.init(bytes, tag, ref);
            }
        } else {
            throw new IllegalArgumentException("Invalid tag: " + tag);
        }
        return dataObject;
    }
	
	static AbstractData create(short tag, short ref, int offset, int length) throws HDFException {
	    AbstractData dataObject = null;
	    if (isValidType(tag)){
	    	dataObject =createDataObject(tag); 
	    	if (dataObject!=null){	//Only create necessary objects
	    		dataObject.init(offset, length, tag, ref);
	    	}
	    } else {
	        throw new IllegalArgumentException("Invalid tag: "+tag);
	    }
	    return dataObject;
	}
	
	private static AbstractData createDataObject(short tag) throws HDFException {
		AbstractData rval = null;
        final Short key = new Short(tag);
        if (INITABLE.containsKey(key)){
            final Class dataClass=(Class)INITABLE.get(key);
            try {
                rval = (AbstractData)dataClass.newInstance();
            } catch (InstantiationException ie) {
                throw new HDFException("Couldn't create "+dataClass.getName()+" instance.", ie);	            	
        	} catch ( IllegalAccessException iae){
                throw new HDFException("Couldn't create "+dataClass.getName()+" instance.", iae);
            } 
        }
		return rval;
	}
	static void interpretBytesAll() throws HDFException {
			final Iterator temp = getDataObjectList().iterator();
			while (temp.hasNext()) {
				final AbstractData dataObject = (AbstractData) (temp.next());
				dataObject.interpretBytes();
			}
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
		//Just add 1, set to 0 every time DataObject.clear is called 
		return ++refCount;
	}

	/* non-javadoc:
	 * Create a unique key given then tag an ref numbers.
	 */
	static Integer calculateKey(short tag, short ref){
	    final int tagInt=tag;
		final int key= (tagInt<<16)+ref;		
		return new Integer(key);
	}
	
	private final StringUtilities UTIL=StringUtilities.instance();
	
	/**
	 * Creates a new HDF DataObject, belonging to the specified <code>HDFile</code>.  My approach is to have a 
	 * separate HDFile object for each physical HDF file on disk.  Each <code>HDFile</code> object
	 * handles the bookkeeping of the HDF File Header (see NCSA HDF: Specifications and Developer's Guide v3.2).
	 *
	 * @param	file	The file to contain the new object.
	 * @param	tag	The hdf tag of the new object.
	 */
	AbstractData(short tag) {
		setTag(tag);
		setRef(getUniqueRef());
		addDataObjectToList(this); //ref gets set in this call
	}
	
	/**
	 * Creates a data object.
	 *
	 */
	protected AbstractData(){
	}
	
	/* non-javadoc:
	 * Creates a new <code>DataObject</code> with the specified byte array as the data which will (or does already) 
	 * physically
	 * reside in the file.
	 *
	 * @param	f	    The file to contain the new object.
	 * @param	data	    The byte representation of the data.
	 * @param	r   The unique value specifying the type of data object.
	 * @throws IllegalArgumentException if the data is null or empty
	 */
	void init(byte[] data, short tag, short ref) {
	    if (data == null || data.length==0){
	        throw new IllegalArgumentException("Can't init DataObject with empty data.");
	    }
		setTag(tag);
		setRef(ref);
		bytes = ByteBuffer.wrap(data);
		addDataObjectToList(this);
	}

	/* non-javadoc:
	 * Creates a new <code>DataObject</code> pointing to the offset in the file where its data reside.  This
	 * option is for when you don't want to hog memory with a large byte array.
	 *
	 * @param	file	    The file to contain the new object.
	 * @param	offset	    The location in <code>file</code>
	 * @param	reference   The unique value specifying the type of data object.
	 */
	void init(int offset, int length, short tag, short reference) {
		this.tag=tag;
		setRef(reference);
		this.offset = offset;
		this.length = length;
		addDataObjectToList(this);
	}
	
	private final void setTag(short newTag){
		tag=newTag;
	}

	/* non-javadoc:
	 * Returns a 4-byte representation of the tag, usually defined in the HDF standard, of this item.  
	 * The tag is a 2-byte integer
	 * denoting a unique data object type.
	 */
	final short getTag() {
		return tag;
	}
	/**
	 * Set the reference which is 2 bytes
	 * 
	 * @param newref
	 */
	final void setRef(short newref) {
		if (refNotSet) {
			ref = newref;
		} else {
			if (ref!=newref){
				//Change key, 
				//remove and add with new key
				final Integer key = calculateKey(tag, ref);
				if (tagRefMap.containsKey(key)) {
					tagRefMap.remove(key);
					//Add
					ref = newref;
					final Integer keyNew = calculateKey(tag, ref);
					tagRefMap.put(keyNew, this);
				 }
			}			
			refNotSet=false;
		}
	}
	
	/**
	 * Refreshes the byte array for each object,
	 * Should be called before find size or writing out.
	 * Override when an update of is needed.
	 */
	void refreshBytes() {
		
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
	
	/* non-javadoc:
	 * Called back by <code>HDFile</code> to set the offset information.
	 */
	void setOffset(int off) {
		offset = off;
	}

	int getOffset() {
		return offset;
	}
	int getLength() {
		return length;
	}
	
	/* non-javadoc:
	 * Returns the byte representation to be written at <code>offset</code> in the file.
	 */
	ByteBuffer getBytes()  {
		return bytes;
	}

	private final Integer getKey(){
		return calculateKey(tag, ref);
	}
	
	/**
	 * Utility method for inserting a String as an ASCII array 
	 * into the data representation.
	 * 
	 * @param string to be converted
	 */
	protected final void putString(String string){
	    bytes.put(UTIL.getASCIIarray(string));
	}
	
	/**
	 * Utility method for getting an ASCII string out of the 
	 * data representation.
	 * 
	 * @param len length of string
	 * @return the string
	 */
	protected final String getString(int len){
	    final byte [] rval=new byte[len];
	    bytes.get(rval);
	    return UTIL.getASCIIstring(rval);
	}
	
	/**
	 * When bytes are read from file, sets the internal fields of the data object.
	 *
	 * @exception   HDFException	    thrown if unrecoverable error occurs
	 */
	protected abstract void interpretBytes() throws HDFException;
}
