package jam.ui;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Action for a cancel button on a dialog window.
 * 
 * @author Dale Visser
 * 
 */
@SuppressWarnings("serial")
public final class WindowCancelAction extends AbstractAction {

	private transient Window window;
	private transient Canceller canceller;

	private enum Type {
		/**
		 * simply dispose a dialog
		 */
		dialog,

		/**
		 * call a canceller's cancel method
		 */
		canceller
	}

    private transient Type type;

	private WindowCancelAction() {
		super("Cancel");
	}

	/**
	 * 
	 * @param window
	 *            to dispose of when action is fired
	 */
	public WindowCancelAction(final Window window) {
		this();
		type = Type.dialog;
		this.window = window;
		this.putValue(SHORT_DESCRIPTION, "Close this dialog.");
	}

	/**
	 * 
	 * @param canceller
	 *            to call when action is fired
	 */
	public WindowCancelAction(final Canceller canceller) {
		this();
		type = Type.canceller;
		this.canceller = canceller;
	}

	public void actionPerformed(final ActionEvent event) {
		if (type.equals(Type.dialog)) {
			window.dispose();
		} else {
			canceller.cancel();
		}
	}

}
