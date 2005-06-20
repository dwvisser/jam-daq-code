package jam.data.control;

import jam.global.Broadcaster;
import jam.global.JamStatus;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * A class to do overall control of the Jam data classes.
 * 
 * @author Ken Swartz
 */
public abstract class AbstractControl extends JDialog implements Observer {

	/**
	 * Default number of rows to display
	 */
	private final int MAX_INITIAL_DISPLAY = 15;

	private static List<AbstractControl> controllers = Collections
			.synchronizedList(new ArrayList<AbstractControl>());

	/**
	 * Reference to instance of JamStatus.
	 */
	protected static final JamStatus STATUS = JamStatus.getSingletonInstance();

	/**
	 * Reference to instance of Broadcaster.
	 */
	protected static final Broadcaster BROADCASTER = Broadcaster
			.getSingletonInstance();

	/**
	 * Default constructor for implementation classes.
	 * 
	 * @param title
	 *            title of dialog
	 * @param modal
	 *            whether dialog is modal
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
		/*
		 * do-nothing implementation for those subclasses that don't care
		 */
	}

	/**
	 * Calculate dimension for a dialog that can scroll a number of field rows
	 * 
	 * @param dialog
	 *            Dialog
	 * @param panelField
	 *            Panel for a field
	 * @param border
	 *            Border for a field panel
	 * @param numberFields
	 *            Number of fields
	 * @return New Dialog size
	 */
	protected Dimension calculateScrollDialogSize(JDialog dialog,
			JPanel panelField, int border, int numberFields) {
		return calculateScrollDialogSize(dialog, panelField, border,
				numberFields, MAX_INITIAL_DISPLAY);
	}

	/**
	 * Calculate dimension for a dialog that can scroll a number of field rows
	 * 
	 * @param dialog
	 *            Dialog
	 * @param panelField
	 *            Panel for a field
	 * @param border
	 *            Border for a field panel
	 * @param numberFields
	 *            Number of fields
	 * @param maxNumField
	 *            Maximum number of fields
	 * @return New Dialog size
	 */
	protected Dimension calculateScrollDialogSize(JDialog dialog,
			JPanel panelField, int border, int numberFields, int maxNumField) {

		Dimension dimDialog = null;
		// Size of one parameter
		if (numberFields >= 1) {

			if (numberFields > maxNumField) {
				dimDialog = dialog.getSize();
				Dimension dimParam = panelField.getSize();
				int height = dimParam.height;
				dimDialog.height = dimDialog.height - (height + border)
						* (numberFields - maxNumField) - border;
			} else {
				dimDialog = dialog.getSize();
			}
		}
		return dimDialog;

	}

}
