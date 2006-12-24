package jam.io.hdf;

import static jam.io.hdf.Constants.DFTAG_SDL;

/**
 * Class to represent an HDF <em>scientific data label</em> data object. The
 * label is meant to be a short probably one or 2 word <em>label</em>.
 * 
 * @version 0.5 December 98
 * @author <a href="mailto:dale@visser.name">Dale Visser </a>
 * @since JDK1.1
 */
final class ScientificDataLabel extends AbstractData {

	ScientificDataLabel() {
		super(DFTAG_SDL);
	}

	/**
	 * Implementation of <code>DataObject</code> abstract method.
	 */
	public void interpretBytes() {
		int[] lengths;
		bytes.position(0);
		int numLabels = 0;
		int lenCounter = 0;
		lengths = new int[10];
		/*
		 * hard to imagine needing this many dimensions, so should be sufficient
		 */
		for (bytes.rewind(); bytes.remaining() > 0; lenCounter++) {
			final byte next = bytes.get();
			if (next == (byte) 0) {
				lengths[numLabels] = lenCounter;
				numLabels++;
				lenCounter = 0;
			}
		}
		final String[] labels = new String[numLabels];
		bytes.rewind();
		for (int i = 0; i < numLabels; i++) {
			labels[i] = getString(lengths[i]);
			bytes.get(); // skip null character
		}
	}
}