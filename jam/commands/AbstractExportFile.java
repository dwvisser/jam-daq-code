package jam.commands;

import jam.data.Histogram;
import jam.global.CommandListenerException;
import jam.io.ImpExpException;

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
    protected final void execute(final Object[] cmdParams) throws CommandException {
        try {
            final Histogram histogram = (Histogram)STATUS.getCurrentHistogram();
            if (cmdParams == null) { //No file given
                importExport.saveFile(histogram);
            } else { //File given
                final File file = (File) cmdParams[0];
                importExport.saveFile(file, histogram);
            }
        } catch (ImpExpException iee) {
            throw new CommandException(iee);
        }
    }

    /**
     * Saves the given file, or opens a save dialog if given <code>null</code>.
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
            throw new CommandListenerException(ce);
        }
    }
}
