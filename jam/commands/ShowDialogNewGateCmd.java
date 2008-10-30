package jam.commands;

import jam.data.AbstractHistogram;
import jam.data.control.GateNew;
import jam.global.BroadcastEvent;
import jam.global.Nameable;
import jam.ui.SelectionTree;

import java.util.Observable;
import java.util.Observer;

/**
 * Show the new gate dialog
 */
final class ShowDialogNewGateCmd extends AbstractShowDialog implements Observer {

	ShowDialogNewGateCmd() {
		super("New\u2026");
	}

	public void initCommand() {
		/* Super class member next line */
		dialog = new GateNew();
	}

	public void update(final Observable observe, final Object obj) {
		final BroadcastEvent event = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = event.getCommand();
		if ((command == BroadcastEvent.Command.GROUP_SELECT)
				|| (command == BroadcastEvent.Command.ROOT_SELECT)) {
			setEnabled(false);
		} else if ((command == BroadcastEvent.Command.HISTOGRAM_SELECT)
				|| (command == BroadcastEvent.Command.GATE_SELECT)) {
			final Nameable hist = SelectionTree.getCurrentHistogram();
			setEnabled(!AbstractHistogram.getHistogramList().isEmpty()
					&& hist instanceof AbstractHistogram);
		}
	}
}
