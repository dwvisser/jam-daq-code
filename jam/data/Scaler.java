package jam.data;

import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.util.StringUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class representing an individual scaler in the experiment.
 * 
 * @author Ken Swartz
 * @version 0.9
 * @since JDK 1.1
 */

public class Scaler implements DataElement {

	private static final Broadcaster BROADCASTER = Broadcaster
			.getSingletonInstance();

	private static final List<Scaler> LIST = Collections
			.synchronizedList(new ArrayList<Scaler>());

	/**
	 * Limit on name length.
	 */
	public final static int NAME_LENGTH = 16;

	private static final Map<String, Scaler> TABLE = Collections
			.synchronizedMap(new HashMap<String, Scaler>());

	/**
	 * Clears the list of all scalers.
	 */
	public static void clearList() {
		TABLE.clear();
		LIST.clear();
		/* run garbage collector to free memory */
		System.gc();
	}

	/**
	 * Returns the scaler with the specified name.
	 * 
	 * @param name
	 *            the name of the desired scaler
	 * @return the scaler with the specified name
	 */
	public static Scaler getScaler(String name) {
		return (Scaler) TABLE.get(name);
	}

	/**
	 * Get the list of scalers
	 * 
	 * @return the list of all scalers
	 */
	public static List getScalerList() {
		return Collections.unmodifiableList(LIST);
	}

	/**
	 * Update all the scaler values. The value indexs refer to the scaler
	 * number.
	 * 
	 * @param inValue
	 *            the list of all the new values for the scalers
	 */
	public static void update(int[] inValue) {
		/* check we do not try to update mores scalers than there are */
		final int numScalers = Math.min(inValue.length, LIST.size());
		for (int i = 0; i < numScalers; i++) {
			final Scaler scaler = (Scaler) LIST.get(i);
			scaler.setValue(inValue[scaler.getNumber()]);
		}
		BROADCASTER.broadcast(BroadcastEvent.Command.SCALERS_UPDATE);
	}

	private transient final String name; // name of scaler

	private transient final int number; // number in list

	private transient final String uniqueName; // unique name in all groups

	private int value; // value of scaler

	/**
	 * Creates a new scaler with an assigned name and number.
	 * 
	 * @param group
	 *            for this scaler to belong to
	 * @param nameIn
	 *            name of the scaler, which must be <=16 characters
	 * @param idNum
	 *            number of scaler, most often the same as the register number
	 *            in a CAMAC scaler unit
	 * @throws IllegalArgumentException
	 *             if name ><code>NAME_LENGTH</code> characters
	 */
	public Scaler(Group group, String nameIn, int idNum) {
		final StringUtilities stringUtil = StringUtilities.instance();
		// Set of names of gates for histogram this gate belongs to
		Set<String> scalerNames = new TreeSet<String>();
		for (Scaler scaler : group.getScalerList()){
			scalerNames.add(scaler.getName());
		}
		name = stringUtil.makeUniqueName(nameIn, scalerNames, NAME_LENGTH);
		uniqueName = group.getName() + "/" + name;
		group.addScaler(this);
		number = idNum;
		/* Add to list of scalers */
		TABLE.put(uniqueName, this);
		LIST.add(this);
	}

	public synchronized double getCount() {
		return value;
	}

	public int getElementType() {
		return DataElement.ELEMENT_TYPE_SCALER;
	}

	/**
	 * Returns the name of this scaler.
	 * 
	 * @return the name of this scaler
	 */

	public String getName() {
		return name;
	}

	/**
	 * Returns the number of this scaler.
	 * 
	 * @return the number of this scaler
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * Returns this scaler's value.
	 * 
	 * @return the value of this scaler
	 */
	public synchronized int getValue() {
		return value;
	}

	/**
	 * Sets this scaler's value.
	 * 
	 * @param valueIn
	 *            the new value for this scaler
	 */
	public synchronized void setValue(int valueIn) {
		value = valueIn;
	}

}
