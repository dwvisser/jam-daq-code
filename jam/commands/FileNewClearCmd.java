package jam.commands;

import jam.data.DataBase;
import jam.global.BroadcastEvent;
import jam.global.SortMode;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *  Command for file menu new also clears
 * 
 * @author Ken Swartz
 *
 */
final class FileNewClearCmd extends AbstractCommand {

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
	
	/*protected void performCommand(int cmdParams) throws CommandException {
		execute(null);
	}*/
	
	/** 
	 * Execute command
	 * @see jam.commands.AbstractCommand#executeStrParam(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens) {
		execute(null);		
	}

}
