package jam.io.hdf;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Class to represent an HDF <em>Scientific Data Dimension</em> data object.
 *
 * @version	0.5 November 98
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 */
final class ScientificDataScales extends DataObject {

	/**
	 * The number of dimensions
	 */
	private final int rank;

	/**
	 * The size of the dimensions.  
	 */
	private final int sizeX, sizeY;

	private static final byte TRUE = 1;//FALSE=0

	ScientificDataScales(ScientificDataDimension sdd) throws HDFException {
		super(DFTAG_SDS); //sets tag
		rank = sdd.getRank();
		sizeX = sdd.getSizeX();
		sizeY = sdd.getSizeY();
		final byte NTsize = NumberType.INT_SIZE;
		//int byteLength=rank + rank * NTsize * size; // see p. 6-33 HDF 4.1r2 specs
		int byteLength = rank + NTsize * sizeX;
		if (rank == 2) {
			byteLength += NTsize * sizeY;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream(byteLength);
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			for (int i = 0; i < rank; i++) {
				dos.writeByte(TRUE);
			}
			for (int i = 0; i < rank; i++) {
				for (int j = 0; j < rank; j++) {
					dos.writeInt(j);
				}
			}
		} catch (IOException ioe) {
			throw new HDFException("Creating ScientificDataScales.",ioe);
		}
		bytes = baos.toByteArray();
	}

	protected void interpretBytes() {
	}
}
