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
public abstract class AbstractControl extends JDialog implements Observer {
	private static List controllers = Collections.synchronizedList(new ArrayList());
	
	/**
	 * Reference to instance of JamStatus.
	 */
	protected static final JamStatus STATUS=JamStatus.getSingletonInstance();
	
	/**
	 * Reference to instance of Broadcaster.
	 */
	protected static final Broadcaster BROADCASTER=Broadcaster.getSingletonInstance();

	/**
	 * Default constructor for implementation classes.
	 * 
	 * @param title title of dialog
	 * @param modal whether dialog is modal
	 */
	protected AbstractControl(String title, boolean modal) {
		super(STATUS.getFrame(), title, modal);
		controllers.add(this);
		BROADCASTER.addObserver(this);
	}
	
	/**
	 * Remove self from list of controllers
	 */
	protected void finalize() throws Throwable {
		controllers.remove(this);
		BROADCASTER.deleteObserver(this);
		super.finalize();
	}
	
	/**
	 * Setup all instances of <code>AbstractControl</code>.
	 */
	public static void setupAll() {
		for (int i = 0; i < controllers.size(); i++) {
			((AbstractControl) controllers.get(i)).doSetup();
		}
	}

	/**
	 * Setup the current instance of <code>AbstractControl</code>.
	 */
	public abstract void doSetup();

	public void update(Observable observable, Object object) {
		/* do-nothing implementation for those subclasses that
		 * don't care */
	}
		 
}
