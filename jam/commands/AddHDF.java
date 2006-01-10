package jam.commands;

import jam.data.Group;
import jam.global.BroadcastEvent;
import jam.global.SortMode;
import jam.io.FileOpenMode;

import java.io.File;
import java.util.Observable;

/**
 * Add counts to histograms in memory from histograms in an HDF file.
 * 
 * @author Ken Swartz
 */
final class AddHDF extends AbstractLoaderHDF {
	
	AddHDF(){
		super();
	}
	 
	public void initCommand(){
		putValue(NAME,"Add Group Counts\u2026");
		fileOpenMode=FileOpenMode.ADD;
	}
	
	protected void execute(final Object[] cmdParams) {
        File file = null;
        loadGroup = (Group)STATUS.getCurrentGroup();
        //Parse commad parameters if given
        if (cmdParams != null) {
            if (cmdParams.length > 0) {
                file = (File) cmdParams[0];
            }
            if (cmdParams.length > 1) {
                loadGroup = (Group) cmdParams[1];
            }
        }
        loadHDFFile(file, loadGroup);
    }
	
	public void update(final Observable observe, final Object obj) {
        final BroadcastEvent event = (BroadcastEvent) obj;
        final BroadcastEvent.Command command = event.getCommand();
        if (command == BroadcastEvent.Command.SORT_MODE_CHANGED) {
            final SortMode mode = STATUS.getSortMode();
            setEnabled(mode != SortMode.REMOTE);
        }
    }
}
