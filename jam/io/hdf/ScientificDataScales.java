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
public class ScientificDataScales extends DataObject {

    /**
     * The number of dimensions
     */
    int rank;
    
    /**
     * The size of the dimensions.  
     */
    int sizeX, sizeY;
    
    boolean isDouble;
    
    private byte numberType;
    
    private static final byte TRUE=1;
    private static final byte FALSE=0;
    
    private ScientificDataDimension sdd;
    
    private int NTsize;

    public ScientificDataScales(ScientificDataDimension sdd){
	super(sdd.getFile(), DFTAG_SDS);//sets tag
	int i,j;
	this.sdd =sdd;
	rank=sdd.getRank();
	sizeX=sdd.getSizeX();
        sizeY=sdd.getSizeY();
	numberType=sdd.getType();
	NTsize = NumberType.INT_SIZE;
	//int byteLength=rank + rank * NTsize * size; // see p. 6-33 HDF 4.1r2 specs
	int byteLength = rank + NTsize*sizeX;
        if (rank == 2) byteLength += NTsize*sizeY;
        ByteArrayOutputStream baos=new ByteArrayOutputStream(byteLength);
	DataOutputStream dos=new DataOutputStream(baos);
	try{
	    for(i=0;i<rank;i++){
		dos.writeByte(TRUE);
	    }
	    for(i=0;i<rank;i++){
		for (j=0;j<rank;j++){
		    dos.writeInt(j);
		}
	    }
	} catch (IOException ioe) {
	    System.err.println(ioe);
	}
	bytes=baos.toByteArray();
    }
    
    public ScientificDataScales(HDFile hdf, byte [] data, short reference) {
	super(hdf,data,reference);
	tag=DFTAG_SDS;
    }
    
    public void interpretBytes(){
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
    
}
