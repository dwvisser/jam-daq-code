package jam.sort.stream;
import static jam.sort.stream.L002Parameters.HEADER_START;
import jam.util.StringUtilities;

import java.io.IOException;

/**
 * This class takes care of reading standard Oak Ridge L002 
 * header records. All event input streams which wish to do
 * the same should extend this.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Feb 2, 2004
 */
public abstract class AbstractL002HeaderReader extends AbstractEventInputStream {

	/**
	 * Creates the input stream given an event size.
	 *
	 * @param eventSize number of parameters per event.
	 * @param consoleExists whether a console exists
	 */
	AbstractL002HeaderReader(boolean consoleExists, int eventSize) {
		super(consoleExists, eventSize);
	}

	/**
	 * @param consoleExists whether a console exists
	 */
	public AbstractL002HeaderReader(boolean consoleExists) {
		super(consoleExists);
	}
	
	/**
	 * @see AbstractEventInputStream#AbstractEventInputStream()
	 */
	public AbstractL002HeaderReader(){
		super();
	}
	
	/**
	 * Implementers must describe their particular
	 * variant of L002 in plain English.
	 * @return a string description
	 */
	public abstract String getFormatDescription();

	/**
	 * @see jam.sort.stream.AbstractEventInputStream#readHeader()
	 */
	public final boolean readHeader() throws EventException {
		final byte[] headerStart=new byte[32];//header key
		final byte[] date=new byte[16];//date mo/da/yr hr:mn
		final byte[] title=new byte[80];//title
		final byte[] reserved1=new byte[8];//reserved set to 0
		final byte[] reserved2=new byte[92];//reserved set to 0
		final byte[] secHead=new byte[256];//read buffer for secondary headers
		final StringUtilities stringUtil = StringUtilities.getInstance();
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
			final int eventParams=dataInput.readInt();
			dataInput.readInt();//DATA_RECORD_LENGTH
			dataInput.readFully(reserved2);
			/* save reads to header variables */
			headerKey=stringUtil.getASCIIstring(headerStart);
			headerRunNumber=number;
			headerTitle=stringUtil.getASCIIstring(title);
			headerEventSize=eventParams;
			headerDate=stringUtil.getASCIIstring(date);
			loadRunInfo();
			/* read secondary headers */
			for (int i=0; i<numSecHead; i++) {
				dataInput.readFully(secHead);
			}
			return headerKey.equals(HEADER_START);
		} catch (IOException ioe) {
			throw new EventException("Problem reading header.", ioe);
		}
	}
}
