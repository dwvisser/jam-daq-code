package jam.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Class for storing and sending CNAF commands to the crate controller. In the
 * user's sort file, an instance of <code>CamacCommands</code> is created, and
 * the CNAF command lists defined one by one.
 * </p>
 * 
 * <p>
 * The five command list types are:
 * </p>
 * 
 * <ul>
 * <li>event</li>
 * <li>init</li>
 * <li>scaler</li>
 * <li>user</li>
 * <li>clear</li>
 * </ul>
 * 
 * @see jam.sort.SortRoutine#cnafCommands
 */
public class CamacCommands {
	private transient final List<int[]> eventCmds = new ArrayList<int[]>();

	private transient final List<int[]> initCommands = new ArrayList<int[]>();

	private transient final List<int[]> scalerCmds = new ArrayList<int[]>();

	private transient final List<int[]> clearCmds = new ArrayList<int[]>();

	private transient final SortRoutine sortRoutine;

	private transient int eventSize = 0;

	/**
	 * <p>
	 * The various lists of CNAF commands are initialized by the constructor and
	 * initially contain no commands.
	 * </p>
	 * 
	 * @param sorter
	 *            <code>SortRoutine</code> to which this object belongs
	 * @see jam.sort.SortRoutine#cnafCommands
	 */
	public CamacCommands(SortRoutine sorter) {
		super();
		sortRoutine = sorter;
	}

	/**
	 * Adds the command specified by the arguments to the next position in the
	 * specified event command list.
	 * 
	 * @param crate
	 *            crate number
	 * @param number
	 *            slot containing device to receive the command
	 * @param address
	 *            address number to receive the command
	 * @param function
	 *            code indicating the action to be taken
	 * @param data
	 *            any data to be passed to the module
	 * @throws SortException
	 *             if invalid CNAF
	 * @return index in eventData array that the <CODE>SortRoutine</CODE>
	 *         processes
	 */
	public int eventRead(final int crate, final int number, final int address,
			final int function, final int data) throws SortException {
		// paramId+1 since stream has 1 for first element
		final int paramId = eventCmds.size() + 1;
		eventSize++;
		eventCmds.add(cnaf(paramId, crate, number, address, function, data));
		sortRoutine.setEventSizeMode(SortRoutine.EventSizeMode.CNAF);
		return (paramId - 1);
	}

	/**
	 * Adds the command specified by the arguments to the next position in the
	 * only event command list.
	 * 
	 * @param crate
	 *            crate number
	 * @param number
	 *            slot containing device to receive the command
	 * @param address
	 *            address number to receive the command
	 * @param function
	 *            code indicating the action to be taken
	 * @throws SortException
	 *             if invalid CNAF
	 * @return index in event data array proccesed by the <CODE>SortRoutine</CODE>
	 */
	public int eventRead(final int crate, final int number, final int address,
			final int function) throws SortException {
		return eventRead(crate, number, address, function, 0);
	}

	/**
	 * Adds the command specified by the arguments to the next position in the
	 * only event command list, NO PARAMETER ASSIGNED.
	 * 
	 * @param crate
	 *            crate number
	 * @param number
	 *            slot containing device to receive the command
	 * @param address
	 *            address number to receive the command
	 * @param function
	 *            code indicating the action to be taken
	 */
	public void eventCommand(final int crate, final int number,
			final int address, final int function) {
		eventCmds.add(cnaf(0, crate, number, address, function, 0));
	}

	/**
	 * Adds the command specified by the arguments to the next position in the
	 * init command list.
	 * 
	 * @param crate
	 *            crate number
	 * @param number
	 *            slot containing device to receive the command
	 * @param address
	 *            address number to receive the command
	 * @param function
	 *            code indicating the action to be taken
	 * @param data
	 *            any data sent with command to module
	 */
	public void init(final int crate, final int number, final int address,
			final int function, final int data) {
		initCommands.add(cnaf(0, crate, number, address, function, data));
	}

