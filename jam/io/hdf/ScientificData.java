package jam.io.hdf;

import java.nio.ByteBuffer;

/**
 * Class to represent an HDF <em>Scientific Data</em> data object.
 * 
 * @version 0.5 November 98
 * @author <a href="mailto:dale@visser.name">Dale Visser </a>
 * @since JDK1.1
 */
final class ScientificData extends AbstractHData {

    /**
     * The number of dimensions
     */
    private int rank;

    /**
     * The size of the dimensions. //PROBLEM I have assumed identical x- and y-
     * dimensions for 2-d spectra. In the process of fixing this.
     */
    private int sizeX;

    private int sizeY;

    /**
     * By default, is STORE which indicates normal mode of operation, where
     * <code>bytes</code> is read in when instantizing, which can be a memory
     * hog and cause problems. Otherwise, is WAIT_AND_READ which only creates
     * the internal array when the data is requested, and then dereferences it.
     */
    private byte inputMode;

    private static final byte STORE = 0;

    private static final byte WAIT_TO_READ = 1;

    private static final String REF_MSG = "AbstractHData mode not properly set: Ref# ";

    private int[] counts;

    private double[] countsD;

    private int[][] counts2d;

    private double[][] counts2dD;

    private int byteLength;

    private byte numberType;

    ScientificData(int[] counts) {
        super(DFTAG_SD); //sets tag
        numberType = NumberType.INT;
        inputMode = STORE;
        rank = 1;
        sizeX = counts.length;
        byteLength = NumberType.INT_SIZE * sizeX; // see p. 6-34 HDF 4.1r2 specs
        this.counts = counts;
    }

    ScientificData(double[] counts) {
        super(DFTAG_SD); //sets tag
        numberType = NumberType.DOUBLE;
        inputMode = STORE;
        rank = 1;
        sizeX = counts.length;
        byteLength = NumberType.DOUBLE_SIZE * sizeX; // see p. 6-34 HDF 4.1r2
                                                     // specs
        this.countsD = counts;
    }

    ScientificData(int[][] counts2d) {
        super(DFTAG_SD); //sets tag
        numberType = NumberType.INT;
        inputMode = STORE;
        rank = 2;
        sizeX = counts2d.length;
        sizeY = counts2d[0].length;
        byteLength = NumberType.INT_SIZE * sizeX * sizeY; // see p. 6-34 HDF
                                                          // 4.1r2 specs
        this.counts2d = counts2d;
    }

    ScientificData(double[][] counts2d) {
        super(DFTAG_SD); //sets tag
        numberType = NumberType.DOUBLE;
        inputMode = STORE;
        rank = 2;
        sizeX = counts2d.length;
        sizeY = counts2d[0].length;
        byteLength = NumberType.DOUBLE_SIZE * sizeX * sizeY;
        // see p. 6-34 HDF 4.1r2 specs
        this.counts2dD = counts2d;
    }

    ScientificData() {
        super();
    }
    
    void init(int offset, int length, short tag, short reference) {
        super.init(offset, length, tag, reference);
        inputMode = WAIT_TO_READ;
    }
    
    void init(byte [] bytes, short tag, short reference) {
        super.init(bytes, tag, reference);
        inputMode = STORE;
    }


    /**
     * requires associated SDD, NT, NDG records
     */
    public void interpretBytes() { }
    
    /* non-javadoc:
     * Late loading of data
     */
    Object getData(HDFile infile, int histDim, byte histNumType, int sizeX, int sizeY) throws HDFException {
    	Object rval;
        if ( (histDim == 1) && (histNumType == NumberType.INT) ) {
            rval = getData1d(infile, sizeX);
        } else if ( (histDim == 1) && (histNumType == NumberType.DOUBLE) ) {
        	rval = getData1dD(infile, sizeX);
        } else if ( (histDim == 2) && (histNumType == NumberType.INT) ) {
        	rval =getData2d(infile, sizeX, sizeY);
        } else if ( (histDim == 2) && (histNumType == NumberType.DOUBLE) ) {    
        	rval =getData2dD(infile, sizeX, sizeY);
        } else { 
        	throw new HDFException("Unknown histogram data type");
        }
        return rval;

    }
    /*
     * non-javadoc: @throws HDFException unrecoverable error @throws
     * UnsupportedOperationException if this object doesn't represent 1d int
     * @throws IllegalStateException if the input mode isn't recognized
     */
    int[] getData1d(HDFile infile, int size) throws HDFException {                                                                                                                                 
        if (numberType != NumberType.INT || rank != 1) {
            throw new HDFException(
                    "getData1d called on wrong type of SD.");
        }
        final byte[] localBytes=getLocalBytes(infile);
        final int[] output = new int[size];
        final ByteBuffer buffer = ByteBuffer.wrap(localBytes);
        for (int i = 0; i < size; i++) {
            output[i] = buffer.getInt();
        }
        bytes = ByteBuffer.allocate(0); //empty it
        return output;
    }

