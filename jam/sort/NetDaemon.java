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
    
    MessageHandler msgHandler;
    /**
     * Host sending data
     */
    private String iNetAddressData;
    
    private InetAddress dataAddress;
    private int dataPort;
    private DatagramSocket dataSocket;
    private DatagramPacket dataIn;
    /**
     * buffer to recieve data from udp socket
     */
    private byte[] packetBuffer;
    private byte[] bufferOut;
    
    /**
     * ring buffers for passing events to sorting
     */
    private RingBuffer sortingRing;
    /**
     * ring buffers for passing events to storage
     */
    private RingBuffer storageRing;
    /**
     * set if we want data to be put is writing pipe
     * writting out data
     */
    private boolean writerOn;
    /**
     *set if we sorting data
     */
    private boolean sorterOn;
    /**
     *sample inteveral to sort data
     */
    private int sortInterval;
    /**
     * number of packets counter
     */
    private int packetCount=0;	    //number of packets recieved
    private int notSortCount=0;	    //number of packest not sorted
    private int notStorCount=0;
    private int packetLength;	    //length of packet recieved
    
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
        this.iNetAddressData=host;        
        try {//ceate a port listener
            dataAddress =InetAddress.getByName(iNetAddressData);
            dataPort=port;
            dataSocket=new DatagramSocket(dataPort,dataAddress);            
            packetBuffer=new byte[BUFFER_SIZE];
            dataIn = new DatagramPacket(packetBuffer, packetBuffer.length);
        } catch (UnknownHostException e) {
            throw new SortException(getClass().getName()+": The host, "+iNetAddressData+", is unknown.");
        } catch(BindException be){
            throw new SortException(getClass().getName()+": Could not bind to data socket.");
        } catch (IOException e) {
            throw new SortException(getClass().getName()+": Could not create data socket.");
        }
        notSortCount=0;
        notStorCount=0;
        writerOn=false;
        sorterOn=true;
        sortInterval=1;//sort every buffer
        this.setPriority(9);//high priority, normal=5
        this.setDaemon(true);
    }
    
    /**
     * Calls <code>receiveLoop</code>.
     *
     * @see #receiveLoop()
     */
    public void run(){
        try {
            receiveLoop();
        } catch (IOException ioe){
            msgHandler.errorOutln("Net Daemon halted "+ioe.getMessage());
        } catch (SortException je){
            msgHandler.errorOutln("Net Daemon halted "+je.getMessage());
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
        while(this.checkState()){//loop as long as state is RUN
            //wait for packet
            dataSocket.receive(dataIn);
            if (this.checkState()) {
                bufferOut=dataIn.getData();
                packetLength=dataIn.getLength();
                packetCount++;
                //put buffer into to sorting ring with aample fraction
                if( sorterOn&&((packetCount%sortInterval)==0) ){
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
     * Sets whether to write out events to the sorting pipe.
     *
     * @param writerOn <code>true</code> if send events, <code>false</code> if not
     */
    /*public void setSorting(boolean sorterOn){
        if(sortingRing!=null){
            this.sorterOn=writerOn;
        } else {
            msgHandler.errorOutln("Cant sort events no ring buffer [NetDaemon]");
        }
        
    }*/
    /**
     * Closes the network connection.
     */
    public void closeNet() {
        if(dataSocket!=null){
            dataSocket.close();
        }
    }
    /**
     * Sets the sort sample interval
     * This is the frequeny of buffers sent to
     * the sort routine.
     * For example if this is set to 2 only every second
     * buffer is sent to the sort routine.
     *
     * @param sample the sample interval
     */
    public void setSortInterval(int sample) {
        sortInterval=sample;
    }
    
    /**
     * Returns the sort sample interval.
     *
     * @see #setSortInterval(int)
     * @return the total number of packets sent
     */
    public int getSortInterval() {
        return sortInterval;
        
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
    
    /**
     * Network test method dumps data to a
     * file netttest.dmp
     * FIXME not finnished
     */
/*DEBUG
    public static void main(String args[]) {
        try {
            FileInputStream fis=new FileInputStream("nettest.dmp");
            NetDaemon net = new NetDaemon(fis);
            net.start();
        } catch (IOException) {
            System.out.println("error in NetDaemon main");
        }
    }
 */
}
