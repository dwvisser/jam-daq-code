package jam.data;

import jam.global.Nameable;
import jam.global.Validator;

/**
 * Class that contains a <code>static</code>method to clear the lists of all the
 * data classes.
 */
public final class DataBase implements Validator {

	private static final DataBase INSTANCE = new DataBase();

	private DataBase() {
		super();
	}

	/**
	 * Get the singleton instance of this class.
	 * 
	 * @return the only instance of this class
	 */
	static public DataBase getInstance() {
		return INSTANCE;
	}

	/**
	 * Calls the <code>clearList</code> methods of the various data classes.
	 * 
	 * @see AbstractHistogram#clearList()
	 * @see Gate#clearList()
	 * @see Scaler#clearList()
	 * @see Monitor#clearList()
	 * @see DataParameter#clearList()
	 * @see GroupCollection#clear()
	 */
	public void clearAllLists() {
		Warehouse.getGroupCollection().clear();
		AbstractHistogram.clearList();
		Gate.clearList();
		Scaler.clearList();
		Monitor.clearList();
		DataParameter.clearList();
	}

	public boolean isValid(final Nameable candidate) {
		boolean rval = false;
		if (candidate instanceof Group) {
			rval = ((Group) candidate).isValid();
		} else if (candidate instanceof AbstractHistogram) {
			rval = AbstractHistogram.isValid((AbstractHistogram) candidate);
		} else if (candidate instanceof Gate) {
			rval = Gate.isValid((Gate) candidate);
		}
		return rval;
	}
}
