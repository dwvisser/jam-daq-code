package jam.io.hdf;

import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * Class to represent an HDF <em>Scientific Data Dimension</em> data object.
 * 
 * @version 0.5 November 98
 * @author <a href="mailto:dale@visser.name">Dale Visser </a>
 * @since JDK1.1
 */
final class ScientificDataDimension extends AbstractHData {

    /**
     * The number of dimensions
     */
    private int rank;

    /**
     * The size of the dimensions. I have assumed identical x- and y- dimensions
     * for 2-d spectra.
     */
    private int sizeX;

    private int sizeY;

    private byte numberType;

    static ScientificDataDimension create(short rank, int sizeX, int sizeY,
            byte numberType) {
        ScientificDataDimension rval = null;//return value
        final Iterator temp = AbstractHData.ofType(AbstractHData.DFTAG_SDD)
                .iterator();
        while (temp.hasNext()) {
            final ScientificDataDimension sdd = (ScientificDataDimension) temp
                    .next();
            if ((sdd.getRank() == rank) && (sdd.getType() == numberType)
                    && (sdd.getSizeX() == sizeX) && (sdd.getSizeY() == sizeY)) {
                rval = sdd;
                break;//for quicker execution
            }
        }
        if (rval == null) {
            rval = new ScientificDataDimension(rank, sizeX, sizeY, numberType);
        }
        return rval;
    }

    private ScientificDataDimension(short rank, int sizeX, int sizeY, byte numberType) {
        super(DFTAG_SDD); //sets tag
        this.rank = rank;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.numberType = numberType;
        final int byteLength = 6 + 8 * rank; // see p. 6-33 HDF 4.1r2 specs
        bytes = ByteBuffer.allocate(byteLength);
        bytes.putShort(rank);
        /* next 2 lines write the dimensions of the ranks */
        bytes.putInt(sizeX);
        if (rank == 2) {
            bytes.putInt(sizeY);
        }
        /* write out data number type */
        final AbstractHData itype = NumberType.getIntType();
        if (numberType == NumberType.DOUBLE) {
            final AbstractHData dtype = NumberType.getDoubleType();
            bytes.putShort(dtype.getTag());
            bytes.putShort(dtype.getRef());
        } else {
            bytes.putShort(itype.getTag());
            bytes.putShort(itype.getRef());
        }
        for (int i = 0; i < rank; i++) { // write out scale number type
            bytes.putShort(itype.getTag());
            bytes.putShort(itype.getRef());
        }
        /*
         * Create new data scales object to go with this. A reference variable
         * is not needed.
         */
        new ScientificDataScales(this);
    }

    ScientificDataDimension() {
        super();
    }

    public void interpretBytes() {
        short numberTag, numberRef;
        bytes.position(0);
        rank = bytes.getShort();
        /* next 2 lines read dimensions of ranks */
        sizeX = bytes.getInt();
        sizeY = (rank == 2) ? bytes.getInt() : 0;
        numberTag = bytes.getShort();
        numberRef = bytes.getShort();
        numberType = ((NumberType) getObject(numberTag, numberRef)).getType();
        /* We don't bother reading the scales */
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
    
    public String toString(){
        final StringBuffer rval=new StringBuffer();
        final String type = numberType == NumberType.DOUBLE ? "Double" : "Integer";
        final String times=" x ";
        rval.append("SDD ").append(ref).append(": ").append(type).append(times).append(sizeX);
        if (rank ==2){
            rval.append(times).append(sizeY);
        }
        return rval.toString();
    }
}