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
	public void initCommand() {
		putValue(NAME, "About\u2026");
		dialog = (new AboutDialog(JamStatus.getSingletonInstance().getFrame())).getDialog();
	}

}
