package jam.io.hdf;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * Class to represent an HDF <em>Scientific Data</em> data object.
 * 
 * @version 0.5 November 98
 * @author <a href="mailto:dale@visser.name">Dale Visser </a>
 * @since JDK1.1
 */
final class ScientificData extends DataObject {

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

    private static final String REF_MSG = "DataObject mode not properly set: Ref# ";

    private static final String TWOD_MSG = "Problem getting 2D data in SD: ";

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
    void init(byte [] bytes, short tag, short reference) throws HDFException {
        super.init(bytes, tag, reference);
        inputMode = STORE;
    }


    /**
     * requires associated SDD, NT, NDG records
     */
    public void interpretBytes() {
    	if (inputMode==STORE) {
    		
    	}
    }

    /*
     * non-javadoc: @throws HDFException unrecoverable error @throws
     * UnsupportedOperationException if this object doesn't represent 1d int
     * @throws IllegalStateException if the input mode isn't recognized
     */
    int[] getData1d(HDFile infile, int size) throws HDFException { 
                                                                                                                                                      
        final byte[] localBytes;
        if (numberType != NumberType.INT || rank != 1) {
            throw new HDFException(
                    "getData1d called on wrong type of SD.");
        }
        switch (inputMode) {
        case STORE: //read data from internal array
            localBytes = bytes.array();
            break;
        case WAIT_TO_READ:
            localBytes = infile.lazyReadData(this);
            break;
        default:
            throw new IllegalStateException(REF_MSG + ref);
        }
        final int[] output = new int[size];
        final DataInput dataInput = new DataInputStream(
                new ByteArrayInputStream(localBytes));
        try {
            for (int i = 0; i < size; i++) {
                output[i] = dataInput.readInt();
            }
        } catch (IOException e) {
            throw new HDFException("Problem getting 1d Data in SD: "
                    + e.getMessage());
        }
        bytes =null;	//so they can be gc'ed
        return output;
    }

    double[] getData1dD(HDFile infile, int size) throws HDFException { 
                                                                                                                                                             
        double[] output;
        byte[] localBytes;

        if (numberType != NumberType.DOUBLE || rank != 1) {
            throw new HDFException("SD ref#" + getRef()
                    + ": getData1dD() called on wrong type");
        }
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
        output = new double[size];
        final DataInput dataInput = new DataInputStream(
                new ByteArrayInputStream(localBytes));
        try {
            for (int i = 0; i < size; i++) {
                output[i] = dataInput.readDouble();
            }
        } catch (IOException e) {
            throw new HDFException("Problem getting 1D data in SD: "
                    + e.getMessage());
        }
        bytes =null;	//so they can be gc'ed
        return output;
    }

    int[][] getData2d(HDFile infile, int sizeX, int sizeY)
            throws HDFException {
        final byte[] localBytes;

        if (numberType != NumberType.INT || rank != 2) {
            throw new HDFException("getData2d called on wrong type of SD.");
        }
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
        int[][] output = new int[sizeX][sizeY];
        final DataInput dataInput = new DataInputStream(
                new ByteArrayInputStream(localBytes));
        try {
            for (int i = 0; i < sizeX; i++) {
                for (int j = 0; j < sizeY; j++) {
                    output[i][j] = dataInput.readInt();
                }
            }
        } catch (IOException e) {
            throw new HDFException(TWOD_MSG + e.getMessage());
        }
        bytes =null;	//so they can be gc'ed
        return output;
    }

    double[][] getData2dD(HDFile infile, int sizeX, int sizeY)
            throws HDFException {
        final byte[] localBytes;

        if (numberType != NumberType.DOUBLE || rank != 2) {
            throw new HDFException("getData2dD called on wrong type of SD.");
        }
        switch (inputMode) {
        case STORE: //read data from internal array
            localBytes = bytes.array();
            break;
        case WAIT_TO_READ:
        	localBytes = infile.lazyReadData(this);        	
        default:
            throw new HDFException(REF_MSG + ref);
        }
        double[][] output = new double[sizeX][sizeY];
        final DataInput dataInput = new DataInputStream(
                new ByteArrayInputStream(localBytes));
        try {
            for (int i = 0; i < sizeX; i++) {
                for (int j = 0; j < sizeY; j++) {
                    output[i][j] = dataInput.readDouble();
                }
            }
        } catch (IOException e) {
            throw new HDFException(TWOD_MSG + e.getMessage());
        }
        bytes =null;	//so they can be gc'ed
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

    /**
     * @return the length of the byte array in the file for this data element.
     */
    /* FIXME KBS remove
    protected int getByteLength() {
        return byteLength;
    }
    */
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
}