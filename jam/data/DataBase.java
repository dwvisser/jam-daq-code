 package jam.data;
 import java.util.Vector;

/**
 * Class that contains a <code>static</code>method to clear the lists of all the data classes.
 */
public class DataBase {

    /**
     * Constructor - never used.
     */
    public DataBase (){
    }

    /**
     * Calls the <code>clearList</code> methods of the various data classes.
     *
     * @see Histogram#clearList()
     * @see Gate#clearList()
     * @see Scaler#clearList()
     * @see Monitor#clearList()
     * @see DataParameter#clearList()
     */
    public static void  clearAllLists(){
      Histogram.clearList();
      Gate.clearList();
      Scaler.clearList();
      Monitor.clearList();
      DataParameter.clearList();
      System.gc();
    }
}
