package jam.commands;

import jam.global.BroadcastEvent;
import jam.global.SortMode;
import jam.io.FileOpenMode;

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
	
	public void update(Observable observe, Object obj){
		final BroadcastEvent be=(BroadcastEvent)obj;
		final BroadcastEvent.Command command=be.getCommand();
		if (command==BroadcastEvent.Command.SORT_MODE_CHANGED){
			final SortMode mode=status.getSortMode();
			setEnabled(mode != SortMode.REMOTE);
		}
	}
}
