package jam.io.hdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Class to represent an HDF <em>Numerical Data Group</em> data object.
 *
 * @version  0.5 December 98
 * @author   Dale Visser
 * @since       JDK1.1
 */
final class NumericalDataGroup extends DataObject {

	/**
	 * List of data elements this NDG ties together.
	 */
	private List elements;

	NumericalDataGroup() throws HDFException {
		super(DFTAG_NDG); //sets tag
		elements = Collections.synchronizedList(new ArrayList());
		refreshBytes();
	}

	/**
	 * Should be called whenever a change is made to the contents of the NDG.
	 *
	 * @exception HDFException unrecoverable error
	 */
	protected void refreshBytes() throws HDFException {
		try {
			final int numBytes = 4 * elements.size();
			/* see DFTAG_NDG specification for HDF 4.1r2 */
			final ByteArrayOutputStream baos = new ByteArrayOutputStream(numBytes);
			final DataOutputStream dos = new DataOutputStream(baos);
			for (Iterator temp = elements.iterator(); temp.hasNext();) {
				final DataObject ob = (DataObject) (temp.next());
				dos.writeShort(ob.getTag());
				dos.writeShort(ob.getRef());
			}
			bytes = baos.toByteArray();
			dos.close();
		} catch (IOException e) {
			throw new HDFException("Problem processing NDG.",e);
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
				addDataObject(getObject(t, r));
				//look up tag/ref in file and add object
			}
		} catch (IOException e) {
			throw new HDFException(
				"Problem interpreting NDG.",e);
		}
	}

	/**
	 * Adds the data element to the NDG.
	 *
	 * @param  data  data element to be added
	 */
	void addDataObject(DataObject data) throws HDFException {
		elements.add(data);
		refreshBytes();
	}

	/* non-javadoc:
	 * Passes the internal vector back.
	 */
	List getObjects() {
		return elements;
	}
}
