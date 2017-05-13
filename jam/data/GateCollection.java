package jam.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Used for accessing gates in memory.
 * 
 * @author Dale Visser
 * 
 */
public final class GateCollection {

	private transient final int dimensions;

	/**
	 * gates that belong to this histogram
	 */
	private transient final List<DataElement> gates = new ArrayList<>();

	GateCollection(final int dim) {
		this.dimensions = dim;
	}

	/**
	 * Add a <code>Gate</code> to this histogram.
	 * 
	 * @param gate
	 *            to add
	 * @throws UnsupportedOperationException
	 *             if a gate of a different type is given
	 */
	public void addGate(final DataElement gate) {
		if (gate.getDimensionality() == dimensions) {
			synchronized (this) {
				if (!gates.contains(gate)) {
					gates.add(gate);
				}
			}
		} else {
			throw new UnsupportedOperationException("Can't add "
					+ gate.getDimensionality() + "D gate to " + dimensions
					+ "D histogram.");
		}
	}

	protected void clear() {
		gates.clear();
	}

	/**
	 * Returns the list of gates that belong to this histogram.
	 * 
	 * @return the list of gates that belong to this histogram
	 */
	public List<DataElement> getGates() {
		return Collections.unmodifiableList(gates);
	}

	/**
	 * @param gate
	 *            that we're wondering about
	 * @return whether this histogram has the given gate
	 */
	public boolean hasGate(final DataElement gate) {
		boolean rval = false;// default return value
		synchronized (this) {
			for (DataElement gate1 : gates) {
				if (gate1 == gate) {
					rval = true;
					break;// drop out of loop
				}
			}
		}
		return rval;
	}
}
