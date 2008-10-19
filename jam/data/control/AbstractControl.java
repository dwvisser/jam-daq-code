package jam.data.control;

import jam.global.Broadcaster;
import jam.global.JamStatus;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * A class to do overall control of the Jam data classes.
 * 
 * @author Ken Swartz
 */
public abstract class AbstractControl extends JDialog implements Observer {

	/**
	 * Reference to instance of Broadcaster.
	 */
	protected static final Broadcaster BROADCASTER = Broadcaster
			.getSingletonInstance();

	/**
	 * Default number of rows to display
	 */

	private static List<AbstractControl> controllers = Collections
			.synchronizedList(new ArrayList<AbstractControl>());

	/**
	 * Logger for all subclasses of AbstractControl.
	 */
	protected static final Logger LOGGER = Logger
			.getLogger(AbstractControl.class.getPackage().getName());

	/**
	 * Reference to instance of JamStatus.
	 */
	protected static final JamStatus STATUS = JamStatus.getSingletonInstance();

	/**
	 * Setup all instances of <code>AbstractControl</code>.
	 */
	public static void setupAll() {
		for (AbstractControl control : controllers) {
			control.doSetup();
		}
	}

	/**
	 * Default constructor for implementation classes.
	 * 
	 * @param title
	 *            title of dialog
	 * @param modal
	 *            whether dialog is modal
	 */
	protected AbstractControl(final String title, final boolean modal) {
		super(STATUS.getFrame(), title, modal);
		controllers.add(this);
		BROADCASTER.addObserver(this);
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
	protected Dimension calculateScrollDialogSize(final JDialog dialog,
			final JPanel panelField, final int border, final int numberFields) {
		final int maxInitDisplay = 15;
		return calculateScrollDialogSize(dialog, panelField, border,
				numberFields, maxInitDisplay);
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
	protected Dimension calculateScrollDialogSize(final JDialog dialog,
			final JPanel panelField, final int border, final int numberFields,
			final int maxNumField) {
		Dimension dimDialog = null;
		// Size of one parameter
		if (numberFields >= 1) {
			if (numberFields > maxNumField) {
				dimDialog = dialog.getSize();
				final Dimension dimParam = panelField.getSize();
				final int height = dimParam.height;
				dimDialog.height = dimDialog.height - (height + border)
						* (numberFields - maxNumField) - border;
			} else {
				dimDialog = dialog.getSize();
			}
		}
		return dimDialog;
	}

	/**
	 * Setup the current instance of <code>AbstractControl</code>.
	 */
	public abstract void doSetup();

	/**
	 * Remove self from list of controllers
	 */
	@Override
	protected void finalize() throws Throwable {
		controllers.remove(this);
		BROADCASTER.deleteObserver(this);
		super.finalize();// NOPMD
	}

	public void update(final Observable observable, final Object object) {
		/*
		 * do-nothing implementation for those subclasses that don't care
		 */
	}

}
