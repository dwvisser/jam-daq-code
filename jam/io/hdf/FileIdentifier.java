package jam.io.hdf;

import java.util.*;
import java.io.*;

/**
 * Class to represent an HDF <em>File Identifier</em> data object.  The label is meant to be a user supplied 
 * title for the file.
 *
 * @version	0.5 December 98
 * @author 	Dale Visser
 * @since       JDK1.1
 */
public class FileIdentifier extends DataObject {

    /**
     * Object being labelled.
     */
    DataObject object;
    
    String label;
        
    public FileIdentifier(HDFile hdf,String label){
	super(hdf, DFTAG_FID);//sets tag
	this.label=label;
	int byteLength=label.length();
	ByteArrayOutputStream baos=new ByteArrayOutputStream(byteLength);
	DataOutputStream dos=new DataOutputStream(baos);
	try{
	    dos.writeBytes(label);
	} catch (IOException ioe) {
	    System.err.println(ioe);
	}
	bytes=baos.toByteArray();
    }
    
    public FileIdentifier(HDFile hdf, byte [] data, short reference) {
	super(hdf,data,reference);
	tag=DFTAG_FID;
    }
    
    /**
     * Implementation of <code>DataObject</code> abstract method.
     *
     * @exception HDFException thrown if there is a problem interpreting the bytes
     */
    public void interpretBytes() throws HDFException {
	byte [] temp;
	ByteArrayInputStream bais=new ByteArrayInputStream(bytes);
	DataInputStream dis=new DataInputStream(bais);
	
	try{
	    temp=new byte[bytes.length];
	    dis.read(temp);
	    label=new String(temp);
	    //System.out.println("FID_"+this.tag+"/"+this.ref+".interpretBytes() : "+label);
	} catch (IOException e) {
	    throw new HDFException ("Problem interpreting FID: "+e.getMessage());
	}
    }
    
    /**
     * Returns the text contained.
     */
    public String getLabel(){
	return label;
    }
        
}
