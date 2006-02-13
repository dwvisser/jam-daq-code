package jam.commands;

import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 * Show the exit dialog.
 * 
 * @author Ken Swartz
 */
final class ShowExitDialog extends AbstractCommand {

	ShowExitDialog() {
		super("Exit\u2026");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q,
				CTRL_MASK));
	}

	/**
	 * Execute the command
	 */
	protected void execute(final Object[] cmdParams) {
		boolean confirm = true;
		final JFrame frame = STATUS.getFrame();
		if (cmdParams != null && !((Boolean) cmdParams[0]).booleanValue()) {
			confirm = false;
		}
		if (confirm) { // Confirm exit
			final int rval = JOptionPane.showConfirmDialog(frame,
					"Are you sure you want to exit?", "Exit Jam Confirmation",
					JOptionPane.YES_NO_OPTION);
			if (rval == JOptionPane.YES_OPTION) {
				System.exit(0);
			} else {
				frame.setVisible(true);
			}
		} else {
			System.exit(0);
		}
	}

	/**
	 * Execute the command
	 */
	protected void executeParse(final String[] cmdParams) {
		if (cmdParams.length == 0) {
			execute(null);
		} else {
			Boolean confirm;
			if (cmdParams[0].equals("noconfirm")) {
				confirm = Boolean.FALSE;
			} else {
				confirm = Boolean.TRUE;
			}
			Object[] cmdParmObj = new Object[1];
			cmdParmObj[0] = confirm;
			execute(cmdParmObj);
		}
	}

}
