package jam.io.hdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Class to represent an HDF <em>Scientific Data Dimension</em> data object.
 *
 * @version	0.5 November 98
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @since       JDK1.1
 */
final class ScientificDataDimension extends DataObject {

	/**
	 * The number of dimensions
	 */
	private int rank;

	/**
	 * The size of the dimensions.  I have assumed identical x- and y- dimensions for
	 * 2-d spectra.
	 */
	private int sizeX;
	private int sizeY;

	private byte numberType;
	
	static ScientificDataDimension getSDD(int rank, int sizeX, int sizeY, byte numberType) throws HDFException  {
		ScientificDataDimension rval=null;//return value
		final Iterator temp = DataObject.ofType(DataObject.DFTAG_SDD).iterator();
		while (temp.hasNext()) {
			final ScientificDataDimension sdd = (ScientificDataDimension) temp.next();
			if ( (sdd.getRank()==rank) &&
				 (sdd.getType() == numberType) && 
				 (sdd.getSizeX() == sizeX) && 
				 (sdd.getSizeY() == sizeY)) {
					rval = sdd;
					break;//for quicker execution
			}
		}
		if (rval==null){
			rval = new ScientificDataDimension(rank, sizeX, sizeY, numberType);
		}
		return rval;
	}
	
	ScientificDataDimension(int rank, int sizeX, int sizeY, byte numberType) throws HDFException {
		super(DFTAG_SDD); //sets tag
		this.rank=rank;
		this.sizeX=sizeX;
		this.sizeY=sizeY;
		this.numberType=numberType;
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
			if (numberType== NumberType.DOUBLE) {
				dos.writeShort(NumberType.getDoubleType().getTag());
				dos.writeShort(NumberType.getDoubleType().getRef());
			} else {
				dos.writeShort(NumberType.getIntType().getTag());
				dos.writeShort(NumberType.getIntType().getRef());
			}
			for (int i = 0; i < rank; i++) { // write out scale number type
				dos.writeShort(NumberType.getIntType().getTag());
				dos.writeShort(NumberType.getIntType().getRef());
			}
		} catch (IOException ioe) {
			throw new HDFException("Creating ScientificDataDimension", ioe);
		}
		bytes = baos.toByteArray();
		/* Create new data scales object to go with this.
		 * A reference variable is not needed.
		 */
		new ScientificDataScales(this);
	}
	
	ScientificDataDimension(){
	    super();
	}

	public void interpretBytes() throws HDFException {
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
				((NumberType) getObject(numberTag, numberRef)).getType();
			/* We don't bother reading the scales */
		} catch (IOException ioe) {
			throw new HDFException("interpretBytes ScientificDataDimension", ioe);
		}
	}

	int getRank() {
		return rank;
	}

	int getSizeX() {
		return sizeX;
	}

	int getSizeY() {
		return sizeY;
	}

	byte getType() {
		return numberType;
	}
}
