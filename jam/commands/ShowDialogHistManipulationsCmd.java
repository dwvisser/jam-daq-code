package jam.commands;

import jam.data.control.Manipulations;

/**
 *  Command to show project histgram dialog
 * 
 * @author Ken Swartz
 */
final class ShowDialogHistManipulationsCmd extends AbstractShowDialog {

	public void initCommand() {
		putValue(NAME, "Combine\u2026");
		dialog = new Manipulations(msghdlr);
	}
}
