package jam.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Class for storing and sending CNAF commands to the crate
 * controller.  In the user's sort file, an instance of
 * <code>CamacCommands</code> is created, and the CNAF command lists
 * defined one by one.</p>
 * 
 * <p>The five command list types are: </p>
 * 
 * <ul>
 *  <li>event</li>
 *  <li>init</li>
 *  <li>scaler</li>
 *  <li>user</li>
 *  <li>clear</li>
 * </ul>
 * 
 * @see jam.sort.SortRoutine#cnafCommands
 */
public class CamacCommands {
    private final List eventCommands=new ArrayList();
    private final List initCommands=new ArrayList();
	private final List scalerCommands=new ArrayList();
	private final List clearCommands=new ArrayList();
	private final SortRoutine sortRoutine;
    private int eventSize=0;
    private int paramId=0;
    private int scalerId;

    /** 
     * <p>The various lists of CNAF commands are initialized by the 
     * constructor and initially contain no commands.</p>
     * 
     * @param sr <code>SortRoutine</code> to which this object belongs
     * @see jam.sort.SortRoutine#cnafCommands
     */
    public CamacCommands(SortRoutine sr) {
        sortRoutine = sr;
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
        eventSize++;
        eventCommands.add(cnaf(paramId, crate, number, address,
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
        eventCommands.add(cnaf(0, crate, number, address, function,  0));
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
        initCommands.add(cnaf(0, crate,number,address,function, data));
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
        initCommands.add(cnaf(0, crate,number,address,function, 0));
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
        scalerCommands.add(cnaf(scalerId, crate,number,address,function, data));
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
        clearCommands.add(cnaf(0, crate, number, address, function, data));
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
        clearCommands.add(cnaf(0, crate,number,address,function, 0));
    }
    
    /** Get the list of init cnafs
     * @return list of
     */
    public List getInitCommands(){
        return Collections.unmodifiableList(initCommands);
    }
    
    /** Get the list of event cnafs
     * @return list of event CNAF commands
     */
    public List getEventCommands(){
        return Collections.unmodifiableList(eventCommands);
    }
    
    /** Get the list of scaler cnafs
     * @return list of scaler read CNAF commands
     */
    public List getScalerCommands(){
        return Collections.unmodifiableList(scalerCommands);
    }
    
    /** Get the list of clear cnafs
     * @return list of "clear" CNAF commands
     */
    public List getClearCommands(){
        return Collections.unmodifiableList(clearCommands);
    }
    
    /** Get the event size for the default stream
     * @return the number of parameters per event
     */
    public int getEventSize(){
        return eventSize;
    }
    
    /* non-javadoc:
     * Used internally to package the parts of a command into a byte array.
     */
    private int [] cnaf(int paramId, int crate, int number, int address, int function, int data){
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