package jam.data.control;

import java.awt.Dimension;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JPanel;

import jam.global.Broadcaster;

/**
 * A class to do overall control of the Jam data classes.
 * 
 * @author Ken Swartz
 */
public abstract class AbstractControl extends JDialog implements PropertyChangeListener {

	/**
	 * Reference to instance of Broadcaster.
	 */
	protected transient final Broadcaster broadcaster;

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
	 * @param frame parent frame
	 * @param title
	 *            title of dialog
	 * @param modal
	 *            whether dialog is modal
     * @param broadcaster broadcasts events to registered listeners
	 */
	protected AbstractControl(final Frame frame, final String title,
			final boolean modal, final Broadcaster broadcaster) {
		super(frame, title, modal);
		controllers.add(this);
		this.broadcaster = broadcaster;
		broadcaster.addPropertyChangeListener(this);
		Runtime.getRuntime().addShutdownHook(new AbstractControl.ControlCleanup(this));
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

	public class ControlCleanup extends Thread {

		private AbstractControl control;

		public ControlCleanup(AbstractControl control) {
			this.control = control;
		}

		public void run() {
			AbstractControl.controllers.remove(this.control);
			this.control.broadcaster.removePropertyChangeListener(this.control);
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		/*
		 * do-nothing implementation for those subclasses that don't care
		 */
	}

}
