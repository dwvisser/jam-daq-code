package jam.commands;

import jam.global.BroadcastEvent;
import jam.global.CommandListenerException;
import jam.global.SortMode;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;

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
final class ReloadHDFCmd extends AbstractCommand implements Observer {
	
	ReloadHDFCmd(){
		putValue(NAME,"Reload\u2026");
		putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(
		KeyEvent.VK_O,
		CTRL_MASK | Event.SHIFT_MASK));
	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) {			
		final HDFIO	hdfio = new HDFIO(status.getFrame(), msghdlr);
		hdfio.readFile(FileOpenMode.RELOAD);		
	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens)
		throws CommandListenerException {
			execute(null);
	}

	public void update(Observable observe, Object obj){
		final BroadcastEvent be=(BroadcastEvent)obj;
		final BroadcastEvent.Command command=be.getCommand();
		if (command==BroadcastEvent.Command.SORT_MODE_CHANGED){
			enable();
		}
	}
	
	protected void enable(){
		final SortMode mode=status.getSortMode();
		final boolean online = mode == SortMode.ONLINE_DISK || 
		mode == SortMode.ONLINE_NO_DISK;
		final boolean offline = mode == SortMode.OFFLINE;
		final boolean sorting = online || offline;
		setEnabled(sorting);
	}
}
