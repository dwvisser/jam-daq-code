package jam.commands;

import jam.data.control.AbstractControl;
import jam.global.BroadcastEvent;
import jam.global.CommandListenerException;
import jam.io.ImpExpException;

import java.io.File;

/**
 * Export data to file. Full implementations must assign an <code>ImpExp</code>
 * object.
 * 
 * @author Ken Swartz
 */
class AbstractImportFile extends AbstractImportExport {

    /**
     * Loads the given file, or opens a load dialog if given <code>null</code>.
     * 
     * @param cmdParams
     *            <code>null</code> or 1-element array with a file reference
     * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
     * @see java.io.File
     */
    protected final void execute(Object[] cmdParams) throws CommandException {
        try {
            if (cmdParams == null) { //No file given
                if (importExport.openFile(null)) {
                    STATUS.setOpenFile(importExport.getLastFile());
                    AbstractControl.setupAll();
                    BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
                }
            } else { //File given
                final File file = (File) cmdParams[0];
                importExport.openFile(file);
            }
        } catch (ImpExpException iee) {
            throw new CommandException(iee.getMessage());
        }
    }

    /**
     * Loads the given file, or opens a load dialog if given <code>null</code>.
     * 
     * @param cmdTokens
     *            0- or 1-element array with a file reference
     * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
     * @see java.io.File
     */
    protected final void executeParse(String[] cmdTokens)
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
