package jam.sort.stream;
import java.io.*;
import java.util.*;
import jam.global.*;
import jam.data.Scaler;

/**
 * This class knows how to handle the Kmax data format.  It extends
 * <code>EventInputStream</code>, adding methods for reading events and returning them
 * as int arrays which the sorter can handle.
 *
 * @version	0.4 June 2001
 * @author 	Ralph France
 * @see         jam.sort.stream.EventInputStream
 * @see jam.sort.stream.L002InputStream
 * @see jam.sort.stream.UconnInputStream
 * @since       JDK1.3
 */
public class Kmax6InputStream extends EventInputStream {

    //private EventInputStatus status;
    private int parameter;
    int blockFullSize;
    int blockCurrSize;
    int blockNumber;
    int blockNumEvnt;
    int blockEventType;
    private short eventSize;
    private int countEvent=0;
    private int countWord=0;
    private boolean newBlock=true;
    short[] eventsze=new short[32];		//event size array

    /**
     * Default constructor.
     */
    public Kmax6InputStream() {
        super();
    }

    /** Creates the input stream given an event size.
     * @param eventSize number of parameters per event.
     * @param console where messages to user go
     */

    public Kmax6InputStream(MessageHandler console,int eventSize) {
        super(console, eventSize);
    }
    
    /** Creates an instance with access to the Jam console.
     * @param console where messages to the user go
     */
    public Kmax6InputStream(MessageHandler console){
        super(console);
    }

    /** Reads an event from the input stream
     * Expects the stream position to be the beginning of an event.
     * It is up to the user to ensure this.
     * @param input data array
     * @exception EventException thrown for errors in the event stream
     * @return status resulting after read attempt
     */
    public synchronized EventInputStatus readEvent(int[] input) throws  EventException {
        int badEvent;		//header padding

        try {
            //status=EventInputStatus.ERROR;
            //if a new block read in block header
            if (newBlock) {
                if(!readBlockHeader()){
                    return EventInputStatus.END_FILE;
                }
                newBlock=false;
                countEvent=0;
                countWord=0;
                //check if we are done with this block
            } else if(countEvent>blockNumEvnt){
                //are we done with this block
                newBlock=true;
                return EventInputStatus.END_BUFFER;
            }
            if (blockEventType==5) {
                for (parameter=0;parameter<eventsze[4];parameter++){
                    //read parameter word
                    input[parameter]=dataInput.readInt();
                }
                return EventInputStatus.EVENT;
            } else  if (blockEventType <5) {
                for (parameter=0;parameter<eventsze[blockEventType-1];
                parameter++) {
                    badEvent=dataInput.readInt();
                }
                return EventInputStatus.ERROR;
            } else {
                throw new Exception (getClass().getName()+": Block Event Type >5: "+blockEventType);
            }
            // we got to the end of a file or stream
        } catch (EOFException e){
            return EventInputStatus.END_FILE;
            //throw new EventException("Reading event "+e.toString()+" [KmaxInputStream]");
        } catch (IOException ioe) {
            console.errorOutln(ioe.toString());
            return EventInputStatus.ERROR;
        } catch (Exception e) {
            console.errorOutln(e.toString());
            return EventInputStatus.ERROR;
        }
        //return status ;
    }

    /** Reads a block from the input stream
     * Expects the stream to be at the beginning of a block
     * It is up to user to ensure this
     * @exception EventException thrown for errors in event stream
     * @return whether read is successful or not
     */
    public boolean readBlockHeader() throws EventException {
        try {
            blockEventType=dataInput.readInt();
            blockNumEvnt=dataInput.readInt();
            //	    System.out.println("Block fullsize  "+blockFullSize+" currsize "+blockCurrSize+
            //		" number "+blockNumber+ "number event "+blockNumEvnt);
            return true;
        } catch (EOFException eof){
            System.out.println("end of file readBlockHeader");
            return false;
        } catch (IOException ioe) {
            throw new EventException ("Reading Block header,"+ioe.getMessage()+" [KmaxInputStream]");
        }
    }

    /** Read in the header
     * Format of KMaxo data
     * Implemented <code>EventInputStream</code> abstract method.
     * @exception EventException thrown for unrecoverable errors
     * @return whether read is successful or not
     */
    public boolean readHeader() throws EventException {
        byte[] headerStart=new byte[354];	//KMax Header
        byte[] headerEnd=new byte[316];		//header padding
        byte[] junk=new byte[54];
        int eventSize;				//event size, parameters per event

        try{
            dataInput.readFully(headerStart);		//Header to be ignored
            eventsze[0]=dataInput.readShort();			//array of event sizes
            eventsze[1]=dataInput.readShort();			//array of event sizes
            eventsze[2]=dataInput.readShort();			//array of event sizes
            eventsze[3]=dataInput.readShort();			//array of event sizes
            eventsze[4]=dataInput.readShort();			//array of event sizes
            dataInput.readFully(junk);
            eventSize=eventsze[4];				//SRQ event size is wanted
            dataInput.readFully(headerEnd);
            //save reads to header variables
            headerKey=new String("unknown");
            headerRunNumber=0;
            headerTitle=new String("unknown");
            headerEventSize=eventSize;
            headerDate=new String("unknown");
            return true;
        } catch (IOException ioe) {
            throw new EventException("Reading event header from IOException "+ioe.getMessage()+" [KmaxInputStream]");
        }
    }
    
    /** Check for end of run word, may not be valid in the Kmax case, which relies solely
     * on the end-of-file condition
     * @param dataWord most recently read word
     * @return whether end-of-file
     */
    public synchronized boolean isEndRun(short dataWord){
        return /*(dataWord==END_RUN_MARKER)*/ false;//FIXME
    }
}
