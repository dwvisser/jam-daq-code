package jam.io.hdf;
import java.util.List;
import java.util.Iterator;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Class to represent an HDF <em>Data identifier annotation</em> data object.  
 * An annotation is lenghtier than a label, and can hold a descriptive text block.
 
 * @version	0.5 December 98
 * @author 	Dale Visser
 * @since       JDK1.1
 * @see		DataIDLabel
 */
public class DataIDAnnotation extends DataObject {

	/**
	 * Object being annotated.
	 */
	DataObject object;

	/**
	 * Text of annotation.
	 */
	String note;

	/**
	 * Annotate an existing <code>DataObject</code> with specified annotation text.
	 *
	 * @param obj   item to be annotated
	 * @param note  text of annotation
	 * @exception  thrown on unrecoverable error 
	 */
	public DataIDAnnotation(DataObject obj, String note) throws HDFException {
		super(obj.getFile(), DFTAG_DIA); //sets tag
		try {
			this.object = obj;
			this.note = note;
			int byteLength = 4 + note.length();
			ByteArrayOutputStream baos = new ByteArrayOutputStream(byteLength);
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeShort(object.getTag());
			dos.writeShort(object.getRef());
			dos.writeBytes(note);
			bytes = baos.toByteArray();
		} catch (IOException e) {
			throw new HDFException("Problem creating DIA: " + e.getMessage());
		}
	}

	public DataIDAnnotation(HDFile hdf, byte[] data, short t, short reference) {
		super(hdf, data, t,reference);
	}

	/**
	 * Implementation of <code>DataObject</code> abstract method.
	 *
	 * @exception HDFException thrown if there is a problem interpreting the bytes
	 */
	public void interpretBytes() throws HDFException {
		short tag;
		short ref;
		byte[] temp;
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);

		try {
			tag = dis.readShort();
			ref = dis.readShort();
			temp = new byte[bytes.length - 4];
			dis.read(temp);
			note = new String(temp);
			object = file.getObject(tag, ref);
		} catch (IOException e) {
			throw new HDFException(
				"Problem interpreting DIA: " + e.getMessage());
		}
	}

	public String getNote() {
		return note;
	}

	public DataObject getObject() {
		return object;
	}

	static public DataIDAnnotation withTagRef(
		List labels,
		int tag,
		int ref) {
		DataIDAnnotation output=null;
		for (Iterator temp = labels.iterator(); temp.hasNext();) {
			DataIDAnnotation dia = (DataIDAnnotation) (temp.next());
			if ((dia.getObject().getTag() == tag)
				&& (dia.getObject().getRef() == ref)) {
				output = dia;
			}
		}
		return output;
	}
}
