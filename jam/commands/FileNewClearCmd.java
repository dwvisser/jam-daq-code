package jam.commands;

import javax.swing.JOptionPane;
import javax.swing.JFrame;
import jam.global.JamStatus;
import jam.global.BroadcastEvent;
import jam.data.DataBase;

/**
 *  Command for file menu new also clears
 * 
 * @author Ken Swartz
 *
 */
public class FileNewClearCmd extends AbstractCommand implements Commandable {

	/**
	 * Excecute command
	 * 
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	public void execute(Object[] cmdParams) {
		
		JFrame frame =status.getFrame();
		
		if (JOptionPane.YES_OPTION==JOptionPane.showConfirmDialog(frame,
		"Erase all current data?","New",JOptionPane.YES_NO_OPTION)){
			status.setSortMode(JamStatus.NO_SORT);
			DataBase.getInstance().clearAllLists();
			broadcaster.broadcast(BroadcastEvent.HISTOGRAM_ADD);
		}
		
	}
	
	public void performCommand(int cmdParams) throws CommandException {
		execute(null);
	}
	/** 
	 * Execute command
	 * @see jam.commands.AbstractCommand#executeStrParam(java.lang.String[])
	 */
	public void executeParse(String[] cmdTokens) {
		execute(null);		

	}

}
