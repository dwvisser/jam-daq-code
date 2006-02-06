package jam.commands;

import jam.data.Gate;
import jam.data.Histogram;
import jam.data.control.GateAdd;
import jam.global.BroadcastEvent;
import jam.global.Nameable;

import java.util.Observable;
import java.util.Observer;

/**
 * Show the scalers dialog box
 * 
 * @author Ken Swartz
 * 
 */
final class ShowDialogAddGate extends AbstractShowDialog implements Observer {

	ShowDialogAddGate() {
		super("Add\u2026");
	}

	public void initCommand() {
		dialog = new GateAdd();
	}

	public void update(final Observable observe, final Object obj) {
		final BroadcastEvent event = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = event.getCommand();
		if ((command == BroadcastEvent.Command.GROUP_SELECT)
				|| (command == BroadcastEvent.Command.ROOT_SELECT)) {
			setEnabled(false);
		} else if ((command == BroadcastEvent.Command.HISTOGRAM_SELECT)
				|| (command == BroadcastEvent.Command.GATE_SELECT)) {
			final Nameable hist = STATUS.getCurrentHistogram();
			setEnabled(!Gate.getGateList().isEmpty()
					&& hist instanceof Histogram);
		}
	}
}
