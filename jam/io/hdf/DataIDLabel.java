package jam.io.hdf;

import jam.util.StringUtilities;

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
final class DataIDLabel extends DataObject {

	/**
	 * Object being labelled.
	 */
	private DataObject object;

	private String label;

	DataIDLabel(DataObject obj, String label) {
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

	DataIDLabel(HDFile hdf, byte[] data, short tag, short reference) {
		super(hdf, data, tag, reference);
	}

	/**
	 * Implementation of <code>DataObject</code> abstract method.
	 *
	 * @exception HDFException thrown if there is a problem interpreting the bytes
	 */
	protected void interpretBytes() throws HDFException {
		final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		final DataInputStream dis = new DataInputStream(bais);

		try {
			final short tag = dis.readShort();
			final short ref = dis.readShort();
			final byte [] temp = new byte[bytes.length - 4];
			dis.read(temp);
			label = StringUtilities.instance().getASCIIstring(temp);
			object = getObject(tag, ref);
		} catch (IOException e) {
			throw new HDFException(
				"Problem interpreting DIL.",e);
		}
	}

	/**
	 * @return the text contained.
	 */
	String getLabel() {
		return label;
	}

	/**
	 * 
	 * @return the object referred to
	 */
	private DataObject getObject() {
		return object;
	}

	static DataIDLabel withTagRef(List labels, int tag, int ref) {
		DataIDLabel output=null;
		for (final Iterator temp = labels.iterator(); temp.hasNext();) {
			final DataIDLabel dil = (DataIDLabel) (temp.next());
			if ((dil.getObject().getTag() == tag)
				&& (dil.getObject().getRef() == ref)) {
				output = dil;
			}
		}
		return output;
	}
}
