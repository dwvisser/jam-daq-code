package jam.sort.stream;
import java.io.*;
import jam.global.*;

/**
 * This class knows how to handle Oak Ridge tape format.  It extends
 * <code>EventInputStream</code>, adding methods for reading events and returning them
 * as int arrays which the sorter can handle.
 *
 * @version	0.5 April 98
 * @author 	Dale Visser, Ken Swartz
 * @see         EventInputStream
 * @since       JDK1.1
 */
public class YaleInputStream extends EventInputStream implements L002Parameters {

    private EventInputStatus status;
    private int parameter;

    //make sure to issue a setConsole() after using this constructor
    //It is here to satisfy the requirements of Class.newInstance()
    public YaleInputStream(){
        super();
    }

    /**
     * Default constructor.
     */
    public YaleInputStream(MessageHandler console) {
        super(console);
    }

    /**
     * Creates the input stream given an event size.
     *
     * @param eventSize number of parameters per event.
     */
    public YaleInputStream(MessageHandler console,int eventSize) {
        super(console, eventSize);
    }

    /**
     * Reads an event from the input stream
     * Expects the stream position to be the beginning of an event.
     * It is up to the user to ensure this.
     *
     * @exception   EventException    thrown for errors in the event stream
     */
    public synchronized EventInputStatus readEvent(int[] input) throws  EventException {
        try {
            while(isParameter(dataInput.readShort())){//could be event or scaler parameter
                if (status == EventInputStatus.PARTIAL_EVENT) {
                    if (parameter >= eventSize) {//skip, since array index would be too great for event array
                        dataInput.readShort();
                    } else {//read into array
                        input[parameter]=(int)dataInput.readShort();	//read event word
                    }
                } else if (status == EventInputStatus.SCALER_VALUE) {
                    dataInput.readInt();//throw away scaler value
                }
            }
        } catch (EOFException eofe) {// we got to the end of a file or stream
            status=EventInputStatus.END_FILE;
            console.warningOutln(getClass().getName()+
            ".readEvent(): End of File reached...file may be corrupted, or run not ended properly.");
        } catch (Exception e){
            status=EventInputStatus.UNKNOWN_WORD;
            throw new EventException(getClass().getName()+".readEvent() parameter = "+parameter+" Exception: "+e.toString());
        }
        return status ;
    }

    /**
     * Read an event parameter.
     */
    private boolean isParameter(short paramWord) throws IOException {
        boolean parameterSuccess;
        //check special types parameter
        //System.err.println(getClass().getName()+".isParameter("+paramWord+")");
        if (paramWord==EVENT_END_MARKER){
            parameterSuccess=false;
            status=EventInputStatus.EVENT;
        } else if  (paramWord==BUFFER_END_MARKER){
            parameterSuccess=false;
            status=EventInputStatus.END_BUFFER;
        } else if (paramWord==RUN_END_MARKER){
            parameterSuccess=false;
            status=EventInputStatus.END_RUN;
            //get parameter value if not special type
        } else if ((paramWord & EVENT_PARAMETER_MARKER) != 0) {
            int paramNumber = paramWord & EVENT_PARAMETER_MASK;
            if (paramNumber < 2048) {
                parameter=(int)paramNumber;//parameter index used in array
                parameterSuccess=true;
                status=EventInputStatus.PARTIAL_EVENT;
            } else {// 2048-4095 assumed
                //dataInput.readShort());//skip scaler value
                parameterSuccess=true;
                status = EventInputStatus.SCALER_VALUE;
            }
        } else {//unknown word
            parameter=paramWord;
            parameterSuccess=false;
            status=EventInputStatus.UNKNOWN_WORD;
        }
        //System.err.println(getClass().getName()+".isParameter(): status = "+status);
        return parameterSuccess;
    }

    /**
     * Read in the header
     * Format of ORNL LOO2 data
     * Implemented <code>EventInputStream</code> abstract method.
     *
     * @exception EventException thrown for unrecoverable errors
     */
    public boolean readHeader() throws EventException {
        byte[] headerStart=new byte[32];	//header key
        byte[] date=new byte[16];		//date mo/da/yr hr:mn
        byte[] title=new byte[80];		//title
        int number;				//header number
        byte[] reserved1=new byte[8];		//reserved set to 0
        int numSecHead;				//number of secondary header records
        int recordLen;				//record length
        int blckLnImgRec;			//Block line image records
        int recordLen2;				//record length
        int eventSize;				//event size, parameters per event
        int dataRecLen;				//data record length
        byte[] reserved2=new byte[92];		//reserved set to 0
        byte[] secHead=new byte[256];

        try {
            dataInput.readFully(headerStart);		//key
            dataInput.readFully(date);			//date
            dataInput.readFully(title);			//title
            number=dataInput.readInt();
            dataInput.readFully(reserved1);
            numSecHead=dataInput.readInt();
            recordLen=dataInput.readInt();			//header record length
            blckLnImgRec=dataInput.readInt();
            recordLen2=dataInput.readInt();			//IMAGE_RECORD_LENGTH
            eventSize=dataInput.readInt();
            dataRecLen=dataInput.readInt();			//DATA_RECORD_LENGTH
            dataInput.readFully(reserved2);
            //save reads to header variables
            headerKey=new String(headerStart);
            headerRunNumber=number;
            headerTitle=new String(title);
            headerEventSize=eventSize;
            headerDate=new String(date);
            loadRunInfo();
            //read secondary headers
            for (int i=0; i<numSecHead; i++) {
                dataInput.readFully(secHead);
            }
            return (headerKey.equals(HEADER_START));
        } catch (IOException ioe) {
            throw new EventException(getClass().getName()+".readHeader(): IOException "+ioe.getMessage());
        }
    }

    /**
     * Check for end of run word
     */
    public synchronized boolean isEndRun(short dataWord){
        return (dataWord==RUN_END_MARKER);
    }

    /*protected void setScalerValue(short parameterNumber, int value){
    Scaler s=(Scaler)scalerTable.get(new Short(parameterNumber));
    if (s != null) {
    //System.err.println("Scaler: "+parameterNumber+" set to: "+value);
    //NOT sure if we should update scaler values this way, though it works.
    //s.setValue(value);
    } else {
    console.warningOutln("Invalid scaler parameter number: "+parameterNumber);
    }
    }*/
}