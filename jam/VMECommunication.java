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
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
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

    //properties for networking local host
    private String LOCAL_IP=JamProperties.getPropString(JamProperties.HOST_IP);
    private int PORT_SEND=JamProperties.getPropInt(JamProperties.HOST_PORT_SEND);
    private int PORT_RECV=JamProperties.getPropInt(JamProperties.HOST_PORT_RECV);
    //target host
    private String VME_IP=JamProperties.getPropString(JamProperties.TARGET_IP);
    private int PORT_VME_SEND=JamProperties.getPropInt(JamProperties.TARGET_PORT);

    static final int MAX_PACKET_SIZE=1024;
    static final int MAX_MESSAGE_SIZE=80;

    static final byte DISPLAY_TRUE=(byte)1;
    static final byte DISPLAY_FALSE=(byte) 0;
    static final byte STRING_NULL=(byte)0x0;

    /* type of message */
    private static final int OK=0;//standard message
	private static final int ERROR=1;//message indicating error condition
	private static final int SCALER=2;//received from VME, contains scaler values
	private static final int CNAF=3;//sent to VME, contains CNAF commands
	private static final int COUNTER=4;//???
	private static final int VME_ADDRESSES=5;//sent to VME, contains VME addressing information
	private static final int SCALER_INTERVAL=6;//sent to VME, contains interval to insert scalers in event stream

    /* stuff for cnaf packet */
	private static final int COMMAND_SIZE=16;
	private static final int CNAF_SIZE=9;

    /**
     * Messages specific to starting, stopping, flushing data, writing out of 
     * data.
     */
	private static final String START="START";
	private static final String STOP="STOP";
	private static final String FLUSH="FLUSH";
	private static final String END="END";
	private static final String OPENFILE="OPENFILE ";//add filename as an argument

	/* stuff that isn't currently used, but might be later?
	 * 
	private static final String SET_PATH="setpath ";
	private static final String LOAD_CNAF_INIT=      "loadcnaf init";
	private static final String LOAD_CNAF_EVENT=    "loadcnaf event";
	private static final String LOAD_CNAF_SCALER=   "loadcnaf scaler";
	private static final String LOAD_CNAF_CLEAR=    "loadcnaf clear";
	private static final String LOAD_CNAF_USER =    "loadcnaf user";
	 */
	
	private static final String RUN_INIT=  "list init";
	private static final String RUN_EVENT=  "list event";
	private static final String RUN_SCALER=  "list scaler";
	private static final String RUN_CLEAR=  "list clear";
	//future private static final String RUN_USER=  "list user";

	private static final String CNAF_INIT=     "cnaf init";
	private static final String CNAF_EVENT=    "cnaf event";
	private static final String CNAF_SCALER=   "cnaf scaler";
	private static final String CNAF_CLEAR=    "cnaf clear";
	//future private static final String CNAF_USER =    "cnaf user";
	
	private static final String VERBOSE_ON=  "verbose on";
	private static final String VERBOSE_OFF=  "verbose off";
	private static final String DEBUG_ON=  "debug on";
	private static final String DEBUG_OFF=  "debug off";

	private static final String COUNT_READ=  "count read";
	private static final String COUNT_ZERO=  "count zero";
	private static final int COUNT_NUMBER=3;

    static final String EXCUTE_CNAF=  "cnaf";

    private JamCommand jamCommand;
    private JamMain jamMain;
    private Broadcaster broadcaster;
    private MessageHandler console;

    private InetAddress addressLocal, addressVME;
    private int vmePort;
    private DatagramSocket  socketSend,  socketReceive;

    private DatagramPacket packetIn;
    private byte[] bufferIn=new byte[MAX_PACKET_SIZE];
    ByteArrayInputStream messageBais;
    DataInputStream messageDis;

    boolean active;        //
    //boolean verbose;        //do we print out messages received
    private int [] scalerValues;    //scaler values, loaded when a scaler packet is received.
    private int [] counterValues;    //counter values, loaded when a counter packet is received.

    /** Creates the instance of this class for handling IP communications with
     * the VME front end computer.
     * @param jamMain main class
     * @param jamCommand class that handles main window GUI events
     * @param broadcaster class that distributes Jam-wide messages
     * @param console class that takes text input from the user
     */
    public VMECommunication(JamMain jamMain, JamCommand jamCommand, Broadcaster broadcaster, MessageHandler console) {
        this.jamMain=jamMain;
        this.jamCommand=jamCommand;
        this.broadcaster=broadcaster;
        this.console=console;
        active=false;
        //verbose=true;
    }

    /** Sets up networking.  Two udp sockets are created: one for receiving and one
     * for sending.
     * A daemon is also set up for receiving.
     * @throws JamException if something goes wrong
     */
    public void setup()  throws JamException {
        int portSend=PORT_SEND;
        int portRecv=PORT_RECV;
        vmePort=PORT_VME_SEND;
        if (!active) { //FIXME maybe should check for null sockets instead
            try {//ceate a ports to send and receive
                addressLocal  =InetAddress.getByName(LOCAL_IP);
            } catch (UnknownHostException ue) {
                throw new JamException("Unknown local host "+LOCAL_IP+"  [VMECommnunications]");
            }
            try {
                addressVME    =InetAddress.getByName(VME_IP);
            } catch (UnknownHostException ue){
                throw new JamException("Unknown VME host "+VME_IP+"  [VMECommnunications]");
            }
            try {
                socketSend    =new DatagramSocket(portSend, addressLocal);
            } catch (BindException be) {
                throw new JamException("Binding send socket (another Jam online running) [VMEcommunications]");
            } catch (SocketException se) {
                throw new JamException("Creating send Socket [VMECommunications]");
            }
            try {
                socketReceive =new DatagramSocket(portRecv, addressLocal);
            } catch (BindException be) {
                throw new JamException("Binding receive socket (another Jam online running) [VMEcommunications]");
            } catch (SocketException se) {
                throw new JamException("Creating receive socket [VMECommunications]");
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
     * @param o additional parameter from <CODE>Observable</CODE> object
     */
    public void update(Observable observable, Object o){
        BroadcastEvent be=(BroadcastEvent)o;
        int command=be.getCommand();
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

    public void openFile(String file) throws JamException{
        this.VMEsend(OPENFILE+file);
    }
    
    /** Tells the front end to start acquisiton.
     * @throws JamException if there's a problem while trying to send the message
     */
    public void VMEstart() throws JamException {
        this.VMEsend(START);
    }

    /** Tells the front end to stop acquisiton, which also flushes out a buffer.
     * @throws JamException if there's a problem while trying to send the message
     */
    public void VMEstop() throws JamException {
        this.VMEsend(STOP);
    }

    /**
     * Tells the front end to stop acquisiton and end the run,
     * which flushes out the data buffer with an appended end-run marker.
     */
    public void end() {
        try {
            this.VMEsend(END);
        } catch (JamException je) {
            console.errorOutln(je.getMessage());
        }
    }

    /**
     * Tells the front end to flush out the data buffer, and send the contents.
     */
    public void flush() {
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
        try {
            this.VMEsend(RUN_SCALER);
        } catch (JamException je) {
            console.errorOutln(je.getMessage());
        }
    }

    /**
     * Tells the VME to clear the scalers and send a reply: OK or ERROR.
     */
    public void clearScalers(){
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
        try {
            this.VMEsend(COUNT_READ);
        } catch (JamException je) {
            console.errorOutln(je.getMessage());
        }
    }

    /**
     * Tells the VME to zero the counters and send a reply: OK or ERROR.
     */
    public void zeroCounters() {
        try {
            this.VMEsend(COUNT_ZERO);
        } catch (JamException je) {
            console.errorOutln(je.getMessage());
        }
    }

    /**
     * Tells the VME whether to print out debugging statements.
     */
    public void debug(boolean state){
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
     * Tells the VME whether to send verbose verbose status messages, which
     * Jam automatically prints on the console.
     */
    public void verbose(boolean state){
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
     */
    public void setupCamac(CamacCommands camacCommands) throws JamException {
        sendCNAFList(CNAF_INIT, camacCommands.getInitCommands()); //load initialize CNAFs
        sendCNAFList(CNAF_EVENT, camacCommands.getEventCommands()); //load eventCNAFs
        sendCNAFList(CNAF_SCALER, camacCommands.getScalerCommands() );  //load scaler CNAFs
        sendCNAFList(CNAF_CLEAR, camacCommands.getClearCommands());    //load clear CNAFs
        this.VMEsend(RUN_INIT);      //initialize camac
        console.messageOutln("Loaded CAMAC command lists, and initialized VME.");
    }

    public void setupVME_Map(VME_Map vmeMap) throws JamException {
        String temp="";
        VME_Channel [] eventParams = vmeMap.getEventParameters();
        Map hRanges = vmeMap.getV775Ranges();
        int numRanges = 0;
        if (eventParams.length > 0) {
            int totalParams = eventParams.length;
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
                Iterator i=hRanges.entrySet().iterator();
                /*while (eb.hasMoreElements()) {
                    int base = ((Integer)eb.nextElement()).intValue();
                    temp += "0x"+Integer.toHexString(base)+" "+er.nextElement()+"\n";
                }*/
                while (i.hasNext()){
                	Map.Entry next=(Map.Entry)i.next();
                	int base = ((Integer)next.getKey()).intValue();
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

    public void sendScalerInterval(int seconds) throws JamException {
        String message= seconds+"\n\0";
        stringPacketDump(SCALER_INTERVAL,message);
        VMEsend(SCALER_INTERVAL,message);
    }

    /**
     * Method which is used to send all packets containing a string to the VME crate.
     *
     * @parameter message   string to send
     */
    private void VMEsend(String message) throws JamException {
        VMEsend(VMECommunication.OK, message);
    }

    /**
     * Method which is used to send all packets containing a string to the VME crate.
     *
     * @param status one of OK, SCALER, ERROR, CNAF, COUNTER, VME_ADDRESSES or SCALER_INTERVAL
     * @param message string to send
     */
    private void VMEsend(int status, String message)  throws JamException {
        DatagramPacket packetMessage;
        byte [] byteMessage = new byte [message.length()+5];
        for (int i=0;i<=2;i++){//zero first int
            byteMessage[i]=0;
        }
        if (! validStatus(status)) {
        	throw new JamException(getClass().getName()+".vmeSend() with invalid status: "+status);
        } 
        byteMessage[3]=(byte) status;
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
     * @parameter listName cnaf list to send
     * @parameter
     */
    private void sendCNAFList(String listName, List cnafList) throws JamException {
        int offset;
        int [] cnaf;

        byte [] byteName=new byte[COMMAND_SIZE];
        if(listName.length()>(COMMAND_SIZE-1)){
            throw new JamException("Command string length too long [VMECommunication]");
        }
        byte [] byteMessage=new byte [4+COMMAND_SIZE+4+cnafList.size()*CNAF_SIZE+1];
        //set first int to 3
        for (int i=0;i<=2;i++){
            byteMessage[i]=0;
        }
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
            offset=4+COMMAND_SIZE+4+CNAF_SIZE*i;
            cnaf=(int [])cnafList.get(i);
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
     * send out a packet
     */
    private void sendPacket(byte [] byteMessage) throws JamException {
        try {//create and send packet
            DatagramPacket packetMessage=new DatagramPacket(byteMessage, byteMessage.length, addressVME,
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
     * Thread method: is a deamon for receiving packets from the VME crate.
     * The first int of the packet is the status word.
     * It determines how the packet is to be handled.
     *
     * @author Ken Swartz
     * @author Dale Visser
     */
    public void run(){
        int status;          //first int of message tells what type of packet

        try {
            while (true) {//loop forever receiving packets
                packetIn = new DatagramPacket(bufferIn, bufferIn.length);
                socketReceive.receive(packetIn);
                messageBais = new ByteArrayInputStream(packetIn.getData());
                messageDis=new DataInputStream(messageBais);
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
            console.messageOutln("Network receive daemon stopped, need to restart Online [VMECommunication]");
        }
    }

    /**
     * Unpack a datagram with a message. Message packets have an ASCII character array
     * terminated with \0.
     */
    private String unPackMessage(DataInputStream messageDis) throws JamException {
        char [] errorChar = new char[MAX_MESSAGE_SIZE];
        String errorMessage="Undecypherable Message";
        int numChar=0;
        try {
            while( (errorChar[numChar]=(char)messageDis.readByte()) !='\0' ) {
                numChar++;
            }
            errorMessage=new String(errorChar, 0, numChar);
        } catch (IOException ioe) {
            throw new JamException ("Unable to unpack message datagram [VMECommnunication]");
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
     */
    private void unPackScalers(DataInputStream messageDis) throws JamException {
        try {
            int numScaler=messageDis.readInt();//number of scalers
            scalerValues=new int [numScaler];
            for (int i=0; i<numScaler;i++){
                scalerValues[i]=messageDis.readInt();
            }
        } catch (IOException ioe) {
            throw new JamException ("Unable to unpack scaler datagram [VMECommnunication]");
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
     */
    private void unPackCounters(DataInputStream messageDis) throws JamException {
        try {
            int numCounter=messageDis.readInt(); // number of counters
            counterValues=new int [COUNT_NUMBER];
            for (int i=0; i<numCounter;i++){
                counterValues[i]=messageDis.readInt();
            }
        } catch (IOException ioe) {
            throw new JamException ("Unable to unpack count datagram [VMECommnunication.unPackCounters()]");
        }
    }

    /**
     * Get the scaler values from the last read.
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
     * Checks whether the parameter is a valid value of a message status.
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
