package jam.util;
/**
 * Contains utilities for manipulating <code>Numbers</code>
 * Converts native types to array of bytes and array of
 * bytes to native types.
 *  
 * @author  Ken Swartz
 * @version 0.5
 * @see	    java.lang.Double
 * @see	    java.lang.Float 
 */
public class NumberUtilities{
     
    /** 
     * Class constructor.
     */
    public NumberUtilities(){
    }
    /**
     * short to array of bytes
     */
    public static final byte [] shortToBytes(short s){
	byte [] out =new byte [2];
	
	out[0]=(byte)((s >>> 8) & 0xFF);
	out[1]=(byte)((s >>> 0) & 0xFF);
	
	return out;
    }   
    /**
     * array of bytes to a short
     */    
    public static final short bytesToShort(byte [] sb){
	return (short)((sb[0]<<8)+(sb[1]<<0));
    }
    /**
     * int to array of bytes
     */
    public static final byte [] intToBytes(int i){
	byte [] out =new byte [4];
	
	out[0]=(byte)((i >>> 24) & 0xFF);
	out[1]=(byte)((i >>> 16) & 0xFF);
	out[2]=(byte)((i >>>  8) & 0xFF);
	out[3]=(byte)((i >>>  0) & 0xFF);
	
	return out;
    }   
    /**
     * array of bytes to a int
     */    
    public static final int bytesToInt(byte [] ib){
	return ((ib[0]<<24)+(ib[1]<<16)+(ib[2]<<8)+(ib[3]<<0));

    }
    /**
     * long to array of bytes
     */
    public static final byte [] longToBytes(long l){
	byte [] out =new byte [8];

	out[0]=(byte)((l >>> 56) & 0xFF);
	out[1]=(byte)((l >>> 48) & 0xFF);
	out[2]=(byte)((l >>> 40) & 0xFF);
	out[3]=(byte)((l >>> 32) & 0xFF);
	out[4]=(byte)((l >>> 24) & 0xFF);
	out[5]=(byte)((l >>> 16) & 0xFF);
	out[6]=(byte)((l >>>  8) & 0xFF);
	out[7]=(byte)((l >>>  0) & 0xFF);
	
	return out;
    }   
    /**
     * array of bytes to a long
     */    
    public static final long bytesToLong(byte [] lb){
	int tempInt1 =(lb[0]<<24)+(lb[1]<<16)+(lb[2]<<8)+(lb[3]<<0);    
	int tempInt2 =(lb[4]<<24)+(lb[5]<<16)+(lb[6]<<8)+(lb[7]<<0); 
	return ((long)(tempInt1 << 32) + (tempInt2 & 0xFFFFFFFFL) );    

    }
    
    
    /**
     *  convert a float to an array of 4 bytes
     */
    public static final byte[] floatToBytes(float f){
	int tempInt=Float.floatToIntBits(f);	
	return intToBytes(tempInt);		    
    }   
    /**
     *
     */    
    public static final float bytesToFloat(byte [] fb){
	int tempInt =(fb[0]<<24)+(fb[1]<<16)+(fb[2]<<8)+(fb[3]<<0);
	return Float.intBitsToFloat(tempInt);			    	
    }
    /**
     *
     */
    public static final byte [] doubleToBytes(double d){
	long tempLong=Double.doubleToLongBits(d);	
	return longToBytes(tempLong);		    
    }   
    /**
     *
     */
    public static final double bytesToDouble(byte [] db){
	int tempInt1 =(db[0]<<24)+(db[1]<<16)+(db[2]<<8)+(db[3]<<0);    
	int tempInt2 =(db[4]<<24)+(db[5]<<16)+(db[6]<<8)+(db[7]<<0); 
	return Double.longBitsToDouble ((long)(tempInt1 << 32) + (tempInt2 & 0xFFFFFFFFL) );    
    }
    public static final byte[] charToBytes(char c){
	byte out []=new byte[1];
	out[0]=(byte)c;
			//out = new byte [1];			
			//numBytes=1;
			//baos = new ByteArrayOutputStream(numBytes);
			//dos = new DataOutputStream(baos);
			//char [] carray = new char[1];
			//carray [0] = ((Character)(cells[col][row])).charValue();
			//dos.writeBytes(new String(carray));
			//out=baos.toByteArray();
	return out;
    }   
    /**
     *
     */
    public static final char bytesToChar(byte cb[]){
	char c=(char)cb[0];
	return c;
    }
    
}
