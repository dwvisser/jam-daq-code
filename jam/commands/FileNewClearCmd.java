package jam.commands;

import jam.data.DataBase;
import jam.global.BroadcastEvent;
import jam.global.SortMode;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *  Command for file menu new also clears
 * 
 * @author Ken Swartz
 *
 */
final class FileNewClearCmd extends AbstractCommand implements Observer {
	
	FileNewClearCmd(){
		this.putValue(NAME,"Clear data");
	}

	/**
	 * Excecute command
	 * 
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) {
		
		JFrame frame =status.getFrame();
		
		if (JOptionPane.YES_OPTION==JOptionPane.showConfirmDialog(frame,
		"Erase all current data?","New",JOptionPane.YES_NO_OPTION)){
			status.setSortMode(SortMode.NO_SORT);
			DataBase.getInstance().clearAllLists();
			broadcaster.broadcast(BroadcastEvent.HISTOGRAM_ADD);
		}
		
	}
	
	/** 
	 * Execute command
	 * @see jam.commands.AbstractCommand#executeStrParam(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens) {
		execute(null);		
	}
	
	public void update(Observable observe, Object obj){
		final BroadcastEvent be=(BroadcastEvent)obj;
		final int command=be.getCommand();
		if (command==BroadcastEvent.SORT_MODE_CHANGED){
			enable();
		}
	}
	protected void enable() {
		final SortMode mode=status.getSortMode();
		setEnabled(mode==SortMode.FILE || mode==SortMode.NO_SORT);

	}

}
