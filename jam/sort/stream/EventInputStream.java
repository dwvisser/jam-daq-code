package jam.sort.stream;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.IOException;
import jam.global.*;
import java.util.Hashtable;
import javax.swing.JOptionPane;

/**
 * A general-purpose <code>InputStream</code> of experiment events that can be sorted.
 *
 * @version	0.5 August 98
 * @author 	Ken Swartz
 * @since       JDK1.1
 */

public abstract class EventInputStream {
    
    /**
     * Number of signal values for each event.
     */
    protected int eventSize;
    
    /**
     * Size of a buffer, if appropriate
     */
    protected int bufferSize;
    
    /**
     *
     */
    protected int numberEvents;
    
    /**
     *
     */
    protected int bufferCount;
    
    /**
     *
     */
    protected int eventCount;
    
    /**
     * Stream events are read from
     */
    protected DataInputStream dataInput;
    
    /**
     *Number of bytes in header
     */
    protected int headerSize;
    
    /**
     * Header information
     */
    protected String headerKey;
    
    /**
     *
     */
    public int headerRunNumber;
    
    /**
     *
     */
    protected String headerTitle="No Title";
    
    /**
     *
     */
    protected String headerDate="No Date";
    
    /**
     *
     */
    protected int headerEventSize=0;
    
    /**
     *
     */
    protected int headerRecordLength=0;
    
    protected MessageHandler console;
    
    protected Hashtable scalerTable;
    
    /**
     * Make sure to issue a setConsole() after using this constructor
     * It is here to satisfy the requirements of Class.newInstance()
     */
    public EventInputStream(){
        super();
        eventCount=0;
        bufferCount=0;
    }
    
    /**
     * Default constructor.
     */
    public EventInputStream(MessageHandler console){
        this();
        setConsole(console);
    }
    
    /**
     * Constructor with event size given.
     *
     * @param console where to write text output to the user
     * @param size the number of signals per event
     */
    public EventInputStream(MessageHandler console, int size){
        this(console);
        eventSize=size;
    }
    
	/**
	 * Define the console.
	 *
	 * @param console where to write text output to the user
	 */
    public final void setConsole(MessageHandler console){
        this.console=console;
    }
    
    /**
     * Sets the event size.
     *
     * @param size the number of signals per event
     */
    public void setEventSize(int size){
        this.eventSize=size;
    }
    
    /**
     * Returns the event size.
     *
     * @return the number of signals per event
     */
    public int getEventSize(){
        return eventSize;
    }
    
    /**
     * Sets the size of the input buffer.
     *
     * @param size  the size in bytes of the input buffer
     */
    public void setBufferSize(int size){
        this.bufferSize=size;
    }
    
    /**
     * Returns the size of the input buffer.
     *
     * @return the size of the input buffer
     */
    public int getBufferSize(){
        return bufferSize;
    }
    
    /**
     *
     */
    public int getHeaderSize(){
        return headerSize;
    }
    
    /**
     * Sets the input stream which will be used as the source of events (and headers).
     *
     * @param inputStream source of event data
     */
    public void setInputStream(InputStream inputStream){
        dataInput=new DataInputStream(inputStream);
    }
    
    /**
     * Reads a byte.  Only implemented as a requirement of extending <code>InputStream</code>, which defines this
     * method <code>abstract</code>.
     *
     * @return the next byte in the stream defined in <code>setInputStream()</code>
     * @exception IOException thrown if ther's a problem reading from the stream
     * @see #setInputStream
     */
    public int read() throws IOException {
        return dataInput.read();
    }
    
    /**
     * Reads the next data word as a short. Can be overidden by subclasses.
     *
     * @return a <code>short</code> read from the input stream
     * @exception IOException thrown if ther's a problem reading from the stream
     * @see #setInputStream
     */
    public short readDataWord() throws IOException {
        return dataInput.readShort();
    }
    
    /**
     * Loads the run information, usually after it is read from a header.
     */
    public void loadRunInfo() {
        RunInfo.runNumber=headerRunNumber;
        RunInfo.runTitle=headerTitle;
        RunInfo.runStartTimeSt=headerDate;
        RunInfo.runEventSize=headerEventSize;
        RunInfo.runRecordLength=headerRecordLength;
    }
    
    // abstract methods for class
    
    /**
     * Reads an event into the passed array and returns a status flag. This can take on the following
     * values <ul><li>EVENT</li>
     * <li>END_RUN</li>
     * <li>END_BUFFER</li>
     * <li>ERROR</li></ul>
     *
     * @param event container for the event info read from the event stream
     * @return an indicator of the status after the read from the event stream
     * @exception EventException thrown if an error condition cannot be handled
     * @see EventInputStatus
     */
    abstract public EventInputStatus readEvent(int [] event) throws EventException;
    
    /**
     * Reads a header and return a status flag.
     *
     * @return <code>true</code> if OK, <code>false</code> if there was a problem
     * @exception EventException thrown if an error condition cannot be handled
     */
    abstract public boolean readHeader() throws EventException;
    
    /**
     * Checks if a word is an end-of-run marker.
     *
     * @param word  to be checked whether it is an end-of-run marker
     * @return	<code>true</code> if yes, <code>false</code> if no
     * @exception EventException thrown if an error condition cannot be handled
     */
    abstract public boolean isEndRun(short word);
    
    protected final void showErrorMessage(Exception e){
    	final String cname=getClass().getName();
    	if (console==null){
    		JOptionPane.showMessageDialog(null,e.getMessage(),cname,
    		JOptionPane.ERROR_MESSAGE);
    	} else {
    		console.errorOutln(cname+"--"+e.getMessage());
    	}
    }
    
	protected final void showWarningMessage(String s){
		final String cname=getClass().getName();
		if (console==null){
			JOptionPane.showMessageDialog(null,s,cname,
			JOptionPane.WARNING_MESSAGE);
		} else {
			console.errorOutln(cname+"--"+s);
		}
	}
	
	protected final void showMessage(String s){
		if (console!=null){
			console.messageOutln(s);
		}
	}
    
}
