package jam.commands;

import jam.data.Histogram;
import jam.data.control.HistogramZero;
import jam.global.BroadcastEvent;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.Icon;

/**
 * Show the zero histograms dialog
 * 
 * @author Ken Swartz
 * 
 */
final class ShowDialogZeroHistogram extends AbstractShowDialog implements
		Observer {

	private final List<Histogram> histogramList = Histogram.getHistogramList();

	/**
	 * Initialize command
	 */
	public void initCommand() {
		putValue(NAME, "Zero\u2026");
		final Icon iZero = loadToolbarIcon("jam/ui/Zero.png");
		putValue(Action.SMALL_ICON, iZero);
		putValue(Action.SHORT_DESCRIPTION, "Zero Histograms.");
		dialog = new HistogramZero();
	}

	public void update(Observable observe, Object obj) {
		final BroadcastEvent be = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = be.getCommand();
		if ((command == BroadcastEvent.Command.GROUP_SELECT)
				|| (command == BroadcastEvent.Command.ROOT_SELECT)) {
			setEnabled(false);
		} else if ((command == BroadcastEvent.Command.HISTOGRAM_SELECT)
				|| (command == BroadcastEvent.Command.GATE_SELECT)) {
			setEnabled(!histogramList.isEmpty());
		}
	}
}
