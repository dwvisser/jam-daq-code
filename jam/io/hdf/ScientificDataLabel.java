package jam.io.hdf;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Class to represent an HDF <em>scientific data label</em> data object.  The label is meant to be a short
 * probably one or 2 word <em>label</em>.
 *
 * @version	0.5 December 98
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @since       JDK1.1
 */
final class ScientificDataLabel extends DataObject {

	private String[] labels;

	ScientificDataLabel(HDFile hdf, byte[] data, short t, short reference) {
		super(hdf, data, t, reference);
	}

	/**
	 * Implementation of <code>DataObject</code> abstract method.
	 *
	 * @exception HDFException thrown if there is a problem interpreting the bytes
	 */
	public void interpretBytes() throws HDFException {
		int i, numLabels, lengthCounter;
		int[] lengths;
		byte[] temp;
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

		try {
			numLabels = 0;
			lengthCounter = 0;
			lengths = new int[10];
			//hard to imagine needing this many dimensions, so should be sufficient
			for (i = 0; i < bytes.length; i++, lengthCounter++) {
				if (bytes[i] == (byte) 0) {
					lengths[numLabels] = lengthCounter;
					numLabels++;
					lengthCounter = 0;
				}
			}
			labels = new String[numLabels];
			for (i = 0; i < numLabels; i++) {
				temp = new byte[lengths[i]];
				bais.read(temp);
				labels[i] = new String(temp);
				bais.read(); //skip null
			}
		} catch (IOException e) {
			throw new HDFException(
				"Problem interpreting SDL.",e);
		}
	}
}
