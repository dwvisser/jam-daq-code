package jam.data.control;

import jam.global.JamStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JDialog;

/**
 * A class to do overall control of the Jam data classes.
 *
 * @author Ken Swartz
 */
public abstract class DataControl extends JDialog {
	private static List controllers = Collections.synchronizedList(new ArrayList());
	protected static JamStatus status=JamStatus.instance();

	/**
	 * Default constructor for implementation classes.
	 */
	protected DataControl(String title, boolean modal) {
		super(status.getFrame(), title, modal);
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
