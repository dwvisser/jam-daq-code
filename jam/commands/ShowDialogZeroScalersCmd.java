package jam.commands;

import jam.data.Scaler;
import jam.data.control.ScalerZero;

import java.util.Observable;
import java.util.Observer;

import com.google.inject.Inject;

/**
 * Show the zero scalers dialog.
 * 
 * @author Ken Swartz
 * 
 */
final class ShowDialogZeroScalersCmd extends AbstractShowDialog implements
		Observer {

	@Inject
	ShowDialogZeroScalersCmd(final ScalerZero scalerZero) {
		super("Zero Scalers\u2026");
		dialog = scalerZero;
	}

	public void update(final Observable observe, final Object obj) {
		setEnabled(!Scaler.getScalerList().isEmpty());
	}

}
