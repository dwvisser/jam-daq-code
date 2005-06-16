package jam.commands;

import jam.data.Histogram;
import jam.data.control.GateSet;
import jam.global.BroadcastEvent;

import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.Icon;

/**
 * Show the scalers dialog box
 * 
 * @author Ken Swartz
 * 
 */
final class ShowDialogSetGate extends AbstractShowDialog implements Observer {

	public void initCommand() {
		putValue(NAME, "Set\u2026");
		dialog = new GateSet();
		final Icon iGateSet = loadToolbarIcon("jam/ui/GateSet.png");
		putValue(Action.SMALL_ICON, iGateSet);
		putValue(Action.SHORT_DESCRIPTION, "Set Gate.");
	}

	public void update(Observable observe, Object obj) {
		final BroadcastEvent be = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = be.getCommand();
		if ((command == BroadcastEvent.Command.GROUP_SELECT)
				|| (command == BroadcastEvent.Command.ROOT_SELECT)) {
			setEnabled(false);
		} else if ((command == BroadcastEvent.Command.HISTOGRAM_SELECT)
				|| (command == BroadcastEvent.Command.GATE_SELECT)) {
			Histogram hist = STATUS.getCurrentHistogram();
			if (hist != null)
				setEnabled(!hist.getGates().isEmpty());
		}
	}
}