	/**
	 * Adds the command specified by the arguments to the next position in the
	 * init command list.
	 * 
	 * @param crate
	 *            crate number
	 * @param number
	 *            slot containing device to receive the command
	 * @param address
	 *            address number to receive the command
	 * @param function
	 *            code indicating the action to be taken
	 */
	public void init(final int crate, final int number, final int address,
			final int function) {
		initCommands.add(cnaf(0, crate, number, address, function, 0));
	}

	/**
	 * Adds the command specified by the arguments to the next position in the
	 * scaler command list.
	 * 
	 * @param crate
	 *            crate number
	 * @param number
	 *            slot containing device to receive the command
	 * @param address
	 *            address number to receive the command
	 * @param function
	 *            code indicating the action to be taken
	 * @param data
	 *            any data sent with the command to the module
	 * @return number of scaler
	 */
	public int scaler(final int crate, final int number, final int address,
			final int function, final int data) {
		final int scalerId = scalerCmds.size();
		scalerCmds.add(cnaf(scalerId, crate, number, address, function, data));
		return scalerId + 1;
	}

	/**
	 * Adds the command specified by the arguments to the next position in the
	 * scaler command list.
	 * 
	 * @param crate
	 *            crate number
	 * @param number
	 *            slot containing device to receive the command
	 * @param address
	 *            address number to receive the command
	 * @param function
	 *            code indicating the action to be taken
	 * @return number of scaler
	 */
	public int scaler(final int crate, final int number, final int address,
			final int function) {
		return scaler(crate, number, address, function, 0);
	}

	/**
	 * Adds the command specified by the arguments to the next position in the
	 * clear command list.
	 * 
	 * @param crate
	 *            crate number
	 * @param number
	 *            slot containing device to receive the command
	 * @param address
	 *            address number to receive the command
	 * @param function
	 *            code indicating the action to be taken
	 * @param data
	 *            any data sent with the command to the module
	 */
	public void clear(final int crate, final int number, final int address,
			final int function, final int data) {
		clearCmds.add(cnaf(0, crate, number, address, function, data));
	}

	/**
	 * Adds the command specified by the arguments to the next position in the
	 * clear command list.
	 * 
	 * @param crate
	 *            crate number
	 * @param number
	 *            slot containing device to receive the command
	 * @param address
	 *            address number to receive the command
	 * @param function
	 *            code indicating the action to be taken
	 */
	public void clear(final int crate, final int number, final int address,
			final int function) {
		clearCmds.add(cnaf(0, crate, number, address, function, 0));
	}

	/**
	 * Get the list of init cnafs
	 * 
	 * @return list of
	 */
	public List<int[]> getInitCommands() {
		return Collections.unmodifiableList(initCommands);
	}

	/**
	 * Get the list of event cnafs
	 * 
	 * @return list of event CNAF commands
	 */
	public List<int[]> getEventCommands() {
		return Collections.unmodifiableList(eventCmds);
	}

	/**
	 * Get the list of scaler cnafs
	 * 
	 * @return list of scaler read CNAF commands
	 */
	public List<int[]> getScalerCommands() {
		return Collections.unmodifiableList(scalerCmds);
	}

	/**
	 * Get the list of clear cnafs
	 * 
	 * @return list of "clear" CNAF commands
	 */
	public List<int[]> getClearCommands() {
		return Collections.unmodifiableList(clearCmds);
	}

	/**
	 * Get the event size for the default stream
	 * 
	 * @return the number of parameters per event
	 */
	public int getEventSize() {
		return eventSize;
	}

	/*
	 * non-javadoc: Used internally to package the parts of a command into a
	 * byte array.
	 */
	private int[] cnaf(final int idParam, final int crate, final int number,
			final int address, final int function, final int data) {
		int[] out = new int[6];
		out[0] = idParam;
		out[1] = crate;
		out[2] = number;
		out[3] = address;
		out[4] = function;
		out[5] = data;
		return out;
	}
}