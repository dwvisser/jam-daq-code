package jam.io.hdf;

import java.nio.ByteBuffer;

import static jam.io.hdf.Constants.DFTAG_SDS;

/**
 * Class to represent an HDF <em>Scientific Data Dimension</em> data object.
 * 
 * @version 0.5 November 98
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser </a>
 */
final class ScientificDataScales extends AbstractData {

	ScientificDataScales() {
		super(DFTAG_SDS); // sets tag
	}

	ScientificDataScales(final ScientificDataDimension sdd) {
		this();
		final int rank = sdd.getRank();
		final int sizeX = sdd.getSizeX();
		final int sizeY = sdd.getSizeY();
		final byte NTsize = NumberType.INT_SIZE;
		/* see p. 6-33 HDF 4.1r2 specs */
		int byteLength = rank + NTsize * sizeX;
		if (rank == 2) {
			byteLength += NTsize * sizeY;
		}
		bytes = ByteBuffer.allocate(byteLength);
		final byte TRUE = 1;// FALSE=0
		for (int i = 0; i < rank; i++) {
			bytes.put(TRUE);
		}
		for (int i = 0; i < rank; i++) {
			for (int j = 0; j < rank; j++) {
				bytes.putInt(j);
			}
		}
	}

	@Override
	protected void interpretBytes() {
		// do-nothing
	}
}