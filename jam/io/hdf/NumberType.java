package jam.io.hdf;

/**
 * Class to represent a 32-bit java int HDF <em>NumberType</em> data object.
 * When constructed with argument <code>NumberType.INT</code>, creates the object indicating
 * <code>int</code> primitives.  When constructed with argument <code>NumberType.DOUBLE</code>,
 * creates the object indicating <code>double</code> primitives.
 *
 * @version	0.5 November 98
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @since       JDK1.1
 */
public class NumberType extends DataObject {

    /**
     * First version of encoding
     */
    private final static byte NT_VERSION=1;

    /**
     * Unsigned int 
     */
    private final static byte	DFNT_UINT32	    = 25;		   
    
    /**
     * int
     */
    private final static byte	DFNT_INT32	    = 24;
    
    /**
     * 64-bit floating point, meant for Java <code>double</code>
     */ 
    private final static byte	DFNT_FLOAT64	    = 6;

    /**
     * int width, in bits
     */
    private final static byte	IntWidth	    = 32;	
    
    /**
     * double width, in bits
     */	    
    private final static byte	DoubleWidth	    = 64;

    /**
     * Motorola byte order, (same as Java), 
     */
    private final static byte	DFNT_MBO	    = 1;		
    
    private byte numberType;
    static final public byte INT=0;
    static final public byte DOUBLE=1;     
    public static final byte INT_SIZE=4;
    public static final byte DOUBLE_SIZE=8;

    /**
     *  @exception HDFException unrecoverable error
     */
    public NumberType(HDFile fi, byte type) throws HDFException{
	super(fi, DFTAG_NT);//sets tag
	if (type==INT){
	    bytes = new byte [] {NT_VERSION, DFNT_INT32, IntWidth, DFNT_MBO};
	} else if (type==DOUBLE) {
	    bytes = new byte [] {NT_VERSION, DFNT_FLOAT64, DoubleWidth, DFNT_MBO};
	} else { 
	    throw new HDFException ("Invalid type for NumberType.");
	}
    }
    
    public NumberType(HDFile hdf,byte [] data, short reference) {
	super(hdf,data,reference);
	tag=DFTAG_NT;
    }
    
    /**
     * Implementation of <code>DataObject</code> abstract method.
     *
     * @exception HDFException thrown if there is a problem interpreting the bytes
     */
    public void interpretBytes() throws HDFException {
	switch (bytes[1]) {
	    case DFNT_UINT32 :	numberType=INT;
				break;
	    case DFNT_INT32  :  numberType=INT;
				break;
	    case DFNT_FLOAT64:	numberType=DOUBLE;
				break;
	    default	     :	throw new HDFException ("NumberType.interpretBytes(): Unrecognized number type.");
	}
    }
    
    public byte getType(){
	return numberType;
    }
    
}
