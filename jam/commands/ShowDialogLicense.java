package jam.commands;

import jam.Help;

/**
 * Show the license dialog.
 * 
 * @author Ken Swartz
 */
final class ShowDialogLicense extends AbstractShowDialog {

	ShowDialogLicense() {
		super("License\u2026");
		dialog = new Help();
	}
}
