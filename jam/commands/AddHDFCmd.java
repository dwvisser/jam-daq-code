package jam.commands;

import jam.global.BroadcastEvent;
import jam.global.SortMode;
import jam.io.FileOpenMode;
import jam.data.Group;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

/**
 * Add counts to histograms in memory from histograms in an HDF file.
 * 
 * @author Ken Swartz
 */
final class AddHDFCmd extends LoaderHDF implements Observer {

	public void initCommand(){
		putValue(NAME,"Add counts\u2026");
		fileOpenMode=FileOpenMode.ADD;
	}
	
	protected final void execute(final Object[] cmdParams) {
		File file=null;
		Group loadGroup;
		
		/*FIXME KBS parse correctly
		if (cmdParams!=null) {
			file =(File)cmdParams[0];
			//loadGroup=(Group)cmdParams[1];
		} 	
		*/
		
		loadGroup =Group.getCurrentGroup();
		loadHDFFile(file, loadGroup);
	}
	
	public void update(Observable observe, Object obj){
		final BroadcastEvent be=(BroadcastEvent)obj;
		final BroadcastEvent.Command command=be.getCommand();
		if (command==BroadcastEvent.Command.SORT_MODE_CHANGED){
			final SortMode mode=STATUS.getSortMode();
			setEnabled(mode != SortMode.REMOTE);
		}
	}
}
