package jam.commands;

import injection.GuiceInjector;
import jam.AboutDialog;

/**
 * Show the about dialog
 * 
 * @author Ken Swartz
 * 
 */
final class ShowDialogAbout extends AbstractShowDialog {

	/**
	 * Initializes dialog info.
	 */
	ShowDialogAbout() {
		super("About\u2026");
		dialog = (new AboutDialog(GuiceInjector.getFrame())).getDialog();
	}

}
