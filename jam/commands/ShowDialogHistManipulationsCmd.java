package jam.commands;

import jam.data.control.Combine;

/**
 *  Command to show project histgram dialog
 * 
 * @author Ken Swartz
 */
final class ShowDialogHistManipulationsCmd extends AbstractShowDialog {

	ShowDialogHistManipulationsCmd() {
		super("Combine\u2026");
		dialog = new Combine();
	}
}
