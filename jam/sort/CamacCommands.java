/*
 */
package jam.sort;

import java.util.*;

/**
 * <p>Class for storing and sending CNAF commands to the crate controller (which may in fact be
 * the jam process using GPIB in the future).  In the user's sort file, an instance of
 * <code>CamacCommands</code> is created, and the CNAF command lists defined one by one.</p>
 * <p>The five command list types are: </p>
 * <ul>
 *  <li>event (1 or many types)</li>
 *  <li>init</li>
 *  <li>scaler</li>
 *  <li>user</li>
 *  <li>clear</li>
 * </ul>
 */
public class CamacCommands {
    List [] eventCommands;
    List initCommands, scalerCommands, userCommands, clearCommands;
    private int eventSize[];
    private int numberStreams, streamNumber, paramId, scalerId;
    private SortRoutine sortRoutine;

    /** <p>This constructor is passed the number of event types.  The most common and useful
     * examples of multiple event types are events with unique triggers.  For example,
     * scalers may be inserted into the event stream every 10 seconds by a unique trigger
     * in the electronics.  Or a beam monitor may send events that are independent of the
     * primary experimental equipment.</p>
     * <p>The various lists of CNAF commands are initialized by the constructor and initially
     * contain no commands.</p>
     * @param numberStreams number of different event types
     * @param sr <code>SortRoutine</code> to which this object belongs
     * @see jam.sort.SortRoutine
     */
    public CamacCommands(int numberStreams, SortRoutine sr) {
        this.numberStreams=numberStreams;
        sortRoutine = sr;
        //command lists
        initCommands=new Vector();
        eventCommands=new Vector[numberStreams];
        for (int i=0;i < numberStreams; i++) {
            eventCommands[i]=new Vector();
        }
        scalerCommands=new Vector();
        userCommands=new Vector();
        clearCommands=new Vector();
        eventSize=new int[numberStreams];
        streamNumber=0;
        paramId=0;
        scalerId=0;
    }
    
    /** Defaults contructor creates one event stream
     * @param sr <CODE>SortRoutine</CODE> to which this object belongs
     */
    public CamacCommands(SortRoutine sr) {
        this(1, sr);
        streamNumber=0;
    }

    /** ???
     * @param number ???
     */
    public void setStreamNumber(int number){
        streamNumber=number;
        paramId=0;
    }

    /** Adds the command specified by the arguments to the next position in the
     * specified event command list.
     * @param crate crate number
     * @param number slot containing device to receive the command
     * @param address address number to receive the command
     * @param function code indicating the action to be taken
     * @param data any data to be passed to the module
     * @throws SortException if invalid CNAF
     * @return index in eventData array that the <CODE>SortRoutine</CODE>
     * processes
     */
    public int eventRead(int crate, int number, int address, int function, 
    int data) throws SortException {
        //paramId+1 since stream has 1 for first element
        paramId++;
        eventSize[streamNumber]++;
        eventCommands[streamNumber].add(CNAF(paramId, crate, number, address,
        function,  data));
        sortRoutine.setEventSizeMode(SortRoutine.SET_BY_CNAF);
        return (paramId-1);	    
    }

    /** Adds the command specified by the arguments to the next position in the only
     * event command list.
     * @param crate crate number
     * @param number slot containing device to receive the command
     * @param address address number to receive the command
     * @param function code indicating the action to be taken
     * @throws SortException if invalid CNAF
     * @return index in event data array proccesed by the <CODE>SortRoutine</CODE>
     */
    public int eventRead(int crate, int number, int address, int function) throws SortException {
        return eventRead(crate, number, address, function, 0);
    }

    /**
     * Adds the command specified by the arguments to the next position in the only
     * event command list, NO PARAMETER ASSIGNED.
     *
     * @param crate crate number
     * @param number slot containing device to receive the command
     * @param address address number to receive the command
     * @param function code indicating the action to be taken
     */
    public void eventCommand(int crate, int number, int address, int function){
        eventCommands[streamNumber].add(CNAF(0, crate, number, address, function,  0));
    }

    /** Adds the command specified by the arguments to the next position in the
     * init command list.
     * @param crate crate number
     * @param number slot containing device to receive the command
     * @param address address number to receive the command
     * @param function code indicating the action to be taken
     * @param data any data sent with command to module
     */
    public void init(int crate, int number, int address, int function, int data){
        initCommands.add(CNAF(0, crate,number,address,function, data));
    }

    /**
     * Adds the command specified by the arguments to the next position in the
     * init command list.
     *
     * @param crate crate number
     * @param number slot containing device to receive the command
     * @param address address number to receive the command
     * @param function code indicating the action to be taken
     */
    public void init(int crate, int number, int address, int function){
        initCommands.add(CNAF(0, crate,number,address,function, 0));
    }
    /** Adds the command specified by the arguments to the next position in the
     * scaler command list.
     * @param crate crate number
     * @param number slot containing device to receive the command
     * @param address address number to receive the command
     * @param function code indicating the action to be taken
     * @param data any data sent with the command to the module
     * @return number of scaler
     */
    public int scaler(int crate, int number, int address, int function, int data){
        scalerCommands.add(CNAF(scalerId, crate,number,address,function, data));
        return scalerId++;
    }

    /** Adds the command specified by the arguments to the next position in the
     * scaler command list.
     * @param crate crate number
     * @param number slot containing device to receive the command
     * @param address address number to receive the command
     * @param function code indicating the action to be taken
     * @return number of scaler
     */
    public int scaler(int crate, int number, int address, int function){
        return scaler(crate, number, address, function, 0);
    }
    
    /** Adds the command specified by the arguments to the next position in the
     * clear command list.
     * @param crate crate number
     * @param number slot containing device to receive the command
     * @param address address number to receive the command
     * @param function code indicating the action to be taken
     * @param data any data sent with the command to the module
     */
    public void clear(int crate, int number, int address, int function, int data){
        clearCommands.add(CNAF(0, crate, number, address, function, data));
    }

    /**
     * Adds the command specified by the arguments to the next position in the
     * clear command list.
     *
     * @param crate crate number
     * @param number slot containing device to receive the command
     * @param address address number to receive the command
     * @param function code indicating the action to be taken
     */
    public void clear(int crate, int number, int address, int function){
        clearCommands.add(CNAF(0, crate,number,address,function, 0));
    }
    
    /** Get the list of init cnafs
     * @return list of
     */
    public List getInitCommands(){
        return initCommands;
    }
    
    /** Get the list of event cnafs
     * @return list of event CNAF commands
     */
    public List getEventCommands(){
        return eventCommands[0];
    }
    
    /** Get the list of scaler cnafs
     * @return list of scaler read CNAF commands
     */
    public List getScalerCommands(){
        return scalerCommands;
    }
    
    /** Get the list of clear cnafs
     * @return list of "clear" CNAF commands
     */
    public List getClearCommands(){
        return clearCommands;
    }
    
    /** Get the event size for the default stream
     * @return the number of parameters per event
     */
    public int getEventSize(){
        return eventSize[0];
    }
    
    /** Get the event size for a given stream
     * @param streamNumber which stream to get number of parameters for
     * @return number of parameters per event
     */
    public int getEventSize(int streamNumber){
        return eventSize[streamNumber];
    }
        
    /**
     * Used internally to package the parts of a command into a byte array.
     */
    private int [] CNAF(int paramId, int crate, int number, int address, int function, int data){
        int [] out = new int[6];
        out[0] = paramId;
        out[1] = crate;
        out[2] = number;
        out[3] = address;
        out[4] = function;
        out[5] = data;
        return out;
    }
}