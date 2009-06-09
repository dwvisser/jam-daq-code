package jam.commands;

import jam.data.control.GateAdd;

import com.google.inject.Inject;

/**
 * Show the gate add dialog box.
 * @author Ken Swartz
 */
final class ShowDialogAddGate extends AbstractShowGateDialog {

    /**
     * Command to show gate add dialog.
     * @param gateAdd
     *            the dialog
     */
    @Inject
    ShowDialogAddGate(final GateAdd gateAdd) {
        super("Add\u2026");
        dialog = gateAdd;
    }

}
