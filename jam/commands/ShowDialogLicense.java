package jam.commands;

import jam.Help;

/**
 * Show the license dialog.
 * 
 * @author Ken Swartz
 */
public class ShowDialogLicense extends AbstractShowDialog {

	public void initCommand() {
		putValue(NAME, "License\u2026");
		dialog = new Help();
	}
}
