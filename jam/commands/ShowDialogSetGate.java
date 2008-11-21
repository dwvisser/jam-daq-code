package jam.commands;

import jam.data.AbstractHistogram;
import jam.data.control.GateSet;
import jam.global.BroadcastEvent;
import jam.global.Nameable;
import jam.ui.SelectionTree;

import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.Icon;

import com.google.inject.Inject;

/**
 * Show the scalers dialog box
 * 
 * @author Ken Swartz
 * 
 */
final class ShowDialogSetGate extends AbstractShowDialog implements Observer {

	@Inject
	ShowDialogSetGate(final GateSet gateSet) {
		super();
		dialog = gateSet;
		putValue(NAME, "Set\u2026");
		final Icon iGateSet = loadToolbarIcon("jam/ui/GateSet.png");
		putValue(Action.SMALL_ICON, iGateSet);
		putValue(Action.SHORT_DESCRIPTION, "Set Gate.");
	}

	private void decideEnable() {
		final Nameable named = SelectionTree.getCurrentHistogram();
		if (named instanceof AbstractHistogram) {
			final AbstractHistogram hist = (AbstractHistogram) named;
			setEnabled(!hist.getGateCollection().getGates().isEmpty());
		}
	}

	public void update(final Observable observe, final Object obj) {
		final BroadcastEvent event = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = event.getCommand();
		if ((command == BroadcastEvent.Command.GROUP_SELECT)
				|| (command == BroadcastEvent.Command.ROOT_SELECT)) {
			setEnabled(false);
		} else if ((command == BroadcastEvent.Command.HISTOGRAM_SELECT)
				|| (command == BroadcastEvent.Command.GATE_SELECT)) {
			decideEnable();
		}
	}
}
