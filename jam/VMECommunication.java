/*
 */
package jam;
import jam.data.Scaler;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.GlobalException;
import jam.global.GoodThread;
import jam.global.JamProperties;
import jam.global.MessageHandler;
import jam.sort.CamacCommands;
import jam.sort.VME_Channel;
import jam.sort.VME_Map;
import java.io.*;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Class to communicate with VME crate using
 * udp packets
 * Two udp sockets can be setup with a daemon that
 * receives packets and knows what to do with them
 * The first int of a packet indicates what type of packe
 * it is.
 *
 * @version  0.5  April 98
 * @author   Ken Swartz and Dale Visser
 * @since       JDK1.1
 */
class VMECommunication  extends GoodThread implements FrontEndCommunication {

    private static final int MAX_PACKET_SIZE=1024;
	private static final int MAX_MESSAGE_SIZE=80;

	private static final byte STRING_NULL=(byte)0x0;

    /* type of message */
    private static final int OK=0;//standard message
	private static final int ERROR=1;//message indicating error condition
	private static final int SCALER=2;//received from VME, contains scaler values
	private static final int CNAF=3;//sent to VME, contains CNAF commands
	private static final int COUNTER=4;//???
	private static final int VME_ADDRESSES=5;//sent to VME, contains VME addressing information
	private static final int SCALER_INTERVAL=6;//sent to VME, contains interval to insert scalers in event stream

    private final JamCommand jamCommand;
    private final JamMain jamMain;
    private final Broadcaster broadcaster;
    private final MessageHandler console;

    private InetAddress addressVME;
    private int vmePort;
    private DatagramSocket  socketSend,  socketReceive;

    private boolean active;        //
    private int [] scalerValues;    //scaler values, loaded when a scaler packet is received.
    private int [] counterValues;    //counter values, loaded when a counter packet is received.

    /** Creates the instance of this class for handling IP 
     * communications with the VME front end computer.
     * 
     * @param jamMain main class
     * @param jamCommand class that handles main window GUI events
     * @param broadcaster class that distributes Jam-wide messages
     * @param console class that takes text input from the user
     */
    VMECommunication(JamMain jamMain, JamCommand jamCommand, Broadcaster broadcaster, MessageHandler console) {
        super();
        this.jamMain=jamMain;
        this.jamCommand=jamCommand;
        this.broadcaster=broadcaster;
        this.console=console;
        active=false;
    }

    /** Sets up networking.  Two udp sockets are created: one for 
     * receiving and one for sending.
     * A daemon is also set up for receiving.
     * @throws JamException if something goes wrong
     */
    public synchronized void setup()  throws JamException {
    	final InetAddress addressLocal;
    	
    	final String LOCAL_IP=JamProperties.getPropString(JamProperties.HOST_IP);
    	final int portSend=JamProperties.getPropInt(JamProperties.HOST_PORT_SEND);
    	final int portRecv=JamProperties.getPropInt(JamProperties.HOST_PORT_RECV);
    	final String VME_IP=JamProperties.getPropString(JamProperties.TARGET_IP);
    	final int PORT_VME_SEND=JamProperties.getPropInt(JamProperties.TARGET_PORT);
        vmePort=PORT_VME_SEND;
        if (!active) { //FIXME maybe should check for null sockets instead
            try {//ceate a ports to send and receive
                addressLocal  =InetAddress.getByName(LOCAL_IP);
            } catch (UnknownHostException ue) {
                throw new JamException(getClass().getName()+": Unknown local host "+LOCAL_IP);
            }
            try {
                addressVME    =InetAddress.getByName(VME_IP);
            } catch (UnknownHostException ue){
                throw new JamException(getClass().getName()+": Unknown VME host "+VME_IP);
            }
            try {
                socketSend    =new DatagramSocket(portSend, addressLocal);
            } catch (BindException be) {
                throw new JamException(getClass().getName()+": Problem binding send socket (another Jam online running)");
            } catch (SocketException se) {
                throw new JamException(getClass().getName()+": problem creating send socket");
            }
            try {
                socketReceive =new DatagramSocket(portRecv, addressLocal);
            } catch (BindException be) {
                throw new JamException(getClass().getName()+": Problem binding receive socket (another Jam online running)");
            } catch (SocketException se) {
                throw new JamException(getClass().getName()+": Problem creating receive socket");
            }
            // setup and start receiving deamon
            this.setDaemon(true);
            this.setPriority(6);      //one higher than main display
            this.start();
            active=true;
            debug(JamProperties.getBooleanProperty(JamProperties.FRONTEND_DEBUG));
            verbose(JamProperties.getBooleanProperty(JamProperties.FRONTEND_VERBOSE));
        }
    }

