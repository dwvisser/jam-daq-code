package jam.commands;

import java.util.Observable;
import java.util.Observer;

import jam.global.BroadcastEvent;
import jam.global.CommandListenerException;
import jam.global.SortMode;
import jam.io.control.OpenMultipleFiles;

/**
 * Shows the open multiple files dialog
 * 
 * @author Ken Swartz
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class OpenMultipleHDFCmd extends AbstractCommand  implements Observer{

	private OpenMultipleFiles openMultiple;
	
	OpenMultipleHDFCmd(){
		putValue(NAME,"Open Multiple\u2026");
		openMultiple=new OpenMultipleFiles(status.getFrame(), msghdlr);
		//broadcaster.addObserver(this);
	}
	
	protected void execute(Object[] cmdParams) throws CommandException {
		openMultiple.show();

	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens)
			throws CommandListenerException {
		// TODO Auto-generated method stub

	}
	
	public void update(Observable observe, Object obj){
		final BroadcastEvent be=(BroadcastEvent)obj;
		final BroadcastEvent.Command command=be.getCommand();
		if (command==BroadcastEvent.Command.SORT_MODE_CHANGED){
			enable();
		}
	}
	
	private void enable(){
		final SortMode mode=status.getSortMode();
		setEnabled(mode==SortMode.FILE || mode==SortMode.NO_SORT);		
	}
	
}
