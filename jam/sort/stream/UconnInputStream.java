package jam.sort.stream;
import jam.data.Scaler;
import jam.global.MessageHandler;

import java.io.EOFException;
import java.io.IOException;

/**
 * This class knows how to handle Uconn Be7.  It extends 
 * EventInputStream, adding methods for reading events and returning them
 * as int arrays which the sorter can handle.
 *
 * @version	0.5 April 98
 * @author 	Ken Swartz Jim McDonald
 * @see         EventInputStream
 * @since       JDK1.1
 */
public final class UconnInputStream extends EventInputStream {

	static private final int HEAD_SIZE = 5; //size of header in 2 byte words

	static final private int VSN_TDC = 0x4;

	static final private int VSN_MARKER = 0x8000;
	static final private int VSN_MASK = 0xFF;

	static final private int ADC_DATA_MASK = 0xFFF;
	static final private int ADC_CHAN_SHFT = 12;
	static final private int ADC_CHAN_MASK = 0x07;
	static final private int ADC_OFFSET = 8; //how much to offset data for each vsn

	static final private int TDC_DATA_MASK = 0x3FF;
	static final private int TDC_CHAN_SHFT = 10;

	static final private int TDC_CHAN_MASK = 0x1F;
	static final private int TDC_OFFSET = 32; //how much to offset data for each vsn

	static final private int SCALER_MASK = 0x00ffffff;

	static final private int NUMBER_SCALERS = 12;

	int blockFullSize;
	int blockCurrSize;
	int blockNumber;
	int blockNumEvnt;
	int[] scalerValues = new int[NUMBER_SCALERS];

	private int eventId;
	private short eventSize;
	private short eventState;
	private short eventNumWord;

	private int countEvent = 0;

	private boolean newBlock = true;

	/**
	 * @see EventInputStream#EventInputStream(MessageHandler)
	 */
	public UconnInputStream(MessageHandler console) {
		super(console);
	}

	/**
	 * @see EventInputStream#EventInputStream(MessageHandler, int)
	 */
	public UconnInputStream(MessageHandler console, int eventSize) {
		super(console, eventSize);
	}

	/**
	 * Reads an event from the input stream
	 * Expects the stream position to be the beginning of an event.  
	 * It is up to the user to ensure this.
	 *
	 * @exception   EventException    thrown for errors in the event stream
	 */
	public synchronized EventInputStatus readEvent(int[] input)
		throws EventException {
		EventInputStatus status;
		long numSkip;
		
		try {
			status = EventInputStatus.ERROR;
			//if a new block read in block header
			if (newBlock) {
				if (!readBlockHeader()) {
					return EventInputStatus.END_FILE;
				}
				newBlock = false;
				countEvent = 0;
				//check if we are done with this buffer 		
			} else if (countEvent == blockNumEvnt) {
				//are we done with this block
				numSkip = blockFullSize - blockCurrSize;
				dataInput.skip(numSkip);
				newBlock = true;
				return EventInputStatus.END_BUFFER;
			}
			//read in the event header
			readEventHeader();
			unpackData(input);
			//flush out rest of event
			numSkip = eventSize - 2 * HEAD_SIZE - 2 * eventNumWord;
			dataInput.skip(numSkip);
			//event state
			input[64] = eventState;
			countEvent++;
			status = EventInputStatus.EVENT;
			// we got to the end of a file or stream	    
		} catch (EOFException eof) {

			status = EventInputStatus.END_FILE;
			throw new EventException(
				"Reading event EOFException "
					+ eof.getMessage()
					+ " [UconnInputStream]");
		} catch (IOException io) {
			status = EventInputStatus.END_FILE;
			throw new EventException(
				"Reading event IOException "
					+ io.getMessage()
					+ " [ConnInputStream]");
		}
		return status;
	}
	
