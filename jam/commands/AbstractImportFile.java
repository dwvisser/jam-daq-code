package jam.commands;

import jam.data.control.DataControl;
import jam.global.BroadcastEvent;
import jam.global.CommandListenerException;
import jam.io.ImpExpException;

import java.io.File;

/**
 * Export data to file
 * @author Ken Swartz
 *
 */
class AbstractImportFile extends AbstractImportExport {

	AbstractImportFile() {
		super();
	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected final void execute(Object[] cmdParams) throws CommandException {
		try {
			if (cmdParams == null) { //No file given		
				if (importExport.openFile(null)) {
					status.setSortMode(importExport.getLastFile());
					DataControl.setupAll();
					broadcaster.broadcast(BroadcastEvent.HISTOGRAM_ADD);
				}
			} else { //File given
				File file = (File) cmdParams[0];
				importExport.openFile(file);
			}
		} catch (ImpExpException iee) {
			throw new CommandException(iee.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens)
		throws CommandListenerException {
		try {
			if (cmdTokens.length == 0) {
				execute(null);
			} else {
				final Object[] cmdParams = new Object[1];
				final File file = new File(cmdTokens[0]);
				cmdParams[0] = file;
				execute(cmdParams);
			}
		} catch (CommandException ce) {
			throw new CommandListenerException(ce.getMessage());
		}
	}
}
