package jam.sort.stream;
import jam.global.MessageHandler;

import java.io.IOException;

/**
 * This class knows how to handle Xsys event data format .  It extends 
 * EventInputStream, adding methods for reading events and returning them
 * as int arrays which the sorter can handle.
 *
 * @version	0.5 April 98
 * @author 	Ken Swartz
 * @see         EventInputStream
 * @since       JDK1.1
 */
public class XsysInputStream extends EventInputStream {

	static private final int END_REC = 0x0a;

	int bufferMarker;
	int bufferNumber;
	
	public XsysInputStream(){
		super();
	}

	/**
	 * Constructor for input stream
	 */
	public XsysInputStream(MessageHandler console) {
		super(console);
		bufferNumber = 0;
	}

	public XsysInputStream(MessageHandler console, int eventSize) {
		super(console, eventSize);
	}

	/**
	 * Reads an event from the input stream
	 * Expects the stream position to be the beginning of an event.  
	 * It is up to the user to ensure this.
	 *
	 * @exception   EventException   thrown when there is a problem with the event stream
	 */
	public synchronized EventInputStatus readEvent(int[] input)
		throws EventException {
		EventInputStatus status = EventInputStatus.ERROR;
		try {
			if (bufferCount < bufferSize) {
				for (int i = 0; i < eventSize; i++) {
					input[i] = readVaxShort();
					bufferCount++;
					if (input[i] < 0) {
						status = EventInputStatus.END_FILE;

						break; //FIXME
					} else {
						status = EventInputStatus.EVENT;
					}
				}
			} else {

				if ((bufferMarker = dataInput.read()) != END_REC) {
					throw new EventException("Incorrect end of record marker");
				}
				status = EventInputStatus.END_BUFFER;
				bufferCount = 0;
			}
		} catch (IOException io) {
			status = EventInputStatus.ERROR;
			throw new EventException(
				"Reading Event from IOException "
					+ io.getMessage()
					+ " [XsysInputStream]");

		}
		return status;
	}
	/**
	 * Read a event stream header.
	 *
	 * @exception   EventException   thrown when there is a problem with the event stream
	 */
	public boolean readHeader() throws EventException {
		headerRunNumber = 0;
		headerTitle = "No Title for Xsys";
		headerDate = "No Date";
		loadRunInfo();
		return true;
	}

	/**
	 * Is the word a end of run word
	 * Xsys event stream has no end of event marker 
	 */
	public boolean isEndRun(short dataWord) {
		return false;
	}

	/** 
	 * reads a little endian short, 2 bytes
	 */
	private int readVaxShort() throws IOException {

		int ch1 = dataInput.read();
		int ch2 = dataInput.read();
		if ((ch1 | ch2) < 0) {
			return -1;
		}
		return (ch2 << 8) + (ch1 << 0);

	}

//	public void setScalerTable(Hashtable table) {
//	}

}
