package jam.commands;

import java.io.File;

import jam.data.Histogram;
import jam.io.ImpExpException;
import jam.global.CommandListenerException;

/**
 * Export data to file
 * @author Ken Swartz
 *
 */
class AbstractExportFile extends AbstractImportExport {

	AbstractExportFile() {
		super();
	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected final void execute(Object[] cmdParams) throws CommandException {
		try {
			final Histogram h=Histogram.getHistogram(
			status.getCurrentHistogramName());
			if (cmdParams == null) { //No file given		
				importExport.saveFile(h);
			} else { //File given
				File file = (File) cmdParams[0];
				importExport.saveFile(file,h);
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
