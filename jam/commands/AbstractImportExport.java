package jam.commands;

import java.io.File;

import jam.global.CommandListenerException;
import jam.io.AbstractImpExp;

/**
 * Export data to file.
 * 
 * @author Ken Swartz
 */
abstract class AbstractImportExport extends AbstractCommand {// NOPMD

	/**
	 * Must be assigned a real value by full implementations.
	 */
	protected AbstractImpExp importExport;

	AbstractImportExport() {
		super();
	}

	/**
	 * 
	 * @see AbstractCommand#AbstractCommand(String)
	 */
	protected AbstractImportExport(String name) {
		super(name);
	}

	/**
	 * Loads the given file, or opens a load dialog if given <code>null</code>.
	 * 
	 * @param cmdTokens
	 *            0- or 1-element array with a file reference
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 * @see java.io.File
	 */
	protected final void executeParse(final String[] cmdTokens)
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
			throw new CommandListenerException(ce.getMessage(), ce);
		}
	}
}
