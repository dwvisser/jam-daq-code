package jam.io.hdf;
import jam.data.Histogram;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.swing.JOptionPane;

/**
 * Class to represent an HDF <em>Scientific Data Dimension</em> data object.
 *
 * @version	0.5 November 98
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @since       JDK1.1
 */
public class ScientificDataDimension extends DataObject {

	/**
	 * The number of dimensions
	 */
	int rank;

	/**
	 * The size of the dimensions.  I have assumed identical x- and y- dimensions for
	 * 2-d spectra.
	 */
	int sizeX;
	int sizeY;

	boolean isDouble;

	private byte numberType;

	private ScientificDataScales sds;

	public ScientificDataDimension(HDFile fi, Histogram h) {
		super(fi, DFTAG_SDD); //sets tag
		rank = h.getDimensionality();
		sizeX = h.getSizeX();
		sizeY = h.getSizeY();
		isDouble = !h.getType().isInteger();
		int byteLength = 6 + 8 * rank; // see p. 6-33 HDF 4.1r2 specs
		ByteArrayOutputStream baos = new ByteArrayOutputStream(byteLength);
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeShort(rank);
			//next 2 lines write the dimensions of the ranks
			dos.writeInt(sizeX);
			if (rank == 2)
				dos.writeInt(sizeY);
			//write out data number type
			if (isDouble) {
				numberType = NumberType.DOUBLE;
				dos.writeShort(file.getDoubleType().getTag());
				dos.writeShort(file.getDoubleType().getRef());
			} else {
				numberType = NumberType.INT;
				dos.writeShort(file.getIntType().getTag());
				dos.writeShort(file.getIntType().getRef());
			}
			for (int i = 0; i < rank; i++) { // write out scale number type
				dos.writeShort(file.getIntType().getTag());
				dos.writeShort(file.getIntType().getRef());
			}
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(null,ioe.getMessage(),
			getClass().getName(),JOptionPane.ERROR_MESSAGE);
		}
		bytes = baos.toByteArray();
		sds = new ScientificDataScales(this);
	}

	public ScientificDataDimension(HDFile hdf, byte[] data, short t, short reference) {
		super(hdf, data, t, reference);
	}

	public void interpretBytes() {
		short numberTag, numberRef;
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);

		try {
			rank = dis.readShort();
			//next 2 lines read dimensions of ranks
			sizeX = dis.readInt();
			if (rank == 2)
				sizeY = dis.readInt();
			numberTag = dis.readShort();
			numberRef = dis.readShort();
			numberType =
				((NumberType) file.getObject(numberTag, numberRef)).getType();
			/* We don't bother reading the scales */
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(null,ioe.getMessage(),
			getClass().getName(),JOptionPane.ERROR_MESSAGE);
		}
	}

	public int getRank() {
		return rank;
	}

	public int getSizeX() {
		return sizeX;
	}

	public int getSizeY() {
		return sizeY;
	}

	public byte getType() {
		return numberType;
	}

	public ScientificDataScales getSDS() {
		return sds;
	}

}
