package jam.sort.stream;
import jam.global.RunInfo;
import jam.util.StringUtilities;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * This class knows how to handle Oak Ridge tape format.  It extends
 * EventOutputStream, adding methods for reading events and returning them
 * as int arrays which the sorter can handle.
 *
 * @version	0.5 April 98
 * @author 	Dale Visser, Ken Swartz
 * @see         EventOutputStream
 * @since       JDK1.1
 */
public final class YaleOutputStream extends EventOutputStream implements L002Parameters {
    private final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy HH:mm  ");
    {
        formatter.setTimeZone(TimeZone.getDefault());        
    }

    /**
     * Default constructor.
     */
    public YaleOutputStream(){
        super();
    }

    /**
     * @see EventOutputStream#EventOutputStream(int)
     */
    public YaleOutputStream(int eventSize){
        super(eventSize);
    }
    
    /**
     * Writes the header block.
     *
     * @exception   EventException    thrown for errors in the event stream
     */
    public void writeHeader() throws EventException {
        String dateString = formatter.format(RunInfo.runStartTime);	    //date
        String title=RunInfo.runTitle;					    //title
        int number=RunInfo.runNumber;					    //header number
        byte [] reserved1=new byte [8];					    //reserved 1
        int numSecHead=0;						    //number of secondary headers
        int recLen=0;							    //record length
        int blckImgRec=0;						    //block line image rec
        int recLen2=IMAGE_RECORD_LENGTH;				    //record length
        int eventSize=RunInfo.runEventSize;				    //parameters per event
        int dataRecLen=RunInfo.runRecordLength;				    //data record length
        byte [] reserved2=new byte [92];				    //reserved 2
        try {
			final StringUtilities su=StringUtilities.instance();
            dataOutput.writeBytes(HEADER_START);				    //header
            dataOutput.writeBytes(su.makeLength(dateString,16));	    //date				    //date
            dataOutput.writeBytes(su.makeLength(title, TITLE_MAX));    //title
            dataOutput.writeInt(number);					    //header number
            dataOutput.write(reserved1, 0, reserved1.length);			    // reserved space
            dataOutput.writeInt(numSecHead);					    //number secondary headers
            dataOutput.writeInt(recLen);					    //record length
            dataOutput.writeInt(blckImgRec);					    //block line image rec
            dataOutput.writeInt(recLen2);					    //record length
            dataOutput.writeInt(eventSize);					    //parameters / event
            dataOutput.writeInt(dataRecLen);					    //data record length
            dataOutput.write(reserved2, 0, reserved2.length);			    //reserved 2
            dataOutput.flush();
        } catch (IOException io){
            throw new EventException("Writing header IOException "+io.getMessage()+" [L002OutputStream]");
        }
    }

    /**
     * Implemented <code>EventOutputStream</code> abstract method.
     *
     * @exception EventException thrown for unrecoverable errors
     */
    public void writeEvent(int[] input) throws EventException {
        try{
            for (short i=0;i<eventSize;i++){
                if (input[i]!=0) writeParameter(i,(short)input[i]);
            }
            dataOutput.writeShort(EVENT_END_MARKER);
        } catch (IOException ie) {
            throw new EventException("Can't write event: "+ie.toString());
        }
    }

    /**
     * Writes out a event in the L002 format
     * Implemented <code>EventOutputStream</code> abstract method.
     *
     * @exception EventException thrown for unrecoverable errors
     */
    public void writeEvent(short[] input) throws EventException {
        try{
            for (short i=0;i<eventSize;i++){
                if (input[i]!=0) writeParameter(i, input[i]);
            }
            dataOutput.writeShort(EVENT_END_MARKER);
        } catch (IOException ie) {
            throw new EventException("Can't write event: "+ie.toString());
        }
    }

    /* non-javadoc:
     * NOT CURRENTLY IMPLEMENTED. Writes a single parameter.
     *  would be called by writeEvent
     * @exception   EventException    thrown for errors in the event stream
     */
    private void writeParameter(short param, short value)  throws EventException {
        try{
            if (isValidParameterNumber(param)){
                dataOutput.writeShort(parameterMarker(param));
                dataOutput.writeShort(value);
            } else {
                throw new EventException("Parameter number out of range: "+param);
            }
        } catch (IOException ioe) {
            throw new EventException(ioe.toString());
        }
    }


    /**
     * Check for end of run word
     */
    public boolean isEndRun(short dataWord){
        return (dataWord==RUN_END_MARKER);
    }

    /**
     *Write the character that signifies the end of the run data.
     */
    public void writeEndRun() throws EventException {
        try {
            dataOutput.writeShort(RUN_END_MARKER);
        } catch (IOException ioe) {
            throw new EventException(ioe.toString());
        }
    }

    /* non-javadoc:
     * Checks whether a valid parameter number (should be 1 to 512 according to ORNL documentation).
     */
    private boolean isValidParameterNumber(short number){
        return ((number>=0 )&& (number < 2048));
    }

    /* non-javadoc:
     * Converts a short to a valid parameter marker for the stream.
     */
    private short parameterMarker(short number) {
        return (short)((EVENT_PARAMETER_MARKER | number)&0xFFFF);
    }
}
