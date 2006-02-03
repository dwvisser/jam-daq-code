package jam.sort.stream;

import jam.global.RunInfo;
import jam.util.StringUtilities;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Abstract superclass of all event output streams that write ORNL L002 headers.
 * @author dvk
 *
 */
public abstract class AbstractL002HeaderWriter extends AbstractEventOutputStream implements L002Parameters {
    
	/**
	 * Default constructor.
	 *
	 */
	protected AbstractL002HeaderWriter(){
		super();
	}

	/**
	 * @param eventSize number of parmas per event
	 */
	protected AbstractL002HeaderWriter(int eventSize){
		super(eventSize);
	}
	
    private static final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy HH:mm  ", Locale.getDefault());
    static {
        formatter.setTimeZone(TimeZone.getDefault());        
    }

    /**
     * Writes the header block.
     *
     * @exception   EventException    thrown for errors in the event stream
     */
    public void writeHeader() throws EventException {
		final StringUtilities stringUtilities=StringUtilities.getInstance();
        final String dateString = formatter.format(RunInfo.runStartTime);	    //date
        final String title=RunInfo.runTitle;					    //title
        final int number=RunInfo.runNumber;					    //header number
        final byte [] reserved1=new byte [8];					    //reserved 1
        final int numSecHead=0;						    //number of secondary headers
        final int recLen=0;							    //record length
        final int blckImgRec=0;						    //block line image rec
        final int paramsPerEvent=RunInfo.runEventSize;				    //parameters per event
        final int dataRecLen=RunInfo.runRecordLength;				    //data record length
        final byte [] reserved2=new byte [92];				    //reserved 2
        try {
            dataOutput.writeBytes(HEADER_START);				    //header
            dataOutput.writeBytes(stringUtilities.makeLength(dateString,16));	    //date				    //date
            dataOutput.writeBytes(stringUtilities.makeLength(title, TITLE_MAX));    //title
            dataOutput.writeInt(number);					    //header number
            dataOutput.write(reserved1, 0, reserved1.length);			    // reserved space
            dataOutput.writeInt(numSecHead);					    //number secondary headers
            dataOutput.writeInt(recLen);					    //record length
            dataOutput.writeInt(blckImgRec);					    //block line image rec
            dataOutput.writeInt(IMAGE_LENGTH);					    //record length
            dataOutput.writeInt(paramsPerEvent);					    //parameters / event
            dataOutput.writeInt(dataRecLen);					    //data record length
            dataOutput.write(reserved2, 0, reserved2.length);			    //reserved 2
            dataOutput.flush();
        } catch (IOException io){
            throw new EventException("Writing header IOException "+io.getMessage()+" [L002OutputStream]");

        }
    }

}
