package jam.commands;

import jam.AboutDialog;
import jam.global.JamStatus;

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
		dialog = (new AboutDialog(JamStatus.getSingletonInstance().getFrame())).getDialog();
	}

}
