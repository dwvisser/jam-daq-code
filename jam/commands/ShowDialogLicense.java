package jam.commands;


/**
 * Show the license dialog.
 * 
 * @author Ken Swartz
 */
final class ShowDialogLicense extends AbstractShowDialog {

	ShowDialogLicense() {
		super("License\u2026");
		dialog = jam.Factory.createHelp();
	}
}
