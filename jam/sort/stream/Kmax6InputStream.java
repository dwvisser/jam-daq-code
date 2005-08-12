package jam.sort.stream;

import jam.global.MessageHandler;

import java.io.EOFException;
import java.io.IOException;

/**
 * This class knows how to handle the Kmax data format. It extends
 * <code>EventInputStream</code>, adding methods for reading events and
 * returning them as int arrays which the sorter can handle.
 * 
 * @version 0.4 June 2001
 * @author Ralph France
 * @see jam.sort.stream.AbstractEventInputStream
 * @see jam.sort.stream.L002InputStream
 * @see jam.sort.stream.UconnInputStream
 * @since JDK1.3
 */
public final class Kmax6InputStream extends AbstractEventInputStream {

	private int blockEventType;

	private int blockNumEvnt;

	private int countEvent = 0;

	private short[] eventsze = new short[32]; // event size array

	// private int countWord=0;
	private boolean newBlock = true;

	private int parameter;

	/**
	 * Default constructor.
	 */
	public Kmax6InputStream() {
		super();
	}

	/**
	 * Creates an instance with access to the Jam console.
	 * 
	 * @param console
	 *            where messages to the user go
	 */
	public Kmax6InputStream(MessageHandler console) {
		super(console);
	}

	/**
	 * Creates the input stream given an event size.
	 * 
	 * @param eventSize
	 *            number of parameters per event.
	 * @param console
	 *            where messages to user go
	 */

	public Kmax6InputStream(MessageHandler console, int eventSize) {
		super(console, eventSize);
	}

	/**
	 * Check for end of run word, may not be valid in the Kmax case, which
	 * relies solely on the end-of-file condition
	 * 
	 * @param dataWord
	 *            most recently read word
	 * @return whether end-of-file
	 */
	public synchronized boolean isEndRun(short dataWord) {
		return false;
	}

	/**
	 * Reads a block from the input stream Expects the stream to be at the
	 * beginning of a block It is up to user to ensure this
	 * 
	 * @return whether read is successful or not
	 * @throws EventException
	 *             thrown for errors in event stream
	 */
	private boolean readBlockHeader() throws EventException {
		boolean rval;
		try {
			blockEventType = dataInput.readInt();
			blockNumEvnt = dataInput.readInt();
			rval = true;
		} catch (EOFException eof) {
			rval = false;
		} catch (IOException ioe) {
			throw new EventException("Reading Block header," + ioe.getMessage()
					+ " [KmaxInputStream]");
		}
		return rval;
	}

	/**
	 * Reads an event from the input stream Expects the stream position to be
	 * the beginning of an event. It is up to the user to ensure this.
	 * 
	 * @param input
	 *            data array
	 * @exception EventException
	 *                thrown for errors in the event stream
	 * @return status resulting after read attempt
	 */
	public synchronized EventInputStatus readEvent(int[] input)
			throws EventException {
		try {
			if (newBlock) {// if a new block read in block header
				if (!readBlockHeader()) {
					return EventInputStatus.END_FILE;
				}
				newBlock = false;
				countEvent = 0;
				// countWord=0;
				// check if we are done with this block
			} else if (countEvent > blockNumEvnt) {
				// are we done with this block
				newBlock = true;
				return EventInputStatus.END_BUFFER;
			}
			if (blockEventType == 5) {
				for (parameter = 0; parameter < eventsze[4]; parameter++) {
					// read parameter word
					input[parameter] = dataInput.readInt();
				}
				return EventInputStatus.EVENT;
			} else if (blockEventType < 5) {
				for (parameter = 0; parameter < eventsze[blockEventType - 1]; parameter++) {
					dataInput.readInt();// header padding
				}
				return EventInputStatus.ERROR;
			} else {
				throw new IllegalStateException(getClass().getName()
						+ ": Block Event Type >5: " + blockEventType);
			}
			// we got to the end of a file or stream
		} catch (EOFException e) {
			return EventInputStatus.END_FILE;
			// throw new EventException("Reading event "+e.toString()+"
			// [KmaxInputStream]");
		} catch (IOException ioe) {
			console.errorOutln(ioe.toString());
			return EventInputStatus.ERROR;
		} catch (Exception e) {
			console.errorOutln(e.toString());
			return EventInputStatus.ERROR;
		}
	}

	/**
	 * Read in the header Format of KMaxo data Implemented
	 * <code>EventInputStream</code> abstract method.
	 * 
	 * @exception EventException
	 *                thrown for unrecoverable errors
	 * @return whether read is successful or not
	 */
	public boolean readHeader() throws EventException {
		try {
			final byte[] headerStart = new byte[354]; // KMax Header
			dataInput.readFully(headerStart); // Header to be ignored
			eventsze[0] = dataInput.readShort(); // array of event sizes
			eventsze[1] = dataInput.readShort(); // array of event sizes
			eventsze[2] = dataInput.readShort(); // array of event sizes
			eventsze[3] = dataInput.readShort(); // array of event sizes
			eventsze[4] = dataInput.readShort(); // array of event sizes
			final byte[] junk = new byte[54];
			dataInput.readFully(junk);
			final int paramsPerEvent = eventsze[4]; // SRQ event size is wanted
			final byte[] headerEnd = new byte[316]; // header padding
			dataInput.readFully(headerEnd);
			// save reads to header variables
			headerKey = "unknown";
			headerRunNumber = 0;
			headerTitle = headerKey;
			headerEventSize = paramsPerEvent;
			headerDate = headerKey;
			return true;
		} catch (IOException ioe) {
			throw new EventException("Reading event header from IOException "
					+ ioe.getMessage() + " [KmaxInputStream]");
		}
	}
}
