package jam.commands;

import jam.Help;

/**
 * Show the license dialog.
 * 
 * @author Ken Swartz
 */
final class ShowDialogLicense extends AbstractShowDialog {

	public void initCommand() {
		putValue(NAME, "License\u2026");
		dialog = new Help();
	}
}
