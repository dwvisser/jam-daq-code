package jam.commands;

import injection.GuiceInjector;
import jam.data.control.AbstractControl;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.io.ImpExpException;

import java.io.File;

/**
 * Export data to file. Full implementations must assign an <code>ImpExp</code>
 * object.
 * 
 * @author Ken Swartz
 */
class AbstractImportFile extends AbstractImportExport {

	private transient final Broadcaster broadcaster;

	AbstractImportFile(final Broadcaster broadcaster) {
		super();
		this.broadcaster = broadcaster;
	}

	AbstractImportFile(final String name, final Broadcaster broadcaster) {
		super(name);
		this.broadcaster = broadcaster;
	}

	/**
	 * Loads the given file, or opens a load dialog if given <code>null</code>.
	 * 
	 * @param cmdParams
	 *            <code>null</code> or 1-element array with a file reference
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 * @see java.io.File
	 */
	@Override
	protected final void execute(final Object[] cmdParams)
			throws CommandException {
		try {
			if (cmdParams == null) { // No file given
				if (importExport.openFile(null)) {
					GuiceInjector.getJamStatus().setOpenFile(
							importExport.getLastFile());
					AbstractControl.setupAll();
					this.broadcaster
							.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
				}
			} else { // File given
				final File file = (File) cmdParams[0];
				importExport.openFile(file);
			}
		} catch (ImpExpException iee) {
			throw new CommandException(iee);
		}
	}
}
