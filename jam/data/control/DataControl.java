package jam.data.control;
import java.util.*;

/**
 * A class to do overall control of the Jam data classes.
 *
 * @author Ken Swartz
 */
public abstract class DataControl {
	private static List controllers = new Vector(5);

	/**
	 * Default constructor for implementation classes.
	 */
	public DataControl() {
		controllers.add(this);
	}
	/**
	 * Remove self from list of controllers
	 */
	public void finalize() {
		controllers.remove(this);
	}
	/**
	 * Setup all instances of <code>DataControl</code>.
	 */
	public static void setupAll() {
		for (int i = 0; i < controllers.size(); i++) {
			((DataControl) controllers.get(i)).setup();
		}
	}

	/**
	 * Setup the current instance of <code>DataControl</code>.
	 */
	public abstract void setup();
}
