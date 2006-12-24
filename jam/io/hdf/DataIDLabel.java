package jam.io.hdf;

import static jam.io.hdf.Constants.DFTAG_DIL;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Class to represent an HDF <em>Data identifier label</em> data object. The
 * label is meant to be a short probably one or 2 word <em>label</em>.
 * 
 * @version 0.5 December 98
 * @author Dale Visser
 * @since JDK1.1
 */
final class DataIDLabel extends AbstractData {

	static <T extends AbstractData> DataIDLabel withTagRef(final Class<T> tag,
			final int ref) {
		return withTagRef(ofType(DataIDLabel.class), tag, ref);
	}

	static <T extends AbstractData> DataIDLabel withTagRef(
			final List<DataIDLabel> labels, final Class<T> tag, final int ref) {
		DataIDLabel rval = null;
		for (DataIDLabel dil : labels) {
			final AbstractData data = dil.getObject();
			if (tag.isInstance(data) && (data.getRef() == ref)) {
				rval = dil;
				break;
			}
		}
		// if (rval==null) {
		// throw new IllegalStateException("We should always have a result here.
		// In a list of "+
		// labels.size()+" labels, none pointed to tag="+tag.getName()+",
		// ref="+ref+".");
		// }
		return rval;
	}

	/**
	 * Object being labelled.
	 */
	private transient AbstractData object;

	private transient String label;

	DataIDLabel(AbstractData obj, String label) {
		super(DFTAG_DIL); // sets tag
		object = obj;
		this.label = label;
		int byteLength = 4 + label.length();
		bytes = ByteBuffer.allocate(byteLength);
		bytes.putShort(object.getTag());
		bytes.putShort(object.getRef());
		putString(label);

		LOGGER.fine("DataIDLabel for Tag: " + object.getTag() + " Ref: "
				+ object.getRef() + " Label: " + label);
	}

	DataIDLabel() {
		super(DFTAG_DIL);
	}

	/**
	 * Implementation of <code>DataObject</code> abstract method.
	 * 
	 * @exception HDFException
	 *                thrown if there is a problem interpreting the bytes
	 */
	protected void interpretBytes() throws HDFException {
		bytes.position(0);
		final short tagType = bytes.getShort();
		final short reference = bytes.getShort();
		label = getString(bytes.remaining());
		LOGGER.fine("DataIDLabel for Tag: " + tagType + " Ref: " + reference
				+ " Label: " + label);
		object = getObject(TYPES.get(tagType), reference);
	}

	/**
	 * @return the text contained.
	 */
	String getLabel() {
		return label;
	}

	public String toString() {
		final StringBuilder rval = new StringBuilder("(Label \"");
		rval.append(label).append("\": ");
		rval.append((object == null) ? "null" : object.toString());
		rval.append(')');
		return rval.toString();
	}

	/**
	 * 
	 * @return the object referred to
	 */
	private AbstractData getObject() {// NOPMD
		return object;
	}

}