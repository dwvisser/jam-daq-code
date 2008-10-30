package jam.commands;

import jam.data.AbstractHistogram;
import jam.global.BroadcastEvent;
import jam.global.Nameable;
import jam.io.ImpExpSPE;
import jam.ui.SelectionTree;

import java.util.Observable;
import java.util.Observer;

/**
 * Export data to a Radware gf3 spectrum file.
 * 
 * @author Dale Visser
 */
final class ExportRadware extends AbstractExportFile implements Observer {

	ExportRadware() {
		super("Radware gf3");
	}

	public void initCommand() {
		importExport = new ImpExpSPE();
	}

	public void update(final Observable observe, final Object obj) {
		final BroadcastEvent event = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = event.getCommand();
		if (command == BroadcastEvent.Command.HISTOGRAM_SELECT) {
			enable();
		}
	}

	private void enable() {
		final Nameable histogram = SelectionTree.getCurrentHistogram();
		if (histogram instanceof AbstractHistogram) {
			setEnabled(((AbstractHistogram) histogram).getDimensionality() == 1);
		}
	}
}