    /** Recieves distributed events. Can listen for broadcasted event.
     * Implementation of Observable interface.
     * @param observable object being observed
     * @param o additional parameter from <CODE>Observable</CODE> 
     * object
     */
    public void update(Observable observable, Object o){
        final BroadcastEvent be=(BroadcastEvent)o;
        final int command=be.getCommand();
        if(command==BroadcastEvent.SCALERS_READ){
            readScalers();
        } else if (command==BroadcastEvent.SCALERS_CLEAR){
            clearScalers();
        } else if (command==BroadcastEvent.COUNTERS_READ){
            readCounters();
        } else if (command==BroadcastEvent.COUNTERS_ZERO){
            zeroCounters();
        }
    }

    /**
     * Method for opening a file for event storage on the VME host? 
     * (not used yet)
     *
     * @param file the filename to open
     * @throws JamException if there's a problem
     */
     public void openFile(String file) throws JamException{
		final String OPENFILE="OPENFILE ";//add filename as an argument
        this.VMEsend(OPENFILE+file);
    }
    
    /** 
     * Tells the front end to start acquisiton.
     * @throws JamException if there's a problem while trying to send 
     * the message
     */
    public void VMEstart() throws JamException {
		final String START="START";
        this.VMEsend(START);
    }

    /** 
     * Tells the front end to stop acquisiton, which also flushes out
     * a buffer.
     * 
     * @throws JamException if there's a problem while trying to send 
     * the message
     */
    public void VMEstop() throws JamException {
		final String STOPACQ="STOP";
        this.VMEsend(STOPACQ);
    }

    /**
     * Tells the front end to stop acquisiton and end the run,
     * which flushes out the data buffer with an appended end-run 
     * marker.
     */
    public void end() {
		final String END="END";
        try {
            this.VMEsend(END);
        } catch (JamException je) {
            console.errorOutln(je.getMessage());
        }
    }

    /**
     * Tells the front end to flush out the data buffer, and send the 
     * contents.
     */
    public void flush() {
		final String FLUSH="FLUSH";
        try {
            this.VMEsend(FLUSH);
        } catch (JamException je) {
            console.errorOutln(je.getMessage());
        }
    }

    /**
     * Tell the VME to read the scalers, and
     * send back two packets: the packet with the scaler values,
     * and a status packet (read OK or ERROR) with a message.
     */
    public void readScalers()  {
 	 	final String RUN_SCALER=  "list scaler";
       	try {
            this.VMEsend(RUN_SCALER);
        } catch (JamException je) {
            console.errorOutln(je.getMessage());
        }
    }

    /**
     * Tells the VME to clear the scalers and send a reply: OK or 
     * ERROR.
     */
    public void clearScalers(){
		final String RUN_CLEAR=  "list clear";
        try {
            this.VMEsend(RUN_CLEAR);
        } catch (JamException je) {
            console.errorOutln(je.getMessage());
        }
    }

    /**
     * Tells the VME to read the counters, and
     * send back two packets: the packet with the counter values,
     * and a status packet (read OK or ERROR) with a message.
     */
    public void readCounters()  {
 		final String COUNT_READ=  "count read";
       try {
            this.VMEsend(COUNT_READ);
        } catch (JamException je) {
            console.errorOutln(je.getMessage());
        }
    }

    /**
     * Tells the VME to zero its counters and send a reply: OK or 
     * ERROR.
     */
    public void zeroCounters() {
 		final String COUNT_ZERO=  "count zero";
       try {
            this.VMEsend(COUNT_ZERO);
        } catch (JamException je) {
            console.errorOutln(je.getMessage());
        }
    }

    /**
     * Tells the VME whether to print out debugging statements.
     *
     * @param state true if we want debug messages from the VME
     */
    public void debug(boolean state){
 		final String DEBUG_ON=  "debug on";
		final String DEBUG_OFF=  "debug off";
       try {
            if (state){
                this.VMEsend(DEBUG_ON);
            } else {
                this.VMEsend(DEBUG_OFF);
            }
        } catch (JamException je) {
            console.errorOutln(je.getMessage());
        }
    }

    /**
     * Tells the VME whether to send verbose verbose status messages, 
     * which Jam automatically prints on the console.
     *
     * @param state true if user wants VME to be verbose
     */
    public void verbose(boolean state){
		final String VERBOSE_ON=  "verbose on";
		final String VERBOSE_OFF=  "verbose off";
        try {
            if (state){
                this.VMEsend(VERBOSE_ON);
            } else {
                this.VMEsend(VERBOSE_OFF);
            }
        } catch (JamException je) {
            console.errorOutln(je.getMessage());
        }
    }

