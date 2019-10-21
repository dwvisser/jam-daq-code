package jam.commands;

import com.google.inject.Inject;

import jam.data.control.GateNew;

/**
 * Show the new gate dialog.
 */
@SuppressWarnings("serial")
final class ShowDialogNewGateCmd extends AbstractShowGateDialog {

    /**
     * Show the new gate dialog.
     * @param gateNew
     *            the dialog
     */
    @Inject
    ShowDialogNewGateCmd(final GateNew gateNew) {
        super("New\u2026");
        dialog = gateNew;
    }
}
