package jam.sort.stream;

import injection.GuiceInjector;
import jam.global.RunInfo;
import jam.util.StringUtilities;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import static jam.sort.stream.L002Parameters.*;

/**
 * Abstract superclass of all event output streams that write ORNL L002 headers.
 * @author Dale Visser
 */
abstract class AbstractL002HeaderWriter extends AbstractEventOutputStream {// NOPMD

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat(
            "MM/dd/yy HH:mm  ", Locale.getDefault());

    static {
        FORMATTER.setTimeZone(TimeZone.getDefault());
    }

    /**
     * Default constructor.
     */
    protected AbstractL002HeaderWriter() {
        super();
    }

    /**
     * @param eventSize
     *            number of parameters per event
     */
    protected AbstractL002HeaderWriter(final int eventSize) {
        super(eventSize);
    }

    /**
     * Writes the header block.
     * @exception EventException
     *                thrown for errors in the event stream
     */
    @Override
    public void writeHeader() throws EventException {
        String dateString;
        final RunInfo runInfo = RunInfo.getInstance();
        synchronized (FORMATTER) {
            dateString = FORMATTER.format(runInfo.runStartTime); // date
        }
        final String title = runInfo.runTitle; // title
        final int number = runInfo.runNumber; // header number
        final byte[] reserved1 = new byte[8]; // reserved 1
        final int numSecHead = 0; // number of secondary headers
        final int recLen = 0; // record length
        final int blckImgRec = 0; // block line image rec
        final int paramsPerEvent = runInfo.runEventSize; // parameters per
        // event
        final int dataRecLen = runInfo.runRecordLength; // data record length
        final byte[] reserved2 = new byte[92]; // reserved 2
        try {
            dataOutput.writeBytes(HEADER_START); // header
            final StringUtilities stringUtilities = GuiceInjector
                    .getObjectInstance(StringUtilities.class);
            dataOutput.writeBytes(stringUtilities.makeLength(dateString, 16)); // date
            // //date
            dataOutput
                    .writeBytes(stringUtilities.makeLength(title, TITLE_MAX)); // title
            dataOutput.writeInt(number); // header number
            dataOutput.write(reserved1, 0, reserved1.length); // reserved
            // space
            dataOutput.writeInt(numSecHead); // number secondary headers
            dataOutput.writeInt(recLen); // record length
            dataOutput.writeInt(blckImgRec); // block line image rec
            dataOutput.writeInt(IMAGE_LENGTH); // record length
            dataOutput.writeInt(paramsPerEvent); // parameters / event
            dataOutput.writeInt(dataRecLen); // data record length
            dataOutput.write(reserved2, 0, reserved2.length); // reserved 2
            dataOutput.flush();
        } catch (IOException io) {
            throw new EventException("Problem writing header.", io);
        }
    }

}
