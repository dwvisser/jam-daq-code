package jam.commands;

import jam.data.AbstractHistogram;
import jam.data.control.CalibrationFit;
import jam.global.BroadcastEvent;
import jam.global.Nameable;
import jam.ui.SelectionTree;

import java.util.Observable;
import java.util.Observer;

/**
 * Show histgoram Calibration fit dialog.
 * 
 * @author Ken Swartz
 */
final class ShowDialogCalibrationFitCmd extends AbstractShowDialog implements
		Observer {

	ShowDialogCalibrationFitCmd() {
		super("Calibration\u2026");
		dialog = new CalibrationFit();
		enable();
	}

	private void enable() {
		boolean enable = false;
		final Nameable named = SelectionTree.getCurrentHistogram();
		enable = named instanceof AbstractHistogram;
		if (enable) {
			final AbstractHistogram histogram = (AbstractHistogram) named;
			enable = (histogram != null && histogram.getDimensionality() == 1);
		}
		setEnabled(enable);
	}

	public void update(final Observable observe, final Object obj) {
		final BroadcastEvent event = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = event.getCommand();
		if ((command == BroadcastEvent.Command.GROUP_SELECT)
				|| (command == BroadcastEvent.Command.ROOT_SELECT)) {
			setEnabled(false);
		} else if ((command == BroadcastEvent.Command.HISTOGRAM_SELECT)
				|| (command == BroadcastEvent.Command.GATE_SELECT)) {
			enable();
		}
	}

}
