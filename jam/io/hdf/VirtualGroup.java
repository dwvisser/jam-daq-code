package jam.io.hdf;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Class to represent an HDF <em>Virtual Group</em> data object.
 * 
 * @version 0.5 December 98
 * @author Dale Visser
 */
final class VirtualGroup extends DataObject {

    /**
     * List of data elements this vGroup ties together.
     */
    private final List elements = Collections.synchronizedList(new ArrayList());

    /**
     * All Vgroup objects can have a name stored in them.
     */
    private String name;

    /**
     * All Vgroup objects can have a class name stored in them, which is
     * arbitrary.
     */
    private String type;

    private final static short EXTAG = 0; //purpose?

    private final static short EXREF = 0; //purpose?

    private final static short VERSION = 3; //version of DFTAG_VG info

    private final static short MORE = 0; //unused but must add

    VirtualGroup(String name, String type) {
        super(DFTAG_VG); //sets tag
        this.name = name;
        this.type = type;
    }

    VirtualGroup() {
        super();
    }

    /**
     * Should be called whenever a change is made to the contents of the vGroup.
     */
    protected void refreshBytes() {
        final int numBytes = 14 + 4 * elements.size() + name.length()
                + type.length();
        //see DFTAG_VG specification for HDF 4.1r2
        bytes = ByteBuffer.allocate(numBytes);
        for (final Iterator temp = elements.iterator(); temp.hasNext();) {
            final DataObject dataObject = (DataObject) (temp.next());
            bytes.putShort(dataObject.getTag());
        }
        for (final Iterator temp = elements.iterator(); temp.hasNext();) {
            final DataObject dataObject = (DataObject) (temp.next());
            bytes.putShort(dataObject.getRef());
        }
        bytes.putShort((short) name.length());
        putString(name);
        bytes.putShort((short) type.length());
        putString(type);
        bytes.putShort(EXTAG);
        bytes.putShort(EXREF);
        bytes.putShort(VERSION);
        bytes.putShort(MORE);
    }

    /**
     * Interprets bytes in internal byte array.
     *  
     */
    protected void interpretBytes() {
        bytes.rewind();
        final short numItems = bytes.getShort();
        elements.clear();
        final short[] tags = new short[numItems];
        final short[] refs = new short[numItems];
        for (int i = 0; i < numItems; i++) {
            tags[i] = bytes.getShort();
        }
        for (int i = 0; i < numItems; i++) {
            refs[i] = bytes.getShort();
        }
        final short nameLen = bytes.getShort();
        name = getString(nameLen);
        final short typeLen = bytes.getShort();
        type = getString(typeLen);
        for (int i = 0; i < numItems; i++) {
            addDataObject(getObject(tags[i], refs[i]));
        }
        /* rest of element has no useful information */
    }

    /**
     * Adds the data element to the vGroup.
     * 
     * @param data
     *            data element to be added
     * @throws IllegalArgumentException
     *             if <code>data==null</code>
     */
    void addDataObject(DataObject data) {
        if (data == null) {
            throw new IllegalArgumentException("Can't add null to vGroup.");
        }
        elements.add(data);
    }

    /**
     * Gets the name of this group.
     * 
     * @return the name of this group
     */
    String getName() {
        return name;
    }

    List getObjects() {
        return elements;
    }

    /**
     * Returns a List of <code>VirtualGroup</code>'s of the type specified by
     * <code>groupType</code>.
     * 
     * @param list
     *            should contain only VirtualGroup objects
     * @param groupType
     *            type string showing what kind of info is contained
     * @return list of groups with the given type
     */
    static List ofType(List list, String groupType) {
        final List output = new ArrayList();
        for (final Iterator temp = list.iterator(); temp.hasNext();) {
            final VirtualGroup group = (VirtualGroup) (temp.next());
            if (group.getType() == groupType) {
                output.add(group);
            }
        }
        return output;
    }

    /**
     * Returns a VirtualGroup of <code>VirtualGroup</code>'s with the name
     * specified. Should only be called when the name is expected to be unique.
     * 
     * @param list
     *            should contain only VirtualGroup objects
     * @param groupName
     *            name of the desired group
     * @return the group with the given name
     */
    static VirtualGroup ofName(List list, String groupName) {
        VirtualGroup output = null;
        for (final Iterator temp = list.iterator(); temp.hasNext();) {
            final VirtualGroup group = (VirtualGroup) (temp.next());
            if (group.getName().equals(groupName)) {
                output = group;
            }
        }
        return output;
    }

    /**
     * Returns string giving group type.
     * 
     * @return the type of this group
     */
    String getType() {
        return type;
    }

    public String toString() {
        return type + ":" + name;
    }
}