    double[] getData1dD(HDFile infile, int size) throws HDFException {                                                                                                                                                        
        double[] output;
        if (numberType != NumberType.DOUBLE || rank != 1) {
            throw new HDFException("SD ref#" + getRef()
                    + ": getData1dD() called on wrong type");
        }
        final byte[] localBytes = getLocalBytes(infile);
        output = new double[size];
        final ByteBuffer buffer = ByteBuffer.wrap(localBytes);
            for (int i = 0; i < size; i++) {
            output[i] = buffer.getDouble();
        }
        bytes = ByteBuffer.allocate(0); //empty it
        return output;
    }

    int[][] getData2d(HDFile infile, int sizeX, int sizeY)
            throws HDFException {
        if (numberType != NumberType.INT || rank != 2) {
            throw new HDFException("getData2d called on wrong type of SD.");
        }
        final byte[] localBytes = getLocalBytes(infile);
        int[][] output = new int[sizeX][sizeY];
        final ByteBuffer buffer = ByteBuffer.wrap(localBytes);
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                output[i][j] = buffer.getInt();
            }
        }
        bytes = ByteBuffer.allocate(0); //empty it
        return output;
    }

    double[][] getData2dD(HDFile infile, int sizeX, int sizeY)
            throws HDFException {
        if (numberType != NumberType.DOUBLE || rank != 2) {
            throw new HDFException("getData2dD called on wrong type of SD.");
        }
        final byte[] localBytes = getLocalBytes(infile);
        double[][] output = new double[sizeX][sizeY];
        final ByteBuffer buffer = ByteBuffer.wrap(localBytes);        
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
            	 output[i][j]= buffer.getDouble();
            }
        }
        bytes = ByteBuffer.allocate(0); //empty it
        return output;
    }

    /**
     * Returns the byte representation to be written at <code>offset</code> in
     * the file.
     * 
     * @throws HDFException
     *             thrown if unrecoverable error occurs
     * @throws IllegalStateException
     *             if the rank is not 1 or 2
     */
    ByteBuffer getBytes() {
        bytes = ByteBuffer.allocate(byteLength);
        switch (rank) {
        case 1:
            for (int i = 0; i < sizeX; i++) { // write out data type
                if (numberType == NumberType.INT) {
                    bytes.putInt(counts[i]);
                } else {
                    bytes.putDouble(countsD[i]);
                }
            }
            break;
        case 2:
            for (int i = 0; i < sizeX; i++) { // write out data type
                for (int j = 0; j < sizeY; j++) {
                    if (numberType == NumberType.INT) {
                        bytes.putInt(counts2d[i][j]);
                    } else {
                        bytes.putDouble(counts2dD[i][j]);
                    }
                }
            }
            break;
        default:
            throw new IllegalStateException("SD_" + tag
                    + ", bad value for rank: " + rank);
        }
        return bytes;
    }

    void setNumberType(byte type) {
        synchronized (this) {
            numberType = type;
        }
    }

    int getNumberType() {
        return numberType;
    }

    void setRank(int newRank) {
        synchronized (this) {
            rank = newRank;
        }
    }
    
    public String toString(){
        final StringBuffer rval=new StringBuffer();
        final String type = numberType == NumberType.DOUBLE ? "Double" : "Integer";
        final String times=" x ";
        rval.append("SD ").append(ref).append(": ").append(type).append(times).append(sizeX);
        if (rank ==2){
            rval.append(times).append(sizeY);
        }
        return rval.toString();
    }
    
    private byte [] getLocalBytes(HDFile infile) throws HDFException {
        final byte [] localBytes;
        switch (inputMode) {
        case STORE: //read data from internal array
            localBytes = bytes.array();
            break;
        case WAIT_TO_READ:
        	localBytes = infile.lazyReadData(this);        
        	break;
        default:
            throw new HDFException(REF_MSG + ref);
        }
        return localBytes;
    }
}