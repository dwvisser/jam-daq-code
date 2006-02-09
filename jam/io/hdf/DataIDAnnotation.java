package jam.io.hdf;

import java.nio.ByteBuffer;
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
	static <T extends AbstractData> DataIDAnnotation withTagRef(
			final Class<T> tag, final int ref) {
		return withTagRef(ofType(DataIDAnnotation.class), tag, ref);
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
	static <T extends AbstractData> DataIDAnnotation withTagRef(
			final List<DataIDAnnotation> labels, final Class<T> tag,
			final int ref) {
		DataIDAnnotation rval = null;
		for (DataIDAnnotation dia : labels) {
			final AbstractData data = dia.getObject();
			if (tag.isInstance(data) && (data.getRef() == ref)) {
				rval = dia;
				break;
			}
		}
		return rval;
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
		object = getObject(TYPES.get(tagType), reference);
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
