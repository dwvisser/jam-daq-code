package jam.io.hdf;

import jam.util.StringUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

/**
 * Class to represent an HDF <em>Virtual Group</em> data object.
 *
 * @version	0.5 December 98
 * @author 	Dale Visser
 */
final class VirtualGroup extends DataObject {

	/**
	 * List of data elements this vGroup ties together.
	 */
	private final List elements=Collections.synchronizedList(new ArrayList());

	/**
	 * All Vgroup objects can have a name stored in them.
	 */
	private String name;

	/**
	 * All Vgroup objects can have a class name stored in them, which is arbitrary.
	 */
	private String type;

	private final static short EXTAG = 0; //purpose?
	private final static short EXREF = 0; //purpose?
	private final static short VERSION = 3; //version of DFTAG_VG info
	private final static short MORE = 0; //unused but must add

	VirtualGroup(String name, String type) throws HDFException {
		super(DFTAG_VG); //sets tag
		this.name = name;
		this.type = type;
		refreshBytes();
	}

	VirtualGroup(byte[] data, short tag, short ref) {
		super(data, tag, ref);
	}

	/**
	 * Should be called whenever a change is made to the contents of the vGroup.
	 * @exception HDFException thrown on unrecoverable error
	 */
	protected void refreshBytes() throws HDFException {
		int numBytes;
		ByteArrayOutputStream baos;
		DataOutputStream dos;
		DataObject dataObject;
		try {
			numBytes = 14 + 4 * elements.size() + name.length() + type.length();
			//see DFTAG_VG specification for HDF 4.1r2
			baos = new ByteArrayOutputStream(numBytes);
			dos = new DataOutputStream(baos);
			dos.writeShort(elements.size());
			for (final Iterator temp = elements.iterator(); temp.hasNext();) {
				dataObject = (DataObject) (temp.next());
				dos.writeShort(dataObject.getTag());
			}
			for (final Iterator temp = elements.iterator(); temp.hasNext();) {
				dataObject = (DataObject) (temp.next());
				dos.writeShort(dataObject.getRef());
			}
			dos.writeShort(name.length());
			dos.writeBytes(name);
			dos.writeShort(type.length());
			dos.writeBytes(type);
			dos.writeShort(EXTAG);
			dos.writeShort(EXREF);
			dos.writeShort(VERSION);
			dos.writeShort(MORE);
			bytes = baos.toByteArray();
		} catch (IOException e) {
			throw new HDFException("Problem processing VG: " + e.getMessage());
		}
	}

	/**
	 * Interprets bytes in internal byte array.
	 *
	 * @exception   HDFException thrown if unrecoverable error occurs
	 */
	protected void interpretBytes() throws HDFException {
		final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		final DataInputStream dis = new DataInputStream(bais);
		short numItems;
		short[] tags;
		short[] refs;
		short nameLen;
		short typeLen;
		byte[] temp;

		try {
			numItems = dis.readShort();
			elements.clear();
			tags = new short[numItems];
			refs = new short[numItems];
			for (int i = 0; i < numItems; i++) {
				tags[i] = dis.readShort();
			}
			for (int i = 0; i < numItems; i++) {
				refs[i] = dis.readShort();
			}
			nameLen = dis.readShort();
			temp = new byte[nameLen];
			dis.read(temp);
            final StringUtilities util = StringUtilities.instance();
			name = util.getASCIIstring(temp);
			typeLen = dis.readShort();
			temp = new byte[typeLen];
			dis.read(temp);
			type = util.getASCIIstring(temp);
			for (int i = 0; i < numItems; i++) {
				addDataObject(getObject(tags[i], refs[i]));
			}
			//rest of element has no useful information
		} catch (IOException e) {
			throw new HDFException(
				"Problem interpreting VG: " + e.getMessage());
		}
	}

	/**
	 * Adds the data element to the vGroup.
	 *
	 * @param	data	data element to be added
	 * @throws   IllegalArgumentException if <code>data==null</code>
	 * @throws HDFException if the data is unreadable somehow
	 */
	void addDataObject(DataObject data) throws HDFException {
		if (data == null){
			throw new IllegalArgumentException("Can't add null to vGroup.");
		}
		elements.add(data);
		refreshBytes();
	}

	/**
	 * Gets the name of this group.
	 * @return the name of this group
	 */
	String getName() {
		return name;
	}

	List getObjects() {
		return elements;
	}

	/**
	 * Returns a List of <code>VirtualGroup</code>'s of the 
	 * type specified by <code>groupType</code>.
	 *
	 * @param list should contain only VirtualGroup objects
	 * @param groupType	type string showing what kind of info is contained
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
	 * Returns a VirtualGroup of <code>VirtualGroup</code>'s with the 
	 * name specified.  Should only be called when the name is expected to be
	 * unique.
	 *
	 * @param list should contain only VirtualGroup objects
	 * @param groupName name of the desired group
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

	public String toString(){
	    return type+":"+name;
	}
}
