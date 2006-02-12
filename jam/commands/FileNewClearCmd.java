package jam.commands;

import jam.data.DataBase;
import jam.global.BroadcastEvent;
import jam.global.SortMode;

import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 *  Command for file menu new also clears
 * 
 * @author Ken Swartz
 *
 */
final class FileNewClearCmd extends AbstractCommand implements Observer {
	
	FileNewClearCmd(){
		super("Clear data\u2026");
		putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_N, 
		CTRL_MASK));
		enable();
	}

	/**
	 * Excecute command
	 * 
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(final Object[] cmdParams) {
		final JFrame frame =STATUS.getFrame();
		if (JOptionPane.YES_OPTION==JOptionPane.showConfirmDialog(frame,
		"Erase all current data?","New",JOptionPane.YES_NO_OPTION)){
			STATUS.setSortMode(SortMode.NO_SORT, "Data Cleared");
			DataBase.getInstance().clearAllLists();
			BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_NEW);
			BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, null);
		}
		
	}
	
	protected void executeParse(final String[] cmdTokens) {
		execute(null);		
	}
	
	public void update(final Observable observe, final Object obj){
		final BroadcastEvent event=(BroadcastEvent)obj;
		final BroadcastEvent.Command command=event.getCommand();
		if (command==BroadcastEvent.Command.SORT_MODE_CHANGED){
			enable();
		}
	}
	
	private void enable() {
		final SortMode mode=STATUS.getSortMode();
		setEnabled(mode==SortMode.FILE || mode==SortMode.NO_SORT);
	}

}
