package jam.commands;

import jam.data.AbstractHistogram;
import jam.data.control.GateNew;
import jam.global.BroadcastEvent;
import jam.global.Nameable;
import jam.ui.SelectionTree;

import java.util.Observable;
import java.util.Observer;

import com.google.inject.Inject;

/**
 * Show the new gate dialog
 */
final class ShowDialogNewGateCmd extends AbstractShowDialog implements Observer {

	@Inject
	ShowDialogNewGateCmd(final GateNew gateNew) {
		super("New\u2026");
		dialog = gateNew;
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
