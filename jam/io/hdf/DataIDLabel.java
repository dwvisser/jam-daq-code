package jam.io.hdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
/**
 * Class to represent an HDF <em>Data identifier label</em> data object.  The label is meant to be a short
 * probably one or 2 word <em>label</em>.
 *
 * @version	0.5 December 98
 * @author 	Dale Visser
 * @since       JDK1.1
 */
public class DataIDLabel extends DataObject {

	/**
	 * Object being labelled.
	 */
	DataObject object;

	String label;

	public DataIDLabel(DataObject obj, String label) {
		super(obj.getFile(), DFTAG_DIL); //sets tag
		object = obj;
		this.label = label;
		int byteLength = 4 + label.length();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(byteLength);
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeShort(object.getTag());
			dos.writeShort(object.getRef());
			dos.writeBytes(label);
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(null,ioe.getMessage(),
			getClass().getName(),JOptionPane.ERROR_MESSAGE);
		}
		bytes = baos.toByteArray();
	}

	public DataIDLabel(HDFile hdf, byte[] data, short t, short reference) {
		super(hdf, data, t, reference);
	}

	/**
	 * Implementation of <code>DataObject</code> abstract method.
	 *
	 * @exception HDFException thrown if there is a problem interpreting the bytes
	 */
	public void interpretBytes() throws HDFException {
		final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		final DataInputStream dis = new DataInputStream(bais);

		try {
			final short tag = dis.readShort();
			final short ref = dis.readShort();
			final byte [] temp = new byte[bytes.length - 4];
			dis.read(temp);
			label = new String(temp);
			object = file.getObject(tag, ref);
		} catch (IOException e) {
			throw new HDFException(
				"Problem interpreting DIL: " + e.getMessage());
		}
	}

	/**
	 * Returns the text contained.
	 */
	public String getLabel() {
		return label;
	}

	public DataObject getObject() {
		return object;
	}

	static public DataIDLabel withTagRef(List labels, int tag, int ref) {
		DataIDLabel output=null;
		DataIDLabel dil;
		for (Iterator temp = labels.iterator(); temp.hasNext();) {
			dil = (DataIDLabel) (temp.next());
			if ((dil.getObject().getTag() == tag)
				&& (dil.getObject().getRef() == ref)) {
				output = dil;
			}
		}
		return output;
	}
}
