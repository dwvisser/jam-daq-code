package jam.commands;

import jam.global.BroadcastEvent;
import jam.global.SortMode;
import jam.io.FileOpenMode;

import java.awt.Event;
import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.KeyStroke;
/**
 *  Reload data from a hdf file
 * 
 * @author Ken Swartz
 *
 */
final class ReloadHDFCmd extends LoaderHDF implements Observer {
	
	public void initCommand(){
		putValue(NAME,"Reload\u2026");
		putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(
		KeyEvent.VK_O,
		CTRL_MASK | Event.SHIFT_MASK));
		fileOpenMode = FileOpenMode.RELOAD;
	}

	public void update(Observable observe, Object obj){
		final BroadcastEvent be=(BroadcastEvent)obj;
		final BroadcastEvent.Command command=be.getCommand();
		if (command==BroadcastEvent.Command.SORT_MODE_CHANGED){
			enable();
		}
	}
	
	private void enable(){
		final SortMode mode=STATUS.getSortMode();
		final boolean online = mode == SortMode.ONLINE_DISK || 
		mode == SortMode.ON_NO_DISK;
		final boolean offline = mode == SortMode.OFFLINE;
		final boolean sorting = online || offline;
		setEnabled(sorting);
	}
}