    /**
     * New version uploads CAMAC CNAF commands with udp pakets, and
     * sets up the camac crate.
     *
     * @param camacCommands object containing CAMAC CNAF commands
     * @throws JamException if there's a problem
     */
    public void setupCamac(CamacCommands camacCommands) throws JamException {
 	   	final String RUN_INIT=  "list init";
		final String CNAF_INIT=     "cnaf init";
		final String CNAF_EVENT=    "cnaf event";
		final String CNAF_SCALER=   "cnaf scaler";
		final String CNAF_CLEAR=    "cnaf clear";
       	sendCNAFList(CNAF_INIT, camacCommands.getInitCommands()); //load initialize CNAFs
        sendCNAFList(CNAF_EVENT, camacCommands.getEventCommands()); //load eventCNAFs
        sendCNAFList(CNAF_SCALER, camacCommands.getScalerCommands() );  //load scaler CNAFs
        sendCNAFList(CNAF_CLEAR, camacCommands.getClearCommands());    //load clear CNAFs
        this.VMEsend(RUN_INIT);      //initialize camac
        console.messageOutln("Loaded CAMAC command lists, and initialized VME.");
    }

    /**
     * Send the message specifying how to use ADC's and TDC's.
     *
     * @param vmeMap the map of channels to use and TDC ranges
     * @throws JamException if there's a problem
     */
     public void setupVME_Map(VME_Map vmeMap) throws JamException {
        String temp="";
        final VME_Channel [] eventParams = vmeMap.getEventParameters();
        final Map hRanges = vmeMap.getV775Ranges();
        int numRanges = 0;
        if (eventParams.length > 0) {
            final int totalParams = eventParams.length;
            temp += totalParams + "\n";
            for (int i=0; i < eventParams.length; i++) {
                temp += eventParams[i].getSlot() + " ";
                temp += "0x"+Integer.toHexString(eventParams[i].getBaseAddress())+" ";
                temp += eventParams[i].getChannel()+" ";
                temp += eventParams[i].getThreshold() + "\n";
            }
            numRanges=hRanges.size();
            temp += numRanges+"\n";
            if (numRanges > 0) {
                /*Enumeration eb = hRanges.keys();
                Enumeration er = hRanges.elements();*/
                final Iterator it=hRanges.entrySet().iterator();
                /*while (eb.hasMoreElements()) {
                    int base = ((Integer)eb.nextElement()).intValue();
                    temp += "0x"+Integer.toHexString(base)+" "+er.nextElement()+"\n";
                }*/
                while (it.hasNext()){
                	final Map.Entry next=(Map.Entry)it.next();
                	final int base = ((Integer)next.getKey()).intValue();
                	temp += "0x"+Integer.toHexString(base)+" "+next.getValue()+"\n";
                }
            }
            temp += "\0";
        } else {
            throw new JamException (getClass().getName()+".setupVME_Map(): no event parameters in map.");
        }
        stringPacketDump(VMECommunication.VME_ADDRESSES,temp);
        VMEsend(VMECommunication.VME_ADDRESSES,temp);
    }

    /**
     * Send the interval in seconds between scaler blocks in the event
     * stream.
     * 
     * @param seconds the interval between scaler blocks
     * @throws JamException if there's a problem
     */
     public void sendScalerInterval(int seconds) throws JamException {
        final String message= seconds+"\n\0";
        stringPacketDump(SCALER_INTERVAL,message);
        VMEsend(SCALER_INTERVAL,message);
    }

    /**
     * Method which is used to send all packets containing a string to
     * the VME crate.
     *
     * @param message   string to send
     * @throws JamException if there's a problem
     */
    private void VMEsend(String message) throws JamException {
        VMEsend(VMECommunication.OK, message);
    }

