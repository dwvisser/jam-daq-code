package jam.data;

/**
 * Class that contains a <code>static</code>method to clear the lists
 * of all the data classes.
 */
public class DataBase {
	
	private static final DataBase dataBase=new DataBase();
	
	private DataBase(){
		super();
	}
	
	/**
	 * Get the singleton instance of this class.
	 * 
	 * @return the only instance of this class
	 */
	static public DataBase getInstance(){
		return dataBase;
	}

	/**
	 * Calls the <code>clearList</code> methods of the various data 
	 * classes.
	 *
	 * @see Histogram#clearList()
	 * @see Gate#clearList()
	 * @see Scaler#clearList()
	 * @see Monitor#clearList()
	 * @see DataParameter#clearList()
	 */
	public void clearAllLists() {
		Histogram.clearList();
		Gate.clearList();
		Scaler.clearList();
		Monitor.clearList();
		DataParameter.clearList();
		System.gc();
	}
}
