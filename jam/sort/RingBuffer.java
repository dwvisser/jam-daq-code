package jam.sort;

import javax.swing.JOptionPane;

/**
 * <code>RingBuffer</code> is a list of buffers which
 * starts repeating after the last buffer is filled.
 * It allows asyncronous inter-Thread communication.
 * If a buffer is placed in a full ring, an exception is
 * thrown.
 *
 * @author Ken Swartz
 * @version 0.5,0.9
 * @since JDK 1.1
 */
public class RingBuffer {
    
    /**
     * Size in bytes of a single buffer.
     */
    public static final int BUFFER_SIZE=8*1024;
    
    /**
     * Number of buffers in ring.
     */
    protected static final int NUMBER_BUFFERS=0x40;  //64 buffers in ring
    protected static final int HALF_BUFFERS=NUMBER_BUFFERS/2;
    
	protected static final int BIG=Integer.MAX_VALUE/2;
    
    /**
     * Mask that makes counter less than Number buffers
     */
    protected static final int MASK=NUMBER_BUFFERS-1;
    
    private final byte [][] ringBuffer=new byte [NUMBER_BUFFERS][BUFFER_SIZE];;
    
    /**
     * where we will put the next buffer
     */
    private int posPut=0;
    
    /**
     * where we will get the next buffer from
     */
    private int posGet=0;
    
    /**
     * Creates a new ring buffer.
     */
    public RingBuffer()  {        
        posPut=0;
        posGet=0;
    }
    
    /**
     * Copies the passed array into the ring buffer.
     *
     * @exception   RingFullException    thrown when the ring is too full to be written to
     */
    public synchronized void putBuffer(byte [] inBuffer) throws RingFullException {
        if(full()){
        	final StringBuffer message=new StringBuffer();
        	message.append("Lost a buffer in thread \"");
        	message.append(Thread.currentThread().getName());
        	message.append("\" when putBuffer() called while already full.");
            throw new RingFullException(message.toString());
        }
        System.arraycopy(inBuffer, 0, ringBuffer[posPut&MASK], 0, inBuffer.length);
        final boolean emptyBeforePut=empty();
        posPut++;
        /* The only reason another thread could be in wait() on this
         * object is that we were empty. Checking eliminates a lot
         * of needless notify() calls.
         */
        if (emptyBeforePut){
			notifyAll();
        }
    }
    
    /**
     * Gives a pointer to the next buffer in the ring.
     *
     * @return the next buffer in the ring
     */
    public synchronized byte [] getBuffer(){        
        while(empty()){
            try {
				/* notified when a putBuffer() occurs on the empty
				 * ring
				 */
                wait();
            } catch (InterruptedException ie){
                JOptionPane.showMessageDialog(null,ie.getMessage(),
				getClass().getName(),JOptionPane.ERROR_MESSAGE);
            }
        }
        //&MASK serves to keep index accessed running 0..63,0..63, etc.
        return ringBuffer[(posGet++)&MASK];
    }
    
    /**
     * Tells you if the ring buffer is empty.
     * Used to check if you have read all the buffers in
     * the ring.
     *
     * @return true if there are no buffers in the ring.
     */
    public synchronized boolean empty(){
        return(posPut==posGet);
    }
    
    public synchronized boolean full(){
		return posPut-posGet+1 > NUMBER_BUFFERS;
    }
    
    public synchronized boolean halfFull(){
		return posPut-posGet+1 > HALF_BUFFERS;
    }
}