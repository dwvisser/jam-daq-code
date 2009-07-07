package jam.commands;

import com.google.inject.Inject;

/**
 * Show the scaler scan dialog.
 * @author Ken Swartz
 */
final class ShowDialogScalerScan extends AbstractShowDialog {

    @Inject
    ShowDialogScalerScan(final ScalerScan scalerScan) {
        super("Scan HDF files for scalers\u2026");
        dialog = scalerScan.getDialog();
    }
}
