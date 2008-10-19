package jam.data;

import jam.global.BroadcastEvent;
import jam.global.Broadcaster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing an individual scaler in the experiment.
 * 
 * @author Ken Swartz
 * @version 0.9
 * @since JDK 1.1
 */

public final class Scaler implements DataElement {

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
	}

	/**
	 * Returns the scaler with the specified name.
	 * 
	 * @param name
	 *            the name of the desired scaler
	 * @return the scaler with the specified name
	 */
	public static Scaler getScaler(final String name) {
		return TABLE.get(name);
	}

	/**
	 * Get the list of scalers
	 * 
	 * @return the list of all scalers
	 */
	public static List<Scaler> getScalerList() {
		return Collections.unmodifiableList(LIST);
	}

	/**
	 * Update all the scaler values. The value indexs refer to the scaler
	 * number.
	 * 
	 * @param inValue
	 *            the list of all the new values for the scalers
	 */
	public static void update(final List<Integer> inValue) {
		/* check we do not try to update mores scalers than there are */
		final int numScalers = Math.min(inValue.size(), LIST.size());
		for (int i = 0; i < numScalers; i++) {
			final Scaler scaler = LIST.get(i);
			scaler.setValue(inValue.get(scaler.getNumber()));
		}
		BROADCASTER.broadcast(BroadcastEvent.Command.SCALERS_UPDATE);
	}

	private transient final String name; // name of scaler

	private transient final int number; // number in list

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
	Scaler(final String nameIn, final String uniqueName, final int idNum) {
		super();
		name = nameIn;
		number = idNum;
		TABLE.put(uniqueName, this);
		LIST.add(this);
	}

	public double getCount() {
		synchronized (this) {
			return value;
		}
	}

	public Type getElementType() {
		return Type.SCALER;
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
	public int getValue() {
		synchronized (this) {
			return value;
		}
	}

	/**
	 * Sets this scaler's value.
	 * 
	 * @param valueIn
	 *            the new value for this scaler
	 */
	public void setValue(final int valueIn) {
		synchronized (this) {
			value = valueIn;
		}
	}

	public int getDimensionality() {
		return 1;
	}
}
