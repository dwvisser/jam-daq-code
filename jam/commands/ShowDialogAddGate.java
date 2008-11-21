package jam.commands;

import jam.data.AbstractHistogram;
import jam.data.Gate;
import jam.data.control.GateAdd;
import jam.global.BroadcastEvent;
import jam.global.Nameable;
import jam.ui.SelectionTree;

import java.util.Observable;
import java.util.Observer;

import com.google.inject.Inject;

/**
 * Show the scalers dialog box
 * 
 * @author Ken Swartz
 * 
 */
final class ShowDialogAddGate extends AbstractShowDialog implements Observer {

	@Inject
	ShowDialogAddGate(final GateAdd gateAdd) {
		super("Add\u2026");
		dialog = gateAdd;
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
			setEnabled(!Gate.getGateList().isEmpty()
					&& hist instanceof AbstractHistogram);
		}
	}
}
