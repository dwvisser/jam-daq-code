package jam.data.control;

import jam.global.Broadcaster;
import jam.global.JamStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JDialog;

/**
 * A class to do overall control of the Jam data classes.
 *
 * @author Ken Swartz
 */
public abstract class DataControl extends JDialog implements Observer {
	private static List controllers = Collections.synchronizedList(new ArrayList());
	protected static final JamStatus status=JamStatus.instance();
	protected static final Broadcaster broadcaster=Broadcaster.getSingletonInstance();

	/**
	 * Default constructor for implementation classes.
	 */
	protected DataControl(String title, boolean modal) {
		super(status.getFrame(), title, modal);
		controllers.add(this);
		broadcaster.addObserver(this);
	}
	
	/**
	 * Remove self from list of controllers
	 */
	public void finalize() {
		controllers.remove(this);
		broadcaster.deleteObserver(this);
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

	public void update(Observable observable, Object o) {
		/* do-nothing implementation for those subclasses that
		 * don't care */
	}
		 
}
