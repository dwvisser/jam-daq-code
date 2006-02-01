package jam.global; 

import jam.sort.SortException;
import jam.sort.stream.AbstractEventOutputStream;

/**
 * Interface for user-written sort classes.
 *
 * User writes a sort class which must have the following methods:
 * <dl><dt><code>initialize</code></dt><dd>called when the sort 
 * process is initialized</dd>    
 * <dt><code>sort</code></dt><dd>called for each event</dd>
 * <dt><code>monitor</code></dt><dd>called each time the monitors are 
 * updated</dd></dl>
 *
 * @author Ken Swartz
 * @version 0.9
 * @since JDK 1.1
 */

 public interface Sorter {
  
    /** 
     * Initializes all variables and process states to be ready to 
     * begin sorting.  Should be called prior to 
     * any calls to <code>sort</code>.
     *
     * @exception Exception any exceptions caught by calls in this 
     * method should be thrown up to the controlling thread
     */
    void initialize() throws Exception;//NOPMD
    
    /**
     * Called to process each event.  The sort method looks at gates 
     * and increments histograms.
     * 
     * @param dataWords event passed as an <code>int</code> array for 
     * speed
     * @exception Exception any exceptions caught by calls in this 
     * method should be thrown up to the controlling thread
     */
    void sort(int [] dataWords) throws Exception;//NOPMD
    
    /**
     * Updates any monitors.
     * 
     * @param	name	name of monitor value to calculate
     * @return 	current value for monitor window to display
     * @see jam.data.Monitor
     */	
    double monitor(String name);
    
	/**
	 * set the state to write out events
	 * 
	 * @param enable
	 *            true to write out selected events
	 */
    void setWriteEnabled(boolean enable);
    
	/**
	 * Returns size of a event.
	 * 
	 * @return the size of the events
	 * @throws SortException
	 *             when there is no event size yet
	 */
	int getEventSize() throws SortException;

	/**
	 * Gets the buffer size in bytes.
	 * 
	 * @return size in bytes of event buffers
	 */
	int getBufferSize();
	
	/**
	 * Set the event stream to use to write out events.
	 * 
	 * @param out
	 *            stream to which presorted event output will go
	 */
	void setEventOutputStream(AbstractEventOutputStream out);

}