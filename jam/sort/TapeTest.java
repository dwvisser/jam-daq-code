 /*
  */
 package jam.sort;
 import java.util.*;
 import java.awt.*; 
 import java.awt.event.*;
 import java.io.*;
 import java.net.*;
 import jam.global.*;
 import jam.sort.stream.*;
 import jam.*;

 /**
  *   Class to test tape deamon to read and write event data for tapes
  *
  * .
  */
 public class TapeTest  {
    
    int BYTE_MASK=0xFF;
	        
    String noRewindDevice;
    String rewindDevice;
    public static int RECORD_SIZE=8192;
    //counters of what we hav writtern to tape
    private int recordByteCounter=0;		//number bytes this record
    private int tapeByteCounter=0;		//number bytes this tape
    private int blockCounter=0;			//number of blocks
    
    TapeDaemon tapeD;
    
    /**
     * Constructor TapeDaemon for online
     */
    public TapeTest() {
    }

    /**
     * main method for testing L003EeventInputs 
     * ORNL formatted with scaler records
     */
    public static void main(String args[]) {
    
	System.out.println("Running Tape test");    
	TapeTest tt=new TapeTest();	
	MessageHandler msgAdapter=new MessageAdaptor();
	
	String tapeDev="/dev/rmt/1";
	EventInputStream eventInput=new L003InputStream(msgAdapter);	
	EventOutputStream eventOut=new L002OutputStream();
	eventInput.setEventSize(8);
	
	boolean status;
	TapeDaemon tapeD=null;	
	byte [] lastByte;
	Vector runList=tt.fakeRunList();
	
	try {
	    //do things SetupSort does.
	    System.out.println("main-   make instance");	
	    tapeD = new TapeDaemon(null, msgAdapter);	
	    tapeD.setDevice(tapeDev);
	    tapeD.setupOff(eventInput, eventOut);
	    
	    //do things SortControl does
	    System.out.println("main-  load files");
	    tapeD.setEventInputList(runList);
	    System.out.println("main-  hasMoreStreams "+tapeD.hasMoreFiles());
	    System.out.println("main-  open list file ");	    	    
	    status=tapeD.openEventInputListFile();
	    System.out.println("main-  open file status "+status);	    
	    

	    if(status){
		// the things sortDaemon would do	    
		System.out.println("main-  testread");		
		tt.testRead(tapeD, eventInput);    
	    }		
	    System.out.println("main-  closeTape");			    
	    tapeD.closeEventInputFile();
	    
	} catch (SortException je) {
	    System.out.println("Error in TapeDaemon JamExptn  "+je.getMessage());
	}

	

    }    
    /**
     * a event read test
     *
     */
    public void testRead(TapeDaemon tapeD, EventInputStream eventInput) {
    
	//event 7 parameters total event size =4x7+4=
	int status;
	int eventNumber;
	byte [] lastByte;
	boolean scalerReadStatus=true;
	int EVENT_SIZE_BYTE=36;
	int EVENT_BUFFER_SIZE=8192;
	int [] event=new int [8] ;
	byte [] inByte=new byte[EVENT_BUFFER_SIZE];
	
	L003InputStream l3eis=(L003InputStream)eventInput;
	
	lastByte=inByte;
	try{	
	    eventNumber=0;
	    System.out.println("read bytes ");
	    status=tapeD.eventInput.read(inByte);		
	    if (status>0)	{	    
		tapeByteCounter+=status;				     	    		
	    }		    
	    
	while ((status>0)&&scalerReadStatus){
	    //catch ioexceptions which could be scalers
	    try {
		//do a read
		status=tapeD.eventInput.read(inByte);
		if (status>0)	{	    
		    tapeByteCounter+=status;				     	    		
		}		    
		    
		    if (eventNumber<20){
			System.out.println ("read "+eventNumber+" bytes "+status);	
			debugDump(inByte);
		    }
	    
		    eventNumber++;			
		    lastByte=inByte;	    
	
	    } catch (IOException ioe) {
		    if (status>0)	{	    
			tapeByteCounter+=status;				     	    		
		    }		    
	    
		    System.out.println("IOExptn  "+ioe.getMessage());
		    System.out.println("Total events read "+eventNumber+" last read "+status+" tapeByteCounter "+tapeByteCounter );	
//		    lastByte=tapeD.lastByte;

		    System.out.println ("last event "+(lastByte[status-8]&BYTE_MASK)+" "+(lastByte[status-7]&BYTE_MASK)+" "+(lastByte[status-6]&BYTE_MASK)+" "+(lastByte[status-5]&BYTE_MASK)+
	    		    			  " "+(lastByte[status-4]&BYTE_MASK)+" "+(lastByte[status-3]&BYTE_MASK)+" "+(lastByte[status-2]&BYTE_MASK)+" "+(lastByte[status-1]&BYTE_MASK));	    	
		    System.out.println(" read in scaler 3200K ascii record");					     
		    scalerReadStatus=l3eis.scalerRead();
		    eventNumber=0;
	    }
	}		
	    System.out.println("End of file read?");	    
	    System.out.println("Total events read "+eventNumber+" last read "+status+" tapeByteCounter "+tapeByteCounter );		    
	    debugDump(inByte);	    
	    System.out.println ("end event "+(lastByte[120]&BYTE_MASK)+" "+(lastByte[121]&BYTE_MASK)+" "+(lastByte[122]&BYTE_MASK)+" "+(lastByte[123]&BYTE_MASK)+
	    		    		     " "+(lastByte[124]&BYTE_MASK)+" "+(lastByte[125]&BYTE_MASK)+" "+(lastByte[126]&BYTE_MASK)+" "+(lastByte[127]&BYTE_MASK));	    	
	    
	    System.out.println("Total bytes read "+tapeByteCounter );
	    
	} catch (IOException ioe) {
	    System.out.println("Error in TapeDaemon IOExptn  "+ioe.getMessage());
	}	    
/*
	} catch (EventException ee) {
	    System.out.println("Error in TapeDaemon EventExptn  "+ee.getMessage());


	} catch (SortException je) {
	    System.out.println("Error in TapeDaemon JamExptn  "+je.getMessage());
	}

*/
		    	
    }
    private void debugDump(byte [] inByte){
	System.out.println ("event "+(inByte[0]&BYTE_MASK)+" "+(inByte[1]&BYTE_MASK)+" "+(inByte[2]&BYTE_MASK)+" "+(inByte[3]&BYTE_MASK)+
				" "+(inByte[4]&BYTE_MASK)+" "+(inByte[5]&BYTE_MASK)+" "+(inByte[6]&BYTE_MASK)+" "+(inByte[7]&BYTE_MASK)+	    
				" "+(inByte[8]&BYTE_MASK)+" "+(inByte[9]&BYTE_MASK)+" "+(inByte[10]&BYTE_MASK)+" "+(inByte[11]&BYTE_MASK)+	    
				" "+(inByte[12]&BYTE_MASK)+" "+(inByte[13]&BYTE_MASK)+" "+(inByte[14]&BYTE_MASK)+" "+(inByte[15]&BYTE_MASK)+	    				     
				" "+(inByte[16]&BYTE_MASK)+" "+(inByte[17]&BYTE_MASK)+" "+(inByte[18]&BYTE_MASK)+" "+(inByte[19]&BYTE_MASK)+	   
				" "+(inByte[20]&BYTE_MASK)+" "+(inByte[21]&BYTE_MASK)+" "+(inByte[22]&BYTE_MASK)+" "+(inByte[23]&BYTE_MASK)+	   
				" "+(inByte[24]&BYTE_MASK)+" "+(inByte[25]&BYTE_MASK)+" "+(inByte[26]&BYTE_MASK)+" "+(inByte[27]&BYTE_MASK)+	   
				" "+(inByte[28]&BYTE_MASK)+" "+(inByte[29]&BYTE_MASK)+" "+(inByte[30]&BYTE_MASK)+" "+(inByte[31]&BYTE_MASK)+	    				     				     
				 " "+(inByte[32]&BYTE_MASK)+" "+(inByte[33]&BYTE_MASK)+" "+(inByte[34]&BYTE_MASK)+" "+(inByte[35]&BYTE_MASK));	    				     				     				     				
    }
    
    private int [] byteMask(byte []b){
	int [] ib=new int [b.length]; 
	
	for (int i=0;i<b.length;i++){
	    ib[i]= b[i]&BYTE_MASK;
	}	    
	return ib;
    }	
    /**
     *
     */
    Vector fakeRunList(){
    
	//fake run number list
	Vector vec=new Vector();
	System.out.println("size of vector begin "+vec.size());	
	System.out.println("is empty "+vec.isEmpty());
	
	vec.addElement(new Integer(2));		
	vec.addElement(new Integer(4));
	vec.addElement(new Integer(5));	
	vec.addElement(new Integer(6));		
	vec.addElement(new Integer(7));			
	vec.addElement(new Integer(8));				
	System.out.println("size of vector add 6 "+vec.size());
	vec.removeElementAt(1);					
	System.out.println("size of vector sub 1 "+vec.size());	
	if (((Integer)vec.elementAt(1)).intValue()==5){
	    System.out.println("match");
	    System.out.println("vector element at 2 is  "+vec.elementAt(2));
	}
	return vec;
    }
    
    /**
     * Execute unix command
     * Goes back num files and leaves fis open at beginning of record.
     */
    private String backSpaceFile(int num){
        String cmd;
	Process exec;
	int returnValue=0;
	String mesg="";
    
	cmd="mt -f "+noRewindDevice+" nbsf "+num;
	System.out.println(cmd);
	try{
	    exec = Runtime.getRuntime().exec(cmd);
	    exec.waitFor();
	    returnValue=exec.exitValue();
	    System.out.println("Exit Value: "+returnValue);
	} catch (Exception e) {
	    System.out.println("Exception: "+e);
	}
	if (returnValue==0){
	    mesg="OK";
	} else if (returnValue==1){
	    mesg="Unable to open drive.";
	} else if (returnValue==2){
	    mesg="Failure.";
	}
	return mesg;
    }

    /**
     * Execute unix command  
     * go past num eof's and leave tape at beginning of file (neccessary to re-open fis)
     *
     */
    private String forwardSpaceFile(int num){
        String cmd;
	Process exec;
	int returnValue=0;
	String mesg="";
    
	cmd="mt -f "+noRewindDevice+" fsf "+num;
	System.out.println(cmd);
	try{
	    exec = Runtime.getRuntime().exec(cmd);
	    exec.waitFor();
	    returnValue=exec.exitValue();
	    System.out.println("Exit Value: "+returnValue);
	} catch (Exception e) {
	    System.out.println("Exception: "+e);
	}
	if (returnValue==0){
	    mesg="OK";
	} else if (returnValue==1){
	    mesg="Unable to open tape drive.";
	} else if (returnValue==2){
	    mesg="Failure.";
	}
	return mesg;
    }
    /** 
     *
     * checks if we ar at End of Media   
     * If no more characters can be read, returns false
     * If there is data, resets pointer to start of record

     */
    private boolean atEOM() throws SortException{
    
	int numByteRead;
	byte endmarker[]=new byte[2];
	boolean EOF=true;
	
	
	FileInputStream fis=tapeD.fis;
	
	try{    
	    //mark where we are (up to 3 bytes)
//	    tapeD.openFile();
	    System.out.println("file mark supported "+fis.markSupported());	
	    
	    fis.mark(3);
	
	    numByteRead=fis.read(endmarker);
	    EOF=(numByteRead==-1);
	    System.out.println("atEOM number byes="+numByteRead+" bytes="+endmarker);	
	    if (!EOF){
		fis.reset();	
		//System.out.println("reset to beginning of file");
	    }
	} catch (IOException ioe) {
	    System.err.println("Error atEOM: "+ioe);	
	    throw new SortException(" checking for End-of-medium [tapeDaemon]");	    	    

	}

	return EOF;    
    }
    
}