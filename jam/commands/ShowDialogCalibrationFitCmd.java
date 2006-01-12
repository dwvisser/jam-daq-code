package jam.commands;

import jam.data.Histogram;
import jam.data.control.CalibrationFit;
import jam.global.BroadcastEvent;
import jam.global.Nameable;

import java.util.Observable;
import java.util.Observer;

/**
 * Show histgoram Calibration fit dialog.
 * 
 * @author Ken Swartz
 */
final class ShowDialogCalibrationFitCmd extends AbstractShowDialog implements
		Observer {

	public void initCommand() {
		putValue(NAME, "Calibration\u2026");
		dialog = new CalibrationFit();
		enable();
	}

	private void enable() {
		boolean enable = false;
		final Nameable named = STATUS.getCurrentHistogram();
		enable = named instanceof Histogram;
		if (enable) {
			final Histogram histogram = (Histogram) named;
			enable = (histogram != null && histogram.getDimensionality() == 1);
		}
		setEnabled(enable);
	}

	public void update(Observable observe, Object obj) {
		final BroadcastEvent be = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = be.getCommand();
		if ((command == BroadcastEvent.Command.GROUP_SELECT)
				|| (command == BroadcastEvent.Command.ROOT_SELECT)) {
			setEnabled(false);
		} else if ((command == BroadcastEvent.Command.HISTOGRAM_SELECT)
				|| (command == BroadcastEvent.Command.GATE_SELECT)) {
			enable();
		}
	}

}
