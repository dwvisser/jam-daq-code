package jam.io.hdf;
import jam.util.StringUtilities;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

/**
 * Class to represent an HDF <em>Data identifier annotation</em> data object.  
 * An annotation is lenghtier than a label, and can hold a descriptive text block.
 
 * @version	0.5 December 98
 * @author 	Dale Visser
 * @since       JDK1.1
 * @see		DataIDLabel
 */
final class DataIDAnnotation extends DataObject {

	/**
	 * Object being annotated.
	 */
	private DataObject object;

	/**
	 * Text of annotation.
	 */
	private String note;

	/**
	 * Annotate an existing <code>DataObject</code> with specified annotation text.
	 *
	 * @param obj   item to be annotated
	 * @param note  text of annotation
	 * @exception  HDFException thrown on unrecoverable error 
	 */
	DataIDAnnotation(DataObject obj, String note) throws HDFException {
		super(DFTAG_DIA); //sets tag
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
			throw new HDFException("Problem creating DIA.",e);
		}
	}
	
	DataIDAnnotation(){
	    super();
	}

//	void init(byte[] data, short tag, short reference) {
//		super.init(data, tag,reference);
//	}

	/**
	 * Implementation of <code>DataObject</code> abstract method.
	 *
	 */
	protected void interpretBytes() {
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        final short tag = buffer.getShort();
        final short ref = buffer.getShort();
        final byte[] temp = new byte[buffer.remaining()];
        buffer.get(temp);
        note = StringUtilities.instance().getASCIIstring(temp);
        object = getObject(tag, ref);
    }

	String getNote() {
		return note;
	}
	
	public String toString(){
	    return note;
	}

	private DataObject getObject() {
		return object;
	}

	/**
	 * 
	 * @param labels list of <code>DataIDAnnotation</code>'s
	 * @param tag to look for
	 * @param ref to look for
	 * @return annotation object that refers to the object witht the given
	 * tag and ref
	 */
	static DataIDAnnotation withTagRef(
		List labels,
		int tag,
		int ref) {
		DataIDAnnotation output=null;
		for (final Iterator temp = labels.iterator(); temp.hasNext();) {
			final DataIDAnnotation dia = (DataIDAnnotation) (temp.next());
			if ((dia.getObject().getTag() == tag)
				&& (dia.getObject().getRef() == ref)) {
				output = dia;
			}
		}
		return output;
	}
}
