package jam.io.hdf;

import injection.GuiceInjector;
import jam.util.StringUtilities;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Logger;

/**
 * Abstract class to represent a generic HDF data object.
 * @version 0.5 November 98
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @since JDK1.1
 */
public abstract class AbstractData {

    /**
     * For logging debugging messages only.
     */
    protected static final Logger LOGGER = Logger.getLogger(AbstractData.class
            .getPackage().getName());

    private static final byte[] CLEARBYTES = new byte[0];

    /**
     * List of Data Objects in the file.
     */
    private static List<AbstractData> objectList = Collections
            .synchronizedList(new ArrayList<AbstractData>());

    /**
     * Count for references, to make sure references are unique Reference count,
     * starts at 1
     */
    private static short refCount = 1;

    /**
     * Map of Data Objects in file with tag/ref as key
     */
    private static Map<Integer, AbstractData> tagRefMap = Collections
            .synchronizedMap(new HashMap<Integer, AbstractData>());

    /**
     * inverse of TYPES map
     */
    protected static final Map<Class<? extends AbstractData>, Short> TAGS = new HashMap<>();

    /**
     * Map of 2 byte tags to data type objects.
     */
    protected static final Map<Short, Class<? extends AbstractData>> TYPES = new HashMap<>();

    protected static final StringUtilities STRING_UTIL = GuiceInjector
            .getObjectInstance(StringUtilities.class);

    // Instance members

    static {
        TYPES.put(Constants.DFTAG_SDS, ScientificDataScales.class);
        TYPES.put(Constants.DFTAG_FD, FileDescription.class);
        TYPES.put(Constants.DFTAG_FID, FileIdentifier.class);
        TYPES.put(Constants.DFTAG_MT, JavaMachineType.class);
        TYPES.put(Constants.DFTAG_DIA, DataIDAnnotation.class);
        TYPES.put(Constants.DFTAG_DIL, DataIDLabel.class);
        TYPES.put(Constants.DFTAG_VER, LibVersion.class);
        TYPES.put(Constants.DFTAG_NT, NumberType.class);
        TYPES.put(Constants.DFTAG_NDG, NumericalDataGroup.class);
        TYPES.put(Constants.DFTAG_SDD, ScientificDataDimension.class);
        TYPES.put(Constants.DFTAG_SD, ScientificData.class);
        TYPES.put(Constants.DFTAG_SDL, ScientificDataLabel.class);
        TYPES.put(Constants.DFTAG_VG, VirtualGroup.class);
        TYPES.put(Constants.DFTAG_VH, VDataDescription.class);
        TYPES.put(Constants.DFTAG_VS, VData.class);
        for (Short tag : TYPES.keySet()) {
            TAGS.put(TYPES.get(tag), tag);
        }
    }

    /**
     * <p>
     * Adds the data object to the list of objects. The reference number is
     * expected to have been previously assigned, but is checked to be unique
     * for the object tag.
     * @param data
     *            data object
     */
    protected static void addDataObjectToList(final AbstractData data) {
        final Integer key = calculateKey(data.getClass(), data.getRef());
        if (tagRefMap.containsKey(key)) {
            throw new IllegalArgumentException(
                    "Can't add to list of DataObjects as DataOjbect with the same tag and ref exists.");
        }
        tagRefMap.put(key, data);
        objectList.add(data);
    }

    /*
     * non-javadoc: Create a unique key given then tag an ref numbers.
     */
    private static <T extends AbstractData> int calculateKey(
            final Class<T> tag, final short ref) {
        final int tagInt = TAGS.get(tag);
        final int refInt = ref;
        final int key = (tagInt << 16) + refInt;
        LOGGER.finer(" Key tag " + tagInt + " ref " + refInt + " key " + key);
        return key;
    }

    /**
     * Clear the lists of all data objects.
     */
    protected static void clearAll() {
        // set all object data to none
        for (AbstractData dataObject : objectList) {
            dataObject.bytes = ByteBuffer.wrap(CLEARBYTES);
        }
        objectList.clear();
        tagRefMap.clear();
        refCount = 1;
    }

