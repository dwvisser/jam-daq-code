package jam.sort.stream;
import java.io.*;
import java.util.*;
import jam.global.MessageHandler;

/**
 * This class knows how to handle Oak Ridge tape format (with special headers as
 * used by Charles Barton's group).  It extends 
 * EventInputStream, adding methods for reading events and returning them
 * as int arrays which the sorter can handle.
 *
 * @version	0.5 April 98
 * @author 	Dale Visser, Ken Swartz
 * @see         EventInputStream
 * @since       JDK1.1
 */
public class L003InputStream  extends EventInputStream implements L003Parameters {


    int recordByteCounter=0;	    //FIXME
    int tapeByteCounter=0;

    private boolean atHeader;
    
    private EventInputStatus status;
    private int parameter;
    private int eventValue;
    String scalers;    
	        
    /**
     * Constructor for offline sorting.
     */
    public L003InputStream(MessageHandler console) {
		super(console);    
		System.out.println("L003InputStream");	        		    
    }
    
    /** 
     * Constuctor passed the number of parameters to an event.
     *
     * @param eventSize number of parameters per event
     */  
    public L003InputStream(MessageHandler console, int eventSize) {
		super(console, eventSize);    
		System.out.println("L003InputStream eventSize: "+eventSize);	        		    
    }
    
    /**
     * Reads an event from the input stream.
     * Expects the stream position to be the beginning of an event.  
     * It is up to the user to ensure this.
     *
     * @exception   EventException    thrown for errors in the event stream
     */
    public synchronized EventInputStatus readEvent(int[] input) throws  EventException {
    	    
	try {	    
	    while(readParameter()){
		input[parameter]=eventValue;
	    }

	} catch (IOException io){	    
	    status=EventInputStatus.ERROR;	    
	    throw new EventException("Reading Event from IOException "+io.getMessage()+" [L003InputStream]");	    

	}	    
	return status ;	//if event read return ok 
    }
    
    /**
     * Read a event parameter
     */
    private boolean readParameter() throws  EventException, IOException {
		
	boolean parameterSuccess;
	int paramWord;
	
	try {
	    paramWord=readVaxShort();				//read parameter word
	    //check special types parameter	
	    if (paramWord==EVENT_END_MARKER){
		//need another read as marker is 2 shorts
		paramWord=readVaxShort();	
		numberEvents++;
		parameterSuccess=false;
		status=EventInputStatus.EVENT;
	    } else if  (paramWord==BUFFER_END_MARKER){
		parameterSuccess=false;
		status=EventInputStatus.END_BUFFER;
	    } else if (paramWord==RUN_END_MARKER){
		parameterSuccess=false;
		status=EventInputStatus.END_RUN;
	    
	    //get parameter value if not special type
	    } else if (0!=(paramWord & EVENT_PARAMETER_MARKER)){
		parameter=(int)((paramWord & EVENT_PARAMETER_MASK)-1);//parameter number	
    		eventValue=(int)readVaxShort();		//read event word			
		parameterSuccess=true;
		status=EventInputStatus.PARTIAL_EVENT;	    
	    } else {
		parameterSuccess=false;
		status=EventInputStatus.UNKNOWN_WORD;
		throw new EventException("L003InputStream parameter value: "+paramWord+" [L003InputStream]");
	    }
	// we got to the end of a file
	} catch (EOFException eof){
	    System.out.println("EOFException "+eof.getMessage());			
	    parameterSuccess=false;
	    status=EventInputStatus.END_FILE;
	  
	//catch error which hopefully a ascii scaler dump try to read it in	    
	} catch (IOException ioe){
	    System.out.println("IOException "+ioe.getMessage());		
	    parameterSuccess=scalerRead();
	    if (parameterSuccess){
		status=EventInputStatus.END_BUFFER;
	    } else {
		status=EventInputStatus.END_FILE;
	    }		
	    
	}			
	return parameterSuccess;
    }
    /**
     * read in a scaler dump
     * this is a 32000 byte ascii record
     */
    public boolean scalerRead() throws IOException {
	//XXXSystem.out.println("Read scaler dump");
		
	boolean readStatus=true;
    	byte [] scalerDump = new byte [SCALER_BUFFER_SIZE];	
	String scalers=null;
	int scalerByteCounter=0;
	int bytesRead=0;

	    while((scalerByteCounter<SCALER_RECORD_SIZE)&&bytesRead>=0 ){
	    
		if (scalerByteCounter<SCALER_RECORD_SIZE-SCALER_BUFFER_SIZE){
		    bytesRead=dataInput.read(scalerDump);
		} else {
		    bytesRead=dataInput.read(scalerDump, 0, SCALER_RECORD_SIZE-scalerByteCounter);		
		}		   

		if (bytesRead!=SCALER_BUFFER_SIZE){
		    System.out.println("scaler Not a full read, only read in "+bytesRead+" bytes");
		} 
		if (bytesRead>0) {	    
		    tapeByteCounter+=bytesRead;				     	    		
		    scalerByteCounter+=bytesRead;		
		}		    
		//save first read
		if ( scalerByteCounter<SCALER_BUFFER_SIZE+1) {
		    scalers=new String(scalerDump);
		}

	    }
	    
	    System.out.println ("scaler dump total bytes read "+scalerByteCounter);
	    System.out.println("scaler dump = "+scalers);		    	    
	    if (bytesRead<0 ) {
		readStatus=false;
		System.out.println ("End of file in scaler read");
	    }	
	return readStatus;	    	
    }
            	    
