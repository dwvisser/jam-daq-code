package jam.sort.stream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import jam.util.StringUtilities;
import jam.global.RunInfo;

/**
 * <b>Not</b> yet fully implemented, but will write out
 * data in a format that <code>YaleCAEN_InputStream</code>
 * can understand.
 * 
 * @author <a href=mailto:dale@visser.name>Dale Visser</a>
 * @see YaleCAEN_InputStream
 */
public abstract class YaleCAEN_OutputStream extends EventOutputStream implements L002Parameters{

    SimpleDateFormat formatter;

    /**
     * Default constructor.
     */
    public YaleCAEN_OutputStream(){
        super();
        formatter = new SimpleDateFormat("MM/dd/yy HH:mm  ");
        formatter.setTimeZone(TimeZone.getDefault());
    }

    /**
     * Creates the output stream with the given event size.
     *
     * @param eventSize the number of parameters per event
     */
    public YaleCAEN_OutputStream(int eventSize){
        super(eventSize);
        formatter = new SimpleDateFormat("MM/dd/yy HH:mm  ");
        formatter.setTimeZone(TimeZone.getDefault());
    }

	/**
	 * @see jam.sort.stream.EventOutputStream#writeEvent(short[])
	 */
	/*public void writeEvent(short[] input) throws EventException {
		System.err.println(getClass().getName()+": writeEvent() not yet implemented");
	}*/

	/**
	 * @see jam.sort.stream.EventOutputStream#writeEvent(int[])
	 */
	/*public void writeEvent(int[] input) throws EventException {
		System.err.println(getClass().getName()+": writeEvent() not yet implemented");
	}*/

	/**
	 * @see jam.sort.stream.EventOutputStream#writeHeader()
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
	 * @see jam.sort.stream.EventOutputStream#writeEndRun()
	 */
	/*public void writeEndRun() throws EventException {
		System.err.println(getClass().getName()+": writeEndRun() not yet implemented");
	}*/

	/**
	 * @see jam.sort.stream.EventOutputStream#isEndRun(short)
	 */
	public boolean isEndRun(short event) {
		final short endRun = (short)(YaleCAEN_InputStream.END_OF_BUFFER & 0xffff);
		return (endRun==event);
	}

}
