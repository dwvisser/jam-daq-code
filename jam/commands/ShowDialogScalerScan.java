package jam.commands;
import jam.util.ScalerScan;

/**
 *  Show the scaler scan dialog
 * @author Ken Swartz
 */
final class ShowDialogScalerScan extends AbstractShowDialog {

	public void initCommand() {
		putValue(NAME, "Scan HDF files for scalers\u2026");
		dialog = (new ScalerScan()).getDialog();
	}
}
