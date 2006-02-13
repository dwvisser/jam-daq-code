package jam.commands;

import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.Icon;

import jam.global.BroadcastEvent;
import jam.global.CommandListenerException;
import jam.global.SortMode;
import jam.io.control.OpenMultipleFiles;

/**
 * Shows the open multiple files dialog
 * 
 * @author Ken Swartz
 *
 */
public class OpenMultipleHDFCmd extends AbstractCommand  implements Observer{

	private transient final OpenMultipleFiles openMultiple;
	
	OpenMultipleHDFCmd(){
		super("Open Multiple\u2026");
		openMultiple=new OpenMultipleFiles(STATUS.getFrame());
		final Icon iOpen = loadToolbarIcon("jam/ui/OpenMultiHDF.png");
		putValue(Action.SMALL_ICON, iOpen);
	}
	
	protected void execute(final Object[] cmdParams) throws CommandException {
		openMultiple.show();

	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(final String[] cmdTokens)
			throws CommandListenerException {
		// do nothing
	}
	
	public void update(final Observable observe, final Object obj){
		final BroadcastEvent event=(BroadcastEvent)obj;
		final BroadcastEvent.Command command=event.getCommand();
		if (command==BroadcastEvent.Command.SORT_MODE_CHANGED){
			enable();
		}
	}
	
	private void enable(){
		final SortMode mode=STATUS.getSortMode();
		setEnabled(mode==SortMode.FILE || mode==SortMode.NO_SORT);		
	}
	
}
