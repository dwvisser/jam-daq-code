package jam.io.hdf;

import java.util.*;
import java.io.*;

/**
 * Class to represent an HDF <em>Numerical Data Group</em> data object.
 *
 * @version  0.5 December 98
 * @author   Dale Visser
 * @since       JDK1.1
 */
public class NumericalDataGroup extends DataObject {

	/**
	 * List of data elements this NDG ties together.
	 */
	List elements;

	public NumericalDataGroup(HDFile fi) {
		super(fi, DFTAG_NDG); //sets tag
		elements = new Vector();
		try {
			refreshBytes();
		} catch (HDFException e) {
			System.err.println(e);
		}
	}

	public NumericalDataGroup(HDFile hdf, byte[] data, short reference) {
		super(hdf, data, reference);
		tag = DFTAG_NDG;
	}

	/**
	 * Should be called whenever a change is made to the contents of the NDG.
	 *
	 * @exception HDFException unrecoverable error
	 */
	protected void refreshBytes() throws HDFException {
		int numBytes;
		ByteArrayOutputStream baos;
		DataOutputStream dos;

		try {
			numBytes = 4 * elements.size();
			/* see DFTAG_NDG specification for HDF 4.1r2 */
			baos = new ByteArrayOutputStream(numBytes);
			dos = new DataOutputStream(baos);
			for (Iterator temp = elements.iterator(); temp.hasNext();) {
				DataObject ob = (DataObject) (temp.next());
				dos.writeShort(ob.getTag());
				dos.writeShort(ob.getRef());
			}
			bytes = baos.toByteArray();
		} catch (IOException e) {
			throw new HDFException("Problem processing NDG: " + e.getMessage());
		}
	}

	/**
	 * Implementation of <code>DataObject</code> abstract method.
	 *
	 * @exception HDFException thrown if there is a problem interpreting the bytes
	 */
	public void interpretBytes() throws HDFException {
		int numItems, i;
		short t, r; //item tag/ref

		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		numItems = bytes.length / 4; //2 for each tag, 2 for each ref
		elements = new Vector(numItems);
		try {
			for (i = 0; i < numItems; i++) {
				t = dis.readShort();
				r = dis.readShort();
				addDataObject(file.getObject(t, r));
				//look up tag/ref in file and add object
			}
		} catch (IOException e) {
			throw new HDFException(
				"Problem interpreting NDG: " + e.getMessage());
		}
	}

	/**
	 * Adds the data element to the NDG.
	 *
	 * @param  data  data element to be added
	 */
	public void addDataObject(DataObject data) {
		elements.add(data);
		try {
			refreshBytes();
		} catch (HDFException e) {
			System.err.println(e);
		}
	}

	/**
	 * Passes the internal vector back.
	 */
	public List getObjects() {
		return elements;
	}
}
