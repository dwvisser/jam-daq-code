package jam.sort;
import jam.global.GoodThread;
import jam.global.MessageHandler;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * <code>NetDeamon</code> receives packets from the network
 * and sends the data data into one or two pipes.
 * One pipe to a <code>SortDeamon</code>, and the other is
 * a <code>TapeDaemon</code> or <code>DiskDaemon</code> if one is activated.
 *
 * @author Ken Swartz
 * @author Dale Visser
 * @version 0.5
 * @since JDK 1.1
 */
public  class NetDaemon extends GoodThread {
    
    public final static int BUFFER_SIZE=8*1024;	//8 kBytes
    
    private final MessageHandler msgHandler;
    private final DatagramSocket dataSocket;
    
    /**
     * ring buffers for passing events to sorting
     */
    private final RingBuffer sortingRing;
    
    /**
     * ring buffers for passing events to storage
     */
    private final RingBuffer storageRing;
    
    /**
     * set if we want data to be put is writing pipe
     * writting out data
     */
    private boolean writerOn=false;
    
    /**
     *set if we sorting data
     */
    private boolean sorterOn=true;
    
    /**
     * sample interval to sort data
     */
    //private int sortInterval=1;//sort every buffer
    
    /**
     * number of packets counter
     */
    private int packetCount=0;	    //number of packets received
    private int notSortCount=0;	    //number of packets not sorted
    private int notStorCount=0;//number of packets not stored
    
    /**
     * Constructor passed both storage and sorting pipes.
     *
     * @param sortingRing buffer the events are sent to for sorting
     * @param storageRing buffer the events are sent to for storage
     * @param msgHandler where messages can be sent to the console
     * @exception SortException thrown if there's a problem setting up the pipes
     */
    public NetDaemon( RingBuffer sortingRing, RingBuffer storageRing, MessageHandler msgHandler,
    String host, int port)
    throws SortException {
        this.sortingRing=sortingRing;
        this.storageRing=storageRing;
        this.msgHandler=msgHandler;
        try {//ceate a port listener
            final InetAddress dataAddress =InetAddress.getByName(host);
            dataSocket=new DatagramSocket(port,dataAddress);            
        } catch (UnknownHostException e) {
            throw new SortException(getClass().getName()+": The host, "+host+", is unknown.");
        } catch(BindException be){
            throw new SortException(getClass().getName()+": Could not bind to data socket. Are other copies of Jam running?");
        } catch (IOException e) {
            throw new SortException(getClass().getName()+": Could not create data socket.");
        }
        setPriority(ThreadPriorities.NET);
        setDaemon(true);//the user doesn't interact with this thread
        setName("UDP/IP Data Buffer Receiver");
    }
    
    /**
     * Calls <code>receiveLoop</code>.
     *
     * @see #receiveLoop()
     */
    public void run(){
    	final StringBuffer message=new StringBuffer(
   		getClass().getName()).append(
		"--communication with acquisition halted, because of ");
        try {
            receiveLoop();
        } catch (Exception e){
        	message.append(e.getClass().getName()).append(':').append(
        	e.getMessage());
            msgHandler.warningOutln(message.toString());
        }
    }
    
    /**
     * Runs in an infinite loop receiving data from the local net
     * and stuffing it into a couple of pipes.
     *
     * @exception IOException if there's a problem storing the data
     * @exception SortException if there's a problem sorting the data
     */
    public  void receiveLoop() throws IOException, SortException {
        if (dataSocket==null) {
            throw new SortException("Could not start netDeamon, socket null {NetDaemon]");
        }
		final byte [] bufferOut=new byte[BUFFER_SIZE];
		final DatagramPacket dataIn = new DatagramPacket(bufferOut, bufferOut.length);
        while(this.checkState()){//loop as long as state is RUN
            //wait for packet
            dataSocket.receive(dataIn);
            if (checkState()) {
                dataIn.getData();//data goes to bufferOut
                packetCount++;
                //put buffer into to sorting ring with sample fraction
                if( sorterOn ){
                    try {
                        sortingRing.putBuffer(bufferOut);
                    } catch (RingFullException rfe){
                        notSortCount++;
                        msgHandler.errorOutln("Sorting Buffer "+rfe.getMessage());
                    }
                }
                //put buffer into to storage ring
                if(writerOn){
                    try {
                        storageRing.putBuffer(bufferOut);
                    } catch (RingFullException rfe) {
                        notStorCount++;
                        msgHandler.errorOutln("Storage Buffer "+rfe.getMessage());
                    }
                }
            } else {//received a packet while thread state not RUN
                msgHandler.warningOutln("Warning: recevied buffer while NetDaemon thread"+
                " state was not RUN: state="+this);
            }
        }//end RUN loop
    }
    
    /**
     * Sets whether to write out events to the storage pipe.
     *
     * @param writerOn <code>true</code> if write events, <code>false</code> if not
     */
    public void setWriter(boolean writerOn){
        if(storageRing!=null){
            this.writerOn=writerOn;
        } else {
            msgHandler.warningOutln("Can't write out events no ring buffer [NetDaemon].");
        }
    }
    
    /**
     * Closes the network connection.
     */
    public void closeNet() {
        if(dataSocket!=null){
            dataSocket.close();
        }
    }
    
    /**
     * Returns the total packets sent.
     *
     * @return the total number of packets sent
     */
    public int getPacketCount() {
        return packetCount;
    }
    
    /**
     * Sets the packet count.
     *
     * @param count the total number of packets sent
     */
    public void setPacketCount(int count) {
        packetCount=count;
    }
    
    public int getStoredPackets(){
    	return packetCount-notStorCount;
    }
    
    public int getSortedBuffers(){
    	return packetCount-notSortCount;
    }
    
    public void resetCounters(){
    	synchronized(sortingRing){
    		packetCount=0;
    		notStorCount=0;
    		notSortCount=0;
    	}
    }
}
