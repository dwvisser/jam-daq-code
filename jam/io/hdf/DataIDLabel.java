package jam.io.hdf;

import java.nio.ByteBuffer;
import java.util.List;

import static jam.io.hdf.Constants.DFTAG_DIL;

/**
 * Class to represent an HDF <em>Data identifier label</em> data object. The
 * label is meant to be a short probably one or 2 word <em>label</em>.
 * 
 * @version 0.5 December 98
 * @author Dale Visser
 * @since JDK1.1
 */
final class DataIDLabel extends AbstractData {

	protected static <T extends AbstractData> DataIDLabel withTagRef(
			final Class<T> tag, final int ref) {
		return withTagRef(ofType(DataIDLabel.class), tag, ref);
	}

	protected static <T extends AbstractData> DataIDLabel withTagRef(
			final List<DataIDLabel> labels, final Class<T> tag, final int ref) {
		DataIDLabel rval = null;
		for (DataIDLabel dil : labels) {
			final AbstractData data = dil.getObject();
			if (tag.isInstance(data) && (data.getRef() == ref)) {
				rval = dil;
				break;
			}
		}
		return rval;
	}

	/**
	 * Object being labelled.
	 */
	private transient AbstractData object;

	private transient String label;

	DataIDLabel(final AbstractData obj, final String label) {
		super(DFTAG_DIL); // sets tag
		object = obj;
		this.label = label;
		final int byteLength = 4 + label.length();
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
	@Override
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
	protected String getLabel() {
		return label;
	}

	@Override
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