    /**
     * Method which is used to send all packets containing a string to
     * the VME crate.
     *
     * @param status one of OK, SCALER, ERROR, CNAF, COUNTER, 
     * VME_ADDRESSES or SCALER_INTERVAL
     * @param message string to send
     * @throws JamException if there's a problem
     */
    private void VMEsend(int status, String message)  throws JamException {
        final DatagramPacket packetMessage;
        /* byte arrays initialized with zeros by definition */
        final byte [] byteMessage = new byte [message.length()+5];
        if (! validStatus(status)) {
        	throw new JamException(getClass().getName()+".vmeSend() with invalid status: "+status);
        } 
        byteMessage[3]=(byte) status;//first four bytes interpreted together as this number
        System.arraycopy(message.getBytes(),0,byteMessage,4,message.length());
        byteMessage[byteMessage.length-1]=STRING_NULL;
        try {//create and send packet
            packetMessage=new DatagramPacket(byteMessage, byteMessage.length, addressVME, vmePort);
            if (socketSend != null) {
                socketSend.send(packetMessage);
            } else {
                console.errorOutln(getClass().getName()+".VMEsend(): "+
				"Attempted to send a message without a connection. To create a connection, set up online acquisition.");
            }
        } catch (IOException e) {
            console.errorOutln(getClass().getName()+".VMEsend(): "+
				"Jam encountered a network communication error attempting to send a packet.");
        }
    }

    /**
     * Method to send a cnaf list to the VME crate
     *
     * packet structure
     * 16 bytes name
     * 4 bytes number of cnafs
     * 4 bytes for each cnaf
     *
     * @param listName name of cnaf list to send
     * @param cnafList the CAMAC commands
     * @throws JamException if there's a problem
     */
    private void sendCNAFList(String listName, List cnafList) throws JamException {
		final int COMMAND_SIZE=16;
		final int CNAF_SIZE=9;
        byte [] byteName=new byte[COMMAND_SIZE];
        if(listName.length()>(COMMAND_SIZE-1)){
            throw new JamException("Command string length too long [VMECommunication]");
        }
        final byte [] byteMessage=new byte [4+COMMAND_SIZE+4+cnafList.size()*CNAF_SIZE+1];
        /* set first int to 3, first three bytes are zero already */
        byteMessage[3]=CNAF;
        //put command string into packet
        byteName=listName.getBytes();
        for(int i=0;i<listName.length();i++){
            byteMessage[i+4]=byteName[i];
        }
        //put length of cnaf list in packet
        for (int i=20;i<=23;i++){
            byteMessage[i]=0;
        }
        byteMessage[23]=(byte)cnafList.size();
        //put list of cnaf commands into packet
        for (int i=0; i<cnafList.size(); i++) {
            final int offset=4+COMMAND_SIZE+4+CNAF_SIZE*i;
            final int [] cnaf=(int [])cnafList.get(i);
			byteMessage[offset+0]=(byte)(cnaf[0]&0xFF);        //id
			byteMessage[offset+1]=(byte)(cnaf[1]&0xFF);        //c
			byteMessage[offset+2]=(byte)(cnaf[2]&0xFF);        //n
			byteMessage[offset+3]=(byte)(cnaf[3]&0xFF);        //a
			byteMessage[offset+4]=(byte)(cnaf[4]&0xFF);        //f
			byteMessage[offset+5]=(byte)((cnaf[5]>>>24 )&0xFF);      //data byte msb
			byteMessage[offset+6]=(byte)((cnaf[5]>>>16 )&0xFF);      //data byte 1
			byteMessage[offset+7]=(byte)((cnaf[5]>>>8)&0xFF);      //data byte 2
			byteMessage[offset+8]=(byte)((cnaf[5]>>>0)&0xFF);      //data byte lsb
        }
        //add a null character
        byteMessage[byteMessage.length-1]=STRING_NULL;
        sendPacket(byteMessage);//send it
    }

    /**
     * Send out a packet of the given bytes.
     * 
     * @param byteMessage the message to send
     * @throws JamException if there's a problem
     */
    private void sendPacket(byte [] byteMessage) throws JamException {
        try {//create and send packet
            final DatagramPacket packetMessage=new DatagramPacket(byteMessage, byteMessage.length, addressVME,
            vmePort);
            if (socketSend!=null) {
                socketSend.send(packetMessage);
            } else {
                throw new JamException("Network communication, send socket not setup [VMECommunication]");
            }
        } catch (IOException e) {
            throw new JamException ("Network communication, sending packet [VMECommnunication]");
        }
    }

