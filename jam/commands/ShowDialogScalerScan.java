package jam.commands;
import jam.util.ScalerScan;

/**
 *  Show the scaler scan dialog
 * @author Ken Swartz
 */
public class ShowDialogScalerScan extends AbstractShowDialog {

	protected void initCommand() {
		putValue(NAME, "Scan HDF files for scalers\u2026");
		dialog = new ScalerScan();
	}

}
