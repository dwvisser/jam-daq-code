package jam.io.hdf;
import java.io.*;
import jam.util.*;

/**
 * Class to represent an HDF <em>scientific data label</em> data object.  The label is meant to be a short
 * probably one or 2 word <em>label</em>.
 *
 * @version	0.5 December 98
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @since       JDK1.1
 */
public class ScientificDataLabel extends DataObject {

	String[] labels;

	String allLabels;

	public ScientificDataLabel(HDFile hdf, String[] label) {
		super(hdf, DFTAG_SDL); //sets tag
		this.file = hdf;
		this.labels = label;
		allLabels = new String();
		for (int i = 0; i < labels.length; i++) {
			allLabels += labels[i];
			allLabels += "\0";
		}
		bytes = StringUtilities.ASCIIarray(allLabels);
	}

	public ScientificDataLabel(HDFile hdf, String label) {
		super(hdf, DFTAG_SDL); //sets tag
		this.file = hdf;
		labels = new String[1];
		this.labels[0] = label;
		allLabels = label + "\0";
		bytes = StringUtilities.ASCIIarray(allLabels);
	}

	public ScientificDataLabel(HDFile hdf, byte[] data, short t, short reference) {
		super(hdf, data, t, reference);
		//tag = DFTAG_SDL;
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
				"Problem interpreting SDL: " + e.getMessage());
		}
	}

	/**
	 * Returns the text contained.
	 */
	public String[] getLabels() {
		return labels;
	}

	/*public DataObject getObject(){
	return object;
	}*/

}
