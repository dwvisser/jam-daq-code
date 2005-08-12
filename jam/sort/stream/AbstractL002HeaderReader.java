package jam.sort.stream;
import jam.global.MessageHandler;

import java.io.IOException;

/**
 * This class takes care of reading standard Oak Ridge L002 
 * header records. All event input streams which wish to do
 * the same should extend this.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Feb 2, 2004
 */
public abstract class AbstractL002HeaderReader extends AbstractEventInputStream implements L002Parameters {

	/**
	 * Creates the input stream given an event size.
	 *
	 * @param eventSize number of parameters per event.
	 * @param console where to print messages
	 */
	AbstractL002HeaderReader(MessageHandler console, int eventSize) {
		super(console, eventSize);
	}

	/**
	 * @param console for printing messages
	 */
	public AbstractL002HeaderReader(MessageHandler console) {
		super(console);
	}
	
	/**
	 * @see AbstractEventInputStream#EventInputStream()
	 */
	public AbstractL002HeaderReader(){
		super();
	}

	/**
	 * @see jam.sort.stream.AbstractEventInputStream#readHeader()
	 */
	public final boolean readHeader() throws EventException {
		final byte[] headerStart=new byte[32];//header key
		final byte[] date=new byte[16];//date mo/da/yr hr:mn
		final byte[] title=new byte[80];//title
		final byte[] reserved1=new byte[8];//reserved set to 0
		byte[] reserved2=new byte[92];//reserved set to 0
		byte[] secHead=new byte[256];//read buffer for secondary headers
		try {
			dataInput.readFully(headerStart);		//key
			dataInput.readFully(date);			//date
			dataInput.readFully(title);			//title
			final int number=dataInput.readInt();//header number
			dataInput.readFully(reserved1);
			final int numSecHead=dataInput.readInt();//number of secondary header records
			dataInput.readInt();//header record length
			dataInput.readInt();//Block line image records
			dataInput.readInt();//IMAGE_RECORD_LENGTH
			final int paramsPerEvent=dataInput.readInt();
			dataInput.readInt();//DATA_RECORD_LENGTH
			dataInput.readFully(reserved2);
			/* save reads to header variables */
			headerKey=String.valueOf(headerStart);
			headerRunNumber=number;
			headerTitle=String.valueOf(title);
			headerEventSize=paramsPerEvent;
			headerDate=String.valueOf(date);
			loadRunInfo();
			/* read secondary headers */
			for (int i=0; i<numSecHead; i++) {
				dataInput.readFully(secHead);
			}
			return headerKey.equals(HEADER_START);
		} catch (IOException ioe) {
			throw new EventException(getClass().getName()+".readHeader(): IOException "+ioe.getMessage());
		}
	}
}