    /**
     * Implementation of <code>EventInputStream</code> abstract method.
     * 
     * @exception   EventException    thrown for errors in the event stream
     */    
    public boolean readHeader() throws EventException {
	//XXXSystem.out.println("L003 readHeader ");    
	byte[] headerStart=new byte[32];	
	byte[] date=new byte[16];
	byte[] title=new byte[80];
	byte[] reserved1=new byte[8];
	byte[] reserved2=new byte[92];
	int numSec;
	int numImage;
	int temp;
	int number=0;
	int size=0;
	
        try{

		    System.out.println("    read header start");		    			
		    dataInput.readFully(headerStart);
		    dataInput.readFully(date);
		    dataInput.readFully(title);
	     number=readVaxInt();
		    dataInput.readFully(reserved1);
	     numSec=readVaxInt();
	      temp=readVaxInt();		//header record length
	  numImage=readVaxInt();
	      temp=readVaxInt();		//IMAGE_RECORD_LENGTH
	      size=readVaxInt();
	      temp=readVaxInt();		//DATA_RECORD_LENGTH
		   dataInput.readFully(reserved2);
	    
	    //save reads to header variables
	    headerKey=new String(headerStart);
	    headerRunNumber=number;	    
	    headerTitle=new String(title);	    
	    headerEventSize=size;
	    headerDate=new String(date);
	    
	    loadRunInfo();
	    	    
	    //dump header to sys.out for debugging
	    dumpHeader();
	    
	    return   (headerKey.equals(HEADER_START));
	} catch (IOException ioe) {
	    throw new EventException("Reading event header -"+ioe.getMessage());
        }
    }
    /**
     * dump out the header for debugging
     */
    private void dumpHeader(){
    
	    System.out.println("    header");
	    System.out.println("       Key "+headerKey);
	    System.out.println("       Date: "+headerDate);
	    System.out.println("       Header Run Number: "+headerRunNumber);
	    System.out.println("       Event Size: "+headerEventSize);
	    System.out.println("LOO3 header done");	    
    }    
    /**
     * Implementation of an <code>EventInputStream</code> abstract method.
     */
    public synchronized boolean isEndRun(short dataWord){
	return (dataWord==RUN_END_MARKER);
    }
    
    /** 
     * reads a little endian integer (4 bytes)
     */
    int readVaxInt() throws IOException {
	    
	int ch1=dataInput.read();
	int ch2=dataInput.read();	
	int ch3=dataInput.read();
	int ch4=dataInput.read();
		
	return (ch4<<24)+(ch3<<16)+(ch2<<8)+(ch1<<0); 
    }		
    /** 
     * reads a little endian short (2 bytes)
     * but return a 4 byte integer
     */    
    short readVaxShort() throws IOException {
	    
	int ch1= dataInput.read();
	int ch2=dataInput.read();	

	if ((ch1|ch2)<0){
	    return -1;
	}	    
	return (short)((ch2<<8)+(ch1<<0));			

    }		

    public void setScalerTable(Hashtable table) {
    }
    
}
    
