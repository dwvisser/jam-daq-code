package jam.commands;

import com.google.inject.Inject;
import jam.data.AbstractHistogram;
import jam.global.BroadcastEvent;
import jam.global.Nameable;
import jam.io.ImpExpSPE;
import jam.ui.SelectionTree;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Export data to a Radware gf3 spectrum file.
 * 
 * @author Dale Visser
 */
final class ExportRadware extends AbstractExportFile implements PropertyChangeListener {

	@Inject
	ExportRadware(final ImpExpSPE impExpSPE) {
		super("Radware gf3");
		importExport = impExpSPE;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (((BroadcastEvent) evt).getCommand() == BroadcastEvent.Command.HISTOGRAM_SELECT) {
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
