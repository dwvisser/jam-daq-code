package jam.commands;

import javax.swing.JDialog;

import jam.global.CommandListenerException;

/**
 * Commands that are for showing <code>JDialog</code>'s. Dialogs simply extend
 * this and assign a reference to
 * <code>dialog</code> in <code>initCommand()</code>.
 * 
 * @author Ken Swartz
 */
@SuppressWarnings("serial")
public class AbstractShowDialog extends AbstractCommand {

	AbstractShowDialog() {
		super();
	}

	/**
	 * @see AbstractCommand#AbstractCommand(String)
	 * @param name of command
	 */
	protected AbstractShowDialog(final String name) {
		super(name);
	}

	/**
	 * Dialog to show.
	 */
	protected transient JDialog dialog;

	@Override
	protected final void execute(final Object[] cmdParams) {
		dialog.setVisible(true);
	}

	@Override
	protected final void executeParse(final String[] cmdTokens)
			throws CommandListenerException {
		execute(null);
	}

	/**
	 * Executes superclass's method of the same name, then disposes the dialog
	 * if its show command is disabled.
	 * 
	 * @see javax.swing.Action#setEnabled(boolean)
	 */
	@Override
	public final void setEnabled(final boolean state) {
		super.setEnabled(state);
		if (!state) {
			dialog.dispose();
		}
	}
}
