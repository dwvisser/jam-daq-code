package jam.commands;

import com.google.inject.Inject;

import jam.data.control.GateAdd;

/**
 * Show the gate add dialog box.
 * @author Ken Swartz
 */
@SuppressWarnings("serial")
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