	/**
	 * Reads a block from the input stream
	 * Expects the stream to be at the beginning of a block
	 * It is up to user to ensure this
	 *
	 * @return <code>true</code> if successful
	 * @exception EventException thrown for errors in event stream
	 */
	private boolean readBlockHeader() throws EventException {
		try {

			blockFullSize = dataInput.readInt();
			blockCurrSize = dataInput.readInt();
			blockNumber = dataInput.readInt();
			blockNumEvnt = dataInput.readInt();
			System.out.println(
				"Block fullsize  "
					+ blockFullSize
					+ " currsize "
					+ blockCurrSize
					+ " number "
					+ blockNumber
					+ "number event "
					+ blockNumEvnt);
			/* read in scalers */
			for (int i = 0; i < NUMBER_SCALERS; i++) {
				scalerValues[i] = dataInput.readInt() & SCALER_MASK;
			}
			Scaler.update(scalerValues);
			return true;

		} catch (EOFException eof) {
			System.out.println("end of file readBlockHeader");
			return false;

		} catch (IOException ioe) {
			throw new EventException(
				"Reading Block header,"
					+ ioe.getMessage()
					+ " [UconnInputStream]");
		}
	}

	/* non-javadoc:
	 * read a event header
	 */
	private void readEventHeader() throws EventException {
		try {
			eventId = dataInput.readInt();
			eventSize = dataInput.readShort();
			eventState = dataInput.readShort();
			eventNumWord = dataInput.readShort();
			System.out.println(
				"Event id "
					+ eventId
					+ " size "
					+ eventSize
					+ " state "
					+ eventState
					+ " numWord "
					+ eventNumWord);

		} catch (IOException ioe) {
			throw new EventException(
				"Reading Event header,"
					+ ioe.getMessage()
					+ " [UconnnInputStream]");
		}
	}
	
	/*non-javadoc:
	 * we are at an event so unpack it
	 */
	private void unpackData(int[] input) throws IOException, EventException {

		short dataWord;
		int vsn = 0;
		int chan;
		int data;

		//while there are words left in the event
		for (int i = 0; i < eventNumWord; i++) {

			//read word
			dataWord = dataInput.readShort();
			//XXXSystem.out.print(" dataWord  "+dataWord);			    			

			//if new event check we start with a vsn
			if (i == 0) {
				if ((dataWord & VSN_MARKER) == 0) {
					//XXXSystem.out.println(" Error event not started with vsn");			    		    		
					throw new EventException(" Event not started with vsn [UconnInputStream]");
				}
			}

			//we have vsn		
			if ((dataWord & VSN_MARKER) != 0) {
				vsn = dataWord & VSN_MASK;
				System.out.println(" vsn  " + vsn);

				//we have a data word		    
			} else {

				//if vsn of adc 1
				if (vsn < VSN_TDC) {
					chan = (dataWord >> ADC_CHAN_SHFT) & ADC_CHAN_MASK;
					data = dataWord & ADC_DATA_MASK;
					//offset data by ADC_OFFSET
					input[vsn * ADC_OFFSET + chan] = data;
					//XXXSystem.out.println(" tdc channel  "+chan+" data "+data);			    					    		    		    		    					
					// if tdc     		     
				} else {
					chan = (dataWord >> TDC_CHAN_SHFT) & TDC_CHAN_MASK;
					data = dataWord & TDC_DATA_MASK;
					//XXXSystem.out.println(" tdc channel  "+chan+" data "+data);			    					    		    
					input[TDC_OFFSET + chan] = data;
				}
				System.out.println("ch " + chan + " data " + data);

				//end data sort     
			}
			//end for				     
		}

	}
	/**
	 * Read an event stream header.
	 *
	 * @exception   EventException    thrown for errors in the event stream
	 */
	public boolean readHeader() throws EventException {
		return true;
	}

	/**
	 *Is the word a end-of-run word
	 * 
	 */
	public synchronized boolean isEndRun(short dataWord) {
		/* no end run marker */
		return false;
	}
}
