package jam.io.hdf;

import static jam.io.hdf.Constants.DFTAG_NDG;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class to represent an HDF <em>Numerical Data Group</em> data object.
 * 
 * @version 0.5 December 98
 * @author Dale Visser
 * @since JDK1.1
 */
final class NumericalDataGroup extends AbstractData {

	/**
	 * List of data elements this NDG ties together.
	 */
	private transient List<AbstractData> elements;

	NumericalDataGroup() {
		super(DFTAG_NDG); // sets tag
		elements = Collections.synchronizedList(new ArrayList<AbstractData>());
	}

	/**
	 * Should be called whenever a change is made to the contents of the NDG.
	 */
	@Override
	protected void refreshBytes() {
		final int numBytes = 4 * elements.size();
		/* see DFTAG_NDG specification for HDF 4.1r2 */
		bytes = ByteBuffer.allocate(numBytes);
		for (AbstractData dataObject : elements) {
			bytes.putShort(dataObject.getTag());
			bytes.putShort(dataObject.getRef());
		}
	}

	/**
	 * Implementation of <code>DataObject</code> abstract method.
	 */
	@Override
	public void interpretBytes() {
		bytes.rewind();
		/* 2 for each tag, 2 for each ref */
		final int numItems = bytes.capacity() / 4;
		elements = new ArrayList<AbstractData>(numItems);
		for (int i = 0; i < numItems; i++) {
			final short tagType = bytes.getShort();
			final short reference = bytes.getShort();
			/* look up tag/ref in file and add object */
			addDataObject(getObject(TYPES.get(tagType), reference));
		}
	}

	/**
	 * Adds the data element to the NDG.
	 * 
	 * @param data
	 *            data element to be added
	 */
	protected void addDataObject(final AbstractData data) {
		elements.add(data);
	}

	/*
	 * non-javadoc: Passes the internal vector back.
	 */
	protected List<AbstractData> getObjects() {
		return Collections.unmodifiableList(elements);
	}

	@Override
	public String toString() {
		final StringBuilder rval = new StringBuilder();
		rval.append("NDG[");
		final String separator = ", ";
		for (Object data : elements) {
			rval.append(data.toString()).append(separator);
		}
		final int len = rval.length();
		rval.replace(len - 2, len, "]");
		return rval.toString();
	}
}