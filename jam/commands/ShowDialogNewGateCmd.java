package jam.commands;

import jam.data.control.GateNew;

import com.google.inject.Inject;

/**
 * Show the new gate dialog.
 */
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
