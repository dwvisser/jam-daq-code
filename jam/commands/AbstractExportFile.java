package jam.commands;

import jam.data.Histogram;
import jam.io.ImpExpException;
import jam.ui.SelectionTree;

import java.io.File;

/**
 * Export data to file. Full implementations must assign an <code>ImpExp</code>
 * object.
 * 
 * @author Ken Swartz
 */
class AbstractExportFile extends AbstractImportExport {

	AbstractExportFile() {
		super();
	}

	/**
	 * 
	 * @see AbstractCommand#AbstractCommand(String)
	 */
	protected AbstractExportFile(String name) {
		super(name);
	}

	/**
	 * Saves the given file, or opens a save dialog if given <code>null</code>.
	 * 
	 * @param cmdParams
	 *            <code>null</code> or 1-element array with a file reference
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 * @see java.io.File
	 */
	protected final void execute(final Object[] cmdParams)
			throws CommandException {
		try {
			final Histogram histogram = (Histogram) SelectionTree
					.getCurrentHistogram();
			if (cmdParams == null) { // No file given
				importExport.saveFile(histogram);
			} else { // File given
				final File file = (File) cmdParams[0];
				importExport.saveFile(file, histogram);
			}
		} catch (ImpExpException iee) {
			throw new CommandException(iee);
		}
	}
}