    protected static final <T extends AbstractData> T create(
            final byte[] bytes, final Class<T> tag, final short ref)
            throws HDFException {
        T dataObject = createDataObject(tag);
        if (dataObject != null) {// Only create necessary objects
            dataObject.init(bytes, ref);
        }
        return dataObject;
    }

    protected static <T extends AbstractData> T create(final Class<T> tag,
            final short ref, final int offset, final int length)
            throws HDFException {
        T dataObject = createDataObject(tag);
        if (dataObject != null) { // Only create necessary objects
            dataObject.init(offset, length, ref);
        }
        return dataObject;
    }

    private static <T extends AbstractData> T createDataObject(
            final Class<T> tag) throws HDFException {
        T rval = null;
        if (TYPES.containsValue(tag)) {
            try {
                rval = tag.newInstance();
            } catch (InstantiationException | IllegalAccessException ie) {
                throw new HDFException("Couldn't create " + tag.getName()
                        + " instance.", ie);
            }
        }
        return rval;
    }

    /*
     * non-javadoc: Create a new ref that is unque for the given tag The HDF
     * standard requires that for a particular tag type, each instance have a
     * unique ref.
     * 
     * @return a reference number for the given HDF object @param refs the map
     * for a given tag type
     */
    protected static <T extends AbstractData> short createUniqueRef(
            final Class<T> tag) {
        if (tag == null) {
            throw new IllegalArgumentException("null tag not acceptable.");
        }
        // Add 1 as its a good guess
        refCount++;
        // see if a unique object already exists
        Integer key = calculateKey(tag, refCount);
        while (tagRefMap.containsKey(key)) {
            refCount++;
            key = calculateKey(tag, refCount);
        }
        return refCount;
    }

    /**
     * Get the list of all data objects.
     * @return list of all objects
     */
    protected static List<AbstractData> getDataObjectList() {
        return Collections.unmodifiableList(objectList);
    }

    /**
     * @return object in file with the matching tag and ref
     * @param tag
     *            tag of HDF object
     * @param ref
     *            <em>unique</em> reference number in file
     * @param <T>
     *            type of data object to return
     */
    public static <T extends AbstractData> T getObject(final Class<T> tag,
            final short ref) {
        T match = null;
        final Integer key = calculateKey(tag, ref);
        if (tagRefMap.containsKey(key)) {
            match = tag.cast(tagRefMap.get(key));
        }
        return match;
    }

    protected static void interpretBytesAll() throws HDFException {
        for (AbstractData dataObject : getDataObjectList()) {
            dataObject.interpretBytes();
        }
    }

    protected static boolean isValidType(final short type) {
        return TYPES.containsKey(type);
    }

    /**
     * @return a list of all <code>DataObject</code> of the given type
     * @param tagType
     *            the type to return
     * @param <T>
     *            type of list to return
     */
    public static <T extends AbstractData> List<T> ofType(
            final Class<T> tagType) {
        return ofType(getDataObjectList(), tagType);
    }

    /**
     * @return a subset of the given list of <code>DataObject</code>'s of the
     *         specified type
     * @param collection
     *            the list to search
     * @param type
     *            the type to return
     */
    protected static <T extends AbstractData> List<T> ofType(
            final Collection<AbstractData> collection, final Class<T> type) {
        final List<T> result = new ArrayList<>();
        for (AbstractData data : collection) {
            if (type.isInstance(data)) {
                result.add(type.cast(data));
            }
        }
        return result;
    }

    /**
     * Actual bytes stored in HDF file.
     */
    protected transient ByteBuffer bytes;

    /**
     * Before bytes is created, length of bytes.
     */
    protected transient int length;

    /**
     * Offset from start of file.
     */
    protected int offset;

    /**
     * Unique reference number, in case several data elements with the same tag
     * exist. Only makes sense in the context of an HDF file.
     */
    protected short ref;

    /**
     * contains the 2-byte tag for the data type
     */
    protected short tag;

    /**
     * Creates a new HDF DataObject, belonging to the specified
     * <code>HDFile</code>. My approach is to have a separate HDFile object for
     * each physical HDF file on disk. Each <code>HDFile</code> object handles
     * the bookkeeping of the HDF File Header (see NCSA HDF: Specifications and
     * Developer's Guide v3.2).
     * @param tag
     *            The hdf tag of the new object.
     */
    AbstractData(final short tag) {
        super();
        this.tag = tag;
        this.ref = createUniqueRef(TYPES.get(tag));
        addDataObjectToList(this); // ref gets set in this call
    }

