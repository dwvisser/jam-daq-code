package jam.io.hdf;

import java.util.*;
import java.io.*;

/**
 * Class to represent an HDF <em>Virtual Group</em> data object.
 *
 * @version	0.5 December 98
 * @author 	Dale Visser
 */
public final class VirtualGroup extends DataObject {

	/**
	 * List of data elements this vGroup ties together.
	 */
	List elements;

	/**
	 * All Vgroup objects can have a name stored in them.
	 */
	String name;

	/**
	 * All Vgroup objects can have a class name stored in them, which is arbitrary.
	 */
	String type;

	short extag = 0; //purpose?
	short exref = 0; //purpose?
	short version = 3; //version of DFTAG_VG info
	short more = 0; //unused but must add

	public VirtualGroup(HDFile fi, String name, String type) {
		super(fi, DFTAG_VG); //sets tag
		this.name = name;
		this.type = type;
		elements = new Vector();
		try {
			refreshBytes();
		} catch (HDFException e) {
			System.err.println(e);
		}
	}

	public VirtualGroup(HDFile hdf, byte[] data, short reference) {
		super(hdf, data, reference);
		tag = DFTAG_VG;
	}

	/**
	 * Should be called whenever a change is made to the contents of the vGroup.
	 * @exception HDFException thrown on unrecoverable error
	 */
	protected void refreshBytes() throws HDFException {
		int numBytes;
		ByteArrayOutputStream baos;
		DataOutputStream dos;
		DataObject ob;
		try {
			numBytes = 14 + 4 * elements.size() + name.length() + type.length();
			//see DFTAG_VG specification for HDF 4.1r2
			baos = new ByteArrayOutputStream(numBytes);
			dos = new DataOutputStream(baos);
			dos.writeShort(elements.size());
			for (Iterator temp = elements.iterator(); temp.hasNext();) {
				ob = (DataObject) (temp.next());
				dos.writeShort(ob.getTag());
			}
			for (Iterator temp = elements.iterator(); temp.hasNext();) {
				ob = (DataObject) (temp.next());
				dos.writeShort(ob.getRef());
			}
			dos.writeShort(name.length());
			dos.writeBytes(name);
			dos.writeShort(type.length());
			dos.writeBytes(type);
			dos.writeShort(extag);
			dos.writeShort(exref);
			dos.writeShort(version);
			dos.writeShort(more);
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
	public void interpretBytes() throws HDFException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		short numItems;
		int i;
		short[] tags;
		short[] refs;
		short nameLen;
		short typeLen;
		byte[] temp;

		try {
			numItems = dis.readShort();
			elements = new Vector(numItems);
			tags = new short[numItems];
			refs = new short[numItems];
			for (i = 0; i < numItems; i++) {
				tags[i] = dis.readShort();
			}
			for (i = 0; i < numItems; i++) {
				refs[i] = dis.readShort();
			}
			nameLen = dis.readShort();
			temp = new byte[nameLen];
			dis.read(temp);
			name = new String(temp);
			typeLen = dis.readShort();
			temp = new byte[typeLen];
			dis.read(temp);
			type = new String(temp);
			for (i = 0; i < numItems; i++) {
				//System.out.println("VG_"+tag+"/"+ref+".interpretBytes(): Read in tag: "+tags[i]+", ref: "+refs[i]);
				addDataObject(file.getObject(tags[i], refs[i]));
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
	 * @exception   HDFException	    thrown if unrecoverable error occurs
	 */
	public void addDataObject(DataObject data) throws HDFException {
		if (data == null)
			throw new HDFException("Can't add null to vGroup.");
		elements.add(data);
		refreshBytes();
	}

	public String getName() {
		return name;
	}

	public List getObjects() {
		return elements;
	}

	/**
	 * Returns a List of <code>VirtualGroup</code>'s of the 
	 * type specified by <code>groupType</code>.
	 *
	 * @param in should contain only VirtualGroup objects
	 * @param groupType	type string showing what kind of info is contained
	 */
	static public List ofType(List in, String groupType) {
		List output = new Vector();
		for (Iterator temp = in.iterator(); temp.hasNext();) {
			VirtualGroup vg = (VirtualGroup) (temp.next());
			if (vg.getType() == groupType) {
				output.add(vg);
			}
		}
		return output;
	}

	/**
	 * Returns a VirtualGroup of <code>VirtualGroup</code>'s with the 
	 * name specified.  Should only be called when the name is expected to be
	 * unique.
	 *
	 * @param in should contain only VirtualGroup objects
	 * @param groupType	type string showing what kind of info is contained
	 */
	static public VirtualGroup ofName(List in, String groupName) {
		VirtualGroup output = null;
		for (Iterator temp = in.iterator(); temp.hasNext();) {
			VirtualGroup vg = (VirtualGroup) (temp.next());
			if (vg.getName().equals(groupName)) {
				output = vg;
			}
		}
		return output;
	}

	/**
	 * Returns string giving group type.
	 */
	public String getType() {
		return type;
	}

}