    /**
     * Thread method: is a deamon for receiving packets from the VME 
     * crate. The first int of the packet is the status word.
     * It determines how the packet is to be handled.
     *
     * @author Ken Swartz
     * @author Dale Visser
     */
    public void run(){
        int status;          //first int of message tells what type of packet

    	final byte[] bufferIn=new byte[MAX_PACKET_SIZE];
        try {
            while (true) {//loop forever receiving packets
                final DatagramPacket packetIn = new DatagramPacket(bufferIn, bufferIn.length);
                socketReceive.receive(packetIn);
                ByteArrayInputStream messageBais = new ByteArrayInputStream(packetIn.getData());
                DataInput messageDis=new DataInputStream(messageBais);
                status=messageDis.readInt();
                if (status==OK) {
                    console.messageOutln(getClass().getName()+": "+unPackMessage(messageDis));
                } else if (status==SCALER){
                    unPackScalers(messageDis);
                    Scaler.update(scalerValues);
                    broadcaster.broadcast(BroadcastEvent.SCALERS_UPDATE);
                } else if (status==COUNTER){
                    unPackCounters(messageDis);
                    broadcaster.broadcast(BroadcastEvent.COUNTERS_UPDATE, counterValues);
                } else if (status==ERROR) {
                    console.errorOutln(getClass().getName()+": "
                        +unPackMessage(messageDis));
                } else {
                    console.errorOutln(getClass().getName()+": packet with unknown message type received");
                }
            }// end of receive message forever loop
        } catch (IOException ioe){
            console.errorOutln("Unable to read datagram status word [VMECommnunication]");
            console.messageOutln("Network receive daemon stopped, need to restart Online [VMECommunication]");
        } catch (JamException je){
            console.errorOutln(je.getMessage());
            console.messageOutln("Network receive daemon stopped, need to restart Online [VMECommunication]");
        } catch (GlobalException ge) {
            console.errorOutln(getClass().getName()+".run(): "+ge);
            console.messageOutln("Network receive daemon stopped, need to restart Online.");
        }
    }

    /**
     * Unpack a datagram with a message. Message packets have an ASCII 
     * character array terminated with \0.
     * 
     * @param messageDis packet contents passed in readable form
     * @return the string contained in the message
     * @throws JamException if there's a problem
     */
    private String unPackMessage(DataInput messageDis) throws JamException {
        final char [] errorChar = new char[MAX_MESSAGE_SIZE];
        String errorMessage="Undecypherable Message";
        int numChar=0;
        try {
            while( (errorChar[numChar]=(char)messageDis.readByte()) !='\0' ) {
                numChar++;
            }
            errorMessage=new String(errorChar, 0, numChar);
        } catch (IOException ioe) {
            throw new JamException (getClass().getName()+
            ": unable to unpack message datagram.");
        }
        return errorMessage;
    }

    /**
     * Unpack scalers from udp packet.
     * Packet format:
     *       <ul>
     *       <li>int type      SCALER (which is already read)
     *      <li>int numScaler   number of scalers
     *      <li>int [] values    scaler values
     *      </ul>
     *
     * @param messageDis message in readable form
     * @throws JamException if there's a problem
     */
    private void unPackScalers(DataInput messageDis) throws JamException {
        try {
        	synchronized (this){
            	final int numScaler=messageDis.readInt();//number of scalers
            	scalerValues=new int [numScaler];
            	for (int i=0; i<numScaler;i++){
                	scalerValues[i]=messageDis.readInt();
            	}
            }
        } catch (IOException ioe) {
            throw new JamException (getClass().getName()+
            ": unable to unpack scaler datagram.");
        }
    }

    /**
     * Unpacks counters from udp packet.
     * Packet format:
     * <dl>
     *      <dt>int type      <dd>SCALER (which is already read)
     *      <dt>int numScaler   <dd>number of scalers
     *      <dt>int [] values    <dd>scaler values
     * </dl>
     * 
     * @param messageDis message in readable form
     * @throws JamException if there's a problem
     */
    private void unPackCounters(DataInput messageDis) throws JamException {
		final int COUNT_NUMBER=3;
        try {
        	synchronized (this){
            	final int numCounter=messageDis.readInt(); // number of counters
            	counterValues=new int [COUNT_NUMBER];
            	for (int i=0; i<numCounter;i++){
                	counterValues[i]=messageDis.readInt();
            	}
            }
        } catch (IOException ioe) {
            throw new JamException (getClass().getName()+
            ".unpackCounters(): unable to unpack count datagram.");
        }
    }

    /**
     * Get the scaler values from the last read.
     * 
     * @return the values of the scalers
     */
    public int [] getScalers(){
        return scalerValues;
    }

    private void stringPacketDump(int type, String message) {
        System.err.println("** PACKET BEGIN (type "+type+") **");
        System.err.println(message);
        System.err.println("*****  PACKET END   ******");
    }

    /**
     * Checks whether the parameter is a valid value of a message 
     * status.
     *
     * @param status to be checked
     * @returns true if valid, false if not
     */
    private boolean validStatus(int status) {
        return (status==OK || status==ERROR || status==SCALER ||
        	status==CNAF || status==COUNTER || status==VME_ADDRESSES ||
        	status==SCALER_INTERVAL);
    }
}
