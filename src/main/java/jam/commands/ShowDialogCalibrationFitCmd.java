package jam.commands;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.google.inject.Inject;

import jam.data.AbstractHistogram;
import jam.data.control.CalibrationFit;
import jam.global.BroadcastEvent;
import jam.global.Nameable;
import jam.ui.SelectionTree;

/**
 * Show histgoram Calibration fit dialog.
 * 
 * @author Ken Swartz
 */
@SuppressWarnings("serial")
final class ShowDialogCalibrationFitCmd extends AbstractShowDialog implements
		PropertyChangeListener {

	@Inject
	ShowDialogCalibrationFitCmd(final CalibrationFit calibrationFit) {
		super("Calibration\u2026");
		dialog = calibrationFit;
		enable();
	}

	private void enable() {
		final Nameable named = SelectionTree.getCurrentHistogram();
		boolean enable = named instanceof AbstractHistogram;
		if (enable) {
			enable = ((AbstractHistogram) named).getDimensionality() == 1;
		}
		setEnabled(enable);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		final BroadcastEvent.Command command = ((BroadcastEvent) evt).getCommand();
		if ((command == BroadcastEvent.Command.GROUP_SELECT)
				|| (command == BroadcastEvent.Command.ROOT_SELECT)) {
			setEnabled(false);
		} else if ((command == BroadcastEvent.Command.HISTOGRAM_SELECT)
				|| (command == BroadcastEvent.Command.GATE_SELECT)) {
			enable();
		}
	}

}