    /*
     * non-javadoc: Returns the byte representation to be written at
     * <code>offset</code> in the file.
     */
    protected ByteBuffer getBytes() {
        return bytes;
    }

    protected int getLength() {
        return length;
    }

    protected int getOffset() {
        return offset;
    }

    /**
     * Returns a 2-byte representation of the reference number, which is unique
     * for any given tag type in an HDF file. In my code, it is unique, period,
     * but the HDF standard does not expect or require this.
     * @return reference number
     */
    public final short getRef() {
        return ref;
    }

    /**
     * Utility method for getting an ASCII string out of the data
     * representation.
     * @param len
     *            length of string
     * @return the string
     */
    protected final String getString(final int len) {
        final byte[] rval = new byte[len];
        bytes.get(rval);
        return STRING_UTIL.getASCIIstring(rval);
    }

    /*
     * non-javadoc: Returns a 4-byte representation of the tag, usually defined
     * in the HDF standard, of this item. The tag is a 2-byte integer denoting a
     * unique data object type.
     */
    protected final short getTag() {
        return tag;
    }

    /*
     * non-javadoc: Creates a new <code>DataObject</code> with the specified
     * byte array as the data which will (or does already) physically reside in
     * the file.
     * 
     * @param f The file to contain the new object. @param data The byte
     * representation of the data. @param r The unique value specifying the type
     * of data object. @throws IllegalArgumentException if the data is null or
     * empty
     */
    protected void init(final byte[] data, final short reference) {
        if (data == null) {
            throw new IllegalArgumentException(
                    "Null data reference. Need data for initializiation.");
        }
        if (data.length == 0) {
            throw new IllegalArgumentException(
                    "Data array empty. Need data for initialization.");
        }
        setTag(TAGS.get(getClass()));
        setRef(reference);
        bytes = ByteBuffer.wrap(data);
    }

    /*
     * non-javadoc: Creates a new <code>DataObject</code> pointing to the offset
     * in the file where its data reside. This option is for when you don't want
     * to hog memory with a large byte array.
     * 
     * @param file The file to contain the new object. @param offset The
     * location in <code>file</code> @param reference The unique value
     * specifying the type of data object.
     */
    protected void init(final int byteOffset, final int len,
            final short reference) {
        tag = TAGS.get(getClass());
        setRef(reference);
        offset = byteOffset;
        length = len;
    }

    /**
     * When bytes are read from file, sets the internal fields of the data
     * object.
     * @exception HDFException
     *                thrown if unrecoverable error occurs
     */
    protected abstract void interpretBytes() throws HDFException;

    /**
     * Utility method for inserting a String as an ASCII array into the data
     * representation.
     * @param string
     *            to be converted
     */
    protected final void putString(final String string) {
        bytes.put(STRING_UTIL.getASCIIarray(string));
    }

    /**
     * Refreshes the byte array for each object, Should be called before find
     * size or writing out. Override when an update of is needed.
     */
    protected void refreshBytes() {
        // default is do nothing
    }

    /*
     * non-javadoc: Called back by <code>HDFile</code> to set the offset
     * information.
     */
    protected void setOffset(final int off) {
        offset = off;
    }

    /**
     * Set the reference.
     * @param newref 2 bytes
     */
    protected final void setRef(final short newref) {
        // Remove object with old ref
        final Integer key = calculateKey(getClass(), ref);
        if (tagRefMap.containsKey(key)) {
            tagRefMap.remove(key);

            // Add
            final Integer keyNew = calculateKey(getClass(), newref);
            // Check for collision
            if (tagRefMap.containsKey(key)) {
                throw new IllegalArgumentException(
                        "Can't set reference on DataObject as one the reference already exists.");
            }
            ref = newref;
            tagRefMap.put(keyNew, this);

        } else {
            throw new IllegalArgumentException(
                    "DataObject not in object table, so cannot change reference");
        }
    }

    private void setTag(final short newTag) {
        tag = newTag;
    }

    @Override
    public String toString() {
        return "(" + getClass().getName() + ": ref=" + getRef() + ")";
    }
}
