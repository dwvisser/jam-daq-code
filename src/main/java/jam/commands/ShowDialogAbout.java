package jam.commands;

import javax.swing.JFrame;

import injection.GuiceInjector;
import jam.AboutDialog;

/**
 * Show the about dialog
 * @author Ken Swartz
 */
@SuppressWarnings("serial")
final class ShowDialogAbout extends AbstractShowDialog {

    /**
     * Initializes dialog info.
     */
    ShowDialogAbout() {
        super("About\u2026");
        dialog = new AboutDialog(GuiceInjector.getObjectInstance(JFrame.class))
                .getDialog();
    }
}