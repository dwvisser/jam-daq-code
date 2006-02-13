package jam.commands;

import jam.data.Scaler;
import jam.data.control.ScalerDisplay;

import java.util.Observable;
import java.util.Observer;

/**
 * Show the scalers dialog box
 * 
 * @author Ken Swartz
 * 
 */
final class ShowDialogScalersCmd extends AbstractShowDialog implements Observer {

	ShowDialogScalersCmd() {
		super("Display Scalers\u2026");
		dialog = new ScalerDisplay();
	}

	public void update(final Observable observe, final Object obj) {
		setEnabled(!Scaler.getScalerList().isEmpty());
	}
}
