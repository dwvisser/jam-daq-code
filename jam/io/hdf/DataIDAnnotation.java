package jam.io.hdf;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

/**
 * Class to represent an HDF <em>Data identifier annotation</em> data object.
 * An annotation is lenghtier than a label, and can hold a descriptive text
 * block.
 * 
 * @version 0.5 December 98
 * @author Dale Visser
 * @since JDK1.1
 * @see DataIDLabel
 */
final class DataIDAnnotation extends AbstractData {

	/**
	 * 
	 * @param tag
	 *            to look for
	 * @param ref
	 *            to look for
	 * @return annotation object that refers to the object witht the given tag
	 *         and ref
	 */
	static DataIDAnnotation withTagRef(final int tag, final int ref) {
		DataIDAnnotation dia = null;
		final List objectList = getDataObjectList();
		final Iterator iter = objectList.iterator();
		while (iter.hasNext()) {
			final AbstractData dataObject = (AbstractData) iter.next();
			if (dataObject.getTag() == Constants.DFTAG_DIA) {
				dia = (DataIDAnnotation) dataObject;
				if ((dia.getObject().getTag() == tag)
						&& (dia.getObject().getRef() == ref)) {
					break;
				}
			}
		}
		return dia;
	}

	/**
	 * 
	 * @param labels
	 *            list of <code>DataIDAnnotation</code>'s
	 * @param tag
	 *            to look for
	 * @param ref
	 *            to look for
	 * @return annotation object that refers to the object witht the given tag
	 *         and ref
	 */
	static DataIDAnnotation withTagRef(final List<AbstractData> labels, final int tag, final int ref) {
		DataIDAnnotation output = null;
		for (AbstractData data : labels) {
			final DataIDAnnotation dia = (DataIDAnnotation) (data);
			if ((dia.getObject().getTag() == tag)
					&& (dia.getObject().getRef() == ref)) {
				output = dia;
				break;
			}
		}
		return output;
	}

	/**
	 * Object being annotated.
	 */
	private transient AbstractData object;

	/**
	 * Text of annotation.
	 */
	private transient String note;

	/**
	 * Annotate an existing <code>DataObject</code> with specified annotation
	 * text.
	 * 
	 * @param obj
	 *            item to be annotated
	 * @param note
	 *            text of annotation
	 */
	DataIDAnnotation(AbstractData obj, String note) {
		super(DFTAG_DIA); // sets tag
		this.object = obj;
		this.note = note;
		int byteLength = 4 + note.length();
		bytes = ByteBuffer.allocate(byteLength);
		bytes.putShort(object.getTag());
		bytes.putShort(object.getRef());
		putString(note);
	}

	DataIDAnnotation() {
		super(DFTAG_DIA);
	}

	/**
	 * Implementation of <code>DataObject</code> abstract method.
	 * 
	 */
	protected void interpretBytes() {
		bytes.position(0);
		final short tagType = bytes.getShort();
		final short reference = bytes.getShort();
		note = getString(bytes.remaining());
		object = getObject(tagType, reference);
	}

	String getNote() {
		return note;
	}

	public String toString() {
		return note;
	}

	private AbstractData getObject() {
		return object;
	}

}
