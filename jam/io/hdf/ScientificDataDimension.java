package jam.io.hdf;

import java.util.*;
import java.io.*;
import jam.data.Histogram;

/**
 * Class to represent an HDF <em>Scientific Data Dimension</em> data object.
 *
 * @version	0.5 November 98
 * @author 	Dale Visser
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

    public ScientificDataDimension(HDFile fi,Histogram h){
	super(fi, DFTAG_SDD);//sets tag
	this.rank=h.getDimensionality();
	this.sizeX=h.getSizeX();
        this.sizeY=h.getSizeY();
	isDouble = (h.getType()==Histogram.ONE_DIM_DOUBLE||h.getType()==Histogram.TWO_DIM_DOUBLE);
	int byteLength=6 + 8 * rank; // see p. 6-33 HDF 4.1r2 specs
	ByteArrayOutputStream baos=new ByteArrayOutputStream(byteLength);
	DataOutputStream dos=new DataOutputStream(baos);
	try{
	    dos.writeShort(rank);
            //next 2 lines write the dimensions of the ranks
            dos.writeInt(sizeX);
            if (rank==2) dos.writeInt(sizeY);
	    //write out data number type
	    if (isDouble){
		numberType=NumberType.DOUBLE;
		dos.writeShort(file.getDoubleType().getTag());
		dos.writeShort(file.getDoubleType().getRef());
	    } else {
		numberType=NumberType.INT;
		dos.writeShort(file.getIntType().getTag());
		dos.writeShort(file.getIntType().getRef());
	    }
	    for (int i=0;i<rank;i++) { // write out scale number type
		dos.writeShort(file.getIntType().getTag());
	        dos.writeShort(file.getIntType().getRef());
	    }
	} catch (IOException ioe) {
	    System.err.println(ioe);
	}
	bytes=baos.toByteArray();
	sds = new ScientificDataScales(this);
    }
    
    public ScientificDataDimension(HDFile hdf, byte [] data, short reference) {
	super(hdf,data,reference);
	tag=DFTAG_SDD;
    }
    
    public void interpretBytes(){
	short numberTag,numberRef;
	ByteArrayInputStream bais=new ByteArrayInputStream(bytes);
	DataInputStream dis=new DataInputStream(bais);
	
	try{
	    rank=dis.readShort();
            //next 2 lines read dimensions of ranks
            sizeX = dis.readInt();
            if (rank==2) sizeY = dis.readInt();
	    numberTag=dis.readShort();
	    numberRef=dis.readShort();
	    numberType = ((NumberType)file.getObject(numberTag,numberRef)).getType();
	    //System.out.println("SDD ref "+ref+"NumType "+numberType);
	    //don't bother reading scales
	    for (int i=0;i<rank;i++) { 
		//dos.writeShort(file.getNumberType().getTag());
		//dos.writeShort(file.getNumberType().getRef());
	    }
	} catch (IOException ioe) {
	    System.err.println(ioe);
	}
    }
    
    public int getRank(){
	return rank;
    }
    
    public int getSizeX(){
	return sizeX;
    }
    
    public int getSizeY(){
        return sizeY;
    }
    
    public byte getType(){
	return numberType;
    }
    
    public ScientificDataScales getSDS(){
	return sds;
    }
    
}
