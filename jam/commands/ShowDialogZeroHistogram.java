package jam.commands;

import jam.data.AbstractHistogram;
import jam.data.control.HistogramZero;
import jam.global.BroadcastEvent;

import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.Icon;

import com.google.inject.Inject;

/**
 * Show the zero histograms dialog
 * 
 * @author Ken Swartz
 * 
 */
final class ShowDialogZeroHistogram extends AbstractShowDialog implements
		Observer {

	/**
	 * Initialize command
	 */
	@Inject
	ShowDialogZeroHistogram(final HistogramZero histogramZero) {
		super("Zero\u2026");
		final Icon iZero = loadToolbarIcon("jam/ui/Zero.png");
		putValue(Action.SMALL_ICON, iZero);
		putValue(Action.SHORT_DESCRIPTION, "Zero Histograms.");
		dialog = histogramZero;
	}

	public void update(final Observable observe, final Object obj) {
		final BroadcastEvent event = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = event.getCommand();
		if ((command == BroadcastEvent.Command.GROUP_SELECT)
				|| (command == BroadcastEvent.Command.ROOT_SELECT)) {
			setEnabled(false);
		} else if ((command == BroadcastEvent.Command.HISTOGRAM_SELECT)
				|| (command == BroadcastEvent.Command.GATE_SELECT)) {
			setEnabled(!AbstractHistogram.getHistogramList().isEmpty());
		}
	}
}
