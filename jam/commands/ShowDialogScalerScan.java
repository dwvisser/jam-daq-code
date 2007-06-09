package jam.commands;

/**
 *  Show the scaler scan dialog
 * @author Ken Swartz
 */
final class ShowDialogScalerScan extends AbstractShowDialog {

	ShowDialogScalerScan() {
		super("Scan HDF files for scalers\u2026");
		dialog = (new ScalerScan()).getDialog();
	}
}
